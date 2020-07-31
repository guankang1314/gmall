package com.atguan.gmall.order.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguan.gmall.bean.enums.ProcessStatus;
import com.atguan.gmall.service.OrderService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;


@Component
public class OrderConsumer {


    @Reference
    private OrderService orderService;

    //获取消息队列中的数据
    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage) throws JMSException {

        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");

        //支付成功
        if ("success".equals(result)) {
            orderService.updateOrderStatus(orderId, ProcessStatus.PAID);

            //发送消息给库存模块,通知减库存
            orderService.sendOrderStatus(orderId);

            //更新订单状态为已通知仓库
            orderService.updateOrderStatus(orderId,ProcessStatus.NOTIFIED_WARE);
        }
    }

    //消费减库存成功的消息
    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {

        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("status");

        //减库存成功
        if ("DEDUCTED".equals(result)) {

            //减库存成功，更新订单状态为以结束
            orderService.updateOrderStatus(orderId, ProcessStatus.DELEVERED);

        }
    }

}
