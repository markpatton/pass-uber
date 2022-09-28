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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;
import org.eclipse.pass.object.converter.AwardStatusToStringConverter;


/**
 * Grant model for the PASS system
 *
 * @author Karen Hanson
 */

@Include
@Entity
@Table(name = "pass_grant")
public class Grant extends PassEntity {

    /**
     * Award number from funder
     */
    private String awardNumber;

    /**
     * Status of award
     */
    @Convert(converter = AwardStatusToStringConverter.class)
    private AwardStatus awardStatus;

    /**
     * A local key assigned to the Grant within the researcher's institution to support matching
     * between PASS and a local system. In the case of JHU this is the key assigned by COEUS
     */
    private String localKey;

    /**
     * Title of the research project
     */
    private String projectName;

    /**
     * The funder.id of the sponsor that is the original source of the funds
     */
    @ManyToOne
    private Funder primaryFunder;

    /**
     * The funder.id of the organization from which funds are directly received
     */
    @ManyToOne
    private Funder directFunder;

    /**
     * The User who is the Principal investigator
     */
    @ManyToOne
    private User pi;

    /**
     * List of User who are the co-principal investigators
     */
    @ManyToMany
    private List<User> coPis = new ArrayList<>();

    /**
     * Date the grant was awarded
     */
    private ZonedDateTime awardDate;

    /**
     * Date the grant started
     */
    private ZonedDateTime startDate;

    /**
     * Date the grant ended
     */
    private ZonedDateTime endDate;

    /**
     * Grant constructor
     */
    public Grant() {
    }

    /**
     * Copy constructor, this will copy the values of the object provided into the new object
     *
     * @param grant the grant to copy
     */
    public Grant(Grant grant) {
        super(grant);
        this.awardNumber = grant.awardNumber;
        this.awardStatus = grant.awardStatus;
        this.localKey = grant.localKey;
        this.projectName = grant.projectName;
        this.primaryFunder = grant.primaryFunder;
        this.directFunder = grant.directFunder;
        this.pi = grant.pi;
        this.coPis = new ArrayList<User>(grant.coPis);
        this.awardDate = grant.awardDate;
        this.startDate = grant.startDate;
        this.endDate = grant.endDate;
    }

    /**
     * @return the awardNumber
     */
    public String getAwardNumber() {
        return awardNumber;
    }

    /**
     * @param awardNumber the awardNumber to set
     */
    public void setAwardNumber(String awardNumber) {
        this.awardNumber = awardNumber;
    }

    /**
     * @return the awardStatus
     */
    public AwardStatus getAwardStatus() {
        return awardStatus;
    }

    /**
     * @param awardStatus the awardStatus to set
     */
    public void setAwardStatus(AwardStatus awardStatus) {
        this.awardStatus = awardStatus;
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

    /**
     * @return the projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * @param projectName the projectName to set
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * @return the primaryFunder
     */
    public Funder getPrimaryFunder() {
        return primaryFunder;
    }

    /**
     * @param primaryFunder the primaryFunder to set
     */
    public void setPrimaryFunder(Funder primaryFunder) {
        this.primaryFunder = primaryFunder;
    }

    /**
     * @return the directFunder
     */
    public Funder getDirectFunder() {
        return directFunder;
    }

    /**
     * @param directFunder the directFunder to set
     */
    public void setDirectFunder(Funder directFunder) {
        this.directFunder = directFunder;
    }

    /**
     * @return the pi
     */
    public User getPi() {
        return pi;
    }

    /**
     * @param pi the pi to set
     */
    public void setPi(User pi) {
        this.pi = pi;
    }

    /**
     * @return the coPis
     */
    public List<User> getCoPis() {
        return coPis;
    }

    /**
     * @param coPis the coPis to set
     */
    public void setCoPis(List<User> coPis) {
        this.coPis = coPis;
    }

    /**
     * @return the awardDate
     */
    public ZonedDateTime getAwardDate() {
        return awardDate;
    }

    /**
     * @param awardDate the awardDate to set
     */
    public void setAwardDate(ZonedDateTime awardDate) {
        this.awardDate = awardDate;
    }

    /**
     * @return the startDate
     */
    public ZonedDateTime getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public ZonedDateTime getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
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

        Grant that = (Grant) o;

        if (awardNumber != null ? !awardNumber.equals(that.awardNumber) : that.awardNumber != null) {
            return false;
        }
        if (awardStatus != null ? !awardStatus.equals(that.awardStatus) : that.awardStatus != null) {
            return false;
        }
        if (localKey != null ? !localKey.equals(that.localKey) : that.localKey != null) {
            return false;
        }
        if (projectName != null ? !projectName.equals(that.projectName) : that.projectName != null) {
            return false;
        }
        if (primaryFunder != null ? !primaryFunder.equals(that.primaryFunder) : that.primaryFunder != null) {
            return false;
        }
        if (directFunder != null ? !directFunder.equals(that.directFunder) : that.directFunder != null) {
            return false;
        }
        if (pi != null ? !pi.equals(that.pi) : that.pi != null) {
            return false;
        }
        if (coPis != null ? !coPis.equals(that.coPis) : that.coPis != null) {
            return false;
        }
        if (awardDate != null ? !awardDate.equals(that.awardDate) : that.awardDate != null) {
            return false;
        }
        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null) {
            return false;
        }
        if (endDate != null ? !endDate.equals(that.endDate) : that.endDate != null) {
            return false;
        }
        return true;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (awardNumber != null ? awardNumber.hashCode() : 0);
        result = 31 * result + (awardStatus != null ? awardStatus.hashCode() : 0);
        result = 31 * result + (localKey != null ? localKey.hashCode() : 0);
        result = 31 * result + (projectName != null ? projectName.hashCode() : 0);
        result = 31 * result + (primaryFunder != null ? primaryFunder.hashCode() : 0);
        result = 31 * result + (directFunder != null ? directFunder.hashCode() : 0);
        result = 31 * result + (pi != null ? pi.hashCode() : 0);
        result = 31 * result + (coPis != null ? coPis.hashCode() : 0);
        result = 31 * result + (awardDate != null ? awardDate.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }
}
