package com.atguan.gmall.passport.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguan.gmall.bean.UserInfo;
import com.atguan.gmall.passport.config.JwtUtil;
import com.atguan.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    private UserService userService;

    @Value("${token.key}")
    private String key;


    @RequestMapping("index")
    public String index(HttpServletRequest request) {

        String originUrl = request.getParameter("originUrl");

        request.setAttribute("originUrl",originUrl);

        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request) {

        //salt服务器ip地址
        String salt = request.getHeader("X-forwarded-for");

        //调用登录方法
        UserInfo info = userService.login(userInfo);
        if (info != null) {

            //制作token
            Map<String,Object> map = new HashMap<>();
            map.put("userId",info.getId());
            map.put("nickName",info.getNickName());
            String token = JwtUtil.encode(key, map, salt);

            System.err.println(token);
            return token;
        }else {
            return  "fail";

        }
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request) {

//        String salt = request.getHeader("X-forwarded-for");
        //获取token
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");

        //调用jwt
        Map<String, Object> map = JwtUtil.decode(token, key, salt);

        if (map != null && map.size() > 0) {
            String userId = (String) map.get("userId");
            UserInfo userInfo = userService.verify(userId);
            if (userInfo != null) {
                return "success";
            }else {
                return "fail";
            }
        }
        return "fail";

    }
}
