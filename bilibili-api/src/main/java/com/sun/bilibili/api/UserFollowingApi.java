package com.sun.bilibili.api;

import com.sun.bilibili.api.support.UserSupport;
import com.sun.bilibili.domain.FollowingGroup;
import com.sun.bilibili.domain.JsonResponse;
import com.sun.bilibili.domain.UserFollowing;
import com.sun.bilibili.service.UserFollowingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
public class UserFollowingApi {

    @Autowired
    private UserFollowingService userFollowingService;

    @Autowired
    private UserSupport userSupport;

    @PostMapping("/user-followings")
    public JsonResponse<String> addUserFollowing(@RequestBody UserFollowing userFollowing){
        Long userId=userSupport.getCurrentUserId();
        userFollowing.setUserId(userId);
        userFollowingService.addUserFollowings(userFollowing);
        return JsonResponse.success();
    }

    @GetMapping("/user-followings")
    public JsonResponse<List<FollowingGroup>> getUserFollowings() {
        Long userId=userSupport.getCurrentUserId();
        List<FollowingGroup> result =userFollowingService.getUserFollowings(userId);
        return new JsonResponse<>(result);
    }

    @GetMapping("/user-fans")
    public JsonResponse<List<UserFollowing>> getUserFans() {
        Long userId=userSupport.getCurrentUserId();
        List<UserFollowing> result =userFollowingService.getUserFans(userId);
        return new JsonResponse<>(result);
    }

    @PostMapping("/user-following-groups")
    public JsonResponse<Long> addUserFollowingGroups(@RequestBody FollowingGroup followingGroup){
        Long userId=userSupport.getCurrentUserId();
        followingGroup.setUserId(userId);
        Long groupId=userFollowingService.addUserFollowingGroups(followingGroup);
        return new JsonResponse<>(groupId);
    }

    @GetMapping("/user-followings-groups")
    public JsonResponse<List<FollowingGroup>> getUserFollowingGroups(){
        Long userId=userSupport.getCurrentUserId();
        List<FollowingGroup> list=userFollowingService.getUserFollowingGroups(userId);
        return new JsonResponse<>(list);
    }

}
