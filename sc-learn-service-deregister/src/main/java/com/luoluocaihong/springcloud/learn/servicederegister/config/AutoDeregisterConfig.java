//package com.luoluocaihong.springcloud.learn.servicederegister.config;
//
//import com.ecwid.consul.v1.ConsulClient;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
//import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Configuration;
//
///**
// * Created by xh on 2019/2/14.
// */
//@Configuration
//@Slf4j
//public class AutoDeregisterConfig implements DisposableBean {
//    @Autowired
//    ApplicationContext ctx;
//    @Autowired
//    private ConsulClient consulClient;
//    @Autowired
//    private ConsulDiscoveryProperties properties;
//    @Override
//    public void destroy() throws Exception {
//        try {
//            //获取当前微服务的实例ID
//            String instanceId = ConsulAutoRegistration.getInstanceId(properties, ctx);
//            //注销微服务实例
//            consulClient.agentServiceDeregister(instanceId);
//        }
//        catch(Exception e) {
//            //注销当前微服务实例不影响应用程序关闭
//            log.error("error deregister current service", e);
//        }
//    }
//}
