package com.atguan.gmall.order.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguan.gmall.bean.CartInfo;
import com.atguan.gmall.bean.OrderDetail;
import com.atguan.gmall.bean.OrderInfo;
import com.atguan.gmall.bean.UserAddress;
import com.atguan.gmall.config.LoginRequire;
import com.atguan.gmall.service.CartInfoService;
import com.atguan.gmall.service.OrderService;
import com.atguan.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.ls.LSInput;

import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {


//    @Autowired

    @Reference
    private UserService userService;

    @Reference
    private CartInfoService cartInfoService;

    @Reference
    private OrderService orderService;


//    @RequestMapping("trade")
//    public String trade() {
//        return "index";
//    }


    @RequestMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest request) {

        //获得userId
        String userId = (String) request.getAttribute("userId");

        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        request.setAttribute("userAddressList",userAddressList);

        //展示送货清单
        List<CartInfo> cartInfoList = cartInfoService.getCartCheckedList(userId);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {

            //将cartInfo属性赋值给orderDetil
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());

            orderDetailList.add(orderDetail);
        }

        //计算总金额并保存
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());


        //保存用户清单集合
        request.setAttribute("orderDetailList",orderDetailList);

        return "trade";
    }

    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(HttpServletRequest request,OrderInfo orderInfo) {

        String userId = (String) request.getAttribute("userId");

        orderInfo.setUserId(userId);

        String orderId = orderService.saveOrder(orderInfo);

        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }

}
