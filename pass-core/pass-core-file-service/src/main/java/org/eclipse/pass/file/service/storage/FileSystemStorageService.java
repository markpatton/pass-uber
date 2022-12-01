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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

import edu.wisc.library.ocfl.api.OcflRepository;
import edu.wisc.library.ocfl.api.model.FileDetails;
import edu.wisc.library.ocfl.api.model.ObjectVersionId;
import edu.wisc.library.ocfl.api.model.VersionDetails;
import edu.wisc.library.ocfl.api.model.VersionInfo;
import edu.wisc.library.ocfl.core.OcflRepositoryBuilder;
import edu.wisc.library.ocfl.core.extension.storage.layout.config.HashedNTupleLayoutConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemStorageService.class);
    private final Path rootLoc;
    private final Path workLoc;
    private final Path tempLoc;
    private final int idLength = 25;
    private final String idCharSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final String storageType = "FILE_SYSTEM";
    private OcflRepository ocflRepository;

    public FileSystemStorageService(StorageProperties storageProperties) {
        this.rootLoc = Paths.get(storageProperties.getStorageRootDir());
        this.workLoc = Paths.get(storageProperties.getStorageWorkDir());
        this.tempLoc = Paths.get(storageProperties.getStorageTempDir());
    }

    @Override
    public void init() {
        try {

            //init directories for the root, work, and temp
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
            if (!Files.exists(tempLoc)) {
                try {
                    Files.createDirectory(tempLoc);
                } catch (IOException e) {
                    throw new FileSystemException("File Service: Temp directory could not be created");
                }
            }
            //test to ensure root, work, temp have permissions read/write
            if (!Files.isReadable(rootLoc) || !Files.isWritable(rootLoc)) {
                throw new FileSystemException("File Service: No permission to read/write root directory.");
            }
            if (!Files.isReadable(workLoc) || !Files.isWritable(workLoc)) {
                throw new FileSystemException("File Service: No permission to read/write work directory.");
            }
            if (!Files.isReadable(tempLoc) || !Files.isWritable(tempLoc)) {
                throw new FileSystemException("File Service: No permission to read/write temp directory.");
            }

            ocflRepository = new OcflRepositoryBuilder()
                    .defaultLayoutConfig(new HashedNTupleLayoutConfig())
                    .storage(storage -> storage.fileSystem(this.rootLoc))
                    .workDir(this.workLoc)
                    .build();

        } catch (FileSystemException e) {
            LOG.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public StorageFile store(MultipartFile mFile) {
        StorageFile storageFile = null;
        //NOTE: the work directory configured on the ocfl-java client should be located on the same mount as the OCFL
        //storage root.

        try {
            if (mFile.isEmpty() || mFile == null) {
                throw new FileSystemException("File Service: Files is empty or missing.");
            }
            String origFileName = mFile.getOriginalFilename();
            String fileId = generateId();
            String mimeType = URLConnection.guessContentTypeFromName(origFileName);

            Path tempFileDir = this.tempLoc.resolve(Paths.get(origFileName))
                    .normalize()
                    .toAbsolutePath();

            //copy uploaded file to temp dir before ocfl persistence
            try (InputStream inputstream = mFile.getInputStream()) {
                Files.copy(inputstream, tempFileDir, StandardCopyOption.REPLACE_EXISTING);
            }

            //persist file using ocfl
            ocflRepository.putObject(ObjectVersionId.head(fileId), tempFileDir,
                    new VersionInfo().setMessage("Pass-Core File Service: Initial commit"));
            LOG.info("File Service: File with ID " + fileId + " was stored in the repo");
            //create pass-core File object to return
            storageFile = new StorageFile(
                    fileId,
                    origFileName,
                    mimeType,
                    storageType,
                    mFile.getSize(),
                    getExtensionFromFile(origFileName).get()
            );

            //clean up temp directory
            if (!FileSystemUtils.deleteRecursively(tempFileDir)) {
                LOG.info("File Service: No files to cleanup on file upload");
            }

        } catch (IOException e) {
            LOG.error(e.toString());
            throw new RuntimeException("File Service: The file system was unable to store the uploaded file", e);
        }

        if (storageFile == null) {
            LOG.error("File Service: The storage file was never initialized.");
            throw new RuntimeException("File Service: The File uploaded could not be processed.");
        } else {
            return storageFile;
        }

    }

    @Override
    public ByteArrayResource loadAsResource(String fileId) {
        ByteArrayResource loadedResource;
        Path tempLoadDir = Paths.get(this.tempLoc.toString(), fileId,
                Instant.now().toString().replace(":","-").replace(".","-"));
        Path tempLoadParentDir = Paths.get(this.tempLoc.toString(), fileId);
        try {
            if (!resourceExists(fileId, this.rootLoc.toString())) {
                throw new Exception("File Service: FileID does not exist: " + fileId);
            }
            //need the parent directory for the OCFL getObject to work
            if (!Files.exists(tempLoadParentDir)) {
                Files.createDirectories(tempLoadParentDir);
            }
            // the output path for getObject must not exist, hence temp dir is created on the fly
            ocflRepository.getObject(ObjectVersionId.head(fileId), tempLoadDir);
            Path fileNamePath = getAbsoluteFileNamePath(tempLoadDir);
            loadedResource = new ByteArrayResource(Files.readAllBytes(fileNamePath));
            if (loadedResource.exists() && loadedResource.isReadable()) {
                //clean up temp directory
                if (!FileSystemUtils.deleteRecursively(tempLoadParentDir)) {
                    LOG.info("File Service: No files to cleanup on file upload");
                }
                return loadedResource;
            }
        } catch (Exception e) {
            throw new RuntimeException("File Service: The file could not be loaded, file ID: " + fileId, e);
        }
        return loadedResource;
    }

    @Override
    public void delete(String fileId) {
        ocflRepository.purgeObject(fileId);
    }

    @Override
    public String generateId() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder sb = new StringBuilder(idLength);
        for (int i = 0; i < idLength; i++) {
            sb.append(idCharSet.charAt(secureRandom.nextInt(idCharSet.length())));
        }
        return sb.toString();
    }

    @Override
    public String getResourceFileName(String fileId) {
        String filePath = getResourceFileRelativePath(fileId);
        return Paths.get(filePath).getFileName().toString();
    }

    @Override
    public Boolean resourceExists(String fileId, String rootDir) {
        String filePath = getResourceFileRelativePath(fileId);
        if (Files.exists(Paths.get(rootDir,filePath))) {
            return true;
        }
        return false;
    }

    private String getResourceFileRelativePath(String fileId) {
        VersionDetails versionDetails = ocflRepository.describeVersion(ObjectVersionId.head(fileId));
        Collection<FileDetails> allVersionFiles = versionDetails.getFiles();
        Optional<FileDetails> fileDetails = allVersionFiles.stream().findFirst();
        return fileDetails.get().getStorageRelativePath();
    }

    private Optional<String> getExtensionFromFile(String fn) {
        return Optional.ofNullable(fn)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(fn.lastIndexOf(".") + 1));
    }

    private Path getAbsoluteFileNamePath(Path path) {
        File[] listOfFiles = path.toFile().listFiles();
        return listOfFiles[0].toPath();
    }
}
