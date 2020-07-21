package com.atguan.gmall.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {


    @Value("${spring.redis.host:disable}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    //将获取到的参数传入initJedisPool方法中
    @Bean
    public RedisUtil getRedisUtil() {

        if ("disable".equals(host)) {
            return null;
        }

        RedisUtil redisUtil = new RedisUtil();

        redisUtil.initJedisPool(host,port,database);
        return redisUtil;
    }
}
