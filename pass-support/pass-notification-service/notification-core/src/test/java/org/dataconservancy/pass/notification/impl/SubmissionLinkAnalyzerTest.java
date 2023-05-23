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

package org.dataconservancy.pass.notification.impl;

import static java.util.stream.Collectors.toList;
import static org.dataconservancy.pass.model.SubmissionEvent.EventType.APPROVAL_REQUESTED;
import static org.dataconservancy.pass.model.SubmissionEvent.EventType.APPROVAL_REQUESTED_NEWUSER;
import static org.dataconservancy.pass.model.SubmissionEvent.EventType.CANCELLED;
import static org.dataconservancy.pass.model.SubmissionEvent.EventType.CHANGES_REQUESTED;
import static org.dataconservancy.pass.model.SubmissionEvent.EventType.SUBMITTED;
import static org.dataconservancy.pass.notification.impl.LinksTest.randomUri;
import static org.dataconservancy.pass.notification.model.Link.Rels.SUBMISSION_REVIEW;
import static org.dataconservancy.pass.notification.model.Link.Rels.SUBMISSION_REVIEW_INVITE;
import static org.dataconservancy.pass.notification.model.Link.Rels.SUBMISSION_VIEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.dataconservancy.pass.model.Submission;
import org.dataconservancy.pass.model.SubmissionEvent;
import org.dataconservancy.pass.notification.model.Link;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author apb@jhu.edu
 */
@RunWith(MockitoJUnitRunner.class)
public class SubmissionLinkAnalyzerTest {

    @Mock
    UserTokenGenerator userTokenGenerator;

    private final Submission submission = new Submission();

    private final SubmissionEvent event = new SubmissionEvent();

    private List<Link> linksGivenToTokenGeneratorFunction;

    private List<Link> linksTransformedByTokenGeneratorFunction;

    private SubmissionLinkAnalyzer toTest;

    @Before
    public void setUp() {
        toTest = new SubmissionLinkAnalyzer(userTokenGenerator);

        submission.setId(randomUri());
        event.setId(randomUri());
        event.setLink(randomUri());

        // Capture links given to the token generator service, and the transformed results
        linksGivenToTokenGeneratorFunction = new ArrayList<>();
        linksTransformedByTokenGeneratorFunction = new ArrayList<>();

        when(userTokenGenerator.forSubmission(any())).thenAnswer(i -> {

            return (UnaryOperator<Link>) link -> {
                linksGivenToTokenGeneratorFunction.add(link);
                if (SUBMISSION_REVIEW_INVITE.equals(link.getRel())) {
                    final Link transformed = new Link(randomUri(), SUBMISSION_REVIEW_INVITE);
                    linksTransformedByTokenGeneratorFunction.add(transformed);
                    return transformed;
                }
                return link;
            };
        });
    }

    @Test
    public void aprovalRequestedNewUserTest() {

        event.setEventType(APPROVAL_REQUESTED_NEWUSER);

        final List<Link> generatedLinks = toTest.apply(submission, event).collect(toList());

        verify(userTokenGenerator).forSubmission(eq(submission));

        // Verify that the link generator service was at least given the expected invite link
        final Link originalInviteLink = new Link(event.getLink(), SUBMISSION_REVIEW_INVITE);
        assertTrue(linksGivenToTokenGeneratorFunction.contains(originalInviteLink));

        // Verify that the link generator service produced a transformed link
        // (i.e. the event link, as transformed by token generator).
        assertTrue(generatedLinks.containsAll(linksTransformedByTokenGeneratorFunction));

        assertEquals(1, generatedLinks.size());
        assertEquals(SUBMISSION_REVIEW_INVITE, generatedLinks.get(0).getRel());
    }

    @Test
    public void aprovalRequestedNewUserLinkMissingTest() {
        event.setLink(null);
        event.setEventType(APPROVAL_REQUESTED_NEWUSER);

        try {
            toTest.apply(submission, event);
            fail("Should have thrown an exception");
        } catch (final NullPointerException e) {
            // The error message should identify the offending submissionEvent
            assertTrue(e.getMessage().contains(event.getId().toString()));
        }
    }

    @Test
    public void approvalRequestedExistingUserTest() {
        event.setEventType(APPROVAL_REQUESTED);

        final List<Link> generatedLinks = toTest.apply(submission, event).collect(toList());

        assertEquals(1, generatedLinks.size());
        assertEquals(event.getLink(), generatedLinks.get(0).getHref());
        assertEquals(SUBMISSION_REVIEW, generatedLinks.get(0).getRel());
    }

    @Test
    public void aprovalRequestedExistingUserLinkMissingTest() {
        event.setLink(null);
        event.setEventType(APPROVAL_REQUESTED);

        try {
            toTest.apply(submission, event);
            fail("Should have thrown an exception");
        } catch (final NullPointerException e) {
            // The error message should identify the offending submissionEvent
            assertTrue(e.getMessage().contains(event.getId().toString()));
        }
    }

    @Test
    public void changesRequestedTest() {
        event.setEventType(CHANGES_REQUESTED);

        final List<Link> generatedLinks = toTest.apply(submission, event).collect(toList());

        assertEquals(1, generatedLinks.size());
        assertEquals(event.getLink(), generatedLinks.get(0).getHref());
        assertEquals(SUBMISSION_REVIEW, generatedLinks.get(0).getRel());
    }

    @Test
    public void changesRequestedLinkMissingTest() {
        event.setEventType(CHANGES_REQUESTED);
        event.setLink(null);

        try {
            toTest.apply(submission, event);
            fail("Should have thrown an exception");
        } catch (final NullPointerException e) {
            // The error message should identify the offending submissionEvent
            assertTrue(e.getMessage().contains(event.getId().toString()));
        }
    }

    @Test
    public void submittedTest() {
        event.setEventType(SUBMITTED);

        final List<Link> generatedLinks = toTest.apply(submission, event).collect(toList());

        assertEquals(1, generatedLinks.size());
        assertEquals(event.getLink(), generatedLinks.get(0).getHref());
        assertEquals(SUBMISSION_VIEW, generatedLinks.get(0).getRel());
    }

    @Test
    public void submitteOptionalLinkMissingTest() {
        event.setEventType(SUBMITTED);
        event.setLink(null);

        assertTrue(toTest.apply(submission, event).collect(toList()).isEmpty());
    }

    @Test
    public void cancelledTest() {
        event.setEventType(CANCELLED);

        final List<Link> generatedLinks = toTest.apply(submission, event).collect(toList());

        assertEquals(1, generatedLinks.size());
        assertEquals(event.getLink(), generatedLinks.get(0).getHref());
        assertEquals(SUBMISSION_VIEW, generatedLinks.get(0).getRel());
    }

    @Test
    public void cancelledOptionalLinkMissingTest() {
        event.setEventType(CANCELLED);
        event.setLink(null);

        assertTrue(toTest.apply(submission, event).collect(toList()).isEmpty());
    }

    @Test
    public void nullSubmissionEventTest() {
        event.setEventType(null);
        event.setLink(null);

        assertTrue(toTest.apply(submission, event).collect(toList()).isEmpty());
    }
}
