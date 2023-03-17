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
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.PassClientResult;
import org.eclipse.pass.object.PassClientSelector;
import org.eclipse.pass.object.model.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetches the schemas from a list of repository URIs and creates a
 * corresponding list of SchemaInstance objects
 */
public class SchemaFetcher {

    private final PassClient passClient;
    private static final Logger LOG = LoggerFactory.getLogger(SchemaFetcher.class);
    PassClientSelector selector;

    public SchemaFetcher(PassClient client) {
        this.passClient = client;
    }

    /**
     * Get all SchemaInstance objects corresponding to the repository URIs
     *
     * @return List<SchemaInstance> ArrayList of relevant SchemaInstance objects
     * @throws IOException if the schemas cannot be fetched
     */
    List<JsonNode> getSchemas(List<String> repository_uris) throws IOException {
        List<JsonNode> schemas = new ArrayList<>();
        List<SchemaInstance> schema_instances = new ArrayList<>();

        for (String repository_uri : repository_uris) {
            List<JsonNode> repository_schemas;
            repository_schemas = getRepositorySchemas(repository_uri);
            for (JsonNode schema : repository_schemas) {
                if (!schemas.contains(schema)) {
                    schemas.add(schema);
                }
            }
        }
        // dereference each of the schemas
        for (JsonNode schema : schemas) {
            SchemaInstance s = new SchemaInstance(schema);
            s.dereference(s.getSchema(), "");
            schema_instances.add(s);
        }

        Collections.sort(schema_instances);

        schemas = new ArrayList<>();
        for (SchemaInstance s : schema_instances) {
            schemas.add(s.getSchema());
        }

        return schemas;
    }

    /**
     * Gets the Repository PASS entity at the URI and generates the corresponding
     * SchemaInstance objects
     *
     * @return List<SchemaInstance> schemas from the repository
     * @throws IOException
     */
    List<JsonNode> getRepositorySchemas(String repositoryId) throws IOException {
        //test the passclient
        Repository repo = null;
        PassClientResult<Repository> result;
        List<JsonNode> repository_schemas = new ArrayList<>();
        try {
            repo = passClient.getObject(Repository.class, Long.parseLong(repositoryId));

            List<URI> schema_uris = repo.getSchemas();
            for (URI schema_uri : schema_uris) {
                repository_schemas.add(getSchemaFromUri(schema_uri));
            }
        } catch (NullPointerException e) {
            throw new IOException("Repository not found at ID: " + repositoryId);
        }
        return repository_schemas;
    }

    /**
     * Gets the schema at the URI and creates a corresponding SchemaInstance object
     *
     * @return SchemaInstance schema at URI
     * @throws IOException if the schema cannot be fetched
     */
    JsonNode getSchemaFromUri(URI schema_uri) throws IOException {
        // Given the schema's $id url, go to the corresponding local json file
        // by loading it as a resource stream based on the last 2 parts of the $id
        // Create a SchemaInstance object from the json file and return it
        String path = schema_uri.getPath();
        String[] path_segments = path.split("/metadata-schemas");
        String path_to_schema = "/schemas" + path_segments[path_segments.length - 1];
        return getLocalSchema(path_to_schema);
    }

    public static JsonNode getLocalSchema(String path) throws IOException {
        try {
            InputStream schema_json = SchemaFetcher.class.getResourceAsStream(path);
            ObjectMapper objmapper = new ObjectMapper();
            return objmapper.readTree(schema_json);
        } catch (StreamCorruptedException | NullPointerException e) {
            throw new IOException("Schema not found at " + path, e);
        }
    }
}