package com.atguan.gmall.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguan.gmall.bean.CartInfo;
import com.atguan.gmall.bean.SkuInfo;
import com.atguan.gmall.config.CookieUtil;
import com.atguan.gmall.config.LoginRequire;
import com.atguan.gmall.service.CartInfoService;
import com.atguan.gmall.service.ManageService;
import org.assertj.core.internal.cglib.asm.$ClassReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {


    @Reference
    private CartInfoService cartInfoService;

    @Reference
    private ManageService manageService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    //区分用户是否登录
    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response) {

        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");
        //获取userId
        String userId = (String) request.getAttribute("userId");
        if (userId != null ) {

            //调用登录添加
            cartInfoService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else {
            //调用未登录添加
            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);

        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response) {

        //获取userId
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (userId != null ) {

            //合并购物车
            List<CartInfo> cartInfos = cartCookieHandler.getCartList(request);
            if (cartInfos != null && cartInfos.size() > 0) {
                //合并购物车
                cartInfoList = cartInfoService.mergeToCartList(cartInfos,userId);
                //删除cookie中的购物车
                cartCookieHandler.deleteCartCookie(request,response);
            }else {
                //调用登录查询
                cartInfoList = cartInfoService.getCartList(userId);
            }


        }else {
            //调用未登录添加
            cartInfoList = cartCookieHandler.getCartList(request);
        }

        request.setAttribute("cartInfoList",cartInfoList);
        return "cartList";
    }

    @RequestMapping("checkCart")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public void checkCart(HttpServletRequest request,HttpServletResponse response) {

        String isChecked = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");
        String userId = (String) request.getAttribute("userId");

        if (userId != null) {
            //已经登录
            cartInfoService.checkCart(skuId,isChecked,userId);
        }else {
            //未登录
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }
    }

    @RequestMapping("toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request,HttpServletResponse response) {

        String userId = (String) request.getAttribute("userId");
        //合并已选中的，未登录-登录的
        List<CartInfo> cartList = cartCookieHandler.getCartList(request);
        if (cartList != null && cartList.size() > 0) {
            //合并
            cartInfoService.mergeToCartList(cartList,userId);
            //删除cookie中的购物车
            cartCookieHandler.deleteCartCookie(request,response);
        }
        return "redirect://order.gmall.com/trade";
    }

}
