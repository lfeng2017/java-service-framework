package com.yongche.psf.container;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * spring 方式启动server
 * Created by stony on 16/11/9.
 */
public class SpringContainer implements Container {

    public static final String SPRING_CONFIG = "psf.spring.config";

    public static final String DEFAULT_SPRING_CONFIG = "classpath:spring/spring-context.xml";

    static ClassPathXmlApplicationContext context;

    public static ClassPathXmlApplicationContext getContext() {
        return context;
    }

    @Override
    public void start() {
        String  configPath = System.getProperty(SPRING_CONFIG, DEFAULT_SPRING_CONFIG);
        context = new ClassPathXmlApplicationContext(configPath);
        context.start();
    }

    @Override
    public void stop() {
        try {
            if (context != null) {
                context.stop();
                context.close();
                context = null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
