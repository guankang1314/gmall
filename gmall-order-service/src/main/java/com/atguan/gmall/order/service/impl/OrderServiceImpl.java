package com.atguan.gmall.order.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.atguan.gmall.bean.OrderDetail;
import com.atguan.gmall.bean.OrderInfo;
import com.atguan.gmall.bean.enums.OrderStatus;
import com.atguan.gmall.bean.enums.ProcessStatus;
import com.atguan.gmall.config.RedisUtil;
import com.atguan.gmall.order.mapper.OrderDetailMapper;
import com.atguan.gmall.order.mapper.OrderInfoMapper;
import com.atguan.gmall.service.OrderService;
import com.atguan.gmall.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional
    public String saveOrder(OrderInfo orderInfo) {

        //总金额
        orderInfo.sumTotalAmount();
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        //第三方交易编号
        String outTradeNo="ATGUAN"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //创建时间
        orderInfo.setCreateTime(new Date());
        //过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        //进程状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);

        orderInfoMapper.insertSelective(orderInfo);

        //保存订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            //设置orderId
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {

        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String tradeNoKey = "user:"+userId+":tradeCode";
        //定义流水号
        String tradeNo = UUID.randomUUID().toString().replaceAll("-","");
        //存值
        jedis.set(tradeNoKey,tradeNo);

        jedis.close();
        return tradeNo;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {

        //获取缓存的流水号
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String tradeNoKey = "user:"+userId+":tradeCode";
        String tradeNo = jedis.get(tradeNoKey);

        jedis.close();

        return tradeCodeNo.equals(tradeNo);
    }

    @Override
    public void delTradeCode(String userId) {

        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String tradeNoKey = "user:"+userId+":tradeCode";
        //删除
        jedis.del(tradeNoKey);
        jedis.close();
    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {

        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);

        return "1".equals(result);
    }
}
