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
import static org.eclipse.pass.notification.service.LinksTest.randomUri;
import static org.eclipse.pass.notification.service.LinksUtil.deserialize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.pass.notification.config.Mode;
import org.eclipse.pass.notification.config.RecipientConfig;
import org.eclipse.pass.notification.model.Link;
import org.eclipse.pass.notification.config.NotificationConfig;
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

    private Composer composer;

    private SubmissionLinkAnalyzer submissionLinkAnalyzer;

    private static final List<Link> generatedSubmissionLinks = asList(
            new Link(randomUri(), "rel1"),
            new Link(randomUri(), "rel2"));

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        Function<Collection<String>, Collection<String>> whitelister = mock(Function.class);
        NotificationConfig notificationConfig = mock(NotificationConfig.class);

        Mode runtimeMode = Mode.PRODUCTION;
        RecipientConfig recipientConfig = new RecipientConfig();
        recipientConfig.setMode(runtimeMode);
        recipientConfig.setFromAddress(NOTIFICATION_FROM_ADDRESS);
        recipientConfig.setGlobalCc(NOTIFICATION_GLOBAL_CC_ADDRESS);
        recipientConfig.setGlobalBcc(NOTIFICATION_GLOBAL_BCC_ADDRESS);

        // all recipients are whitelisted
        when(whitelister.apply(any())).thenAnswer(inv -> inv.getArgument(0));
        when(notificationConfig.getMode()).thenReturn(runtimeMode);
        when(notificationConfig.getRecipientConfigs()).thenReturn(singletonList(recipientConfig));

        ObjectMapper mapper = new ObjectMapper();

        submissionLinkAnalyzer = mock(SubmissionLinkAnalyzer.class);
        when(submissionLinkAnalyzer.apply(any(), any())).thenReturn(generatedSubmissionLinks.stream());

        LinkValidator linkValidator = mock(LinkValidator.class);
        when(linkValidator.test(any())).thenReturn(true);

        composer = new Composer(recipientConfig,
            new RecipientAnalyzer(), submissionLinkAnalyzer, linkValidator, mapper);
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

        Submission submission = new Submission();
        submission.setMetadata(RESOURCE_METADATA);
        submission.setId("test-sub-id");
        User submitter = new User("test-user-id");
        submitter.setEmail("test-user@test");
        submission.setSubmitter(submitter);
        event.setSubmission(submission);

        Notification notification = composer.apply(submission, event);

        verifyNotification(notification, submitter.getEmail(), NotificationType.SUBMISSION_APPROVAL_INVITE);
        assertLinksPresent(notification, submission, event);
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

    @Test
    public void testModeFilter() {
        RecipientConfig prod = new RecipientConfig();
        prod.setMode(Mode.PRODUCTION);

        RecipientConfig demo = new RecipientConfig();
        demo.setMode(Mode.DEMO);

        RecipientConfig disabled = new RecipientConfig();
        disabled.setMode(Mode.DISABLED);

        NotificationConfig config = new NotificationConfig();
        config.setRecipientConfigs(Arrays.asList(prod, demo, disabled));

        config.setMode(Mode.PRODUCTION);
        assertTrue(Composer.RecipientConfigFilter.modeFilter(config).test(prod));

        config.setMode(Mode.DEMO);
        assertTrue(Composer.RecipientConfigFilter.modeFilter(config).test(demo));

        config.setMode(Mode.DISABLED);
        assertTrue(Composer.RecipientConfigFilter.modeFilter(config).test(disabled));

        assertFalse(Composer.RecipientConfigFilter.modeFilter(config).test(demo));
        assertFalse(Composer.RecipientConfigFilter.modeFilter(config).test(prod));

        config.setMode(Mode.PRODUCTION);
        config.setRecipientConfigs(Arrays.asList(prod, demo));

        assertFalse(Composer.RecipientConfigFilter.modeFilter(config).test(disabled));
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