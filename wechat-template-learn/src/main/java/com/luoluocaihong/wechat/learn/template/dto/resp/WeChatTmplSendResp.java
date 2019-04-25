package com.luoluocaihong.wechat.learn.template.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xh on 2019/4/25.
 */
@Data
public class WeChatTmplSendResp implements Serializable {
    private static final long serialVersionUID = 1L;

    private String errcode;
    private String errmsg;
    private String msgid;
}
