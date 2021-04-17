package com.lagou.rpc.api;

import com.lagou.rpc.anno.SimpleClient;
import com.lagou.rpc.pojo.User;

/**
 * 用户服务
 */
@SimpleClient(service = "user")
public interface IUserService {

    /**
     * 根据ID查询用户
     *
     * @param id
     * @return
     */
    User getById(Integer id);
}
