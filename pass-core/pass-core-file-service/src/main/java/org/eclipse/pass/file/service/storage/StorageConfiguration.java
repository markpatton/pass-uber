/*
 *
 * Copyright 2023 Johns Hopkins University
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.eclipse.pass.file.service.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * The StorageConfiguration is responsible for handling the StorageProperties. The FileStorageService does not get the
 * storage configuration directly but through the StorageConfiguration.
 *
 * @author Tim Sanders
 * @see StorageProperties
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfiguration {
    private StorageProperties storageProperties;

    private StorageConfiguration() {
    }

    /**
     * StorageConfiguration constructor that initializes the StorageProperties.
     *
     * @param storageProperties are injected into the StorageConfiguration at startup.
     */
    public StorageConfiguration(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    /**
     * Gets the StorageProperties of the StorageConfiguration.
     *
     * @return An instance of the StorageProperties
     */
    public StorageProperties getStorageProperties() {
        return storageProperties;
    }

    /**
     * Sets the StorageProperties of the StorageConfiguration. The FileService is designed to load the application
     * variables defined in the .env file. To override the StorageProperties should be used with caution as there might
     * be unintended results. If the configuration of the FileService needs to be modified, do so in the .env file.
     *
     * @param storageProperties The StorageProperties to set in the StorageConfiguration.
     */
    public void setStorageProperties(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

}
