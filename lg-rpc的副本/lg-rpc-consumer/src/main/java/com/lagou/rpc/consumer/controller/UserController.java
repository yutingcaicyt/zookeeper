package com.lagou.rpc.consumer.controller;

import com.lagou.rpc.api.IUserService;
import com.lagou.rpc.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {


    @Autowired
    private IUserService iUserService;

    @GetMapping("/query")
    public User getUserInfo(@RequestParam Integer id){
        User user = iUserService.getById(id);
        log.info("<getUserInfo> user-{}",user);
        return user;
    }
}
