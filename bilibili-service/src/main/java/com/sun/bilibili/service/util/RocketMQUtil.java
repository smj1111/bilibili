package com.sun.bilibili.service.util;


import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.CountDownLatch2;
import org.apache.rocketmq.common.message.Message;

public class RocketMQUtil {
    public static void syncSendMessage(DefaultMQProducer producer, Message msg)throws Exception{
        SendResult result=producer.send(msg);
        System.out.println(result);
    }

    public static void asyncSendMessage(DefaultMQProducer producer, Message msg) throws Exception{
        int messageCount=2;
        CountDownLatch2 countDownLatch=new CountDownLatch2(messageCount);
        for(int i=0;i<messageCount;i++){
            producer.send(msg, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    countDownLatch.countDown();
                    System.out.println(sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable e) {
                    countDownLatch.countDown();
                    System.out.println("发送消息时异常"+e);
                    e.printStackTrace();
                }
            });

        }
    }
}
