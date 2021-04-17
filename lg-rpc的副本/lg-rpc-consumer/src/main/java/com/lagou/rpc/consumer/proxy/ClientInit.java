package com.lagou.rpc.consumer.proxy;

import cn.hutool.core.lang.ClassScanner;
import com.google.common.base.Stopwatch;
import com.lagou.rpc.anno.SimpleClient;
import com.lagou.rpc.consumer.common.Student;
import com.lagou.rpc.registrationcenter.pojo.Host;
import com.lagou.rpc.registrationcenter.pojo.ServiceInfo;
import com.lagou.rpc.registrationcenter.service.ConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ClientInit implements BeanFactoryPostProcessor {


    private static final String CLIENT_PACKAGE = "com.lagou.rpc.api";

    private static List<String> totalServices = new ArrayList<>();

    private ConsumerService consumerService;

    @Autowired
    private Student student;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        System.out.println("student:"+student);
        consumerService = new ConsumerService();
        consumerService.init();
        Set<Class<?>> classes = ClassScanner.scanPackageByAnnotation(CLIENT_PACKAGE, SimpleClient.class);
        log.info("<ClientInit> classes-{}", classes);
        classes.stream()
                .forEach(clientClass -> {

                    List<Host> services = new ArrayList<>();
                    String service = clientClass.getAnnotation(SimpleClient.class).service();
                    log.info("<ClientInit> clientClass-{} service-{}", clientClass, service);
                    totalServices.add(service);

                    ServiceInfo serviceInfo = new ServiceInfo(services,service, clientClass);
                    RpcClientProxy rpcClientProxy = new RpcClientProxy(serviceInfo, consumerService);
                    log.info("<consumerService> consumer-{}", this.consumerService);
                    try {
                        this.consumerService.getHosts(serviceInfo, service);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Object proxy = rpcClientProxy.createProxy();
                    beanFactory.registerSingleton(clientClass.getName(), proxy);
                });


        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                Stopwatch started = Stopwatch.createStarted();
                this.consumerService.ResponseTimecheck(totalServices);
                log.info("<scheduleCheck> cost-{}", started.stop());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }



}
