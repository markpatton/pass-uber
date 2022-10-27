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

import static org.eclipse.pass.object.model.support.TestObjectCreator.createJournal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.pass.object.model.support.TestValues;
import org.junit.Test;

/**
 * Model has been annotated with JSON tags. These tests do a simple check to ensure the
 * Jackson integration is functional and the equals / hashcode functions work
 *
 * @author Karen Hanson
 * @author Jim Martino
 */
public class JournalModelTests {

    /**
     * Creates two identical Journals and checks the equals and hashcodes match.
     * Modifies one field on one of the journals and verifies they no longer are
     * equal or have matching hashcodes.
     *
     * @throws Exception
     */
    @Test
    public void testJournalEqualsAndHashCode() throws Exception {

        Journal journal1 = createJournal(TestValues.JOURNAL_ID_1);
        Journal journal2 = createJournal(TestValues.JOURNAL_ID_1);

        assertEquals(journal1, journal2);
        journal1.setJournalName("different");
        assertTrue(!journal1.equals(journal2));

        assertTrue(journal1.hashCode() != journal2.hashCode());
        journal1 = journal2;
        assertEquals(journal1.hashCode(), journal2.hashCode());

    }

    /**
     * Test copy constructor creates a valid duplicate that is not the same object
     *
     * @throws Exception
     */
    @Test
    public void testJournalCopyConstructor() throws Exception {
        Journal journal = createJournal(TestValues.JOURNAL_ID_1);
        List<String> issnsOrig = new ArrayList<String>(
            Arrays.asList(TestValues.JOURNAL_ISSN_1, TestValues.JOURNAL_ISSN_2));
        journal.setIssns(issnsOrig);
        Journal journalCopy = new Journal(journal);

        assertEquals(journal, journalCopy);

        journalCopy.setPmcParticipation(PmcParticipation.A);
        assertEquals(PmcParticipation.valueOf(TestValues.JOURNAL_PMCPARTICIPATION), journal.getPmcParticipation());
        assertEquals(PmcParticipation.A, journalCopy.getPmcParticipation());

        List<String> issnsNew = new ArrayList<String>(Arrays.asList("9876-1234"));
        journalCopy.setIssns(issnsNew);
        assertEquals(issnsOrig, journal.getIssns());
        assertEquals(issnsNew, journalCopy.getIssns());
    }

}
