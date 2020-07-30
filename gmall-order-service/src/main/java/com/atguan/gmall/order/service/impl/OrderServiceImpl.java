package com.atguan.gmall.order.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguan.gmall.bean.OrderDetail;
import com.atguan.gmall.bean.OrderInfo;
import com.atguan.gmall.bean.enums.OrderStatus;
import com.atguan.gmall.bean.enums.ProcessStatus;
import com.atguan.gmall.config.ActiveMQUtil;
import com.atguan.gmall.config.RedisUtil;
import com.atguan.gmall.order.mapper.OrderDetailMapper;
import com.atguan.gmall.order.mapper.OrderInfoMapper;
import com.atguan.gmall.service.OrderService;
import com.atguan.gmall.util.HttpClientUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ActiveMQUtil activeMQUtil;

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

    @Override
    public OrderInfo getOrderInfo(String orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);

        //查询orderDetail
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        orderInfo.setOrderDetailList(orderDetailList);

        return orderInfo;


    }

    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    @Override
    public void sendOrderStatus(String orderId) {

        //获取连接
        Connection connection = activeMQUtil.getConnection();

        String orderInfoJson = initWareOrder(orderId);
        //创建session
        try {

            //开启连接
            connection.start();
            //获取session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建队列
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            //创建提供者
            MessageProducer producer = session.createProducer(order_result_queue);
            //创建消息
            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            //orderInfo组成的字符串
            activeMQTextMessage.setText(orderInfoJson);
            //放入消息
            producer.send(activeMQTextMessage);
            //提交
            session.commit();
            //关闭
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成JSON字符串
     * @param orderId
     * @return
     */
    private String initWareOrder(String orderId) {

        //根据orderId查询orderInfo
        OrderInfo orderInfo = getOrderInfo(orderId);

        //将orderInfo中有用的数据封装进map中
        Map map = initWareOrder(orderInfo);

        return JSON.toJSONString(map);
    }

    private Map initWareOrder(OrderInfo orderInfo) {

        Map<String,Object> map = new HashMap<>();

        //为map赋值
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        //创建一个集合存放map
        List<Map> list = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetailList) {
            Map<String,Object> orderDetailMap = new HashMap<>();
            orderDetailMap.put("skuId",orderDetail.getSkuId());
            orderDetailMap.put("skuNum",orderDetail.getSkuNum());
            orderDetailMap.put("skuName",orderDetail.getSkuName());

            list.add(orderDetailMap);
        }

        map.put("details",list);
        return map;

    }
}
