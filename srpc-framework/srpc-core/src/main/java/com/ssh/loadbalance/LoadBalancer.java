package com.ssh.loadbalance;

public interface LoadBalancer {

    String getServerHost(String interfaceName);
}