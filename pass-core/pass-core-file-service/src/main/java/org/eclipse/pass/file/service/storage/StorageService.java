package org.eclipse.pass.file.service.storage;

import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void init();

    StorageFile store(MultipartFile mFile, String fileName);

    Path load(String fileId);

    Resource loadAsResource(String fileId);

    void delete(String fileId);

    String generateId();

}
