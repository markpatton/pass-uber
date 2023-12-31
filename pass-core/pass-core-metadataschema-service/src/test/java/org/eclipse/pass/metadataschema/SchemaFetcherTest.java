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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.pass.object.model.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SchemaFetcherTest {

    private RefreshableElideMocked refreshableElideMocked;
    private Repository repositoryMock1;
    private Repository repositoryMock2;
    private SchemaFetcher schemaFetcher;
    private ObjectMapper map;

    @BeforeEach
    void setup() {
        repositoryMock1 = mock(Repository.class);
        repositoryMock2 = mock(Repository.class);
        refreshableElideMocked = SchemaTestUtils.getMockedRefreshableElide();
        schemaFetcher = new SchemaFetcher(refreshableElideMocked.getRefreshableElideMock());
        map = new ObjectMapper();
    }

    @Test
    void getSchemaFromUriTest() throws Exception {
        URI uri = new URI("https://example.com/metadata-schemas/jhu/schema1.json");
        String expectedJsonString = "{\r\n" + "    \"$schema\": \"http://example.org/metadata-schemas/schemas/jhu\",\r\n"
                + "    \"$id\": \"http://example.org/metadata-schemas/schemas/jhu/foo\",\r\n" + "    \"title\": \"foo\",\r\n"
                + "    \"description\": \"foo schema\",\r\n" + "    \"$comment\": \"one\",\r\n"
                + "    \"a\": \"1\",\r\n" + "    \"x\": {\r\n" + "        \"title\": \"X\",\r\n"
                + "        \"description\": \"a letter\",\r\n" + "        \"$comment\": \"displays good\",\r\n"
                + "        \"type\": \"letter\"\r\n" + "    },\r\n" + "    \"array\": [\"a\", \"b\", \"c\"],\r\n"
                + "    \"aa\": \"b\",\r\n" + "    \"cc\": [\"d\", \"e\"]\r\n" + "}";
        JsonNode expected = map.readTree(expectedJsonString);
        assertEquals(expected, schemaFetcher.getSchemaFromUri(uri));
    }

    @Test
    void getRepositorySchemasTest() throws Exception {
        when(refreshableElideMocked.getDataStoreTransactionMock().loadObject(any(), anyLong(), any()))
                .thenReturn(repositoryMock1);

        List<URI> r1_schemas_list = Arrays.asList(new URI("https://example.com/metadata-schemas/jhu/schema1.json"),
                new URI("https://example.com/metadata-schemas/jhu/schema2.json"));

        when(repositoryMock1.getSchemas()).thenReturn(r1_schemas_list);

        String expectedJsonSchema1 = "{\r\n" + "    \"$schema\": \"http://example.org/metadata-schemas/schemas/jhu\",\r\n"
                + "    \"$id\": \"http://example.org/metadata-schemas/schemas/jhu/foo\",\r\n" + "    \"title\": \"foo\",\r\n"
                + "    \"description\": \"foo schema\",\r\n" + "    \"$comment\": \"one\",\r\n"
                + "    \"a\": \"1\",\r\n" + "    \"x\": {\r\n" + "        \"title\": \"X\",\r\n"
                + "        \"description\": \"a letter\",\r\n" + "        \"$comment\": \"displays good\",\r\n"
                + "        \"type\": \"letter\"\r\n" + "    },\r\n" + "    \"array\": [\"a\", \"b\", \"c\"],\r\n"
                + "    \"aa\": \"b\",\r\n" + "    \"cc\": [\"d\", \"e\"]\r\n" + "}";

        String expectedJsonSchema2 = "{\r\n" + "    \"$schema\": \"http://example.org/metadata-schemas/schemas/jhu\",\r\n"
                + "    \"$id\": \"http://example.org/metadata-schemas/schemas/jhu/schema2\",\r\n" + "    \"title\": \"bar\",\r\n"
                + "    \"description\": \"bar schema\",\r\n" + "    \"$comment\": \"two\",\r\n"
                + "    \"b\": \"2\",\r\n" + "    \"x\": {\r\n" + "        \"title\": \"x\",\r\n"
                + "        \"description\": \"an awesome letter\",\r\n"
                + "        \"$comment\": \"displays nicely\",\r\n" + "        \"type\": \"letter\"\r\n" + "    },\r\n"
                + "    \"array\": [\"b\", \"c\", \"d\"],\r\n"
                + "    \"complexarray\": [{\"a\": [\"b\", {\"c\": \"d\"}]}, \"e\"],\r\n" + "    \"aa\": \"b\",\r\n"
                + "    \"cc\": [\"e\", \"f\", \"g\"]\r\n" + "}";

        JsonNode expectedschema1 = map.readTree(expectedJsonSchema1);
        JsonNode expectedschema2 = map.readTree(expectedJsonSchema2);
        List<JsonNode> expected = new ArrayList<>(Arrays.asList(expectedschema1, expectedschema2));
        List<JsonNode> result = schemaFetcher.getRepositorySchemas("1");
        assertEquals(expected, result);
    }

    @Test
    void getSchemasTest() throws Exception {
        List<String> repositoryIds = new ArrayList<>(Arrays.asList("1", "2"));

        when(refreshableElideMocked.getDataStoreTransactionMock().loadObject(any(), eq(1L), any()))
                .thenReturn(repositoryMock1);
        when(refreshableElideMocked.getDataStoreTransactionMock().loadObject(any(), eq(2L), any()))
                .thenReturn(repositoryMock2);

        List<URI> r1_schemas_list = Arrays.asList(new URI("https://example.com/metadata-schemas/jhu/schema1.json"),
                new URI("https://example.com/metadata-schemas/jhu/schema2.json"));

        List<URI> r2_schemas_list = Arrays.asList(new URI("https://example.com/metadata-schemas/jhu/schema2.json"),
                new URI("https://example.com/metadata-schemas/jhu/schema3.json"),
                new URI("https://example.com/metadata-schemas/jhu/schema_to_deref.json"));

        when(repositoryMock1.getSchemas()).thenReturn(r1_schemas_list);
        when(repositoryMock2.getSchemas()).thenReturn(r2_schemas_list);

        InputStream expectedJsonSchemaInput1 = SchemaFetcherTest.class
                .getResourceAsStream("/schemas/jhu/schema1.json");

        InputStream expectedJsonSchemaInput2 = SchemaFetcherTest.class
                .getResourceAsStream("/schemas/jhu/schema2.json");

        InputStream expectedJsonSchemaInput3 = SchemaFetcherTest.class
                .getResourceAsStream("/schemas/jhu/schema3.json");

        // dereferenced version of schema_to_deref.json
        InputStream expectedJsonSchemaInput4 = SchemaFetcherTest.class
                .getResourceAsStream("/schemas/jhu/schema_to_deref_expected.json");

        JsonNode expectedschema1 = map.readTree(expectedJsonSchemaInput1);
        JsonNode expectedschema2 = map.readTree(expectedJsonSchemaInput2);
        JsonNode expectedschema3 = map.readTree(expectedJsonSchemaInput3);
        JsonNode expectedschema4 = map.readTree(expectedJsonSchemaInput4);
        List<JsonNode> expected = new ArrayList<>(
                Arrays.asList(expectedschema1, expectedschema2, expectedschema3, expectedschema4));
        List<JsonNode> result = schemaFetcher.getSchemas(repositoryIds);
        assertEquals(expected, result);
    }

    @Test
    void invalidSchemaUriTest() throws Exception {
        when(refreshableElideMocked.getDataStoreTransactionMock().loadObject(any(), eq(1L), Mockito.any()))
                .thenReturn(repositoryMock1);
        List<URI> r1_schemas_list = Arrays.asList(new URI("https://example.com/metadata-schemas/jhu/invalidschema.json"),
                new URI("https://example.com/metadata-schemas/jhu/schema2.json"));
        when(repositoryMock1.getSchemas()).thenReturn(r1_schemas_list);
        assertThrows(IllegalArgumentException.class, () ->
                schemaFetcher.getRepositorySchemas("repository1"));
    }

}
