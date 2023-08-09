/*
 *
 *  * Copyright 2023 Johns Hopkins University
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.eclipse.pass.client.nihms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.eclipse.pass.support.client.PassClient;
import org.eclipse.pass.support.client.model.Grant;
import org.eclipse.pass.support.client.model.Journal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NihmsPassClientServiceIT {

    private  NihmsPassClientService underTest;

    private PassClient passClient;

    @BeforeEach
    public void setUp() {
        System.setProperty("pass.core.url","http://localhost:8080");
        System.setProperty("pass.core.user","backend");
        System.setProperty("pass.core.password","backend");
        passClient = PassClient.newInstance();
        underTest = new NihmsPassClientService(passClient);
    }

    /**
     * Demonstrate that a Journal can be looked up by any of its ISSNs.
     */
    @Test
    public void lookupJournalByIssn() throws IOException {
        Journal journal = new Journal();
        journal.setIssns(Arrays.asList("fooissn", "barissn"));
        journal.setJournalName("My Journal");

        passClient.createObject(journal);
        String journalId = journal.getId();

        assertEquals(journalId, underTest.findJournalByIssn("fooissn"));
        assertEquals(journalId, underTest.findJournalByIssn("barissn"));

        // and that a lookup by a non-existent issn returns null.
        assertNull(underTest.findJournalByIssn("nonexistentissn"));
    }

    /**
     * Using different variants of the same award number, demonstrate that normalized award numbers are found using
     * different variants of an award number e.g. R01AR074846 should match R01AR074846-01A1 or 1R01AR074846-01
     * the activity code, institute code and serial number are the minimum set of strings required to match
     */
    @Test
    public void shouldFindNihGrantAwardNumber() throws IOException {
        Grant grant1 = new Grant();
        grant1.setAwardNumber("R01AR074846");
        grant1.setStartDate(ZonedDateTime.now());

        Grant grant2 = new Grant();
        grant2.setAwardNumber("UM1AI068613-01");
        grant2.setStartDate(ZonedDateTime.now());

        Grant grant3 = new Grant();
        grant3.setAwardNumber("K23HL151758");
        grant3.setStartDate(ZonedDateTime.now());

        Grant grant4 = new Grant();
        grant4.setAwardNumber("F32NS120940-01A1");
        grant4.setStartDate(ZonedDateTime.now());

        Grant grant5 = new Grant();
        grant5.setAwardNumber("1P50DA044123-B2");
        grant5.setStartDate(ZonedDateTime.now());

        Grant grant6 = new Grant();
        grant6.setAwardNumber("K23HL153778-1A1");
        grant6.setStartDate(ZonedDateTime.now());

        Grant grant7 = new Grant();
        grant7.setAwardNumber("5R01ES020425-05S2");
        grant7.setStartDate(ZonedDateTime.now());

        passClient.createObject(grant1);
        passClient.createObject(grant2);
        passClient.createObject(grant3);
        passClient.createObject(grant4);
        passClient.createObject(grant5);
        passClient.createObject(grant6);
        passClient.createObject(grant7);

        List<String> grant1Variants = Arrays.asList("R01AR074846", "R01 AR074846", "000-R01 AR074846",
                "000R01 AR074846", "1R01AR074846-A1", "1R01 AR074846-A1", "R01 AR074846-A1", "R01AR074846-A1",
                "R01AR074846-01S2");

        List<String> grant2Variants = Arrays.asList("UM1AI068613", "UM1 AI068613", "000-UM1 AI068613",
                "000UM1 AI068613", "1UM1AI068613-A1", "1UM1 AI068613-A1", "UM1 AI068613-A1", "UM1AI068613-A1",
                "UM1AI068613-01S2");

        List<String> grant3Variants = Arrays.asList("K23HL151758", "K23 HL151758", "000-K23 HL151758",
                "000K23 HL151758", "1K23HL151758-A1", "1K23 HL151758-A1", "K23 HL151758-A1", "K23HL151758-A1",
                "K23HL151758-01S2");

        List<String> grant4Variants = Arrays.asList("F32NS120940", "F32 NS120940", "000-F32 NS120940",
                "000F32 NS120940", "1F32NS120940-A1", "1F32 NS120940-A1", "F32 NS120940-A1", "F32NS120940-A1",
                "F32NS120940-01S2");

        List<String> grant5Variants = Arrays.asList("P50DA044123", "P50 DA044123", "000-P50 DA044123",
                "000P50 DA044123", "1P50DA044123-A1", "1P50 DA044123-A1", "P50 DA044123-A1", "P50DA044123-A1",
                "P50DA044123-01S2");

        List<String> grant6Variants = Arrays.asList("K23HL153778", "K23 HL153778", "000-K23 HL153778",
                "000K23 HL153778", "1K23HL153778-A1", "1K23 HL153778-A1", "K23 HL153778-A1", "K23HL153778-A1",
                "K23HL153778-01S2");

        List<String> grant7Variants = Arrays.asList("R01ES020425", "R01 ES020425", "000-R01 ES020425",
                "000R01 ES020425", "5R01ES020425-A1", "5R01 ES020425-A1", "5R01 ES020425-A1", "5R01ES020425-A1",
                "5R01ES020425-01S2");

        //test different variants of R01AR074846
        for (String variant : grant1Variants) {
            assertEquals(grant1.getAwardNumber(), underTest.findMostRecentGrantByAwardNumber(variant).getAwardNumber());
        }

        //test different variants of UM1AI068613
        for (String variant : grant2Variants) {
            assertEquals(grant2.getAwardNumber(), underTest.findMostRecentGrantByAwardNumber(variant).getAwardNumber());
        }

        //test different variants of K23HL151758
        for (String variant : grant3Variants) {
            assertEquals(grant3.getAwardNumber(), underTest.findMostRecentGrantByAwardNumber(variant).getAwardNumber());
        }

        //test different variants of F32NS120940-01A1
        for (String variant : grant4Variants) {
            assertEquals(grant4.getAwardNumber(), underTest.findMostRecentGrantByAwardNumber(variant).getAwardNumber());
        }

        //test different variants of K23HL153778-1A1
        for (String variant : grant5Variants) {
            assertEquals(grant5.getAwardNumber(), underTest.findMostRecentGrantByAwardNumber(variant).getAwardNumber());
        }

        //test different variants of P50DA044123-B2
        for (String variant : grant6Variants) {
            assertEquals(grant6.getAwardNumber(), underTest.findMostRecentGrantByAwardNumber(variant).getAwardNumber());
        }

        //test different variants of 5R01ES020425-05S2
        for (String variant : grant7Variants) {
            assertEquals(grant7.getAwardNumber(), underTest.findMostRecentGrantByAwardNumber(variant).getAwardNumber());
        }
    }

    /**
     * Generate grants based on a large list of known award numbers in PASS. The search should return the grant that
     * match the award numbers and not return any false positives or inadvertently modify the award number
     */
    @Test
    public void shouldFindNonNormalizedNihGrantAwardNumber() throws IOException, URISyntaxException {
        URI testAwardNumberUri = NihmsPassClientServiceTest.class.getResource("/valid_award_numbers.csv").toURI();
        List<String> awardNumbers = Files.readAllLines(Paths.get(testAwardNumberUri));

        for (String award : awardNumbers) {
            Grant grant = new Grant();
            grant.setAwardNumber(award);
            grant.setStartDate(ZonedDateTime.now());
            passClient.createObject(grant);
            Grant found = underTest.findMostRecentGrantByAwardNumber(award);
            assertEquals(award, found.getAwardNumber());
            assertEquals(grant.getId(), found.getId());
        }
    }

}
