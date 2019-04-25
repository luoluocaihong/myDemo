package com.luoluocaihong.wechat.learn.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedisProperties {
    private int    expireSeconds;
    private String  host;
    private int port;
    private String password;
    private int    commandTimeout;
    private RedisProperties.Cluster cluster;

    @Data
    public static class Cluster {
        private String nodes;
    }
}
