package com.ssh;

import com.ssh.Imp.HelloServiceImp;
import com.ssh.bootstrap.ProtocolConfig;
import com.ssh.bootstrap.RegistryConfig;
import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.bootstrap.ServiceConfig;
import com.ssh.interfaces.HelloService;

public class Application {
    public static void main(String[] args) {


        // 封装所有当前服务器向外提供的接口和对象
        ServiceConfig<HelloService> service = new ServiceConfig<>();
        service.setInterface(HelloService.class);
        service.setRef(new HelloServiceImp());

        // 启动 Dubbo
        SRPCBootstrap.getInstance()
                // 当前服务的名称
                .application("first-dubbo-provider")
                // 为启动器设置一个注册中心
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                // 选择客户端和服务端通信的序列化算法
                .protocol(new ProtocolConfig("dubbo"))
                // 把本服务器提供的接口注册到注册中心
                .service(service)
                // 启动netty服务 等待客户端连接
                .start();


    }
}
