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
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;

/**
 * The funder or sponsor of Grant or award.
 *
 * @author Karen Hanson
 */

@Include
@Entity
@Table(name = "pass_funder")
public class Funder extends PassEntity {

    /**
     * Funder name
     */
    private String name;

    /**
     * Funder URL
     */
    private URI url;

    /**
     * the Policy associated with funder
     */
    @ManyToOne
    private Policy policy;

    /**
     * Local key assigned to the funder within the researcher's institution to support matching between
     * PASS and a local system. In the case of JHU this is the key assigned in COEUS
     */
    private String localKey;

    /**
     * Funder constructor
     */
    public Funder() {
    }

    /**
     * Copy constructor, this will copy the values of the object provided into the new object
     *
     * @param funder the funder to copy
     */
    public Funder(Funder funder) {
        super(funder);
        this.name = funder.name;
        this.url = funder.url;
        this.policy = funder.policy;
        this.localKey = funder.localKey;
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
     * @return the the policy
     */
    public Policy getPolicy() {
        return policy;
    }

    /**
     * @param policy the policy to set
     */
    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    /**
     * @return the localKey
     */
    public String getLocalKey() {
        return localKey;
    }

    /**
     * @param localKey the localKey to set
     */
    public void setLocalKey(String localKey) {
        this.localKey = localKey;
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

        Funder that = (Funder) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }
        if (policy != null ? !policy.equals(that.policy) : that.policy != null) {
            return false;
        }
        if (localKey != null ? !localKey.equals(that.localKey) : that.localKey != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (policy != null ? policy.hashCode() : 0);
        result = 31 * result + (localKey != null ? localKey.hashCode() : 0);
        return result;
    }
}
