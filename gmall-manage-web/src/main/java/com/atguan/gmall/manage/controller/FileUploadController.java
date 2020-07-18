package com.atguan.gmall.manage.controller;


import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
public class FileUploadController {


    @Value("${fileServer.url}")
    private String fileUrl;

    @RequestMapping("/fileUpload")
    public String fileUpload(MultipartFile file)throws IOException, MyException {

        String imgUrl = fileUrl;

        if (file != null) {

            String configFile  = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(configFile);
            TrackerClient trackerClient=new TrackerClient();
            TrackerServer trackerServer=trackerClient.getTrackerServer();
            StorageClient storageClient=new StorageClient(trackerServer,null);
            //获取上传文件名称
            String originalFilename = file.getOriginalFilename();

            //获取文件后缀
            //String ext = originalFilename.substring(originalFilename.lastIndexOf("."));

            String ext = StringUtils.substringAfterLast(originalFilename, ".");

            //String orginalFilename="D://图片//1.jpg";
            //String[] upload_file = storageClient.upload_file(originalFilename,ext, null);

            String[] upload_file = storageClient.upload_file(file.getBytes(),ext, null);
            for (int i = 0; i < upload_file.length; i++) {
                String s = upload_file[i];
                System.out.println("s = " + s);


                imgUrl+="/"+s;
            }
        }


        return imgUrl;
    }
}
