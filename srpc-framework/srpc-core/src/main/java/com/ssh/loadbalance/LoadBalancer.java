package com.ssh.loadbalance;

public interface LoadBalancer {

    /**
     * 通过负载均衡策略获取服务方的地址
     * @param interfaceName
     * @return
     */
    String getServerHost(String interfaceName);



    void reLoadBalance(String interfaceName);
}
