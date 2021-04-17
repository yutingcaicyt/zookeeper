package com.lagou.rpc.consumer.config;

import com.lagou.rpc.consumer.common.Student;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsumerConfig {


//    @Bean
//    public ConsumerService comsumerService(){
//        ConsumerService consumerService = new ConsumerService();
//        consumerService.init();
//        return consumerService;
//    }


    @Bean
    public Student student(){
        Student s = new Student();
        s.setAge(10);
        return s;
    }
}
