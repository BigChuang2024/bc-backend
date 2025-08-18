package com.haderacher.bcbackend.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MinioService {

    @Autowired
    MinioClient minioClient;

    public String uploadFileToMinio(MultipartFile file,String userId, String bucket) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        String originalFilename = file.getOriginalFilename();
        // 使用 UUID 生成唯一的文件名，防止重名覆盖
        String fileKey = UUID.randomUUID() + "-" + originalFilename;

        // 设置对象的元数据 (可选)
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        metadata.put("userId", userId);

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(fileKey)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .userMetadata(metadata)
                        .build()
        );

        return fileKey;
    }

    public byte[] getFileBytesFromMinio(String fileKey, String bucket) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        try (InputStream stream = minioClient.getObject(GetObjectArgs
                .builder()
                .bucket(bucket)
                .object(fileKey)
                .build())) {
            return stream.readAllBytes();
        }
    }
}
