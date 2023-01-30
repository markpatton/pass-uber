package org.eclipse.pass.file.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;

import edu.wisc.library.ocfl.api.exception.NotFoundException;
import org.eclipse.pass.file.service.storage.FileStorageService;
import org.eclipse.pass.file.service.storage.StorageConfiguration;
import org.eclipse.pass.file.service.storage.StorageFile;
import org.eclipse.pass.file.service.storage.StorageProperties;
import org.eclipse.pass.file.service.storage.StorageServiceUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

public class FileStorageServiceTest {
    StorageConfiguration storageConfiguration;
    private FileStorageService fileStorageService;
    private final StorageProperties properties = new StorageProperties();
    private final String fileSystemType = "FILE_SYSTEM";
    private final String rootDir = System.getProperty("java.io.tmpdir") + "/pass-file-system-test";
    private final int idLength = 25;
    private final String idCharSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Setup the FileStorageService for testing. Uses the system temp directory for the root directory.
     */
    @BeforeEach
    void setUp() {
        properties.setStorageType(fileSystemType);
        properties.setRootDir(rootDir);
        storageConfiguration =  new StorageConfiguration(properties);
        try {
            fileStorageService = new FileStorageService(storageConfiguration);
        } catch (IOException e) {
            assertEquals("Exception during setup", e.getMessage());
        }
    }

    /**
     * Cleanup the FileStorageService after testing. Deletes the root directory.
     */
    @AfterEach
    void tearDown() {
        try {
            FileSystemUtils.deleteRecursively(Paths.get(rootDir));
        } catch (IOException e) {
            assertEquals("An exception was thrown during cleanup.", e.getMessage());
        }
    }

    /**
     * Test that the file is stored and the relative path is returned. If the file didn't exist then
     * its relative path would not be found.
     */
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

    /**
     * File doesn't exist and should throw an exception.
     */
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

    /**
     * File is stored and then retrieved.
     */
    @Test
    void getFileShouldReturnFile() {
        try {
            StorageFile storageFile = fileStorageService.storeFile(new MockMultipartFile("test", "test.txt",
                    MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()));
            ByteArrayResource file = fileStorageService.getFile(storageFile.getId());
            assertTrue(file.contentLength() > 0);
        } catch (IOException e) {
            assertEquals("Exception during getFileShouldReturnFile", e.getMessage());
        }
    }

    /**
     * Should throw exception because file ID does not exist
     */
    @Test
    void getFileShouldThrowException() {
        Exception exception = assertThrows(IOException.class,
                () -> {
                    ByteArrayResource file = fileStorageService.getFile("12345");
                }
        );
        String expectedExceptionText = "File Service: The file could not be loaded";
        String actualExceptionText = exception.getMessage();
        assertTrue(actualExceptionText.contains(expectedExceptionText));
    }

    /**
     * Store file, then delete it. Should throw exception because the file was deleted.
     */
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

    /**
     * Generate an ID based on the charset and length and verify that it is a valid ID.
     */
    @Test
    void generateIdShouldBeValidId() {
        String id = StorageServiceUtils.generateId(idCharSet, idLength);
        assertTrue(id.matches("(\\w)+"));
        assertEquals(id.length(), 25);
    }

}
