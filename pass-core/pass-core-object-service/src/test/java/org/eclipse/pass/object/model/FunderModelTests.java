/*
 * Copyright 2018 Johns Hopkins University
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
package org.eclipse.pass.object.model;

import static org.eclipse.pass.object.model.support.TestObjectCreator.createFunder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.eclipse.pass.object.model.support.TestValues;
import org.junit.jupiter.api.Test;

/**
 * Model has been annotated with JSON tags. These tests do a simple check to ensure the
 * Jackson integration is functional and the equals / hashcode functions work
 *
 * @author Karen Hanson
 * @author Jim Martino
 */
public class FunderModelTests {

    /**
     * Creates two identical Funders and checks the equals and hashcodes match.
     * Modifies one field on one of the Funders and verifies they no longer are
     * equal or have matching hashcodes.
     *
     * @throws Exception
     */
    @Test
    public void testFunderEqualsAndHashCode() throws Exception {

        Funder funder1 = createFunder(TestValues.FUNDER_ID_1);
        Funder funder2 = createFunder(TestValues.FUNDER_ID_1);

        assertEquals(funder1, funder2);
        assertEquals(funder1.hashCode(), funder2.hashCode());
        funder1.setName("different");
        assertTrue(!funder1.equals(funder2));
        assertTrue(funder1.hashCode() != funder2.hashCode());
    }

    /**
     * Test copy constructor creates a valid duplicate that is not the same object
     *
     * @throws Exception
     */
    @Test
    public void testFunderCopyConstructor() throws Exception {
        Funder funder = createFunder(TestValues.FUNDER_ID_1);
        Funder funderCopy = new Funder(funder);
        assertEquals(funder, funderCopy);

        String newLocalKey = "different:key";
        funderCopy.setLocalKey(newLocalKey);
        assertEquals(TestValues.FUNDER_LOCALKEY, funder.getLocalKey());
        assertEquals(newLocalKey, funderCopy.getLocalKey());

        URI newUrl = new URI("different:url");
        funderCopy.setUrl(newUrl);
        assertEquals(new URI(TestValues.FUNDER_URL), funder.getUrl());
        assertEquals(newUrl, funderCopy.getUrl());
    }

}
