package com.yongche.psf.test.spring;

import com.yongche.psf.service.ServiceController;
import com.yongche.psf.service.ServiceMapping;
import com.yongche.psf.service.ServiceRequest;

/**
 * Created by stony on 16/11/4.
 */
@ServiceController
public class HotelController {


    @ServiceMapping(value = "/hotel/info")
    public String getInfo(ServiceRequest request){
        return "{'name' : 'TeFei','id' : 10000, 'price' : 199, 'location' : '31.11,113.82'}";
    }


    @ServiceMapping(value = "/hotel/food")
    public String getFood(ServiceRequest request){
        return "{'name' : 'pizza','id' : 20000, 'price' : 39, 'location' : '31.11,113.82'}";
    }
}
