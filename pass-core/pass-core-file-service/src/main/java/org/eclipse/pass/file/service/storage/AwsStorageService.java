package org.eclipse.pass.file.service.storage;

import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

//TODO implement
public class AwsStorageService implements StorageService {

    private final StorageProperties storageProperties;
    public AwsStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public void init() {

    }

    @Override
    public StorageFile store(MultipartFile mFile, String fileName) {
        return null;
    }

    @Override
    public Path load(String fileId) {
        return null;
    }

    @Override
    public Resource loadAsResource(String fileId) {
        return null;
    }

    @Override
    public void delete(String fileId) {

    }

    @Override
    public String generateId() {
        return null;
    }
}
