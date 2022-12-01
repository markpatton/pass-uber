/*
 *
 * Copyright 2019 Johns Hopkins University
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
