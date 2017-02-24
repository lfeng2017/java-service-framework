package com.yongche.psf.test.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by stony on 16/11/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-context-name.xml")
public class ClientManagerSpringTest {

    @Resource
    ClientService clientService;

    @Test
    public void test() throws Exception {
        System.out.println(clientService.getCatInfo());
    }
}
