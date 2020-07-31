package com.atguan.gmall.order.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguan.gmall.bean.*;
import com.atguan.gmall.config.LoginRequire;
import com.atguan.gmall.service.CartInfoService;
import com.atguan.gmall.service.ManageService;
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
import java.util.Map;

@Controller
public class OrderController {


//    @Autowired

    @Reference
    private UserService userService;

    @Reference
    private CartInfoService cartInfoService;

    @Reference
    private OrderService orderService;

    @Reference
    private ManageService manageService;


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

        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);

        return "trade";
    }

    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(HttpServletRequest request,OrderInfo orderInfo) {

        String userId = (String) request.getAttribute("userId");

        orderInfo.setUserId(userId);

        //判断表单是否重复提交
        String tradeNo = (String) request.getParameter("tradeNo");
        boolean b = orderService.checkTradeCode(userId, tradeNo);
        if (!b) {

            request.setAttribute("errMsg","订单已提交，不能重复提交");
            return "tradeFail";
        }

        //验证库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (orderDetailList != null && orderDetailList.size() > 0) {
            for (OrderDetail orderDetail : orderDetailList) {
                String skuId = orderDetail.getSkuId();
                Integer skuNum = orderDetail.getSkuNum();

                boolean checkStock = orderService.checkStock(skuId,skuNum);
                if (!checkStock) {
                    request.setAttribute("errMsg","商品库存不足");
                    return "tradeFail";
                }

                //验证订单价格
                SkuInfo skuInfo = manageService.getSkuInfo(skuId);
                int res = skuInfo.getPrice().compareTo(orderDetail.getOrderPrice());
                if (res != 0) {
                    request.setAttribute("errMsg","价格不匹配");
                    cartInfoService.loadCartCache(userId);
                    return "tradeFail";
                }
            }
        }



        String orderId = orderService.saveOrder(orderInfo);

        //删除流水号
        orderService.delTradeCode(userId);

        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }

    @RequestMapping("orderSplit")
    @ResponseBody
    public String orderSplit(HttpServletRequest request) {

        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");

        List<OrderInfo> orderInfoList = orderService.splitOrder(orderId,wareSkuMap);

        //定义集合存放map
        List<Map> list = new ArrayList<>();
        for (OrderInfo orderInfo : orderInfoList) {

            Map map = orderService.initWareOrder(orderInfo);

            list.add(map);
        }

        return JSON.toJSONString(list);
    }

}
