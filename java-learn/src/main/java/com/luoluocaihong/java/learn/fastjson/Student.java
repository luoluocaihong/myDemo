package com.luoluocaihong.java.learn.fastjson;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by xh on 2019/10/31.
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Student implements Serializable {
    private boolean isHealth;
    @JSONField(name = "isSick")
    private boolean isSick;
    private Boolean isHappy;
}
