package com.yonche.test.weather;

import com.yongche.psf.PSFClient;
import com.yongche.psf.client.ClientManager;
import com.yongche.psf.server.ServiceCenterManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stony on 16/11/21.
 */
public class SimpleClientTest extends AbstractPerformanceTest{
    @Test
    public void testGetHeaders()throws Exception{
        final ServiceCenterManager serviceCenterManager = new ServiceCenterManager(serviceCentimes);
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));

        PSFClient psf = new PSFClient(serviceCenterManager.getServiceCenter(), 2000, 30000, 64 * 1024, 3);

        Hashtable headers = new Hashtable();
        headers.put("name","百事");
        String response = new String();
        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = null;
        request.service_uri = "/weather/getHeaders";
        request.headers = headers;

        response = psf.call("weather", request);
        System.out.println(response);
        Assert.assertEquals("返回头消息响应", "{\"name\":\"百事\"}", response);

        psf.close();
        System.out.println("||---- Done.");
    }
    @Test
    public void testGetParameters()throws Exception{
        final ServiceCenterManager serviceCenterManager = new ServiceCenterManager(serviceCentimes);
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));

        PSFClient psf = new PSFClient(serviceCenterManager.getServiceCenter(), 2000, 30000, 64 * 1024, 3);

        String response = new String();
        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = "";
        request.service_uri = "/weather/getParameters?city=北京&uid=1001&time=8454534343";


        response = psf.call("weather", request);
        System.out.println(response);
        Assert.assertEquals("返回GET请求参数", "{\"uid\":\"1001\",\"city\":\"北京\",\"time\":\"8454534343\"}", response);
        psf.close();
        System.out.println("||---- Done.");
    }
    @Test
    public void getMessage() throws Exception{
        final ServiceCenterManager serviceCenterManager = new ServiceCenterManager(serviceCentimes);
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));

        PSFClient psf = new PSFClient(serviceCenterManager.getServiceCenter(), 2000, 30000, 64 * 1024, 3);

        String response = new String();
        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = "{\"uid\":\"1001\",\"city\":\"北京\",\"time\":\"8454534343\"}";
        request.service_uri = "/weather/getMessage";


        response = psf.call("weather", request);
        System.out.println(response);
        Assert.assertEquals("返回GET请求参数", "{\"uid\":\"1001\",\"city\":\"北京\",\"time\":\"8454534343\"}", response);
        psf.close();
        System.out.println("||---- Done.");
    }

    @Test
    public void testBalance() throws Exception {
        final ServiceCenterManager serviceCenterManager = new ServiceCenterManager(serviceCentimes);
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));
        final AtomicInteger incr = new AtomicInteger(0);
        for (int i = 0; i < 30; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    PSFClient psf = null;
                    try {
                        psf = new PSFClient(serviceCenterManager.getServiceCenter(), 2000, 30000, 64 * 1024, 3);
                        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
                        request.data = "{\"uid\":\"1001\",\"city\":\"北京\",\"time\":\"8454534343\"}";
                        request.service_uri = "/weather/getMessage";
                        System.out.println("第 "+incr.incrementAndGet()+" 次调用 ：" + psf.call("weather", request));
                        sleep(1);
                        System.out.println("第 "+incr.incrementAndGet()+" 次调用 ：" + psf.call("weather", request));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        sleep(1);
                        if(psf != null) psf.close();
                        System.out.println("---------------------- over ");
                    }
                }
            }
            ).start();
        }
        sleep(3);
        System.out.println("---------------- done   -------------");
    }
    @Test
    public void testBind() throws Exception {
        final ServiceCenterManager serviceCenterManager = new ServiceCenterManager(serviceCentimes);
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));
        final AtomicInteger incr = new AtomicInteger(0);
        for (int i = 0; i < 30; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ClientManager clientManager = null;
                    try {
                        clientManager = new ClientManager("weather", "1.0.0", serviceCenterManager);
                        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
                        request.data = "{\"uid\":\"1001\",\"city\":\"北京\",\"time\":\"8454534343\"}";
                        request.service_uri = "/weather/getMessage";
                        System.out.println("第 "+incr.incrementAndGet()+" 次调用 ：" + clientManager.call(request.service_uri,request.headers,request.data));
                        if(incr.get() % 2 == 0) clientManager.bind();
                        sleep(1);
                        System.out.println("第 "+incr.incrementAndGet()+" 次调用 ：" + clientManager.call(request.service_uri,request.headers,request.data));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        sleep(1);
                        if(clientManager != null) clientManager.destroy();
                        System.out.println("---------------------- over ");
                    }
                }
            }
            ).start();
        }
        sleep(3);
        System.out.println("---------------- done   -------------");
    }
    private static void sleep(int minutes){
        try {
            TimeUnit.MINUTES.sleep(minutes);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void sleep(TimeUnit timeUnit,int times){
        try {
            timeUnit.sleep(times);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }


    @Test
    public void testMulti() throws Exception {
        System.out.println("-----------------  begin ---------------");
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        final ServiceCenterManager serviceCenterManager = new ServiceCenterManager(serviceCentimes);
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));

        final int loopCount = 10000;
        final int parallel = 10;
        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel);
        final AtomicInteger error = new AtomicInteger(0);

        final PSFClient psf = new PSFClient(serviceCenterManager.getServiceCenter(), 2000, 30000, (1* 15 * 1024), 3);

        final PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = generateString(10*1024);
        request.service_uri = "/weather/getMessage";
        final String serviceType = "weather";
        for (int i = 0; i < parallel; i++) {
            executorService.execute(new SimpleClientTask(serviceType, signal, finish, error, psf, request, loopCount));
        }
        signal.countDown();
        finish.await();
        int count = (parallel * loopCount) - error.get();
        long time = System.currentTimeMillis() - start;
        System.out.println("|---------------------------------|");
        System.out.println(" count: " + (parallel * loopCount));
        System.out.println(" loop: " + count);
        System.out.println(" time: " + time + "ms");
        System.out.println(" tps: " + (double) count / ((double) time / 1000));
        System.out.println("|---------------------------------|");
        if(psf != null){
            psf.close();
        }
        executorService.shutdown();
        System.out.println("-----------------  end ---------------");
    }
    @Test
    public void testParallel() throws Exception {
        System.out.println("-----------------  begin ---------------");
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        final ServiceCenterManager serviceCenterManager = new ServiceCenterManager(serviceCentimes);
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));
        final int clientCount = 5;
        final int loopCount = 10000;
        final int parallel = 20;
        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel);
        final AtomicInteger error = new AtomicInteger(0);

        PSFClient[] clients = new PSFClient[clientCount];
        for (int i = 0; i < clientCount; i++) {
            clients[i] = new PSFClient(serviceCenterManager.getServiceCenter(), 2000, 30000, (1* 15 * 1024), 3);
        }

        final PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = generateString(10*1024);
        request.service_uri = "/weather/getMessage";
        final String serviceType = "weather";
        PSFClient psf;
        for (int i = 0; i < parallel; i++) {
            psf = clients[i%clientCount];
            executorService.execute(new SimpleClientTask(serviceType, signal, finish, error, psf, request, loopCount));
        }

        signal.countDown();
        finish.await();
        int count = (parallel * loopCount) - error.get();
        long time = System.currentTimeMillis() - start;
        System.out.println("|---------------------------------|");
        System.out.println(" count: " + (parallel * loopCount));
        System.out.println(" loop: " + count);
        System.out.println(" time: " + time + "ms");
        System.out.println(" tps: " + (double) count / ((double) time / 1000));
        System.out.println("|---------------------------------|");
        for (int i = 0; i < clientCount; i++) {
            if(clients[i] != null) clients[i].close();
        }
        executorService.shutdown();
        System.out.println("-----------------  end ---------------");

    }


}
