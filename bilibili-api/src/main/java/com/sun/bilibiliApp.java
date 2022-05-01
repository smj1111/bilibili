package com.sun;

import com.sun.bilibili.service.webSocket.WebSocketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class bilibiliApp {
    public static void main(String[] args) {
        ApplicationContext app= SpringApplication.run(bilibiliApp.class,args);
        WebSocketService.setApplicationContext(app);
    }
}
