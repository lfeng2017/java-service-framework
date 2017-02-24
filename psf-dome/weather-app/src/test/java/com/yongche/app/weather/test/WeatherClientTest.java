package com.yongche.app.weather.test;

import com.yongche.psf.PSFClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by stony on 16/11/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:spring/spring-context.xml")
public class WeatherClientTest {

    @Resource
    PSFClient psfClient;

    @Test
    public void test() throws Exception {
        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = "{\"user_id\":\"5555\",\"full\":true,\"user_type\":\"PA\", \"zh\": \"啦啦啦啦啦\"}";
        request.service_uri = "/weather/getMessage";

        System.out.println(psfClient.call("weather", request));
    }
}
