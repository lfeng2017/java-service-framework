package com.yongche.psf.test.spring;

import com.yongche.psf.service.ServiceController;
import com.yongche.psf.service.ServiceMapping;
import com.yongche.psf.service.ServiceRequest;

import java.util.Map;

/**
 * Created by stony on 16/11/4.
 */
@ServiceController
public class CityInfoController {

    @ServiceMapping(value = "/getInfoByCityId")
    public String getInfoByCityId(ServiceRequest request){
//        System.out.println("header --> " + request.getHeaders());
        System.out.println("parameters --> " + request.getParameters());
//        System.out.println("message --> " + request.getMessage());
        return "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"CN\", \"res\": \"中文字母\"}";
    }


    @ServiceMapping(value = "/getMsgId")
    public String getMsgId(ServiceRequest request){
        return "{\"msg_id\":\""+request.getHeader("msg_id")+"\"}";
    }


    @ServiceMapping(value = "/getHeaders")
    public Map getHeaders(ServiceRequest request){
        return request.getHeaders();
    }
    @ServiceMapping(value = "/getParameters")
    public Map getParameters(ServiceRequest request){
        return request.getParameters();
    }
    @ServiceMapping(value = "/getMessage")
    public String getMessage(ServiceRequest request){
        return request.getMessage();
    }
}
