/*
 *
 * Copyright 2023 Johns Hopkins University
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.eclipse.pass.policy.service;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import org.eclipse.pass.object.model.Policy;
import org.eclipse.pass.object.model.Repository;

/**
 * Interface for Policy Sevice implementations.
 *
 * @author jrm
 */
public interface PolicyService {

    /**
     * Retrieve the Set of Policies associated with this submission
     *
     * @param submissionId - The id for the submission
     * @param userPrincipal - The user principal
     * @param institution - the value for the institution as on the Affiliations field for a user, e.g. johnshopkins.edu
     * @param institutionalPolicyTitle - the value for title on the institutions Policy object
     * @throws IOException if the connection to the datastore fails
     */
    Set<Policy> findPoliciesForSubmission(Long submissionId, Principal userPrincipal,
                                          String institution, String institutionalPolicyTitle)
        throws IOException;

    /**
     * Retrieve the Ser of Repositories this submission may be deposited into
     *
     * @param submissionId - The id for the submission
     * @param userPrincipal - The user principal
     * @param institution - the value for the institution as on the Affiliations field for a user, e.g. johnshopkins.edu
     * @param institutionalPolicyTitle - the value for title on the institutions Policy object
     * @throws IOException if the connection to the datastore fails
     */
    Set<Repository> findRepositoriesForSubmission(Long submissionId, Principal userPrincipal,
                                                  String institution, String institutionalPolicyTitle)
        throws IOException;
}
