package com.atguan.gmall.service;

import com.atguan.gmall.bean.OrderInfo;
import com.atguan.gmall.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {

    /**
     * 保存订单
     * @param orderInfo
     * @return
     */
    String saveOrder(OrderInfo orderInfo);


    /**
     * 生成流水号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 比较流水号
     * @param userId
     * @param tradeCodeNo
     * @return
     */
    boolean checkTradeCode(String userId,String tradeCodeNo);

    /**
     * 删除流水号
     * @param userId
     */
    void  delTradeCode(String userId);

    /**
     * 检验库存
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId, Integer skuNum);

    /**
     * 通过orderId获取orderInfo
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(String orderId);

    /**
     * 取出消息队列中的数据更新订单状态
     * @param orderId
     * @param processStatus
     */
    void updateOrderStatus(String orderId, ProcessStatus processStatus);

    /**
     * 发送消息给库存模块，通知减库存
     * @param orderId
     */
    void sendOrderStatus(String orderId);

    /**
     * 查询过期订单
     * @return
     */
    List<OrderInfo> getExpiredOrderList();

    /**
     * 处理过期订单
     * @param orderInfo
     */
    void execExpiredOrder(OrderInfo orderInfo);

    /**
     *将orderInfo转换成需要的map集合
     * @param orderInfo
     * @return
     */
    Map initWareOrder(OrderInfo orderInfo);

    /**
     * 拆单接口
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    List<OrderInfo> splitOrder(String orderId, String wareSkuMap);
}
