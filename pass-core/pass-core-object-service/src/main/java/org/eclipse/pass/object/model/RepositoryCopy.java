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
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;
import org.eclipse.pass.object.converter.CopyStatusToStringConverter;
import org.eclipse.pass.object.converter.ListToStringConverter;

/**
 * A Repository Copy represents a copy of a Publication that exists in a target Repository.
 *
 * @author Karen Hanson
 */
@Include
@Entity
@Table(name = "pass_repository_copy")
public class RepositoryCopy extends PassEntity {

    /**
     * IDs assigned by the repository
     */
    @Convert(converter = ListToStringConverter.class)
    private List<String> externalIds = new ArrayList<String>();

    /**
     * Status of deposit
     */
    @Convert(converter = CopyStatusToStringConverter.class)
    private CopyStatus copyStatus;

    /**
     * URL to access the item in the repository
     */
    private URI accessUrl;

    /**
     * the Publication that this Repository Copy is a copy of
     */
    @ManyToOne
    private Publication publication;

    /**
     * the Repository the Copy is in
     */
    @ManyToOne
    private Repository repository;

    /**
     * RepositoryCopy constructor
     */
    public RepositoryCopy() {
    }

    /**
     * Copy constructor, this will copy the values of the object provided into the new object
     *
     * @param repositoryCopy the repositoryCopy to copy
     */
    public RepositoryCopy(RepositoryCopy repositoryCopy) {
        super(repositoryCopy);
        this.externalIds = new ArrayList<String>(repositoryCopy.externalIds);
        this.copyStatus = repositoryCopy.copyStatus;
        this.accessUrl = repositoryCopy.accessUrl;
        this.publication = repositoryCopy.publication;
        this.repository = repositoryCopy.repository;
    }

    /**
     * @return the externalIds
     */
    public List<String> getExternalIds() {
        return externalIds;
    }

    /**
     * @param externalIds the externalIds to set
     */
    public void setExternalIds(List<String> externalIds) {
        this.externalIds = externalIds;
    }

    /**
     * @return the repository copy status
     */
    public CopyStatus getCopyStatus() {
        return copyStatus;
    }

    /**
     * @return the accessUrl
     */
    public URI getAccessUrl() {
        return accessUrl;
    }

    /**
     * @param accessUrl the accessUrl to set
     */
    public void setAccessUrl(URI accessUrl) {
        this.accessUrl = accessUrl;
    }

    /**
     * @param copyStatus The repository's status to set
     */
    public void setCopyStatus(CopyStatus copyStatus) {
        this.copyStatus = copyStatus;
    }

    /**
     * @return the publication
     */
    public Publication getPublication() {
        return publication;
    }

    /**
     * @param publication the publication to set
     */
    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    /**
     * @return the repository
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * @param repository the repository to set
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        RepositoryCopy that = (RepositoryCopy) o;

        if (externalIds != null ? !externalIds.equals(that.externalIds) : that.externalIds != null) {
            return false;
        }
        if (copyStatus != null ? !copyStatus.equals(that.copyStatus) : that.copyStatus != null) {
            return false;
        }
        if (accessUrl != null ? !accessUrl.equals(that.accessUrl) : that.accessUrl != null) {
            return false;
        }
        if (publication != null ? !publication.equals(that.publication) : that.publication != null) {
            return false;
        }
        if (repository != null ? !repository.equals(that.repository) : that.repository != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (externalIds != null ? externalIds.hashCode() : 0);
        result = 31 * result + (copyStatus != null ? copyStatus.hashCode() : 0);
        result = 31 * result + (accessUrl != null ? accessUrl.hashCode() : 0);
        result = 31 * result + (publication != null ? publication.hashCode() : 0);
        result = 31 * result + (repository != null ? repository.hashCode() : 0);
        return result;
    }
}
