package com.sun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class bilibiliApp {
    public static void main(String[] args) {
        ApplicationContext app= SpringApplication.run(bilibiliApp.class,args);
    }
}
