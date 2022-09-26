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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipse.pass.object.converter.ListToStringConverter;

import com.yahoo.elide.annotation.Include;

/**
 * Describes a Journal and the path of it's participation in PubMedCentral
 *
 * @author Karen Hanson
 */

@Include
@Entity
@Table(name = "pass_journal")
public class Journal extends PassEntity {

    /**
     * Name of journal
     */
    private String journalName;

    /**
     * Array of ISSN(s) for Journal
     */
    @Convert(converter = ListToStringConverter.class)
    private List<String> issns = new ArrayList<>();

    /**
     * ID of publisher
     */
    @ManyToOne
    private Publisher publisher;

    /**
     * National Library of Medicine Title Abbreviation
     */
    private String nlmta;

    /**
     * This field indicates whether a journal participates in the NIH Public Access Program by sending final
     * published article to PMC. If so, whether it requires additional processing fee.
     */
    @Enumerated(EnumType.STRING)
    private PmcParticipation pmcParticipation;

    /**
     * Journal constructor
     */
    public Journal() {
    }

    /**
     * Copy constructor, this will copy the values of the object provided into the new object
     *
     * @param journal the journal to copy
     */
    public Journal(Journal journal) {
        super(journal);
        this.journalName = journal.journalName;
        this.issns = new ArrayList<String>(journal.issns);
        this.publisher = journal.publisher;
        this.nlmta = journal.nlmta;
        this.pmcParticipation = journal.pmcParticipation;
    }

    /**
     * @return the journalName
     */
    public String getJournalName() {
        return journalName;
    }

    /**
     * @param journalName the journalName to set
     */
    public void setJournalName(String journalName) {
        this.journalName = journalName;
    }

    /**
     * @return the issns
     */
    public List<String> getIssns() {
        return issns;
    }

    /**
     * @param issn the issn list to set
     */
    public void setIssns(List<String> issn) {
        this.issns = issn;
    }

    /**
     * @return the publisher ID
     */
    public Publisher getPublisher() {
        return publisher;
    }

    /**
     * @param publisher the publisher to set
     */
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    /**
     * @return the nlmta
     */
    public String getNlmta() {
        return nlmta;
    }

    /**
     * @param nlmta the nlmta to set
     */
    public void setNlmta(String nlmta) {
        this.nlmta = nlmta;
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

        Journal that = (Journal) o;

        if (journalName != null ? !journalName.equals(that.journalName) : that.journalName != null) {
            return false;
        }
        if (issns != null ? !issns.equals(that.issns) : that.issns != null) {
            return false;
        }
        if (publisher != null ? !publisher.equals(that.publisher) : that.publisher != null) {
            return false;
        }
        if (nlmta != null ? !nlmta.equals(that.nlmta) : that.nlmta != null) {
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
        result = 31 * result + (journalName != null ? journalName.hashCode() : 0);
        result = 31 * result + (issns != null ? issns.hashCode() : 0);
        result = 31 * result + (publisher != null ? publisher.hashCode() : 0);
        result = 31 * result + (nlmta != null ? nlmta.hashCode() : 0);
        result = 31 * result + (pmcParticipation != null ? pmcParticipation.hashCode() : 0);
        return result;
    }
}
