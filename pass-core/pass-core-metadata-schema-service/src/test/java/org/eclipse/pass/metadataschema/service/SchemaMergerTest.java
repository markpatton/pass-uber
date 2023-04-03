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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaMergerTest {

    private SchemaMerger merger;
    private ObjectMapper map;

    @BeforeEach
    void setup() {
        map = new ObjectMapper();
    }

    // @Test
    void simpleIgnorePreamble() throws Exception {
        String schema1 = "{\r\n" + "            \"$schema\": \"http://example.org/schema\",\r\n"
                + "            \"$id\": \"http://example.org/foo\",\r\n" + "            \"title\": \"foo\",\r\n"
                + "            \"description\": \"foo schema\",\r\n" + "            \"$comment\": \"one\",\r\n"
                + "            \"a\": \"1\"\r\n" + "        }";
        String schema2 = "{\r\n" + "            \"$schema\": \"http://example.org/schema\",\r\n"
                + "            \"$id\": \"http://example.org/bar\",\r\n" + "            \"title\": \"bar\",\r\n"
                + "            \"description\": \"bar schema\",\r\n" + "            \"$comment\": \"two\",\r\n"
                + "            \"b\": \"2\"\r\n" + "        }";
        String expected_json = "{\r\n" + "            \"a\": \"1\",\r\n" + "            \"b\": \"2\"\r\n" + "        }";
        JsonNode schema_one = map.readTree(schema1);
        JsonNode schema_two = map.readTree(schema2);
        JsonNode expected = map.readTree(expected_json);

        List<JsonNode> toMerge = Arrays.asList(schema_one, schema_two);
        merger = new SchemaMerger();
        JsonNode result = merger.mergeSchemas(toMerge);
        assertEquals(result, expected);
    }

    // @Test
    void ignorableConflicts() throws Exception {
        String schema1 = "{\r\n" + "            \"a\": {\r\n" + "                \"title\": \"A\",\r\n"
                + "                \"description\": \"a letter\",\r\n"
                + "                \"$comment\": \"displays good\",\r\n" + "                \"type\": \"letter\"\r\n"
                + "            }\r\n" + "        }";
        String schema2 = "{\r\n" + "            \"a\": {\r\n" + "                \"title\": \"a\",\r\n"
                + "                \"description\": \"an awesome letter\",\r\n"
                + "                \"$comment\": \"displays nicely\",\r\n" + "                \"type\": \"letter\"\r\n"
                + "            }\r\n" + "        }";
        String expected_json = "{\r\n" + "            \"a\": {\r\n" + "                \"title\": \"a\",\r\n"
                + "                \"$comment\": \"displays nicely\",\r\n"
                + "                \"description\": \"an awesome letter\",\r\n"
                + "                \"type\": \"letter\"\r\n" + "            }\r\n" + "        }";
        JsonNode schema_one = map.readTree(schema1);
        JsonNode schema_two = map.readTree(schema2);
        JsonNode expected = map.readTree(expected_json);

        List<JsonNode> toMerge = Arrays.asList(schema_one, schema_two);
        merger = new SchemaMerger();
        JsonNode result = merger.mergeSchemas(toMerge);
        assertEquals(expected, result);
    }

    // @Test
    void simpleArrayDeduplication() throws Exception {
        String schema1 = "{\r\n" + "            \"array\": [\"a\", \"b\", \"c\"]\r\n" + "        }";
        String schema2 = "{\r\n" + "            \"array\": [\"b\", \"c\", \"d\"]\r\n" + "        }";
        String schema3 = "{\r\n" + "            \"array\": [\"c\", \"d\", \"e\"]\r\n" + "        }";
        String expected_json = "{\r\n" + "            \"array\": [\"a\", \"b\", \"c\", \"d\", \"e\"]\r\n" + "        }";
        JsonNode schema_one = map.readTree(schema1);
        JsonNode schema_two = map.readTree(schema2);
        JsonNode schema_three = map.readTree(schema3);
        JsonNode expected = map.readTree(expected_json);

        List<JsonNode> toMerge = Arrays.asList(schema_one, schema_two, schema_three);
        merger = new SchemaMerger();
        JsonNode result = merger.mergeSchemas(toMerge);
        assertEquals(expected, result);
    }

    // @Test
    void complexArrayDeduplication() throws Exception {
        String schema1 = "{\r\n" + "            \"array\": [{\"a\": [\"b\", {\"c\": \"d\"}]}, \"e\"]\r\n" + "        }";
        String schema2 = "{\r\n" + "            \"array\": [{\"a\": [\"b\", {\"c\": \"d\"}]}, \"f\"]\r\n" + "        }";
        String schema3 = "{\r\n" + "            \"array\": [\"e\", \"f\", {\"g\": \"h\"}]\r\n" + "        }";
        String expected_json = "{\r\n"
                + "            \"array\": [{\"a\": [\"b\", {\"c\": \"d\"}]}, \"e\", \"f\", {\"g\": \"h\"}]\r\n"
                + "        }";
        JsonNode schema_one = map.readTree(schema1);
        JsonNode schema_two = map.readTree(schema2);
        JsonNode schema_three = map.readTree(schema3);
        JsonNode expected = map.readTree(expected_json);

        List<JsonNode> toMerge = Arrays.asList(schema_one, schema_two, schema_three);
        merger = new SchemaMerger();
        JsonNode result = merger.mergeSchemas(toMerge);
        assertEquals(expected, result);
    }

    // @Test
    void objectMerge() throws Exception {
        String schema1 = "{\r\n" + "            \"a\": \"b\",\r\n" + "            \"c\": [\"d\", \"e\"]\r\n"
                + "        }";
        String schema2 = "{\r\n" + "            \"a\": \"b\",\r\n" + "            \"c\": [\"e\", \"f\", \"g\"]\r\n"
                + "        }";
        String schema3 = "{\r\n" + "            \"h\": {\r\n" + "                \"i\": \"j\",\r\n"
                + "                \"k\": [\"l\", \"m\"],\r\n" + "                \"n\": {\r\n"
                + "                    \"o\": \"p\"\r\n" + "                }\r\n" + "            }\r\n" + "        }";
        String schema4 = "{\r\n" + "            \"h\": {\r\n" + "                \"k\": [\"l\", \"m\", \"m'\"],\r\n"
                + "                \"n\": {\r\n" + "                    \"q\": \"r\"\r\n" + "                }\r\n"
                + "            }\r\n" + "        }";
        String expected_json = "{\r\n" + "            \"a\": \"b\",\r\n"
                + "            \"c\": [\"d\", \"e\", \"f\", \"g\"],\r\n" + "            \"h\": {\r\n"
                + "                \"i\": \"j\",\r\n" + "                \"k\": [\"l\", \"m\", \"m'\"],\r\n"
                + "                \"n\": {\r\n" + "                    \"o\": \"p\",\r\n"
                + "                    \"q\": \"r\"\r\n" + "                }\r\n" + "            }\r\n" + "        }";
        JsonNode schema_one = map.readTree(schema1);
        JsonNode schema_two = map.readTree(schema2);
        JsonNode schema_three = map.readTree(schema3);
        JsonNode schema_four = map.readTree(schema4);
        JsonNode expected = map.readTree(expected_json);

        List<JsonNode> toMerge = Arrays.asList(schema_one, schema_two, schema_three, schema_four);
        merger = new SchemaMerger();
        JsonNode result = merger.mergeSchemas(toMerge);
        assertEquals(expected, result);
    }

    // @Test
    void testMergerFull() throws Exception {
        InputStream schema1 = SchemaMergerTest.class.getResourceAsStream("/example/schemas/schema1.json");
        InputStream schema2 = SchemaMergerTest.class.getResourceAsStream("/example/schemas/schema2.json");
        InputStream schema3 = SchemaMergerTest.class.getResourceAsStream("/example/schemas/schema3.json");
        InputStream schema4 = SchemaMergerTest.class.getResourceAsStream("/example/schemas/schema4.json");
        InputStream expected_json = SchemaMergerTest.class
                .getResourceAsStream("/example/schemas/example_merged_basic.json");
        JsonNode schema_one = map.readTree(schema1);
        JsonNode schema_two = map.readTree(schema2);
        JsonNode schema_three = map.readTree(schema3);
        JsonNode schema_four = map.readTree(schema4);
        JsonNode expected = map.readTree(expected_json);

        List<JsonNode> toMerge = Arrays.asList(schema_one, schema_two, schema_three, schema_four);
        merger = new SchemaMerger();
        JsonNode result = merger.mergeSchemas(toMerge);
        assertEquals(expected, result);
    }

    @Test
    void valueNodeTypeConflictTest() throws Exception {
        String schema1 = "{\r\n" + "            \"key\": [\"l\", \"m\"]\r\n" + "        }";
        String schema2 = "{\r\n" + "            \"key\": \"keyString\"\r\n" + "        }";
        JsonNode schema_one = map.readTree(schema1);
        JsonNode schema_two = map.readTree(schema2);

        List<JsonNode> toMerge = Arrays.asList(schema_one, schema_two);
        merger = new SchemaMerger();
        Exception ex = assertThrows(MergeFailException.class, () -> {
            merger.mergeSchemas(toMerge);
        });

        String expectedMessage = "Type conflict for property 'key': ARRAY vs STRING/NUMBER";
        String actualMessage = ex.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void arrayNodeTypeConflictTest() throws Exception {
        String schema1 = "{\r\n" + "            \"key\": \"keyString\"\r\n" + "        }";
        String schema2 = "{\r\n" + "            \"key\": [\"l\", \"m\"]\r\n" + "        }";
        JsonNode schema_one = map.readTree(schema1);
        JsonNode schema_two = map.readTree(schema2);

        List<JsonNode> toMerge = Arrays.asList(schema_one, schema_two);
        merger = new SchemaMerger();
        Exception ex = assertThrows(MergeFailException.class, () -> {
            merger.mergeSchemas(toMerge);
        });

        String expectedMessage = "Type conflict for property 'key': STRING vs ARRAY";
        String actualMessage = ex.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void objectNodeTypeConflictTest() throws Exception {
        String schema1 = "{\r\n" + "            \"key\": \"keyString\"\r\n" + "        }";
        String schema2 = "{\r\n" + "            \"key\": {\"c\": \"d\"}\r\n" + "        }";
        JsonNode schema_one = map.readTree(schema1);
        JsonNode schema_two = map.readTree(schema2);

        List<JsonNode> toMerge = Arrays.asList(schema_one, schema_two);
        merger = new SchemaMerger();
        Exception ex = assertThrows(MergeFailException.class, () -> {
            merger.mergeSchemas(toMerge);
        });

        String expectedMessage = "Type conflict for property 'key': STRING vs OBJECT";
        String actualMessage = ex.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }
}
