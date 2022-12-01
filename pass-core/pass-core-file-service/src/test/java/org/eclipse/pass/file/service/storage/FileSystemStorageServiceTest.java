package org.eclipse.pass.file.service.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;

import edu.wisc.library.ocfl.api.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

class FileSystemStorageServiceTest {

    private String ocflRootDir = "C:/test/files/root";
    private String ocflWorkDir = "C:/test/files/work";
    private String ocflTempDir = "C:/test/files/temp";
    private StorageProperties properties = new StorageProperties();
    private FileSystemStorageService storageService;

    @BeforeEach
    void setUp() {
        properties.setStorageType("FILE_SYSTEM");
        properties.setOcflRootDir(ocflRootDir);
        properties.setOcflWorkDir(ocflWorkDir);
        properties.setTempDir(ocflTempDir);
        storageService = new FileSystemStorageService(properties);
        storageService.init();
    }

    @AfterEach
    void tearDown() {
        try {
            assertTrue(FileSystemUtils.deleteRecursively(Paths.get(ocflRootDir)));
        } catch (IOException e) {
            assertEquals("An exception was thrown during cleanup.", e.getMessage());
        }
    }

    @Test
    public void storeFileThatExists() {
        StorageFile storageFile = storageService.store(new MockMultipartFile("test", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()));
        assertTrue(!storageService.getResourceFileName(storageFile.getId()).isEmpty());

    }

    @Test
    void storeFileNotExistsShouldThrowException() {
        Exception exception = assertThrows(RuntimeException.class,
                () -> {
                    storageService.store(new MockMultipartFile("test", "test.txt",
                        MediaType.TEXT_PLAIN_VALUE, "".getBytes()));
                }
        );
        String expectedExceptionText = "File Service: The file system was unable to store the uploaded file";
        String actualExceptionText = exception.getMessage();
        assertTrue(actualExceptionText.contains(expectedExceptionText));
    }

    @Test
    void deleteShouldThrowExceptionFileNotExist() {
        StorageFile storageFile = storageService.store(new MockMultipartFile("test", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()));
        storageService.delete(storageFile.getId());
        Exception exception = assertThrows(NotFoundException.class,
                () -> {
                    storageService.getResourceFileName(storageFile.getId());
                }
        );
        String exceptionText = exception.getMessage();
        assertTrue(exceptionText.matches("(.)+(was not found){1}(.)+"));
    }

    @Test
    void generateIdShouldBeValidId() {
        String id = storageService.generateId();
        assertTrue(id.matches("(\\w)+"));
        assertEquals(id.length(), 25);
    }

}