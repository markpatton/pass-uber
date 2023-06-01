package org.eclipse.pass.notification.service;

import static org.apache.commons.io.IOUtils.resourceToString;
import static org.eclipse.pass.notification.model.Link.SUBMISSION_REVIEW_INVITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.eclipse.pass.notification.AbstractNotificationSpringTest;
import org.eclipse.pass.notification.model.Link;
import org.eclipse.pass.notification.util.PathUtil;
import org.eclipse.pass.support.client.PassClient;
import org.eclipse.pass.support.client.model.EventType;
import org.eclipse.pass.support.client.model.PerformerRole;
import org.eclipse.pass.support.client.model.Source;
import org.eclipse.pass.support.client.model.Submission;
import org.eclipse.pass.support.client.model.SubmissionEvent;
import org.eclipse.pass.support.client.model.User;
import org.eclipse.pass.support.client.model.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "pass.notification.mode=DEMO",
    "pass.notification.configuration=classpath:notification.json"
})
public class NotificationServiceIT extends AbstractNotificationSpringTest {
    private static final String SENDER = "noreply@pass.jh.edu";
    private static final String RECIPIENT = "staffWithNoGrants@jhu.edu";
    private static final String CC = "notification-demo-cc@jhu.edu";

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP);

    @Autowired private PassClient passClient;
    @Autowired private NotificationService notificationService;

    @Test
    public void postNewEvent() throws Exception {
        // This User prepares the submission on behalf of the Submission.submitter
        // Confusingly, this User has the ability to submit to PASS.  The authorization-related role of
        // User.Role.SUBMITTER should not be confused with the the logical role as a preparer of a submission.
        User preparer = new User();
        preparer.setEmail("emetsger@gmail.com");
        preparer.setDisplayName("Submission Preparer");
        preparer.setFirstName("Pre");
        preparer.setLastName("Parer");
        preparer.setRoles(List.of(UserRole.SUBMITTER));

        passClient.createObject(preparer);

        // The Submission as prepared by the preparer.
        // The preparer did not find the authorized submitter in PASS, so they filled in the email address of the
        // authorized submitter. Therefore, the Submission.submitter field will be null (because that *must* be a URI
        // to a User resource, and the User does not exist). The Submission.submitterEmail will be set to the email
        // address of the authorized submitter
        Submission submission = new Submission();
        submission.setMetadata(resourceToString("/" + PathUtil.packageAsPath(this.getClass()) +
                                                "/submission-metadata.json", StandardCharsets.UTF_8));
        submission.setPreparers(List.of(preparer));
        submission.setSource(Source.PASS);
        submission.setSubmitter(null);
        submission.setSubmitterEmail(URI.create("mailto:" + RECIPIENT));

        passClient.createObject(submission);

        // When this event is processed, the authorized submitter will recieve an email notification with a link that
        // will invite them to use PASS, and link the Submission to their newly created User (created when they login
        // to PASS for the first time)
        SubmissionEvent event = new SubmissionEvent();
        event.setSubmission(submission);
        event.setPerformerRole(PerformerRole.PREPARER);
        event.setPerformedBy(preparer);
        String comment = "How does this submission look?";
        event.setComment(comment);
        event.setEventType(EventType.APPROVAL_REQUESTED_NEWUSER);
        event.setPerformedDate(ZonedDateTime.now());

        String submissionId = submission.getId();

        Link link = new Link(URI.create(submissionId
                .replace("http://localhost", "https://pass.local")), SUBMISSION_REVIEW_INVITE);
        event.setLink(link.getHref());

        passClient.createObject(event);

        SubmissionEvent submissionEventMessage = new SubmissionEvent();
        submissionEventMessage.setId(event.getId());

        notificationService.notify(submissionEventMessage);

        List<MimeMessage> receivedMessages = Arrays.asList(greenMail.getReceivedMessages());
        assertEquals(1, receivedMessages.size());

        MimeMessage message = receivedMessages.get(0);
        String body = message.getContent().toString();
        assertTrue(message.getSubject().contains("Specific protein supplementation using soya"));
        assertTrue(message.getSubject().contains("approval"));
        assertEquals(SENDER, message.getFrom()[0].toString());
        assertEquals(CC, message.getRecipients(MimeMessage.RecipientType.CC)[0].toString());
        assertEquals(RECIPIENT, message.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        assertTrue(body.contains("https://pass.local"));
        assertTrue(body.contains("?userToken="));
        assertTrue(body.contains(submissionId));
        assertTrue(body.contains(comment));
    }
}
