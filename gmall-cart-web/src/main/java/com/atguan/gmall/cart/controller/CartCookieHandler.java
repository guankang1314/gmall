package com.atguan.gmall.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguan.gmall.bean.CartInfo;
import com.atguan.gmall.bean.SkuInfo;
import com.atguan.gmall.config.CookieUtil;
import com.atguan.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.internal.cglib.asm.$Label;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {


    @Reference
    private ManageService manageService;

    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;


    /**
     * 添加购物车未登录情况下
     * @param request
     * @param response
     * @param skuId
     * @param userId
     * @param skuNum
     */
    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, int skuNum) {

        //从cookie中获取skuId
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);

        List<CartInfo> cartInfoList = new ArrayList<>();
        boolean ifExist=false;
        if (StringUtils.isNotEmpty(cookieValue)) {
            cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);

            for (CartInfo cartInfo : cartInfoList) {
                //比较skuId
                if (cartInfo.getSkuId().equals(skuId)) {
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    //设置实时价格
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());

                    ifExist = true;
                    break;
                }
            }

        }
        //购物车中没有该商品
        if (!ifExist) {
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);
            cartInfoList.add(cartInfo);

        }
        //把集合放入到cookie中
        CookieUtil.setCookie(request,response,cookieCartName,JSON.toJSONString(cartInfoList),COOKIE_CART_MAXAGE,true);



    }

    /**
     * 查询cookie中购物车列表
     * @param request
     * @return
     */
    public List<CartInfo> getCartList(HttpServletRequest request) {

        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);
        if (StringUtils.isNotEmpty(cookieValue)) {
            List<CartInfo> cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
            return cartInfoList;
        }

        return null;
    }

    /**
     * 合并完删除cookie中的购物车
     * @param request
     * @param response
     */
    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {

        CookieUtil.deleteCookie(request,response,cookieCartName);
    }

    /**
     *未登录情况下选中商品
     * @param request
     * @param response
     * @param skuId
     * @param isChecked
     */
    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {

        List<CartInfo> cartList = getCartList(request);
        if (cartList != null && cartList.size() > 0) {
            for (CartInfo cartInfo : cartList) {
                if (cartInfo.getSkuId().equals(skuId)) {
                    cartInfo.setIsChecked(isChecked);
                }
            }
        }

        //将购物车集合写回cookie
        CookieUtil.setCookie(request,response,cookieCartName,JSON.toJSONString(cartList),COOKIE_CART_MAXAGE,true);

    }
}
