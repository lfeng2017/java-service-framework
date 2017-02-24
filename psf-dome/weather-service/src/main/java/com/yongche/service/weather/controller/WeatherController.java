package com.yongche.service.weather.controller;

import com.yongche.common.weather.bean.WeatherInfo;
import com.yongche.psf.service.ServiceController;
import com.yongche.psf.service.ServiceMapping;
import com.yongche.psf.service.ServiceRequest;

import java.util.Map;

/**
 * Created by stony on 16/11/9.
 */
@ServiceController
public class WeatherController {


    @ServiceMapping(value = "/city/info")
    public WeatherInfo getWeatherInfoByCity(ServiceRequest request){
        WeatherInfo info = new WeatherInfo();
        info.setCity(request.getParameter("city"));
        info.setTemperature(10);
        info.setHumidity(32);
        info.setWindPower(3);
        info.setWindDirect("西北风");
        return info;
    }

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
