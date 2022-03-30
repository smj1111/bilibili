package com.sun.bilibili.api;

import com.sun.bilibili.api.support.UserSupport;
import com.sun.bilibili.domain.JsonResponse;
import com.sun.bilibili.domain.PageResult;
import com.sun.bilibili.domain.Video;
import com.sun.bilibili.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoApi {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserSupport userSupport;

    @PostMapping("/videos")
    public JsonResponse<String> addVideos(Video video){
        Long userId=userSupport.getCurrentUserId();
        video.setUserId(userId);
        videoService.adaVideos(video);
        return JsonResponse.success();
    }

    @GetMapping("/videos")
    public JsonResponse<PageResult<Video>> pageListVideos(Integer size,Integer no,String area){
        PageResult<Video> result=videoService.pageListVideos(size,no,area);
        return new JsonResponse<>(result);
    }

}
