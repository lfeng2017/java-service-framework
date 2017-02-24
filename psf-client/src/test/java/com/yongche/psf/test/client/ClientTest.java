package com.yongche.psf.test.client;

import com.yongche.psf.client.Client;
import com.yongche.psf.client.NioClient;
import org.junit.Test;

import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stony on 16/11/8.
 */
public class ClientTest {

    @Test
    public void testClientJoin() throws Exception {
        String host = "127.0.0.1";
        int port = 28080;
        String service_type = "weather";
        Client client = new NioClient(host, port, service_type, "1.0.0").run();
        client.bind();

        client.close();
    }

    @Test
    public void testRequest() throws Exception {
        String host = "127.0.0.1";
        int port = 28080;
        String service_type = "weather";
        Client client = new NioClient(host, port, service_type, "1.0.0").run();
        String data = "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"CN\", \"res\": \"啦啦啦啦啦\"}";
        Hashtable headers = new Hashtable();
        headers.put("name","lele");
        headers.put("id", 1003223);
        client.call("/weather/getInfoByCityId", headers, data);
        client.call("/weather/cat/info", headers, data);

        Thread.sleep(1000);
        client.call("/weather/cat/info/all", headers, data);

        client.close();
    }
    @Test
    public void testC() throws Exception {
        long start = System.currentTimeMillis();
        String host = "127.0.0.1";
        int port = 28080;
        String service_type = "weather";
        Client client = new NioClient(host, port, service_type, "1.0.0").run();
        String data = "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"CN\", \"res\": \"啦啦啦啦啦\"}";
        String v = null;
        Hashtable headers = new Hashtable();
        headers.put("name","lele");
        headers.put("id", 1003223);

        int count = 3000;
        for (int i = 0; i < count; i++) {
            v = client.call(("/weather/getInfoByCityId?_time="+System.nanoTime()), headers, data);
            if(i % 100 == 0){
                System.out.println("|---->  " + v);
            }
        }
        System.out.println("----------- end ------------");
        client.close();

        long time = System.currentTimeMillis() - start;
        System.out.println("|---------------------------------|");
        System.out.println(" loop: " + count);
        System.out.println(" time: " + time + "ms");
        System.out.println(" tps: " + (double) count / ((double) time / 1000));
        System.out.println("|---------------------------------|");
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

        String host = "127.0.0.1";
        int port = 28080;
        String service_type = "weather";
        final Client client = new NioClient(host, port, service_type, "1.0.0").run();
        final String data = "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"CN\", \"res\": \"宛在水中央\"}";
        String v = null;
        final Hashtable headers = new Hashtable();
        headers.put("name","lele");
        headers.put("id", 1003223);

        for (int i = 0; i < parallel; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    String v;
                    try {
                        for (int i = 0; i < loopCount; i++) {
                            v = client.call(("/weather/getInfoByCityId?_time="+System.nanoTime()), headers, data);
                            if(i % 100 == 0){
                                System.out.println("|---->  " + v);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        error.incrementAndGet();
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
        if(client != null) client.close();
        executorService.shutdown();
        System.out.println("-----------------  end ---------------");
    }

    @Test
    public void testParallel() throws Exception {
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        final int parallel = 1000;
        final int loopCount = 200;
        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel);
        final AtomicInteger error = new AtomicInteger(0);

        //        String[] serviceCenter = {"10.0.11.243:5201", "10.0.11.244:5201"};
        String[] serviceCenter = {"10.0.11.71:5201", "10.0.11.72:5201"};
        final String host = "127.0.0.1";
        final int port = 28080;
        final String service_type = "weather";

        final String data = "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"CN\", \"res\": \"宛在水中央\"}";
        String v = null;
        final Hashtable headers = new Hashtable();
        headers.put("name","沙丁鱼");
        headers.put("id", 24555555);
        final String uri = ("/weather/getInfoByCityId?_time=" + System.nanoTime());
        for (int i = 0; i < parallel; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    String v;
                    Client client = null;
                    try {
                        client = new NioClient(host, port, service_type, "1.0.0").run();
                        for (int j = 0; j < loopCount; j++) {
                            v = client.call(uri, headers, data);
                            if(j % 500 == 0) System.out.println(v);
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        error.incrementAndGet();
                    } finally {
                        finish.countDown();
                        if (client != null) client.close();
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
        executorService.shutdown();
        System.out.println("-----------------  end ---------------");

    }
}
