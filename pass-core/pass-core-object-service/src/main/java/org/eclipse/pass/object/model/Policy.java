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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
public class Policy {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

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
    private String policyUrl;

    /**
     * List of URIs for repositories that can satisfying this policy
     */
    @ManyToMany
    private List<Repository> repositories = new ArrayList<>();

    /**
     * URI of the Institution whose Policy this is (note: if institution has a value, funder should be null)
     */
    private String institution;
}
