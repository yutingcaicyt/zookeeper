package com.lagou.rpc.registrationcenter.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Host {
    private String ip;

    private Integer port;

    public Host(String host){
        String[] split = host.split(":");
        this.ip = split[0];
        this.port = Integer.parseInt(split[1]);
    }

    @Override
    public String toString(){
        return ip+":"+port;
    }
}
