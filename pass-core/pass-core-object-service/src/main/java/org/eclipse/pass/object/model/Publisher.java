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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;

@Include
@Entity
@Table(name = "pass_publisher")
public class Publisher {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    /**
     * Name of publisher
     */
    private String name;

    /**
     * This field indicates whether a journal participates in the NIH Public Access Program by sending final
     * published article to PMC. If so, whether it requires additional processing fee.
     */

    @Enumerated(EnumType.STRING)
    private PmcParticipation pcmParticipation;
}
