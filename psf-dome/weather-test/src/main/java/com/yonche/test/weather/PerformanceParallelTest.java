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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stony on 16/11/21.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:spring-test.xml")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PerformanceParallelTest extends AbstractPerformanceTest{

    static List<Performance> oneMessage_1 = new ArrayList<>(10);
    static List<Performance> oneMessage_10 = new ArrayList<>(10);
    static List<Performance> oneMessage_20 = new ArrayList<>(10);
    static List<Performance> oneMessage_50 = new ArrayList<>(10);


    @AfterClass
    public static void afterClass(){
        Performance performance_1 = new Performance(0,0,0,0L,0D);
        for(Performance performance : oneMessage_1){
            performance_1.count += performance.count;
            performance_1.loop += performance.loop;
            performance_1.fail += performance.fail;
            performance_1.time += performance.time;
            performance_1.tps += performance.tps;
        }
        performance_1.count = performance_1.count/5;
        performance_1.loop = performance_1.loop/5;
        performance_1.fail = performance_1.fail/5;
        performance_1.time = performance_1.time/5L;
        double tps = (double) (performance_1.count) / ((double) performance_1.time / 1000);
        System.out.println("|--------------20并发1K数据-----------------|");
        System.out.println("|---count: " + performance_1.count);
        System.out.println("|---loop: " + performance_1.loop);
        System.out.println("|---fail: " + performance_1.fail);
        System.out.println("|---time: " + performance_1.time + "ms");
        System.out.println("|---tps: " + tps);
        System.out.println("|-----------------------------------------|");

        Performance performance_10 = new Performance(0,0,0,0L,0D);
        for(Performance performance : oneMessage_10){
            performance_10.count += performance.count;
            performance_10.loop += performance.loop;
            performance_10.fail += performance.fail;
            performance_10.time += performance.time;
            performance_10.tps += performance.tps;
        }
        performance_10.count = performance_10.count/5;
        performance_10.loop = performance_10.loop/5;
        performance_10.fail = performance_10.fail/5;
        performance_10.time = performance_10.time/5L;
        double tps10 = (double) (performance_10.count) / ((double) performance_10.time / 1000);
        System.out.println("|--------------20并发10K数据-----------------|");
        System.out.println("|---count: " + performance_10.count);
        System.out.println("|---loop: " + performance_10.loop);
        System.out.println("|---fail: " + performance_10.fail);
        System.out.println("|---time: " + performance_10.time + "ms");
        System.out.println("|---tps: " + tps10);
        System.out.println("|-----------------------------------------|");


        Performance performance_20 = new Performance(0,0,0,0L,0D);
        for(Performance performance : oneMessage_20){
            performance_20.count += performance.count;
            performance_20.loop += performance.loop;
            performance_20.fail += performance.fail;
            performance_20.time += performance.time;
            performance_20.tps += performance.tps;
        }
        performance_20.count = performance_20.count/5;
        performance_20.loop = performance_20.loop/5;
        performance_20.fail = performance_20.fail/5;
        performance_20.time = performance_20.time/5L;
        double tps20 = (double) (performance_20.count) / ((double) performance_20.time / 1000);
        System.out.println("|--------------20并发20K数据-----------------|");
        System.out.println("|---count: " + performance_20.count);
        System.out.println("|---loop: " + performance_20.loop);
        System.out.println("|---fail: " + performance_20.fail);
        System.out.println("|---time: " + performance_20.time + "ms");
        System.out.println("|---tps: " + tps20);
        System.out.println("|-----------------------------------------|");


        Performance performance_50 = new Performance(0,0,0,0L,0D);
        for(Performance performance : oneMessage_50){
            performance_50.count += performance.count;
            performance_50.loop += performance.loop;
            performance_50.fail += performance.fail;
            performance_50.time += performance.time;
            performance_50.tps += performance.tps;
        }
        performance_50.count = performance_50.count/5;
        performance_50.loop = performance_50.loop/5;
        performance_50.fail = performance_50.fail/5;
        performance_50.time = performance_50.time/5L;
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
    @Repeat(5)
    @Test
    public void parallel_1() throws Exception {
        System.out.println("-----------------  begin ---------------");
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(22);
        final ServiceCenterManager serviceCenterManager = new ServiceCenterManager(serviceCentimes);
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));
        final int clientCount = 20;
        final int loopCount = 100;
        final int parallel = 20;
        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel);
        final AtomicInteger error = new AtomicInteger(0);

        PSFClient[] clients = new PSFClient[clientCount];
        for (int i = 0; i < clientCount; i++) {
            clients[i] = new PSFClient(serviceCenterManager.getServiceCenter(), 2000, 30000, (5 * 1024), 3);
        }

        final PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = generateString(1024);
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
        oneMessage_1.add(new Performance(count, loop, fail, time, tps));
        for (int i = 0; i < clientCount; i++) {
            if(clients[i] != null) clients[i].close();
        }
        executorService.shutdown();
        System.out.println("-----------------  end ---------------");
    }
    @Repeat(5)
    @Test
    public void parallel_10() throws Exception {
        System.out.println("-----------------  begin ---------------");
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(22);
        final ServiceCenterManager serviceCenterManager = new ServiceCenterManager(serviceCentimes);
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));
        final int clientCount = 20;
        final int loopCount = 100;
        final int parallel = 20;
        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel);
        final AtomicInteger error = new AtomicInteger(0);

        PSFClient[] clients = new PSFClient[clientCount];
        for (int i = 0; i < clientCount; i++) {
            clients[i] = new PSFClient(serviceCenterManager.getServiceCenter(), 2000, 30000, (20 * 1024), 3);
        }

        final PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = generateString(1024*10);
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
        oneMessage_10.add(new Performance(count, loop, fail, time, tps));
        for (int i = 0; i < clientCount; i++) {
            if(clients[i] != null) clients[i].close();
        }
        executorService.shutdown();
        System.out.println("-----------------  end ---------------");
    }
    @Repeat(5)
    @Test
    public void parallel_20() throws Exception {
        System.out.println("-----------------  begin ---------------");
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(22);
        final ServiceCenterManager serviceCenterManager = new ServiceCenterManager(serviceCentimes);
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));
        final int clientCount = 20;
        final int loopCount = 100;
        final int parallel = 20;
        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel);
        final AtomicInteger error = new AtomicInteger(0);

        PSFClient[] clients = new PSFClient[clientCount];
        for (int i = 0; i < clientCount; i++) {
            clients[i] = new PSFClient(serviceCenterManager.getServiceCenter(), 2000, 30000, (40 * 1024), 3);
        }

        final PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = generateString(1024*20);
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
        oneMessage_20.add(new Performance(count, loop, fail, time, tps));
        for (int i = 0; i < clientCount; i++) {
            if(clients[i] != null) clients[i].close();
        }
        executorService.shutdown();
        System.out.println("-----------------  end ---------------");
    }
    @Repeat(5)
    @Test
    public void parallel_50() throws Exception {
        System.out.println("-----------------  begin ---------------");
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(22);
        final ServiceCenterManager serviceCenterManager = new ServiceCenterManager(serviceCentimes);
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));
        final int clientCount = 20;
        final int loopCount = 100;
        final int parallel = 20;
        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel);
        final AtomicInteger error = new AtomicInteger(0);

        PSFClient[] clients = new PSFClient[clientCount];
        for (int i = 0; i < clientCount; i++) {
            clients[i] = new PSFClient(serviceCenterManager.getServiceCenter(), 2000, 30000, (80 * 1024), 3);
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
}
