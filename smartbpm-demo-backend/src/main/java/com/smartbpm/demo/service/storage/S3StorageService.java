package com.smartbpm.demo.service.storage;

import com.smartbpm.demo.config.SmartBpmProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final SmartBpmProperties properties;

    public S3StorageService(S3Client s3Client, SmartBpmProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    public String storeText(String processId, String folder, String fileName, String content) {
        return storeBytes(processId, folder, fileName, content.getBytes(), "text/plain");
    }

    @Override
    public String storeBytes(String processId, String folder, String fileName, byte[] content, String contentType) {
        String key = processId + "/" + folder + "/" + fileName;
        s3Client.putObject(builder -> builder.bucket(properties.getStorage().getS3Bucket())
                        .key(key)
                        .contentType(contentType),
                RequestBody.fromBytes(content));
        return key;
    }

    @Override
    public byte[] read(String key) {
        return s3Client.getObjectAsBytes(builder -> builder
                        .bucket(properties.getStorage().getS3Bucket())
                        .key(key))
                .asByteArray();
    }
}
