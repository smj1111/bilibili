package com.sun.bilibili.dao;

import com.sun.bilibili.domain.File;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileDao {

    Integer addFile(File file);

    File getFileByMd5(String md5);

}
