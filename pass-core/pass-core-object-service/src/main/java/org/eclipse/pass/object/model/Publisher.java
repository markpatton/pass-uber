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

/**
 * Describes a Publisher and its related Journals, also the path of it's participation in PubMedCentral
 *
 * @author Karen Hanson
 */

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;

@Include
@Entity
@Table(name = "pass_publisher")
public class Publisher extends PassEntity {

    /**
     * Name of publisher
     */
    private String name;

    /**
     * This field indicates whether a journal participates in the NIH Public Access Program by sending final
     * published article to PMC. If so, whether it requires additional processing fee.
     */

    @Enumerated(EnumType.STRING)
    private PmcParticipation pmcParticipation;

    /**
     * Publisher constructor
     */
    public Publisher() {
    }

    /**
     * Copy constructor, this will copy the values of the object provided into the new object
     *
     * @param publisher the publisher to copy
     */
    public Publisher(Publisher publisher) {
        super(publisher);
        this.name = publisher.name;
        this.pmcParticipation = publisher.pmcParticipation;
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
     * @return the pmcParticipation
     */
    public PmcParticipation getPmcParticipation() {
        return pmcParticipation;
    }

    /**
     * @param pmcParticipation the pmcParticipation to set
     */
    public void setPmcParticipation(PmcParticipation pmcParticipation) {
        this.pmcParticipation = pmcParticipation;
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

        Publisher that = (Publisher) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (pmcParticipation != null ? !pmcParticipation.equals(
            that.pmcParticipation) : that.pmcParticipation != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (pmcParticipation != null ? pmcParticipation.hashCode() : 0);
        return result;
    }
}
