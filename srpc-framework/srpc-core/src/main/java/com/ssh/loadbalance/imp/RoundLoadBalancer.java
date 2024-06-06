package com.ssh.loadbalance.imp;

import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.exceptions.RegistryDiscoveryException;
import com.ssh.loadbalance.AbstractLoadBalancer;
import com.ssh.loadbalance.Selector;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 每个服务只有一个loadBalancer实例
 */
public class RoundLoadBalancer extends AbstractLoadBalancer {

    @Override
    public Selector getNewSelector(String interfaceName) {
        return new RoundSelector(SRPCBootstrap.getInstance().getRegistry().discover(interfaceName));
    }




    public static class RoundSelector implements Selector{

        private List<String> serverList;

        private AtomicInteger index;

        public RoundSelector(List<String> serverList){
            this.serverList = serverList;
            index = new AtomicInteger(0);
        }
        @Override
        public String getNext() {
            if(serverList.isEmpty()){
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
