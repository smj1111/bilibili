package com.sun.bilibili.service;

import com.mysql.cj.util.StringUtils;
import com.sun.bilibili.dao.UserDao;
import com.sun.bilibili.domain.User;
import com.sun.bilibili.domain.UserInfo;
import com.sun.bilibili.domain.constant.UserConstant;
import com.sun.bilibili.domain.exception.ConditionException;
import com.sun.bilibili.service.util.MD5Util;
import com.sun.bilibili.service.util.RSAUtil;
import com.sun.bilibili.service.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;


    public void addUser(User user) {
        String phone=user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)){
            throw new ConditionException("手机号不能为空");
        }
        User dbUser=this.getUserByPhone(phone);
        if (dbUser!=null){
            throw new ConditionException("改手机号已被注册");
        }
        Date now=new Date();
        String salt=String.valueOf(now.getTime());
        String password=user.getPassword();
        String rowPassword;
        try {
            rowPassword= RSAUtil.decrypt(password);
        }catch (Exception e){
            throw new ConditionException("解析密码失败");
        }
        String md5Password= MD5Util.sign(rowPassword,salt,"UTF-8");
        user.setSalt(salt);
        user.setPassword(md5Password);
        user.setCreateTime(now);
        userDao.addUser(user);
        UserInfo userInfo=new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setBirth(UserConstant.GENDER_BIRTH);
        userInfo.setGender(UserConstant.GENDER_MAIL);
        userInfo.setNick(UserConstant.GENDER_NICK);
        userInfo.setCreateTime(now);
        userDao.addUserInfo(userInfo);
    }

    public User getUserByPhone(String phone){
        return userDao.getUserByPhone(phone);
    }

    public String login(User user) throws Exception {
        String phone=user.getPhone();
        if(StringUtils.isNullOrEmpty(phone)){
            throw new ConditionException("手机号不能为空");
        }
        User dbUser=getUserByPhone(phone);
        if(dbUser==null){
            throw new ConditionException("该用户不存在");
        }
        String password=user.getPassword();
        String rawPassword;
        try {
            rawPassword=RSAUtil.decrypt(password);
        }catch (Exception e){
            throw new ConditionException("密码解析失败");
        }
        String salt= dbUser.getSalt();
        String md5Password=MD5Util.sign(rawPassword,salt,"UTF-8");
        if(!md5Password.equals(dbUser.getPassword())){
            throw new ConditionException("密码错误");
        }
        return TokenUtil.generateToken(dbUser.getId());
    }

    public User getUserInfo(Long userId) {
        User user=userDao.getUserById(userId);
        UserInfo userInfo=userDao.getUserInfoByUserId(userId);
        user.setUserInfo(userInfo);
        return user;
    }

    public void updateUserInfos(UserInfo userInfo) {
        userInfo.setUpdateTime(new Date());
        userDao.updateUserInfos(userInfo);
    }

    public void updateUsers(User user) {
        Long id = user.getId();
        User dbUser = userDao.getUserById(id);
        if(dbUser == null){
            throw new ConditionException("用户不存在！");
        }
        if(!StringUtils.isNullOrEmpty(user.getPassword())){
            String rawPassword = null;
            try {
                rawPassword = RSAUtil.decrypt(user.getPassword());
            } catch (Exception e) {
                new ConditionException("解析密码失败");
            }
            String md5Password = MD5Util.sign(rawPassword, dbUser.getSalt(), "UTF-8");
            user.setPassword(md5Password);
        }
        user.setUpdateTime(new Date());
        userDao.updateUsers(user);
    }
}
