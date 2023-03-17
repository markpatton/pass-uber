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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.model.Repository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

class SchemaControllerTest {
    private static final Logger LOG = LoggerFactory.getLogger(PassSchemaServiceController.class);
    private PassSchemaServiceController schemaServiceController;
    private PassClient passClientMock;
    private Repository repositoryMock1;
    private Repository repositoryMock2;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setup() {
        passClientMock = Mockito.mock(PassClient.class);
        repositoryMock1 = Mockito.mock(Repository.class);
        repositoryMock2 = Mockito.mock(Repository.class);
        schemaServiceController = new PassSchemaServiceController(passClientMock);
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
        List<String> expected = Arrays.asList(new String[] { "http://example.org/foo1", "http://example.org/bar1",
            "http://example.org/foo2", "http://example.org/bar2" });
        Reader text_string = new StringReader(text_list);
        BufferedReader text_bufferedReader = new BufferedReader(text_string);
        assertEquals(expected, schemaServiceController.readText(text_bufferedReader));
    }

    @Test
    void readJsonTest() throws Exception {
        String json_list = "[\"http://example.org/foo\", \"http://example.org/bar\"]";
        List<String> expected = Arrays.asList(new String[] { "http://example.org/foo", "http://example.org/bar" });
        Reader json_string = new StringReader(json_list);
        BufferedReader json_bufferedReader = new BufferedReader(json_string);
        assertEquals(expected, schemaServiceController.readJson(json_bufferedReader));
    }

    private ServletInputStream createServletInputStream(ByteArrayInputStream in) {
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() {
                return in.read();
            }
        };
    }

    @Test
    void getSchemaTest() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(passClientMock.getObject(Repository.class, 1L)).thenReturn(repositoryMock1);
        when(passClientMock.getObject(Repository.class, 2L)).thenReturn(repositoryMock2);

        List<URI> r1_schemas_list = Arrays.asList(new URI("http://example.org/metadata-schemas/jhu/schema1.json"),
                new URI("http://example.org/metadata-schemas/jhu/schema2.json"),
                new URI("http://example.org/metadata-schemas/jhu/schema3.json"));

        List<URI> r2_schemas_list = Arrays.asList(new URI("http://example.org/metadata-schemas/jhu/schema3.json"),
                new URI("http://example.org/metadata-schemas/jhu/schema4.json"),
                new URI("http://example.org/metadata-schemas/jhu/schema_to_deref.json"));

        when(repositoryMock1.getSchemas()).thenReturn(r1_schemas_list);
        when(repositoryMock2.getSchemas()).thenReturn(r2_schemas_list);

        String repositories = "[\"1\", \"2\"]";
        ByteArrayInputStream stream = new ByteArrayInputStream(repositories.getBytes(StandardCharsets.UTF_8));
        ServletInputStream servletstream = createServletInputStream(stream);

        when(request.getInputStream()).thenReturn(servletstream);
        when(request.getContentType()).thenReturn("application/json");

        ResponseEntity response = schemaServiceController.getSchema(request);
        assertEquals(response.getBody().toString(),response.getBody().toString());
        InputStream expected_schema_json = SchemaServiceTest.class
                .getResourceAsStream("/schemas/jhu/example_merged_dereferenced.json");
        ObjectMapper map = new ObjectMapper();
        JsonNode expected = map.readTree(expected_schema_json);
        JsonNode actual = map.readTree(response.getBody().toString());
        assertEquals(expected, actual);
    }
}
