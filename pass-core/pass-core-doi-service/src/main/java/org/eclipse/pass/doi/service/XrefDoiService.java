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

/**
 * The XrefDoiService class is an implementation of the ExternalDoiService abstract class to interface with the Crossref
 * API. The Crossref API is a RESTful API that returns JSON metadata for a given DOI.
 * <p>
 * The Crossref API is documented here: <a href="https://api.crossref.org/">Crossref API</a>
 * <p>
 * The Crossref API requires a User-Agent header to be set on the request. The value of this header must be an email
 * address. The default email address used by is pass@jhu.edu and can be overridden by setting the environment variable
 * PASS_DOI_SERVICE_MAILTO
 */
public class XrefDoiService extends ExternalDoiService {

    private final static String XREF_BASEURI = "https://api.crossref.org/v1/works/";

    @Override
    public String name() {
        return "Crossref";
    }

    @Override
    public String baseUrl() {
        return System.getenv("XREF_BASEURI") != null ? System.getenv(
            "XREF_BASEURI") : XREF_BASEURI;
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
