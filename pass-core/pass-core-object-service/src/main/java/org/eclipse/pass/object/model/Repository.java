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
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;
import org.eclipse.pass.object.converter.IntegrationTypeToStringConverter;
import org.eclipse.pass.object.converter.ListToURIStringConverter;

/**
 * Describes a Repository. A Repository is the target of a Deposit.
 *
 * @author Karen Hanson
 */

@Include
@Entity
@Table(name = "pass_repository")
public class Repository extends PassEntity {

    /**
     * Name of repository e.g. "PubMed Central"
     */
    private String name;

    /**
     * Several sentence description of repository
     */
    private String description;

    /**
     * URL to the homepage of the repository so that PASS users can view the platform before deciding whether to
     * participate in it
     */
    private URI url;

    /**
     * The legal text that a submitter must agree to in order to submit a publication to this repository
     */
    @Column(columnDefinition = "text")
    private String agreementText;

    /**
     * Stringified JSON representing a form template to be loaded by the front-end when this Repository is selected
     */
    @Column(columnDefinition = "text")
    private String formSchema;

    /**
     * Type of integration PASS has with the Repository
     */
    @Convert(converter = IntegrationTypeToStringConverter.class)
    private IntegrationType integrationType;

    /**
     * Key that is unique to this {@code Repository} instance.  Used to reference the {@code Repository} when its URI
     * is not available (e.g. prior to the creation of a {@code Repository} resource in Fedora).
     */
    private String repositoryKey;

    /**
     * URLs that link to JSON schema documents describing the repository's metadata requirements
     */
    @Convert(converter = ListToURIStringConverter.class)
    private List<URI> schemas = new ArrayList<>();

    /**
     * Repository constructor
     */
    public Repository() {
    }

    /**
     * Copy constructor, this will copy the values of the object provided into the new object
     *
     * @param repository the repository to copy
     */
    public Repository(Repository repository) {
        super(repository);
        this.name = repository.name;
        this.description = repository.description;
        this.url = repository.url;
        this.agreementText = repository.agreementText;
        this.formSchema = repository.formSchema;
        this.integrationType = repository.integrationType;
        this.repositoryKey = repository.repositoryKey;
        this.schemas = new ArrayList<>(repository.schemas);
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
     * @return the url
     */
    public URI getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(URI url) {
        this.url = url;
    }

    /**
     * @return the agreement text
     */
    public String getAgreementText() {
        return agreementText;
    }

    /**
     * @param agreementText the agreement text to set
     */
    public void setAgreementText(String agreementText) {
        this.agreementText = agreementText;
    }

    /**
     * @return the formSchema
     */
    public String getFormSchema() {
        return formSchema;
    }

    /**
     * @param formSchema the form schema (typically, a stringified JSON blob)
     */
    public void setFormSchema(String formSchema) {
        this.formSchema = formSchema;
    }

    /**
     * @return the integrationType
     */
    public IntegrationType getIntegrationType() {
        return integrationType;
    }

    /**
     * @param integrationType the integrationType to set
     */
    public void setIntegrationType(IntegrationType integrationType) {
        this.integrationType = integrationType;
    }

    /**
     * Key that is unique to this {@code Repository} instance.  Used to look up the {@code Repository} when its URI
     * is not available (e.g. prior to the creation of a {@code Repository} resource in Fedora).
     *
     * @return a String unique to this {@code Repository} within PASS, may be {@code null}
     */
    public String getRepositoryKey() {
        return repositoryKey;
    }

    /**
     * Key that is unique to this {@code Repository} instance.  Used to look up the {@code Repository} when its URI
     * is not available (e.g. prior to the creation of a {@code Repository} resource in Fedora).
     *
     * @param repositoryKey a String unique to this {@code Repository} within PASS
     */
    public void setRepositoryKey(String repositoryKey) {
        this.repositoryKey = repositoryKey;
    }

    /**
     * @return URLs that link to JSON schema documents describing the repository's metadata requirements
     */
    public List<URI> getSchemas() {
        return schemas;
    }

    /**
     * @param schemas URLs that link to JSON schema documents describing the repository's metadata requirements
     */
    public void setSchemas(List<URI> schemas) {
        this.schemas = schemas;
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

        Repository that = (Repository) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }
        if (formSchema != null ? !formSchema.equals(that.formSchema) : that.formSchema != null) {
            return false;
        }
        if (integrationType != null ? !integrationType.equals(that.integrationType) : that.integrationType != null) {
            return false;
        }
        if (agreementText != null ? !agreementText.equals(that.agreementText) : that.agreementText != null) {
            return false;
        }
        if (repositoryKey != null ? !repositoryKey.equals(that.repositoryKey) : that.repositoryKey != null) {
            return false;
        }
        if (schemas != null ? !schemas.equals(that.schemas) : that.schemas != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (formSchema != null ? formSchema.hashCode() : 0);
        result = 31 * result + (integrationType != null ? integrationType.hashCode() : 0);
        result = 31 * result + (agreementText != null ? agreementText.hashCode() : 0);
        result = 31 * result + (repositoryKey != null ? repositoryKey.hashCode() : 0);
        result = 31 * result + (schemas != null ? schemas.hashCode() : 0);
        return result;
    }
}
