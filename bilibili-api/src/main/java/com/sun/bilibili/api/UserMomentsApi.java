package com.sun.bilibili.api;

import com.sun.bilibili.api.support.UserSupport;
import com.sun.bilibili.domain.JsonResponse;
import com.sun.bilibili.domain.UserMoment;
import com.sun.bilibili.domain.annotation.ApiLimitedRole;
import com.sun.bilibili.domain.annotation.DataLimited;
import com.sun.bilibili.domain.constant.AuthRoleConstant;
import com.sun.bilibili.service.UserMomentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserMomentsApi {

    @Autowired
    private UserMomentsService userMomentsService;

    @Autowired
    private UserSupport userSupport;

    @ApiLimitedRole(limitedRoleCodeList = {AuthRoleConstant.ROLE_LV0})
    @DataLimited
    @PostMapping("/user-moments")
    public JsonResponse<String> addUserMoments(@RequestBody UserMoment userMoment)throws Exception{
        Long userId=userSupport.getCurrentUserId();
        userMoment.setUserId(userId);
        userMomentsService.addUserMoments(userMoment);
        return JsonResponse.success();
    }

    @GetMapping("/user-subscribed-moments")
    public JsonResponse<List<UserMoment>> getUserSubscribedMoments(){
        Long userId=userSupport.getCurrentUserId();
        List<UserMoment> list=userMomentsService.getUserSubscribedMoments(userId);
        return new JsonResponse<>(list);
    }

}
