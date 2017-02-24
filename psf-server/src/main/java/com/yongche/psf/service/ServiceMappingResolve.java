package com.yongche.psf.service;

import com.yongche.psf.core.LoggerHelper;
import com.yongche.psf.exception.ServerException;
import javassist.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by stony on 16/11/16.
 */
public interface ServiceMappingResolve {

    void resolve(ApplicationContext applicationContext, String serviceType, Map<String, ServiceMappingInfo> urlMappings) throws BeansException;


    class ReflectionServiceMappingResolve implements ServiceMappingResolve {

        @Override
        public void resolve(ApplicationContext applicationContext, String serviceType, Map<String, ServiceMappingInfo> urlMappings) throws BeansException {
            String serviceTypePath = "/" + serviceType;
            Map<String, Object> serviceControllers = applicationContext.getBeansWithAnnotation(ServiceController.class);
            for (Map.Entry<String, Object> entry : serviceControllers.entrySet()) {
                String beanName = entry.getKey();
                Object target = entry.getValue();
                ServiceMapping rootMapping = AnnotationUtils.findAnnotation(target.getClass(), ServiceMapping.class);
                String rootPath = "";
                if (null != rootMapping) {
                    rootPath = rootMapping.value();
                }
                Method[] methods = ReflectionUtils.getAllDeclaredMethods(target.getClass());
                for (Method method : methods) {
                    ServiceMapping mapping = AnnotationUtils.findAnnotation(method, ServiceMapping.class);
                    if (null != mapping) {
                        String mappingPath = mapping.value();
                        String uri = serviceTypePath + rootPath + mappingPath;
                        // register mapping
                        urlMappings.put(uri,
                                new ServiceMappingInfo(uri, beanName, target, method.getName(), method.getReturnType(), method));
                    }
                }
            }
        }
    }
    class JavassistServiceMappingResolve implements ServiceMappingResolve {
        static final String PROXY_CLASS_NAME = "ServiceProxy$";
        static int proxyIndex = 1;
        @Override
        public void resolve(ApplicationContext applicationContext, String serviceType, Map<String, ServiceMappingInfo> urlMappings) throws BeansException {
            ClassPool pool = new ClassPool(true); //ClassPool.getDefault();
            String serviceTypePath = "/" + serviceType;
            Map<String, Object> serviceControllers = applicationContext.getBeansWithAnnotation(ServiceController.class);
            CtClass makeInterface = pool.makeInterface("com.yongche.psf.service.ServiceRequestInvoker");
            StringBuilder proxyInner = new StringBuilder();

            for (Map.Entry<String, Object> entry : serviceControllers.entrySet()) {
                String beanName = entry.getKey();
                Object target = entry.getValue();
                Class<?> targetClass = target.getClass();
                ServiceMapping rootMapping = AnnotationUtils.findAnnotation(target.getClass(), ServiceMapping.class);
                String rootPath = "";
                if (null != rootMapping) {
                    rootPath = rootMapping.value();
                }
                Method[] methods = ReflectionUtils.getAllDeclaredMethods(target.getClass());
                try{
                    // create new target
                    String proxyName = PROXY_CLASS_NAME+proxyIndex++;
                    String proxyClassName = targetClass.getPackage().getName()+"."+proxyName;
                    CtClass proxy = pool.makeClass(proxyClassName);
                    // add filed target
                    proxyInner.setLength(0);
                    proxyInner.append(targetClass.getCanonicalName()).append(" target;");
                    proxy.addField(CtField.make(proxyInner.toString(), proxy));
                    // add constructor
                    proxyInner.setLength(0);
                    proxyInner.append("public ").append(proxyName)
                            .append("(").append(targetClass.getCanonicalName()).append(" target){")
                            .append("\n\t").append("this.target = target;").append("\n").append("}");
                    proxy.addConstructor(CtNewConstructor.make(proxyInner.toString(), proxy));
                    // add all method
                    proxyInner.setLength(0);
                    proxyInner.append("public Object handler(com.yongche.psf.service.ServiceRequest request, String methodName) throws Throwable{\n");
                    boolean isFirst = true;
                    for (Method method : methods) {

                    }
                    for (Method method : methods) {
                        ServiceMapping mapping = AnnotationUtils.findAnnotation(method, ServiceMapping.class);
                        if (null != mapping) {
                            if(!isFirst){
                                proxyInner.append("else ");
                            }
                            proxyInner.append("if (").append("\"").append(method.getName()).append("\"").append(".equals(methodName)) {");
                            if(method.getReturnType().isAssignableFrom(Void.TYPE)) {
                                proxyInner.append("\n\t").append("target.").append(method.getName()).append("(").append("request);");
                                proxyInner.append("\n\t return null;\n");
                            }else{
                                proxyInner.append("\n\t").append("return target.").append(method.getName()).append("(").append("request);\n");
                            }
                            proxyInner.append("}\n");
                            isFirst = false;
                        }
                    }
                    proxyInner.append("else {\n").append("throw new com.yongche.psf.exception.ServerException(\"not find request method[\"").append("+methodName+").append("\"].\");\n").append("}\n");
                    proxyInner.append("}");
                    LoggerHelper.debug(proxyInner.toString());
                    proxy.addMethod(CtMethod.make(proxyInner.toString(), proxy));
                    proxy.addInterface(makeInterface);
                    Object newTarget = proxy.toClass().getConstructor(targetClass).newInstance(target);
                    String mappingPath;
                    String uri;
                    for (Method method : methods) {
                        ServiceMapping mapping = AnnotationUtils.findAnnotation(method, ServiceMapping.class);
                        if (null != mapping) {
                            mappingPath = mapping.value();
                             uri = serviceTypePath + rootPath + mappingPath;
                            // register mapping
                            urlMappings.put(uri,
                                    new ServiceMappingInfo(uri, beanName, newTarget, method.getName(), method.getReturnType(), method));
                        }
                    }
                }catch (Exception e){
                    throw new ServerException("service mapping resolve error : ", e);
                }
            }
        }
    }
}
