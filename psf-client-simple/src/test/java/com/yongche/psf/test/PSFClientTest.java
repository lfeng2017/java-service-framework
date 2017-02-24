package com.yongche.psf.test;

import com.yongche.psf.PSFClient;

/**
 * Created by stony on 16/11/3.
 */
public class PSFClientTest {

    private static PSFClient psf = null;

    public static void main(String[] args) throws Exception {

        int count = 0;

        String[] serviceCenter = {"10.0.11.243:5201", "10.0.11.244:5201"};

        psf = new PSFClient(serviceCenter, 2000, 30000, 64 * 1024, 3);

        String response = new String();
        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = "{\"user_id\":\"5555\",\"full\":true,\"user_type\":\"PA\", \"哦哦哦哦哦\": \"啦啦啦啦啦\"}";
        request.service_uri = "/device/getDeviceIdByUserId";

        for (int i = 0; i < 3; i++){

            try {
                response = psf.call("device", request);
                System.out.println(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        psf.close();
        System.out.println("Done.");
    }
}
