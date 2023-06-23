/*
 * Copyright 2023 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

public abstract class FileStorageServiceTest {
    protected StorageConfiguration storageConfiguration;
    protected FileStorageService storageService;
    protected final StorageProperties properties = new StorageProperties();
    protected final static String ROOT_DIR = System.getProperty("java.io.tmpdir") + "/pass-file-service-test";
    protected final static String USER_NAME = "USER1";
    protected final static String USER_NAME2 = "USER2";

    @BeforeEach
    protected abstract void setUp() throws IOException;

    /**
     * Cleanup the FileStorageService after testing. Deletes the root directory.
     */
    @AfterEach
    protected void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(Paths.get(ROOT_DIR));
    }

    /**
     * Test that the file is stored and the relative path is returned. If the file didn't exist then
     * its relative path would not be found.
     */
    @Test
    public void storeFileThatExists() throws IOException {
        StorageFile storageFile = storageService.storeFile(new MockMultipartFile("test", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()), USER_NAME);
        assertFalse(storageService.getResourceFileRelativePath(storageFile.getId()).isEmpty());

        //check that the owner is the same
        assertEquals(storageService.getFileOwner(storageFile.getId()), USER_NAME);
    }

    /**
     * File is stored and then retrieved.
     */
    @Test
    void getFileShouldReturnFile() throws IOException {
        StorageFile storageFile = storageService.storeFile(new MockMultipartFile("test", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()), USER_NAME);
        ByteArrayResource file = storageService.getFile(storageFile.getId());
        assertTrue(file.contentLength() > 0);
    }

    /**
     * Should throw exception because file ID does not exist
     */
    @Test
    void getFileShouldThrowException() {
        Exception exception = assertThrows(IOException.class,
                () -> {
                    storageService.getFile("12345");
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
                StorageFile storageFile = storageService.storeFile(new MockMultipartFile("test", v,
                        MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()), USER_NAME);
                assertFalse(storageService.getResourceFileRelativePath(storageFile.getId()).isEmpty());
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
        StorageFile storageFile = storageService.storeFile(new MockMultipartFile("test", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()), USER_NAME);
        storageService.deleteFile(storageFile.getId());
        Exception exception = assertThrows(NotFoundException.class,
                () -> {
                    storageService.getResourceFileRelativePath(storageFile.getId());
                });
        String exceptionText = exception.getMessage();
        assertTrue(exceptionText.matches("(.)+(was not found){1}(.)+"));
    }

    @Test
    void userHasPermissionToDeleteFile() throws IOException {
        Boolean hasPermission = false;
        StorageFile storageFile = storageService.storeFile(new MockMultipartFile("test", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()), USER_NAME);
        hasPermission = storageService.checkUserDeletePermissions(storageFile.getId(), USER_NAME);
        assertTrue(hasPermission);
    }

    @Test
    void userNoPermissionToDeleteFile() throws IOException {
        Boolean hasPermission = false;
        StorageFile storageFile = storageService.storeFile(new MockMultipartFile("test", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()), USER_NAME);
        hasPermission = storageService.checkUserDeletePermissions(storageFile.getId(), USER_NAME2);
        assertFalse(hasPermission);
    }
}
