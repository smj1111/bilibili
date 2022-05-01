package com.sun.bilibili.service.webSocket;

import com.alibaba.fastjson.JSONObject;
import com.sun.bilibili.domain.Danmu;
import com.sun.bilibili.domain.constant.UserMomentsConstant;
import com.sun.bilibili.service.DanmuService;
import com.sun.bilibili.service.util.RocketMQUtil;
import com.sun.bilibili.service.util.TokenUtil;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/imserver/{token}")
public class WebSocketService {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private static final AtomicInteger ONLINE_COUNT=new AtomicInteger(0);

    public static final ConcurrentHashMap<String,WebSocketService> WEBSOCKET_MAP=new ConcurrentHashMap<>();

    private Session session;

    private String sessionId;

    private Long userId;

    private static ApplicationContext APPLICATION_CONTEXT;

    public static void setApplicationContext(ApplicationContext applicationContext){
        WebSocketService.APPLICATION_CONTEXT=applicationContext;
    }

    @OnOpen
    public void OpenConnection(Session session, @PathParam("token") String token){
        try {
            this.userId = TokenUtil.verifyToken(token);
        }catch (Exception e){}
        this.sessionId=session.getId();
        this.session=session;
        if(WEBSOCKET_MAP.containsKey(sessionId)){
            WEBSOCKET_MAP.remove(sessionId);
            WEBSOCKET_MAP.put(sessionId,this);
        }else {
            WEBSOCKET_MAP.put(sessionId,this);
            ONLINE_COUNT.getAndIncrement();
        }
        logger.info("用户连接成功："+sessionId+"当前连接人数："+ONLINE_COUNT.get());
        try {
            this.sendMessage("0");
        }catch (Exception e){
            logger.error("连接异常");
        }
    }

    @OnClose
    public void closeConnection(){
        if(WEBSOCKET_MAP.containsKey(sessionId)){
            WEBSOCKET_MAP.remove(sessionId);
            ONLINE_COUNT.getAndDecrement();
        }
        logger.info("用户退出："+sessionId+"当前连接人数："+ONLINE_COUNT);
    }

    @OnMessage
    public void onMessage(String message){
        logger.info("用户信息："+sessionId+"报文"+message);
        if(!StringUtil.isNullOrEmpty(message)){
            try {
                //群发
                for(Map.Entry<String,WebSocketService> entry:WEBSOCKET_MAP.entrySet()){
                    WebSocketService webSocketService=entry.getValue();
                    DefaultMQProducer danmusProducer=(DefaultMQProducer) APPLICATION_CONTEXT.getBean("danmusProducer");
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("message",message);
                    jsonObject.put("sessionId",webSocketService.getSessionId());
                    Message msg=new Message(UserMomentsConstant.TOPIC_DANMUS,jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
                    RocketMQUtil.asyncSendMessage(danmusProducer,msg);
                }
                if(this.userId!=null) {
                    Danmu danmu = JSONObject.parseObject(message, Danmu.class);
                    danmu.setUserId(userId);
                    danmu.setCreateTime(new Date());
                    DanmuService danmuService=(DanmuService) APPLICATION_CONTEXT.getBean("danmuService");
                    danmuService.asyncAddDanmu(danmu);
                    danmuService.addDanmuToRedis(danmu);
                }

            }catch (Exception e){
                logger.error("弹幕接收出现问题！");
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Throwable error){
        System.out.println("onerror......"+error.getMessage());
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    @Scheduled(fixedRate = 5000)
    public void noticeOnlineCount() throws Exception{
        for(Map.Entry<String,WebSocketService> entry:WEBSOCKET_MAP.entrySet()){
            WebSocketService webSocketService=entry.getValue();
            if(webSocketService.getSession().isOpen()){
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("onlineCount",ONLINE_COUNT.get());
                jsonObject.put("msg","当前在线人数："+ONLINE_COUNT.get());
                webSocketService.sendMessage(jsonObject.toJSONString());
            }
        }
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
