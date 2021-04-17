package com.lagou.rpc.anno;

import java.lang.annotation.*;

/**
 * 标记client的接口
 * @author yuting.cai
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SimpleClient {

    /**
     * 服务名称
     * @return
     */
    String service();
}
