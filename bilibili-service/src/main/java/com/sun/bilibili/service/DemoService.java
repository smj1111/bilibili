package com.sun.bilibili.service;

import com.sun.bilibili.dao.DemoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DemoService {
    @Autowired
    private DemoDao dao1;
    public long query(long id){
        return dao1.query(id);
    }
}
