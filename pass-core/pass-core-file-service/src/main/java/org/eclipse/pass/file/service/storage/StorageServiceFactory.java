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

import org.eclipse.pass.file.service.FileServiceException;
import org.springframework.stereotype.Component;

@Component
public class StorageServiceFactory {

    /**
     * Class constructor
     */
    public StorageServiceFactory() {
    }

    /**
     * Creates the StorageService which is responsible for performing the persistence operations. The type of storage
     * is determined by the StorageProperties which looks at the application.yaml for the PASS_CORE_FILE_SERVICE_TYPE.
     * This environment variable is responsible for determining the type of storage pass-core uses.
     *
     * @return StorageService, the implementation will be determined by the StorageProperties
     * @throws FileServiceException
     */
    public StorageService createStorage(StorageProperties storageProperties) throws FileServiceException {
        StorageService storageService = null;
        String storageType = storageProperties.getStorageType();
        if (storageType != null) {
            switch (storageType.toUpperCase()) {
                case "FILE_SYSTEM":
                    storageService = new FileSystemStorageService(storageProperties);
                    break;
                case "AWS_S3":
                    //TODO will need to implement AWS S3
                    storageService = new S3StorageService(storageProperties);
                    break;
                default:
                    throw new FileServiceException("File Service: No Storage Type provided. Please check the .env " +
                            "and ensure that a PASS_CORE_FILE_SERVICE_TYPE value is provided");
            }
        }
        storageService.init();
        return storageService;
    }
}
