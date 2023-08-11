/*
 * Copyright 2023 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.pass.main.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.eclipse.pass.main.ShibIntegrationTest;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.model.Grant;
import org.eclipse.pass.object.model.PassEntity;
import org.eclipse.pass.object.model.Source;
import org.eclipse.pass.object.model.Submission;
import org.eclipse.pass.object.model.User;
import org.eclipse.pass.object.model.UserRole;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * Ensure that HTTP requests are authenticated and authorized appropriately.
 */
public class AccessControlTest extends ShibIntegrationTest {
    private final static String JSON_API_CONTENT_TYPE = "application/vnd.api+json";
    private final static MediaType JSON_API_MEDIA_TYPE = MediaType.parse("application/vnd.api+json; charset=utf-8");

    // Check the HTTP response code and try to return the JSON result
    private JSONObject check(Response response, int code) throws IOException {
        if (response.code() != code) {
            print(response);
        }

        assertEquals(code, response.code());

        if (response.isSuccessful() && response.code() != 204) {
            String json = response.body().string();

            try {
                return new JSONObject(json);
            } catch (JSONException e) {
                fail("Expected JSON object, got: " + json);
            }
        }

        return null;
    }

    // Construct a PASS object with an optional attribute and optional relationship
    private JSONObject pass_object(String type, Object id) throws JSONException {
        JSONObject result = new JSONObject();
        JSONObject data = new JSONObject();

        data.put("type", type);

        if (id != null) {
            data.put("id", id.toString());
        }

        result.put("data", data);
        data.put("attributes", new JSONObject());
        data.put("relationships", new JSONObject());

        return result;
    }

    private JSONObject pass_object(String type) throws JSONException {
        return pass_object(type, null);
    }

    private void set_attribute(JSONObject obj, String field, Object value) throws JSONException {
        obj.getJSONObject("data").getJSONObject("attributes").put(field, value);
    }

    private String get_id(JSONObject obj) throws JSONException {
        return obj.getJSONObject("data").getString("id");
    }

    private void set_relationship(JSONObject obj, String rel_name, String rel_type, String rel_target)
            throws JSONException {
        JSONObject rels = obj.getJSONObject("data").getJSONObject("relationships");

        JSONObject rel = new JSONObject();

        rels.put(rel_name, rel);
        JSONObject rel_data = new JSONObject();

        rel.put("data", rel_data);
        rel_data.put("id", rel_target);
        rel_data.put("type", rel_type);
    }

    private void print(Response response) throws IOException {
        System.err.println(response.code() + " " + response.message());
        response.headers().names().forEach(h -> System.err.println("  " + h + ": " + response.header(h)));
        System.err.println(response.body().string());
    }

    @Test
    public void testReadGrantsAsAnonymous() throws IOException {
        String url = getBaseUrl() + "data/grant";

        Request request = new Request.Builder().url(url).header("Accept", JSON_API_CONTENT_TYPE)
                .addHeader("Content-Type", JSON_API_CONTENT_TYPE).get().build();

        Response response = client.newCall(request).execute();

        check(response, 401);
    }

    @Test
    public void testReadGrantsAsBackend() throws IOException {
        String url = getBaseUrl() + "data/grant";

        String credentials = Credentials.basic(BACKEND_USER, BACKEND_PASSWORD);

        Request request = new Request.Builder().url(url).header("Accept", JSON_API_CONTENT_TYPE)
                .addHeader("Content-Type", JSON_API_CONTENT_TYPE).header("Authorization", credentials).get().build();

        Response response = client.newCall(request).execute();

        check(response, 200);
    }

    @Test
    public void testReadPublicationsAsInvalidBackend() throws IOException {
        String url = getBaseUrl() + "data/publication";

        String credentials = Credentials.basic("baduser", "badpassword");

        Request request = new Request.Builder().url(url).header("Accept", JSON_API_CONTENT_TYPE)
                .addHeader("Content-Type", JSON_API_CONTENT_TYPE).header("Authorization", credentials).get().build();

        Response response = client.newCall(request).execute();

        check(response, 401);
    }

