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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
public class Funder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Funder name
     */
    private String name;

    /**
     * Funder URL
     */
    private String url;

    /**
     * URI of the Policy associated with funder
     */
    @ManyToOne
    private Policy policy;

    /**
     * Local key assigned to the funder within the researcher's institution to support matching between
     * PASS and a local system. In the case of JHU this is the key assigned in COEUS
     */
    private String localKey;
}
