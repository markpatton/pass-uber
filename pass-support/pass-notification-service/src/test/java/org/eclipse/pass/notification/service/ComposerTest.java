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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.pass.notification.model.Link.SUBMISSION_REVIEW_INVITE;
import static org.eclipse.pass.notification.service.LinksTest.randomUri;
import static org.eclipse.pass.notification.service.LinksUtil.deserialize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.pass.notification.config.Mode;
import org.eclipse.pass.notification.config.RecipientConfig;
import org.eclipse.pass.notification.model.Link;
import org.eclipse.pass.notification.model.Notification;
import org.eclipse.pass.notification.model.NotificationParam;
import org.eclipse.pass.notification.model.NotificationType;
import org.eclipse.pass.support.client.model.EventType;
import org.eclipse.pass.support.client.model.Submission;
import org.eclipse.pass.support.client.model.SubmissionEvent;
import org.eclipse.pass.support.client.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class ComposerTest {

    private static final String RESOURCE_METADATA = "{\"title\":\"Specific protein supplementation using soya, " +
        "casein or whey differentially affects regional gut growth and luminal growth factor bioactivity in rats; " +
        "implications for the treatment of gut injury and stimulating repair\",\"journal-title\":\"Food & Function\"," +
        "\"volume\":\"9\",\"issue\":\"1\",\"abstract\":\"Differential enhancement of luminal growth factor " +
        "bioactivity and targeted regional gut growth occurs dependent on dietary protein supplement.\"," +
        "\"doi\":\"10.1039/c7fo01251a\",\"publisher\":\"Royal Society of Chemistry (RSC)\",\"authors\":\"[" +
        "{\\\"author\\\":\\\"Tania Marchbank\\\",\\\"orcid\\\":\\\"http://orcid.org/0000-0003-2076-9098\\\"}," +
        "{\\\"author\\\":\\\"Nikki Mandir\\\"},{\\\"author\\\":\\\"Denis Calnan\\\"}," +
        "{\\\"author\\\":\\\"Robert A. Goodlad\\\"},{\\\"author\\\":\\\"Theo Podas\\\"}," +
        "{\\\"author\\\":\\\"Raymond J. Playford\\\",\\\"orcid\\\":\\\"http://orcid.org/0000-0003-1235-8504\\\"}]\"}";

    private static final String NOTIFICATION_FROM_ADDRESS = "pass-production-noreply@jhu.edu";
    private static final List<String> NOTIFICATION_GLOBAL_CC_ADDRESS =
        Arrays.asList("pass@jhu.edu", "pass-prod-cc@jhu.edu");
    private static final List<String> NOTIFICATION_GLOBAL_BCC_ADDRESS = singletonList("pass-prod-bcc@jhu.edu");
    private static final List<Link> generatedSubmissionLinks = asList(
        new Link(randomUri(), "rel1"),
        new Link(randomUri(), "rel2"));

    private Composer composer;
    private RecipientConfig recipientConfig;
    private SubmissionLinkAnalyzer submissionLinkAnalyzer;
    private LinkValidator linkValidator;

    @BeforeEach
    public void setUp() throws Exception {

        recipientConfig = new RecipientConfig();
        recipientConfig.setMode(Mode.PRODUCTION);
        recipientConfig.setFromAddress(NOTIFICATION_FROM_ADDRESS);
        recipientConfig.setGlobalCc(NOTIFICATION_GLOBAL_CC_ADDRESS);
        recipientConfig.setGlobalBcc(NOTIFICATION_GLOBAL_BCC_ADDRESS);

        submissionLinkAnalyzer = mock(SubmissionLinkAnalyzer.class);
        when(submissionLinkAnalyzer.apply(any(), any())).thenReturn(generatedSubmissionLinks.stream());

        linkValidator = mock(LinkValidator.class);
        when(linkValidator.test(any())).thenReturn(true);

        composer = new Composer(recipientConfig,
            new RecipientAnalyzer(), submissionLinkAnalyzer, linkValidator, new ObjectMapper());
    }

    /**
     * test the notification that is composed by the business layer for requesting approval to submit where the
     * authorized submitter is not a {@code User} in PASS.
     */
    @Test
    public void testApprovalRequestedNewUser() {
        SubmissionEvent event = new SubmissionEvent();
        event.setEventType(EventType.APPROVAL_REQUESTED_NEWUSER);
        event.setId("test-sub-event-id");
        URI eventLink = randomUri();
        event.setLink(eventLink);
        URI userTokenLink = randomUri();
        event.setUserTokenLink(userTokenLink);

        Submission submission = new Submission();
        submission.setMetadata(RESOURCE_METADATA);
        submission.setId("test-sub-id");
        User submitter = new User("test-user-id");
        submitter.setEmail("test-user@test");
        submission.setSubmitter(submitter);
        event.setSubmission(submission);

        composer = new Composer(recipientConfig,
            new RecipientAnalyzer(), new SubmissionLinkAnalyzer(), linkValidator, new ObjectMapper());

        Notification notification = composer.apply(submission, event);

        verifyNotification(notification, submitter.getEmail(), NotificationType.SUBMISSION_APPROVAL_INVITE);

        String serializedLinks = notification.getParameters().get(NotificationParam.LINKS);
        List<Link> deserializedLinks = new ArrayList<>(deserialize(serializedLinks));
        assertEquals(1, deserializedLinks.size());
        assertEquals(userTokenLink, deserializedLinks.get(0).getHref());
        assertEquals(SUBMISSION_REVIEW_INVITE, deserializedLinks.get(0).getRel());
    }

    @Test
    public void testApprovalRequested() {
        SubmissionEvent event = new SubmissionEvent();
        event.setEventType(EventType.APPROVAL_REQUESTED);
        event.setId("test-sub-event-id");

        Submission submission = new Submission();
        submission.setId("test-sub-id");
        User submitter = new User("test-user-id");
        submitter.setEmail("test-user@test");
        submission.setMetadata(RESOURCE_METADATA);
        submission.setSubmitter(submitter);
        event.setSubmission(submission);

        Notification notification = composer.apply(submission, event);

        verifyNotification(notification, submitter.getEmail(), NotificationType.SUBMISSION_APPROVAL_REQUESTED);
        assertLinksPresent(notification, submission, event);
    }

    @Test
    public void testChangesRequested() {
        SubmissionEvent event = new SubmissionEvent();
        event.setEventType(EventType.CHANGES_REQUESTED);
        event.setId("test-sub-event-id");

        Submission submission = new Submission();
        submission.setId("test-sub-id");
        submission.setMetadata(RESOURCE_METADATA);
        User preparer = new User("test-user-id");
        preparer.setEmail("test-user-prep@test");
        submission.setPreparers(List.of(preparer));
        event.setSubmission(submission);

        Notification notification = composer.apply(submission, event);

        verifyNotification(notification, preparer.getEmail(), NotificationType.SUBMISSION_CHANGES_REQUESTED);
        assertLinksPresent(notification, submission, event);
    }

    @Test
    public void testSubmitted() {
        SubmissionEvent event = new SubmissionEvent();
        event.setEventType(EventType.SUBMITTED);
        event.setId("test-sub-event-id");

        Submission submission = new Submission();
        submission.setId("test-sub-id");
        submission.setMetadata(RESOURCE_METADATA);
        User preparer = new User("test-user-id");
        preparer.setEmail("test-user-prep@test");
        submission.setPreparers(List.of(preparer));
        event.setSubmission(submission);

        Notification notification = composer.apply(submission, event);

        verifyNotification(notification, preparer.getEmail(), NotificationType.SUBMISSION_SUBMISSION_SUBMITTED);
        assertLinksPresent(notification, submission, event);
    }

    @Test
    public void testCancelledByPreparer() {
        SubmissionEvent event = new SubmissionEvent();
        event.setEventType(EventType.CANCELLED);
        event.setId("test-sub-event-id");

        Submission submission = new Submission();
        submission.setId("test-sub-id");
        submission.setMetadata(RESOURCE_METADATA);
        User preparer = new User("test-user-id-1");
        preparer.setEmail("test-user-prep@test");
        submission.setPreparers(List.of(preparer));
        User submitter = new User("test-user-id-2");
        submitter.setEmail("test-user-sub@test");
        submission.setSubmitter(submitter);
        event.setPerformedBy(preparer);
        event.setSubmission(submission);

        Notification notification = composer.apply(submission, event);

        verifyNotification(notification, submitter.getEmail(), NotificationType.SUBMISSION_SUBMISSION_CANCELLED);
        assertLinksPresent(notification, submission, event);
    }

    @Test
    public void testCancelledBySubmitter() {
        SubmissionEvent event = new SubmissionEvent();
        event.setEventType(EventType.CANCELLED);
        event.setId("test-sub-event-id");

        Submission submission = new Submission();
        submission.setMetadata(RESOURCE_METADATA);
        submission.setId("test-sub-id");
        submission.setMetadata(RESOURCE_METADATA);
        User preparer = new User("test-user-id-1");
        preparer.setEmail("test-user-prep@test");
        submission.setPreparers(List.of(preparer));
        User submitter = new User("test-user-id-2");
        submitter.setEmail("test-user-sub@test");
        submission.setSubmitter(submitter);
        event.setPerformedBy(submitter);
        event.setSubmission(submission);

        Notification notification = composer.apply(submission, event);

        verifyNotification(notification, preparer.getEmail(), NotificationType.SUBMISSION_SUBMISSION_CANCELLED);
        assertLinksPresent(notification, submission, event);
    }

    private void assertLinksPresent(Notification notification, Submission submission, SubmissionEvent event) {

        // Make sure the submission link analyzer was called with the appropriate arguments
        verify(submissionLinkAnalyzer).apply(eq(submission), eq(event));

        // Make sure the generated links are attached.
        String serializedLinks = notification.getParameters().get(NotificationParam.LINKS);
        assertNotNull(serializedLinks);

        Collection<Link> deserializedLinks = deserialize(serializedLinks);
        assertEquals(generatedSubmissionLinks.size(), deserializedLinks.size());
        assertTrue(deserializedLinks.containsAll(generatedSubmissionLinks));
    }

    private void verifyNotification(Notification notification, String expectedTo, NotificationType expectedType) {
        Map<NotificationParam, String> params = notification.getParameters();
        assertNotNull(params);
        assertEquals(expectedTo, params.get(NotificationParam.TO));
        assertEquals(NOTIFICATION_FROM_ADDRESS, params.get(NotificationParam.FROM));
        assertEquals(String.join(",", NOTIFICATION_GLOBAL_CC_ADDRESS), params.get(NotificationParam.CC));
        assertEquals(String.join(",", NOTIFICATION_GLOBAL_BCC_ADDRESS), params.get(NotificationParam.BCC));
        assertEquals(RESOURCE_METADATA, params.get(NotificationParam.RESOURCE_METADATA));
        assertEquals(expectedType, notification.getType());
    }
}