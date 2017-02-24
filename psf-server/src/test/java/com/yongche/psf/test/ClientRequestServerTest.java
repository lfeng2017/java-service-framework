package com.yongche.psf.test;

import com.yongche.psf.core.ConnectionInfo;
import com.yongche.psf.core.PackageBuilder;
import com.yongche.psf.core.ProtocolHeader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.yongche.psf.core.ContextHolder.PSF_DEFAULT_CHARSET;

/**
 * Created by stony on 16/11/3.
 */
public class ClientRequestServerTest {

    private ConnectionInfo connectionInfo;
    private PackageBuilder packageBuilder;
    private OutputStream out;
    private InputStream in;

    public ClientRequestServerTest() throws Exception {
        connectionInfo = new ConnectionInfo("127.0.0.1", 28080);
        packageBuilder = new PackageBuilder();
        connectionInfo.connect(packageBuilder.context.connect_timeout,packageBuilder.context.network_timeout);
    }
    public String call(String service_type, Hashtable request_headers, String request_data, String request_service_uri)
            throws  Exception {
        ProtocolHeader header;
        String result = null;
        if(request_data == null) request_data = "";
        try {
            int len = packageBuilder.buildRpcRequestPackage(request_headers,request_data,request_service_uri);

            out = connectionInfo.sock.getOutputStream();
            out.write(packageBuilder.context.send_recv_buf, 0, len);
            out.flush();

            in = connectionInfo.sock.getInputStream();
            header = packageBuilder.recvHeader(in);
            byte[] recvBuff = null;
            if (header.body_len > 0) {
                recvBuff = packageBuilder.readFully(in, header.body_len);
            }
            if (header.status != 0) {
                String error = "recv package status " + header.status + " != 0";
                if (recvBuff != null) {
                    error += ", error info: " + new String(recvBuff, PSF_DEFAULT_CHARSET);
                }
                System.out.println("call failed " + header);
                throw new IOException(error);
            }
            //System.out.println("join server :28080 success " + header);
            if(recvBuff != null){
                return new String(recvBuff, PSF_DEFAULT_CHARSET);
            }
        }catch (IOException e){
            connectionInfo.close();
            throw e;
        }
        return result;
    }
    public void close(){
        connectionInfo.close();
    }

    @Test
    public void testNoProcess() throws Exception {
        ClientRequestServerTest test = new ClientRequestServerTest();
        String data = "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"CN\", \"res\": \"啦啦啦啦啦\"}";
        String v = test.call("weather", null, data, "/weather/getNoProcess");
        System.out.println("---->  " + v);

        test.close();
    }

    @Test
    public void testNoBody() throws Exception {
        ClientRequestServerTest test = new ClientRequestServerTest();
        String data = "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"CN\", \"res\": \"啦啦啦啦啦\"}";
        String v = test.call("weather", null, null, "/weather/getInfoByCityId");
        System.out.println("---->  " + v);

        test.close();
    }

    @Test
    public void testCatInfo() throws Exception {
        ClientRequestServerTest test = new ClientRequestServerTest();
        String data = "{\"city_id\":\"3011\",\"full\":false,\"weather_type\":\"CN\", \"res\": \"小猫猫\"}";
        Hashtable headers = new Hashtable();
        headers.put("name","RedSky");
        headers.put("id", 1003223);
        String v = test.call("weather", headers, data, "/weather/getInfoByCityId");
        System.out.println("---->  " + v);

        test.close();
    }
    @Test
    public void testNoHeader() throws Exception {
        long start = System.currentTimeMillis();
        ClientRequestServerTest test = new ClientRequestServerTest();
        String data = "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"CN\", \"res\": \"啦啦啦啦啦\"}";
        String v = null;

        int count = 10000;
        for (int i = 0; i < count; i++) {
            v = test.call("weather", null, data, "/weather/getInfoByCityId");
            if(i % 1000 == 0){
                System.out.println("|---->  " + i);
            }
        }
        System.out.println("----------- end ------------");
        test.close();

        long time = System.currentTimeMillis() - start;
        System.out.println("|---------------------------------|");
        System.out.println(" loop: " + count);
        System.out.println(" time: " + time + "ms");
        System.out.println(" tps: " + (double) count / ((double) time / 1000));
        System.out.println("|---------------------------------|");
    }

