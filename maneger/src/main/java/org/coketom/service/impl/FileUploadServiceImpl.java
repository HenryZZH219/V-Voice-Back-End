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
import java.util.*;

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
                System.out.println("Bucket not exists.");
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

            String contentType = getContentTypeByFileExtension(file.getOriginalFilename());
            // 文件上传
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(minioProperties.getBucketName())
                            .object(filename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
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

    // 根据文件后缀确定Content-Type
    private static String getContentTypeByFileExtension(String fileName) {
        // 定义文件后缀与Content-Type的映射
        Map<String, String> mimeTypes = new HashMap<>();
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("bmp", "image/bmp");
        mimeTypes.put("webp", "image/webp");

        // 提取文件后缀
        String fileExtension = getFileExtension(fileName).toLowerCase();

        // 返回对应的Content-Type，默认为application/octet-stream
        return mimeTypes.getOrDefault(fileExtension, "application/octet-stream");
    }

    // 提取文件后缀名
    private static String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');
        return (lastIndexOfDot == -1) ? "" : fileName.substring(lastIndexOfDot + 1);
    }
}

