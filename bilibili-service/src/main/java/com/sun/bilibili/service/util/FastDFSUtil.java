package com.sun.bilibili.service.util;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.sun.bilibili.domain.exception.ConditionException;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import java.io.*;
import java.util.*;

@Component
public class FastDFSUtil {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    private static final String DEFAULT_GROUP="group1";

    public String getFileType(MultipartFile file){
        if(file==null){
            throw new ConditionException("非法文件！");
        }
        String fileName=file.getOriginalFilename();
        int index=fileName.lastIndexOf(".");
        return fileName.substring(index+1);
    }

    //上传
    public String uploadCommonFile(MultipartFile file) throws IOException {
        Set<MetaData> metaDataSet=new HashSet<>();
        String type=this.getFileType(file);
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), type, metaDataSet);
        return storePath.getPath();
    }
    public String uploadCommonFile(File file, String fileType) throws Exception {
        Set<MetaData> metaDataSet = new HashSet<>();
        StorePath storePath = fastFileStorageClient.uploadFile(new FileInputStream(file),
                file.length(), fileType, metaDataSet);
        return storePath.getPath();
    }

    public String uploadAppenderFile(MultipartFile file) throws Exception {
        String fileName=file.getOriginalFilename();
        String fileType=this.getFileType(file);
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(), file.getSize(), fileType);
        return storePath.getPath();
    }

    public void modifyAppenderFile(MultipartFile file,String filePath,long offSet) throws Exception{
        appendFileStorageClient.modifyFile(DEFAULT_GROUP,filePath,file.getInputStream(),file.getSize(),offSet);
    }

    private static final String PATH_KEY="path-key:";

    private static final String UPLOADED_SIZE_KEY="uploaded-size-key:";

    private static final String UPLOADED_NO_KEY="uploaded-no-key:";

    private static final int SLICE_SIZE=1024*1024;

    public String uploadFileBySlices(MultipartFile file,String fileMd5,Integer sliceNo,Integer totalSlicesNo) throws Exception {
        if(file==null||sliceNo==null||totalSlicesNo==null){
            throw new ConditionException("参数异常!");
        }
        String pathKey=PATH_KEY+fileMd5;
        String uploadedSizeKey=UPLOADED_SIZE_KEY+fileMd5;
        String uploadedNoKey=UPLOADED_NO_KEY+fileMd5;
        String uploadedSizeStr=redisTemplate.opsForValue().get(uploadedSizeKey);
        Long uploadedSize=0L;
        if(!StringUtil.isNullOrEmpty(uploadedSizeStr)){
            uploadedSize=Long.valueOf(uploadedSizeStr);
        }
        String fileType=this.getFileType(file);
        if(sliceNo==1){
            String path=this.uploadAppenderFile(file);
            if(StringUtil.isNullOrEmpty(path)){
                throw new ConditionException("上传失败！");
            }
            redisTemplate.opsForValue().set(pathKey,path);
            redisTemplate.opsForValue().set(uploadedNoKey,"1");
        }else {
            String filePath=redisTemplate.opsForValue().get(pathKey);
            if(StringUtil.isNullOrEmpty(filePath)){
                throw new ConditionException("上传失败！");
            }
            this.modifyAppenderFile(file,filePath,uploadedSize);
            redisTemplate.opsForValue().increment(uploadedNoKey);
        }
        uploadedSize+=file.getSize();
        redisTemplate.opsForValue().set(uploadedSizeKey,String.valueOf(uploadedSize));
        String uploadedNoStr = redisTemplate.opsForValue().get(uploadedNoKey);
        Integer uploadedNo = Integer.valueOf(uploadedNoStr);
        String resultPath="";
        if(uploadedNo.equals(totalSlicesNo)){
            resultPath=redisTemplate.opsForValue().get(pathKey);
            List<String> keyList= Arrays.asList(pathKey,uploadedNoKey,uploadedSizeKey);
            redisTemplate.delete(keyList);
        }
        return resultPath;
    }

    public void convertFileToSlices(MultipartFile multipartFile) throws IOException {
        String fileName=multipartFile.getOriginalFilename();
        String fileType=this.getFileType(multipartFile);
        File file=this.multipartFileToFile(multipartFile);
        Long fileLength=file.length();
        int count=1;
        for(int i=0;i<fileLength;i+=SLICE_SIZE){
            RandomAccessFile randomAccessFile=new RandomAccessFile(file,"r");
            randomAccessFile.seek(i);
            byte[] bytes=new byte[SLICE_SIZE];
            int len=randomAccessFile.read(bytes);
            String path="D:\\smj\\Documents\\slice\\"+count+"."+fileType;
            File slice=new File(path);
            FileOutputStream fos=new FileOutputStream(slice);
            fos.write(bytes,0,len);
            fos.close();
            randomAccessFile.close();
            count++;
        }
        file.delete();
    }

    public File multipartFileToFile(MultipartFile multipartFile) throws IOException {
        String originalFileName=multipartFile.getOriginalFilename();
        String[] fileName=originalFileName.split("\\.");
        File file=File.createTempFile(fileName[0],"."+fileName[1]);
        multipartFile.transferTo(file);
        return file;
    }

    //删除
    public void deleteFile(String filePath){
        fastFileStorageClient.deleteFile(filePath);
    }

    @Value("${fdfs.http.storage-addr}")
    private String httpFdfsStorageAddr;

    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String path) throws Exception{
        FileInfo fileInfo=fastFileStorageClient.queryFileInfo(DEFAULT_GROUP,path);
        long totalFileSize=fileInfo.getFileSize();
        String url=httpFdfsStorageAddr+path;
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String,Object> headers=new HashMap<>();
        while(headerNames.hasMoreElements()){
            String header = headerNames.nextElement();
            headers.put(header,request.getHeader(header));
        }
        String rangesStr=request.getHeader("Range");
        String[] range;
        if (StringUtil.isNullOrEmpty(rangesStr)){
            rangesStr="bytes=0-"+(totalFileSize-1);
        }
        range=rangesStr.split("bytes=|-");
        long begin=0;
        if(range.length>=2){
            begin=Long.parseLong(range[1]);
        }
        long end=totalFileSize-1;
        if (range.length>=3){
            end=Long.parseLong(range[2]);
        }
        long len=(end-begin)+1;
        String contentRange="type "+begin+"-"+end+"/"+totalFileSize;
        response.setHeader("Content-Range",contentRange);
        response.setHeader("Accept-Ranges","bytes");
        response.setHeader("Content-Type","video/mp4");
        response.setContentLength((int)len);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        HttpUtil.get(url,headers,response);
    }

    public void downLoadFile(String url, String localPath) {
        fastFileStorageClient.downloadFile(DEFAULT_GROUP, url,
                new DownloadCallback<String>() {
                    @Override
                    public String recv(InputStream ins) throws IOException {
                        File file = new File(localPath);
                        OutputStream os = new FileOutputStream(file);
                        int len = 0;
                        byte[] buffer = new byte[1024];
                        while ((len = ins.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        os.close();
                        ins.close();
                        return "success";
                    }
                });
    }
}
