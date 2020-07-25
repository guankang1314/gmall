package com.atguan.gmall.config;


import com.alibaba.fastjson.JSON;
import com.atguan.gmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getParameter("newToken");

        if (token != null) {
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }

        if (token == null) {
            token = CookieUtil.getCookieValue(request, "token", false);
        }
        if (token != null) {
            Map<String,Object> map = getUserMapByToken(token);
            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName",nickName);
        }


        //在拦截器中获取方法上的注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //获取方法上的注解
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if (methodAnnotation != null) {
            //有注解
            //判断用户是否登录,调用verify方法
            //获得服务器的ip地址
            String salt = request.getHeader("X-forwarded-for");

            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS+"?token="+token+"&salt="+salt);
            if ("success".equals(result)) {
                //认证成功
                //开始解密token
                Map map = getUserMapByToken(token);
                //取出userId
                String userId = (String) map.get("userId");
                //保存到作用域
                request.setAttribute("userId",userId);
                return true;
            }else {
                //认证失败
                if (methodAnnotation.autoRedirect()) {
                    //表示必须登录
                    //获取url
                    String requestUrl = request.getRequestURL().toString();
                    System.err.println(requestUrl);
                    //进行转码
                    String encode = URLEncoder.encode(requestUrl, "UTF-8");
                    System.err.println(encode);

                    //拼接重定向
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encode);
                    return false;
                }
            }

        }

        return true;
    }

    /**
     * 获取map数据
     * @param token
     * @return
     */
    private Map<String, Object> getUserMapByToken(String token) {

        //截取中间部分
        String userInfo = StringUtils.substringBetween(token, ".");
        //base64解码
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] decode = base64UrlCodec.decode(userInfo);
        //将decode转成string
        String mapJson = null;
        try {
            mapJson = new String(decode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //将字符串转化为map
        return JSON.parseObject(mapJson,Map.class);

    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
