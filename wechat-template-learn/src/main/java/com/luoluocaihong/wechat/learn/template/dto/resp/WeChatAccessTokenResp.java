package com.luoluocaihong.wechat.learn.template.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xh on 2019/4/4.
 */
@Data
public class WeChatAccessTokenResp implements Serializable{
    private String accessToken;
    private Long expiresIn;
}
