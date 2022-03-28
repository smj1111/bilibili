package com.sun.bilibili.api;

import com.sun.bilibili.domain.JsonResponse;
import com.sun.bilibili.service.FileService;
import com.sun.bilibili.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileApi {

    @Autowired
    private FileService fileService;

    @PutMapping("/file-slices")
    public JsonResponse<String> uploadFileBySlices(MultipartFile slice,String fileMd5,Integer sliceNo,Integer totalSliceNo) throws Exception {
        String filePath=fileService.uploadFileBySlices(slice,fileMd5,sliceNo,totalSliceNo);
        return new JsonResponse<>(filePath);
    }

    @PostMapping("/md5files")
    public JsonResponse<String> getFileMd5(MultipartFile file) throws Exception {
        String fileMd5=fileService.getFileMd5(file);
        return new JsonResponse<>(fileMd5);
    }

}
