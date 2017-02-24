package com.yongche.psf.service;

import com.alibaba.fastjson.JSON;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Created by stony on 16/11/4.
 */
public class ServiceMappingInfo {

    private String uri;
    private String beanName;
    private Object target;
    private String methodName;
    private Class<?> returnType;
    private ServiceRequest request;
    private MethodInvoker methodInvoker;

    public ServiceMappingInfo(String uri, String beanName, Object target, String methodName,
                              Class<?> returnType, Method method) {
        this.uri = uri;
        this.beanName = beanName;
        this.target = target;
        this.methodName = methodName;
        this.returnType = returnType;
        this.methodInvoker = new MethodInvoker(method);
    }

    @Override
    public String toString() {
        return "ServiceMappingInfo{" +
                "uri='" + uri + '\'' +
                ", beanName='" + beanName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", returnType=" + returnType +
                ", request=" + request +
                '}';
    }

    public String invoke() throws Throwable{
        Object result = null;
        if(target instanceof ServiceRequestInvoker){
            result = ((ServiceRequestInvoker) target).handler(request, methodName);
        }else{
            if(returnType.isAssignableFrom(Void.TYPE)){
                methodInvoker.invoke(target, new Object[]{request}, false);
                return null;
            }else{
                result = methodInvoker.invoke(target, new Object[]{request}, true);
            }
        }
        if(result == null){
            return null;
        }
        if(!returnType.isAssignableFrom(String.class)){
            return JSON.toJSONString(result);
        }
        return (String) result;
    }

    public void setRequest(ServiceRequest request) {
        this.request = request;
    }

    class MethodInvoker{
        Method method;
        protected Object invoke(Object target, Object[] args, boolean isReturn) throws Throwable{
            if(isReturn) {
                return ReflectionUtils.invokeMethod(method, target, args);
            }else{
                ReflectionUtils.invokeMethod(method, target, args);
                return null;
            }
        }

        public MethodInvoker(Method method) {
            this.method = method;
        }
    }
}
