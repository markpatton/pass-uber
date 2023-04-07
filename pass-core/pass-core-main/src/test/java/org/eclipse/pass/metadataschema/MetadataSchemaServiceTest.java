package org.eclipse.pass.metadataschema;

import okhttp3.Credentials;
import org.eclipse.pass.main.IntegrationTest;
import org.eclipse.pass.main.Main;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.model.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = Main.class,
        properties = {"PASS_CORE_BACKEND_PASSWORD=test"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MetadataSchemaServiceTest extends IntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    private TestRestTemplate restTemplateCred;
    @MockBean
    private PassClient passClientMock;
    private Repository repositoryMock1;
    private Repository repositoryMock2;
    private String credentials = Credentials.basic(BACKEND_USER, BACKEND_PASSWORD);

    @BeforeEach
    public void setup() {
        restTemplate.getRestTemplate().getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", credentials);
            return execution.execute(request, body);
        });
        passClientMock = mock(PassClient.class);
        repositoryMock1 = mock(Repository.class);
        repositoryMock2 = mock(Repository.class);
    }

    @Test
    public void testGetSchemaOneRepoWithMergeTrue() throws Exception {
        List<URI> repoSchemas = Arrays.asList(new URI("https://example.com/metadata-schemas/jhu/schema1.json"),
                new URI("https://example.com/metadata-schemas/jhu/schema2.json"));
        when(passClientMock.getObject(Repository.class, 1L)).thenReturn(repositoryMock1);
        when(repositoryMock1.getSchemas()).thenReturn(repoSchemas);

        String entityIds = "1";
        String mergeSchemaOpt = "true";
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/schemaservice?entityIds=" + entityIds + "&merge=" + mergeSchemaOpt, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();
    }

    /*@TestConfiguration
    static class TestConfig {

        @Bean
        public PassSchemaServiceController passSchemaServiceController(PassClient passClient) {
            return new PassSchemaServiceController(passClient);
        }

        @Bean
        public PassClient passClient() {
            return mock(PassClient.class);
        }
    }*/
}
