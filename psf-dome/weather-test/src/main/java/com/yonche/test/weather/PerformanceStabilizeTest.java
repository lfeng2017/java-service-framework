package com.yonche.test.weather;

import com.yongche.psf.PSFClient;
import com.yongche.psf.server.ServiceCenterManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stony on 16/11/22.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:spring-test.xml")
public class PerformanceStabilizeTest extends AbstractPerformanceTest {

    static List<Performance> oneMessage_50 = new ArrayList<>();
    static int repeat = 10000;


    @Repeat(10000)
    @Test
    public void test() throws Exception {
        System.out.println("-----------------  begin ---------------");
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(22);
        final ServiceCenterManager serviceCenterManager = new ServiceCenterManager(serviceCentimes);
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));
        final int clientCount = 20;
        final int loopCount = 10000;
        final int parallel = 20;
        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel);
        final AtomicInteger error = new AtomicInteger(0);

        PSFClient[] clients = new PSFClient[clientCount];
        for (int i = 0; i < clientCount; i++) {
            clients[i] = new PSFClient(serviceCenterManager.getServiceCenter(), 2000, 30000, (75 * 1024), 3);
        }

        final PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = generateString(1024*50);
        request.service_uri = "/weather/getMessage";
        final String serviceType = "weather";
        PSFClient psf;
        for (int i = 0; i < parallel; i++) {
            psf = clients[i%clientCount];
            executorService.execute(new SimpleClientTask(serviceType, signal, finish, error, psf, request, loopCount));
        }

        signal.countDown();
        finish.await();
        int count = (parallel * loopCount);
        int fail = error.get();
        int loop = count - fail;
        long time = System.currentTimeMillis() - start;
        double tps = (double) loop / ((double) time / 1000);
        System.out.println("|------------------------------------|");
        System.out.println("|---count: " + count);
        System.out.println("|---loop: " + loop);
        System.out.println("|---fail: " + fail);
        System.out.println("|---time: " + time + "ms");
        System.out.println("|---tps: " + tps);
        System.out.println("|------------------------------------|");
        oneMessage_50.add(new Performance(count, loop, fail, time, tps));
        for (int i = 0; i < clientCount; i++) {
            if(clients[i] != null) clients[i].close();
        }
        executorService.shutdown();
        System.out.println("-----------------  end ---------------");
    }


    @AfterClass
    public static void afterClass() {

        Performance performance_50 = new Performance(0,0,0,0L,0D);
        for(Performance performance : oneMessage_50){
            performance_50.count += performance.count;
            performance_50.loop += performance.loop;
            performance_50.fail += performance.fail;
            performance_50.time += performance.time;
            performance_50.tps += performance.tps;
        }
        performance_50.count = performance_50.count/repeat;
        performance_50.loop = performance_50.loop/repeat;
        performance_50.fail = performance_50.fail/repeat;
        performance_50.time = performance_50.time/(long)repeat;
        double tps50 = (double) (performance_50.count) / ((double) performance_50.time / 1000);
        System.out.println("|--------------20并发50K数据-----------------|");
        System.out.println("|---count: " + performance_50.count);
        System.out.println("|---loop: " + performance_50.loop);
        System.out.println("|---fail: " + performance_50.fail);
        System.out.println("|---time: " + performance_50.time + "ms");
        System.out.println("|---tps: " + tps50);
        System.out.println("|-----------------------------------------|");

        doAfterClass();
    }


}
