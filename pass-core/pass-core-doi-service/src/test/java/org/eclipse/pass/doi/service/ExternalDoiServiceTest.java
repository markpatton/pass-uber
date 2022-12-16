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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExternalDoiServiceTest {

    ExternalDoiService underTest = new XrefDoiService();

    /**
     * Test that our verify method correctly handles the usual expected doi formats
     */
    @Test
    public void verifyTest() {
        String doi0 = "http://dx.doi.org/10.4137/cmc.s38446";
        assertEquals("10.4137/cmc.s38446", underTest.verify(doi0));

        String doi1 = "https://dx.doi.org/10.4137/cmc.s38446";
        assertEquals("10.4137/cmc.s38446", underTest.verify(doi1));

        String doi2 = "dx.doi.org/10.4137/cmc.s38446";
        assertEquals("10.4137/cmc.s38446", underTest.verify(doi2));

        String doi3 = "10.4137/cmc.s38446";
        assertEquals("10.4137/cmc.s38446", underTest.verify(doi3));

        String doi4 = "4137/cmc.s38446";
        assertNull(underTest.verify(doi4));
    }
}
