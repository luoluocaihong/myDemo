package com.luoluocaihong.java.learn.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.Date;
import java.util.Map;

/**
 * Created by xh on 2019/10/31.
 */
public class Test {
    public static void main(String[] args) {
        Test t = new Test();
        Person p1 = new Person(1, "luoluocaihong", 12, Boolean.TRUE, new Date());
        Person p2 = new Person(2, "bigFaceCat", 20);

        //测试序列化特性
        t.testSerializable(p1);
        t.testSerializable(p2);

        //测试时间格式
        t.convertDate(p1);
        t.convertDate(p2);

        //测试转换   josnStr  jsonObject  javaBean  Map
        t.convert(p1);
        t.convert(p2);

        //测试Boolean/boolean
        t.testBoolean();
    }


    public void testSerializable(Person p) {
        String jsonStr0 = JSON.toJSONString(p);
        System.out.println(jsonStr0);

        //使用单引号、不设置默认值 、Boolean类型为空则输出false、格式化输出
        String jsonStr1 = JSON.toJSONString(p, SerializerFeature.UseSingleQuotes, SerializerFeature.NotWriteDefaultValue, SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.PrettyFormat);
        System.out.println(jsonStr1);
    }

    public void convertDate(Person p) {
        //使用序列化特性UseISO8601DateFormat
        String jsonStr0 = JSON.toJSONString(p, SerializerFeature.UseISO8601DateFormat);
        System.out.println(jsonStr0);  //"bornDate":"2019-10-31T13:22:12.854+08:00"

        //使用序列化特性WriteDateUseDateFormat,默认格式JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
        String jsonStr1 = JSON.toJSONString(p, SerializerFeature.WriteDateUseDateFormat);
        System.out.println(jsonStr1);  //"bornDate":"2019-10-31 13:22:12"

        //使用序列化特性WriteDateUseDateFormat,指定时间格式
        String jsonStr2 = JSON.toJSONStringWithDateFormat(p, "MM-dd-yyyy h:mm:ss aa");
        System.out.println(jsonStr2); //"bornDate":"10-31-2019 1:22:12 下午"
    }

    public void testBoolean() {
        Student s = new Student(true, false, Boolean.TRUE);
        String jsonStr = JSON.toJSONString(s);
        System.out.println(jsonStr);
    }


    public void convert(Person p) {
        System.out.println("==========object(javabean) to string==========");
        String jsonStr = JSON.toJSONString(p);
        System.out.println(jsonStr);

        System.out.println("==========object(javabean) to map==========");
        Map<String, Object> map1 = (Map<String, Object>) JSON.toJSON(p);
        map1.forEach((key, value) -> {
            System.out.println("key=" + key + ",value=" + value);
        });

        System.out.println("==========string to map ==========");
        Map<String, Object> map2 = (Map<String, Object>) JSON.parse(jsonStr);
        map2.forEach((key, value) -> {
            System.out.println("key=" + key + ",value=" + value);
        });

        System.out.println("==========string to map 指定目标类型 ==========");
        Map<String, String> map3 = (Map<String, String>) JSON.parseObject(jsonStr, new TypeReference<Map<String, String> >() { });
        map3.forEach((key, value) -> {
            System.out.println("key=" + key + ",value=" + value);
        });

        System.out.println("==========map to String ==========");
        //map3中value都被转成了String
        String jsonStr1 = JSON.toJSONString(map1);
        String jsonStr3 = JSON.toJSONString(map3);
        System.out.println(jsonStr1);
        System.out.println(jsonStr3);

        System.out.println("==========String to object(javabean)==========");
        //推荐这种方式直接转对象，不推荐下面先转JSONObject再转JavaBean
        Person person = JSON.parseObject(jsonStr1, Person.class);
        System.out.println(person.toString());

        Object obj1 = JSON.parse(jsonStr1);
        Object obj3 = JSON.parse(jsonStr3);

        JSONObject jsonObj1 = JSON.parseObject(jsonStr1);
        JSONObject jsonObj3 = JSON.parseObject(jsonStr3);

        Person p1 = JSON.toJavaObject(jsonObj1, Person.class);
        System.out.println(p1.toString());
        /**
         * 下面注释掉的这个方法会报错
         * JavaBeanDeserializer 报错，IllegalArgumentException: argument type mismatch  因为类型都被转成了String
         * 如果非要这么做,比如对于int类型的age字段,利用方法的多态,在Person.java中写一个String类型的setAge方法，反序列化的时候会自动调用这个String类型入参的set方法
         * 其他非String类型类似处理即可。
         * public void setAge(String age) { this.age = Integer.valueOf(age); }
         */
        //Person p3 = JSON.toJavaObject(jsonObj3, Person.class);
        //System.out.println(p3.toString());
    }
}
