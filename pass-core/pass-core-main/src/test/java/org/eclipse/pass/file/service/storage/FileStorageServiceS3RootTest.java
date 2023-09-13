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

import io.findify.s3mock.S3Mock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * The FileStorageServiceS3RootTest class is a test class for the FileStorageService that uses the S3 mock server. This
 * set of tests is when the PASS_CORE_FILE_SERVICE_ROOT_DIR environment variable is set to a S3 bucket.
 *
 * The S3 configuration is managed through application-test-root-s3.yml profile. The AWS access key and secret need to be
 * set prior to the Application Context initializing, and in addition, the S3 mock server needs to be started before the
 * Application Context. The S3 mock server is stopped and started prior to each test. This test extends the
 * FileStorageServiceTest class, which contains the tests that are common to all FileStorageService configurations.
 * @see FileStorageServiceTest
 * @see FileStorageService
 */
@ActiveProfiles("test-root-S3")
class FileStorageServiceS3RootTest extends FileStorageServiceTest {
    private static S3Mock s3MockApi;
    private static final int S3_MOCK_PORT = 8010;

    // Set up the S3 mock server before the Application Context is loaded.
    static {
        s3MockApi = new S3Mock.Builder().withPort(S3_MOCK_PORT).withInMemoryBackend().build();
        s3MockApi.start();
    }

    /**
     * Set up the FileStorageService for testing. Set environment variables for AWS access key and secret key that are
     * required for the aws s3 client.
     * @param registry the dynamic property registry
     */
    @DynamicPropertySource
    static void setupBeforeAppContext(DynamicPropertyRegistry registry) {
        System.setProperty("aws.accessKeyId", "A B C");
        System.setProperty("aws.secretAccessKey", "D E F");

    }

    /**
     * Start the S3 mock server.
     */
    @BeforeEach
    protected void setUp() {
        s3MockApi.stop();
        s3MockApi.start();
    }

    /**
     * Stop the S3 mock server.
     */
    @AfterEach
    protected void stops3Server() {
        s3MockApi.stop();
    }

}