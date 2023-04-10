package org.eclipse.pass.metadataschema;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import com.yahoo.elide.RefreshableElide;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.eclipse.pass.main.IntegrationTest;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.PassClientResult;
import org.eclipse.pass.object.PassClientSelector;
import org.eclipse.pass.object.RSQL;
import org.eclipse.pass.object.model.IntegrationType;
import org.eclipse.pass.object.model.Repository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

public class MetadataSchemaServiceTest extends IntegrationTest {
    private String credentials = Credentials.basic(BACKEND_USER, BACKEND_PASSWORD);
    private OkHttpClient httpClient = new OkHttpClient();
    private Long repo1Id;
    private Long repo2Id;
    private Long repo3Id;
    private Long repo4Id;
    private Long repo5Id;

    @Autowired
    protected RefreshableElide refreshableElide;

    @BeforeAll
    public void setup() {
        repo1Id = setupRepo1();
        repo2Id = setupRepo2();
        repo3Id = setupRepo3(); //contains missing schema to test error handling
        repo4Id = setupRepo4(); //contains bad schema to test error handling
        repo5Id = setupRepo5(); //contains schemas with merge conflict to test error handling
    }

    @Test
    public void testSchemaControllerOneRepoWithMergeTrue() throws Exception {
        List<URI> r1_schemas_list = Arrays.asList(new URI("https://example.com/metadata-schemas/jhu/schema1.json"),
                new URI("https://example.com/metadata-schemas/jhu/schema2.json"));

        String url = getBaseUrl() + "schemaservice?entityIds=" + repo1Id.toString() + "&merge=true";
        Request okHttpRequest = new Request.Builder()
                .url(url).header("Authorization", credentials)
                .build();
        Call call = httpClient.newCall(okHttpRequest);
        Response response = call.execute();

        assertThat(response.code()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.body()).isNotNull();
    }

    @Test
    public void testSchemaControllerOneRepoWithMergeFalse() throws Exception {
        String url = getBaseUrl() + "schemaservice?entityIds=" + repo1Id.toString() + "&merge=false";
        Request okHttpRequest = new Request.Builder()
                .url(url).header("Authorization", credentials)
                .build();
        Call call = httpClient.newCall(okHttpRequest);
        Response response = call.execute();

        assertThat(response.code()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.body()).isNotNull();
    }

    @Test
    public void testSchemaControllerTwoRepoWithMergeTrue() throws Exception {
        String url = getBaseUrl() + "schemaservice?entityIds=" + repo1Id.toString() + ","
                + repo2Id.toString() + "&merge=true";
        Request okHttpRequest = new Request.Builder()
                .url(url).header("Authorization", credentials)
                .build();
        Call call = httpClient.newCall(okHttpRequest);
        Response response = call.execute();

        assertThat(response.code()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.body()).isNotNull();
    }

    @Test
    public void testSchemaControllerTwoRepoWithMergeFalse() throws Exception {
        String url = getBaseUrl() + "schemaservice?entityIds=" + repo1Id.toString() + ","
                + repo2Id.toString() + "&merge=false";
        Request okHttpRequest = new Request.Builder()
                .url(url).header("Authorization", credentials)
                .build();
        Call call = httpClient.newCall(okHttpRequest);
        Response response = call.execute();

        assertThat(response.code()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.body()).isNotNull();
    }

    @Test
    public void testSchemaControllerWithNoEntityId() throws Exception {
        String url = getBaseUrl() + "schemaservice?entityIds=&merge=true";
        Request okHttpRequest = new Request.Builder()
                .url(url).header("Authorization", credentials)
                .build();
        Call call = httpClient.newCall(okHttpRequest);
        Response response = call.execute();

        assertThat(response.code()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.body()).isNotNull();
    }

    @Test
    public void testSchemaControllerWithMissingLocalSchema() throws Exception {
        String url = getBaseUrl() + "schemaservice?entityIds=" + repo3Id.toString() + "&merge=true";
        Request okHttpRequest = new Request.Builder()
                .url(url).header("Authorization", credentials)
                .build();
        Call call = httpClient.newCall(okHttpRequest);
        Response response = call.execute();

        assertThat(response.code()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.body()).isNotNull();
    }

    @Test
    public void testSchemaControllerWithBadLocalSchema() throws Exception {
        String url = getBaseUrl() + "schemaservice?entityIds=" + repo4Id.toString() + "&merge=true";
        Request okHttpRequest = new Request.Builder()
                .url(url).header("Authorization", credentials)
                .build();
        Call call = httpClient.newCall(okHttpRequest);
        Response response = call.execute();

        assertThat(response.code()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.body()).isNotNull();
    }

    @Test
    public void testSchemaControllerWithMergeConflict() throws Exception {
        String url = getBaseUrl() + "schemaservice?entityIds=" + repo5Id.toString() + "&merge=true";
        Request okHttpRequest = new Request.Builder()
                .url(url).header("Authorization", credentials)
                .build();
        Call call = httpClient.newCall(okHttpRequest);
        Response response = call.execute();

        assertThat(response.code()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.body()).isNotNull();
    }

