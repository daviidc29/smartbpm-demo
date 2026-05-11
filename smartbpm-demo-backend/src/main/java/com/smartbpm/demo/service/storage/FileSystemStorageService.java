package com.smartbpm.demo.service.storage;

import com.smartbpm.demo.config.SmartBpmProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemStorageService implements StorageService {

    private final Path root;

    public FileSystemStorageService(SmartBpmProperties properties) {
        this.root = Path.of(properties.getStorage().getFilesystemRoot());
    }

    @Override
    public String storeText(String processId, String folder, String fileName, String content) {
        return storeBytes(processId, folder, fileName, content.getBytes(StandardCharsets.UTF_8), "text/plain");
    }

    @Override
    public String storeBytes(String processId, String folder, String fileName, byte[] content, String contentType) {
        try {
            Path target = root.resolve(processId).resolve(folder).resolve(fileName);
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return target.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to store artifact on local filesystem", e);
        }
    }

    @Override
    public byte[] read(String key) {
        try {
            return Files.readAllBytes(Path.of(key));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read artifact from local filesystem", e);
        }
    }
}
