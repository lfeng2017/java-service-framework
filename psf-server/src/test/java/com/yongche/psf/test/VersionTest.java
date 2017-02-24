package com.yongche.psf.test;

import com.google.common.util.concurrent.RateLimiter;
import com.yongche.psf.core.PackageBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stony on 16/11/3.
 */
public class VersionTest {
    @Test
    public void test(){
        String version = "1.0.0";
        int[] versions = new int[3];
        String[] _versionStr = version.split("\\.", 3);
        Assert.assertEquals("版本正确", 3, _versionStr.length);
        for (int i = 0; i < _versionStr.length; i++) {
            System.out.println(_versionStr[i]);
            versions[i] = Integer.parseInt(_versionStr[i]);
        }
        for (int i = 0; i < versions.length; i++) {
            System.out.println(versions[i]);
        }
    }

    @Test
    public void test2(){
        String uri = "/weather/cat/info?name=中国&id=10&ch=1&a=22&b=&c=";
        int index = uri.indexOf("?");
        if(index != -1){
            String parameters = uri.substring(index+1);
            Assert.assertEquals("分割参数正确", "name=中国&id=10&ch=1&a=22&b=&c=", parameters);
            System.out.println(parameters);
            System.out.println(PackageBuilder.parseParameters(parameters));
            System.out.println(PackageBuilder.parseUri(uri));

            System.out.println(uri.substring(0,index));
            Assert.assertEquals("获取uri路径", "/weather/cat/info", uri.substring(0,index));
        }

    }
    final Semaphore semaphore = new Semaphore(1);
    @Test
    public void testSem() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 100; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        semaphore.acquireUninterruptibly();
                        System.out.println(sdf.format(new Date()) + " run is ok.");
                    }finally{
                        semaphore.release();
                    }
                }
            });
        }
        executorService.shutdown();
    }

    @Test
    public void test3() throws Exception {
        //速率是每秒两个许可
        //每秒的任务提交不超过两个
        final RateLimiter rateLimiter = RateLimiter.create(2.0);
        ExecutorService executorService = Executors.newFixedThreadPool(30);

        VTestTask[] tasks = new VTestTask[100];
        for (int i = 0; i < 100; i++) {
            tasks[i] = new VTestTask();
        }
        for (VTestTask task : tasks){
            rateLimiter.acquire();
            executorService.execute(task);
        }
        executorService.shutdown();
    }
    static SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    class VTestTask implements Runnable{
        @Override
        public void run() {
            System.out.println(sdf.format(new Date()) + " run is ok.");
        }
    }

    private static final AtomicInteger number = new AtomicInteger(Integer.MAX_VALUE-1);
    @Test
    public void testX(){
        for (int i = 0; i < 100; i++) {
            System.out.println(Math.abs(number.incrementAndGet()));
        }
    }
}
