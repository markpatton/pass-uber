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

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

import edu.wisc.library.ocfl.api.OcflRepository;
import edu.wisc.library.ocfl.api.model.FileDetails;
import edu.wisc.library.ocfl.api.model.ObjectVersionId;
import edu.wisc.library.ocfl.api.model.VersionDetails;
import edu.wisc.library.ocfl.api.model.VersionInfo;
import edu.wisc.library.ocfl.aws.OcflS3Client;
import edu.wisc.library.ocfl.core.OcflRepositoryBuilder;
import edu.wisc.library.ocfl.core.extension.storage.layout.config.HashedNTupleLayoutConfig;
import edu.wisc.library.ocfl.core.path.constraint.ContentPathConstraints;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

/**
 * The FileStorageService is responsible for the implementation of the persistence of files to their respective
 * storage or repository. The types of storage are defined in the StorageServiceUtils. The FileStorageService depends
 * on a properly configured repository. The environment variables are externalized in the .ENV file. The
 * FileStorageService is lazily loaded to ensure that the configuration is properly loaded and to minimize the startup
 * time. The FileStorageService currently supports File System and S3 storage. A configuration of File System requires
 * that the environment variables are properly set in the env file and the respective directories have read/write
 * access. For a S3 configuration to work the client needs the following permissions: s3:PutObject, s3:GetObject,
 * s3:DeleteObject, s3:ListBucket, s3:AbortMultipartUpload.
 *
 * The directory structure for the File System is as follows:
 *  - rootDir: This is the root directory for the File System. This is set in the
 *      .env file (PASS_CORE_FILE_SERVICE_ROOT_DIR). If it is not set then the default is the system temp directory.
 *  - ocflDir: This is the directory where files are stored in the OCFL repository. This is a child of the rootDir.
 *  - workDir: This is a temporary working directory that is required by the OcflRepositoryBuilder. This is a child of
 *       the rootDir. Both the ocflDir and workDir are required to be on the same mount.
 *  - tempDir: This is a temporary directory that is used to move files to/from the OCFL repository and staging them
 *      for download. This is a child of the rootDir.
 *
 * Note, the S3 OCFL implementation does not cache locally and therefore performs much slower compared to the file
 * system implementation, most notably on large files.
 *
 * @author Tim Sanders
 * @see StorageServiceUtils
 */
