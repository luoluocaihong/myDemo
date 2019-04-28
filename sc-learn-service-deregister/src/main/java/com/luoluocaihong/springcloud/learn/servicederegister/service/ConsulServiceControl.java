package com.luoluocaihong.springcloud.learn.servicederegister.service;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Check;
import com.ecwid.consul.v1.agent.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xh on 2019/2/13.
 * 注销不可用服务的实例
 */
@RestController
public class ConsulServiceControl {

    @Autowired
    private ConsulClient consulClient;

    //consul集群地址, ip1:port1,ip2,ip3:port3.... 端口不配置默认8500
    @Value("${luoluocaihong.spring.cloud.consul.cluster.nodes}")
    private String consulAddress;

    /**
     * 注销不可用服务实例
     * 根据配置的consul集群地址,循环处理每个consul服务器,根据Agent提供的HTTP API(https://www.consul.io/api/agent.html)来处理
     * List Checks:  /agent/checks  注册到当前agent下的所有微服务的检测信息,通过这个端点可以检测不可用微服务,Status不为passing状态的即为不可用的微服务
     * Deregister Service: /agent/service/deregister/{service_id}  注销当前agent下的某个微服务实例
     * @return
     */
    @GetMapping("/deregister")
    public String deregister() {
        Map<String, String> consulAddMap = this.getConsulAddress();
        consulAddMap.forEach((host,port) -> {
            ConsulClient client = new ConsulClient(host, Integer.valueOf(port));
            deregister(client);
        });

//        //通过List Members端点(GET /agent/members) 查询所有的agent
//        Response<List<Member>>  agentResp = consulClient.getAgentMembers();
//        if (agentResp != null) {
//            List<Member> members = agentResp.getValue();
//            if (!CollectionUtils.isEmpty(members)) {
//                for (Member member: members) {
//                    ConsulClient client = new ConsulClient(member.getAddress());
//                    deregister(client);
//                }
//            }
//        }
        return "OK";
    }


    private void deregister(ConsulClient client) {
        Response<Map<String, Check>> checkResp = client.getAgentChecks();
        if (checkResp != null) {
            Map<String, Check> serviceCheckMap = checkResp.getValue();
            if (!CollectionUtils.isEmpty(serviceCheckMap)) {
                for (Check check : serviceCheckMap.values()) {
                    if (!check.getStatus().equals(Check.CheckStatus.PASSING)) {
                        client.agentServiceDeregister(check.getServiceId());
                    }
                }
            }
        }
    }


    private Map<String, String> getConsulAddress() {
        Map<String, String> addressMap = new HashMap<String, String>();
        String[] addressArray = consulAddress.split(",");
        for (String address : addressArray) {
            String port = "8500";
            String[] ipPortArray = address.split(":");
            if (ipPortArray.length == 2) {
                port = ipPortArray[1];
            }
            addressMap.put(ipPortArray[0], port);
        }
        return addressMap;
    }
}
