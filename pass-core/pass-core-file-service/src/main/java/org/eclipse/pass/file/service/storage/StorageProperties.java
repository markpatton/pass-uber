package org.eclipse.pass.file.service.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.file-service")
public class StorageProperties {
    private String ocflRootDir;
    private String ocflWorkDir;
    private String tempDir;
    private String storageType;

    public String getStorageRootDir() {
        return ocflRootDir;
    }

    public String getStorageWorkDir() {
        return ocflWorkDir;
    }

    public String getStorageTempDir() {
        return tempDir;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setOcflRootDir(String ocflRootDir) {
        this.ocflRootDir = ocflRootDir;
    }

    public void setOcflWorkDir(String ocflWorkDir) {
        this.ocflWorkDir = ocflWorkDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }
}
