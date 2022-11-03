/*
 *
 * Copyright 2019 Johns Hopkins University
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
package org.eclipse.pass.doi.service;

import java.io.IOException;
import java.io.OutputStream;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yahoo.elide.RefreshableElide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PassDoiServiceController {

    private static final Logger LOG = LoggerFactory.getLogger(PassDoiServiceController.class);
    ElideConnector elideConnector;
    XrefConnector xrefConnector;

    PassDoiServiceController(RefreshableElide refreshableElide) {
        this.elideConnector = new ElideConnector(refreshableElide);
        this.xrefConnector = new XrefConnector();
    }

    @GetMapping("pass-doi-service/journal")
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        LOG.info("Servicing new request ... ");
        LOG.debug("Context path: " + request.getContextPath() + "; query string " + request.getQueryString());

        //we will call out to crossref and collect the work JSON object
        //the value of this parameter is expected to be already URIencoded
        String doi = request.getParameter("doi");

        //stage 1: verify doi is valid
        if (xrefConnector.verify(doi) == null) {
            // do not have have a valid xref doi
            try (OutputStream out = response.getOutputStream()) {
                JsonObject jsonObject = Json.createObjectBuilder()
                                            .add("error", "Supplied DOI is not in valid Crossref format.")
                                            .build();
                out.write(jsonObject.toString().getBytes());
                response.setStatus(400);
                return;
            }
        }

        //Stage 2: make sure we don't already have a request being processed for this doi
        //
        if (xrefConnector.isAlreadyActive(doi)) {
            // return already processing error (429>)
            try (OutputStream out = response.getOutputStream()) {
                String message = "There is already an active request for " + doi;
                JsonObject jsonObject = Json.createObjectBuilder()
                                            .add("error", message + "; try again later.")
                                            .build();
                out.write(jsonObject.toString().getBytes());
                response.setStatus(429);
                LOG.info(message);
            }
        }

        //stage 3: try to get crossref record, catch errors first, and halt processing
        JsonObject xrefJsonObject = xrefConnector.retrieveXrefMetdata(doi);
        if (xrefJsonObject == null) {
            try (OutputStream out = response.getOutputStream()) {
                String message = "There was an error getting the metadata from Crossref for " + doi;
                JsonObject jsonObject = Json.createObjectBuilder()
                                            .add("error", message)
                                            .build();
                out.write(jsonObject.toString().getBytes());
                response.setStatus(500);
                LOG.info(message);
            }
        } else if (xrefJsonObject.getJsonString("error") != null) {
            int responseCode;
            String message;
            if (xrefJsonObject.getString("error").equals("Resource not found.")) {
                responseCode = 404;
                message = "The resource for DOI " + doi + " could not be found on Crossref.";
            } else {
                responseCode = 500;
                message = "A record for this resource could not be returned from Crossref: " +
                          xrefJsonObject.getJsonString("error");
            }
            try (OutputStream out = response.getOutputStream()) {
                JsonObject jsonObject = Json.createObjectBuilder()
                                            .add("error", message)
                                            .build();
                out.write(jsonObject.toString().getBytes());
                response.setStatus(responseCode);
                LOG.info(message);
            }
        } else {
            // have a non-empty string to process
            String journalId = elideConnector.resolveJournal(xrefJsonObject);
            if (journalId != null) {

                try (OutputStream out = response.getOutputStream()) {
                    JsonObject jsonObject = Json.createObjectBuilder()
                                                .add("journal-id", journalId)
                                                .add("crossref", xrefJsonObject)
                                                .build();

                    out.write(jsonObject.toString().getBytes());
                    response.setStatus(200);
                    LOG.info("Returning result for DOI " + doi);
                }

            } else {
                // journal id is null - this should never happen unless Crosssref journal is insufficient
                // for example, if a book doi ws supplied which has no issns

                try (OutputStream out = response.getOutputStream()) {
                    String message = "Insufficient information to locate or specify a journal entry.";
                    JsonObject jsonObject = Json.createObjectBuilder()
                                                .add("error", message)
                                                .build();

                    out.write(jsonObject.toString().getBytes());
                    response.setStatus(422);
                    LOG.info(message);
                }
            }
        }
    }
}