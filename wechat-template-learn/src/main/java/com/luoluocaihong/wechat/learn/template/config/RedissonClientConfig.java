package com.luoluocaihong.wechat.learn.template.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Created by xh on 2019/4/25.
 */
@Configuration
public class RedissonClientConfig {

    @Autowired
    RedisProperties redisProperties;


    @Bean
    @ConditionalOnProperty(prefix = "spring.redis.cluster", value = "enabled", matchIfMissing = true, havingValue = "false")
    public RedissonClient redissonClientSingle() {
        Config config = new Config();
        StringBuilder sb = new StringBuilder().append("redis://").append(redisProperties.getHost()).append(":").append(redisProperties.getPort());
        if (StringUtils.isEmpty(redisProperties.getPassword())) {
            config.useSingleServer().setAddress(sb.toString());
        }
        else {
            config.useSingleServer().setAddress(sb.toString()).setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.redis.cluster", value = "enabled", matchIfMissing = false, havingValue = "true")
    public RedissonClient redissonClientCluster() {
        Config config = new Config();
        String[] serverArray = StringUtils.commaDelimitedListToStringArray(redisProperties.getCluster().getNodes());
        String[] nodeAddress = new String[serverArray.length];
        for (int i = 0; i < serverArray.length; i++) {
            String[] ipPortPair = StringUtils.delimitedListToStringArray(serverArray[i], ":");
            StringBuilder sb = new StringBuilder().append("redis://").append(ipPortPair[0].trim()).append(":").append(ipPortPair[1].trim());
            nodeAddress[i] = sb.toString();
        }
        if (StringUtils.isEmpty(redisProperties.getPassword())) {
            config.useClusterServers().addNodeAddress(nodeAddress);
        }
        else {
            config.useClusterServers().addNodeAddress(nodeAddress).setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }
}
