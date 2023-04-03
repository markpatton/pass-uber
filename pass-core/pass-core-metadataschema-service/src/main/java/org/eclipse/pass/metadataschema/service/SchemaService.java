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
package org.eclipse.pass.metadataschema.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.pass.object.PassClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The SchemaService class handles the business logic of the metadata schema
 * service. It can be used to get a merged schema composed of all the schemas
 * relevant to the repositories that a PASS submission must be published to.
 */
public class SchemaService {

    @Autowired
    private PassClient passClient;

    /**
     * SchemaService constructor
     */
    public SchemaService() {
    }

    // Used in unit tests for inserting a mock client
    public SchemaService(PassClient client) {
        this.passClient = client;
    }

    /**
     * Get a merged schema composed of all the repository schemas provided as input
     *
     * @param repositoryUris List of repository URIs containing schemas to be merged
     * @return JsonSchema merged schema
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws URISyntaxException
     * @throws FetchFailException
     */
    JsonNode getMergedSchema(List<String> repository_list)
            throws MergeFailException, IllegalArgumentException, URISyntaxException, IOException {

        // Create a SchemaFetcher instance to get the schemas from the repository URIs
        SchemaFetcher f = new SchemaFetcher(passClient);
        List<JsonNode> repository_schemas;
        repository_schemas = f.getSchemas(repository_list);
        SchemaMerger m = new SchemaMerger();
        JsonNode mergedSchema;
        mergedSchema = m.mergeSchemas(repository_schemas);

        return mergedSchema;
    }

    List<JsonNode> getIndividualSchemas(List<String> repository_list)
            throws IllegalArgumentException, URISyntaxException, IOException {
        SchemaFetcher f = new SchemaFetcher(passClient);
        List<JsonNode> repository_schemas;
        repository_schemas = f.getSchemas(repository_list);
        return repository_schemas;
    }

}
