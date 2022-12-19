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

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;

/**
 * Publication model. Contains details of work being submitted, where it is being deposited to, related Grants etc.
 *
 * @author Karen Hanson
 */
@Include
@Entity
@Table(name = "pass_publication")
public class Publication extends PassEntity {

    /**
     * Title of publication
     */
    private String title;

    /**
     * Abstract of the publication
     */
    @Column(columnDefinition = "text")
    private String publicationAbstract;

    /**
     * DOI of the publication
     */
    private String doi;

    /**
     * PMID of the publication
     */
    private String pmid;

    /**
     * The journal the publication is part of (if article)
     */
    @ManyToOne
    private Journal journal;

    /**
     * Volume of journal that contains the publication (if article)
     */
    private String volume;

    /**
     * Issue of journal that contains the publication (if article)
     */
    private String issue;

    /**
     * Publication constructor
     */
    public Publication() {
    }

    /**
     * Copy constructor, this will copy the values of the object provided into the new object
     *
     * @param publication the publication to copy
     */
    public Publication(Publication publication) {
        super(publication);
        this.title = publication.title;
        this.publicationAbstract = publication.publicationAbstract;
        this.doi = publication.doi;
        this.pmid = publication.pmid;
        this.journal = publication.journal;
        this.volume = publication.volume;
        this.issue = publication.issue;
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
     * @return the publication abstract
     */
    public String getPublicationAbstract() {
        return publicationAbstract;
    }

    /**
     * @param publicationAbstract the publicationAbstract to set
     */
    public void setPublicationAbstract(String publicationAbstract) {
        this.publicationAbstract = publicationAbstract;
    }

    /**
     * @return the doi
     */
    public String getDoi() {
        return doi;
    }

    /**
     * @param doi the doi to set
     */
    public void setDoi(String doi) {
        this.doi = doi;
    }

    /**
     * @return the pmid
     */
    public String getPmid() {
        return pmid;
    }

    /**
     * @param pmid the pmid to set
     */
    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    /**
     * @return the Journal
     */
    public Journal getJournal() {
        return journal;
    }

    /**
     * @param journal the journal to set
     */
    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    /**
     * @return the volume
     */
    public String getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(String volume) {
        this.volume = volume;
    }

    /**
     * @return the issue
     */
    public String getIssue() {
        return issue;
    }

    /**
     * @param issue the issue to set
     */
    public void setIssue(String issue) {
        this.issue = issue;
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
        Publication other = (Publication) obj;
        return Objects.equals(doi, other.doi) && Objects.equals(issue, other.issue)
                && Objects.equals(journal, other.journal) && Objects.equals(pmid, other.pmid)
                && Objects.equals(publicationAbstract, other.publicationAbstract) && Objects.equals(title, other.title)
                && Objects.equals(volume, other.volume);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), doi, title);
    }

    @Override
    public String toString() {
        return "Publication [title=" + title + ", publicationAbstract=" + publicationAbstract + ", doi=" + doi
                + ", pmid=" + pmid + ", journal=" + journal + ", volume=" + volume + ", issue=" + issue
                + ", id=" + getId() + "]";
    }
}
