package com.ssh.bootstrap;

import com.ssh.exceptions.RegistryDiscoveryException;
import com.ssh.registry.Registry;
import com.ssh.registry.ZookeeperRegistry;

public class RegistryConfig {

    private String connectString;

    public RegistryConfig(String connectString){
        this.connectString = connectString;
    }

    /**
     * 工厂方法 获取注册中心
     * @return
     */
    public Registry getRegistry(){
        String registryType = getRegistryType();

        if(registryType.equals("zookeeper")){
            return new ZookeeperRegistry();
        }

        throw new RegistryDiscoveryException();
    }

    private String getRegistryType(){
        String[] split = connectString.split("://");
        if(split.length == 2){
            return split[0].toLowerCase().trim();
        }
        throw new RegistryDiscoveryException();

    }

    private String getRegistryAddress(){
        String[] split = connectString.split("://");
        if(split.length == 2){
            return split[1].trim();
        }
        throw new RegistryDiscoveryException();
    }

}
