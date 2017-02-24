package com.yongche.psf.test.proxy;

import com.yongche.psf.exception.ServerException;
import com.yongche.psf.service.*;
import javassist.*;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stony on 16/11/7.
 */
public class JavassistServiceRequestTest {

    public static final String PROXY_CLASS_NAME = "ServiceProxy$";
    private static int proxyIndex = 1;
    @Test
    public void test2() throws Exception {
        try {
            String serviceTypePath = "/weather";
            ClassPool pool = ClassPool.getDefault();

            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-context-test.xml");
            context.start();

            Map<String, Object> serviceControllers = context.getBeansWithAnnotation(ServiceController.class);

            CtClass makeInterface = pool.makeInterface("com.yongche.psf.service.ServiceRequestInvoker");
            StringBuilder proxyInner = new StringBuilder();

            for (Map.Entry<String, Object> entry : serviceControllers.entrySet()) {
                String beanName = entry.getKey();
                Object target = entry.getValue();
                Class<?> targetClass = target.getClass();
                System.out.println("target : " + target);

                ServiceMapping rootMapping = AnnotationUtils.findAnnotation(target.getClass(), ServiceMapping.class);
                String rootPath = "";
                if (null != rootMapping) {
                    rootPath = rootMapping.value();
                }
                Method[] methods = ReflectionUtils.getAllDeclaredMethods(target.getClass());
                System.out.println("------------------------------------------------------------------------------");
                // create new target
                String proxyName = PROXY_CLASS_NAME+proxyIndex++;
                String proxyClassName = targetClass.getPackage().getName()+"."+proxyName;
                CtClass proxy = pool.makeClass(proxyClassName);


                // add filed target
                proxyInner.setLength(0);
                proxyInner.append(targetClass.getCanonicalName()).append(" target;");
                proxy.addField(CtField.make(proxyInner.toString(), proxy));
                System.out.println(proxyInner);
                // add constructor
                proxyInner.setLength(0);
                proxyInner.append("public ").append(proxyName)
                        .append("(").append(targetClass.getCanonicalName()).append(" target){")
                        .append("\n\t").append("this.target = target;").append("\n").append("}");
                proxy.addConstructor(CtNewConstructor.make(proxyInner.toString(), proxy));
                System.out.println(proxyInner);
                // add all method
                proxyInner.setLength(0);
                proxyInner.append("public Object handler(com.yongche.psf.service.ServiceRequest request, String methodName) throws Throwable{\n");
                boolean isFirst = true;
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
                System.out.println(proxyInner);
                System.out.println("------------------------------------------------------------------------------");
                proxy.addMethod(CtMethod.make(proxyInner.toString(), proxy));
                proxy.addInterface(makeInterface);
                Object newTarget = proxy.toClass().getConstructor(targetClass).newInstance(target);
                for (Method method : methods) {
                    ServiceMapping mapping = AnnotationUtils.findAnnotation(method, ServiceMapping.class);
                    if (null != mapping) {
                        String mappingPath = mapping.value();
                        String uri = serviceTypePath + rootPath + mappingPath;
                        //new ServiceMappingInfo(uri, beanName, newTarget, method.getName(), method.getReturnType(), method);
                        System.out.println("|----> newTarget =  " +newTarget);
                        if(newTarget instanceof com.yongche.psf.service.ServiceRequestInvoker){
                            System.out.println("|----> execute result =  " +
                                    ((ServiceRequestInvoker) newTarget).handler(new ServiceRequest(null,null,"{'name':'lele'}"),method.getName()));
                        }
                    }
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
    @Test
    public void test() throws Exception {
        try {
            String serviceTypePath = "/weather";
            ClassPool pool = ClassPool.getDefault();

            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-context-test.xml");
            context.start();

            Map<String, Object> serviceControllers = context.getBeansWithAnnotation(ServiceController.class);

            for (Map.Entry<String, Object> entry : serviceControllers.entrySet()) {
                String beanName = entry.getKey();
                Object target = entry.getValue();
                Class<?> targetClass = target.getClass();
                System.out.println("target : " + target);

                ServiceMapping rootMapping = AnnotationUtils.findAnnotation(target.getClass(), ServiceMapping.class);
                String rootPath = "";
                if (null != rootMapping) {
                    rootPath = rootMapping.value();
                }
                Method[] methods = ReflectionUtils.getAllDeclaredMethods(target.getClass());
                CtClass makeInterface = pool.makeInterface("com.yongche.psf.service.ServiceRequestInvoker");
                for (Method method : methods) {
                    ServiceMapping mapping = AnnotationUtils.findAnnotation(method, ServiceMapping.class);
                    if (null != mapping) {
                        String mappingPath = mapping.value();
                        String uri = serviceTypePath + rootPath + mappingPath;
                        // register mapping
                        String proxyName = PROXY_CLASS_NAME+proxyIndex++;
                        String proxyClassName = targetClass.getPackage().getName()+"."+proxyName;
                        CtClass proxy = pool.makeClass(proxyClassName);
                        StringBuilder proxyInner = new StringBuilder();
                        // add filed target
                        proxyInner.append(targetClass.getCanonicalName()).append(" target;");
                        proxy.addField(CtField.make(proxyInner.toString(), proxy));
                        System.out.println(proxyInner);
                        // add constructor
                        proxyInner.setLength(0);
                        proxyInner.append("public ").append(proxyName)
                                .append("(").append(targetClass.getCanonicalName()).append(" target){")
                                .append("\n\t").append("this.target = target;").append("\n").append("}");
                        System.out.println(proxyInner);
                        proxy.addConstructor(CtNewConstructor.make(proxyInner.toString(), proxy));
                        // add method
                        proxyInner.setLength(0);
                        System.out.println("returnType " + method.getReturnType());
                        if(method.getReturnType().isAssignableFrom(Void.TYPE)){
                            proxyInner.append("public Object handler(com.yongche.psf.service.ServiceRequest request) throws Throwable{\n");
                            proxyInner.append("\n\t").append("target.").append(method.getName()).append("(").append("request);");
                            proxyInner.append("\n\t return null;");
                            proxyInner.append("}");
                            System.out.println(proxyInner);
                            proxy.addMethod(CtMethod.make(proxyInner.toString(), proxy));
                        }else {
                            proxyInner.append("public Object handler(com.yongche.psf.service.ServiceRequest request) throws Throwable{\n");
                            proxyInner.append("\n\t").append("return target.").append(method.getName()).append("(").append("request);\n");
                            proxyInner.append("}");
                            System.out.println(proxyInner);
                            proxy.addMethod(CtMethod.make(proxyInner.toString(), proxy));
                        }
                        proxy.addInterface(makeInterface);
                        Object newTarget = proxy.toClass().getConstructor(targetClass).newInstance(target);
                        // register mapping
//                        urlMappings.put(uri,
//                                new ServiceMappingInfo(uri, beanName, newTarget, method.getName(), method.getReturnType(), method));
//                        new ServiceMappingInfo(uri, beanName, newTarget, method.getName(), method.getReturnType(), method);
                        System.out.println(newTarget);
                        if(newTarget instanceof com.yongche.psf.service.ServiceRequestInvoker){
                            System.out.println("|----> execute result =  " + ((ServiceRequestInvoker) newTarget).handler(new ServiceRequest(null,null,"{'name':'lele'}"),method.getName()));
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
