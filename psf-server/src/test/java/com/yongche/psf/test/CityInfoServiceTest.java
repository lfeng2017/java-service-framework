package com.yongche.psf.test;

import com.yongche.psf.service.ServiceRequest;

/**
 * Created by stony on 16/11/4.
 */
public class CityInfoServiceTest {

    public String getInfoByCityId(ServiceRequest request){
        System.out.println("header --> " + request.getHeaders());
        System.out.println("parameters --> " + request.getParameters());
        System.out.println("message --> " + request.getMessage());
        return "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"CN\", \"res\": \"sadfasdfsadf\"}";
    }
}
