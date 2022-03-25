package com.sun.bilibili.api;

import com.sun.bilibili.api.support.UserSupport;
import com.sun.bilibili.domain.JsonResponse;
import com.sun.bilibili.domain.auth.UserAuthorities;
import com.sun.bilibili.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAuthApi {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserAuthService userAuthService;

    @GetMapping("/user-authorities")
    public JsonResponse<UserAuthorities> getUserAuthorities(){
        Long userId=userSupport.getCurrentUserId();
        UserAuthorities userAuthorities=userAuthService.getUserAuthorities(userId);
        return new JsonResponse<>(userAuthorities);
    }

}
