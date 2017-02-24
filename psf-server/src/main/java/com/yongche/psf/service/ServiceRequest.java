package com.yongche.psf.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * Created by stony on 16/11/4.
 */
public class ServiceRequest {

    private Map<String,String> headers;
    private Map<String,String> parameters;
    private String message;
    private JSONObject body;
    private boolean isInitBody = false;

    public ServiceRequest(Map<String, String> headers, Map<String, String> parameters, String message) {
        this.headers = headers;
        this.parameters = parameters;
        this.message = message;
    }


    public String getParameter(String key){
        return (null == parameters) ? null : parameters.get(key);
    }
    public String getHeader(String key){
        return (null == headers) ? null : headers.get(key);
    }
    public JSONObject getBody(){
        initBody();
        return body;
    }

    /**
     * first find boy, if value is null, second find parameters,threes find header
     * @param key
     * @return
     */
    public Object get(String key){
        initBody();
        Object v = null;
        if(body != null){
            v = body.get(key);
        }
        if(v == null){
            v = getParameter(key);
        }
        if(v == null){
            v = getHeader(key);
        }
        return v;
    }
    private void initBody(){
        if(isInitBody) return;
        if(!isEmpty(message)){
            this.body =  JSON.parseObject(message);
        }
        isInitBody = true;
    }
    private static boolean isEmpty(String str){
        return str == null || str.length() == 0;
    }
    public String getMessage() {
        return message;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
