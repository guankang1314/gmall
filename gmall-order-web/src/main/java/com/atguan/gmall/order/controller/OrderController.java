package com.atguan.gmall.order.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguan.gmall.bean.UserAddress;
import com.atguan.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class OrderController {


//    @Autowired

    @Reference
    private UserService userService;


//    @RequestMapping("trade")
//    public String trade() {
//        return "index";
//    }


    @RequestMapping("trade")
    @ResponseBody
    public List<UserAddress> trade(String userId) {
        return userService.getUserAddressList(userId);
    }

}
