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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class FileStorageServiceFileSystemTest extends FileStorageServiceTest {

    /**
     * Set up the FileStorageService for testing. Uses the system temp directory for the root directory.
     */
    @BeforeEach
    protected void setUp() {
    }

    /**
     * Cleanup the FileStorageService after testing. Deletes the root directory.
     */
    @AfterEach
    @Override
    protected void tearDown() throws IOException {
        super.tearDown();
    }
}
