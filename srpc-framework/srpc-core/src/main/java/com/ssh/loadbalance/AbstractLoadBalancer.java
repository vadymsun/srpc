package com.ssh.loadbalance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLoadBalancer implements LoadBalancer{
    private Map<String, Selector> selectorMap = new ConcurrentHashMap<>();

    @Override
    public String getServerHost(String interfaceName) {
        if(!selectorMap.containsKey(interfaceName)){
            selectorMap.put(interfaceName,getSelector(interfaceName));
        }
        Selector selector = selectorMap.get(interfaceName);
        return selector.getNext();
    }

    public abstract Selector getSelector(String interfaceName);

}
