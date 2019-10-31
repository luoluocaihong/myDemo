package com.luoluocaihong.java.learn.fastjson;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xh on 2019/10/31.
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Person implements Serializable {
    private int number;
    private String name;
    private int age;
    private Boolean childFlag;
    //@JSONField(format = "MM-dd-yyyy h:mm:ss aa")
    private Date bornDate;

    public Person(int number, String name, int age) {
        this.number = number;
        this.name = name;
        this.age = age;
    }
}
