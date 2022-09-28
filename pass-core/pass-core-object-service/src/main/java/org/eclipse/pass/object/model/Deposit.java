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

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;
import org.eclipse.pass.object.converter.DepositStatusToStringConverter;

/**
 * A Submission can have multiple Deposits, each to a different Repository. This describes a single deposit to a
 * Repository and captures
 * its current status.
 *
 * @author Karen Hanson
 */

@Include
@Entity
@Table(name = "pass_deposit")
public class Deposit extends PassEntity {

    /**
     * A URL or some kind of reference that can be dereferenced, entity body parsed, and used to determine the status
     * of Deposit
     */
    private String depositStatusRef;

    /**
     * Status of deposit
     */
    @Convert(converter = DepositStatusToStringConverter.class)
    private DepositStatus depositStatus;

    /**
     * the Submission that this Deposit is a part of
     */
    @ManyToOne
    private Submission submission;

    /**
     * the Repository being deposited to
     */
    @ManyToOne
    private Repository repository;

    /**
     * the Repository Copy representing the copy that is reltaed to this Deposit. The value is null if there
     * is no copy
     */
    @ManyToOne
    private RepositoryCopy repositoryCopy;

    /**
     * Deposit constructor
     */
    public Deposit() {
    }

    /**
     * Copy constructor, this will copy the values of the object provided into the new object
     *
     * @param deposit the deposit to copy
     */
    public Deposit(Deposit deposit) {
        super(deposit);
        this.depositStatusRef = deposit.depositStatusRef;
        this.depositStatus = deposit.depositStatus;
        this.submission = deposit.submission;
        this.repository = deposit.repository;
        this.repositoryCopy = deposit.repositoryCopy;
    }

    /**
     * @return the deposit status
     */
    public DepositStatus getDepositStatus() {
        return depositStatus;
    }

    /**
     * @param depositStatus status the deposit status to set
     */
    public void setDepositStatus(DepositStatus depositStatus) {
        this.depositStatus = depositStatus;
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

    /**
     * @return the depositStatusRef
     */
    public String getDepositStatusRef() {
        return depositStatusRef;
    }

    /**
     * @param depositStatusRef the depositStatusRef to set
     */
    public void setDepositStatusRef(String depositStatusRef) {
        this.depositStatusRef = depositStatusRef;
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

    /**
     * @return the repositoryCopy
     */
    public RepositoryCopy getRepositoryCopy() {
        return repositoryCopy;
    }

    /**
     * @param repositoryCopy the repositoryCopy to set
     */
    public void setRepositoryCopy(RepositoryCopy repositoryCopy) {
        this.repositoryCopy = repositoryCopy;
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

        Deposit that = (Deposit) o;

        if (depositStatusRef != null ? !depositStatusRef.equals(
            that.depositStatusRef) : that.depositStatusRef != null) {
            return false;
        }
        if (depositStatus != null ? !depositStatus.equals(that.depositStatus) : that.depositStatus != null) {
            return false;
        }
        if (submission != null ? !submission.equals(that.submission) : that.submission != null) {
            return false;
        }
        if (repository != null ? !repository.equals(that.repository) : that.repository != null) {
            return false;
        }
        if (repositoryCopy != null ? !repositoryCopy.equals(that.repositoryCopy) : that.repositoryCopy != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (depositStatusRef != null ? depositStatusRef.hashCode() : 0);
        result = 31 * result + (depositStatus != null ? depositStatus.hashCode() : 0);
        result = 31 * result + (submission != null ? submission.hashCode() : 0);
        result = 31 * result + (repository != null ? repository.hashCode() : 0);
        result = 31 * result + (repositoryCopy != null ? repositoryCopy.hashCode() : 0);
        return result;
    }
}
