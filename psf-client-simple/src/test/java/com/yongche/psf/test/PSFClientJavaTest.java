package com.yongche.psf.test;

import com.yongche.psf.PSFClient;
import org.junit.Test;

import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stony on 16/11/3.
 */
public class PSFClientJavaTest {

    @Test
    public void testCityInfo() throws Exception{
        PSFClient psf = null;
        int count = 0;

//        String[] serviceCenter = {"10.0.11.243:5201", "10.0.11.244:5201"};
        String[] serviceCenter = {"10.0.11.71:5201", "10.0.11.72:5201"};

        psf = new PSFClient(serviceCenter, 2000, 30000, 64 * 1024, 3);

        Hashtable headers = new Hashtable();
        headers.put("name","百事");
        String response = new String();
        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = "{\"user_id\":\"5555\",\"full\":true,\"user_type\":\"PA\", \"zh\": \"啦啦啦啦啦\"}";
        request.service_uri = "/weather/city/info?city=北京&uid=1001&time=8454534343";
        request.headers = headers;

        response = psf.call("weather", request);
        System.out.println(response);

        psf.close();
        System.out.println("||---- Done.");
    }
    @Test
    public void testCatInfoGet() throws Exception {
        PSFClient psf = null;
        int count = 0;

//        String[] serviceCenter = {"10.0.11.243:5201", "10.0.11.244:5201"};
        String[] serviceCenter = {"10.0.11.71:5201", "10.0.11.72:5201"};

        psf = new PSFClient(serviceCenter, 2000, 30000, 64 * 1024, 3);

        Hashtable headers = new Hashtable();
        headers.put("name","可乐");
        headers.put("id", 1003223);

        String response = new String();
        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = "{\"user_id\":\"5555\",\"full\":true,\"user_type\":\"PA\", \"zh\": \"啦啦啦啦啦\"}";
        request.service_uri = "/weather/cat/info/all?ch=中国&uid=1001&time=8454534343";
        request.headers = headers;

        response = psf.call("weather", request);
        System.out.println(response);

        psf.close();
        System.out.println("||---- Done.");
    }
    @Test
    public void testCatInfoNoHeaderGet() throws Exception {
        PSFClient psf = null;
        int count = 0;

//        String[] serviceCenter = {"10.0.11.243:5201", "10.0.11.244:5201"};
        String[] serviceCenter = {"10.0.11.71:5201", "10.0.11.72:5201"};

        psf = new PSFClient(serviceCenter, 2000, 30000, 64 * 1024, 3);

        String response = new String();
        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = "{\"user_id\":\"5555\",\"full\":true,\"user_type\":\"AA\", \"zh\": \"啦啦啦啦啦\"}";
        request.service_uri = "/weather/cat/info?ch=中国&uid=1001&time=84784534343";
        request.headers = null;

        response = psf.call("weather", request);
        System.out.println(response);

        psf.close();
        System.out.println("||---- Done.");
    }

    @Test
    public void testCatInfo() throws Exception {
        PSFClient psf = null;
        int count = 0;

//        String[] serviceCenter = {"10.0.11.243:5201", "10.0.11.244:5201"};
        String[] serviceCenter = {"10.0.11.71:5201", "10.0.11.72:5201"};

        psf = new PSFClient(serviceCenter, 2000, 30000, 64 * 1024, 3);

        Hashtable headers = new Hashtable();
        headers.put("name","lele");
        headers.put("id", 1003223);

        String response = new String();
        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = "{\"user_id\":\"5555\",\"full\":true,\"user_type\":\"PA\", \"zh\": \"啦啦啦啦啦\"}";
        request.service_uri = "/weather/cat/info";
        request.headers = headers;

        response = psf.call("weather", request);
        System.out.println(response);

        psf.close();
        System.out.println("||---- Done.");
    }

    @Test
    public void testCatInfoAll() throws Exception {
        PSFClient psf = null;
        int count = 0;

        //        String[] serviceCenter = {"10.0.11.243:5201", "10.0.11.244:5201"};
        String[] serviceCenter = {"10.0.11.71:5201", "10.0.11.72:5201"};

        psf = new PSFClient(serviceCenter, 2000, 30000, 64 * 1024, 3);

        Hashtable headers = new Hashtable();
        headers.put("name","Bubu");
        headers.put("id", 1004423);

        String response = new String();
        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = "{\"user_id\":\"5555\",\"full\":true,\"user_type\":\"PA\", \"zh\": \"哈哈哦哦\"}";
        request.service_uri = "/weather/cat/info/all";
        request.headers = headers;

        response = psf.call("weather", request);
        System.out.println(response);

        psf.close();
        System.out.println("|||----- Done.");
    }
    @Test
    public void testCatUpdateInfo() throws Exception {
        PSFClient psf = null;
        int count = 0;

        //        String[] serviceCenter = {"10.0.11.243:5201", "10.0.11.244:5201"};
        String[] serviceCenter = {"10.0.11.71:5201", "10.0.11.72:5201"};

        psf = new PSFClient(serviceCenter, 2000, 30000, 64 * 1024, 3);

        Hashtable headers = new Hashtable();
        headers.put("name","Kale");
        headers.put("id", 10000112);

        String response = new String();
        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = "{\"user_id\":\"1234\",\"full\":false,\"user_type\":\"ZO\", \"zh\": \"呜呜了了\"}";
        request.service_uri = "/weather/cat/info/update";
        request.headers = headers;

        response = psf.call("weather", request);
        System.out.println(response);

        psf.close();
        System.out.println("|>---- Done.");
    }

