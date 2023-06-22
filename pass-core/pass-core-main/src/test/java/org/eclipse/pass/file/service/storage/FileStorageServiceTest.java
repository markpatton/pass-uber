package org.eclipse.pass.file.service.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import edu.wisc.library.ocfl.api.exception.NotFoundException;
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
    private final String rootDir = System.getProperty("java.io.tmpdir") + "/pass-file-system-test";
    private final static String USER_NAME = "USER1";
    private final static String USER_NAME2 = "USER2";

    /**
     * Set up the FileStorageService for testing. Uses the system temp directory for the root directory.
     */
    @BeforeEach
    void setUp() throws IOException {
        properties.setStorageType(StorageServiceType.FILE_SYSTEM);
        properties.setRootDir(rootDir);
        storageConfiguration =  new StorageConfiguration(properties);
        fileStorageService = new FileStorageService(storageConfiguration, null);
    }

    /**
     * Cleanup the FileStorageService after testing. Deletes the root directory.
     */
    @AfterEach
    void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(Paths.get(rootDir));
    }

    /**
     * Test that the file is stored and the relative path is returned. If the file didn't exist then
     * its relative path would not be found.
     */
    @Test
    public void storeFileThatExists() throws IOException {
        StorageFile storageFile = fileStorageService.storeFile(new MockMultipartFile("test", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()), USER_NAME);
        assertFalse(fileStorageService.getResourceFileRelativePath(storageFile.getId()).isEmpty());

        //check that the owner is the same
        assertEquals(fileStorageService.getFileOwner(storageFile.getId()), USER_NAME);
    }

    /**
     * File is stored and then retrieved.
     */
    @Test
    void getFileShouldReturnFile() throws IOException {
        StorageFile storageFile = fileStorageService.storeFile(new MockMultipartFile("test", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()), USER_NAME);
        ByteArrayResource file = fileStorageService.getFile(storageFile.getId());
        assertTrue(file.contentLength() > 0);
    }

    /**
     * Should throw exception because file ID does not exist
     */
    @Test
    void getFileShouldThrowException() {
        Exception exception = assertThrows(IOException.class,
                () -> {
                    fileStorageService.getFile("12345");
                }
        );
        String expectedExceptionText = "File Service: The file could not be loaded";
        String actualExceptionText = exception.getMessage();
        assertTrue(actualExceptionText.contains(expectedExceptionText));
    }

    @Test
    void storeFileWithDifferentLangFilesNames() {
        String engFileName = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()_+{}|:\"<>?`~[]\\;',./.txt";
        String frFileName = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖÙÚÛÜÝßàáâãäå" +
                "çèéêëìíîïñòóôõöùúûüýÿœŒæÆ.txt";
        String spFileName = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZÁÉÍÑÓÚÜáéíñóúü¡¿.txt";
        String arFileName = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZءآأؤإئابةتثجحخدذرزسشصضطظعغ" +
                "ـفقكلمنهوي.txt";
        String chFileName = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ我 爱 你 我爱你 家庭 家人 我想你 我想你 " +
                "我喜欢你 的 shì yí de 一个人 - yí gè rén 是 shì wǒ 我 .txt";
        String ruFileName = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZАа Бб Вв Гг Дд Ее Ёё Жж Зз Ии Йй Кк " +
                "Лл Мм Нн Оо Пп Рр Сс Тт Уу Фф Хх Цц Чч Шш Щщ Ъъ Ыы Ьь Ээ Юю Яя .txt";

        Map<String, String> allCharSets = new HashMap<>();
        allCharSets.put("eng", engFileName);
        allCharSets.put("fr", frFileName);
        allCharSets.put("sp", spFileName);
        allCharSets.put("ar", arFileName);
        allCharSets.put("ch", chFileName);
        allCharSets.put("ru", ruFileName);

        allCharSets.forEach((k,v) -> {
            try {
                StorageFile storageFile = fileStorageService.storeFile(new MockMultipartFile("test", v,
                        MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()), USER_NAME);
                assertFalse(fileStorageService.getResourceFileRelativePath(storageFile.getId()).isEmpty());
            } catch (IOException e) {
                assertEquals("An exception was thrown in storeFileWithDifferentLangFilesNames. On charset=" + k,
                        e.getMessage());
            }
        });
    }

    /**
     * Store file, then delete it. Should throw exception because the file was deleted.
     */
    @Test
    void deleteShouldThrowExceptionFileNotExist() throws IOException {
        StorageFile storageFile = fileStorageService.storeFile(new MockMultipartFile("test", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()), USER_NAME);
        fileStorageService.deleteFile(storageFile.getId());
        Exception exception = assertThrows(NotFoundException.class,
                () -> {
                    fileStorageService.getResourceFileRelativePath(storageFile.getId());
                });
        String exceptionText = exception.getMessage();
        assertTrue(exceptionText.matches("(.)+(was not found){1}(.)+"));
    }

    //TODO: will be refactored in the next ticket #478
    @Test
    void userHasPermissionToDeleteFile() throws IOException {
        Boolean hasPermission = false;
        StorageFile storageFile = fileStorageService.storeFile(new MockMultipartFile("test", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()), USER_NAME);
        hasPermission = fileStorageService.checkUserDeletePermissions(storageFile.getId(), USER_NAME);
        assertTrue(hasPermission);
    }

    //TODO: will be refactored in the next ticket #478
    @Test
    void userNoPermissionToDeleteFile() throws IOException {
        Boolean hasPermission = false;
        StorageFile storageFile = fileStorageService.storeFile(new MockMultipartFile("test", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()), USER_NAME);
        hasPermission = fileStorageService.checkUserDeletePermissions(storageFile.getId(), USER_NAME2);
        assertFalse(hasPermission);
    }
}
