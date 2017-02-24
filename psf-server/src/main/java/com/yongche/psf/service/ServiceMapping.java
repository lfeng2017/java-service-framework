package com.yongche.psf.service;

import java.lang.annotation.*;

/**
 * RPC 请求映射
 * Created by stony on 16/11/4.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceMapping {

    /**
     * 请求路径 uri
     * @return
     */
    String value();
}
