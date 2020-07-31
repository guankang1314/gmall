package com.atguan.gmall.payment.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguan.gmall.bean.OrderInfo;
import com.atguan.gmall.bean.PaymentInfo;
import com.atguan.gmall.bean.enums.PaymentStatus;
import com.atguan.gmall.payment.config.AlipayConfig;
import com.atguan.gmall.service.OrderService;
import com.atguan.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {


    @Reference
    private OrderService orderService;

    @Reference
    private PaymentService paymentService;

    @Autowired
    private AlipayClient alipayClient;

    @RequestMapping("index")
    public String index(HttpServletRequest request,String orderId) {

        //选中支付渠道
        //设置订单id
        request.setAttribute("orderId",orderId);
        //获取总金额
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        return "index";
    }

    @RequestMapping("alipay/submit")
    @ResponseBody
    public String alipaysubmit(HttpServletRequest request, HttpServletResponse httpResponse) {

        String orderId = request.getParameter("orderId");
        //通过orderId查询orderInfo
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        //保存支付记录
        PaymentInfo paymentInfo = new PaymentInfo();

        //属性赋值
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("买东西");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setCreateTime(new Date());


        paymentService.savePaymentInfo(paymentInfo);

        //生成二维码
        //AlipayClient alipayClient =  new DefaultAlipayClient( "https://openapi.alipay.com/gateway.do" , APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);  //获得初始化的AlipayClient
        AlipayTradePagePayRequest alipayRequest =  new  AlipayTradePagePayRequest(); //创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url); //在公共参数中设置回跳和通知地址
//        alipayRequest.setReturnUrl( "http://domain.com/CallBack/return_url.jsp" );
//        alipayRequest.setNotifyUrl( "http://domain.com/CallBack/notify_url.jsp" ); //在公共参数中设置回跳和通知地址
//        //alipayRequest.putOtherTextParam("app_auth_token", "201611BB8xxxxxxxxxxxxxxxxxxxedcecde6");//如果 ISV 代商家接入电脑网站支付能力，则需要传入 app_auth_token，使用第三方应用授权；自研开发模式请忽略

        //参数
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",paymentInfo.getTotalAmount());
        map.put("subject",paymentInfo.getSubject());

        alipayRequest.setBizContent(JSON.toJSONString(map));

//        alipayRequest.setBizContent( "{"  +
//                "    \"out_trade_no\":\"20150320010101001\","  +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\","  +
//                "    \"total_amount\":88.88,"  +
//                "    \"subject\":\"Iphone6 16G\","  +
//                "    \"body\":\"Iphone6 16G\","  +
//                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\","  +
//                "    \"extend_params\":{"  +
//                "    \"sys_service_provider_id\":\"2088511833207846\""  +
//                "    }" +
//                "  }" ); //填充业务参数
        String form= "" ;
        try  {
            form = alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
        }  catch  (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType( "text/html;charset=UTF-8");
//        httpResponse.getWriter().write(form); //直接将完整的表单html输出到页面
//        httpResponse.getWriter().flush();
//        httpResponse.getWriter().close();

        //A调用延迟队列
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15,3);
        return form;
    }

    //同步回调，帮助用户重定向
    @RequestMapping("alipay/callback/return")
    public String callbackreturn() {
        return "redirect:"+AlipayConfig.return_order_url;
    }

    //异步回调
    @RequestMapping("alipay/callback/notify")
    public String callbacknotify(@RequestParam Map<String, String> paramsMap,HttpServletRequest request) {

        //paramsMap = ... //将异步通知中收到的所有参数都存放到map中
        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        String out_trade_no = paramsMap.get("out_trade_no");

        if(signVerified){
            //

            //对业务的二次校验
            //只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功。
            //需要得到trade_status
            String trade_status = paramsMap.get("trade_status");
            if ("TRADE_SUCCESS".equals(trade_status)||"TRADE_FINISHED".equals(trade_status)) {

                //根据out_trade_no查询paymentInfo
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOutTradeNo(out_trade_no);
                PaymentInfo paymentInfoHas = paymentService.getPaymentInfo(paymentInfo);

                if (paymentInfoHas.getPaymentStatus()==PaymentStatus.PAID || paymentInfoHas.getPaymentStatus()==PaymentStatus.ClOSED) {
                    return "failure";
                }

                //更新交易记录状态
                PaymentInfo paymentInfoUPD = new PaymentInfo();
                paymentInfoUPD.setPaymentStatus(PaymentStatus.PAID);
                paymentInfoUPD.setCallbackTime(new Date());
                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUPD);
                //消息队列
                paymentService.sendPaymentResult(paymentInfoHas,"success");
                return "success";
            }

        }else{
            //

            return "failure";
        }
        return "failure";

    }

    /**
     * 根据oredrId退款
     */
    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId) {
        Boolean result = paymentService.refund(orderId);
        return ""+result;
    }

    @RequestMapping("wx/submit")
    @ResponseBody
    public Map wxsubmit(String orderId) {

        //String orderId = request.getParameter("orderId");

        orderId = "109";
        //调用服务层生成数据
        Map<String,String> map = paymentService.createNative(orderId,"0.01");

        System.err.println(map.get("code_url"));
        return map;
    }

    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,String result) {

        paymentService.sendPaymentResult(paymentInfo,result);
        return "ok";
    }

    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult(String orderId) {

        //根据orderId查询paymentInfo
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        PaymentInfo queryPaymentInfo1 = paymentService.getPaymentInfo(paymentInfo);
        boolean flag = paymentService.checkPayment(queryPaymentInfo1);
        return ""+flag;


    }
}