    @Test
    public void testHeader() throws Exception {
        long start = System.currentTimeMillis();
        ClientRequestServerTest test = new ClientRequestServerTest();
        Hashtable headers = new Hashtable();
        headers.put("name","lele");
        headers.put("id", 1003223);
        String data = "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"CN\", \"res\": \"啦啦啦啦啦\"}";
        String v = null;

        int count = 1000000;
        for (int i = 0; i < count; i++) {
            v = test.call("weather", headers, "", "/weather/getInfoByCityId");
            if(i % 100 == 0){
                System.out.println("---->  " + i);
            }
        }

        System.out.println("----------- end ------------");
        test.close();

        long time = System.currentTimeMillis() - start;
        System.out.println("|---------------------------------|");
        System.out.println(" loop: " + count);
        System.out.println(" time: " + time + "ms");
        System.out.println(" tps: " + (double) count / ((double) time / 1000));
        System.out.println("|---------------------------------|");
    }

    @Test
    public void test3() throws Exception {
        long start = System.currentTimeMillis();
        ClientRequestServerTest test = new ClientRequestServerTest();
        String data = "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"CN\", \"res\": \"啦啦啦啦啦\"}";
        String v = test.call("weather", null, "", "/weather/getInfoByCityId");
        System.out.println("---->  " + v);

        Hashtable headers = new Hashtable();
        headers.put("name","lele");
        headers.put("id", 1003223);
        int count = 1000000;
        for (int i = 0; i < count; i++) {
            v = test.call("weather", headers, "", "/weather/getInfoByCityId");
            if(i % 100 == 0){
                System.out.println("---->  " + i);
            }
        }
        System.out.println("----------- end ------------");
        test.close();

        long time = System.currentTimeMillis() - start;
        System.out.println("|---------------------------------|");
        System.out.println(" loop: " + count);
        System.out.println(" time: " + time + "ms");
        System.out.println(" tps: " + (double) count / ((double) time / 1000));
        System.out.println("|---------------------------------|");
    }

    @Test
    public void testMultiThread() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(30);
        System.out.println("-----------------  begin ---------------");
        long start = System.currentTimeMillis();
        final int loopCount = 500;
        int parallel = 10000;
        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel);
        final AtomicInteger error = new AtomicInteger(0);

        for (int i = 0; i < parallel; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
//                        signal.await();
                        ClientRequestServerTest test = new ClientRequestServerTest();
                        for (int i = 0; i < loopCount; i++) {
                            String data = "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"CN\", \"res\": \"啦啦啦啦啦\"}";
                            Hashtable headers = new Hashtable();
                            headers.put("name","lele");
                            headers.put("id", 1003223);
                            String v = test.call("weather", headers, data, "/weather/getInfoByCityId?uid=10000"+i);
                        }
                        test.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        error.incrementAndGet();
                    } finally {
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

        executorService.shutdown();
        System.out.println("-----------------  end ---------------");
    }



    @Test
    public void testParallel() throws Exception {
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        int parallel = 10000;
        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel);
        final AtomicInteger error = new AtomicInteger(0);
        final ClientRequestServerTest test = new ClientRequestServerTest();

        for (int i = 0; i < parallel; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {

                    try {
                        signal.await();
                        String data = "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"CN\", \"res\": \"啦啦啦啦啦\"}";
                        Hashtable headers = new Hashtable();
                        headers.put("name","lele");
                        headers.put("id", 1003223);
                        String v = test.call("weather", headers, data, "/weather/getInfoByCityId");
                        //System.out.println("|---> " + v);

                        finish.countDown();
                    } catch (Exception e){
                        System.out.println(Thread.currentThread() + " run error : " + e.getMessage());
                        error.incrementAndGet();
                    } finally {
                        finish.countDown();
                    }
                }
            });
        }

        signal.countDown();
        finish.await();
        int count = parallel - error.get();
        long time = System.currentTimeMillis() - start;
        System.out.println("|---------------------------------|");
        System.out.println(" loop: " + count);
        System.out.println(" time: " + time + "ms");
        System.out.println(" tps: " + (double) count / ((double) time / 1000));
        System.out.println("|---------------------------------|");
        test.close();
        executorService.shutdown();
    }
}
