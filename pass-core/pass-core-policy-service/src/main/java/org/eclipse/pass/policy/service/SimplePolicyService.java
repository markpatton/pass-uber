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
import java.util.HashSet;
import java.util.Set;

import com.yahoo.elide.RefreshableElide;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.PassClientResult;
import org.eclipse.pass.object.PassClientSelector;
import org.eclipse.pass.object.RSQL;
import org.eclipse.pass.object.model.Funder;
import org.eclipse.pass.object.model.Grant;
import org.eclipse.pass.object.model.Policy;
import org.eclipse.pass.object.model.Repository;
import org.eclipse.pass.object.model.Submission;
import org.eclipse.pass.object.model.User;

/**
 * Simple implementation of the Policy Service interface. Provides Sets of policies or repositories
 *
 * @author jrm
 */
public class SimplePolicyService implements PolicyService {

    private final RefreshableElide refreshableElide;

    /**
     * SimplePolicyService constructor
     * @param refreshableElide a RefreshableElide instance
     */
    public SimplePolicyService(RefreshableElide refreshableElide) {
        this.refreshableElide = refreshableElide;
    }

    @Override
    public Set<Policy> findPoliciesForSubmission(Long submissionId, Principal userPrincipal, String institution,
                                                 String institutionalPolicyTitle) throws IOException {
        try (PassClient passClient = PassClient.newInstance(refreshableElide)) {
            Submission submission = passClient.getObject(Submission.class, submissionId);

            return findPoliciesForSubmission(passClient, submission, userPrincipal, institution,
                    institutionalPolicyTitle);
        }
    }

    private Set<Policy> findPoliciesForSubmission(PassClient passClient, Submission submission,
            Principal userPrincipal, String institution, String institutionalPolicyTitle) throws IOException {
        Set<Policy> policies = new HashSet<>(); //use Set to avoid duplicates

        for (Grant grant : submission.getGrants()) {
            for (Funder funder : getFunders(grant)) {
                if (funder.getPolicy() != null) {
                    policies.add(funder.getPolicy());
                }
            }
        }

        //If the user is an affiliate of the institution, add the institution's policy
        String user_name = userPrincipal.getName();
        PassClientSelector<User> userSelector = new PassClientSelector<>(User.class);
        userSelector.setFilter(RSQL.equals("username", user_name));
        PassClientResult<User> userResult = passClient.selectObjects(userSelector);

        if (userResult.getObjects().size() == 1
                && userResult.getObjects().get(0).getAffiliation().contains(institution)
                && institutionalPolicyTitle != null) { //have a unique user in the system
            PassClientSelector<Policy> policySelector = new PassClientSelector<>(Policy.class);
            policySelector.setFilter(RSQL.equals("title", institutionalPolicyTitle));
            PassClientResult<Policy> policyResult = passClient.selectObjects(policySelector);
            if (policyResult.getObjects().size() == 1) {
                policies.add(policyResult.getObjects().get(0));
            }

        }
        return policies;
    }

    @Override
    public Set<Repository> findRepositoriesForSubmission(Long submissionId, Principal userPrincipal,
                                                         String institution, String institutionalPolicyTitle)
        throws IOException {
        try (PassClient passClient = PassClient.newInstance(refreshableElide)) {
            Submission submission = passClient.getObject(Submission.class, submissionId);

            // Set of policies is intersection of effective policies on submission and the computed policies

            Set<Policy> policies = new HashSet<>(submission.getEffectivePolicies());
            policies.retainAll(findPoliciesForSubmission(passClient, submission, userPrincipal, institution,
                    institutionalPolicyTitle));

            Set<Repository> repositories = new HashSet<>();

            policies.forEach(p -> {
                repositories.addAll(p.getRepositories());
            });

            return repositories;
        }
    }

    /**
     * A convenience method
     *
     * @param grant - the Grant to find Funders for
     * @return - the Set of Funders for the provided Grant
     */
    private Set<Funder> getFunders(Grant grant) {
        Set<Funder> funders = new HashSet<>(); // use Set to avoid duplicates
        Funder primaryFunder = grant.getPrimaryFunder();
        Funder directFunder = grant.getDirectFunder();

        if (primaryFunder != null) {
            funders.add(primaryFunder);
        }
        if (directFunder != null) {
            funders.add(directFunder);
        }

        return funders;
    }

}
