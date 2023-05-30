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
package org.eclipse.pass.notification.service;

import static org.eclipse.pass.support.client.model.EventType.APPROVAL_REQUESTED;
import static org.eclipse.pass.support.client.model.EventType.APPROVAL_REQUESTED_NEWUSER;
import static org.eclipse.pass.support.client.model.EventType.CANCELLED;
import static org.eclipse.pass.support.client.model.EventType.CHANGES_REQUESTED;
import static org.eclipse.pass.support.client.model.EventType.SUBMITTED;
import static org.eclipse.pass.notification.service.LinksTest.randomUri;
import static org.eclipse.pass.notification.model.Link.SUBMISSION_REVIEW;
import static org.eclipse.pass.notification.model.Link.SUBMISSION_REVIEW_INVITE;
import static org.eclipse.pass.notification.model.Link.SUBMISSION_VIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.List;

import org.eclipse.pass.support.client.model.Submission;
import org.eclipse.pass.support.client.model.SubmissionEvent;
import org.eclipse.pass.notification.model.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author apb@jhu.edu
 */
public class SubmissionLinkAnalyzerTest {

    private final Submission submission = new Submission();
    private final SubmissionEvent event = new SubmissionEvent();

    private SubmissionLinkAnalyzer submissionLinkAnalyzer;

    @BeforeEach
    public void setUp() {
        submissionLinkAnalyzer = new SubmissionLinkAnalyzer();

        submission.setId("test-submission");
        event.setId("test-event");
        event.setLink(randomUri());
    }

    @Test
    public void testApprovalRequestedNewUser() {

        event.setEventType(APPROVAL_REQUESTED_NEWUSER);
        event.setUserTokenLink(URI.create("http://foobar"));

        final List<Link> generatedLinks = submissionLinkAnalyzer.apply(submission, event).toList();

        assertEquals(1, generatedLinks.size());
        assertEquals(event.getUserTokenLink(), generatedLinks.get(0).getHref());
        assertEquals(SUBMISSION_REVIEW_INVITE, generatedLinks.get(0).getRel());
    }

    @Test
    public void testApprovalRequestedNewUserLinkMissing() {
        event.setLink(null);
        event.setEventType(APPROVAL_REQUESTED_NEWUSER);

        NullPointerException e = assertThrows(NullPointerException.class, () -> {
            submissionLinkAnalyzer.apply(submission, event);
        });

        assertTrue(e.getMessage().contains(event.getId()));
    }

    @Test
    public void testApprovalRequestedExistingUser() {
        event.setEventType(APPROVAL_REQUESTED);

        final List<Link> generatedLinks = submissionLinkAnalyzer.apply(submission, event).toList();

        assertEquals(1, generatedLinks.size());
        assertEquals(event.getLink(), generatedLinks.get(0).getHref());
        assertEquals(SUBMISSION_REVIEW, generatedLinks.get(0).getRel());
    }

    @Test
    public void testApprovalRequestedExistingUserLinkMissing() {
        event.setLink(null);
        event.setEventType(APPROVAL_REQUESTED);

        NullPointerException e = assertThrows(NullPointerException.class, () -> {
            submissionLinkAnalyzer.apply(submission, event);
        });

        assertTrue(e.getMessage().contains(event.getId()));
    }

    @Test
    public void changesRequestedTest() {
        event.setEventType(CHANGES_REQUESTED);

        final List<Link> generatedLinks = submissionLinkAnalyzer.apply(submission, event).toList();

        assertEquals(1, generatedLinks.size());
        assertEquals(event.getLink(), generatedLinks.get(0).getHref());
        assertEquals(SUBMISSION_REVIEW, generatedLinks.get(0).getRel());
    }

    @Test
    public void changesRequestedLinkMissingTest() {
        event.setEventType(CHANGES_REQUESTED);
        event.setLink(null);

        NullPointerException e = assertThrows(NullPointerException.class, () -> {
            submissionLinkAnalyzer.apply(submission, event);
        });

        assertTrue(e.getMessage().contains(event.getId()));
    }

    @Test
    public void submittedTest() {
        event.setEventType(SUBMITTED);

        final List<Link> generatedLinks = submissionLinkAnalyzer.apply(submission, event).toList();

        assertEquals(1, generatedLinks.size());
        assertEquals(event.getLink(), generatedLinks.get(0).getHref());
        assertEquals(SUBMISSION_VIEW, generatedLinks.get(0).getRel());
    }

    @Test
    public void submitteOptionalLinkMissingTest() {
        event.setEventType(SUBMITTED);
        event.setLink(null);

        assertEquals(0, submissionLinkAnalyzer.apply(submission, event).count());
    }

    @Test
    public void cancelledTest() {
        event.setEventType(CANCELLED);

        final List<Link> generatedLinks = submissionLinkAnalyzer.apply(submission, event).toList();

        assertEquals(1, generatedLinks.size());
        assertEquals(event.getLink(), generatedLinks.get(0).getHref());
        assertEquals(SUBMISSION_VIEW, generatedLinks.get(0).getRel());
    }

    @Test
    public void cancelledOptionalLinkMissingTest() {
        event.setEventType(CANCELLED);
        event.setLink(null);

        assertEquals(0, submissionLinkAnalyzer.apply(submission, event).count());
    }

    @Test
    public void nullSubmissionEventTest() {
        event.setEventType(null);
        event.setLink(null);

        assertEquals(0, submissionLinkAnalyzer.apply(submission, event).count());
    }
}
