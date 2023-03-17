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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.model.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SchemaServiceTest {

    private PassClient passClientMock;
    private Repository repositoryMock1;
    private Repository repositoryMock2;
    private SchemaService schemaService;

    @BeforeEach
    void setup() {
        passClientMock = Mockito.mock(PassClient.class);
        repositoryMock1 = Mockito.mock(Repository.class);
        repositoryMock2 = Mockito.mock(Repository.class);
        schemaService = new SchemaService(passClientMock);
    }

    @Test
    void getMergedSchemaTest() throws Exception {
        List<String> repository_uris = Arrays.asList("1", "2");
        when(passClientMock.getObject(Repository.class, 1L)).thenReturn(repositoryMock1);
        when(passClientMock.getObject(Repository.class, 2L)).thenReturn(repositoryMock2);

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
        JsonNode result = schemaService.getMergedSchema(repository_uris);
        assertEquals(expected, result);
    }
}
