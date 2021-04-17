package com.lagou.rpc.provider.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "netty")
@Data
public class NettyConfig {
    private String ip;

    private Integer port;


}
