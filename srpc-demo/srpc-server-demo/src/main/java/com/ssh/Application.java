package com.ssh;

import com.ssh.bootstrap.ProtocolConfig;
import com.ssh.registry.RegistryFactory;
import com.ssh.bootstrap.SRPCBootstrap;

public class Application {
    public static void main(String[] args) {
        // 启动 Dubbo
        SRPCBootstrap.getInstance()
                // 当前服务的名称
                .application("first-dubbo-provider")
                // 为启动器设置一个注册中心
                .registry(new RegistryFactory("zookeeper://127.0.0.1:2181"))
                // 选择客户端和服务端通信的序列化算法
                .protocol(new ProtocolConfig("dubbo"))
                // 把本服务器提供的接口注册到注册中心
                // 启动netty服务 等待客户端连接
                .start();
    }
}
