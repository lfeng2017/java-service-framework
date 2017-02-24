package com.yonche.test.weather;

import com.yongche.psf.PSFClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stony on 16/11/21.
 */
public abstract class AbstractPerformanceTest {

    static SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    static Date beginDate;

    static String serviceCentimes = "10.0.11.71:5201,10.0.11.72:5201";


    @BeforeClass
    public static void beforeClass(){
        beginDate = new Date();
        System.out.println(">>>>>>>  开始运行时间 ：" + sdf.format(beginDate));
    }
    public static void doAfterClass(){
        Date endDate = new Date();
        System.out.println(">>>>>>>  结束运行时间 ：" + sdf.format(new Date()));
        double duration = (endDate.getTime() - beginDate.getTime()) / 1000D;
        System.out.println(">>>>>>>  连续运行时间 ：" + duration + "ms");
    }
    public Runnable newSimpleClientTask(String serviceType, CountDownLatch signal, CountDownLatch finish, AtomicInteger error, PSFClient psf, PSFClient.PSFRPCRequestData request, int loopCount) {
        return new SimpleClientTask(serviceType, signal, finish, error, psf, request, loopCount);
    }

    public static class SimpleClientTask implements Runnable{
        final int loopCount;
        final CountDownLatch signal;
        final CountDownLatch finish;
        final AtomicInteger error;
        final PSFClient psf;
        final PSFClient.PSFRPCRequestData request;
        final String serviceType;

        public SimpleClientTask(String serviceType,CountDownLatch signal, CountDownLatch finish, AtomicInteger error, PSFClient psf, PSFClient.PSFRPCRequestData request,int loopCount) {
            this.serviceType = serviceType;
            this.signal = signal;
            this.finish = finish;
            this.error = error;
            this.psf = psf;
            this.request = request;
            this.loopCount = loopCount;
        }

        @Override
        public void run() {
            try {
                signal.await();
                String response = null;
                for (int j = 0; j < loopCount; j++) {
                    response = psf.call(serviceType, request);
//                    if(j % 500 == 0) System.out.println(response);
                }
            } catch (Exception e) {
                error.incrementAndGet();
                System.out.println(e.getMessage());
            }finally {
                finish.countDown();
            }
        }
    }
    public static String generateString(int size){
        StringBuffer buffer = new StringBuffer((int) (size*1.5F));
        for (int i = 0; i < size; i++) {
            buffer.append("1");
        }
        return buffer.toString();
    }
    public Performance newPerformance(int count, int loop, int fail, long time,double tps){
        return new Performance(count,loop,fail,time,tps);
    }
    public static class Performance{
        public int count;
        public int loop;
        public int fail;
        public long time;
        public double tps;

        public Performance(int count, int loop, int fail, long time, double tps) {
            this.count = count;
            this.loop = loop;
            this.fail = fail;
            this.time = time;
            this.tps = tps;
        }

        @Override
        public String toString() {
            return "Performance{" +
                    "count=" + count +
                    ", loop=" + loop +
                    ", fail=" + fail +
                    ", time=" + time +
                    ", tps=" + tps +
                    '}';
        }
    }
}
