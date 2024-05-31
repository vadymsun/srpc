package com.ssh.registry;

import com.ssh.Constants;
import com.ssh.bootstrap.ServiceConfig;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;

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
            // 把当前服务的名作为临时节点加入到service下
            String node = parentNode + "/" + "127.0.0.1:8080";
            if(zooKeeper.exists(node, null) == null){
                zooKeeper.create(node,
                        "".getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param interfaceName : 接口的全限定名
     * @return
     */
    @Override
    public String discover(String interfaceName) {
        try {
            List<String> children = zooKeeper.getChildren(Constants.BASE_PROVIDER_PATH+"/"+interfaceName, null);
            if(children.isEmpty()){
                return "";
            }

            // TODO 负载均衡算法选择一个服务
            return children.get(0);


        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
