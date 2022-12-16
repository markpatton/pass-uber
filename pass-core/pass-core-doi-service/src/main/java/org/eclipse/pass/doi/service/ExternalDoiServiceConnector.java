/*
 *
 * Copyright 2022 Johns Hopkins University
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.eclipse.pass.doi.service;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalDoiServiceConnector {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalDoiServiceConnector.class);

    private OkHttpClient client;

    ExternalDoiServiceConnector() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, SECONDS);
        builder.readTimeout(30, SECONDS);
        builder.writeTimeout(30, SECONDS);
        this.client = builder.build();
    }

    /**
     * consult external service to get a json object for a supplied doi
     *
     * @param doi - the supplied doi string, prefix trimmed if necessary
     * @return a string representing the works object if successful; an empty string if not found; null if IO exception
     */
    JsonObject retrieveMetdata(String doi, ExternalDoiService service) {
        LOG.debug("Attempting to retrieve " + service.name() + "metadata for doi " + doi);

        HttpUrl.Builder urlBuilder = HttpUrl.parse(service.baseUrl() + doi).newBuilder();

        if ( service.parameterMap() != null ) {
            for ( String key : service.parameterMap().keySet() ) {
                urlBuilder.addQueryParameter(key, service.parameterMap().get(key));
            }
        }

        String url = urlBuilder.build().toString();

        Request.Builder requestBuilder =  new Request.Builder()
            .url(url);
        if ( service.headerMap() != null ) {
            requestBuilder.headers(Headers.of(service.headerMap()));
        }
        Request okHttpRequest =  requestBuilder.build();

        Call call = client.newCall(okHttpRequest);
        JsonReader reader;
        JsonObject metadataJsonObject;
        String responseString = null;

        try (Response okHttpResponse = call.execute()) {
            responseString = okHttpResponse.body().string();
            reader = Json.createReader(new StringReader(responseString));
            metadataJsonObject = reader.readObject();
            reader.close();

            service.unlockDoi(doi);

            return metadataJsonObject;
        } catch (JsonParsingException e) {
            if (responseString != null) {
                return Json.createObjectBuilder()
                           .add("error", responseString)
                           .build();
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

}

