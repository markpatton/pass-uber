package org.eclipse.pass.metadataschema.service;

/*import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.pass.object.PassClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;*/

class SchemaControllerTest {

    /*private PassSchemaServiceController s;
    private PassClient passClientMock;
    private Repository repository1mock;
    private Repository repository2mock;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setup() {
        passClientMock = Mockito.mock(PassClient.class);
        repository1mock = Mockito.mock(Repository.class);
        repository2mock = Mockito.mock(Repository.class);
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
        s = new PassSchemaServiceController();
        assertEquals(expected, s.readText(text_bufferedReader));
    }

    @Test
    void readJsonTest() throws Exception {
        String json_list = "[\"http://example.org/foo\", \"http://example.org/bar\"]";
        List<String> expected = Arrays.asList(new String[] { "http://example.org/foo", "http://example.org/bar" });
        Reader json_string = new StringReader(json_list);
        BufferedReader json_bufferedReader = new BufferedReader(json_string);
        s = new PassSchemaServiceController();
        assertEquals(expected, s.readJson(json_bufferedReader));
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
            public int read() throws IOException {
                return in.read();
            }
        };
    }

    @Test
    void doPostTest() throws Exception {
        PassSchemaServiceController s = new PassSchemaServiceController(passClientMock);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(passClientMock.findByAttribute(Repository.class, "@id", new URI("repository1")))
                .thenReturn(new URI("uri_to_repository1"));
        when(passClientMock.findByAttribute(Repository.class, "@id", new URI("repository2")))
                .thenReturn(new URI("uri_to_repository2"));
        when(passClientMock.readResource(new URI("uri_to_repository1"), Repository.class)).thenReturn(repository1mock);
        when(passClientMock.readResource(new URI("uri_to_repository2"), Repository.class)).thenReturn(repository2mock);
        List<URI> r1_schemas_list = Arrays.asList(new URI("/example/schemas/schema1.json"),
                new URI("/example/schemas/schema2.json"), new URI("/example/schemas/schema3.json"));
        List<URI> r2_schemas_list = Arrays.asList(new URI("/example/schemas/schema3.json"),
                new URI("/example/schemas/schema4.json"), new URI("/example/schemas/schema_to_deref.json"));
        when(repository1mock.getSchemas()).thenReturn(r1_schemas_list);
        when(repository2mock.getSchemas()).thenReturn(r2_schemas_list);

        String repositories = "[\"repository1\", \"repository2\"]";
        ByteArrayInputStream stream = new ByteArrayInputStream(repositories.getBytes(Charset.forName("UTF-8")));
        ServletInputStream servletstream = createServletInputStream(stream);
        PrintWriter printwriter = new PrintWriter(System.out);

        when(response.getHeader("Accept-Post")).thenReturn("application/json, text/plain");
        when(response.getHeader("Server")).thenReturn("PASS schema service");
        when(request.getInputStream()).thenReturn(servletstream);
        when(response.getWriter()).thenReturn(printwriter);

        s.doPost(request, response);
        InputStream expected_schema_json = SchemaServiceTest.class
                .getResourceAsStream("/example/schemas/example_merged_dereferenced.json");
        ObjectMapper map = new ObjectMapper();
        JsonNode expected = map.readTree(expected_schema_json);
        JsonNode actual = map.readTree(outputStreamCaptor.toString());

        assertEquals("application/json, text/plain", response.getHeader("Accept-Post"));
        assertEquals("PASS schema service", response.getHeader("Server"));
        assertEquals(expected, actual);
    }*/
}
