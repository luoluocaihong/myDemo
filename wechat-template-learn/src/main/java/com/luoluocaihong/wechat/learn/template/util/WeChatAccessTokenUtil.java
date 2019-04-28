package com.luoluocaihong.wechat.learn.template.util;


import com.alibaba.fastjson.JSONObject;
import com.luoluocaihong.wechat.learn.template.config.WeChatProperties;
import com.luoluocaihong.wechat.learn.template.dto.resp.WeChatAccessTokenResp;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RBucketAsync;
import org.redisson.api.RedissonClient;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 *  获取微信的AccessToken
 *
 *  微信提供了一个rest接口，根据appid和secret 更新并返回 AccessToken；
 *  微信的这个AccessToken有几点需要注意：
 *  1.每次调用该接口，会返回新的AccessToken，老的AccessToken会有5分钟的存活期
 *  2.微信端该接口返回的AccessToken有效期目前为7200s
 * Created by xh on 2019/4/25.
 */
@Slf4j
public class WeChatAccessTokenUtil {

    private static RestTemplate restTemplate;
    private static WeChatProperties weChatProperties;
    private static RedissonClient redissonClient;

    private volatile static String accessToken;
    private volatile static boolean callFlag = true;
    private static CountDownLatch latch = new CountDownLatch(1);

    private static boolean initFlag = false;

    private static final String LOCK_KEY = "lock-AccessToken";
    private static final String ACCESSTOKEN = "ACCESSTOKEN";
    private static final String ACCESSTOKEN_LASTUPDATE = "ACCESSTOKEN_LASTUPDATE";

    static {
        restTemplate = SpringContext.getBean(RestTemplate.class);
        weChatProperties = SpringContext.getBean(WeChatProperties.class);
        redissonClient = SpringContext.getBean(RedissonClient.class);
    }

    /**
     *  获取AccessToken
     * @return String
     */
    public static String getAccessToken() throws Exception {
        log.info("WeChatAccessTokenUtil.getAccessToken start");
        //优先从redis中获取
        RBucket<String> accessTokenCache = redissonClient.getBucket(ACCESSTOKEN);
        //redis中存在，返回redis中的ACCESS_TOKEN；
        // 同时如果accessToken未初始化，则将redis中的ACCESS_TOKEN值写入共享变量accessToken  这个不需要考虑并发问题，重复设置也没事
        if (accessTokenCache != null && !StringUtils.isEmpty(accessTokenCache.get())) {
            if (!initFlag) {
                accessToken = accessTokenCache.get();
                initFlag = true;
            }
            return accessTokenCache.get();
        }
        //redis中不存在,那么就需要让一个线程A去调用微信接口查询accessToken并刷入redis;
        //其他线程使用老的accessToken(即共享变量accessToken),如果存在的话；  如果老的accessToken不存在则等待线程A的通知；
        //老的accessToken有5分钟的存活期，所以这里使用一个缓存key并设置失效时间来控制老的accessToken是否可用，具体方式是：
        //在将accessToken刷入redis时,同时刷入另一个key:ACCESSTOKEN_LASTUPDATE,并控制失效时间比accessToken多五分钟，当缓存失效时，我们判断缓存ACCESSTOKEN_LASTUPDATE是否存在，如果不存在则表示老的accessToken失效不可用了，这时候清空共享变量accessToken.
        else {
            Lock lock = redissonClient.getLock(LOCK_KEY);

            //所有线程循环尝试获取分布式锁,只有一个线程X 会获得锁，获得锁的线程X 首先设置计数器latch为1，然后判断是否存在缓存ACCESSTOKEN_LASTUPDATE，不存在表示老的accessToken已经过了5分钟的存活期，那么就清空共享变量accessToken；
            //然后线程X 设置共享变量callFlag = false，那么其他线程会退出while循环；
            //对于线程X，因为要考虑分布式的场景，所以首选再次去redis中查询accessToken，查询到则更新共享变量accessToken；查询不到则调rest接口获取accessToken；
            //对于其他退出循环的线程,如果共享变量accessToken有值，表示还在存活期内，则使用老的accessToken返回给业务使用；如果accessToken为空，则需要等待线程X 的通知；
            boolean innerFlag = true;  //线程私有的变量, 获得锁的线程通过修改这个标志退出循环
            //callFlag 线程共享的变量，用于当一个线程获取锁时，通知其他线程跳出循环
            while (innerFlag && callFlag) {
                if (lock.tryLock()) {  //默认30000ms
                    try {
                        latch = new CountDownLatch(1);

                        RBatch batch = redissonClient.createBatch();
                        RBucketAsync accessTokenLastUpdateAsync = batch.getBucket(ACCESSTOKEN_LASTUPDATE);
                        RBucketAsync accessTokenAsync = batch.getBucket(ACCESSTOKEN);
                        batch.execute();

                        //判断老的accessToken是否可用
                        if (accessTokenLastUpdateAsync.getAsync().get() == null) {
                            accessToken = null;
                        }
                        //获取锁之后，首先查询redis ，如果redis中存在则不再需要调用微信接口了  这里是考虑分布式的场景
                        if (accessTokenAsync.getAsync().get() != null) {
                            accessToken = accessTokenCache.get();
                        }
                        callFlag = false;

                        if (accessTokenAsync.getAsync().get() == null)  {
                            //调用微信的接口查询ACCESS_TOKEN
                            WeChatAccessTokenResp accessTokenResp =  getAccessTokenFromWechat();
                            accessToken = accessTokenResp.getAccessToken();
                            //共享变量accessToken已经设置新值为可用的accessToken，通知其他线程
                            latch.countDown();
                            Long expire = accessTokenResp.getExpiresIn();
                            if (expire > 200) {
                                expire -= 200;
                            }

                            //批量更新缓存
                            batch = redissonClient.createBatch();
                            batch.getBucket(ACCESSTOKEN).setAsync(accessToken, expire, TimeUnit.SECONDS);
                            batch.getBucket(ACCESSTOKEN_LASTUPDATE).setAsync(System.currentTimeMillis(), expire + 300, TimeUnit.SECONDS);
                            batch.execute();
                        }
                    }
                    finally {
                        innerFlag = false;
                        //还原
                        callFlag = true;
                        lock.unlock();
                    }
                }
            }

            if (StringUtils.isEmpty(accessToken)) {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("WeChatAccessTokenUtil.getAccessToken end");
        return accessToken;
    }


    /**
     * 调用微信的rest接口查询AccessToken
     * @return WeChatAccessTokenResp
     * @throws Exception </>
     */
    public static WeChatAccessTokenResp getAccessTokenFromWechat() throws Exception {
        log.info("WeChatAccessTokenUtil.getAccessTokenFromWechat start");
        Map<String, String> requestParam = new HashMap<String, String>(3);
        requestParam.put("grant_type", "client_credential");
        requestParam.put("appid", weChatProperties.getAppId());
        requestParam.put("secret", weChatProperties.getSecret());
        String url = WeChatAPIUrlUtil.buildRequestParamUrl("/cgi-bin/token", requestParam);

        String accessTokenStr = "";
        try {
            accessTokenStr = restTemplate.getForObject(url, String.class);
        }
        catch(RestClientException e) {
            log.error("call wechat token api error, {}", e);
            throw new Exception("CALL_WECHAT_API_ERROR", e);
        }
        JSONObject obj = JSONObject.parseObject(accessTokenStr);
        WeChatAccessTokenResp accessTokenResp = JSONObject.toJavaObject(obj, WeChatAccessTokenResp.class);
        log.info("WeChatAccessTokenUtil.getAccessTokenFromWechat end");
        return accessTokenResp;
    }
}
