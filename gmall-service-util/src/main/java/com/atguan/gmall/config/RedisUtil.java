package com.atguan.gmall.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {


    //创建链接池
    private JedisPool jedisPool;

    //初始化连接池
    public void initJedisPool(String host,int port,int database) {

        //创建一个配置类
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        //设置连接池最大连接数
        jedisPoolConfig.setMaxTotal(200);

        //设置等待时间
        jedisPoolConfig.setMaxWaitMillis(10*1000);

        //设置最小剩余数
        jedisPoolConfig.setMinIdle(10);

        //开启获取连接池阻塞队列
        jedisPoolConfig.setBlockWhenExhausted(true);

        //在借用连接后，自检是否可用
        jedisPoolConfig.setTestOnBorrow(true);

        jedisPool = new JedisPool(jedisPoolConfig,host,port,20*1000);
    }

    //获取jedis
    public Jedis getJedis() {

        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}
