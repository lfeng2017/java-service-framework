package com.yongche.psf.service;

import java.lang.annotation.*;

/**
 * Created by stony on 16/11/4.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceController {

    String name() default "";
}
