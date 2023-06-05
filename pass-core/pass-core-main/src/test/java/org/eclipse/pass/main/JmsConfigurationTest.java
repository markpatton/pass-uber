package org.eclipse.pass.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import javax.jms.JMSException;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.yahoo.elide.RefreshableElide;
import org.eclipse.pass.object.ElidePassClient;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.model.Deposit;
import org.eclipse.pass.object.model.DepositStatus;
import org.eclipse.pass.object.model.EventType;
import org.eclipse.pass.object.model.Submission;
import org.eclipse.pass.object.model.SubmissionEvent;
import org.eclipse.pass.object.model.SubmissionStatus;
import org.eclipse.pass.usertoken.BadTokenException;
import org.eclipse.pass.usertoken.Token;
import org.eclipse.pass.usertoken.TokenFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;

public class JmsConfigurationTest extends IntegrationTest {
    @Autowired
    private JmsTemplate jms;

    @Autowired
    protected RefreshableElide refreshableElide;

    @Autowired
    private JmsConfiguration jmsConfig;

    @Autowired
    private TokenFactory userTokenFactory;

    private PassClient client;

    private void clear_queue(String queue) throws JMSException {
        int count = count_queue(queue);

        while (count-- > 0) {
            jms.receive(queue).acknowledge();
        }
    }

    private int count_queue(String queue) {
        return jms.browse(queue, new BrowserCallback<Integer>() {
            @Override
            public Integer doInJms(final Session session, final QueueBrowser browser) throws JMSException {
                return Collections.list((Enumeration<?>)browser.getEnumeration()).size();
            }
        });
    }

    @BeforeEach
    public void setupClient() throws JMSException {
        // Use this implementation of PassClient in order to invoke hooks
        client = new ElidePassClient(refreshableElide);

        clear_queue(jmsConfig.getSubmissionQueue());
        clear_queue(jmsConfig.getSubmissionEventQueue());
        clear_queue(jmsConfig.getDepositQueue());
    }

    @AfterEach
    public void cleanupClient() throws IOException {
        client.close();
    }

    private JsonObject parse_json(String json) {
        try (JsonParser parser = Json.createParser(new StringReader(json))) {
            if (parser.hasNext() && parser.next() == Event.START_OBJECT) {
                return parser.getObject();
            }
        }

        fail("Expected JSON object: " + json);
        return null;
    }

    private JsonObject get_json_message(String queue, String message_type) throws JMSException {
        TextMessage mesg = (TextMessage) jms.receive(queue);
        mesg.acknowledge();

        assertEquals(message_type, mesg.getStringProperty(JmsConfiguration.MESSAGE_PROPERTY_TYPE_KEY));

        return parse_json(mesg.getText());
    }

    @Test
    public void testSubmissionMesssage() throws IOException, JMSException {
        Submission sub = new Submission();
        sub.setSubmitterName("Bob");
        sub.setSubmitted(true);

        client.createObject(sub);

        JsonObject result = get_json_message(jmsConfig.getSubmissionQueue(),
                JmsConfiguration.SUBMISSION_MESSAGE_TYPE);

        assertEquals(sub.getId().toString(), result.getString(JmsConfiguration.SUBMISSION_KEY));
        assertEquals(JmsConfiguration.SUBMISSION_MESSAGE_TYPE, result.getString(JmsConfiguration.TYPE_KEY));

        sub.setSubmissionStatus(SubmissionStatus.SUBMITTED);

        client.updateObject(sub);

        result = get_json_message(jmsConfig.getSubmissionQueue(),
                JmsConfiguration.SUBMISSION_MESSAGE_TYPE);

        assertEquals(sub.getId().toString(), result.getString(JmsConfiguration.SUBMISSION_KEY));
        assertEquals(JmsConfiguration.SUBMISSION_MESSAGE_TYPE, result.getString(JmsConfiguration.TYPE_KEY));
    }

    @Test
    public void testSubmissionEventMessage() throws JMSException, IOException {
        SubmissionEvent se = new SubmissionEvent();
        se.setComment("This is a comment");

        client.createObject(se);

        JsonObject result = get_json_message(jmsConfig.getSubmissionEventQueue(),
                JmsConfiguration.SUBMISSION_EVENT_MESSAGE_TYPE);

        assertEquals(se.getId().toString(), result.getString(JmsConfiguration.SUBMISSION_EVENT_KEY));
        assertEquals(JmsConfiguration.SUBMISSION_EVENT_MESSAGE_TYPE, result.getString(JmsConfiguration.TYPE_KEY));
    }

    @Test
    public void testSubmissionEventMessageWithApprovalLink() throws JMSException, IOException, BadTokenException {
        Submission sub = new Submission();
        sub.setSubmitterEmail(URI.create("mailto:someone@example.com"));

        client.createObject(sub);

        SubmissionEvent se = new SubmissionEvent();
        se.setSubmission(sub);
        se.setComment("This submission needs approval from new user");
        se.setEventType(EventType.APPROVAL_REQUESTED_NEWUSER);
        se.setLink(URI.create("http://pass.example.com/ui"));

        client.createObject(se);

        JsonObject result = get_json_message(jmsConfig.getSubmissionEventQueue(),
                JmsConfiguration.SUBMISSION_EVENT_MESSAGE_TYPE);

        assertEquals(se.getId().toString(), result.getString(JmsConfiguration.SUBMISSION_EVENT_KEY));
        assertEquals(JmsConfiguration.SUBMISSION_EVENT_MESSAGE_TYPE, result.getString(JmsConfiguration.TYPE_KEY));

        String link = result.getString(JmsConfiguration.APPROVAL_LINK_KEY);

        Token token = userTokenFactory.fromUri(URI.create(link));

        assertEquals(sub.getId(), token.getPassResourceIdentifier());
        assertEquals("submission", token.getPassResourceType());
        assertEquals(sub.getSubmitterEmail(), token.getReference());
    }

    @Test
    public void testDepositMessage() throws JMSException, IOException {
        Deposit dep = new Deposit();
        dep.setDepositStatus(DepositStatus.ACCEPTED);

        client.createObject(dep);

        JsonObject result = get_json_message(jmsConfig.getDepositQueue(),
                JmsConfiguration.DEPOSIT_MESSAGE_TYPE);

        assertEquals(dep.getId().toString(), result.getString(JmsConfiguration.DEPOSIT_KEY));
        assertEquals(JmsConfiguration.DEPOSIT_MESSAGE_TYPE, result.getString(JmsConfiguration.TYPE_KEY));
    }
}