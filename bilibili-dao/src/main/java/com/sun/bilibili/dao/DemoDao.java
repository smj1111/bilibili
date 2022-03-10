package com.sun.bilibili.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface DemoDao {
    public long query(long id);
}
