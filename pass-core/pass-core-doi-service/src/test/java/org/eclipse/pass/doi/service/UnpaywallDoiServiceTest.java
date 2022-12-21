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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UnpaywallDoiServiceTest {

    ExternalDoiService underTest = new UnpaywallDoiService();

    String unpaywallData =
        "{\"manuscripts\":[{\"url\":\"https://journals.sagepub.com/doi/pdf/10.4137/CMC.S38446\"," +
        "\"repositoryLabel\":null,\"type\":\"application/pdf\",\"source\":\"Unpaywall\"," +
        "\"name\":\"CMC.S38446\",\"isBest\":true},{\"url\":\"https://europepmc.org/articles/pmc5072460?pdf=render\"," +
        "\"repositoryLabel\":\"PubMed Central - Europe PMC\",\"type\":\"application/pdf\"," +
        "\"source\":\"Unpaywall\",\"name\":\"pmc5072460?pdf=render\",\"isBest\":false}," +
        "{\"url\":null,\"repositoryLabel\":null,\"type\":\"application/pdf\"," +
        "\"source\":\"Unpaywall\",\"name\":null,\"isBest\":false}]}";

    @Test
    public void testProcessObject() {

        assertEquals(unpaywallData,
            underTest.processObject(JsonTestObjects.unpaywallTestJsonObject()).toString());
    }

}
