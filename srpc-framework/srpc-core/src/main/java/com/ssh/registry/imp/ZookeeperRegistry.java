package com.ssh.registry.imp;

import com.ssh.Constants;
import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.registry.AbstractRegistry;
import com.ssh.util.NetworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;


@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {
    private final ZooKeeper zooKeeper;

    public ZookeeperRegistry(){
        String connect = "127.0.0.1:2181";
        int timeout = 1000;
        try {
            zooKeeper = new ZooKeeper(connect, timeout, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ZookeeperRegistry(String host){
        int timeout = 1000;
        try {
            zooKeeper = new ZooKeeper(host, timeout, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void publish(String interfaceName) {
        // 获取zookeeper连接
        try {
            // 如果service节点(接口的全限定名)不存在 则添加永久节点
            String parentNode = Constants.BASE_PROVIDER_PATH + "/" + interfaceName;
            if(zooKeeper.exists(parentNode, null) == null){
                zooKeeper.create(parentNode,
                        "".getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
            //  把当前服务的名作为临时节点加入到service下
            String node = parentNode + "/" + NetworkUtil.getIP() +":"+ SRPCBootstrap.getInstance().getConfiguration().getPort();
            if(zooKeeper.exists(node, null) == null){
                zooKeeper.create(node,
                        "".getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.EPHEMERAL);
            }
        } catch (InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param interfaceName : 接口的全限定名
     * @return
     */
    @Override
    public List<String> discover(String interfaceName) {
        try {
            String path = interfaceName;
            if(interfaceName.length() < Constants.BASE_PROVIDER_PATH.length() ||
                    !Constants.BASE_PROVIDER_PATH.equals(path.substring(0,Constants.BASE_PROVIDER_PATH.length()))){
                path = Constants.BASE_PROVIDER_PATH + "/" + interfaceName;
            }
            log.debug("发现服务{}" , path);
            // 获取服务列表 优先从缓存中获取
            return zooKeeper.getChildren(path, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged){
                        // 输出日志
                        log.debug("检测到服务{}节点数量有变化！", watchedEvent.getPath());
                        // 获取负载均衡器 重新进行负载均衡
                        String path = watchedEvent.getPath().substring(Constants.BASE_PROVIDER_PATH.length()+1);
                        log.debug("接口名称{}", path);
                        SRPCBootstrap.getInstance().getLoadBalancer().reLoadBalance(path);
                    }
                }
            });
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
