package com.lagou.rpc.registrationcenter.client;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ZookeeperConnector {


    private static volatile CuratorFramework curatorFramework;


    public static CuratorFramework getConnect(){
        if(curatorFramework == null){

            synchronized (CuratorFramework.class){
                if(curatorFramework == null){

                    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);

                    curatorFramework = CuratorFrameworkFactory.builder().connectString("152.136.24.197:2181,152.136.24.197:2182,152.136.24.197:2183")
                            .sessionTimeoutMs(5000)
                            .connectionTimeoutMs(3000)
                            .retryPolicy(retryPolicy)
                            .namespace("registration")
                            .build();

                }
            }

        }
        return curatorFramework;
    }


}
