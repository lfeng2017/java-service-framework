package com.yongche.psf.server;

import com.yongche.psf.exception.ServerException;
import java.util.List;

/**
 * Created by stony on 16/11/4.
 */
public class ServiceCenterManager {


    private String[] serviceCenter;
    public String[] getServiceCenter(){
        return serviceCenter;
    }

    public ServiceCenterManager() {
    }

    public ServiceCenterManager(String serviceCentimes){
        this.serviceCenter = serviceCentimes.split(",");
    }

    /**
     *
     * @param serviceCenter
     */
    public void setServiceCenter(String[] serviceCenter) {
        this.serviceCenter = serviceCenter;
    }
}
