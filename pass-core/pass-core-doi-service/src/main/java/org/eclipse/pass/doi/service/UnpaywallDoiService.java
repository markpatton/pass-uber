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

import java.util.HashMap;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;

public class UnpaywallDoiService extends ExternalDoiService {

    @Override
    public String name() {
        return "Unpaywall";
    }

    @Override
    public String baseUrl() {
        String UNPAYWALL_BASEURI = "https://api.unpaywall.org/v2/";
        return System.getenv("UNPAYWALL_BASEURI") != null ? System.getenv(
            "UNPAYWALL_BASEURI") : UNPAYWALL_BASEURI;
    }

    @Override
    public HashMap<String, String> parameterMap() {
        HashMap<String, String> parameterMap = new HashMap<>();
        String MAILTO = "pass@jhu.edu";
        String agent = System.getenv("PASS_DOI_SERVICE_MAILTO") != null ? System.getenv(
            "PASS_DOI_SERVICE_MAILTO") : MAILTO;
        parameterMap.put("email", agent);
        return parameterMap;
    }

    @Override
    public HashMap<String, String> headerMap() {
        return null;
    }

    @Override
    public JsonObject processObject(JsonObject object) {
        JsonArray locations = object.getJsonArray("oa_locations");
        JsonArrayBuilder jab = Json.createArrayBuilder();

        for (int i = 0; i < locations.size(); i++) {
            JsonObject manuscript = locations.getJsonObject(i);
            JsonValue urlForPdf = manuscript.getValue("/url_for_pdf");

            JsonValue filename;
            if ( urlForPdf == JsonValue.NULL ) {
                filename = JsonValue.NULL;
            } else {
                String urlForPdfString = urlForPdf.toString().replaceAll("\"","");
                filename = Json.createValue (urlForPdfString.substring(urlForPdfString.lastIndexOf('/') + 1));
            }

            JsonValue repoInst = manuscript.getValue("/repository_institution");

            JsonObject manuscriptObject = Json.createObjectBuilder().add("url", urlForPdf)
                                              .add("repositoryLabel", repoInst)
                                              .add("type", "application/pdf")
                                              .add("source", name())
                                              .add("name", filename)
                                              .build();
            jab.add(manuscriptObject);
        }

        return Json.createObjectBuilder()
                   .add("manuscripts", jab.build())
                   .build();
    }
}
