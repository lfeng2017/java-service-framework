package com.yongche.psf.test;

import com.yongche.psf.core.NamedThreadFactory;
import com.yongche.psf.server.Server;
import com.yongche.psf.service.ServiceMappingInfo;
import com.yongche.psf.service.ServiceRequest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by stony on 16/11/2.
 */
public class ServerTest {

    public static void main(String[] args)throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 28080;
        }
        String[] serviceCenter = {"10.0.11.243:5201", "10.0.11.244:5201"};


        Map<String, ServiceMappingInfo> urlMaps = new HashMap<>();
        CityInfoServiceTest cityInfoServiceTest = new CityInfoServiceTest();
        Method method  = cityInfoServiceTest.getClass().getMethod("getInfoByCityId", ServiceRequest.class);


        ServiceMappingInfo serviceMappingInfo = new ServiceMappingInfo("/weather/getInfoByCityId",
                "weatherService", cityInfoServiceTest,
                method.getName(), method.getReturnType(), method);

        urlMaps.put("/weather/getInfoByCityId",serviceMappingInfo);
        ThreadPoolExecutor serviceExecutor = NamedThreadFactory.newExecutor(100, -1);
        new Server(port,"weather","1.0.0", serviceCenter, urlMaps, serviceExecutor).run();
    }


}
