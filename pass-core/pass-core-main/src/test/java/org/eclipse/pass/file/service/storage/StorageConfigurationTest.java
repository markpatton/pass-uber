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

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;

import org.eclipse.pass.main.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Russ Poetker (rpoetke1@jh.edu)
 */
@ActiveProfiles("default-test")
public class StorageConfigurationTest extends IntegrationTest {

    @Autowired private StorageConfiguration storageConfiguration;

    @Test
    public void testDefaultValuesFromConfiguration() throws IOException {
        assertFalse(storageConfiguration.getStorageProperties().getStorageRootDir().isEmpty());
        assertFalse(storageConfiguration.getStorageProperties().getStorageRootDir().contains("#{null}"));
    }
}
