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
import javax.json.JsonObject;

public class XrefDoiService extends ExternalDoiService {

    private String MAILTO = "pass@jhu.edu";
    private String XREF_BASEURI = "https://api.crossref.org/v1/works/";

    @Override
    public String name() {
        return "Crossref";
    }

    @Override
    public String baseUrl() {
        String baseUri = System.getenv("XREF_BASEURI") != null ? System.getenv(
            "XREF_BASEURI") : XREF_BASEURI;
        return baseUri;
    }

    @Override
    public HashMap<String, String> parameterMap() {
        return null;
    }

    @Override
    public HashMap<String, String> headerMap() {
        HashMap<String, String> headerMap = new HashMap<>();
        String agent = System.getenv("PASS_DOI_SERVICE_MAILTO") != null ? System.getenv(
            "PASS_DOI_SERVICE_MAILTO") : MAILTO;
        headerMap.put("User-Agent", agent);
        return headerMap;
    }

    @Override
    public JsonObject processObject(JsonObject object) {
        return object;
    }

}