    private Long setupRepo1() {
        Repository repository = new Repository();
        repository.setName("Test Repository 1");
        repository.setDescription("Repository 1 description");
        repository.setUrl(URI.create("https://example.com/repository1"));
        repository.setAgreementText("Repository 1 agreement text");
        repository.setIntegrationType(IntegrationType.of("web-link"));
        repository.setSchemas(Arrays.asList(URI.create("https://example.com/metadata-schemas/jhu/schema1.json"),
                URI.create("https://example.com/metadata-schemas/jhu/schema2.json")));
        repository.setRepositoryKey("nih-repository");

        PassClient passClient = PassClient.newInstance(refreshableElide);

        try {
            passClient.createObject(repository);
            String filter = RSQL.equals("name", "Test Repository 1");
            PassClientResult<Repository> result = passClient.
                    selectObjects(new PassClientSelector<>(Repository.class, 0, 100,
                            filter, null));
            return result.getObjects().get(0).getId();
        } catch (IOException e) {
            assertThat("error creating repository")
                    .isEqualTo("error creating repository1 " + e.getMessage());
        }
        return null;
    }

    private Long setupRepo2() {
        Repository repository = new Repository();
        repository.setName("Test Repository 2");
        repository.setDescription("Repository 2 description");
        repository.setUrl(URI.create("https://example.com/repository2"));
        repository.setAgreementText("Repository 2 agreement text");
        repository.setIntegrationType(IntegrationType.of("web-link"));
        repository.setSchemas(Arrays.asList(URI.create("https://example.com/metadata-schemas/jhu/schema2.json"),
                URI.create("https://example.com/metadata-schemas/jhu/schema3.json"),
                URI.create("https://example.com/metadata-schemas/jhu/schema_to_deref.json")));
        repository.setRepositoryKey("nih-repository");

        PassClient passClient = PassClient.newInstance(refreshableElide);

        try {
            passClient.createObject(repository);
            String filter = RSQL.equals("name", "Test Repository 2");
            PassClientResult<Repository> result = passClient.
                    selectObjects(new PassClientSelector<>(Repository.class, 0, 100,
                            filter, null));
            return result.getObjects().get(0).getId();
        } catch (IOException e) {
            assertThat("error creating repository")
                    .isEqualTo("error creating repository2 " + e.getMessage());
        }
        return null;
    }

    private Long setupRepo3() {
        Repository repository = new Repository();
        repository.setName("Test Repository 3");
        repository.setDescription("Repository 3 - missing schema");
        repository.setUrl(URI.create("https://example.com/repository3"));
        repository.setAgreementText("Repository 3 agreement text");
        repository.setIntegrationType(IntegrationType.of("web-link"));
        repository.setSchemas(Arrays.asList(URI.create("https://example.com/metadata-schemas/jhu/schema2.json"),
                URI.create("https://example.com/metadata-schemas/jhu/MissingSchema.json"),
                URI.create("https://example.com/metadata-schemas/jhu/schema_to_deref.json")));
        repository.setRepositoryKey("nih-repository");

        PassClient passClient = PassClient.newInstance(refreshableElide);

        try {
            passClient.createObject(repository);
            String filter = RSQL.equals("name", "Test Repository 3");
            PassClientResult<Repository> result = passClient.
                    selectObjects(new PassClientSelector<>(Repository.class, 0, 100,
                            filter, null));
            return result.getObjects().get(0).getId();
        } catch (IOException e) {
            assertThat("error creating repository")
                    .isEqualTo("error creating repository3 " + e.getMessage());
        }
        return null;
    }

    private Long setupRepo4() {
        Repository repository = new Repository();
        repository.setName("Test Repository 4");
        repository.setDescription("Repository 4 - missing schema");
        repository.setUrl(URI.create("https://example.com/repository4"));
        repository.setAgreementText("Repository 4 agreement text");
        repository.setIntegrationType(IntegrationType.of("web-link"));
        repository.setSchemas(Arrays.asList(URI.create("https://example.com/metadata-schemas/jhu/schema2.json"),
                URI.create("https://example.com/metadata-schemas/jhu/bad_schema.json"),
                URI.create("https://example.com/metadata-schemas/jhu/schema_to_deref.json")));
        repository.setRepositoryKey("nih-repository");

        PassClient passClient = PassClient.newInstance(refreshableElide);

        try {
            passClient.createObject(repository);
            String filter = RSQL.equals("name", "Test Repository 4");
            PassClientResult<Repository> result = passClient.
                    selectObjects(new PassClientSelector<>(Repository.class, 0, 100,
                            filter, null));
            return result.getObjects().get(0).getId();
        } catch (IOException e) {
            assertThat("error creating repository")
                    .isEqualTo("error creating repository4 " + e.getMessage());
        }
        return null;
    }

    private Long setupRepo5() {
        Repository repository = new Repository();
        repository.setName("Test Repository 5");
        repository.setDescription("Repository 5 - merge conflict schema");
        repository.setUrl(URI.create("https://example.com/repository5"));
        repository.setAgreementText("Repository 5 agreement text");
        repository.setIntegrationType(IntegrationType.of("web-link"));
        repository.setSchemas(Arrays.asList(URI.create("https://example.com/metadata-schemas/jhu/schema_merge_conflict1.json"),
                URI.create("https://example.com/metadata-schemas/jhu/schema_merge_conflict2.json")));
        repository.setRepositoryKey("nih-repository");

        PassClient passClient = PassClient.newInstance(refreshableElide);

        try {
            passClient.createObject(repository);
            String filter = RSQL.equals("name", "Test Repository 5");
            PassClientResult<Repository> result = passClient.
                    selectObjects(new PassClientSelector<>(Repository.class, 0, 100,
                            filter, null));
            return result.getObjects().get(0).getId();
        } catch (IOException e) {
            assertThat("error creating repository")
                    .isEqualTo("error creating repository5 " + e.getMessage());
        }
        return null;
    }

}
