package com.luoluocaihong.springcloud.learn.servicederegister;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
import org.springframework.context.ApplicationContext;

/**
 * Created by xh on 2019/2/13.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceApplication {
    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = SpringApplication.run(ServiceApplication.class, args);

        //不需要多此一举,微服务优雅停机时是会自动注销的(org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration.destroy)
        //而且这样做可能此时ctx已经被关闭了。 因为Spring应用程序启动的时候已经注册了一个钩子(org.springframework.boot.SpringApplication.refreshContext中注册的,用于关闭应用上下文)
        //如果consul客户端没有做自动注销,可以参考com.luoluocaihong.springcloud.learn.servicederegister.config.AutoDeregisterConfig实现（实现DisposableBean接口或者使用@PreDestroy注解）
//        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ConsulClient consulClient = ctx.getBean(ConsulClient.class);
//                ConsulDiscoveryProperties properties = ctx.getBean(ConsulDiscoveryProperties.class);
//                //获取当前微服务的实例ID
//                String instanceId = ConsulAutoRegistration.getInstanceId(properties, ctx);
//                //注销微服务实例
//                consulClient.agentServiceDeregister(instanceId);
//            }
//        }));

//        //for test
//        Thread.sleep(10000);
//        System.exit(1);
    }
}