@Lazy
@Service
public class FileStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(FileStorageService.class);
    private Path rootLoc;
    private Path ocflLoc;
    private Path workLoc;
    private Path tempLoc;
    private final int idLength = 25;
    private final String idCharSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private String storageType = "";
    private OcflRepository ocflRepository;
    private S3Client cloudS3Client;
    private StorageProperties storageProperties;
    private String bucketName;
    private String repoPrefix;
    private Region region;

    private FileStorageService(){
    }

    /**
     *  FileStorageService Class constructor.
     * @param storageConfiguration A set of configuration properties of the File Service. These properties are
     * set in the .env file in the pass-core-main module.
     * @throws FileSystemException If the File Service directories cannot be created and readable/writeble a
     * FileSystemException will be thrown
     */
    @Autowired
    public FileStorageService(StorageConfiguration storageConfiguration) throws IOException {
        this.storageProperties = storageConfiguration.getStorageProperties();
        storageType = this.storageProperties.getStorageType();

        if (this.storageProperties.getStorageRootDir() == null
                || this.storageProperties.getStorageRootDir().isEmpty()) {
            this.rootLoc = Paths.get(System.getProperty("java.io.tmpdir"));
        } else {
            this.rootLoc = Paths.get(this.storageProperties.getStorageRootDir());
        }

        // The ocflLoc only needs to be set if the storage type is file system.
        // If the storageType is S3 then workLoc and tempLoc are used because they are used with FILE_SYSTEM AND S3
        if (storageType.equals(StorageServiceUtils.StorageServiceType.FILE_SYSTEM.label)) {
            ocflLoc = Paths.get(this.rootLoc.toString(),this.storageProperties.getStorageOcflDir());
        }
        this.workLoc = Paths.get(this.rootLoc.toString(),this.storageProperties.getStorageWorkDir());
        this.tempLoc = Paths.get(this.rootLoc.toString(),this.storageProperties.getStorageTempDir());

        try {
            if (!Files.exists(rootLoc)) {
                Files.createDirectory(rootLoc);
            }
            if (!Files.exists(workLoc)) {
                Files.createDirectory(workLoc);
            }
            if (!Files.isReadable(workLoc) || !Files.isWritable(workLoc)) {
                throw new IOException("File Service: No permission to read/write work directory.");
            }
            if (!Files.isReadable(rootLoc) || !Files.isWritable(rootLoc)) {
                throw new IOException("File Service: No permission to read/write File Service root directory.");
            }
        } catch (IOException e) {
            throw new IOException("File Service: Unable to setup File Storage directories: " + e);
        }

        if (storageType.equals(StorageServiceUtils.StorageServiceType.FILE_SYSTEM.label)) {
            try {
                if (!Files.exists(ocflLoc)) {
                    Files.createDirectory(ocflLoc);
                }
                if (!Files.isReadable(ocflLoc) || !Files.isWritable(ocflLoc)) {
                    throw new IOException("File Service: No permission to read/write OCFL directory.");
                }
                ocflRepository = new OcflRepositoryBuilder()
                        .defaultLayoutConfig(new HashedNTupleLayoutConfig())
                        .storage(storage -> storage.fileSystem(this.ocflLoc))
                        .workDir(this.workLoc)
                        .build();
            } catch (IOException e) {
                throw new IOException("File Service: Unable to setup File Storage directories: " + e);
            }
        } else if (storageType.equals(StorageServiceUtils.StorageServiceType.S3.label)) {
            LOG.info("File Service: S3 Storage Type");
            if (storageProperties.getBucketName().isPresent()) {
                bucketName = storageProperties.getBucketName().get();
            } else {
                throw new IOException("File Service: S3 bucket name is not set");
            }

            if (storageProperties.getRegion().isPresent()) {
                region = storageProperties.getRegion().get();
            } else {
                throw new IOException("File Service: S3 region is not set");
            }

            //repoPrefix is not required
            if (storageProperties.getS3RepoPrefix().isPresent()) {
                repoPrefix = storageProperties.getS3RepoPrefix().get();
            }

            //endpoint is not required, but if one is supplied then S3 client is built with endpoint override
            if (storageProperties.getS3Endpoint().isPresent()) {
                String endpoint = storageProperties.getS3Endpoint().get();
                cloudS3Client = S3Client.builder()
                        .credentialsProvider(AnonymousCredentialsProvider.create())
                        .region(region)
                        .endpointOverride(URI.create(endpoint))
                        .build();
                LOG.info("File Service: S3 client built with endpoint override");
            } else if (!storageProperties.getS3Endpoint().isPresent() && storageProperties.getRegion().isPresent()) {
                //TODO credentials will need to be setup for S3
                cloudS3Client = S3Client.builder()
                        .credentialsProvider(AnonymousCredentialsProvider.create())
                        .region(region)
                        .build();
                LOG.info("File Service: S3 client built");
            }

            if (!cloudS3Client.listBuckets().buckets().stream().anyMatch(b -> b.name().equals(bucketName))) {
                cloudS3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            }

            if (cloudS3Client == null) {
                throw new IOException("File Service: S3 client is not configured");
            }

            //repoPrefix is optional and can be built with or without it
            if (storageProperties.getS3RepoPrefix().isPresent()) {
                ocflRepository = new OcflRepositoryBuilder()
                        .defaultLayoutConfig(new HashedNTupleLayoutConfig())
                        .contentPathConstraints(ContentPathConstraints.cloud())
                        .storage(storage -> storage
                                .cloud(OcflS3Client.builder()
                                        .s3Client(cloudS3Client)
                                        .bucket(bucketName)
                                        .repoPrefix(repoPrefix)
                                        .build()))
                        .workDir(workLoc)
                        .build();
            } else {
                ocflRepository = new OcflRepositoryBuilder()
                        .defaultLayoutConfig(new HashedNTupleLayoutConfig())
                        .contentPathConstraints(ContentPathConstraints.cloud())
                        .storage(storage -> storage
                                .cloud(OcflS3Client.builder()
                                        .s3Client(cloudS3Client)
                                        .bucket(bucketName)
                                        .build()))
                        .workDir(workLoc)
                        .build();
            }
            LOG.info("File Service: S3 client is configured and OCFL repository is built");
        } else {
            throw new IOException("File Service: File Service Type is missing or incorrect in the " +
                    "environment variables.");
        }
    }

    /**
     * Persists a file to the repository/storage indicated in the StorageProperties.
     *
     * @param mFile A MultiPart file that is to be persisted into storage or repository.
     * @return StorageFile representation of the file that was persisted. It contains meta information about the file
     * for example the name, file size and mime type.
     * @throws IOException If a file is empty or missing, paths are incorrect, or the appropriate permissions
     * are not configured on the repository an IOException will be thrown.
     *
     * @see StorageFile
     */
    public StorageFile storeFile(MultipartFile mFile) throws IOException {
        StorageFile storageFile = null;
        //NOTE: the work directory on the ocfl-java client should be located on the same mount as the OCFL storage root.
        try {
            if (mFile.isEmpty()) {
                throw new FileSystemException("File Service: File is empty or missing.");
            }
            String origFileNameExt = mFile.getOriginalFilename();
            String origFileName = FilenameUtils.removeExtension(origFileNameExt);
            String fileExt = FilenameUtils.getExtension(origFileNameExt);
            String fileId = StorageServiceUtils.generateId(idCharSet, idLength);
            String mimeType = URLConnection.guessContentTypeFromName(origFileNameExt);

            if (!Files.exists(Paths.get(tempLoc.toString()))) {
                Files.createDirectory(Paths.get(tempLoc.toString()));
            }
            Path tempPathAndFileName = Paths.get(tempLoc.toString(),(origFileName + fileId + "." + fileExt));
            mFile.transferTo(tempPathAndFileName);
            if (storageType.equals(StorageServiceUtils.StorageServiceType.FILE_SYSTEM.label)) {
                ocflRepository.putObject(ObjectVersionId.head(fileId), tempPathAndFileName,
                        new VersionInfo().setMessage("Pass-Core File Service: Initial commit"));
                LOG.info("File Service: File with ID " + fileId + " was stored in the file system repo");
            } else if (storageType.equals(StorageServiceUtils.StorageServiceType.S3.label)) {
                ocflRepository.putObject(ObjectVersionId.head(fileId), tempPathAndFileName,
                        new VersionInfo().setMessage("Pass-Core File Service: Initial commit"));
                LOG.info("File Service: File with ID " + fileId + " was stored in the S3 repo");
            }

            storageFile = new StorageFile(
                    fileId,
                    origFileNameExt,
                    mimeType,
                    storageType,
                    mFile.getSize(),
                    fileExt
            );

        } catch (IOException e) {
            LOG.error(e.toString());
            throw new IOException("File Service: The file system was unable to store the uploaded file", e);
        }

        return storageFile;
    }

    /**
     * Gets the file (bytes) of the supplied fileId.
     *
     * @param fileId The fileId of the file to be returned.
     * @return Returns a file as a ByteArrayResource
     * @throws IOException If a file does not exist or the appropriate read/write permissions are not correct an
     * IOException will be thrown.
     */
    public ByteArrayResource getFile(String fileId) throws IOException {
        LOG.info("File Service: init get file storageType " + storageType);
        ByteArrayResource loadedResource;
        Path tempLoadDir = Paths.get(this.tempLoc.toString(), fileId,
                Instant.now().toString().replace(":","-").replace(".","-"));
        Path tempLoadParentDir = Paths.get(this.tempLoc.toString(), fileId);
        try {
            if (storageType.equals(StorageServiceUtils.StorageServiceType.FILE_SYSTEM.label)) {
                String filePath = getResourceFileRelativePath(fileId);
                Path filePathAbs = Paths.get(this.ocflLoc.toString(), filePath);
            }
            LOG.info("File Service: get file storageType " + storageType);
            LOG.info("File Service: get file tempLoadDir " + tempLoadDir.toString());

            //need the parent directory for the OCFL getObject to work
            if (!Files.exists(tempLoadParentDir)) {
                Files.createDirectories(tempLoadParentDir);
            }
            // the output path for getObject must not exist, hence temp dir is created on the fly
            ocflRepository.getObject(ObjectVersionId.head(fileId), tempLoadDir);
            LOG.info("File Service: File with ID " + fileId + " was loaded from the repo");
            Path fileNamePath = StorageServiceUtils.getAbsoluteFileNamePath(tempLoadDir);
            loadedResource = new ByteArrayResource(Files.readAllBytes(fileNamePath));

        } catch (Exception e) {
            throw new IOException("File Service: The file could not be loaded, file ID: " + fileId + " " + e);
        }

        if (loadedResource.exists() && loadedResource.isReadable()) {
            //clean up temp directory
            if (!FileSystemUtils.deleteRecursively(Paths.get(this.tempLoc.toString()))) {
                LOG.info("File Service: No files to cleanup on file get");
            }
            return loadedResource;
        } else {
            throw new IOException("File Service: Unable to return the file. Verify read/write " +
                    "permissions of the temp directory.");
        }
    }

    /**
     * Deletes a file in storage or repository that is defined in the configuration
     * @param fileId The fileId of the file to be deleted
     */
    public void deleteFile(String fileId) {
        ocflRepository.purgeObject(fileId);
    }

    /**
     * Gets the relative path in the OCFL repository from the fileID supplied. It will return the most recent version
     * file path. When using S3, this will provide the path of the file in the S3 bucket.
     * @param fileId The fileId of the file path to be returned.
     * @return The relative path of the file.
     */
    public String getResourceFileRelativePath(String fileId) {
        VersionDetails versionDetails = ocflRepository.describeVersion(ObjectVersionId.head(fileId));
        Collection<FileDetails> allVersionFiles = versionDetails.getFiles();
        Optional<FileDetails> fileDetails = allVersionFiles.stream().findFirst();
        return fileDetails.get().getStorageRelativePath();
    }
}

