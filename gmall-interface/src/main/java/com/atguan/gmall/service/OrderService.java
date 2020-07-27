package com.atguan.gmall.service;

import com.atguan.gmall.bean.OrderInfo;

public interface OrderService {

    /**
     * 保存订单
     * @param orderInfo
     * @return
     */
    String saveOrder(OrderInfo orderInfo);
}
