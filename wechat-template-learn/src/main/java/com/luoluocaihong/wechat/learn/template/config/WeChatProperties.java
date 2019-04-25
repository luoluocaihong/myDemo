package com.luoluocaihong.wechat.learn.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by xh on 2019/4/25.
 */
@Configuration
@ConfigurationProperties(prefix = "wechat")
@Data
public class WeChatProperties {
    private String appId;
    private String secret;
    private String domain;
}
