package com.atguan.gmall.service;

import com.atguan.gmall.bean.CartInfo;

import java.util.List;

public interface CartInfoService {

    /**
     * 加入购物车方法
     * @param skuId
     * @param userId
     * @param skuNum
     */
    void  addToCart(String skuId,String userId,Integer skuNum);

    /**
     * 根据登录userId查询购物车
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);

    /**
     * 登录时合并购物车
     * @param cartInfos
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartInfos, String userId);

    /**
     * 修改商品状态
     * @param skuId
     * @param isChecked
     * @param userId
     */
    void checkCart(String skuId, String isChecked, String userId);

    /**
     * 展示勾选的购物车
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);
}
