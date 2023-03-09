package org.eclipse.pass.metadataschema.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.PassClientResult;
import org.eclipse.pass.object.model.Repository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

class SchemaControllerTest {

    private PassSchemaServiceController schemaServiceController;
    private PassClient passClientMock;
    private PassClientResult passClientResultMock;
    private Repository repositoryMock;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setup() {
        passClientMock = Mockito.mock(PassClient.class);
        passClientResultMock = Mockito.mock(PassClientResult.class);
        repositoryMock = Mockito.mock(Repository.class);
        schemaServiceController = new PassSchemaServiceController();
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
        PassSchemaServiceController s = new PassSchemaServiceController(passClientMock);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(passClientMock.selectObjects(any())).thenReturn(passClientResultMock);
        when(passClientResultMock.getTotal()).thenReturn(1L);
        when(passClientResultMock.getObjects()).thenReturn(Collections.singletonList(repositoryMock));

        List<URI> r1_schemas_list = Arrays.asList(new URI("/example/schemas/schema1.json"),
                new URI("/example/schemas/schema2.json"), new URI("/example/schemas/schema3.json"),
                new URI("/example/schemas/schema4.json"), new URI("/example/schemas/schema_to_deref.json"));

        when(repositoryMock.getSchemas()).thenReturn(r1_schemas_list);

        String repositories = "[\"repository1\", \"repository2\"]";
        ByteArrayInputStream stream = new ByteArrayInputStream(repositories.getBytes(StandardCharsets.UTF_8));
        ServletInputStream servletstream = createServletInputStream(stream);

        when(request.getInputStream()).thenReturn(servletstream);

        ResponseEntity response = s.getSchema(repositories, request);
        InputStream expected_schema_json = SchemaServiceTest.class
                .getResourceAsStream("/example/schemas/example_merged_dereferenced.json");
        ObjectMapper map = new ObjectMapper();
        JsonNode expected = map.readTree(expected_schema_json);
        JsonNode actual = map.readTree(response.getBody().toString());
        String respHeader = response.getHeaders().getAccept().toString();

        //assertEquals("application/json, text/plain", respHeader);
        assertEquals(expected, actual);
    }
}
