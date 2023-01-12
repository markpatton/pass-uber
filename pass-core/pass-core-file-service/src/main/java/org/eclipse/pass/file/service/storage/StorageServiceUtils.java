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

import java.io.File;
import java.nio.file.Path;
import java.security.SecureRandom;

/**
 * Utility class that helps with File Service. The StorageServiceTypes defines the type of storage supported by the
 * File Service. The two types of persistence are supported: File Systems and S3 buckets
 *
 * @author Tim Sanders
 */
public final class StorageServiceUtils {

    public enum StorageServiceType {
        FILE_SYSTEM("FILE_SYSTEM"),
        S3("S3");

        public final String label;

        private StorageServiceType(String label) {
            this.label = label;
        }
    }

    /**
     * Private constructor to prevent instantiation of a utility class.
     */
    private StorageServiceUtils(){
    }

    /**
     * Generates a SecureRandom string containing a sequence of characters supplied by idCharSet at a length
     * supplied by the idLength.
     *
     * @param idCharSet the character set used to create the ID
     * @param idLength the length of the to be generated ID
     * @return A string that contains the characters supplied by the idCharSet the length specified by idLength
     */
    public static String generateId(String idCharSet, int idLength) {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder sb = new StringBuilder(idLength);
        for (int i = 0; i < idLength; i++) {
            sb.append(idCharSet.charAt(secureRandom.nextInt(idCharSet.length())));
        }
        return sb.toString();
    }

    public static Path getAbsoluteFileNamePath(Path path) {
        File[] listOfFiles = path.toFile().listFiles();
        return listOfFiles[0].toPath();
    }
}
