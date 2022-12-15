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
package org.eclipse.pass.file.service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import org.eclipse.pass.file.service.storage.FileStorageService;
import org.eclipse.pass.file.service.storage.StorageFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
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
 * internal and external services to upload, retrieve and delete files. Configuration of the File Service is done
 * through .env file and is loaded into the StorageProperties.
 *
 * @author Tim Sanders
 */
@RestController
public class PassFileServiceController {
    private static final Logger LOG = LoggerFactory.getLogger(PassFileServiceController.class);

    @Lazy
    @Autowired
    private FileStorageService fileStorageService;

    /**
     *   Class constructor.
     */
    public PassFileServiceController() {
    }

    /**
     * Handles a file upload and will call the FileStorageService to determine the repository where the file is to be
     * deposited.
     *
     * @param file A multipart file that is uploaded from the client.
     * @return return a File object that has been uploaded.
     */
    @PostMapping("/file")
    public ResponseEntity<?> fileUpload(@RequestParam("file") MultipartFile file) {
        StorageFile returnStorageFile;

        try {
            if (file.getBytes().length == 0) {
                return ResponseEntity.badRequest().build();
            }
        } catch (IOException e) {
            LOG.error("File Service: Error processing file upload: " + e);
            return ResponseEntity.notFound().build();
        }

        try {
            returnStorageFile = fileStorageService.storeFile(file);
        } catch (IOException e) {
            LOG.error("File Service: Error storing file upload: " + e);
            return ResponseEntity.notFound().build();
        } catch (NullPointerException e) {
            LOG.error("File Service: Error storing file upload: " + e);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.created(URI.create(returnStorageFile.getId())).body(returnStorageFile);
    }

    /**
     * Gets a file by the fileId and returns a single file. Implicitly supports HTTP HEAD.
     *
     * @param fileId ID of the file to return (required)
     * @return Bitstream The file requested by the fileId
     */
    @GetMapping("/file/{fileId:.+}")
    @ResponseBody
    public ResponseEntity<?> getFileById(@PathVariable String fileId) {
        LOG.info("Get file by ID. File ID: " + fileId);
        if (fileId == null) {
            LOG.error("File ID not provided to get a file.");
            return ResponseEntity.badRequest().body("File ID not provided to get a file.");
        }
        ByteArrayResource fileResource;
        String fileName = "";
        try {
            fileName = Paths.get(fileStorageService.getResourceFileRelativePath(fileId)).getFileName().toString();
        } catch (Exception e) {
            LOG.info("Get file by ID. File ID not found: " + fileId);
            return ResponseEntity.notFound().build();
        }

        try {
            fileResource = fileStorageService.getFile(fileId);
        } catch (Exception e) {
            LOG.error("File Service: File not found: " + e);
            return ResponseEntity.notFound().build();
        }

        LOG.info("File Service: Filename= " + fileName);
        String headerAttachment = "attachment; filename=\"" + fileName + "\"";
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
     */
    @DeleteMapping("/file/{fileId}")
    public ResponseEntity<?> deleteFileById(@PathVariable String fileId) {
        LOG.info("Delete file by ID.");
        if (fileId == null) {
            LOG.error("File ID not provided to delete file.");
            return ResponseEntity.notFound().build();
        }
        fileStorageService.deleteFile(fileId);
        return ResponseEntity.ok().body("Deleted");
    }
}