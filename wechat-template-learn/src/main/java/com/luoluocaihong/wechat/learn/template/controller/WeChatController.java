package com.luoluocaihong.wechat.learn.template.controller;

import com.luoluocaihong.wechat.learn.template.dto.resp.WeChatTemplateResp;
import com.luoluocaihong.wechat.learn.template.manager.WeChatManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by xh on 2019/4/25.
 */
@RestController
@Slf4j
@RequestMapping("/luoluocaihong/wechat")
public class WeChatController {

    @Autowired
    private WeChatManager weChatManager;

    /**
     * 获取微信的通知模板列表
     * @return List<WeChatTemplateResp>
     * @throws Exception </>
     */
    @GetMapping("/template")
    public List<WeChatTemplateResp> getWeChatTemplate() throws Exception{
        log.info("WeChatController.getWeChatTemplate start");
        List<WeChatTemplateResp> list = weChatManager.getWeChatTemplate();
        log.info("WeChatController.getWeChatTemplate end");
        return list;
    }


    /**
     * 发送模板通知消息
     * @return String
     * @throws Exception </>
     */
    @PostMapping("/send")
    public String sendWechatTemplateMessage() throws Exception {
        log.info("WeChatController.sendWechatTemplateMessage start");
        String msgId = weChatManager.sendWechatTemplateMessage();
        log.info("WeChatController.sendWechatTemplateMessage end");
        return msgId;
    }
}