    @Test
    public void testParseShibHeaders() {
        MockHttpServletRequest req = new MockHttpServletRequest();

        req.addHeader(ShibConstants.SCOPED_AFFILIATION_HEADER, SUBMITTER_SCOPED_AFFILIATION);
        req.addHeader(ShibConstants.SN_HEADER, SUBMITTER_SUR_NAME);
        req.addHeader(ShibConstants.GIVENNAME_HEADER, SUBMITTER_GIVEN_NAME);
        req.addHeader(ShibConstants.UNIQUE_ID_HEADER, getSubmitterUniqueId());
        req.addHeader(ShibConstants.EMAIL_HEADER, SUBMITTER_EMAIL);
        req.addHeader(ShibConstants.EMPLOYEE_ID_HEADER, getSubmitterEmployeeId());
        req.addHeader(ShibConstants.DISPLAY_NAME_HEADER, SUBMITTER_NAME);
        req.addHeader(ShibConstants.EPPN_HEADER, getSubmitterEppn());

        assertTrue(ShibAuthenticationFilter.isShibRequest(req));

        User test = ShibAuthenticationFilter.parseShibHeaders(req);

        assertNotNull(test);
        test.setId(submitter.getId());
        assertEquals(submitter, test);
    }

    @Test
    public void testParseInvalidShibHeaders() {
        MockHttpServletRequest req = new MockHttpServletRequest();

        // No headers

        assertFalse(ShibAuthenticationFilter.isShibRequest(req));
        assertThrows(BadCredentialsException.class, () -> ShibAuthenticationFilter.parseShibHeaders(req));

        // Enough headers to look like a request

        req.addHeader(ShibConstants.UNIQUE_ID_HEADER, getSubmitterUniqueId());
        req.addHeader(ShibConstants.EPPN_HEADER, getSubmitterEppn());

        assertTrue(ShibAuthenticationFilter.isShibRequest(req));
        assertThrows(BadCredentialsException.class, () -> ShibAuthenticationFilter.parseShibHeaders(req));
    }

    @Test
    public void testReadGrantsAsShibUser() throws IOException {
        String url = getBaseUrl() + "data/grant";

        Request.Builder builder = new Request.Builder();
        setShibHeaders(builder);
        Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE)
                .addHeader("Content-Type", JSON_API_CONTENT_TYPE).get().build();

        Response response = client.newCall(request).execute();

