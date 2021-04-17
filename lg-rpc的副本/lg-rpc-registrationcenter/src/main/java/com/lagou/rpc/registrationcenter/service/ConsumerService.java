package com.lagou.rpc.registrationcenter.service;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.lagou.rpc.registrationcenter.client.ZookeeperConnector;
import com.lagou.rpc.registrationcenter.pojo.Host;
import com.lagou.rpc.registrationcenter.pojo.ServiceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ConsumerService {
    private CuratorFramework connect;
    public void init(){
        connect = ZookeeperConnector.getConnect();
        connect.start();
    }


    /**
     * 将服务相关的信息封装进ServiceInfo中去
     * @param serviceInfo
     * @param service
     * @throws Exception
     */
    public void getHosts(ServiceInfo serviceInfo, java.lang.String service) throws Exception {

        //添加监听器
        PathChildrenCache pathChildrenCache = new PathChildrenCache(connect, "/service/" + service, true);
        addHostListner(serviceInfo, pathChildrenCache, service);
    }

    /**
     * 根据service查询所有的Host信息
     * @param service
     * @return
     * @throws Exception
     */
    private List<Host> nodeToNodeData(String service) throws Exception {
        List<String> list = connect.getChildren()
                .forPath("/service/" + service);

        return list.stream().map(Host::new).collect(Collectors.toList());
    }


    /**
     * 处理hostNode更改事件，若发生更改，重新获取host列表
     *
     * @param serviceInfo
     * @param pathChildrenCache
     * @param service
     */
    public void addHostListner(ServiceInfo serviceInfo, PathChildrenCache pathChildrenCache, String service) throws Exception {

        PathChildrenCacheListener pathChildrenCacheListener = new PathChildrenCacheListener() {

            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                    case CHILD_REMOVED:
                    case CHILD_UPDATED:
                        serviceInfo.setServices(nodeToNodeData(service));
                        log.info("childEvent event-{} service-{} hostlistNow-{}", event.getType(), service, serviceInfo.getServices());
                        break;
                    default:
                }
            }
        };

        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }

    private final static String LOCK_NODE = "/updateRT_lock_node";

    /**
     * 更新节点数据
     * @param service
     * @param host
     */
    public void updateResponseTime(String service, String host, Long timestamp) throws Exception {
        InterProcessMutex lock = new InterProcessMutex(connect, LOCK_NODE);
        //使用锁，避免多客户端因为延迟而导致时间戳的变脏
        try {
            lock.acquire();
            Long lastResponseTime = Long.parseLong(new String(connect.getData().forPath("/service/" + service + "/" + host)));
            if(timestamp > lastResponseTime){
                connect.setData().forPath("/service/"+service+"/"+host, timestamp.toString().getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.release();
        }

    }

    /**
     * 检验响应时间是否失效，若现在的时间超过上次响应时间5秒，则将node的值置为0
     * @throws Exception
     */
    public void ResponseTimecheck(List<String> services) throws Exception {
        InterProcessMutex lock = new InterProcessMutex(connect, LOCK_NODE);
        try{
            lock.acquire();
            //为每个service进行校验
            services.forEach(service -> {
                try {
                    List<String> hosts = connect.getChildren().forPath(getServicePath(service));
                    hosts.forEach(host -> {
                        long lastResponseTime = 0;
                        try {
                            lastResponseTime = Long.parseLong(new String(connect.getData().forPath("/service/" + service + "/" + host)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        long between = DateUtil.between(new Date(lastResponseTime), new Date(), DateUnit.SECOND);
                        log.info("<ResponseTimecheck> lastResponseTime-{} between-{}", lastResponseTime, between);
                        if(between > 5){
                            try {
                                connect.setData().forPath("/service/"+service+"/"+host, "0".getBytes());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.release();
        }
    }

    /**
     * 简单的负载均衡，使用最闲的host
     * @param service
     * @return
     * @throws Exception
     */
    public Host loadBalance(String service) throws Exception {
        List<String> list = connect.getChildren().forPath("/service/" + service);

        Map<String, Long> collect = list.stream()
                .collect(Collectors.toMap(s -> s, s -> {
                    try {
                        return Long.parseLong(new String(connect.getData().forPath("/service/" + service + "/" + s)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return -1L;
                    }
                }, (x, y) -> x));
        List<String> keys = new ArrayList<>(collect.keySet());
        keys.sort(Comparator.comparing(collect::get));
        log.info("<loadBalance> keys-{}", keys);
        if(!keys.isEmpty()){
            return new Host(keys.get(0));
        }
        return new Host();
    }


    private String  getServicePath(String service){
        return "/service/"+service;
    }

    private String getHostPath(String service, String host){
        return getServicePath(service) + "/"+host;
    }

}
