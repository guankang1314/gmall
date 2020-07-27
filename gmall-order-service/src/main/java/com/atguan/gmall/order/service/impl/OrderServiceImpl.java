package com.atguan.gmall.order.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.atguan.gmall.bean.OrderDetail;
import com.atguan.gmall.bean.OrderInfo;
import com.atguan.gmall.bean.enums.OrderStatus;
import com.atguan.gmall.bean.enums.ProcessStatus;
import com.atguan.gmall.order.mapper.OrderDetailMapper;
import com.atguan.gmall.order.mapper.OrderInfoMapper;
import com.atguan.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

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
}