        check(response, 200);
    }

    @Test
    public void testCreateGrantAsShibUser() throws IOException, JSONException {

        String url = getBaseUrl() + "data/grant";
        JSONObject grant = pass_object("grant");
        set_attribute(grant, "projectName", "This is a test");

        RequestBody body = RequestBody.create(grant.toString(), JSON_API_MEDIA_TYPE);
        Request.Builder builder = new Request.Builder();
        setShibHeaders(builder);
        Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE)
                .addHeader("Content-Type", JSON_API_CONTENT_TYPE).post(body).build();

        Response response = client.newCall(request).execute();

        check(response, 403);
    }

    @Test
    public void testCreateUpdateDeleteSubmissionAsShibUser() throws IOException, JSONException {
        JSONObject sub = pass_object("submission");
        set_attribute(sub, "submitterName", "Person Personson");
        set_relationship(sub, "submitter", "user", submitter.getId().toString());

        {
            String url = getBaseUrl() + "data/submission";

            RequestBody body = RequestBody.create(sub.toString(), JSON_API_MEDIA_TYPE);
            Request.Builder builder = new Request.Builder();
            setShibHeaders(builder);
            Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE)
                    .addHeader("Content-Type", JSON_API_CONTENT_TYPE).post(body).build();

            Response response = client.newCall(request).execute();

            sub = check(response, 201);
        }

        {
            set_attribute(sub, "submitterName", "Major Major");

            String url = getBaseUrl() + "data/submission/" + get_id(sub);
            RequestBody body = RequestBody.create(sub.toString(), JSON_API_MEDIA_TYPE);
            Request.Builder builder = new Request.Builder();
            setShibHeaders(builder);
            Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE)
                    .addHeader("Content-Type", JSON_API_CONTENT_TYPE).patch(body).build();

            Response response = client.newCall(request).execute();

            check(response, 204);
        }

        {
            String url = getBaseUrl() + "data/submission/" + get_id(sub);
            Request.Builder builder = new Request.Builder();
            setShibHeaders(builder);
            Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE).delete().build();

            Response response = client.newCall(request).execute();

            check(response, 403);
        }
    }

    @Test
    public void testCreateUpdateDeletePublicationAsShibUser() throws IOException, JSONException {
        JSONObject pub = pass_object("publication");
        set_attribute(pub, "title", "This is a title");

        {
            String url = getBaseUrl() + "data/publication";

            RequestBody body = RequestBody.create(pub.toString(), JSON_API_MEDIA_TYPE);
            Request.Builder builder = new Request.Builder();
            setShibHeaders(builder);
            Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE)
                    .addHeader("Content-Type", JSON_API_CONTENT_TYPE).post(body).build();

            Response response = client.newCall(request).execute();

            pub = check(response, 201);
        }

        {
            set_attribute(pub, "title", "updated title");

            String url = getBaseUrl() + "data/publication/" + get_id(pub);
            RequestBody body = RequestBody.create(pub.toString(), JSON_API_MEDIA_TYPE);
            Request.Builder builder = new Request.Builder();
            setShibHeaders(builder);
            Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE)
                    .addHeader("Content-Type", JSON_API_CONTENT_TYPE).patch(body).build();

            Response response = client.newCall(request).execute();

            check(response, 204);
        }

        {
            String url = getBaseUrl() + "data/publication/" + get_id(pub);
            Request.Builder builder = new Request.Builder();
            setShibHeaders(builder);
            Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE).delete().build();

            Response response = client.newCall(request).execute();

            check(response, 403);
        }
    }

    @Test
    public void testCreateUpdateDeleteFileAsShibUserOwningSubmission() throws IOException, JSONException {
        // File is associated with a submission associated with submitter
        // Shib user can create file, update the file, but not delete it.

        JSONObject file = pass_object("file");

        set_attribute(file, "name", "test.pdf");

        Submission sub = new Submission();

        try (PassClient pass_client = PassClient.newInstance(refreshableElide)) {
            sub.setSource(Source.PASS);
            sub.setSubmitter(submitter);

            pass_client.createObject(sub);
        }

        set_relationship(file, "submission", "submission", sub.getId().toString());

        {
            String url = getBaseUrl() + "data/file";

            RequestBody body = RequestBody.create(file.toString(), JSON_API_MEDIA_TYPE);
            Request.Builder builder = new Request.Builder();
            setShibHeaders(builder);
            Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE)
                    .addHeader("Content-Type", JSON_API_CONTENT_TYPE).post(body).build();

            Response response = client.newCall(request).execute();

            file = check(response, 201);
        }

        {
            set_attribute(file, "name", "test2.doc");

            String url = getBaseUrl() + "data/file/" + get_id(file);
            RequestBody body = RequestBody.create(file.toString(), JSON_API_MEDIA_TYPE);
            Request.Builder builder = new Request.Builder();
            setShibHeaders(builder);
            Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE)
                    .addHeader("Content-Type", JSON_API_CONTENT_TYPE).patch(body).build();

            Response response = client.newCall(request).execute();

            check(response, 204);
        }

        {
            String url = getBaseUrl() + "data/file/" + get_id(file);
            Request.Builder builder = new Request.Builder();
            setShibHeaders(builder);
            Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE).delete().build();

            Response response = client.newCall(request).execute();

            check(response, 403);
        }
    }

    @Test
    public void testCreateUpdateDeleteEventAsShibUserOwningSubmission() throws IOException, JSONException {
        // File is associated with a submission associated with submitter
        // Shib user can create file, update the file, but not delete it.

        JSONObject event = pass_object("submissionEvent");

        set_attribute(event, "comment", "good submission");

        Submission sub = new Submission();

        try (PassClient pass_client = PassClient.newInstance(refreshableElide)) {
            sub.setSource(Source.PASS);
            sub.setSubmitter(submitter);

            pass_client.createObject(sub);
        }

        set_relationship(event, "submission", "submission", sub.getId().toString());

        {
            String url = getBaseUrl() + "data/submissionEvent";

            RequestBody body = RequestBody.create(event.toString(), JSON_API_MEDIA_TYPE);
            Request.Builder builder = new Request.Builder();
            setShibHeaders(builder);
            Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE)
                    .addHeader("Content-Type", JSON_API_CONTENT_TYPE).post(body).build();

            Response response = client.newCall(request).execute();

            event = check(response, 201);
        }

        {
            set_attribute(event, "comment", "hmm");

            String url = getBaseUrl() + "data/submissionEvent/" + get_id(event);
            RequestBody body = RequestBody.create(event.toString(), JSON_API_MEDIA_TYPE);
            Request.Builder builder = new Request.Builder();
            setShibHeaders(builder);
            Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE)
                    .addHeader("Content-Type", JSON_API_CONTENT_TYPE).patch(body).build();

            Response response = client.newCall(request).execute();

            check(response, 403);
        }

        {
            String url = getBaseUrl() + "data/submissionEvent/" + get_id(event);
            Request.Builder builder = new Request.Builder();
            setShibHeaders(builder);
            Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE).delete().build();

            Response response = client.newCall(request).execute();

            check(response, 403);
        }
    }

    @Test
    public void testCreateFileAsShibUserNotOwningSubmission() throws IOException, JSONException {
        String url = getBaseUrl() + "data/file";

        JSONObject file = pass_object("file");
        // File does not point to submission user owns
        set_attribute(file, "name", "moo.xml");

        RequestBody body = RequestBody.create(file.toString(), JSON_API_MEDIA_TYPE);
        Request.Builder builder = new Request.Builder();
        setShibHeaders(builder);
        Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE)
                .addHeader("Content-Type", JSON_API_CONTENT_TYPE).post(body).build();

        Response response = client.newCall(request).execute();

        check(response, 403);
    }

    @Test
    public void testCreateEventAsShibUserNotOwningSubmission() throws IOException, JSONException {
        String url = getBaseUrl() + "data/submissionEvent";

        JSONObject event = pass_object("submissionEvent");
        // File does not point to submission user owns
        set_attribute(event, "comment", "This should not work");

        RequestBody body = RequestBody.create(event.toString(), JSON_API_MEDIA_TYPE);
        Request.Builder builder = new Request.Builder();
        setShibHeaders(builder);
        Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE)
                .addHeader("Content-Type", JSON_API_CONTENT_TYPE).post(body).build();

        Response response = client.newCall(request).execute();

        check(response, 403);
    }

    @Test
    public void testUpdateGrantAsShibUser() throws IOException, JSONException {
        Long id = null;

        try (PassClient client = PassClient.newInstance(refreshableElide)) {
            Grant grant = new Grant();
            grant.setAwardNumber("zipededoda");
            client.createObject(grant);

            id = grant.getId();
        }

        JSONObject grant = pass_object("grant", id);
        set_attribute(grant, "projectName", "The best project");

        String url = getBaseUrl() + "data/grant/" + id;
        RequestBody body = RequestBody.create(grant.toString(), JSON_API_MEDIA_TYPE);
        Request.Builder builder = new Request.Builder();
        setShibHeaders(builder);
        Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE)
                .addHeader("Content-Type", JSON_API_CONTENT_TYPE).patch(body).build();

        Response response = client.newCall(request).execute();

        check(response, 403);
    }

    @Test
    public void testDeleteGrantAsShibUser() throws IOException {
        Long id = null;

        try (PassClient client = PassClient.newInstance(refreshableElide)) {
            Grant grant = new Grant();
            grant.setAwardNumber("zipededoda");
            client.createObject(grant);

            id = grant.getId();
        }

        String url = getBaseUrl() + "data/grant/" + id;
        Request.Builder builder = new Request.Builder();
        setShibHeaders(builder);
        Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE).delete().build();

        Response response = client.newCall(request).execute();

        check(response, 403);
    }

    @Test
    public void testCreateUpdateDeleteGrantAsBackend() throws IOException, JSONException {
        String credentials = Credentials.basic(BACKEND_USER, BACKEND_PASSWORD);

        JSONObject grant = pass_object("grant");
        set_attribute(grant, "projectName", "backend test");

        // Create a grant
        {
            String url = getBaseUrl() + "data/grant";

            RequestBody body = RequestBody.create(grant.toString(), JSON_API_MEDIA_TYPE);
            Request request = new Request.Builder().url(url).header("Accept", JSON_API_CONTENT_TYPE)
                    .addHeader("Content-Type", JSON_API_CONTENT_TYPE).header("Authorization", credentials).post(body)
                    .build();

            Response response = client.newCall(request).execute();

            grant = check(response, 201);
        }

        // Update the grant
        {
            set_attribute(grant, "projectName", "backend update");

            String url = getBaseUrl() + "data/grant/" + get_id(grant);
            RequestBody body = RequestBody.create(grant.toString(), JSON_API_MEDIA_TYPE);
            Request request = new Request.Builder().url(url).header("Accept", JSON_API_CONTENT_TYPE)
                    .addHeader("Content-Type", JSON_API_CONTENT_TYPE).header("Authorization", credentials).patch(body)
                    .build();

            Response response = client.newCall(request).execute();

            check(response, 204);
        }

        // Delete the grant
        {
            String url = getBaseUrl() + "data/grant/" + get_id(grant);
            Request request = new Request.Builder().url(url).header("Accept", JSON_API_CONTENT_TYPE)
                    .header("Authorization", credentials).delete().build();

            Response response = client.newCall(request).execute();

            check(response, 204);
        }
    }

    @Test
    public void testUpdateExistingShibUserWithNullValues() throws IOException {
        // Create a user with null values except for the required locators
        User user = new User();

        try (PassClient pass_client = PassClient.newInstance(refreshableElide)) {
            String[] locators = {
                "johnshopkins.edu:unique-id:cow123",
                "johnshopkins.edu:eppn:bessiecow123"
            };
            user.setLocatorIds(Arrays.asList(locators));

            pass_client.createObject(user);
        }

        // User should be matched by request and updated

        String url = getBaseUrl() + "data/grant";

        Request.Builder builder = new Request.Builder();

        builder.addHeader(ShibConstants.SCOPED_AFFILIATION_HEADER, SUBMITTER_SCOPED_AFFILIATION);
        builder.addHeader(ShibConstants.SN_HEADER, "Cow");
        builder.addHeader(ShibConstants.GIVENNAME_HEADER, "Bessie");
        builder.addHeader(ShibConstants.UNIQUE_ID_HEADER, "cow123@johnshopkins.edu");
        builder.addHeader(ShibConstants.EMAIL_HEADER, "cow123@jhu.edu");
        builder.addHeader(ShibConstants.EMPLOYEE_ID_HEADER, "123");
        builder.addHeader(ShibConstants.DISPLAY_NAME_HEADER, "Bessie the Cow");
        builder.addHeader(ShibConstants.EPPN_HEADER, "bessiecow123@johnshopkins.edu");

        Request request = builder.url(url).header("Accept", JSON_API_CONTENT_TYPE)
                .addHeader("Content-Type", JSON_API_CONTENT_TYPE).get().build();

        Response response = client.newCall(request).execute();

        check(response, 200);

        // Check that user is updated

        try (PassClient pass_client = PassClient.newInstance(refreshableElide)) {
            user = pass_client.getObject(User.class, user.getId());

            String[] locators = {
                "johnshopkins.edu:unique-id:cow123",
                "johnshopkins.edu:eppn:bessiecow123",
                "johnshopkins.edu:employeeid:123"
            };
            String[] affil = {"FACULTY@johnshopkins.edu", "johnshopkins.edu"};

            assertEquals(user.getDisplayName(), "Bessie the Cow");
            assertEquals(user.getUsername(), "bessiecow123@johnshopkins.edu");
            assertEquals(user.getLastName(), "Cow");
            assertEquals(user.getEmail(), "cow123@jhu.edu");
            assertTrue(PassEntity.listEquals(user.getLocatorIds(), List.of(locators)));
            assertEquals(user.getRoles(), List.of(UserRole.SUBMITTER));
            assertEquals(user.getAffiliation(), Set.of(affil));
        }
    }
}
