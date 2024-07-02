package org.coketom.service.impl;


import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.coketom.exception.TomException;
import org.coketom.properties.MinioProperties;
import org.coketom.service.FileUploadService;
import org.coketom.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private MinioProperties minioProperties;

    @Override
    public String upload(MultipartFile file) {
        try {
            // 创建MinioClient对象
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(minioProperties.getEndpointUrl())
                            .credentials(minioProperties.getAccessKey(),
                                    minioProperties.getSecreKey())
                            .build();

            // 创建bucket
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
            if (!found) {
                // Make a new bucket called 'asiatrip'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
            } else {
                System.out.println("Bucket 'asiatrip' already exists.");
            }

            //获取上传文件名称
            // 1 每个上传文件名称唯一的   uuid生成 01.jpg
            //2 根据当前日期对上传文件进行分组 20230910
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Calendar calendar = Calendar.getInstance(); // 获取当前日期
            String dateDir = sdf.format(calendar.getTime());
            // 20230910/u7r54209l097501.jpg
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            String filename = dateDir+"/"+uuid+file.getOriginalFilename();

            // 文件上传
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(minioProperties.getBucketName())
                            .object(filename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build());

            //获取上传文件在minio路径
            //http://127.0.0.1:9000/spzx-bucket/01.jpg
            String url = minioProperties.getEndpointUrl()+"/"+minioProperties.getBucketName()+"/"+filename;

            return url;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TomException(ResultCodeEnum.SYSTEM_ERROR);
        }
    }
}

