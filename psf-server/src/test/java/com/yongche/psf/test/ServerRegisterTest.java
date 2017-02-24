package com.yongche.psf.test;

import com.yongche.psf.server.ServerRegister;
import org.junit.Test;

/**
 * Created by stony on 16/11/3.
 */
public class ServerRegisterTest {





    @Test
    public void test() throws Exception{
        String[] serviceCenter = {"10.0.11.243:5201", "10.0.11.244:5201"};

        ServerRegister serverRegister = new ServerRegister(serviceCenter[0], 28099, "weather", "1.0.0");
        ServerRegister serverRegister1 = new ServerRegister(serviceCenter[1], 28099, "weather", "1.0.0");

        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
