package com.ssh;

import com.ssh.Imp.HelloServiceImp;
import com.ssh.bootstrap.ProtocolConfig;
import com.ssh.bootstrap.RegistryConfig;
import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.bootstrap.ServiceConfig;
import com.ssh.interfaces.HelloService;

public class Application {
    public static void main(String[] args) {

        ServiceConfig<HelloService> service = new ServiceConfig<>();
        service.setInterface(HelloService.class);
        service.setRef(new HelloServiceImp());

        // 启动 Dubbo
        SRPCBootstrap.getInstance()
                .application("first-dubbo-provider")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .protocol(new ProtocolConfig("dubbo"))
                .service(service)
                .start();


    }
}
