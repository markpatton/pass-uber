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
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yahoo.elide.RefreshableElide;
import org.eclipse.pass.object.model.Policy;
import org.eclipse.pass.object.model.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class defines Policy and Repository service endpoints and orchestrates responses
 *
 * @author jrm
 */
@RestController
public class PassPolicyServiceController {

    @Value("${pass.policy.institution}")
    private String institution;

    @Value("${pass.policy.institutional_policy_title}")
    private String institutionalPolicyTitle;

    @Value("${pass.policy.institutional_repository_name}")
    private String institutionalRepositoryName;

    private static final Logger LOG = LoggerFactory.getLogger(PassPolicyServiceController.class);
    private final PolicyService policyService;

    /**
     * PassPolicyServiceController Constructor
     *
     * @param refreshableElide A refreshable Elide instance
     */
    public PassPolicyServiceController(RefreshableElide refreshableElide) {
        this.policyService = new SimplePolicyService(refreshableElide);
    }

    /**
     * Handles incoming GET requests to the /policy/policies endpoint
     *
     * @param request the incoming request
     * @param response the outgoing response
     * @throws IOException if an IO exception occurs
     */
    @GetMapping("/policy/policies")
    public void doGetPolicies(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        LOG.info("Servicing new request......");
        LOG.debug("Context path: " + request.getContextPath() + "; query string " + request.getQueryString());

        // retrieve submission ID from request
        String submissionParameter = request.getParameter("submission");
        Long submissionId;
        try {
            submissionId = Long.parseLong(submissionParameter);
        } catch (NumberFormatException nfe) {
            submissionId = null;
        }
        Principal userPrincipal = request.getUserPrincipal();

        // handle empty or invalid request submission error
        if (submissionId == null) {
            set_error_response(response, "Missing or invalid submission parameter: " +
                                         "must be a String representation of a Long", HttpStatus.BAD_REQUEST);
            return;
        }

        Set<Policy> policies;
        try {
            policies = policyService.findPoliciesForSubmission(submissionId, userPrincipal,
                                                               institution, institutionalPolicyTitle);
        } catch (IOException ioe) {
            set_error_response(response, "IO error encountered connecting to data store",
                               HttpStatus.INTERNAL_SERVER_ERROR);
            LOG.debug(ioe.getMessage(), ioe);
            return;
        }

        JsonArrayBuilder jab = Json.createArrayBuilder();
        for (Policy policy : policies) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("id", policy.getId().toString());
            if (policy.getTitle() != null
                && policy.getTitle().equals(institutionalPolicyTitle)) {
                job.add("type", "institution");
            } else {
                job.add("type", "funder");
            }
            jab.add(job.build());
        }
        JsonArray array = jab.build();
        set_array_response(response,  array);
    }

    /**
     * Handles incoming GET requests to the /policy/repositories endpoint
     *
     * @param request the incoming request
     * @param response the outgoing response
     * @throws IOException if an IO exception occurs
     */
    @GetMapping("/policy/repositories")
    public void doGetRepositories(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        LOG.info("Servicing new request......");
        LOG.debug("Context path: " + request.getContextPath() + "; query string " + request.getQueryString());

        // retrieve submission parameter value from request
        String submissionParameterValue = request.getParameter("submission");
        Long submissionId;
        try {
            submissionId = Long.parseLong(submissionParameterValue);
        } catch (NumberFormatException nfe) {
            submissionId = null;
        }

        Principal userPrincipal = request.getUserPrincipal();

        // handle empty or invalid request submission error
        if (submissionId == null) {
            set_error_response(response, "Missing or invalid submission parameter: " +
                                         "must be a String representation of a Long", HttpStatus.BAD_REQUEST);
            return;
        }

        Set<Repository> repositories;
        try {
            repositories = policyService.findRepositoriesForSubmission(submissionId, userPrincipal,
                                                                       institution, institutionalPolicyTitle);
        } catch (IOException ioe) {
            set_error_response(response, "IO error encountered connecting to data store",
                               HttpStatus.INTERNAL_SERVER_ERROR);
            LOG.debug(ioe.getMessage(), ioe);
            return;
        }

        JsonObjectBuilder outerObject = Json.createObjectBuilder();
        JsonArrayBuilder optional = Json.createArrayBuilder();
        JsonArrayBuilder required = Json.createArrayBuilder();

        for (Repository repository : repositories) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("url", repository.getId().toString());
            if (repository.getName() != null
                && repository.getName().equals(institutionalRepositoryName)
                && repositories.size() > 1) {
                job.add("selected", "true");
                optional.add(job.build());
            } else {
                job.add("selected", "true");
                required.add(job.build());
            }
        }

        outerObject.add("optional", optional);
        outerObject.add("required", required);

        set_object_response(response,  outerObject.build());
    }

    private void set_object_response(HttpServletResponse response, JsonObject obj) throws IOException {
        response.getWriter().print(obj.toString());
        response.setStatus(HttpStatus.OK.value());
    }

    private void set_array_response(HttpServletResponse response, JsonArray obj) throws IOException {
        response.getWriter().print(obj.toString());
        response.setStatus(HttpStatus.OK.value());
    }

    private void set_error_response(HttpServletResponse response, String message,
                                    HttpStatus status) throws IOException {
        JsonObject obj = Json.createObjectBuilder().add("message", message).build();
        response.getWriter().print(obj.toString());
        response.setStatus(status.value());
        LOG.error(message);
    }

}
