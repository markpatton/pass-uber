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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

/**
 * The SchemaService class handles the business logic of the metadata schema
 * service. It can be used to get a merged schema composed of all the schemas
 * relevant to the repositories that a PASS submission must be published to.
 */
@Service
public class SchemaService {

    private final SchemaFetcher schemaFetcher;

    /**
     * Constructor for SchemaService
     * @param schemaFetcher the component responsible for reading schemas
     */
    public SchemaService(SchemaFetcher schemaFetcher) {
        this.schemaFetcher = schemaFetcher;
    }

    /**
     * Get a merged schema composed of all the repository schemas provided as input. This uses the SchemaMerger
     * class to merge the schemas into a single JSON schema. The schemas are fetched from the repository URIs,
     * using the SchemaFetcher class, and then merged using the SchemaMerger class.
     *
     * @param repository_list List of repository URIs containing schemas to be merged
     * @return JsonSchema merged schema
     * @throws MergeFailException, if the schemas cannot be merged
     * @throws IOException, if the schemas cannot be fetched
     */
    JsonNode getMergedSchema(List<String> repository_list) throws MergeFailException, IOException {
        List<JsonNode> repository_schemas = schemaFetcher.getSchemas(repository_list);
        return mergeSchemas(repository_schemas);
    }

    List<JsonNode> getIndividualSchemas(List<String> repository_list)
            throws IllegalArgumentException, URISyntaxException, IOException {
        return schemaFetcher.getSchemas(repository_list);
    }

    /**
     * Merges a list of SchemaInstance object into a single one. Field:value pairs
     * should only be added if not already present
     *
     * @param schemasToMerge list of schemas to be merged
     * @return SchemaInstance of merged schemas
     */
    JsonNode mergeSchemas(List<JsonNode> schemasToMerge) {
        ObjectNode merged = new ObjectMapper().createObjectNode();
        Set<String> ignorable =
                new HashSet<>(Arrays.asList("title", "description", "$id", "$schema", "$comment"));
        for (JsonNode schema : schemasToMerge) {
            Iterator<String> fieldnames = schema.fieldNames();
            fieldnames.forEachRemaining(f -> {
                if (!ignorable.contains(f)) {
                    mergeIn(f, schema.get(f), merged);
                }
            });
        }
        return merged;
    }

    /**
     * Merges a field:value pair into the schema If there is already a value in this
     * field, it should be the same type, else error.
     *
     * @throws MergeFailException, if the field:value pair cannot be merged
     */
    private void mergeIn(String fieldName, JsonNode value, ObjectNode schema) throws MergeFailException {

        // if this value is not already in the merged schema, add it
        if (schema.get(fieldName) == null) {
            schema.set(fieldName, value);
            return;
        }

        // the field already exists, so check for type agreement
        if (value.isValueNode() && !value.isArray()) {
            if (!schema.get(fieldName).isValueNode()) {
                throw new MergeFailException("Type conflict for property '" + fieldName + "': "
                        + schema.get(fieldName).getNodeType() + " vs STRING/NUMBER");
            }
            schema.set(fieldName, value);
            return;
        }
        if (value.isArray()) {
            if (!schema.get(fieldName).isArray()) {
                throw new MergeFailException("Type conflict for property '" + fieldName + "': "
                        + schema.get(fieldName).getNodeType() + " vs ARRAY");
            }
            // iterate through array and add elements that are not already in merged schema
            for (JsonNode element : value) {
                Iterator<JsonNode> existing_elements = schema.get(fieldName).elements();
                boolean hasElement = false;
                while (existing_elements.hasNext()) {
                    if (existing_elements.next().equals(element)) {
                        hasElement = true;
                    }
                }
                if (!hasElement) {
                    ((ArrayNode) (schema.get(fieldName))).add(element);
                }
            }
            return;
        }
        if (value.isObject()) {
            if (!schema.get(fieldName).isObject()) {
                throw new MergeFailException("Type conflict for property '" + fieldName + "': "
                        + schema.get(fieldName).getNodeType() + " vs OBJECT");
            }
            Iterator<String> fieldnames = value.fieldNames();
            fieldnames.forEachRemaining(f -> mergeIn(f, value.get(f), (ObjectNode) schema.get(fieldName)));
        }
    }
}
