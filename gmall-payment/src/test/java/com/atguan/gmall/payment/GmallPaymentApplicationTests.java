package com.atguan.gmall.payment;

import com.atguan.gmall.config.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPaymentApplicationTests {


    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Test
    public void contextLoads() {
    }

    @Test
    public void testMQ() throws JMSException {

        Connection connection = activeMQUtil.getConnection();

        //开启连接
        connection.start();

        //创建session;第一个参数表示是否支持事务，第二个参数表示开启/关闭事务的相应参数
        //Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        //session开启事务
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

        //创建队列
        Queue atgaun = session.createQueue("atgaun-true");
        //创建消息提供者
        MessageProducer producer = session.createProducer(atgaun);
        //创建消息对象
        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText("很难受");
        //发送消息
        producer.send(activeMQTextMessage);
        //提交事务
        session.commit();
        //关闭
        producer.close();
        session.close();
        connection.close();
    }

}
