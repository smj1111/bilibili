package com.sun.bilibili.api;

import com.sun.bilibili.api.support.UserSupport;
import com.sun.bilibili.domain.JsonResponse;
import com.sun.bilibili.domain.User;
import com.sun.bilibili.domain.UserInfo;
import com.sun.bilibili.service.UserService;
import com.sun.bilibili.service.util.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserApi {
    @Autowired
    private UserService userService;

    @Autowired
    private UserSupport userSupport;

    @GetMapping("/users")
    public JsonResponse<User> getUserInfo(){
        Long userId=userSupport.getCurrentUserId();
        User user=userService.getUserInfo(userId);
        return new JsonResponse<>(user);
    }

    @GetMapping("/rsa-pks")
    public JsonResponse<String> getRsaPublicKey(){
        String pk= RSAUtil.getPublicKeyStr();
        return new JsonResponse<>(pk);
    }

    @PostMapping("/users")
    public JsonResponse<String> addUser(@RequestBody User user){
        userService.addUser(user);
        return JsonResponse.success();
    }

    @PostMapping("/user-tokens")
    public JsonResponse<String> login(@RequestBody User user) throws Exception {
        String token=userService.login(user);
        return new JsonResponse<>(token);
    }

    @PutMapping("/user-infos")
    public JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo){
        Long userId=userSupport.getCurrentUserId();
        userInfo.setUserId(userId);
        userService.updateUserInfos(userInfo);
        return JsonResponse.success();
    }

    @PutMapping("/users")
    public JsonResponse<String> updateUsers(@RequestBody User user){
        Long userId=userSupport.getCurrentUserId();
        user.setId(userId);
        userService.updateUsers(user);
        return JsonResponse.success();
    }

}
