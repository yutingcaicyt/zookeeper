package com.lagou.rpc.registrationcenter.service;

import com.lagou.rpc.registrationcenter.client.ZookeeperConnector;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

public class ProviderService {

    private CuratorFramework connect;
    public void init(){
        connect = ZookeeperConnector.getConnect();
        connect.start();
    }

    /**
     * 将服务端的主机端口号等注册到注册中心
     * @param service
     * @param host
     * @throws Exception
     */
    public void register(String service, String host) throws Exception {
        Stat stat = connect.checkExists().forPath("/service"+"/" + service);

        if(stat == null){
            connect.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath("/service"+"/" + service);
        }

        //创建临时节点
        connect.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath("/service"+"/" + service+"/"+host, "0".getBytes());

    }

}
