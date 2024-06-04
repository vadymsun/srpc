package com.ssh.loadbalance;

import com.ssh.exceptions.RegistryDiscoveryException;
import com.ssh.registry.Registry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 每个服务只有一个loadBalancer实例
 */
public class RoundLoadBalancer implements LoadBalancer{

    private Registry registry;
    private Map<String, Selector> selectorMap = new ConcurrentHashMap<>();

    public RoundLoadBalancer(Registry registry){
        this.registry = registry;
    }
    @Override
    public String getServerHost(String interfaceName) {
        if(!selectorMap.containsKey(interfaceName)){
            selectorMap.put(interfaceName,new RoundSelector(registry.discover(interfaceName)));
        }
        Selector selector = selectorMap.get(interfaceName);
        return selector.getNext();
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
