package org.eclipse.pass.file.service.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Paths;

import edu.wisc.library.ocfl.api.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

public class FileStorageServiceTest {
    StorageConfiguration storageConfiguration;
    private FileStorageService fileStorageService;
    private final StorageProperties properties = new StorageProperties();
    private final String fileSystemType = "FILE_SYSTEM";
    private final String rootDir = Paths.get(System.getProperty("java.io.tmpdir"),"/pass-core-ocfl-root").toString();
    private final String workDir = Paths.get(System.getProperty("java.io.tmpdir"),"/pass-core-ocfl-work").toString();
    private String tempDir = "/pass-core-temp"; //File Service uses the system tmpdir, no need to modify this path

    private final int idLength = 25;
    private final String idCharSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @BeforeEach
    void setUp() {
        properties.setStorageType(fileSystemType);
        properties.setOcflRootDir(rootDir);
        properties.setOcflWorkDir(workDir);
        properties.setTempDir(tempDir);
        storageConfiguration =  new StorageConfiguration(properties);
        try {
            fileStorageService = new FileStorageService(storageConfiguration);
        } catch (FileSystemException e) {
            assertEquals("Exception during setup", e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        try {
            assertTrue(FileSystemUtils.deleteRecursively(Paths.get(properties.getStorageRootDir())));
        } catch (IOException e) {
            assertEquals("An exception was thrown during cleanup.", e.getMessage());
        }
    }

    @Test
    public void storeFileThatExists() {
        try {
            StorageFile storageFile = fileStorageService.storeFile(new MockMultipartFile("test", "test.txt",
                    MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()));
            assertFalse(fileStorageService.getResourceFileRelativePath(storageFile.getId()).isEmpty());
        } catch (Exception e) {
            assertEquals("An exception was thrown in storeFileThatExists.", e.getMessage());
        }
    }

    @Test
    void storeFileNotExistsShouldThrowException() {
        Exception exception = assertThrows(IOException.class,
                () -> {
                    fileStorageService.storeFile(new MockMultipartFile("test", "test.txt",
                            MediaType.TEXT_PLAIN_VALUE, "".getBytes()));
                }
        );
        String expectedExceptionText = "File Service: The file system was unable to store the uploaded file";
        String actualExceptionText = exception.getMessage();
        assertTrue(actualExceptionText.contains(expectedExceptionText));
    }

    @Test
    void deleteShouldThrowExceptionFileNotExist() {
        try {
            StorageFile storageFile = fileStorageService.storeFile(new MockMultipartFile("test", "test.txt",
                    MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()));
            fileStorageService.deleteFile(storageFile.getId());
            Exception exception = assertThrows(NotFoundException.class,
                    () -> {
                        fileStorageService.getResourceFileRelativePath(storageFile.getId());
                    });
            String exceptionText = exception.getMessage();
            assertTrue(exceptionText.matches("(.)+(was not found){1}(.)+"));
        } catch (IOException e) {
            assertEquals("Exception during deleteShouldThrowExceptionFileNotExist", e.getMessage());
        }
    }

    @Test
    void generateIdShouldBeValidId() {
        String id = StorageServiceUtils.generateId(idCharSet, idLength);
        assertTrue(id.matches("(\\w)+"));
        assertEquals(id.length(), 25);
    }

}
