package com.atguan.gmall.payment.mq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ConsumerTest {


    public static void main(String[] args) throws JMSException {

        //创建连接工厂
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_USER,ActiveMQConnectionFactory.DEFAULT_PASSWORD,"tcp://192.168.113.132:61616");

        //创建连接
        Connection connection = activeMQConnectionFactory.createConnection();

        //开启连接
        connection.start();

        //创建session
        Session session = connection.createSession(true,Session.SESSION_TRANSACTED);

        //创建队列
        Queue atguan = session.createQueue("atgaun-true");

        //创建consumer
        MessageConsumer consumer = session.createConsumer(atguan);

        //消费消息,设置一个监听器
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if (message instanceof TextMessage) {
                    try {
                        String text = ((TextMessage) message).getText();
                        System.err.println(text);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }

}
