package com.atguan.gmall.order.task;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguan.gmall.bean.OrderInfo;
import com.atguan.gmall.service.OrderService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@EnableScheduling
@Component
public class OrderTask {


    @Reference
    private OrderService orderService;

    //每分钟的第5秒执行该方法
    @Scheduled(cron = "5 * * * * ?")
    public void test01() {
        System.err.println(Thread.currentThread().getName()+"**************************01");
    }

    //每隔5秒执行一次
    @Scheduled(cron = "0/5 * * * * ?")
    public void test02() {
        System.err.println(Thread.currentThread().getName()+"********************************02");
    }

    @Scheduled(cron = "0/20 * * * * ?")
    public void checkOrder() {

        //关闭过期订单
        List<OrderInfo> orderInfoList = orderService.getExpiredOrderList();

        for (OrderInfo orderInfo : orderInfoList) {

            //处理过期订单
            orderService.execExpiredOrder(orderInfo);
        }
    }
}
