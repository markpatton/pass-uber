package org.eclipse.pass.file.service.storage;

import org.eclipse.pass.file.service.PassFileServiceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(PassFileServiceController.class);

    private StorageProperties storageProperties;

    public StorageConfiguration(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public StorageProperties getStorageProperties() {
        return storageProperties;
    }

}
