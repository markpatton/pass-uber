package org.eclipse.pass.metadataschema.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.PassClientResult;
import org.eclipse.pass.object.model.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SchemaServiceTest {

    private PassClient passClientMock;
    private PassClientResult passClientResultMock;
    private Repository repositoryMock;
    private SchemaService schemaService;

    @BeforeEach
    void setup() {
        passClientMock = Mockito.mock(PassClient.class);
        repositoryMock = Mockito.mock(Repository.class);
        passClientResultMock = Mockito.mock(PassClientResult.class);
        schemaService = new SchemaService(passClientMock);
    }

    @Test
    void getMergedSchemaTest() throws Exception {
        List<String> repository_uris = Arrays.asList("repository1", "repository2");
        when(passClientMock.selectObjects(any())).thenReturn(passClientResultMock);
        when(passClientResultMock.getTotal()).thenReturn(1L);
        when(passClientResultMock.getObjects()).thenReturn(Collections.singletonList(repositoryMock));

        List<URI> r1_schemas_list = Arrays.asList(new URI("/example/schemas/schema1.json"),
                new URI("/example/schemas/schema2.json"), new URI("/example/schemas/schema3.json"),
                new URI("/example/schemas/schema4.json"), new URI("/example/schemas/schema_to_deref.json"));

        when(repositoryMock.getSchemas()).thenReturn(r1_schemas_list);

        InputStream expected_schema_json = SchemaServiceTest.class
                .getResourceAsStream("/example/schemas/example_merged_dereferenced.json");
        ObjectMapper map = new ObjectMapper();
        JsonNode expected = map.readTree(expected_schema_json);
        JsonNode result = schemaService.getMergedSchema(repository_uris);
        assertEquals(expected, result);
    }
}
