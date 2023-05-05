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
package org.eclipse.pass.support.grant.data;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This util class is designed to hit a service which provides resolution of one identifier to another. The two
 * endpoints
 * provide lookups between our Hopkins Id, which is a durable identifier for all members of the Hopkins community,
 * and the
 * employee ID, which is a durable identifier for all Hopkins employees. This lookup service is necessary because we
 * do not
 * have access to the wider identifier in our grants data source.
 *
 * @author jrm
 */
class DirectoryServiceUtil {

    static final String DIRECTORY_SERVICE_BASE_URL = "directory.base.url";
    static final String DIRECTORY_SERVICE_CLIENT_ID = "directory.client.id";
    static final String DIRECTORY_SERVICE_CLIENT_SECRET = "directory.client.secret";

    private String directoryBaseUrl;
    private String directoryClientId;
    private String directoryClientSecret;

    private final OkHttpClient client;
    private final JsonFactory factory = new JsonFactory();

    //these are for caching results
    private final Map<String, String> hopkins2ee = new HashMap<>();
    private final Map<String, String> ee2hopkins = new HashMap<>();

    DirectoryServiceUtil(Properties connectionProperties) {
        if (connectionProperties != null) {

            if (connectionProperties.getProperty(DIRECTORY_SERVICE_BASE_URL) != null) {
                this.directoryBaseUrl = connectionProperties.getProperty(DIRECTORY_SERVICE_BASE_URL);
            }
            if (connectionProperties.getProperty(DIRECTORY_SERVICE_CLIENT_ID) != null) {
                this.directoryClientId = connectionProperties.getProperty(DIRECTORY_SERVICE_CLIENT_ID);
            }
            if (connectionProperties.getProperty(DIRECTORY_SERVICE_CLIENT_SECRET) != null) {
                this.directoryClientSecret = connectionProperties.getProperty(DIRECTORY_SERVICE_CLIENT_SECRET);
            }
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, SECONDS);
        builder.readTimeout(30, SECONDS);
        builder.writeTimeout(30, SECONDS);
        client = builder.build();
    }

    /**
     * an enum to hold values for the service URL ending, and the query parameter name
     * for these lookup services
     */
    private enum Type {
        EMPLOYEE2HOPKINS("EmployeeID_to_HopkinsID", "employeeid"),
        HOPKINS2EMPLOYEE("HopkinsID_to_EmployeeID", "hopkinsid");

        private final String serviceUrlEnding;
        private final String queryParameter;

        Type(String serviceUrlEnding, String queryParameter) {
            this.serviceUrlEnding = serviceUrlEnding;
            this.queryParameter = queryParameter;
        }

        public String getServiceUrlEnding() {
            return serviceUrlEnding;
        }

        public String getQueryParameter() {
            return queryParameter;
        }
    }

    /**
     * Return Hopkins ID for a given employee ID. we cache lookups in a map so that we only need to perform
     * a lookup once per session per user.
     *
     * @param employeeId the user's employeeId
     * @return the user's Hopkins ID - should never be null
     * @throws IOException if the service cannot be reached
     */
    String getHopkinsIdForEmployeeId(String employeeId) throws IOException {
        String hopkinsId;
        if (!ee2hopkins.containsKey(employeeId)) {
            hopkinsId = askDirectoryForMappedValue(Type.EMPLOYEE2HOPKINS, employeeId);
            ee2hopkins.put(employeeId, hopkinsId);
        } else {
            hopkinsId = ee2hopkins.get(employeeId);
        }
        return hopkinsId;
    }

    /**
     * Return employee ID for a given Hopkins ID. we cache lookups in a map so that we only need to perform
     * a lookup once per session per user.
     *
     * @param hopkinsId the user's Hopkins ID
     * @return the user's employee ID if it exists; null if it does not
     * @throws IOException if there is an IO exception
     */
    String getEmployeeIdForHopkinsId(String hopkinsId) throws IOException {
        String employeeId;
        if (!hopkins2ee.containsKey(hopkinsId)) {
            employeeId = askDirectoryForMappedValue(Type.HOPKINS2EMPLOYEE, hopkinsId);
            hopkins2ee.put(hopkinsId, employeeId);
        } else {
            employeeId = hopkins2ee.get(hopkinsId);
        }
        return employeeId;
    }

    private String askDirectoryForMappedValue(Type type, String sourceId) throws IOException {
        String name = type.getQueryParameter();
        String suffix = type.getServiceUrlEnding();
        String serviceUrl;
        if (!directoryBaseUrl.endsWith("/")) {
            directoryBaseUrl = directoryBaseUrl + "/";
        }
        serviceUrl = directoryBaseUrl + suffix;

        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(serviceUrl)).newBuilder()
                                            .addQueryParameter(name, sourceId);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder().header("client_id", directoryClientId)
                                               .header("client_secret", directoryClientSecret).url(url).build();

        Response response = client.newCall(request).execute();

        assert response.body() != null;
        JsonParser parser = factory.createParser(response.body().string());
        String mappedValue = null;
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                String fieldName = parser.getCurrentName();
                parser.nextToken();
                if (sourceId.equals(fieldName)) {
                    mappedValue = parser.getValueAsString();
                    mappedValue = mappedValue.equals("NULL") ? null : mappedValue;
                }
            }
        }

        return mappedValue;

    }

}
