package org.eclipse.pass.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;
import org.eclipse.pass.main.ShibIntegrationTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class UserServiceTest extends ShibIntegrationTest {
    @Test
    public void testHandleRequest() throws IOException, JSONException {
        String url = getBaseUrl() + "user/whoami";

        Request.Builder builder = new Request.Builder().url(url);
        setShibHeaders(builder);
        Request request = builder.get().build();

        Response response = client.newCall(request).execute();

        assertEquals(200, response.code());

        JSONObject result = new JSONObject(response.body().string());

        assertEquals(submitter.getId().toString(), result.getString("id"));
        assertEquals("user", result.getString("type"));
        assertTrue(result.getString("uri").endsWith("/data/user/" + submitter.getId()));
    }
}
