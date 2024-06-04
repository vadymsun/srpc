package com.ssh.registry;

import com.ssh.bootstrap.ServiceConfig;
import org.apache.zookeeper.server.ServerConfig;

import java.util.List;

public interface Registry {

    /**
     * 服务发布到注册中心
     * @param serviceConfig
     */
     void publish(ServiceConfig serviceConfig);


    /**
     * 服务调用方发现服务
     * @param interfaceName : 接口的全限定名
     * @return
     */
     List<String> discover(String interfaceName);

}
