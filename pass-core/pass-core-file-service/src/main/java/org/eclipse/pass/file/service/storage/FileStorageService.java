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
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * The FileStorageService is responsible for the implementation of the persistence of files to their respective
 * storage or repository. The types of storage are defined in the StorageServiceUtils. The FileStorageService depends
 * on a properly configured repository. The environment variables are externalized in the .ENV file. The
 * FileStorageService is lazily loaded to ensure that the configuration is properly loaded and to minimize the startup
 * time.
 *
 * @author Tim Sanders
 * @see StorageServiceUtils
 */
@Lazy
@Service
public class FileStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(FileStorageService.class);
    private Path rootLoc;
    private Path workLoc;
    private Path tempLoc;
    private final int idLength = 25;
    private final String idCharSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private String storageType = "";
    private OcflRepository ocflRepository;
    private S3Client cloudS3Client;
    private StorageProperties storageProperties;

    //TODO move to env and get from storage properties
    private String bucketName = "";
    private String repoPrefix = "";
    private Region region = Region.US_EAST_1;

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
    public FileStorageService(StorageConfiguration storageConfiguration) throws FileSystemException {
        this.storageProperties = storageConfiguration.getStorageProperties();
        storageType = this.storageProperties.getStorageType();
        if (storageType.equals(StorageServiceUtils.StorageServiceType.FILE_SYSTEM.label)) {
            this.rootLoc = Paths.get(this.storageProperties.getStorageRootDir());
            this.workLoc = Paths.get(this.storageProperties.getStorageWorkDir());
            this.tempLoc = Paths.get(this.storageProperties.getStorageTempDir());

            try {
                if (!Files.exists(rootLoc)) {
                    try {
                        Files.createDirectory(rootLoc);
                    } catch (IOException e) {
                        throw new FileSystemException("File Service: Root directory could not be created");
                    }
                }
                if (!Files.exists(workLoc)) {
                    try {
                        Files.createDirectory(workLoc);
                    } catch (IOException e) {
                        throw new FileSystemException("File Service: Work directory could not be created");
                    }
                }

                if (!Files.isReadable(rootLoc) || !Files.isWritable(rootLoc)) {
                    throw new FileSystemException("File Service: No permission to read/write root directory.");
                }
                if (!Files.isReadable(workLoc) || !Files.isWritable(workLoc)) {
                    throw new FileSystemException("File Service: No permission to read/write work directory.");
                }

                ocflRepository = new OcflRepositoryBuilder()
                        .defaultLayoutConfig(new HashedNTupleLayoutConfig())
                        .storage(storage -> storage.fileSystem(this.rootLoc))
                        .workDir(this.workLoc)
                        .build();

            } catch (FileSystemException e) {
                LOG.error(e.toString());
                throw new FileSystemException("File Service: Unable to setup File Storage directories: " + e);
            }
        } else if (storageType.equals(StorageServiceUtils.StorageServiceType.S3.label)) {
            //TODO IMPLEMENT

            ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();
            S3Client cloudS3Client = S3Client.builder()
                    .region(region)
                    .credentialsProvider(credentialsProvider)
                    .build();
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
            LOG.error("File Service: File Service Type is missing or incorrect in the environment variables.");
            throw new FileSystemException("File Service: File Service Type is missing or incorrect in the " +
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
        //NOTE: the work directory configured on the ocfl-java client should be located on the same mount as the OCFL
        //storage root.
        try {
            if (mFile.isEmpty()) {
                throw new FileSystemException("File Service: File is empty or missing.");
            }
            String origFileNameExt = mFile.getOriginalFilename();
            String origFileName = FilenameUtils.removeExtension(origFileNameExt);
            String fileExt = FilenameUtils.getExtension(origFileNameExt);
            String fileId = StorageServiceUtils.generateId(idCharSet, idLength);
            String mimeType = URLConnection.guessContentTypeFromName(origFileNameExt);

            String tmpDir = System.getProperty("java.io.tmpdir");
            if (!Files.exists(Paths.get(tmpDir, tempLoc.toString()))) {
                Files.createDirectory(Paths.get(tmpDir, tempLoc.toString()));
            }
            Path tempPathAndFileName = Paths.get(tmpDir, tempLoc.toString(),(origFileName + fileId + "." + fileExt));
            mFile.transferTo(tempPathAndFileName);

            ocflRepository.putObject(ObjectVersionId.head(fileId), tempPathAndFileName,
                    new VersionInfo().setMessage("Pass-Core File Service: Initial commit"));
            LOG.info("File Service: File with ID " + fileId + " was stored in the repo");

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
        ByteArrayResource loadedResource;
        String tmpDir = System.getProperty("java.io.tmpdir");
        Path tempLoadDir = Paths.get(tmpDir, this.tempLoc.toString(), fileId,
                Instant.now().toString().replace(":","-").replace(".","-"));
        Path tempLoadParentDir = Paths.get(tmpDir, this.tempLoc.toString(), fileId);
        try {
            String filePath = getResourceFileRelativePath(fileId);
            Path filePathAbs = Paths.get(this.rootLoc.toString(), filePath);
            if (!Files.exists(filePathAbs)) {
                throw new IOException("File Service: FileID does not exist: " + fileId);
            }
            //need the parent directory for the OCFL getObject to work
            if (!Files.exists(tempLoadParentDir)) {
                Files.createDirectories(tempLoadParentDir);
            }
            // the output path for getObject must not exist, hence temp dir is created on the fly
            if (storageType.equals(StorageServiceUtils.StorageServiceType.FILE_SYSTEM.label)) {
                ocflRepository.getObject(ObjectVersionId.head(fileId), tempLoadDir);
            } else if (storageType.equals(StorageServiceUtils.StorageServiceType.S3.label)) {
                //TODO implement
            }

            Path fileNamePath = StorageServiceUtils.getAbsoluteFileNamePath(tempLoadDir);
            loadedResource = new ByteArrayResource(Files.readAllBytes(fileNamePath));

        } catch (Exception e) {
            throw new IOException("File Service: The file could not be loaded, file ID: " + fileId, e);
        }

        if (loadedResource.exists() && loadedResource.isReadable()) {
            //clean up temp directory
            if (!FileSystemUtils.deleteRecursively(tempLoadParentDir)) {
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
     * file path.
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

