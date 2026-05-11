package com.smartbpm.demo.service.storage;

public interface StorageService {
    String storeText(String processId, String folder, String fileName, String content);
    String storeBytes(String processId, String folder, String fileName, byte[] content, String contentType);
    byte[] read(String key);
}
