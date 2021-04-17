package com.lagou.rpc.provider.config;

import com.lagou.rpc.registrationcenter.service.ProviderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegistrationConfig {

    @Bean
    public ProviderService getProviderService(){
        ProviderService providerService = new ProviderService();
        providerService.init();
        return providerService;
    }

}
