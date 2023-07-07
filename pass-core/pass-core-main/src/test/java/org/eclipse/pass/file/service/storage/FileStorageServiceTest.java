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

import static java.io.File.createTempFile;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import edu.wisc.library.ocfl.api.exception.NotFoundException;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.eclipse.pass.main.IntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

public abstract class FileStorageServiceTest extends IntegrationTest {
    protected final String USER_NAME = "USER1";
    protected final String USER_NAME2 = "USER2";
    private final String credentialsBackend = Credentials.basic(BACKEND_USER, BACKEND_PASSWORD);
    private final OkHttpClient httpClient = new OkHttpClient();
    public static final MediaType MEDIA_TYPE_TEXT
            = MediaType.parse("text/plain");
    public static final MediaType MEDIA_TYPE_APPLICATION
            = MediaType.parse("application/octet-stream");
    @Autowired
    protected FileStorageService storageService;
    @Autowired
    protected StorageConfiguration storageConfiguration;

    /**
     * Cleanup the FileStorageService after testing. Deletes the root directory.
     */
    @AfterAll
    protected void tearDown() throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        String rootDirName = storageConfiguration.getStorageProperties().getStorageRootDir();
        File tempRootDir = tempDir.resolve(rootDirName).toFile();
        deleteDirectory(tempRootDir);
    }

    /**
     * Test that the file is stored and the relative path is returned. If the file didn't exist then
     * its relative path would not be found.
     */
    @Test
    public void storeFileThatExists() throws IOException {
        StorageFile storageFile = storageService.storeFile(new MockMultipartFile("test", "test.txt",
                MEDIA_TYPE_TEXT.toString(), "Test Pass-core".getBytes()), USER_NAME);
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
                MEDIA_TYPE_TEXT.toString(), "Test Pass-core".getBytes()), USER_NAME);
        ByteArrayResource file = storageService.getFile(storageFile.getId());
        assertTrue(file.contentLength() > 0);
    }

    /**
     * Test file content type is returned.
     */
    @Test
    void getFileContentTypeShouldReturnContentType() throws IOException {
        StorageFile storageFile = storageService.storeFile(new MockMultipartFile("test", "test.txt",
                MEDIA_TYPE_TEXT.toString(), "Test Pass-core".getBytes()), USER_NAME);
        String contentType = storageService.getFileContentType(storageFile.getId());
        assertEquals(MEDIA_TYPE_TEXT.toString(), contentType);
    }

    /**
     * Should throw exception because file ID does not exist
     */
    @Test
    void getFileShouldThrowException() {
        Exception exception = assertThrows(IOException.class,
                () -> storageService.getFile("12345")
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
                        MEDIA_TYPE_TEXT.toString(), "Test Pass-core".getBytes()), USER_NAME);
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
                MEDIA_TYPE_TEXT.toString(), "Test Pass-core".getBytes()), USER_NAME);
        storageService.deleteFile(storageFile.getId());
        Exception exception = assertThrows(NotFoundException.class,
                () -> storageService.getResourceFileRelativePath(storageFile.getId()));
        String exceptionText = exception.getMessage();
        assertTrue(exceptionText.matches("(.)+(was not found){1}(.)+"));
    }

    /**
     * Store file, and then check user permissions on that file. User has permissions to delete file.
     *
     * @throws IOException if there is an error
     */
    @Test
    void userHasPermissionToDeleteFile() throws IOException {
        boolean hasPermission;
        StorageFile storageFile = storageService.storeFile(new MockMultipartFile("test", "test.txt",
                MEDIA_TYPE_TEXT.toString(), "Test Pass-core".getBytes()), USER_NAME);
        hasPermission = storageService.checkUserDeletePermissions(storageFile.getId(), USER_NAME);
        assertTrue(hasPermission);
    }

    /**
     * Store file, and then check user permissions on that file. User does not have permissions to delete file.
     *
     * @throws IOException if there is an error
     */
    @Test
    void userNoPermissionToDeleteFile() throws IOException {
        boolean hasPermission = false;
        StorageFile storageFile = storageService.storeFile(new MockMultipartFile("test", "test.txt",
                MEDIA_TYPE_TEXT.toString(), "Test Pass-core".getBytes()), USER_NAME);
        hasPermission = storageService.checkUserDeletePermissions(storageFile.getId(), USER_NAME2);
        assertFalse(hasPermission);
    }

    /**
     * Get file by ID using the PassFileServiceController.
     *
     * @throws IOException if there is an error
     */
    @Test
    void getFileByIdUsingController() throws IOException {
        StorageFile storageFile = storageService.storeFile(new MockMultipartFile("test", "test.txt",
                MEDIA_TYPE_TEXT.toString(), "Test Pass-core".getBytes()), USER_NAME);

        ByteArrayResource file = storageService.getFile(storageFile.getId());
        //ensure that the file has been stored by the service
        assertTrue(file.contentLength() > 0);

        String url = getBaseUrl() + "file/" + storageFile.getId();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", credentialsBackend)
                .get()
                .build();
        Response response = client.newCall(request).execute();

        assertEquals(HttpStatus.OK.value(), response.code());
        assertNotNull(response.body());
    }

    /**
     * Upload file using the PassFileServiceController.
     *
     * @throws IOException if there is an error
     */
    @Test
    void uploadFile() throws IOException {
        String url = getBaseUrl() + "file";
        File file = createTestFile();

        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file",file.getName(),
                        RequestBody.create(file, MEDIA_TYPE_APPLICATION))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", credentialsBackend)
                .build();

        Response response = httpClient.newCall(request).execute();
        assertEquals(HttpStatus.CREATED.value(), response.code());
        assertNotNull(response.body());
        file.delete();
    }

    /**
     * Attempt to upload a file, that has file missing in the body. Should return 400 Bad Request.
     * @throws IOException if there is an error
     */
    @Test
    void uploadFileMissingFile() throws IOException {
        String url = getBaseUrl() + "file";

        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file","test.txt",
                        RequestBody.create("", MEDIA_TYPE_APPLICATION))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", credentialsBackend)
                .build();

        Response response = httpClient.newCall(request).execute();
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.code());
    }

    /**
     * Attempt to upload a file, that has file name missing in the body. Should return 400 Bad Request.
     * @throws IOException if there is an error
     */
    @Test
    void uploadFileMissingFileName() throws IOException {
        String url = getBaseUrl() + "file";
        File file = createTestFile();

        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file",null,
                        RequestBody.create(file, MEDIA_TYPE_APPLICATION))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", credentialsBackend)
                .build();

        Response response = httpClient.newCall(request).execute();
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.code());
        file.delete();
    }

    /**
     * Delete file using the controller. Should return 200 OK.
     * @throws IOException if there is an error
     */
    @Test
    void deleteFileUsingFileServiceController() throws IOException {
        StorageFile storageFile = storageService.storeFile(new MockMultipartFile("test", "test.txt",
                MEDIA_TYPE_TEXT.toString(), "Test Pass-core".getBytes()), USER_NAME);
        String url = getBaseUrl() + "file" + "/" + storageFile.getId();

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("Authorization", credentialsBackend)
                .build();

        Response response = httpClient.newCall(request).execute();
        assertEquals(HttpStatus.OK.value(), response.code());
    }

    private File createTestFile() throws IOException {
        File file = createTempFile("test", ".txt");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("Test Pass-core");
        fileWriter.close();
        return file;
    }
}
