/*
 * Copyright 2023 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.pass.file.service.storage;

import java.io.IOException;

import io.findify.s3mock.S3Mock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

class FileStorageServiceS3Test extends FileStorageServiceTest {
    private S3Mock s3MockApi;

    /**
     * Set up the test environment. Uses custom endpoint for the in-memory S3 mock.
     */
    @BeforeEach
    @Override
    protected void setUp() throws IOException {
        s3MockApi = new S3Mock.Builder().withPort(8001).withInMemoryBackend().build();
        s3MockApi.start();
        properties.setStorageType(StorageServiceType.S3);
        properties.setRootDir(ROOT_DIR);
        properties.setS3Endpoint("http://localhost:8001");
        properties.setS3BucketName("bucket-test-name");
        properties.setS3RepoPrefix("s3-repo-prefix");
        StorageConfiguration storageConfiguration = new StorageConfiguration(properties);

        // Set properties to make the credentials provider happy
        System.setProperty("aws.accessKeyId", "A B C");
        System.setProperty("aws.secretAccessKey", "D E F");

        storageService = new FileStorageService(storageConfiguration, "us-east-1");
    }

    /**
     * Tear down the test environment. Deletes the temporary directory.
     */
    @AfterEach
    @Override
    protected void tearDown() throws IOException {
        s3MockApi.stop();
        super.tearDown();
    }
}