/*
 * Copyright 2018 Johns Hopkins University
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
package org.eclipse.pass.object.model;

import java.net.URI;
import java.util.Objects;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;
import org.eclipse.pass.object.converter.FileRoleToStringConverter;

/**
 * Files are associated with a Submissions to be used to form Deposits into Repositories
 *
 * @author Karen Hanson
 */

@Include
@Entity
@Table(name = "pass_file")
public class File extends PassEntity {

    /**
     * Name of file, defaults to filesystem.name
     */
    private String name;

    /**
     * URI to the bytestream that Deposit services will use to retrieve the bytestream for Deposit
     */
    private URI uri;

    /**
     * Description of file provided by User
     */
    private String description;

    /**
     * Role of the file e.g. manuscript, supplemental
     */
    @Convert(converter = FileRoleToStringConverter.class)
    private FileRole fileRole;

    /**
     * Mime-type of file
     */
    private String mimeType;

    /**
     * The Submission the File is a part of
     */
    @ManyToOne
    private Submission submission;

    /**
     * File constructor
     */
    public File() {
    }

    /**
     * Copy constructor, this will copy the values of the object provided into the new object
     *
     * @param file the file to copy
     */
    public File(File file) {
        super(file);
        this.name = file.name;
        this.uri = file.uri;
        this.description = file.description;
        this.fileRole = file.fileRole;
        this.mimeType = file.mimeType;
        this.submission = file.submission;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the fileRole
     */
    public FileRole getFileRole() {
        return fileRole;
    }

    /**
     * @param fileRole the fileRole to set
     */
    public void setFileRole(FileRole fileRole) {
        this.fileRole = fileRole;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return the submission
     */
    public Submission getSubmission() {
        return submission;
    }

    /**
     * @param submission the submission to set
     */
    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        File other = (File) obj;
        return Objects.equals(description, other.description) && fileRole == other.fileRole
                && Objects.equals(mimeType, other.mimeType) && Objects.equals(name, other.name)
                && Objects.equals(submission, other.submission) && Objects.equals(uri, other.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), name);
    }

    @Override
    public String toString() {
        return "File [name=" + name + ", uri=" + uri + ", description=" + description + ", fileRole=" + fileRole
                + ", mimeType=" + mimeType + ", submission=" + submission + ", id=" + getId() + "]";
    }
}
