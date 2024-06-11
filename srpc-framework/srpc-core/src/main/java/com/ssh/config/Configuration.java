package com.ssh.config;

import com.ssh.network.serialize.SerializerFactory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Configuration {
    // rpc服务方监听的端口号
    private int port = 8082;

    // 服务方netty工作线程池 线程数量
    private int nettyWorkerNum = 10;

    // 服务方包扫描路径
    private String scanPath = "com.ssh.Imp";

    // 服务名称
    private String appName;



    // 序列化方式
    private int serializerType = SerializerFactory.JSON_SERIALIZER;
}
