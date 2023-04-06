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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.model.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetches the schemas from a list of repository URIs and creates a corresponding list of SchemaInstance objects.
 * In order to fetch the schemas properly they need to have the following structure in the schema URL:
 * repositoryId/metadata-schemas/institution/schema_name.json. When the schemas are fetched the schemas instantiated
 * as a SchemaInstance and then dereferenced. Schemas are located in the Resources /schemas directory and grouped by
 * their respective institution folder e.g. JHU, Harvard, etc. Adding a new institution needs to be done in the schemas
 * directory.
 *
 * @see SchemaInstance
 */
public class SchemaFetcher {

    private final PassClient passClient;
    private static final Logger LOG = LoggerFactory.getLogger(SchemaFetcher.class);
    private static Map<String, CacheSchema> localSchemaCache = new HashMap<>();

    public SchemaFetcher(PassClient client) {
        this.passClient = client;
    }

    /**
     * Get all SchemaInstance objects corresponding to the repository URIs
     *
     * @return List<SchemaInstance> ArrayList of relevant SchemaInstance objects
     * @throws IOException if the schemas cannot be fetched
     */
    List<JsonNode> getSchemas(List<String> entityIds) throws IOException {
        List<JsonNode> schemas = new ArrayList<>();
        List<SchemaInstance> schema_instances = new ArrayList<>();

        for (String entityId : entityIds) {
            List<JsonNode> repository_schemas;
            repository_schemas = getRepositorySchemas(entityId);
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

        //order schema dependencies
        for (SchemaInstance s : schema_instances) {
            for (SchemaInstance k: schema_instances) {
                s.updateOrderDeps(k);
            }
        }

        //sort schemas
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
     * @throws IOException if the repository cannot be found.
     */
    List<JsonNode> getRepositorySchemas(String entityId) throws IOException {
        Repository repo;
        List<JsonNode> repository_schemas = new ArrayList<>();
        try {
            repo = passClient.getObject(Repository.class, Long.parseLong(entityId));

            List<URI> schema_uris = repo.getSchemas();
            for (URI schema_uri : schema_uris) {
                repository_schemas.add(getSchemaFromUri(schema_uri));
            }
        } catch (NullPointerException e) {
            LOG.error("Repository not found at ID: " + entityId, e);
            throw new IOException("Repository not found at ID: " + entityId);
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
        ObjectMapper objmapper = new ObjectMapper();
        URL localSchemaURL = SchemaFetcher.class.getResource(path);
        if (localSchemaURL == null) {
            LOG.error("Schema not found at " + path);
            throw new IOException("Schema not found at " + path);
        }

        File schemaFile = new File(localSchemaURL.getFile());
        long lastModified = schemaFile.lastModified();

        if (localSchemaCache.containsKey(path)) {
            CacheSchema cacheSchema = localSchemaCache.get(path);
            if (cacheSchema.getLastModified() == lastModified) {
                return cacheSchema.getSchema().deepCopy();
            }
        }

        try {
            InputStream schema_json = SchemaFetcher.class.getResourceAsStream(path);
            JsonNode schema = objmapper.readTree(schema_json);
            localSchemaCache.put(path, new CacheSchema(schema.deepCopy(), lastModified));
            return schema.deepCopy();
        } catch (StreamCorruptedException | NullPointerException e) {
            LOG.error("Schema not found at " + path, e);
            throw new IOException("Schema not found at " + path, e);
        }
    }
}