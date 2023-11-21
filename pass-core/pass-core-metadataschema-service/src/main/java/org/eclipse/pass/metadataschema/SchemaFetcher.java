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

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.elide.RefreshableElide;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.model.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
@Component
public class SchemaFetcher {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaFetcher.class);

    private final RefreshableElide refreshableElide;
    private final ConcurrentHashMap<String, JsonNode> localSchemaCache = new ConcurrentHashMap<>();

    /**
     * Constructor for SchemaFetcher
     * @param refreshableElide A refreshable Elide instance
     */
    public SchemaFetcher(RefreshableElide refreshableElide) {
        this.refreshableElide = refreshableElide;
    }

    /**
     * Get all SchemaInstance objects corresponding to the repository URIs
     *
     * @param entityIds a list of entity IDs
     * @return an ArrayList of relevant JsonNode objects
     * @throws IOException if the schemas cannot be fetched
     */
    public List<JsonNode> getSchemas(List<String> entityIds) throws IOException {
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

        //order schema dependencies
        for (SchemaInstance s : schema_instances) {
            for (SchemaInstance k: schema_instances) {
                s.updateOrderDeps(k);
            }
        }

        // dereference each of the schemas - only perform after ordering dependencies
        for (JsonNode schema : schemas) {
            SchemaInstance s = new SchemaInstance(schema);
            s.dereference(s.getSchema(), this);
            schema_instances.add(s);
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
     * @param entityId the repository ID
     * @return an Arraylist of schemas from the repository
     * @throws IOException if the repository cannot be found.
     */
    public List<JsonNode> getRepositorySchemas(String entityId) throws IOException {
        List<JsonNode> repository_schemas = new ArrayList<>();
        try (PassClient passClient = PassClient.newInstance(refreshableElide)) {
            Repository repo = passClient.getObject(Repository.class, Long.parseLong(entityId));
            if (Objects.isNull(repo)) {
                throw new IOException("Repository not found at ID: " + entityId);
            }
            List<URI> schema_uris = repo.getSchemas();
            for (URI schema_uri : schema_uris) {
                repository_schemas.add(getSchemaFromUri(schema_uri));
            }
        }
        return repository_schemas;
    }

    /**
     * Gets the schema at the URI and creates a corresponding SchemaInstance object
     *
     * @param schemaUri URI of the schema
     * @return SchemaInstance schema at URI
     * @throws IOException if the schema cannot be fetched
     */
    public JsonNode getSchemaFromUri(URI schemaUri) throws IOException {
        // Given the schema's $id url, go to the corresponding local json file
        // by loading it as a resource stream based on the last 2 parts of the $id
        // Create a SchemaInstance object from the json file and return it
        String path = schemaUri.getPath();
        String[] path_segments = path.split("/metadata-schemas");
        String path_to_schema = "/schemas" + path_segments[path_segments.length - 1];
        return getLocalSchema(path_to_schema);
    }

    /**
     * Get the local schema from the path. If the schema is already in the cache, return the cached schema.
     * Otherwise, read the schema from the path and add it to the cache.
     *
     * @param path the path to the local schema
     * @return the local schema
     * @throws IOException if the schema cannot be found or is corrupted
     */
    public JsonNode getLocalSchema(String path) throws IOException {
        ObjectMapper objmapper = new ObjectMapper();
        if (localSchemaCache.containsKey(path)) {
            JsonNode cacheSchema = localSchemaCache.get(path);
            return cacheSchema.deepCopy();
        }

        try {
            InputStream schema_json = SchemaFetcher.class.getResourceAsStream(path);
            JsonNode schema = objmapper.readTree(schema_json);
            localSchemaCache.put(path, schema.deepCopy());
            return schema.deepCopy();
        } catch (StreamCorruptedException | NullPointerException e) {
            LOG.error("Schema not found at " + path, e);
            throw new IOException("Schema not found at " + path, e);
        }
    }
}