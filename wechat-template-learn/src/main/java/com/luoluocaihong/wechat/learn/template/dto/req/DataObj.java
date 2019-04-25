package com.luoluocaihong.wechat.learn.template.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xh on 2019/4/25.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataObj implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模板数据值
     */
    private String value;

    /**
     * 模板内容字体颜色，不填默认为黑色
     */
    private String color;

    public DataObj(String value) {
        this.value = value;
    }
}
