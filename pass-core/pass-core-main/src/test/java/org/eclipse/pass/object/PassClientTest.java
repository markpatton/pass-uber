/*
 * Copyright 2022 Johns Hopkins University
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
package org.eclipse.pass.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.eclipse.pass.object.model.AggregatedDepositStatus;
import org.eclipse.pass.object.model.Grant;
import org.eclipse.pass.object.model.Journal;
import org.eclipse.pass.object.model.PmcParticipation;
import org.eclipse.pass.object.model.Source;
import org.eclipse.pass.object.model.Submission;
import org.eclipse.pass.object.model.SubmissionEvent;
import org.eclipse.pass.object.model.SubmissionStatus;
import org.eclipse.pass.object.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

/**
 * Tests must be written such that they can run in any order and handle objects already existing.
 */
public abstract class PassClientTest extends IntegrationTest {
    protected PassClient client;

    @AfterAll
    public void cleanup() throws IOException {
        client.close();
    }

    @Test
    public void testCreateObject() throws IOException {
        Journal journal = new Journal();
        journal.setJournalName("Test journal");
        journal.setNlmta("hmm");
        journal.setPmcParticipation(PmcParticipation.A);
        journal.setIssns(List.of("test1", "test2"));
        client.createObject(journal);

        assertNotNull(journal.getId());

        Journal test = client.getObject(Journal.class, journal.getId());

        assertEquals(journal, test);
    }

    @Test
    public void testGetObjectNotExisting() throws IOException {
        Journal test = client.getObject(Journal.class, 10000000L);

        assertNull(test);
    }

    @Test
    public void testUpdateObject() throws IOException {
        Submission submission = new Submission();

        submission.setAggregatedDepositStatus(AggregatedDepositStatus.NOT_STARTED);
        submission.setSubmissionStatus(SubmissionStatus.DRAFT);
        submission.setSubmitterName("Bessie");

        client.createObject(submission);

        submission.setSource(Source.OTHER);
        submission.setSubmissionStatus(SubmissionStatus.SUBMITTED);

        client.updateObject(submission);

        Submission test = client.getObject(submission.getClass(), submission.getId());

        assertEquals(submission.getId(), test.getId());
        assertEquals(submission.getAggregatedDepositStatus(), test.getAggregatedDepositStatus());
        assertEquals(submission.getSubmitterName(), test.getSubmitterName());
        assertEquals(submission.getSource(), test.getSource());
        assertEquals(submission.getSubmissionStatus(), test.getSubmissionStatus());
        assertEquals(submission.getMetadata(), test.getMetadata());

        // The lazy loading of objects from relationships does not play nicely with equality tests
        // assertEquals(submission, test);
    }

    @Test
    public void testDeleteObject() throws IOException {
        SubmissionEvent ev = new SubmissionEvent();
        ev.setComment("This is a comment");

        client.createObject(ev);

        SubmissionEvent test = client.getObject(ev.getClass(), ev.getId());

        assertEquals(ev.getComment(), test.getComment());

        client.deleteObject(ev);

        test = client.getObject(ev.getClass(), ev.getId());

        assertNull(test);
    }

    @Test
    public void testSelectObjects() throws IOException {
        int num_grants = 10;

        // All grants created in this test will share the same localKey
        String key = "key: " + UUID.randomUUID();
        for (int i = 0; i < num_grants; i++) {
            User pi = new User();

            pi.setDisplayName("user " + i);
            pi.setEmail("pi" + i + "@example.com");

            client.createObject(pi);

            User copi1 = new User();
            copi1.setDisplayName("copi 1 of user " + i);
            copi1.setLastName("Boberson");

            client.createObject(copi1);

            User copi2 = new User();
            copi2.setDisplayName("copi 2 of user " + i);
            copi2.setLastName("Bobert");

            client.createObject(copi2);

            Grant grant = new Grant();
            grant.setAwardDate(ZonedDateTime.now());
            grant.setAwardNumber("award:" + i);
            grant.setLocalKey(key);
            grant.setPi(pi);
            grant.getCoPis().add(copi1);
            grant.getCoPis().add(copi2);

            client.createObject(grant);
        }

        String filter = RSQL.equals("localKey", key);
        PassClientResult<Grant> result = client.selectObjects(new PassClientSelector<>(Grant.class, 0,
                100, filter, null));

        assertEquals(num_grants, result.getEntities().size());
        assertEquals(num_grants, result.getTotal());

        result.getEntities().forEach(g -> {
            assertTrue(g.getPi().getDisplayName().startsWith("user"));
            assertEquals(2, g.getCoPis().size());
        });

        result = client.selectObjects(new PassClientSelector<>(Grant.class, 0, 5, filter, null));

        assertEquals(5, result.getEntities().size());
        assertEquals(num_grants, result.getTotal());

        filter = RSQL.and(RSQL.equals("localKey", key), RSQL.equals("awardNumber", "award:3"));
        result = client.selectObjects(new PassClientSelector<>(Grant.class, 0, 100, filter, "id"));

        assertEquals(1, result.getEntities().size());
        assertEquals(1, result.getTotal());
        assertEquals(result.getEntities().get(0).getAwardNumber(), "award:3");
    }
}
