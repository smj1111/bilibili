package com.sun.bilibili.service;

import com.sun.bilibili.dao.UserCoinDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserCoinService {

    @Autowired
    private UserCoinDao userCoinDao;

    public void updateUserCoinAmount(Long userId,Integer amount){
        Date updateTime=new Date();
        userCoinDao.updateUserCoinAmount(userId,amount,updateTime);
    }

    public Integer getUserCoinsAmount(Long userId) {
        return userCoinDao.getUserCoinsAmount(userId);
    }
}
