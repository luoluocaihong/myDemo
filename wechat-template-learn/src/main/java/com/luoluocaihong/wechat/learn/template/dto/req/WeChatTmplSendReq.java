package com.luoluocaihong.wechat.learn.template.dto.req;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by xh on 2019/4/12.
 */
@Data
public class WeChatTmplSendReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 接收者openid
     */
    private String touser;

    /**
     * 微信模板ID
     */
    private String templateId;

    /**
     *  模板跳转链接
     */
    private String url;

    /**
     * 跳小程序所需数据，不需跳小程序可不用传该数据
     */
    private MiniProgramReq miniprogramReq;

    /**
     * 模板数据
     */
    private Map<String, DataObj> data;
}
