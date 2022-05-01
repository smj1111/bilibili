package com.sun.bilibili.api;

import com.sun.bilibili.domain.JsonResponse;
import com.sun.bilibili.domain.Video;
import com.sun.bilibili.service.DemoService;
import com.sun.bilibili.service.ElasticSearchService;
import com.sun.bilibili.service.util.FastDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class DemoApi {
    @Autowired
    private DemoService service1;

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @Autowired
    private ElasticSearchService elasticSearchService;


    @GetMapping("/query")
    public long query(long id){
        return service1.query(id);
    }

    @GetMapping("/slices")
    public void slices(MultipartFile multipartFile) throws IOException {
        fastDFSUtil.convertFileToSlices(multipartFile);
    }

    @GetMapping("/es-videos")
    public JsonResponse<Video> getEsVideos(String keyword){
        return new JsonResponse<>(elasticSearchService.getVideos(keyword));
    }
}
