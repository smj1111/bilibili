package com.sun.bilibili.api;

import com.sun.bilibili.domain.JsonResponse;
import com.sun.bilibili.service.ElasticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class SystemApi {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @GetMapping("/contents")
    public JsonResponse<List<Map<String,Object>>> getContents(@RequestParam String keyword,@RequestParam Integer pageNo,@RequestParam Integer pageSize) throws IOException {
        List<Map<String,Object>> re=elasticSearchService.getContents(keyword,pageNo,pageSize);
        return new JsonResponse<>(re);
    }
    
}
