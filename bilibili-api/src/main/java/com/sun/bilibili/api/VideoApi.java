package com.sun.bilibili.api;

import com.sun.bilibili.api.support.UserSupport;
import com.sun.bilibili.domain.*;
import com.sun.bilibili.service.ElasticSearchService;
import com.sun.bilibili.service.VideoService;
import org.apache.ibatis.annotations.Param;
import org.apache.mahout.cf.taste.common.TasteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
public class VideoApi {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @PostMapping("/videos")
    public JsonResponse<String> addVideos(Video video){
        Long userId=userSupport.getCurrentUserId();
        video.setUserId(userId);
        videoService.adaVideos(video);
        elasticSearchService.addVideo(video);
        return JsonResponse.success();
    }

    @GetMapping("/videos")
    public JsonResponse<PageResult<Video>> pageListVideos(Integer size,Integer no,String area){
        PageResult<Video> result=videoService.pageListVideos(size,no,area);
        return new JsonResponse<>(result);
    }

    @GetMapping("/video-slices")
    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response,String url)throws Exception{
        videoService.viewVideoOnlineBySlices(request,response,url);
    }

    @PostMapping("/video-likes")
    public JsonResponse<String> addVideoLike(@RequestParam Long videoId){
        Long userId=userSupport.getCurrentUserId();
        videoService.addVideoLike(videoId,userId);
        return JsonResponse.success();
    }

    @DeleteMapping("/video-likes")
    public JsonResponse<String> deleteVideoLike(@RequestParam Long videoId){
        Long userId=userSupport.getCurrentUserId();
        videoService.deleteVideoLike(videoId,userId);
        return JsonResponse.success();
    }

    @GetMapping("/video-likes")
    public JsonResponse<Map<String,Object>> getVideoLikes(@RequestParam Long videoId){
        Long userId=null;
        try {
            userId=userSupport.getCurrentUserId();
        }catch (Exception ignored){}
        Map<String,Object> result=videoService.getVideoLikes(videoId,userId);
        return new JsonResponse<>(result);
    }

    @PostMapping("/video-collections")
    public JsonResponse<String> addVideoCollection(@RequestBody VideoCollection videoCollection){
        Long userId=userSupport.getCurrentUserId();
        videoService.addVideoCollection(videoCollection,userId);
        return JsonResponse.success();
    }

    @DeleteMapping("/video-collections")
    public JsonResponse<String> deleteVideoCollection(@RequestParam Long videoId){
        Long userId=userSupport.getCurrentUserId();
        videoService.deleteVideoCollection(videoId,userId);
        return JsonResponse.success();
    }

    @GetMapping("/video-collections")
    public JsonResponse<Map<String,Object>> getVideoCollections(@RequestParam Long videoId){
        Long userId=null;
        try {
            userId=userSupport.getCurrentUserId();
        }catch (Exception ignored){}
        Map<String,Object> result=videoService.getVideoCollections(videoId,userId);
        return new JsonResponse<>(result);
    }

    @PostMapping("/video-coins")
    public JsonResponse<String> addVideoCoins(@RequestBody VideoCoin videoCoin){
        Long userId=userSupport.getCurrentUserId();
        videoService.addVideoCoins(videoCoin,userId);
        return JsonResponse.success();
    }

    @GetMapping("/video-coins")
    public JsonResponse<Map<String,Object>> getVideoCoins(@RequestParam Long videoId){
        Long userId=null;
        try {
            userId=userSupport.getCurrentUserId();
        }catch (Exception ignored){}
        Map<String,Object> result=videoService.getVideoCoins(videoId,userId);
        return new JsonResponse<>(result);
    }

    @PostMapping("/video-comments")
    public JsonResponse<String> addVideoComment(@RequestBody VideoComment videoComment){
        Long userId=userSupport.getCurrentUserId();
        videoService.addVideoComment(videoComment,userId);
        return JsonResponse.success();
    }

    @GetMapping("/video-comments")
    public JsonResponse<PageResult<VideoComment>> pageListVideoComments(@RequestParam Integer size,@RequestParam Integer no,@RequestParam Long videoId){
        PageResult<VideoComment> result=videoService.pageListVideoComments(size,no,videoId);
        return new JsonResponse<>(result);
    }

    @GetMapping("/video-details")
    public JsonResponse<Map<String,Object>> getVideoDetails(@RequestParam Long videoId){
        Map<String,Object> result=videoService.getVideoDetails(videoId);
        return new JsonResponse<>(result);
    }

    @PostMapping("/video-views")
    public JsonResponse<String> addVideoView(@RequestBody VideoView videoView,HttpServletRequest request){
        Long userId;
        try {
            userId=userSupport.getCurrentUserId();
            videoView.setUserId(userId);
            videoService.addVideoView(videoView,request);
        }catch (Exception e){
            videoService.addVideoView(videoView,request);
        }
        return JsonResponse.success();
    }

    @GetMapping("/video-view-counts")
    public JsonResponse<Integer> getVideoViewCounts(Long videoId){
        Integer count=videoService.getVideoViewCounts(videoId);
        return new JsonResponse<>(count);
    }

    @GetMapping("/recommendations")
    public JsonResponse<List<Video>> recommend() throws TasteException {
        Long userId=userSupport.getCurrentUserId();
        List<Video> list=videoService.recommend(userId);
        return new JsonResponse<>(list);
    }

    @GetMapping("/video-frames")
    public JsonResponse<List<VideoBinaryPicture>> captureVideoFrame(@RequestParam Long videoId,@RequestParam String fildMD5) throws Exception {
        List<VideoBinaryPicture> list=videoService.convertVideoToImage(videoId,fildMD5);
        return new JsonResponse<>(list);
    }

}
