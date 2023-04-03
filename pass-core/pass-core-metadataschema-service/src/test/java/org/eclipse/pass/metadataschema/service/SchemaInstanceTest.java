/*
 * Copyright 2022 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.pass.metadataschema.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaInstanceTest {

    private ObjectMapper map;

    @BeforeEach
    void setup() {
        map = new ObjectMapper();
    }

    /*
     * Sort schemas based on the following rules: If one schema is referenced by
     * another in a $ref, then that schema appears before the other For schemas that
     * are independent of one another, the one with the greatest number of form
     * properties appears before those that have fewer. If two schemas have no
     * dependencies and have the same number of properties, the one that appears
     * first in the initial list will be first in the result.
     */
    @Test
    void testSort() throws JsonMappingException, JsonProcessingException {
        String one = "{\r\n" + "        \"$id\": \"http://example.org/schemas/one.json\",\r\n"
                + "        \"definitions\": {\r\n" + "            \"form\": {\r\n"
                + "                \"properties\": {\r\n" + "                    \"foo\": \"bar\"\r\n"
                + "                }\r\n" + "            }\r\n" + "        }\r\n" + "    }";

        String two = "{\r\n" + "        \"$id\": \"http://example.org/schemas/two.json\",\r\n"
                + "        \"definitions\": {\r\n" + "            \"form\": {\r\n"
                + "                \"properties\": {\r\n"
                + "                    \"foo\": {\"$ref\": \"one.json#/definitions/form/properties/foo\"},\r\n"
                + "                    \"bar\": \"baz\",\r\n"
                + "                    \"baz\": {\"$ref\": \"#/definitions/form/properties/bar\"}\r\n"
                + "                }\r\n" + "            }\r\n" + "        }\r\n" + "    }";

        String three = "{\r\n" + "        \"$id\": \"http://example.org/schemas/three.json\",\r\n"
                + "        \"definitions\": {\r\n" + "            \"form\": {\r\n"
                + "                \"properties\": {\r\n"
                + "                    \"foo\": {\"$ref\": \"one.json#/definitions/form/properties/foo\"},\r\n"
                + "                    \"bar\": {\"$ref\": \"two.json#/definitions/form/properties/foo\"},\r\n"
                + "                    \"baz0\": \"value0\",\r\n" + "                    \"baz\": \"value\"\r\n"
                + "                }\r\n" + "            }\r\n" + "        }\r\n" + "    }";

        String four = "{\r\n" + "        \"$id\": \"http://example.org/schemas/four.json\",\r\n"
                + "        \"definitions\": {\r\n" + "            \"form\": {\r\n"
                + "                \"properties\": {\r\n"
                + "                    \"foo2\": {\"$ref\": \"one.json#/definitions/form/properties/foo\"},\r\n"
                + "                    \"bar2\": {\"$ref\": \"two.json#/definitions/form/properties/foo\"},\r\n"
                + "                    \"baz\": \"value\"\r\n" + "                }\r\n" + "            }\r\n"
                + "        }\r\n" + "    }";

        String five = "{\r\n" + "        \"$id\": \"http://example.org/schemas/five.json\",\r\n"
                + "        \"definitions\": {\r\n" + "            \"form\": {\r\n"
                + "                \"properties\": {\r\n" + "                    \"one\": 1,\r\n"
                + "                    \"two\": 2\r\n" + "                }\r\n" + "            }\r\n" + "        }\r\n"
                + "    }";

        String six = "{\r\n" + "        \"$id\": \"http://example.org/schemas/six.json\",\r\n"
                + "        \"definitions\": {\r\n" + "            \"form\": {\r\n"
                + "                \"properties\": {\r\n" + "                    \"one\": 1\r\n"
                + "                }\r\n" + "            }\r\n" + "        }\r\n" + "    }";

        String seven = "{\r\n" + "        \"$id\": \"http://example.org/schemas/seven.json\"\r\n" + "    }";

        SchemaInstance schema_one = new SchemaInstance(map.readTree(one));
        SchemaInstance schema_two = new SchemaInstance(map.readTree(two));
        SchemaInstance schema_three = new SchemaInstance(map.readTree(three));
        SchemaInstance schema_four = new SchemaInstance(map.readTree(four));
        SchemaInstance schema_five = new SchemaInstance(map.readTree(five));
        SchemaInstance schema_six = new SchemaInstance(map.readTree(six));
        SchemaInstance schema_seven = new SchemaInstance(map.readTree(seven));

        ArrayList<SchemaInstance> toSort = new ArrayList<SchemaInstance>(Arrays.asList(schema_five, schema_two,
                schema_seven, schema_one, schema_six, schema_three, schema_four));
        ArrayList<SchemaInstance> expected = new ArrayList<SchemaInstance>(Arrays.asList(schema_one, schema_two,
                schema_three, schema_four, schema_five, schema_six, schema_seven));

        Collections.sort(toSort);
        assertEquals(toSort, expected);
    }

    @Test
    void dereferenceTest() throws JsonMappingException, JsonProcessingException {
        /*String example_schema_json = "{\r\n" + "  \"$schema\": \"http://example.org/schema_to_dereference\",\r\n"
                + "  \"$id\": \"https://example.org/example/schemas/deref\",\r\n"
                + "  \"copySchemaName\": {\"$ref\": \"#/$schema\"},\r\n"
                + "  \"title\": {\"$ref\": \"schema1.json#/x/title\"},\r\n"
                + "  \"x\": {\"$ref\": \"schema2.json#/x\"},\r\n"
                + "  \"array\": {\"$ref\": \"schema3.json#/array\"},\r\n"
                + "  \"complexarray\": {\"$ref\": \"schema4.json#/complexarray\"},\r\n"
                + "  \"k\": {\"$ref\": \"schema4.json#/h/k\"}\r\n" + "}";

        String expected = "{\r\n" + "  \"$schema\": \"http://example.org/schema_to_dereference\",\r\n"
                + "  \"$id\": \"https://example.org/example/schemas/deref\",\r\n"
                + "  \"copySchemaName\": \"http://example.org/schema_to_dereference\",\r\n" + "  \"title\": \"X\",\r\n"
                + "  \"x\": {\r\n" + "    \"title\": \"x\",\r\n" + "    \"description\": \"an awesome letter\",\r\n"
                + "    \"$comment\": \"displays nicely\",\r\n" + "    \"type\": \"letter\"\r\n" + "  },\r\n"
                + "  \"array\": [\"c\", \"d\", \"e\"],\r\n" + "  \"complexarray\": [\"e\", \"f\", {\"g\": \"h\"}],\r\n"
                + "  \"k\": [\"l\", \"m\", \"m'\"]\r\n" + "}";

        SchemaInstance testSchema = new SchemaInstance(map.readTree(example_schema_json));
        SchemaInstance expectedSchema = new SchemaInstance(map.readTree(expected));
        testSchema.dereference(testSchema.getSchema(), "");
        assertEquals(expectedSchema.getSchema(), testSchema.getSchema());*/
    }

}