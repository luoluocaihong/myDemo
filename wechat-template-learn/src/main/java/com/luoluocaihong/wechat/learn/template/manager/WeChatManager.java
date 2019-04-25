package com.luoluocaihong.wechat.learn.template.manager;

import com.alibaba.fastjson.JSONObject;
import com.luoluocaihong.wechat.learn.template.dto.resp.WeChatTemplateListResp;
import com.luoluocaihong.wechat.learn.template.dto.resp.WeChatTemplateResp;
import com.luoluocaihong.wechat.learn.template.dto.resp.WeChatTmplSendResp;
import com.luoluocaihong.wechat.learn.template.util.WeChatAPIUrlUtil;
import com.luoluocaihong.wechat.learn.template.util.WeChatAccessTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xh on 2019/4/25.
 */
@Service
@Slf4j
public class WeChatManager {

    @Autowired
    private RestTemplate restTemplate;

    public List<WeChatTemplateResp> getWeChatTemplate() throws Exception {
        log.info("WeChatManager.getWeChatTemplate start");

        List<WeChatTemplateResp> list = null;
        Map<String, String> requestParam = new HashMap<String, String>(1);
        requestParam.put("access_token", WeChatAccessTokenUtil.getAccessToken());
        String url = WeChatAPIUrlUtil.buildRequestParamUrl("/cgi-bin/template/get_all_private_template", requestParam);
        ResponseEntity<String> responseEntity = null;

        try {
            responseEntity =  restTemplate.getForEntity(url, String.class);
        }
        catch (RestClientException e) {
            log.error("call wechat get_all_private_template api error, {}", e);
            throw new Exception("CALL_WECHAT_API_ERROR", e);
        }

        if (HttpStatus.OK != responseEntity.getStatusCode()) {
            throw new Exception("CALL_WECHAT_API_ERROR");
        }

        JSONObject obj = JSONObject.parseObject(responseEntity.getBody());

        //{"errcode":40001,"errmsg":"invalid credential, access_token is invalid or not latest hint: [9wduFA0441vr45!]"}
        String errcode = obj.getString("errcode");
        String errmsg = obj.getString("errmsg");
        //这里先直接抛出返回的异常吧.
        if (!StringUtils.isEmpty(errcode)) {
            throw new Exception(errmsg);
        }

        WeChatTemplateListResp weChatTemplateListResp = JSONObject.toJavaObject(obj, WeChatTemplateListResp.class);
        list = weChatTemplateListResp.getTemplateList();
        if (CollectionUtils.isEmpty(list)) {
            list = new ArrayList<WeChatTemplateResp>();
        }

        log.info("WeChatManager.getWeChatTemplate end");
        return list;
    }




    /**
     *  https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1433751277
     * @throws Exception
     */
    public String sendWechatTemplateMessage() throws Exception {
        log.info("WeChatManager.sendWechatTemplateMessage start");
        String msgId = null;

        //查询待发送通知  这里是demo 就直接写死咯
        String sendContent = "{\"touser\":\"o1xYF1f0usBHSUDZg-k0eI6wudZ4\",\"url\":\"www.baidu.com\",\"template_id\":\"NTGqIwifErpioNS1m5bX6M1DtdQAusj0q4bZMFBmRw8\",\"data\":{\"state\":{\"value\":\"到达代收点\",\"color\":\"#CC551A\"},\"deliverTime\":{\"value\":\"2019-04-25\",\"color\":\"#EE5518\"}}}\n";

        //推送消息给微信公众号平台
        Map<String, String> requestParam = new HashMap<String, String>(1);
        requestParam.put("access_token", WeChatAccessTokenUtil.getAccessToken());
        String url = WeChatAPIUrlUtil.buildRequestParamUrl("/cgi-bin/message/template/send", requestParam);

        if (log.isDebugEnabled()) {
            log.debug("send message content is {}", sendContent);
        }

        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity =  restTemplate.postForEntity(url, sendContent, String.class);
        }
        catch (RestClientException e) {
            log.error("call wechat send template message api error, {}", e);
            throw new Exception("CALL_WECHAT_API_ERROR", e);
        }

        JSONObject obj = JSONObject.parseObject(responseEntity.getBody());
        WeChatTmplSendResp weChatTmplSendResp = JSONObject.toJavaObject(obj, WeChatTmplSendResp.class);

        if (weChatTmplSendResp != null) {

            if (!"0".equals(weChatTmplSendResp.getErrcode())) {
                //失败直接抛异常咯
                throw new Exception(weChatTmplSendResp.getErrmsg());
            }
            else {
                msgId = weChatTmplSendResp.getMsgid();
            }
        }
        log.info("WeChatManager.sendWechatTemplateMessage end");
        return msgId;
    }

}
