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

import static org.eclipse.pass.object.model.support.TestObjectCreator.createRepository;
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
public class RepositoryModelTests {

    /**
     * Creates two identical Repositorys and checks the equals and hashcodes match.
     * Modifies one field on one of the repositorys and verifies they no longer are
     * equal or have matching hashcodes.
     *
     * @throws Exception
     */
    @Test
    public void testRepositoryEqualsAndHashCode() throws Exception {

        Repository repository1 = createRepository(TestValues.REPOSITORY_ID_1);
        Repository repository2 = createRepository(TestValues.REPOSITORY_ID_1);

        assertEquals(repository1, repository2);
        repository1.setName("different");
        assertTrue(!repository1.equals(repository2));

        assertTrue(repository1.hashCode() != repository2.hashCode());
        repository1 = repository2;
        assertEquals(repository1.hashCode(), repository2.hashCode());

    }

    /**
     * Test copy constructor creates a valid duplicate that is not the same object
     *
     * @throws Exception
     */
    @Test
    public void testRepositoryCopyConstructor() throws Exception {
        Repository repository = createRepository(TestValues.REPOSITORY_ID_1);
        Repository repositoryCopy = new Repository(repository);
        assertEquals(repository, repositoryCopy);

        String newAgreementText = "new agreement text";
        repositoryCopy.setAgreementText(newAgreementText);
        assertEquals(TestValues.REPOSITORY_AGREEMENTTEXT, repository.getAgreementText());
        assertEquals(newAgreementText, repositoryCopy.getAgreementText());

        repositoryCopy.setIntegrationType(IntegrationType.ONE_WAY);
        assertEquals(IntegrationType.of(TestValues.REPOSITORY_INTEGRATION_TYPE), repository.getIntegrationType());
        assertEquals(IntegrationType.ONE_WAY, repositoryCopy.getIntegrationType());
    }

}
