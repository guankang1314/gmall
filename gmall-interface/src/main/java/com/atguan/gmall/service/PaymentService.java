package com.atguan.gmall.service;

import com.atguan.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {


    /**
     * 保存交易记录
     * @param paymentInfo
     */
    void savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 根据out_trade_no查询PaymentInfo
     * @param paymentInfo
     * @return
     */
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    /**
     * 更新订单状态
     * @param out_trade_no
     * @param paymentInfo
     */
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfo);

    /**
     * 根据orderId退款
     * @param orderId
     * @return
     */
    Boolean refund(String orderId);

    /**
     * 微信支付
     * @param orderId
     * @param s
     * @return
     */
    Map<String,String> createNative(String orderId, String s);

    /**
     * 发送订单给消息队列
     * @param paymentInfo
     * @param result
     */
    void sendPaymentResult(PaymentInfo paymentInfo,String result);
}
