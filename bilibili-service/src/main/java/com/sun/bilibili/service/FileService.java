package com.sun.bilibili.service;

import com.sun.bilibili.dao.FileDao;
import com.sun.bilibili.domain.File;
import com.sun.bilibili.service.util.FastDFSUtil;
import com.sun.bilibili.service.util.MD5Util;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Service
public class FileService {

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @Autowired
    private FileDao fileDao;

    public String uploadFileBySlices(MultipartFile slice, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws Exception {
        File dbFileMd5=fileDao.getFileByMd5(fileMd5);
        if(dbFileMd5!=null){
            return dbFileMd5.getUrl();
        }
        String url= fastDFSUtil.uploadFileBySlices(slice,fileMd5,sliceNo,totalSliceNo);
//        if (StringUtil.isNullOrEmpty(url))return "9";
        if(!StringUtil.isNullOrEmpty(url)){
            dbFileMd5=new File();
            dbFileMd5.setUrl(url);
            dbFileMd5.setCreateTime(new Date());
            dbFileMd5.setType(fastDFSUtil.getFileType(slice));
            dbFileMd5.setMd5(fileMd5);
            fileDao.addFile(dbFileMd5);
//            return "666";
        }
        return url;
    }

    public String getFileMd5(MultipartFile file) throws Exception {
        return MD5Util.getFileMD5(file);
    }

    public File getFileByMd5(String fileMd5) {
        return fileDao.getFileByMd5(fileMd5);
    }
}
