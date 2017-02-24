package com.yongche.psf.test.namespace;

import com.yongche.psf.service.ServiceController;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by stony on 16/11/5.
 */
public class PSFNamespaceHandlerTest {

    @Test
    public void test(){
        try {
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-context-name.xml");
            context.start();

            System.out.println(context.getBeansWithAnnotation(ServiceController.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
