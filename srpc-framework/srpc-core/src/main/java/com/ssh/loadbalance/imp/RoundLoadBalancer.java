package com.ssh.loadbalance.imp;

import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.exceptions.RegistryDiscoveryException;
import com.ssh.loadbalance.AbstractLoadBalancer;
import com.ssh.loadbalance.Selector;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// 轮询负载均衡
@Slf4j
public class RoundLoadBalancer extends AbstractLoadBalancer {

    @Override
    public Selector getNewSelector(String interfaceName) {
        return new RoundSelector(SRPCBootstrap.getInstance().getRegistry().discover(interfaceName));
    }




    public static class RoundSelector implements Selector{

        // 保存提供当前服务的机器的地址
        private List<String> serverList;

        private AtomicInteger index;

        public RoundSelector(List<String> serverList){
            this.serverList = serverList;
            index = new AtomicInteger(0);
        }
        @Override
        public String getNext() {
            if(serverList.isEmpty()){
                log.error("当前服务没有提供方！");
                throw new RegistryDiscoveryException();
            }
            String res = serverList.get(index.getAndIncrement());
            if(index.get() == serverList.size()){
                index.set(0);
            }
            return res;

        }
    }
}
