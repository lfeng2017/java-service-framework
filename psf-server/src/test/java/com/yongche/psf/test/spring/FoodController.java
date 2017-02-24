package com.yongche.psf.test.spring;

import com.yongche.psf.service.ServiceController;
import com.yongche.psf.service.ServiceMapping;
import com.yongche.psf.service.ServiceRequest;

/**
 * Created by stony on 16/11/4.
 */
@ServiceController
@ServiceMapping(value = "/food")
public class FoodController {

    @ServiceMapping(value = "/info")
    public String getInfo(ServiceRequest request){
        return "{'name' : 'noodle','id' : 10000, 'price' : 18.00}";
    }
}
