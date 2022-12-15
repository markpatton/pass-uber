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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * StorageProperties are the File Service configuration properties indicating where and what type of storage is used
 * for persistence. The StorageProperties are managed by the StorageConfiguration.
 * Important: The OCFL root and working directory must be on the same mount.
 *
 * @author Tim Sanders
 * @see StorageConfiguration
 */
@ConfigurationProperties(prefix = "spring.file-service")
public class StorageProperties {
    private String ocflRootDir;
    private String ocflWorkDir;
    private String tempDir;
    private String storageType;

    /**
     * Get the logical absolute path of the storage root directory
     * @return The logical path of the OCFL storage root
     */
    public String getStorageRootDir() {
        return ocflRootDir;
    }

    /**
     * Get the logical absolute path of the OCFL working directory. This directory must be on the same
     * mount as the OCFL root directory
     * @return The logical path of the OCFL working directory
     */
    public String getStorageWorkDir() {
        return ocflWorkDir;
    }

    /**
     * Get the logical path of the temp directory. This directory is not the absolute path, but the relative
     * path of the temp directory. All temp files are stored within the {system_temp_directory}/tempDir
     * @return The logical path of the temp directory
     */
    public String getStorageTempDir() {
        return tempDir;
    }

    /**
     * Get the storage type that is configured for the File Service
     * @return The storage type of the File Service
     */
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
