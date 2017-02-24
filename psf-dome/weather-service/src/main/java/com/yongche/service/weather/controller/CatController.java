package com.yongche.service.weather.controller;

import com.yongche.psf.service.ServiceController;
import com.yongche.psf.service.ServiceMapping;
import com.yongche.psf.service.ServiceRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stony on 16/11/9.
 */
@ServiceController
@ServiceMapping(value = "/cat")
public class CatController {

    @ServiceMapping(value = "/info")
    public String getInfo(ServiceRequest request){
        System.out.println("header --> " + request.getHeaders());
        System.out.println("parameters --> " + request.getParameters());
        System.out.println("message --> " + request.getMessage());
        System.out.println("body --> " + request.getBody());
        System.out.println("get --> " + request.get("user_id"));
        System.out.println("get --> " + request.get("name"));
        return "{'name' : 'BlueSky','id' : 10000,'zh' : '中国'}";
    }

    @ServiceMapping(value = "/info/all")
    public Map getInfoAll(ServiceRequest request){
        System.out.println("header --> " + request.getHeaders());
        System.out.println("parameters --> " + request.getParameters());
        System.out.println("message --> " + request.getMessage());
        System.out.println("body --> " + request.getBody());
        System.out.println("get --> " + request.get("user_id"));
        System.out.println("get --> " + request.get("name"));

        Map result = new HashMap<>();
        result.put("Blue", "{'name' : 'BlueSky','id' : 10001,'zh' : '蓝色天空'}");
        result.put("Red", "{'name' : 'RedStorm','id' : 10002,'zh' : '红色风暴'}");
        result.put("Black", "{'name' : 'BlackFlash','id' : 10003,'zh' : '黑色闪电'}");
        return result;
    }

    @ServiceMapping(value = "/info/update")
    public void updateInfo(ServiceRequest request){
        System.out.println("header --> " + request.getHeaders());
        System.out.println("parameters --> " + request.getParameters());
        System.out.println("message --> " + request.getMessage());
        System.out.println("body --> " + request.getBody());
        System.out.println("get --> " + request.get("user_id"));
        System.out.println("get --> " + request.get("name"));
        // do something
    }

}