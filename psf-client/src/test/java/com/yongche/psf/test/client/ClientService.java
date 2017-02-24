package com.yongche.psf.test.client;

import com.yongche.psf.client.ClientManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Hashtable;

/**
 * Created by stony on 16/11/11.
 */
@Service("clientService")
public class ClientService {

    @Resource(name = "weatherClient")
    ClientManager weatherClient;


    public String getCatInfo() throws Exception{
        String data = "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"UV\", \"res\": \"为伊消得人憔悴\"}";
        String serviceUri = ("/weather/cat/info?_time=" + System.nanoTime());
        Hashtable headers = new Hashtable();
        headers.put("name","尹人");
        headers.put("id", 10099);
        return weatherClient.call(serviceUri, headers, data);
    }
}
