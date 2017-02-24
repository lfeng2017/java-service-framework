package com.yongche.psf.test.spring.demo;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by stony on 16/11/4.
 */
public class WeatherServerTest {

    @Test
    public void test() {
        try {
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-context.xml");
            context.start();
            System.out.println("psf server "+context+" started.");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try{
//            synchronized (WeatherServerTest.class){
//                WeatherServerTest.class.wait();
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }
}
