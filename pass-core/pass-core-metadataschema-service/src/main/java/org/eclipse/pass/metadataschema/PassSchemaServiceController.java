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
package org.eclipse.pass.metadataschema;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.elide.RefreshableElide;
import org.eclipse.pass.object.PassClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RestController implementation class PassSchemaServiceController. This class handles the web
 * request handling of POST requests from the client. It interacts with the SchemaService class, which handles
 * the business logic of retrieving, sorting, and merging the metadata schemas.
 *
 * @see SchemaService
 */
@RestController
public class PassSchemaServiceController {
    private static final Logger LOG = LoggerFactory.getLogger(PassSchemaServiceController.class);
    private final PassClient passClient;
    private SchemaService schemaService;

    @Autowired
    public PassSchemaServiceController(RefreshableElide refreshableElide) {
        this.passClient = PassClient.newInstance(refreshableElide);
        schemaService = new SchemaService(passClient);
    }

    /**
     * This constructor is used for unit testing to inject a mock client
     * @param passClient PassClient to use for unit testing
     */
    protected PassSchemaServiceController(PassClient passClient) {
        this.passClient = passClient;
        schemaService = new SchemaService(passClient);
    }

    protected List<String> readText(BufferedReader r) throws IOException {
        String next;
        List<String> repository_list = new ArrayList<>();
        while ((next = r.readLine()) != null) {
            repository_list.add(next);
        }
        return repository_list;
    }

    protected List<String> readJson(BufferedReader r) throws Exception {
        String next;
        String json_list = r.readLine();
        ObjectMapper o = new ObjectMapper();
        List<String> repository_list = o.readValue(json_list, new TypeReference<ArrayList<String>>() {
        });
        if ((next = r.readLine()) != null) {
            throw new Exception("Too many lines");
        }
        return repository_list;
    }

    /**
     * Handle POST requests by invoking the SchemaService to handle the business
     * logic of generating a merged schema from the list of relevant repository
     * schemas to a PASS submission
     *
     * @param entityIds A comma-separated list of repository entity IDs
     * @throws IOException if the request cannot be read or schema cannot be merged
     * @return a merged schema in JSON format
     */
    @GetMapping("/schemaservice")
    public ResponseEntity<?> getSchema(@RequestParam("entityIds") String entityIds) throws IOException {
        if (entityIds == null || entityIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No entityIds provided");
        }
        List<String> repository_list = Arrays.asList(entityIds.split(","));

        ObjectMapper m = new ObjectMapper();
        JsonNode mergedSchema = null;
        String jsonResponse = "";
        try {
            mergedSchema = schemaService.getMergedSchema(repository_list);
        } catch (IllegalArgumentException | IOException e) {
            LOG.error("Failed to parse schemas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to parse schemas");
        } catch (MergeFailException e) { // if the merge was unsuccessful
            List<JsonNode> individual_schemas;
            try {
                individual_schemas = schemaService.getIndividualSchemas(repository_list);
                for (int i = 0; i < individual_schemas.size(); i++) {
                    jsonResponse += m.writeValueAsString(individual_schemas.get(i));
                    if (i < individual_schemas.size() - 1) {
                        jsonResponse += ",";
                    }
                }
            } catch (IllegalArgumentException | URISyntaxException | IOException e1) {
                LOG.error("Failed to retrieve schemas", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve schemas");
            }

        }
        jsonResponse += m.writeValueAsString(mergedSchema);
        HttpHeaders headers = new HttpHeaders();
        //APPLICATION_JSON_UTF8 is deprecated and APPLICATION_JSON is preferred, will be interpreted as UTF-8
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.ok().headers(headers).body(jsonResponse);
    }

}