    @org.junit.Test
    public void testA() throws Exception {
        System.out.println("-----------------  begin ---------------");
        long start = System.currentTimeMillis();
        PSFClient psf = null;
        int count = 10000;

        //        String[] serviceCenter = {"10.0.11.243:5201", "10.0.11.244:5201"};
        String[] serviceCenter = {"10.0.11.71:5201", "10.0.11.72:5201"};
        psf = new PSFClient(serviceCenter, 2000, 30000, 64 * 1024, 3);

        String response = new String();
        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = "{\"user_id\":\"5555\",\"full\":true,\"user_type\":\"PA\", \"zh\": \"啦啦啦啦啦\"}";
        request.service_uri = "/weather/getMessage";
        for (int i = 0; i < count; i++) {
            response = psf.call("weather", request);
            if(i % 100 == 0) System.out.print(i);
            else if(i % 200 == 0) System.out.println();
        }
        psf.close();
        System.out.println("---- Done.");
        long time = System.currentTimeMillis() - start;
        System.out.println("|------------------------------------|");
        System.out.println("|---loop: " + count);
        System.out.println("|---time: " + time + "ms");
        System.out.println("|---tps: " + (double) (count) / ((double) time / 1000));
        System.out.println("|------------------------------------|");
    }

    @org.junit.Test
    public void testB() throws Exception {
        System.out.println("-----------------  begin ---------------");
        long start = System.currentTimeMillis();
        PSFClient psf = null;
        int count = 10000;

        //        String[] serviceCenter = {"10.0.11.243:5201", "10.0.11.244:5201"};
        String[] serviceCenter = {"10.0.11.71:5201", "10.0.11.72:5201"};
        psf = new PSFClient(serviceCenter, 2000, 30000, 64 * 1024, 3);

        String response = new String();
        PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = "{\"user_id\":\"5555\",\"full\":false,\"user_type\":\"PA\", \"哦哦哦哦哦\": \"啦啦啦啦啦\"}";
        request.service_uri = "/weather/getInfoByCityId?uid=123123";
        for (int i = 0; i < count; i++) {
            response = psf.call("weather", request);
            if(i%300 ==0) {
                System.out.print(i);
            }
        }
        System.out.println();
        psf.close();
        System.out.println("Done.");
        long time = System.currentTimeMillis() - start;
        System.out.println("|------------------------------------|");
        System.out.println("|---loop: " + count);
        System.out.println("|---time: " + time + "ms");
        System.out.println("|---tps: " + (double) (count) / ((double) time / 1000));
        System.out.println("|------------------------------------|");
    }


    @Test
    public void testMulti() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        System.out.println("-----------------  begin ---------------");
        long start = System.currentTimeMillis();

        final int loopCount = 300;
        int parallel = 1000;

        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel);
        final AtomicInteger error = new AtomicInteger(0);
        //        String[] serviceCenter = {"10.0.11.243:5201", "10.0.11.244:5201"};
        String[] serviceCenter = {"10.0.11.71:5201", "10.0.11.72:5201"};
        final PSFClient psf = new PSFClient(serviceCenter, 6000, 30000, 64 * 1024, 3);

        final PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = "{\"user_id\":\"5555\",\"full\":true,\"user_type\":\"PA\", \"哦哦哦哦哦\": \"啦啦啦啦啦\"}";
        request.service_uri = "/weather/getInfoByCityId?uid=2022";

        Hashtable headers = new Hashtable();
        headers.put("name","布丁");
        headers.put("id", 199999);
        request.headers = headers;
        for (int i = 0; i < parallel; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    String response = new String();
                    try {
//                        signal.await();
                        for (int i = 0; i < loopCount; i++) {
                            response = psf.call("weather", request);
                            if(i % 100 == 0) System.out.println(response);
                        }
                    }catch (Exception e){
                        error.incrementAndGet();
                        e.printStackTrace();
                    }finally {
                        finish.countDown();
                    }
                }
            });
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
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        int parallel = 10000;
        final int loopCount = 500;
        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel);
        final AtomicInteger error = new AtomicInteger(0);

        //        String[] serviceCenter = {"10.0.11.243:5201", "10.0.11.244:5201"};
        final String[] serviceCenter = {"10.0.11.71:5201", "10.0.11.72:5201"};

        final PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
        request.data = "{\"user_id\":\"5555\",\"full\":true,\"user_type\":\"PA\", \"哦哦哦哦哦\": \"啦啦啦啦啦\"}";
        request.service_uri = "/weather/getInfoByCityId?_time="+System.nanoTime();

        Hashtable headers = new Hashtable();
        headers.put("name","FeiLe");
        headers.put("pm","uue");
        headers.put("id", 1999001);

        for (int i = 0; i < parallel; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    PSFClient psf = null;
                    try{
//                        signal.await();
                        psf = new PSFClient(serviceCenter, 2000, 30000, 64 * 1024, 3);
                        String response = null;
                        for (int j = 0; j < loopCount; j++) {
                            response = psf.call("weather", request);
                            if(j % 100 == 0) System.out.println(response);
                        }
                    } catch (Exception e){
                        System.out.println(Thread.currentThread() + " run error : " + e.getMessage());
                        error.incrementAndGet();
                        e.printStackTrace();
                    } finally {
                        finish.countDown();
                        if(psf != null){
                            psf.close();
                        }
                    }
                }
            });
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
    }
}
