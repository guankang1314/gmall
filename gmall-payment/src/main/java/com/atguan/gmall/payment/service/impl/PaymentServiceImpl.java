package com.atguan.gmall.payment.service.impl;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguan.gmall.bean.OrderInfo;
import com.atguan.gmall.bean.PaymentInfo;
import com.atguan.gmall.config.ActiveMQUtil;
import com.atguan.gmall.payment.mapper.PaymentInfoMapper;
import com.atguan.gmall.service.OrderService;
import com.atguan.gmall.service.PaymentService;
import com.atguan.gmall.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {



    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Reference
    private OrderService orderService;

    // 服务号Id
    @Value("${appid}")
    private String appid;
    // 商户号Id
    @Value("${partner}")
    private String partner;
    // 密钥
    @Value("${partnerkey}")
    private String partnerkey;


    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo) {

        PaymentInfo info = paymentInfoMapper.selectOne(paymentInfo);

        return info;
    }

    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfo) {

        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }

    @Override
    public Boolean refund(String orderId) {


        //通过orderId获取数据
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        String outTradeNo = orderInfo.getOutTradeNo();

        //AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key");
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",outTradeNo);
        map.put("refund_amount",orderInfo.getTotalAmount());
        map.put("refund_reason","不买了");
        request.setBizContent(JSON.toJSONString(map));

//        request.setBizContent("{" +
//                "    \"out_trade_no\":\"20150320010101001\"," +
//                "    \"trade_no\":\"2014112611001004680073956707\"," +
//                "    \"refund_amount\":200.12," +
//                "    \"refund_reason\":\"正常退款\"," +
//                "    \"out_request_no\":\"HZ01RF001\"," +
//                "    \"operator_id\":\"OP001\"," +
//                "    \"store_id\":\"NJ_S_001\"," +
//                "    \"terminal_id\":\"NJ_T_001\"" +
//                "  }");
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            //更新状态
            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }
    }

    @Override
    public Map<String,String> createNative(String orderId, String s) {

        //制作参数
        Map<String,String> map = new HashMap<>();
        map.put("appid",appid);
        map.put("mch_id",partner);
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        map.put("body","买手机");
        map.put("out_trade_no",orderId);
        map.put("total_fee",s);
        map.put("spbill_create_ip","127.0.0.1");
        map.put("notify_url","http://order.gmall.com/trade");
        map.put("trade_type","Native");


        try {
            //将map转为xml
            String signedXml = WXPayUtil.generateSignedXml(map, partnerkey);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //设置https请求
            httpClient.setHttps(true);
            //将signedXml发送到接口上
            httpClient.setXmlParam(signedXml);
            //设置post请求
            httpClient.post();
            //将结果放入map中,返回map
            Map<String,String> resultMap = new HashMap<>();
            String content = httpClient.getContent();
            Map<String, String> stringMap = WXPayUtil.xmlToMap(content);

            //将获得的结果放入resultMap中
            resultMap.put("code_url",stringMap.get("code_url"));
            resultMap.put("total_fee",s);
            resultMap.put("out_trade_no",orderId);

            return resultMap;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {

        //创建连接
        Connection connection = activeMQUtil.getConnection();

        try {
            //打开连接
            connection.start();
            //创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建队列
            Queue payment_result_queue = session.createQueue("PAYMENT_RESULT_QUEUE");
            //创建消息提供者
            MessageProducer producer = session.createProducer(payment_result_queue);
            //创建消息对象
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("orderId",paymentInfo.getOrderId());
            activeMQMapMessage.setString("result",result);

            //发送消息
            producer.send(activeMQMapMessage);
            //提交
            session.commit();
            //关闭
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
