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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
public class Publication {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

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
     * URI of the journal the publication is part of (if article)
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
}
