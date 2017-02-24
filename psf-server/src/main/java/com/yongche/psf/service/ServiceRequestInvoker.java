package com.yongche.psf.service;

/**
 * Created by stony on 16/11/7.
 */
public interface ServiceRequestInvoker {

    public Object handler(ServiceRequest request,String methodName) throws Throwable;
}
