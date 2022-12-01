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

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class StorageFile {
    @SerializedName("id")
    private String id = null;

    @SerializedName("fileName")
    private String fileName = null;

    @SerializedName("mimeType")
    private String mimeType = null;

    @SerializedName("storageType")
    private String storageType = null;

    @SerializedName("size")
    private Long size = null;

    @SerializedName("extension")
    private String extension = null;

    public StorageFile() {
        this.id = "999";
        this.id = "DefaultFileName";
        this.mimeType = "DefaultMineType";
        this.storageType = "DefaultStorageType";
        this.size = 0L;
        this.extension = "DefaultExtension";
    }

    public StorageFile(String id, String fileName, String mimeType, String storageType, Long size, String extension) {
        this.id = id;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.storageType = storageType;
        this.size = size;
        this.extension = extension;
    }

    /**
     * Get id
     * @return id
     **/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get fileName
     * @return fileName
     **/
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Get mimeType
     * @return mimeType
     **/
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Get storageType
     * @return storageType
     **/
    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    /**
     * Get size
     * @return size
     **/
    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * Get extension
     * @return extension
     **/
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StorageFile storageFile = (StorageFile) o;
        return Objects.equals(this.id, storageFile.id) &&
                Objects.equals(this.fileName, storageFile.fileName) &&
                Objects.equals(this.mimeType, storageFile.mimeType) &&
                Objects.equals(this.storageType, storageFile.storageType) &&
                Objects.equals(this.size, storageFile.size) &&
                Objects.equals(this.extension, storageFile.extension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fileName, mimeType, storageType, size, extension);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class File {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    fileName: ").append(toIndentedString(fileName)).append("\n");
        sb.append("    mimeType: ").append(toIndentedString(mimeType)).append("\n");
        sb.append("    storageType: ").append(toIndentedString(storageType)).append("\n");
        sb.append("    size: ").append(toIndentedString(size)).append("\n");
        sb.append("    extension: ").append(toIndentedString(extension)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
