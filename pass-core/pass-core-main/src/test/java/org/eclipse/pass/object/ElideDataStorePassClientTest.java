package org.eclipse.pass.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.eclipse.pass.object.model.AggregatedDepositStatus;
import org.eclipse.pass.object.model.Grant;
import org.eclipse.pass.object.model.Journal;
import org.eclipse.pass.object.model.PmcParticipation;
import org.eclipse.pass.object.model.Publisher;
import org.eclipse.pass.object.model.Source;
import org.eclipse.pass.object.model.Submission;
import org.eclipse.pass.object.model.SubmissionEvent;
import org.eclipse.pass.object.model.SubmissionStatus;
import org.eclipse.pass.object.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.yahoo.elide.RefreshableElide;

public class ElideDataStorePassClientTest extends IntegrationTest {
    @Autowired
    private RefreshableElide refreshableElide;

    private PassClient client;

    @BeforeAll
    public void setup() {
        client = new ElideDataStorePassClient(refreshableElide, null);
    }

    @AfterAll
    public void cleanup() throws IOException {
        client.close();
    }

    @Test
    public void testCreateObject() throws IOException {
        Journal journal = new Journal();
        journal.setJournalName("Test journal");
        journal.setPmcParticipation(PmcParticipation.A);

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
        for (int i = 0; i < num_grants; i++) {
            User pi = new User();

            pi.setDisplayName("user " + i);
            pi.setEmail("pi" + i + "@example.com");

            client.createObject(pi);

            Grant grant = new Grant();
            grant.setAwardDate(ZonedDateTime.now());
            grant.setAwardNumber("award:" + i);
            grant.setLocalKey("key:" + i);
            grant.setPi(pi);

            client.createObject(grant);
        }

        Publisher publisher = new Publisher();
        publisher.setName("This is the name of a publisher");
        client.createObject(publisher);

        PassClientResult<Grant> result = client.selectObjects(new PassClientSelector<>(Grant.class));

        assertEquals(num_grants, result.getEntities().size());
        assertEquals(num_grants, result.getTotal());

        result.getEntities().forEach(g -> {
            assertTrue(g.getPi().getDisplayName().startsWith("user"));
        });

        result = client.selectObjects(new PassClientSelector<>(Grant.class, 0, 5, null, null));

        assertEquals(5, result.getEntities().size());
        assertEquals(num_grants, result.getTotal());

        String filter = RSQL.equals("localKey", "key:3");
        result = client.selectObjects(new PassClientSelector<>(Grant.class, 0, 100, filter, "id"));

        assertEquals(1, result.getEntities().size());
        assertEquals(1, result.getTotal());
        assertEquals(result.getEntities().get(0).getAwardNumber(), "award:3");
    }
}
