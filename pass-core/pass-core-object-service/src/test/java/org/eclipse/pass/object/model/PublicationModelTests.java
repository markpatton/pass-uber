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

import static org.eclipse.pass.object.model.support.TestObjectCreator.createPublication;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.pass.object.model.support.TestValues;
import org.junit.Test;

/**
 * Model has been annotated with JSON tags. These tests do a simple check to ensure the
 * Jackson integration is functional and the equals / hashcode functions work
 *
 * @author Karen Hanson
 * @author Jim Martino
 */
public class PublicationModelTests {

    /**
     * Creates two identical Publication and checks the equals and hashcodes match.
     * Modifies one field on one of the publications and verifies they no longer are
     * equal or have matching hashcodes.
     *
     * @throws Exception
     */
    @Test
    public void testSubmissionEqualsAndHashCode() throws Exception {
        Publication publication1 = createPublication(TestValues.PUBLICATION_ID_1);
        Publication publication2 = createPublication(TestValues.PUBLICATION_ID_1);

        assertEquals(publication1, publication2);
        publication1.setIssue("different");
        assertTrue(!publication1.equals(publication2));

        assertTrue(publication1.hashCode() != publication2.hashCode());
        publication1 = publication2;
        assertEquals(publication1.hashCode(), publication2.hashCode());

    }

    /**
     * Test copy constructor creates a valid duplicate that is not the same object
     *
     * @throws Exception
     */
    @Test
    public void testPublicationCopyConstructor() throws Exception {
        Publication publication = createPublication(TestValues.PUBLICATION_ID_1);
        Publication publicationCopy = new Publication(publication);
        assertEquals(publication, publicationCopy);

        String newDoi = "different:doi";
        publicationCopy.setDoi(newDoi);
        assertEquals(TestValues.PUBLICATION_DOI, publication.getDoi());
        assertEquals(newDoi, publicationCopy.getDoi());

        String newVolume = "abcdef";
        publicationCopy.setVolume(newVolume);
        assertEquals(TestValues.PUBLICATION_VOLUME, publication.getVolume());
        assertEquals(newVolume, publicationCopy.getVolume());
    }

}
