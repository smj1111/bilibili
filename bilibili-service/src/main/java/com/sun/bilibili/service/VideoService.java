package com.sun.bilibili.service;

import com.sun.bilibili.dao.VideoDao;
import com.sun.bilibili.domain.PageResult;
import com.sun.bilibili.domain.Video;
import com.sun.bilibili.domain.VideoTag;
import com.sun.bilibili.domain.exception.ConditionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class VideoService {

    @Autowired
    private VideoDao videoDao;

    @Transactional
    public void adaVideos(Video video) {
        Date now=new Date();
        video.setCreateTime(now);
        videoDao.addVideos(video);
        Long videoId=video.getId();
        List<VideoTag> tagList=video.getVideoTagList();
        for(VideoTag tag:tagList){
            tag.setCreateTime(now);
            tag.setVideoId(videoId);
        }
        videoDao.batchAddVideoTags(tagList);
    }

    public PageResult<Video> pageListVideos(Integer size, Integer no, String area) {
        if (size==null||no==null){
            throw new ConditionException("参数异常！");
        }
        Map<String,Object> params=new HashMap<>();
        params.put("start",(no-1)*size);
        params.put("limit",size);
        params.put("area",area);
        List<Video> list=new ArrayList<>();
        Integer total=videoDao.pageCountVideos(params);
        if (total>0){
            list=videoDao.pageListVideos(params);
        }
        return new PageResult<>(total,list);
    }
}
