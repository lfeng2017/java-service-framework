package com.yongche.psf.test.client;

import com.yongche.psf.client.ClientManager;
import com.yongche.psf.server.ServiceCenterManager;
import org.junit.Test;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by stony on 16/11/10.
 */
public class ClientManagerTest {

    @Test
    public void testParallelMsgId2() throws Exception {
        ExecutorService boss = Executors.newFixedThreadPool(10);
        final ExecutorService executorService = Executors.newFixedThreadPool(30);
        System.out.println("-----------------  begin ---------------");
        long start = System.currentTimeMillis();
        final int bossCount = 10;
        final int loopCount = 300;
        final int parallel = 20;

        ClientManager[] clientManagers = new ClientManager[bossCount];

        CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel*bossCount);
        final AtomicInteger error = new AtomicInteger(0);

        final AtomicLong msgId = new AtomicLong(0);
        final ServiceCenterManager serviceCenterManager = new ServiceCenterManager("/Users/stony/Downloads/mmap_cache.conf");
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));

        for (int i = 0; i < bossCount; i++) {
            clientManagers[i] = ClientManager.newOioClientManager("weather", "1.0.0", serviceCenterManager);
        }
        for (int z = 0; z < bossCount; z++) {
            final ClientManager clientManager = clientManagers[z];
            boss.execute(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < parallel; i++) {
                        executorService.execute(new CliTestTask(loopCount, finish, error, msgId, clientManager));
                    }
                }
            });
        }

        signal.countDown();
        finish.await();
        int count = (bossCount*parallel * loopCount) - error.get();
        long time = System.currentTimeMillis() - start;
        System.out.println("|---------------------------------|");
        System.out.println(" count: " + (bossCount* parallel * loopCount));
        System.out.println(" loop: " + count);
        System.out.println(" time: " + time + "ms");
        System.out.println(" tps: " + (double) count / ((double) time / 1000));
        System.out.println("|---------------------------------|");

        executorService.shutdown();
        System.out.println("-----------------  end ---------------");
    }

    @Test
    public void testParallelMsgId() throws Exception {
        ExecutorService boss = Executors.newFixedThreadPool(20);
        final ExecutorService executorService = Executors.newFixedThreadPool(50);
        System.out.println("-----------------  begin ---------------");
        long start = System.currentTimeMillis();
        final int bossCount = 20;
        final int loopCount = 10;
        final int parallel = 50;

        ClientManager[] clientManagers = new ClientManager[bossCount];

        CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel*bossCount);
        final AtomicInteger error = new AtomicInteger(0);

        final AtomicLong msgId = new AtomicLong(0);
        final ServiceCenterManager serviceCenterManager = new ServiceCenterManager("/Users/stony/Downloads/mmap_cache.conf");
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));

        for (int i = 0; i < bossCount; i++) {
            clientManagers[i] = ClientManager.newNioClientManager("weather", "1.0.0", serviceCenterManager);
        }
        for (int z = 0; z < bossCount; z++) {
            final ClientManager clientManager = clientManagers[z];
            boss.execute(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < parallel; i++) {
                        executorService.execute(new CliTestTask(loopCount, finish, error, msgId, clientManager));
                    }
                }
            });
        }

        signal.countDown();
        finish.await();
        int count = (bossCount*parallel * loopCount) - error.get();
        long time = System.currentTimeMillis() - start;
        System.out.println("|---------------------------------|");
        System.out.println(" count: " + (bossCount* parallel * loopCount));
        System.out.println(" loop: " + count);
        System.out.println(" time: " + time + "ms");
        System.out.println(" tps: " + (double) count / ((double) time / 1000));
        System.out.println("|---------------------------------|");

        executorService.shutdown();
        System.out.println("-----------------  end ---------------");
    }
    @Test
    public void testMultiMsgId() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        System.out.println("-----------------  begin ---------------");
        long start = System.currentTimeMillis();

        final int loopCount = 10000;
        int parallel = 20;

        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(parallel);
        final AtomicInteger error = new AtomicInteger(0);

        final AtomicLong msgId = new AtomicLong(0);
        ServiceCenterManager serviceCenterManager = new ServiceCenterManager("/Users/stony/Downloads/mmap_cache.conf");
        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));
        final ClientManager clientManager = new ClientManager("weather", "1.0.0", serviceCenterManager);

        String v = null;
        for (int i = 0; i < parallel; i++) {
            executorService.execute(new CliTestTask(loopCount,finish,error,msgId,clientManager));
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
        clientManager.destroy();
        executorService.shutdown();
        System.out.println("-----------------  end ---------------");

    }
    class CliTestTask implements Runnable{
        final int loopCount;
        final CountDownLatch finish;
        final AtomicInteger error;
        final AtomicLong msgId;
        final ClientManager clientManager;

        public CliTestTask(int loopCount, CountDownLatch finish, AtomicInteger error, AtomicLong msgId, ClientManager clientManager) {
            this.loopCount = loopCount;
            this.finish = finish;
            this.error = error;
            this.msgId = msgId;
            this.clientManager = clientManager;
        }

        @Override
        public void run() {
            try {
                String data;
                String v;
                for (int j = 0; j < loopCount; j++) {
                    data = (""+msgId.incrementAndGet());
                    v = clientManager.call(("/weather/getMessage"), null, data);
                    if(!data.equals(v)) {
                        System.out.println(v + "<<--->>" + data);
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                error.incrementAndGet();
            }finally {
                finish.countDown();
            }
        }
    }

    @Test
    public void testMsgId() throws Exception {
        ServiceCenterManager serviceCenterManager = new ServiceCenterManager("/Users/stony/Downloads/mmap_cache.conf");

        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));

        ClientManager clientManager = new ClientManager("weather", "1.0.0", serviceCenterManager);

        String data = "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"UV\", \"res\": \"测试消息\"}";
        String v = null;
        Hashtable headers = new Hashtable();
        headers.put("name","无衣");
        int count = 3000;
        for (int i = 0; i < count; i++) {
            headers.put("msg_id", UUID.randomUUID().toString());
            v = clientManager.call(("/weather/getMsgId?_time=123"), headers, data);
            System.out.println(v);
        }
        clientManager.destroy();
    }
    @Test
    public void testOneMessage() throws Exception {
        long start = System.currentTimeMillis();
        ServiceCenterManager serviceCenterManager = new ServiceCenterManager("/Users/stony/Downloads/mmap_cache.conf");

        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));

        ClientManager clientManager = new ClientManager("weather", "1.0.0", serviceCenterManager);

        String data = generateString(1024);
        String v = null;
        Hashtable headers = new Hashtable();
        headers.put("name","尹人");
        headers.put("id", 10099);
        int count = 10;
        for (int i = 0; i < count; i++) {
            v = clientManager.call("/weather/getMessage", null, data);
            if(i % 100 == 0) System.out.print(i);
            if(i % 500 == 0) System.out.println(i);
        }
        long time = System.currentTimeMillis() - start;
        double tps = (double) (count) / ((double) time / 1000);
        System.out.println("|------------------------------------|");
        System.out.println("|---count: " + count);
        System.out.println("|---loop: " + count);
        System.out.println("|---fail: " + 0);
        System.out.println("|---time: " + time + "ms");
        System.out.println("|---tps: " + tps);
        System.out.println("|------------------------------------|");
        clientManager.destroy();
    }
    public static String generateString(int size){
        StringBuffer buffer = new StringBuffer((int) (size*1.5F));
        for (int i = 0; i < size; i++) {
            buffer.append("1");
        }
        return buffer.toString();
    }
    @Test
    public void testObjectMessage() throws Exception {
        ServiceCenterManager serviceCenterManager = new ServiceCenterManager("/Users/stony/Downloads/mmap_cache.conf");

        System.out.println(Arrays.toString(serviceCenterManager.getServiceCenter()));

        ClientManager clientManager = new ClientManager("weather", "1.0.0", serviceCenterManager);
        BodyObjectMessage message = new BodyObjectMessage();
        message.res = "岂曰无衣？与子同袍。";
        message.name = "无衣";
        String serviceUri = ("/weather/cat/info?_time=" + System.nanoTime());
        String v = clientManager.call(serviceUri, null, message);

        System.out.println(v);

        CatInfo info = clientManager.call(serviceUri, null, message, CatInfo.class);
        System.out.println(info);

        clientManager.destroy();
    }

    class BodyObjectMessage{
        public String res;
        public String name;

    }

    public static class CatInfo{
        public int id;
        public String name;
        public String zh;

        @Override
        public String toString() {
            return "{" +
                    "'name' : '" + name + '\'' +
                    ",'id' : " + id +
                    ",'zh' : '" + zh + '\'' +
                    '}';
        }
    }
}
