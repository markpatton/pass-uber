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
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;

/**
 * Describes a Policy. Policies determine the rules that need to be followed by a Submission.
 *
 * @author Karen Hanson
 */
@Include
@Entity
@Table(name = "pass_policy")
public class Policy extends PassEntity {

    /**
     * Title of policy e.g. "NIH Public Access Policy"
     */
    private String title;

    /**
     * Several sentence description of policy
     */
    @Column(columnDefinition = "text")
    private String description;

    /**
     * A link to the actual policy on the policy-owner's page
     */
    private URI policyUrl;

    /**
     * List of repositories that can satisfying this policy
     */
    @ManyToMany
    private List<Repository> repositories = new ArrayList<>();

    /**
     * the Institution whose Policy this is (note: if institution has a value, funder should be null)
     */
    private URI institution;

    /**
     * Policy constructor
     */
    public Policy() {
    }

    ;

    /**
     * Copy constructor, this will copy the values of the object provided into the new object
     *
     * @param policy the policy to copy
     */
    public Policy(Policy policy) {
        super(policy);
        this.title = policy.title;
        this.description = policy.description;
        this.policyUrl = policy.policyUrl;
        this.repositories = new ArrayList<Repository>(policy.repositories);
        this.institution = policy.institution;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
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
     * @return the policy URL
     */
    public URI getPolicyUrl() {
        return policyUrl;
    }

    /**
     * @param policyUrl the policyUrl to set
     */
    public void setPolicyUrl(URI policyUrl) {
        this.policyUrl = policyUrl;
    }

    /**
     * @return the institution
     */
    public URI getInstitution() {
        return institution;
    }

    /**
     * @param institution the institution to set
     */
    public void setInstitution(URI institution) {
        this.institution = institution;
    }

    /**
     * @return the list of repositories
     */
    public List<Repository> getRepositories() {
        return repositories;
    }

    /**
     * @param repositories list repositories to set
     */
    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
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

        Policy that = (Policy) o;

        if (title != null ? !title.equals(that.title) : that.title != null) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (policyUrl != null ? !policyUrl.equals(that.policyUrl) : that.policyUrl != null) {
            return false;
        }
        if (repositories != null ? !repositories.equals(that.repositories) : that.repositories != null) {
            return false;
        }
        if (institution != null ? !institution.equals(that.institution) : that.institution != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (policyUrl != null ? policyUrl.hashCode() : 0);
        result = 31 * result + (repositories != null ? repositories.hashCode() : 0);
        result = 31 * result + (institution != null ? institution.hashCode() : 0);
        return result;
    }
}
