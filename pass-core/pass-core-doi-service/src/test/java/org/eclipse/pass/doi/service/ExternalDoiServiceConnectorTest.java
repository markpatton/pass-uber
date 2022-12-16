/*
 *
 * Copyright 2019 Johns Hopkins University
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Unit tests for the xref connector
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExternalDoiServiceConnectorTest {

    private final ExternalDoiServiceConnector underTest = new ExternalDoiServiceConnector();
    private final ExternalDoiService xrefService = new XrefDoiService();
    private final ExternalDoiService unpaywallService = new UnpaywallDoiService();

    /**
     * test that hitting the Crossref API with a doi returns the expected JSON object
     */
    @Test
    public void testXrefLookup() {
        String realDoi = "10.4137/cmc.s38446";
        JsonObject blob = underTest.retrieveMetadata(realDoi, xrefService);
        //these results will differ by a timestamp - but a good check is that they return the same journal objects
        JsonObject object = JsonTestObjects.xrefTestJsonObject();

        assertNotNull(blob.getJsonObject("message").getJsonArray("ISSN"));
        assertEquals(blob.getJsonObject("message").getJsonArray("ISSN"),
                     object.getJsonObject("message").getJsonArray("ISSN"));
    }

    /**
     * test that hitting the Unpaywall API with a doi returns the expected JSON object
     */
    @Test
    public void testUnpaywallLookup() {
        String realDoi = "10.4137/cmc.s38446"; //"10.1038/nature12373";
        JsonObject blob = underTest.retrieveMetadata(realDoi, unpaywallService);

        JsonObject object = JsonTestObjects.unpaywallTestJsonObject();

        assertNotNull(blob.getJsonString("doi"));
        assertEquals(blob.getJsonString("doi"), object.getJsonString("doi"));

    }

    /**
     * test that a bad doi gives the required error message
     */
    @Test
    public void testBadXrefDoiLookup() {
        String badDoi = "10.1212/abc.DEF";
        JsonObject blob = underTest.retrieveMetadata(badDoi, xrefService);
        assertEquals("Resource not found.", blob.getString("error"));
    }

    /**
     * test that a bad doi gives the required error message
     */
    @Test
    public void testBadUnpaywallDoiLookup() {
        String badDoi = "10.1212/abc.DEF";
        JsonObject blob = underTest.retrieveMetadata(badDoi, unpaywallService);
        assertEquals("true",blob.getValue("/error").toString());
        assertEquals("404", blob.getValue("/HTTP_status_code").toString());
    }

}

