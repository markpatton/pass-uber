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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import software.amazon.awssdk.regions.Region;

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
    private static final Logger LOG = LoggerFactory.getLogger(StorageProperties.class);
    private final String ocflDir = "pass-core-ocfl";
    private final String ocflWorkDir = "pass-core-ocfl-work";
    private final String tempDir = "pass-core-temp";
    private String rootDir;
    private String storageType;
    private String s3BucketName;
    private String s3Region;
    private String s3Endpoint;
    private String s3RepoPrefix;

    /**
     * Get the root directory for the File Service. This is the absolute path of the root directory.
     * @return The root directory of the File Service storage
     */
    public String getStorageRootDir() {
        return rootDir;
    }

    /**
     * Get the relative path of the OCFL storage directory. This directory is a child of the root directory.
     *
     * @return The logical path of the OCFL storage directory
     */
    public String getStorageOcflDir() {
        return ocflDir;
    }

    /**
     * Get the relative path of the OCFL working directory. This directory must be on the same
     * mount as the OCFL root directory. This directory is a child of the root directory.
     * @return The logical path of the OCFL working directory
     */
    public String getStorageWorkDir() {
        return ocflWorkDir;
    }

    /**
     * Get the relative path of the temp directory. This directory is a child of the root directory.
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

    /**
     * Get the S3 repo prefix that is configured for the File Service
     * @return The S3 bucket name of the File Service
     */
    public Optional<String> getS3RepoPrefix() {
        return Optional.ofNullable(s3RepoPrefix);
    }

    /**
     * Get the S3 bucket name that is configured for the File Service
     * @return The S3 bucket name of the File Service
     */
    public Optional<String> getBucketName() {
        return Optional.of(s3BucketName);
    }

    /**
     * Get the S3 region that is configured for the File Service
     * @return The S3 region of the File Service
     */
    public Optional<Region> getRegion() {
        for (Region r : Region.regions()) {
            if (r.toString().toLowerCase().equals(s3Region)) {
                return Optional.of(r);
            }
        }
        return null;
    }

    /**
     * Get the S3 endpoint that is configured for the File Service
     * @return The S3 endpoint of the File Service
     */
    public Optional<String> getS3Endpoint() {
        return Optional.ofNullable(s3Endpoint);
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public void setS3BucketName(String bucketName) {
        this.s3BucketName = bucketName;
    }

    public void setS3RepoPrefix(String s3RepoPrefix) {
        this.s3RepoPrefix = s3RepoPrefix;
    }

    public void setS3Region(String region) {
        this.s3Region = region;
    }

    public void setS3Endpoint(String endpoint) {
        this.s3Endpoint = endpoint;
    }

}
