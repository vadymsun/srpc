package com.ssh.registry;

import com.ssh.Constants;
import com.ssh.bootstrap.SRPCBootstrap;
import com.ssh.bootstrap.ServiceConfig;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZookeeperRegistry extends AbstractRegistry{


    private ZooKeeper zooKeeper;

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
    public void publish(ServiceConfig service) {
        // 获取zookeeper连接
        try {
            // 如果service节点(接口的全限定名)不存在 则添加永久节点
            String parentNode = Constants.BASE_PROVIDER_PATH + "/" + service.getInterfaceName();
            if(zooKeeper.exists(parentNode, null) == null){
                zooKeeper.create(parentNode,
                        "".getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
            // todo 读取当前服务的ip和端口号
            //  把当前服务的名作为临时节点加入到service下
            String node = parentNode + "/" + SRPCBootstrap.CUR_IP+":"+ SRPCBootstrap.PORT;
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
            // 获取服务列表 优先从缓存中获取

            return zooKeeper.getChildren(Constants.BASE_PROVIDER_PATH+"/"+interfaceName, null);

        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
