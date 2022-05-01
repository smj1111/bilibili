package com.sun.bilibili.api;

import com.sun.bilibili.api.support.UserSupport;
import com.sun.bilibili.domain.Danmu;
import com.sun.bilibili.domain.JsonResponse;
import com.sun.bilibili.service.DanmuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DanmuApi {

    @Autowired
    UserSupport userSupport;

    @Autowired
    private DanmuService danmuService;

    @GetMapping("/danmus")
    public JsonResponse<List<Danmu>> getDanmus(@RequestParam Long videoId,String startTime,String endTime)throws Exception{
        List<Danmu> list;
        try {
            userSupport.getCurrentUserId();
            list=danmuService.getDanmus(videoId,startTime,endTime);
        }catch (Exception e){
            list=danmuService.getDanmus(videoId,null,null);
        }
        return new JsonResponse<>(list);
    }

}
