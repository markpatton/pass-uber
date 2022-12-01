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
package org.eclipse.pass.file.service;

import java.io.IOException;
import java.net.URI;

import org.eclipse.pass.file.service.storage.StorageConfiguration;
import org.eclipse.pass.file.service.storage.StorageFile;
import org.eclipse.pass.file.service.storage.StorageService;
import org.eclipse.pass.file.service.storage.StorageServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * PassFileServiceController is the controller class responsible for the File Service endpoints, which allows pass-core
 * internal and external services to upload, retrieve and delete files.
 *
 * Configuration of the File Service is done through .env environment variable file.
 */
@RestController
public class PassFileServiceController {
    private static final Logger LOG = LoggerFactory.getLogger(PassFileServiceController.class);
    private StorageService storageService;
    @Autowired
    StorageConfiguration storageConfiguration;
    @Autowired
    StorageServiceFactory storageFactory;

    /**
     *   Class constructor.
     */
    public void PassFileServiceController() {
    }

    /**
     * Handles a file upload and will call the StorageService to determine the repository where the file is to be
     * deposited.
     *
     * @param file
     * @return return a File object that has been uploaded.
     * @throws FileServiceException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @PostMapping("/file")
    public ResponseEntity<?> fileUpload(@RequestParam("file") MultipartFile file)
            throws FileServiceException {
        LOG.info("Pass File Service: Uploading New File");
        try {
            if (file.getBytes().length == 0) {
                return ResponseEntity.badRequest().build();
            }
        } catch (IOException e) {
            LOG.error("File Service: Error processing file upload: " + e);
            return ResponseEntity.notFound().build();
        }
        storageService = storageFactory.createStorage(storageConfiguration.getStorageProperties());
        StorageFile returnStorageFile = storageService.store(file);
        return ResponseEntity.created(URI.create(returnStorageFile.getId())).body(returnStorageFile);
    }

    /**
     * Gets a file by the fileId and returns a single file. Implicitly supports HTTP HEAD.
     *
     * @param fileId ID of the file to return (required)
     * @return Bitstream
     * @throws FileServiceException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @GetMapping("/file/{fileId:.+}")
    @ResponseBody
    public ResponseEntity<?> getFileById(@PathVariable String fileId) throws FileServiceException {
        LOG.info("Get file by ID. File ID: " + fileId);
        ByteArrayResource fileResource;
        if (fileId == null) {
            LOG.error("File ID not provided to get a file.");
            return ResponseEntity.badRequest().body("File ID not provided to get a file.");
        }
        storageService = storageFactory.createStorage(storageConfiguration.getStorageProperties());
        try {
            fileResource = storageService.loadAsResource(fileId);
        } catch (Exception e) {
            LOG.error("File Service: File not found: " + e);
            return ResponseEntity.notFound().build();
        }
        LOG.info("File Service: Filename= " + storageService.getResourceFileName(fileId));
        String headerAttachment = "attachment; filename=\"" + storageService.getResourceFileName(fileId) + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, headerAttachment)
                .contentLength(fileResource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileResource);
    }

    /**
     * Deletes a file by the provided file ID
     *
     * @param fileId ID of the file to delete (required)
     * @return File
     * @throws FileServiceException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @DeleteMapping("/file/{fileId}")
    public ResponseEntity<?> deleteFileById(@PathVariable String fileId) throws FileServiceException {
        LOG.info("Delete file by ID.");
        if (fileId == null) {
            LOG.error("File ID not provided to delete file.");
            throw new FileServiceException(HttpStatus.NOT_FOUND, "Missing File ID");
        }
        storageService = storageFactory.createStorage(storageConfiguration.getStorageProperties());
        storageService.delete(fileId);
        return ResponseEntity.ok().body("Deleted");
    }
}
