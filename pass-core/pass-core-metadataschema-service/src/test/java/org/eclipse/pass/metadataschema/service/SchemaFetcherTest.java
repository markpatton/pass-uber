package org.eclipse.pass.metadataschema.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.PassClientResult;
import org.eclipse.pass.object.PassClientSelector;
import org.eclipse.pass.object.model.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SchemaFetcherTest {

    private PassClient passClientMock;
    private Repository repositoryMock;
    private PassClientResult passClientResultMock;
    private SchemaFetcher schemaFetcher;
    private ObjectMapper map;

    @BeforeEach
    void setup() {
        passClientMock = Mockito.mock(PassClient.class);
        repositoryMock = Mockito.mock(Repository.class);
        passClientResultMock = Mockito.mock(PassClientResult.class);
        schemaFetcher = new SchemaFetcher(passClientMock);
        map = new ObjectMapper();
    }

    @Test
    void getSchemaFromUriTest() throws Exception {
        URI uri = new URI("https://example.com/pass-metadata-schemas/example/schemas/schema1.json");
        String expectedJsonString = "{\r\n" + "    \"$schema\": \"http://example.org/example/schemas/schema\",\r\n"
                + "    \"$id\": \"http://example.org/example/schemas/foo\",\r\n" + "    \"title\": \"foo\",\r\n"
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
        when(passClientMock.selectObjects(Mockito.any(PassClientSelector.class))).thenReturn(passClientResultMock);
        when(passClientResultMock.getObjects()).thenReturn(Collections.singletonList(repositoryMock));
        when(passClientResultMock.getTotal()).thenReturn(1L);
        List<URI> r1_schemas_list = Arrays.asList(new URI("/example/schemas/schema1.json"),
                new URI("/example/schemas/schema2.json"));
        when(repositoryMock.getSchemas()).thenReturn(r1_schemas_list);

        InputStream expectedJsonSchema1 = SchemaFetcherTest.class
                .getResourceAsStream("/example/schemas/schema1.json");

        InputStream expectedJsonSchema2 = SchemaFetcherTest.class
                .getResourceAsStream("/example/schemas/schema2.json");

        JsonNode expectedschema1 = map.readTree(expectedJsonSchema1);
        JsonNode expectedschema2 = map.readTree(expectedJsonSchema2);
        List<JsonNode> expected = new ArrayList<>(Arrays.asList(expectedschema1, expectedschema2));
        List<JsonNode> result = schemaFetcher.getRepositorySchemas("repository1");
        assertEquals(expected, result);
    }

    @Test
    void getSchemasTest() throws Exception {
        List<String> repository_uris = new ArrayList<>(Arrays.asList("repository1"));
        when(passClientMock.selectObjects(any())).thenReturn(passClientResultMock);
        when(passClientResultMock.getTotal()).thenReturn(1L);
        when(passClientResultMock.getObjects()).thenReturn(Collections.singletonList(repositoryMock));

        List<URI> r1_schemas_list = Arrays.asList(new URI("/example/schemas/schema1.json"),
                new URI("/example/schemas/schema2.json"), new URI("/example/schemas/schema3.json"),
                new URI("/example/schemas/schema_to_deref.json"));

        when(repositoryMock.getSchemas()).thenReturn(r1_schemas_list);

        InputStream expectedJsonSchemaInput1 = SchemaFetcherTest.class
                .getResourceAsStream("/example/schemas/schema1.json");

        InputStream expectedJsonSchemaInput2 = SchemaFetcherTest.class
                .getResourceAsStream("/example/schemas/schema2.json");

        InputStream expectedJsonSchemaInput3 = SchemaFetcherTest.class
                .getResourceAsStream("/example/schemas/schema3.json");

        // dereferenced version of schema_to_deref.json
        String expectedJsonSchemaInput4 = "{\r\n"
                + "  \"$schema\": \"http://example.org/example/schemas/schema_to_dereference\",\r\n"
                + "  \"$id\": \"http://example.org/example/schemas/deref\",\r\n"
                + "  \"copySchemaName\": \"http://example.org/example/schemas/schema_to_dereference\",\r\n"
                + "  \"schema1_title\": \"X\",\r\n" + "  \"schema2_x\": {\r\n" + "    \"title\": \"x\",\r\n"
                + "    \"description\": \"an awesome letter\",\r\n" + "    \"$comment\": \"displays nicely\",\r\n"
                + "    \"type\": \"letter\"\r\n" + "},\r\n" + "  \"schema3_array\": [\"c\", \"d\", \"e\"],\r\n"
                + "  \"schema4_complexarray\": [\"e\", \"f\", {\"g\": \"h\"}],\r\n"
                + "  \"schema4_hk\": [\"l\", \"m\", \"m'\"]\r\n" + "}";

        JsonNode expectedschema1 = map.readTree(expectedJsonSchemaInput1);
        JsonNode expectedschema2 = map.readTree(expectedJsonSchemaInput2);
        JsonNode expectedschema3 = map.readTree(expectedJsonSchemaInput3);
        JsonNode expectedschema4 = map.readTree(expectedJsonSchemaInput4);
        List<JsonNode> expected = new ArrayList<>(
                Arrays.asList(expectedschema1, expectedschema2, expectedschema3, expectedschema4));
        List<JsonNode> result = schemaFetcher.getSchemas(repository_uris);
        assertEquals(expected, result);
    }

    @Test
    void invalidSchemaUriTest() throws Exception {
        when(passClientMock.selectObjects(any())).thenReturn(passClientResultMock);
        when(passClientResultMock.getTotal()).thenReturn(1L);
        when(passClientResultMock.getObjects()).thenReturn(Collections.singletonList(repositoryMock));
        List<URI> r1_schemas_list = Arrays.asList(new URI("/example/schemas/invalidschema.json"),
                new URI("/example/schemas/schema2.json"));
        when(repositoryMock.getSchemas()).thenReturn(r1_schemas_list);
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                schemaFetcher.getRepositorySchemas("repository1"));
    }

}
