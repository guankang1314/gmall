package com.atguan.gmall.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorServiceConfig {


    @Value("${corePoolSize}")
    private int corePoolSize;

    @Value("${maxPoolSize}")
    private int maxPoolSize;

    @Value("${keepAliveTime}")
    private long keepAliveTime;

    @Value("${blockQueueSize}")
    private int blockQueueSize;

    @Bean
    public ExecutorServiceUtil getExecutorServiceUtil() {

        ExecutorServiceUtil executorServiceUtil = new ExecutorServiceUtil();
        executorServiceUtil.initPool(corePoolSize,maxPoolSize,keepAliveTime,blockQueueSize);
        return executorServiceUtil;
    }


}
