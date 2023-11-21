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

import static org.eclipse.pass.metadataschema.SchemaTestUtils.RefreshableElideMocked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.pass.object.model.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SchemaServiceTest {

    private RefreshableElideMocked refreshableElideMocked;
    private Repository repositoryMock1;
    private Repository repositoryMock2;
    private SchemaService schemaService;
    private ObjectMapper map;

    @BeforeEach
    void setup() {
        repositoryMock1 = Mockito.mock(Repository.class);
        repositoryMock2 = Mockito.mock(Repository.class);
        refreshableElideMocked = SchemaTestUtils.getMockedRefreshableElide();
        SchemaFetcher schemaFetcher = new SchemaFetcher(refreshableElideMocked.getRefreshableElideMock());
        schemaService = new SchemaService(schemaFetcher);
        map = new ObjectMapper();
    }

    @Test
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
        JsonNode result = schemaService.mergeSchemas(toMerge);
        assertEquals(result, expected);
    }

    @Test
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
        JsonNode result = schemaService.mergeSchemas(toMerge);
        assertEquals(expected, result);
    }

    @Test
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
        JsonNode result = schemaService.mergeSchemas(toMerge);
        assertEquals(expected, result);
    }

    @Test
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
        JsonNode result = schemaService.mergeSchemas(toMerge);
        assertEquals(expected, result);
    }

    @Test
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
        JsonNode result = schemaService.mergeSchemas(toMerge);
        assertEquals(expected, result);
    }

    @Test
    void testMergerFull() throws Exception {
        InputStream schema1 = SchemaServiceTest.class.getResourceAsStream("/schemas/jhu/schema1.json");
        InputStream schema2 = SchemaServiceTest.class.getResourceAsStream("/schemas/jhu/schema2.json");
        InputStream schema3 = SchemaServiceTest.class.getResourceAsStream("/schemas/jhu/schema3.json");
        InputStream schema4 = SchemaServiceTest.class.getResourceAsStream("/schemas/jhu/schema4.json");
        InputStream expected_json = SchemaServiceTest.class
                .getResourceAsStream("/schemas/jhu/example_merged_basic.json");
        JsonNode schema_one = map.readTree(schema1);
        JsonNode schema_two = map.readTree(schema2);
        JsonNode schema_three = map.readTree(schema3);
        JsonNode schema_four = map.readTree(schema4);
        JsonNode expected = map.readTree(expected_json);

        List<JsonNode> toMerge = Arrays.asList(schema_one, schema_two, schema_three, schema_four);
        JsonNode result = schemaService.mergeSchemas(toMerge);
        assertEquals(expected, result);
    }

    @Test
    void valueNodeTypeConflictTest() throws Exception {
        String schema1 = "{\r\n" + "            \"key\": [\"l\", \"m\"]\r\n" + "        }";
        String schema2 = "{\r\n" + "            \"key\": \"keyString\"\r\n" + "        }";
        JsonNode schema_one = map.readTree(schema1);
        JsonNode schema_two = map.readTree(schema2);

        List<JsonNode> toMerge = Arrays.asList(schema_one, schema_two);
        Exception ex = assertThrows(MergeFailException.class, () -> schemaService.mergeSchemas(toMerge));

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
        Exception ex = assertThrows(MergeFailException.class, () -> schemaService.mergeSchemas(toMerge));

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
        Exception ex = assertThrows(MergeFailException.class, () -> schemaService.mergeSchemas(toMerge));

        String expectedMessage = "Type conflict for property 'key': STRING vs OBJECT";
        String actualMessage = ex.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void getMergedSchemaTest() throws Exception {
        List<String> repositoryIds = Arrays.asList("1", "2");
        when(refreshableElideMocked.getDataStoreTransactionMock().loadObject(any(), eq(1L), any()))
                .thenReturn(repositoryMock1);
        when(refreshableElideMocked.getDataStoreTransactionMock().loadObject(any(), eq(2L), any()))
                .thenReturn(repositoryMock2);

        List<URI> r1_schemas_list = Arrays.asList(new URI("https://example.com/metadata-schemas/jhu/schema1.json"),
                new URI("https://example.com/metadata-schemas/jhu/schema2.json"),
                new URI("https://example.com/metadata-schemas/jhu/schema3.json"));

        List<URI> r2_schemas_list = Arrays.asList(new URI("https://example.com/metadata-schemas/jhu/schema3.json"),
                new URI("https://example.com/metadata-schemas/jhu/schema4.json"),
                new URI("https://example.com/metadata-schemas/jhu/schema_to_deref.json"));

        when(repositoryMock1.getSchemas()).thenReturn(r1_schemas_list);
        when(repositoryMock2.getSchemas()).thenReturn(r2_schemas_list);

        InputStream expected_schema_json = SchemaServiceTest.class
                .getResourceAsStream("/schemas/jhu/example_merged_dereferenced.json");
        ObjectMapper map = new ObjectMapper();
        JsonNode expected = map.readTree(expected_schema_json);
        JsonNode result = schemaService.getMergedSchema(repositoryIds);
        assertEquals(expected, result);
    }

    @Test
    void getMergeJscholarSchemaTest() throws Exception {
        List<String> repositoryIds = List.of("1");
        when(refreshableElideMocked.getDataStoreTransactionMock().loadObject(any(), eq(1L), any()))
                .thenReturn(repositoryMock1);

        List<URI> r1_schemas_list = List.of(new URI("https://example.com/metadata-schemas/jhu/jscholarship.json"));

        when(repositoryMock1.getSchemas()).thenReturn(r1_schemas_list);

        InputStream expected_schema_json = SchemaServiceTest.class
                .getResourceAsStream("/schemas/jhu/jscholarship_merge_deref.json");
        ObjectMapper map = new ObjectMapper();
        JsonNode expected = map.readTree(expected_schema_json);
        JsonNode result = schemaService.getMergedSchema(repositoryIds);
        assertEquals(expected, result);
    }
}
