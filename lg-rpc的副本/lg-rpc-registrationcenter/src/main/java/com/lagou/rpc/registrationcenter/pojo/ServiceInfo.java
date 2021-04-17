package com.lagou.rpc.registrationcenter.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceInfo {

    private List<Host> services;

    private String service;

    private Class<?> serviceClass;
}
