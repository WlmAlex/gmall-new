package com.learn.gmall.manage.controller;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin
@RestController
public class FileController {

    @Value("${fastdfs.server}")
    private String server;

    @PostMapping("/fileUpload")
    public String fileUpload(MultipartFile file) throws IOException, MyException {

        String fileName = file.getOriginalFilename().split("\\.")[1];
        byte[] bytes = file.getBytes();
        ClientGlobal.init("fdfs_client.conf");
        TrackerClient client = new TrackerClient();
        TrackerServer trackerServer = client.getTrackerServer();
        StorageServer storageServer = client.getStoreStorage(trackerServer);
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
        String[] path = storageClient.upload_file(bytes, fileName, null);
        String filePath = server + path[0] + "/" + path[1];
        return filePath;
    }
}
