package org.eclipse.pass.doi.service;

import static com.jayway.restassured.RestAssured.port;
import static org.junit.jupiter.api.Assertions.assertEquals;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.eclipse.pass.main.IntegrationTest;
import org.junit.jupiter.api.Test;

public class DoiServiceTest extends IntegrationTest {

    OkHttpClient client = new OkHttpClient();

    /**
     * throw in a "moo" doi, expect a 400 error
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void smokeTest() throws Exception {

        HttpUrl url = new HttpUrl.Builder()
            .scheme("http")
            .host("localhost")
            .port(port)
            .addPathSegment("pass-doi-service")
            .addPathSegment("journal")
            .addQueryParameter("doi", "moo")
            .build();

        Request okHttpRequest = new Request.Builder()
            .url(url)
            .build();
        Call call = client.newCall(okHttpRequest);
        try (Response okHttpResponse = call.execute()) {

            assertEquals(400, okHttpResponse.code());
            assertEquals("{\"error\":\"Supplied DOI is not in valid Crossref format.\"}",
                         okHttpResponse.body().string());

        }
    }


}
