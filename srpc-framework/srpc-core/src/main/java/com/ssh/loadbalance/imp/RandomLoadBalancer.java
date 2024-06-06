package com.ssh.loadbalance.imp;

import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.exceptions.RegistryDiscoveryException;
import com.ssh.loadbalance.AbstractLoadBalancer;
import com.ssh.loadbalance.Selector;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer extends AbstractLoadBalancer {
    @Override
    public Selector getNewSelector(String interfaceName) {
        return new RandomSelector(SRPCBootstrap.getInstance().getRegistry().discover(interfaceName));
    }



    public static class RandomSelector implements Selector{

        private List<String> serverList;

        public RandomSelector(List<String> serverList){
            this.serverList = serverList;
        }

        @Override
        public String getNext() {
            if(serverList.isEmpty()){
                throw new RegistryDiscoveryException();
            }

            return serverList.get(new Random().nextInt(serverList.size()));
        }
    }
}
