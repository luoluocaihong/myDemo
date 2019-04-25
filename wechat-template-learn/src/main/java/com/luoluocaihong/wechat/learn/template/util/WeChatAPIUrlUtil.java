package com.luoluocaihong.wechat.learn.template.util;


import com.luoluocaihong.wechat.learn.template.config.WeChatProperties;
import org.apache.commons.collections.MapUtils;

import java.util.Map;

/**
 * Created by xh on 2019/4/25.
 */
public class WeChatAPIUrlUtil {

    private static WeChatProperties weChatProperties;

    static {
        weChatProperties = SpringContext.getBean(WeChatProperties.class);
    }

    public static String buildRequestParamUrl(String apiPath, Map<String, String> requestParam) {
        StringBuilder sb =  new StringBuilder().append(weChatProperties.getDomain()).append(apiPath);

        if (!MapUtils.isEmpty(requestParam)) {
            sb.append("?");

            requestParam.forEach((key, value) -> {
                sb.append(key).append("=").append(value).append("&");
            });
            sb.deleteCharAt(sb.lastIndexOf("&"));
        }
        return sb.toString();
    }
}
