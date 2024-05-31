package com.ssh.bootstrap;


import com.ssh.registry.Registry;
import com.ssh.registry.ZookeeperRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SRPCBootstrap {
    // 单例模式 每个服务只允许拥有一个启动器实例
    public static SRPCBootstrap srpcBootstrap = new SRPCBootstrap();

    // 服务名称
    private String appName;


    private ProtocolConfig protocolConfig;

    private Registry registry;

    private static Map<String, ServiceConfig<?>> serverMap = new ConcurrentHashMap<>();

    private SRPCBootstrap(){

    }

    public static SRPCBootstrap getInstance(){
        return srpcBootstrap;
    }

    /**
     * 配置服务提供者的名称
     * @param appName
     * @return
     */
    public SRPCBootstrap application(String appName) {
        this.appName = appName;
        return srpcBootstrap;
    }


    /**
     * j连接zookeeper
     * @param registryConfig
     * @return
     */
    public SRPCBootstrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
        return srpcBootstrap;
    }

    /**
     * 协议名称
     * @param protocolConfig
     * @return
     */
    public SRPCBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        return srpcBootstrap;
    }

    /**
     * 注册服务 - 在zookeeper中创建节点
     * @param service
     * @return
     */
    public SRPCBootstrap service(ServiceConfig<?> service) {
        registry.publish(service);
        serverMap.put(service.getInterfaceName(), service);
        return srpcBootstrap;
    }

    public SRPCBootstrap service(List<ServiceConfig<?>> services) {
        return srpcBootstrap;
    }


    /**
     * 启动netty 等待服务调用方的请求
     */
    public void start() {

    }

    /**
     * 调用者获取目标接口的代理类
     * @param referenceConfig
     * @return
     */
    public SRPCBootstrap reference(ReferenceConfig<?> referenceConfig) {

        ClassLoader classLoader = referenceConfig.getInterfaceConsumed().getClassLoader();
        Class<?>[] interfaces = new Class[]{referenceConfig.getInterfaceConsumed()};

        Object reference = Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 1. 发现服务：从注册中心用负责均衡算法选择一个服务
                String serverHost = registry.discover(referenceConfig.getInterfaceConsumed().getName());
                if (serverHost == "") {
                    throw new RuntimeException();
                }
                String ip = serverHost.split(":")[0];
                int port = Integer.valueOf(serverHost.split(":")[1]);
                System.out.println("connect" + ip + port);
                // 2。用netty与该服务建立连接，获取结果


                return null;
            }
        });
        referenceConfig.setReference(reference);

        return srpcBootstrap;
    }
}
