package com.sun.bilibili.api;

import com.sun.bilibili.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoApi {
    @Autowired
    private DemoService service1;

    @GetMapping("/query")
    public long query(long id){
        return service1.query(id);
    }
}
