package org.eclipse.pass.metadataschema.service;
/*
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dataconservancy.pass.client.PassClient;
import org.dataconservancy.pass.model.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;*/

class SchemaServiceTest {

    /*private PassClient passClientMock;
    private Repository repository1mock;
    private Repository repository2mock;
    private SchemaService s;

    @BeforeEach
    void setup() {
        passClientMock = Mockito.mock(PassClient.class);
        repository1mock = Mockito.mock(Repository.class);
        repository2mock = Mockito.mock(Repository.class);
    }

    @Test
    void getMergedSchemaTest() throws Exception {
        List<String> repository_uris = Arrays.asList("repository1", "repository2");
        s = new SchemaService(passClientMock);
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

        InputStream expected_schema_json = SchemaServiceTest.class
                .getResourceAsStream("/example/schemas/example_merged_dereferenced.json");
        ObjectMapper map = new ObjectMapper();
        JsonNode expected = map.readTree(expected_schema_json);
        JsonNode result = s.getMergedSchema(repository_uris);
        assertEquals(expected, result);
    }*/
}
