package com.atguan.gmall.config;

import java.util.concurrent.*;

public class ExecutorServiceUtil {

    private ExecutorService pool;

    public void initPool(int corePoolSize, int maxPoolSize, long keepAliveTime,int blockQueueSize) {


        pool = new ThreadPoolExecutor(corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>(blockQueueSize),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
    public ExecutorService getPool() {
        return pool;
    }


}
