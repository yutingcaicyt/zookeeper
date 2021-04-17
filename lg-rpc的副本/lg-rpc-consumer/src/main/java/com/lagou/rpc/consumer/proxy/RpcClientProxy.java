package com.lagou.rpc.consumer.proxy;

import com.alibaba.fastjson.JSON;
import com.lagou.rpc.common.RpcRequest;
import com.lagou.rpc.common.RpcResponse;
import com.lagou.rpc.consumer.client.RpcClient;
import com.lagou.rpc.registrationcenter.pojo.Host;
import com.lagou.rpc.registrationcenter.pojo.ServiceInfo;
import com.lagou.rpc.registrationcenter.service.ConsumerService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * 客户端代理类-创建代理对象
 * 1.封装request请求对象
 * 2.创建RpcClient对象
 * 3.发送消息
 * 4.返回结果
 */
@Slf4j
@Data
public class RpcClientProxy {

    private ServiceInfo serviceInfo;

    private Integer lastInvoceIndex;

    private ConsumerService consumerService;

    RpcClientProxy(ServiceInfo serviceInfo, ConsumerService consumerService){
        this.consumerService = consumerService;
        this.serviceInfo = serviceInfo;
        lastInvoceIndex = 0;
    }


    /**
     * 构建rpcRequest
     * @param method
     * @param args
     * @return
     */
    protected RpcRequest rpcRequestBuilder(Method method, Object[] args){

        //1.封装request请求对象
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setParameters(args);

        return rpcRequest;
    }

    /**
     * 发送rpc请求
     * @param rpcClient
     * @param rpcRequest
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    protected Object rpcSend(RpcClient rpcClient, RpcRequest rpcRequest) throws ExecutionException, InterruptedException {
        try {
            //3.发送消息
            Object responseMsg = rpcClient.send(JSON.toJSONString(rpcRequest));
            RpcResponse rpcResponse = JSON.parseObject(responseMsg.toString(), RpcResponse.class);
            if (rpcResponse.getError() != null) {
                throw new RuntimeException(rpcResponse.getError());
            }
            //4.返回结果
           return rpcResponse.getResult();
        } catch (Exception e) {
            throw e;
        } finally {
            rpcClient.close();
        }
    }


    private  synchronized Integer getNextIndex(Integer indexNow, Integer size){
        if(indexNow < size - 1){
            indexNow++;
            return indexNow;
        }else {
            return 0;
        }
    }


    /**
     * 创建代理
     * @return
     */
    public Object createProxy() {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{serviceInfo.getServiceClass()}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        RpcRequest rpcRequest = rpcRequestBuilder(method, args);

                        //获取Host
                        Host host = consumerService.loadBalance(serviceInfo.getService());
                        //2.创建RpcClient对象
                        List<Host> services = serviceInfo.getServices();
                        RpcClient rpcClient = new RpcClient(host.getIp(), host.getPort());

                        log.info("<createProxy> host-{} services-{} lastInvokeIndex-{}", host,services, lastInvoceIndex);
                        Object response = rpcSend(rpcClient, rpcRequest);

                        //请求完成后更新节点的响应时间
                        consumerService.updateResponseTime(serviceInfo.getService(), host.toString(), System.currentTimeMillis());
                        return JSON.parseObject(response.toString(), method.getReturnType());

                    }
                });
    }
}
