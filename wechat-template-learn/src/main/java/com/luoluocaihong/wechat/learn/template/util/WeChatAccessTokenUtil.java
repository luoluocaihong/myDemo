package com.luoluocaihong.wechat.learn.template.util;


import com.alibaba.fastjson.JSONObject;
import com.luoluocaihong.wechat.learn.template.config.WeChatProperties;
import com.luoluocaihong.wechat.learn.template.dto.resp.WeChatAccessTokenResp;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
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
 *  2.该接口返回的AccessToken有效期目前为7200s
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

    private static boolean initCacheFlag = false;

    static {
        restTemplate = SpringContext.getBean(RestTemplate.class);
        weChatProperties = SpringContext.getBean(WeChatProperties.class);
        redissonClient = SpringContext.getBean(RedissonClient.class);
    }

    /**
     *  从redis中获取
     * @return
     */
    public static String getAccessToken() throws Exception {
        log.info("WeChatAccessTokenUtil.getAccessToken start");
        //优先从redis中获取
        RBucket<String> accessTokenCache = redissonClient.getBucket("ACCESSTOKEN");
        //redis中存在，返回redis中的ACCESS_TOKEN
        //如果是首次过来的请求 将redis中的ACCESS_TOKEN值写入共享变量accessToken
        if (accessTokenCache != null && !StringUtils.isEmpty(accessTokenCache.get())) {
            if (!initCacheFlag) {
                accessToken = accessTokenCache.get();
                initCacheFlag = true;
            }
            return accessTokenCache.get();
        }
        //redis中不存在,则调用微信接口查询accessToken,利用分布式锁控制调用一次.
        // 其他的请求，直接返回共享变量accessToken
        // 如果共享变量accessToken为空，则等待调用微信接口的线程查询ACCESS_TOKEN并写入共享变量accessToken;
        else {
            Lock lock = redissonClient.getLock("lock-AccessToken");

            //同一个进程内的多个线程,当一个线程获得分布式锁，设置了共享变量callFlag = false，那么其他线程会退出while循环；
            //如果共享变量accessToken有值则直接使用该共享变量;如果共享变量accessToken为空，则等待获得锁的线程设置共享变量
            //不同进程的多个线程 只有一个线程能获得分布式锁

            boolean innerFlag = true;  //线程私有的变量, 用这个标志退出循环
            //callFlag 线程共享的变量，用于当一个线程获取锁时，通知其他线程跳出循环
            while (innerFlag && callFlag) {
                if (lock.tryLock()) {  //默认30000ms
                    try {
                        latch = new CountDownLatch(1);
                        callFlag = false;

                        //获取锁之后，首先查询redis ，如果redis中存在则不再需要调用微信接口了  这里是考虑分布式的场景
                        accessTokenCache = redissonClient.getBucket("ACCESSTOKEN");
                        if (accessTokenCache != null && !StringUtils.isEmpty(accessTokenCache.get())) {
                            accessToken = accessTokenCache.get();
                            initCacheFlag = true;
                        }
                        else {
                            //调用微信的接口查询ACCESS_TOKEN
                            WeChatAccessTokenResp accessTokenResp =  getAccessTokenFromWechat();
                            accessToken = accessTokenResp.getAccessToken();
                            latch.countDown();
                            Long expire = accessTokenResp.getExpiresIn();
                            if (expire > 200) {
                                expire -= 200;
                            }
                            accessTokenCache.set(accessToken, expire, TimeUnit.SECONDS);
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
