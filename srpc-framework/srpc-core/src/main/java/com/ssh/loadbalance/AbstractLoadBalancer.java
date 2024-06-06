package com.ssh.loadbalance;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractLoadBalancer implements LoadBalancer{
    private Map<String, Selector> selectorMap = new ConcurrentHashMap<>();

    @Override
    public String getServerHost(String interfaceName) {
        if(!selectorMap.containsKey(interfaceName)){
            selectorMap.put(interfaceName, getNewSelector(interfaceName));
        }
        Selector selector = selectorMap.get(interfaceName);
        return selector.getNext();
    }

    @Override
    public void reLoadBalance(String interfaceName) {
        if(selectorMap.containsKey(interfaceName)){
            Selector newSelector = getNewSelector(interfaceName);
            selectorMap.put(interfaceName, newSelector);
            log.debug("更新selector {}",newSelector);

        }
    }

    public abstract Selector getNewSelector(String interfaceName);

}
