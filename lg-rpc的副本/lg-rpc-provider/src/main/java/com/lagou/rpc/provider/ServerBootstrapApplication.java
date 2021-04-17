package com.lagou.rpc.provider;

import com.lagou.rpc.provider.config.NettyConfig;
import com.lagou.rpc.provider.server.RpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerBootstrapApplication implements CommandLineRunner {


    @Autowired
    RpcServer rpcServer;

    @Autowired
    private NettyConfig nettyConfig;
    public static void main(String[] args) {
        SpringApplication.run(ServerBootstrapApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                rpcServer.startServer(nettyConfig.getIp(), nettyConfig.getPort());
            }
        }).start();
    }
}
