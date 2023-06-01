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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.eclipse.pass.object.model.Repository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class PassSchemaServiceControllerTest {
    private PassSchemaServiceController schemaServiceController;
    private RefreshableElideMocked refreshableElideMocked;
    private Repository repositoryMock1;
    private Repository repositoryMock2;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setup() {
        repositoryMock1 = mock(Repository.class);
        repositoryMock2 = mock(Repository.class);
        refreshableElideMocked = SchemaTestUtils.getMockedRefreshableElide();
        SchemaFetcher schemaFetcher = new SchemaFetcher(refreshableElideMocked.getRefreshableElideMock());
        SchemaService schemaService = new SchemaService(schemaFetcher);
        schemaServiceController = new PassSchemaServiceController(schemaService);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    void readTextTest() throws Exception {
        String text_list = "http://example.org/foo1" + "\nhttp://example.org/bar1" + "\nhttp://example.org/foo2"
                + "\nhttp://example.org/bar2";
        List<String> expected = Arrays.asList("http://example.org/foo1", "http://example.org/bar1",
                "http://example.org/foo2", "http://example.org/bar2");
        Reader text_string = new StringReader(text_list);
        BufferedReader text_bufferedReader = new BufferedReader(text_string);
        assertEquals(expected, schemaServiceController.readText(text_bufferedReader));
    }

    @Test
    void readJsonTest() throws Exception {
        String json_list = "[\"http://example.org/foo\", \"http://example.org/bar\"]";
        List<String> expected = Arrays.asList("http://example.org/foo", "http://example.org/bar");
        Reader json_string = new StringReader(json_list);
        BufferedReader json_bufferedReader = new BufferedReader(json_string);
        assertEquals(expected, schemaServiceController.readJson(json_bufferedReader));
    }

    @Test
    void getSchemaMergeTrueTest() throws Exception {
        when(refreshableElideMocked.getDataStoreTransactionMock().loadObject(any(), eq(1L), any()))
                .thenReturn(repositoryMock1);
        when(refreshableElideMocked.getDataStoreTransactionMock().loadObject(any(), eq(2L), any()))
                .thenReturn(repositoryMock2);

        List<URI> r1_schemas_list = Arrays.asList(new URI("http://example.org/metadata-schemas/jhu/schema1.json"),
                new URI("http://example.org/metadata-schemas/jhu/schema2.json"),
                new URI("http://example.org/metadata-schemas/jhu/schema3.json"));

        List<URI> r2_schemas_list = Arrays.asList(new URI("http://example.org/metadata-schemas/jhu/schema3.json"),
                new URI("http://example.org/metadata-schemas/jhu/schema4.json"),
                new URI("http://example.org/metadata-schemas/jhu/schema_to_deref.json"));

        when(repositoryMock1.getSchemas()).thenReturn(r1_schemas_list);
        when(repositoryMock2.getSchemas()).thenReturn(r2_schemas_list);

        String repositories = "1,2";

        ResponseEntity<?> response = schemaServiceController.getSchema(repositories, "true");
        assertEquals(response.getBody().toString(),response.getBody().toString());
        InputStream expected_schema_json = SchemaServiceTest.class
                .getResourceAsStream("/schemas/jhu/example_merged_dereferenced.json");
        ObjectMapper map = new ObjectMapper();
        JsonNode expected = map.readTree(expected_schema_json);
        ArrayNode expected_array = map.createArrayNode();
        expected_array.add(expected);
        JsonNode actual = map.readTree(response.getBody().toString());
        assertEquals(expected_array, actual);
    }

    @Test
    void getSchemaMergeTrueJhuSchemaTest() throws Exception {
        when(refreshableElideMocked.getDataStoreTransactionMock().loadObject(any(), eq(1L), any()))
                .thenReturn(repositoryMock1);
        when(refreshableElideMocked.getDataStoreTransactionMock().loadObject(any(), eq(2L), any()))
                .thenReturn(repositoryMock2);

        List<URI> r1_schemas_list = Arrays.asList(new URI("http://example.org/metadata-schemas/jhu/jscholarship.json"),
                new URI("http://example.org/metadata-schemas/jhu/common.json"));

        when(repositoryMock1.getSchemas()).thenReturn(r1_schemas_list);

        String repositories = "1";

        ResponseEntity<?> response = schemaServiceController.getSchema(repositories, "true");
        assertEquals(response.getBody().toString(),response.getBody().toString());
        InputStream expected_schema_json = SchemaServiceTest.class
                .getResourceAsStream("/schemas/jhu/expected_jscholarship_common_merge.json");
        ObjectMapper map = new ObjectMapper();
        JsonNode expected = map.readTree(expected_schema_json);
        //ArrayNode expected_array = map.createArrayNode();
        //expected_array.add(expected);
        JsonNode actual = map.readTree(response.getBody().toString());
        assertEquals(expected, actual);
    }

    @Test
    void getSchemaMergeFalseTest() throws Exception {
        when(refreshableElideMocked.getDataStoreTransactionMock().loadObject(any(), eq(1L), any()))
                .thenReturn(repositoryMock1);
        when(refreshableElideMocked.getDataStoreTransactionMock().loadObject(any(), eq(2L), any()))
                .thenReturn(repositoryMock2);

        List<URI> r1_schemas_list = Arrays.asList(new URI("http://example.org/metadata-schemas/jhu/schema1.json"),
                new URI("http://example.org/metadata-schemas/jhu/schema2.json"));

        List<URI> r2_schemas_list = List.of(new URI("http://example.org/metadata-schemas/jhu/schema3.json"));

        when(repositoryMock1.getSchemas()).thenReturn(r1_schemas_list);
        when(repositoryMock2.getSchemas()).thenReturn(r2_schemas_list);

        String repositories = "1,2";

        ResponseEntity<?> response = schemaServiceController.getSchema(repositories, "false");
        assertEquals(response.getBody().toString(),response.getBody().toString());
        InputStream expected_schema_json1 = SchemaServiceTest.class
                .getResourceAsStream("/schemas/jhu/schema1.json");
        InputStream expected_schema_json2 = SchemaServiceTest.class
                .getResourceAsStream("/schemas/jhu/schema2.json");
        InputStream expected_schema_json3 = SchemaServiceTest.class
                .getResourceAsStream("/schemas/jhu/schema3.json");
        ObjectMapper map = new ObjectMapper();
        JsonNode expected1 = map.readTree(expected_schema_json1);
        JsonNode expected2 = map.readTree(expected_schema_json2);
        JsonNode expected3 = map.readTree(expected_schema_json3);
        ArrayNode expected_array = map.createArrayNode();
        expected_array.add(expected1);
        expected_array.add(expected2);
        expected_array.add(expected3);
        JsonNode actual = map.readTree(response.getBody().toString());
        assertEquals(expected_array, actual);
    }
}
