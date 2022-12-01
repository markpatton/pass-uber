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

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

//TODO implement
public class S3StorageService implements StorageService {

    private final StorageProperties storageProperties;
    public S3StorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public void init() {
        //TODO implement
    }

    @Override
    public StorageFile store(MultipartFile mFile) {
        //TODO implement
        return null;
    }

    @Override
    public ByteArrayResource loadAsResource(String fileId) {
        //TODO implement
        return null;
    }

    @Override
    public Boolean resourceExists(String fileId, String rootDir) {
        //TODO implement
        return null;
    }

    @Override
    public void delete(String fileId) {
        //TODO implement
    }

    @Override
    public String generateId() {
        //TODO implement
        return null;
    }

    @Override
    public String getResourceFileName(String fileId) {
        //TODO implement
        return null;
    }
}
