package org.eclipse.pass.file.service.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.wisc.library.ocfl.api.OcflRepository;
import edu.wisc.library.ocfl.api.model.ObjectVersionId;
import edu.wisc.library.ocfl.api.model.VersionInfo;
import edu.wisc.library.ocfl.core.OcflRepositoryBuilder;
import edu.wisc.library.ocfl.core.extension.storage.layout.config.HashedNTupleLayoutConfig;
import liquibase.repackaged.org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemStorageService.class);
    private final Path rootLoc;
    private final Path workLoc;
    private final Path tempLoc;
    private final int idLength = 20;
    private final String idCharSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final String storageType = "FILE_SYSTEM";

    //TODO might delete, don't think it will be necessary for user to delete
    enum OsType {
        WINDOWS,
        LINUX,
        UNIX,
        UNKNOWN
    }

    public FileSystemStorageService() {
        this.rootLoc = Paths.get("C:\\root");
        this.workLoc = Paths.get("C:\\work");
        this.tempLoc = Paths.get("C:\\temp");
    }

    public FileSystemStorageService(StorageProperties storageProperties) {
        //TODO read in the properties from the application.yaml
        LOG.info("FileSystemStorageService Constructor:");
        LOG.info("getStorageRootDir = " + storageProperties.getStorageRootDir());
        this.rootLoc = Paths.get(storageProperties.getStorageRootDir());
        this.workLoc = Paths.get(storageProperties.getStorageWorkDir());
        this.tempLoc = Paths.get(storageProperties.getStorageTempDir());
        //this.rootLoc = Paths.get("C:\\pass-core-ocfl-root");
        //this.workLoc = Paths.get("C:\\pass-core-ocfl-work");
        //this.tempLoc = Paths.get("C:\\pass-core-temp");
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

        } catch (FileSystemException e) {
            LOG.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public StorageFile store(MultipartFile mFile, String fileName) {
        StorageFile storageFile = null;
        //NOTE: the work directory configured on the ocfl-java client should be located on the same mount as the OCFL
        //storage root.
        OcflRepository repo = new OcflRepositoryBuilder()
                .defaultLayoutConfig(new HashedNTupleLayoutConfig())
                .storage(storage -> storage.fileSystem(this.rootLoc))
                .workDir(this.workLoc)
                .build();
        try {

            if (mFile.isEmpty()) {
                throw new FileSystemException("File Service: Files is empty.");
            }
            Path tempFileDir = this.tempLoc.resolve(Paths.get(mFile.getOriginalFilename()))
                    .normalize()
                    .toAbsolutePath();

            //copy uploaded file to temp dir before ocfl persistence
            try (InputStream inputstream = mFile.getInputStream()) {
                Files.copy(inputstream, tempFileDir, StandardCopyOption.REPLACE_EXISTING);
            }

            //persist file using ocfl
            repo.putObject(ObjectVersionId.head("o1"), tempFileDir,
                    new VersionInfo().setMessage("Pass-Core File Service: Initial commit"));

            //create pass-core File object to return
            storageFile = new StorageFile(
                    "1",
                    "test",
                    "test",
                    storageType,
                    mFile.getSize(),
                    getExtensionFromFile(mFile.getOriginalFilename()).toString()
            );

        } catch (IOException e) {
            LOG.error(e.toString());
            throw new RuntimeException("The file system was unable to store the uploaded file", e);
        }

        if (storageFile == null) {
            LOG.error("The storage file was never initialized.");
            throw new RuntimeException("File Service: The File uploaded could not be processed.");
        } else {
            return storageFile;
        }

    }

    @Override
    public Path load(String fileId) {
        //TODO implement OCFL storage using UW Madison implementation
        return null;
    }

    @Override
    public Resource loadAsResource(String fileId) {
        //TODO implement OCFL storage using UW Madison implementation
        return null;
    }

    @Override
    public void delete(String fileId) {
        //TODO implement OCFL storage using UW Madison implementation
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

    public Optional<String> getExtensionFromFile(String fn) {
        return Optional.ofNullable(fn)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(fn.lastIndexOf(".") + 1));
    }

    //TODO might remove this - should the OS type be explicit set by the user? Could cause issues trying to infer the
    // OS type?
    private OsType getOsType() {
        Pattern winPattern = Pattern.compile("windows", Pattern.CASE_INSENSITIVE);
        Pattern linuxPattern = Pattern.compile("linux", Pattern.CASE_INSENSITIVE);
        Pattern unixPattern = Pattern.compile("unix", Pattern.CASE_INSENSITIVE);
        Matcher winMatch = winPattern.matcher(SystemUtils.OS_NAME);
        Matcher linuxMatch = linuxPattern.matcher(SystemUtils.OS_NAME);
        Matcher unixMatch = unixPattern.matcher(SystemUtils.OS_NAME);
        if (winMatch.find()) {
            return OsType.WINDOWS;
        }
        if (linuxMatch.find()) {
            return OsType.LINUX;
        }
        if (unixMatch.find()) {
            return OsType.UNIX;
        }
        return OsType.UNKNOWN;
    }

}
