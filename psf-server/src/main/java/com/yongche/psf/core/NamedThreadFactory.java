package com.yongche.psf.core;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author shihui
 * Created by stony on 16/11/23.
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger threadPoolNumber = new AtomicInteger(1);
    private static final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String prefix;
    private final boolean isDaemon;
    private final ThreadGroup threadGroup;

    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }
    public NamedThreadFactory() {
        this(false);
    }
    public NamedThreadFactory(boolean daemon) {
        this("psf-thread-pool-" + threadPoolNumber.getAndIncrement(), daemon);
    }
    public NamedThreadFactory(String prefix, boolean daemon) {
        this.prefix = prefix + "-thread-";
        this.isDaemon = daemon;
        SecurityManager s = System.getSecurityManager();
        this.threadGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }
    @Override
    public Thread newThread(Runnable runnable) {
        String name = prefix + threadNumber.getAndIncrement();
        Thread ret = new Thread(threadGroup, runnable, name, 0);
        ret.setDaemon(isDaemon);
        return ret;
    }
    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    /**
     *
     * @param threads
     * @param queues   if queues equals 0 return SynchronousQueue
     * @return
     */
    public static ThreadPoolExecutor newExecutor(int threads, int queues){
        return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS,
                queues == 0 ? new SynchronousQueue<Runnable>()
                        : (queues < 0 ? new LinkedBlockingQueue<Runnable>() : new LinkedBlockingQueue<Runnable>(queues)),
                new NamedThreadFactory(true), new ThreadPoolExecutor.AbortPolicy());
    }
}
