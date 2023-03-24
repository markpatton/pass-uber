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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The SchemaInstance class represents a schema map, read from a schema URI.
 */
public class SchemaInstance implements Comparable<SchemaInstance> {

    private JsonNode schema;
    private HashMap<String, String> deps = new HashMap<String, String>();
    private HashMap<String, String> refs = new HashMap<String, String>();
    private String keyRef = "$ref";
    private String schema_name;
    private String schema_dir;
    private static final Logger logger = Logger.getLogger(SchemaInstance.class.getName());

    // all dependencies of a schema on other schemas, as well as dependencies of the
    // schemas with "greater" value than the given schema
    private static Map<String, Collection<String>> orderedDeps = new HashMap<String, Collection<String>>();

    public SchemaInstance(JsonNode schema) {
        this.schema = schema;
        String[] schema_tkns = schema.get("$id").asText().split("/");
        schema_name = schema_tkns[schema_tkns.length - 1];
        schema_dir = schema_tkns[schema_tkns.length - 3] + "/" + schema_tkns[schema_tkns.length - 2];
        findRefs(schema, "");
        findDeps();
    }

    /**
     * Sort schemas based on the following rules: If one schema is referenced by
     * another in a $ref, then that schema appears before the other For schemas that
     * are independent of one another, the one with the greatest number of form
     * properties appears before those that have fewer. If two schemas have no
     * dependencies and have the same number of properties, the one that appears
     * first in the initial list will be first in the result.
     *
     */
    @Override
    public int compareTo(SchemaInstance s) {
        // first check if this schema is referenced by schema s; if it is, then this schema should appear before s;
        // ie. less than s
        if (checkIfReferenced(s.getName(), schema_name) && !checkIfReferenced(schema_name, s.getName())) {
            return -1;
        } // vice versa
        if (checkIfReferenced(schema_name, s.getName()) && !checkIfReferenced(s.getName(), schema_name)) {
            return 1;
        }

        // for schemas independent of each other, the one with the most form properties should appear first
        int this_properties = countFormProperties();
        int s_properties = s.countFormProperties();
        if (this_properties > s_properties) {
            orderedDeps.put(s.getName(), orderedDeps.get(schema_name));
            return -1;
        } else if (this_properties < s_properties) {
            orderedDeps.put(schema_name, orderedDeps.get(s.getName()));
            return 1;
        }
        return 0;
    }

    private boolean checkIfReferenced(String referencer, String schema) {
        if (orderedDeps.get(referencer) != null) {
            for (String s : orderedDeps.get(referencer)) {
                if (s.startsWith(schema)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Counts the number of form properties in the schema. The number of properties is used to
     * determine the order of schemas in the form.
     *
     * @return int number of properties
     */
    private int countFormProperties() {
        int num_properties = 0;
        JsonNode properties = schema.at("/definitions/form/properties");
        Iterator<String> properties_iterator = properties.fieldNames();
        while (properties_iterator.hasNext()) {
            num_properties++;
            properties_iterator.next();
        }
        return num_properties;
    }

    /**
     * Finds references in this schema. Find by going through $ref tags in the
     * schema
     *
     */
    void dereference(JsonNode node, String pointer) {

        Iterator<String> it = node.fieldNames();
        it.forEachRemaining(k -> {
            JsonNode value = node.get(k);
            String path;
            String stringval;
            if (value.isValueNode()) {
                if (k.equals(keyRef)) {
                    path = pointer + "/" + k;
                    stringval = value.asText();

                    // If the reference is pointing to the current schema,
                    // replace it by the value it is pointing to
                    if (stringval.charAt(0) == '#') {
                        ((ObjectNode) schema).replace(path.split("/")[1],
                                resolveRef(stringval.split("#")[1], schema));
                    } else { // referencing another schema
                        JsonNode ext_schema = null;
                        try {
                            ext_schema = SchemaFetcher.getLocalSchema("/" + schema_dir + "/" + stringval.split("#")[0]);
                        } catch (IllegalArgumentException e) {
                            logger.log(Level.SEVERE, "Invalid Schema URI", e);
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "Failed to dereference schema", e);
                        }

                        ((ObjectNode) schema).replace(path.split("/")[1],
                                resolveRef(stringval.split("#")[1], ext_schema));
                    }
                }
            } else if (value.isObject()) {
                dereference(value, pointer + "/" + k);
            }
        });
    }

    private void findRefs(JsonNode node, String pointer) {
        Iterator<String> it = node.fieldNames();
        it.forEachRemaining(k -> {
            JsonNode value = node.get(k);
            String path;
            if (value.isValueNode()) {
                if (k.equals(keyRef)) {
                    path = pointer + "/" + k;
                    refs.put(path, value.asText());
                }
            } else if (value.isObject()) {
                findRefs(value, pointer + "/" + k);
            }
        });
    }

    /**
     * Finds dependencies of this schema on other schemas. Find by going through
     * $ref tags in the schema and adding only those that point to different schemas
     *
     */
    private void findDeps() {
        refs.forEach((path, ref) -> {
            String schema = ref.split("#")[0];
            if (!schema.equals("")) { // only add refs to other schemas; not to itself
                deps.put(path, ref);
            }
        });
        orderedDeps.put(schema_name, deps.values());
    }

    private JsonNode resolveRef(String dep, JsonNode schema) {
        return schema.at(dep);
    }

    public JsonNode getSchema() {
        return schema;
    }

    public String getName() {
        return schema_name;
    }

    public HashMap<String, String> getDeps() {
        return deps;
    }

}