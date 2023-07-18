package org.eclipse.pass.main;

import java.net.URI;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import javax.jms.ConnectionFactory;
import javax.jms.TextMessage;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.yahoo.elide.RefreshableElide;
import com.yahoo.elide.annotation.LifeCycleHookBinding.Operation;
import com.yahoo.elide.annotation.LifeCycleHookBinding.TransactionPhase;
import com.yahoo.elide.core.dictionary.EntityDictionary;
import com.yahoo.elide.core.dictionary.Injector;
import com.yahoo.elide.core.lifecycle.LifeCycleHook;
import com.yahoo.elide.core.type.Type;
import com.yahoo.elide.core.utils.ClassScanner;
import com.yahoo.elide.core.utils.coerce.CoerceUtil;
import com.yahoo.elide.modelconfig.DynamicConfiguration;
import com.yahoo.elide.spring.config.ElideConfigProperties;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.pass.object.model.Deposit;
import org.eclipse.pass.object.model.EventType;
import org.eclipse.pass.object.model.Submission;
import org.eclipse.pass.object.model.SubmissionEvent;
import org.eclipse.pass.usertoken.KeyGenerator;
import org.eclipse.pass.usertoken.TokenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

/**
 * Configures Elide such that updates to Submission, SubmissionEvent, and Deposit send messages to a JMS broker.
 */
@Configuration
public class JmsConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(JmsConfiguration.class);

    static final String APPROVAL_LINK_KEY = "approval-link";
    static final String DEPOSIT_MESSAGE_TYPE = "DepositStatus";
    static final String SUBMISSION_MESSAGE_TYPE = "SubmissionReady";
    static final String SUBMISSION_KEY = "submission";
    static final String SUBMISSION_EVENT_MESSAGE_TYPE = "SubmissionEvent";
    static final String DEPOSIT_KEY = "deposit";
    static final String SUBMISSION_EVENT_KEY = "submission-event";
    static final String TYPE_KEY = "type";
    static final String MESSAGE_PROPERTY_TYPE_KEY = "type";

    @Value("${pass.jms.queue.submission}")
    private String submission_queue;

    @Value("${pass.jms.queue.submission-event}")
    private String submission_event_queue;

    @Value("${pass.jms.queue.deposit}")
    private String deposit_queue;

    @Value("${aws.sqs.endpoint-override:AWS_SQS_ENDPOINT_OVERRIDE}")
    private String awsSqsEndpointOverride;

    /**
     * @return name of queue for Submission object updates
     */
    String getSubmissionQueue() {
        return submission_queue;
    }

    /**
     * @return name of queue for SubmissionEvent object updates
     */
    String getSubmissionEventQueue() {
        return submission_event_queue;
    }

    /**
     * @return name of queue for Deposit object updates
     */
    String getDepositQueue() {
        return deposit_queue;
    }

    /**
     * Return TokenFactory configured with a key. Generate the key if it is not given.
     *
     * @param key token key or null
     * @param refreshableElide a RefreshableElide instance
     * @return TokenFactory
     */
    @Bean
    public TokenFactory userTokenFactory(@Value("${pass.usertoken.key:#{null}}") String key,
            RefreshableElide refreshableElide) {
        if (key == null || key.isEmpty()) {
            key = KeyGenerator.generateKey();

            LOG.error("No user token key specified, generating key");
        }

        return new TokenFactory(key);
    }

    /**
     * Provide a JMS connection to Amazon SQS if configured to do so.
     *
     * @param awsRegion AWS region
     * @return JmsListenerContainerFactory
     */
    @Bean
    @ConditionalOnProperty(name = "pass.jms.sqs", havingValue = "true")
    public ConnectionFactory jmsConnectionFactory(@Value("${aws.region}") String awsRegion) {
        AmazonSQSClientBuilder sqsClientBuilder = configureSqsBuilder(AmazonSQSClientBuilder.standard(), awsRegion);
        return new SQSConnectionFactory(new ProviderConfiguration(), sqsClientBuilder);
    }

    private AmazonSQSClientBuilder configureSqsBuilder(AmazonSQSClientBuilder sqsClientBuilder, String awsRegion) {
        if (StringUtils.isNotEmpty(awsSqsEndpointOverride)) {
            return sqsClientBuilder.withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(awsSqsEndpointOverride, awsRegion));
        }
        return sqsClientBuilder.withRegion(Regions.fromName(awsRegion));
    }

    /**
     * Optionally start a JMS broker
     *
     * @param url for the broker
     * @return BrokerService
     * @throws Exception on error creating broker
     */
    @Bean
    @ConditionalOnExpression("#{${pass.jms.embed} and !${pass.jms.sqs}}")
    public BrokerService brokerService(@Value("${spring.activemq.broker-url}") String url) throws Exception {
        BrokerService brokerService = new BrokerService();
        brokerService.setPersistent(false);
        brokerService.setUseJmx(false);
        brokerService.addConnector(url);
        brokerService.setUseShutdownHook(false);
        return brokerService;
    }

    /**
     * Override the EnitityDictionary bean in order to add hooks.
     *
     * @param injector the Injector
     * @param scanner the ClassScanner
     * @param optionalDynamicConfig the DynamicConfiguration
     * @param settings the ElideConfigProperties
     * @param entitiesToExclude the set of entities to exclude
     * @param jms the JmsTemplate used by the hooks
     * @param userTokenFactory the TokenFactory
     * @return configured EntityDictionary.
     */
    @Bean
    public EntityDictionary buildDictionary(Injector injector, ClassScanner scanner,
            Optional<DynamicConfiguration> optionalDynamicConfig, ElideConfigProperties settings,
            @Qualifier("entitiesToExclude") Set<Type<?>> entitiesToExclude, JmsTemplate jms,
            TokenFactory userTokenFactory) {

        EntityDictionary dictionary = new EntityDictionary(new HashMap<>(), new HashMap<>(), injector,
                CoerceUtil::lookup, entitiesToExclude, scanner);

        setupHooks(dictionary, jms, userTokenFactory);

        return dictionary;
    }

    private void setupHooks(EntityDictionary dictionary, JmsTemplate jms, TokenFactory userTokenFactory) {
        LifeCycleHook<SubmissionEvent> sub_event_hook = (op, phase, event, scope, changes) -> {
            send(jms, submission_event_queue, createMessage(event, userTokenFactory), SUBMISSION_EVENT_MESSAGE_TYPE);
        };

        LifeCycleHook<Submission> sub_hook = (op, phase, sub, scope, changes) -> {
            if (sub.getSubmitted() != null && sub.getSubmitted() == true) {
                send(jms, submission_queue, createMessage(sub), SUBMISSION_MESSAGE_TYPE);
            }
        };

        LifeCycleHook<Deposit> deposit_hook = (op, phase, dep, scope, changes) -> {
            send(jms, deposit_queue, createMessage(dep), DEPOSIT_MESSAGE_TYPE);
        };

        dictionary.bindTrigger(SubmissionEvent.class, Operation.CREATE, TransactionPhase.POSTCOMMIT, sub_event_hook,
                false);

        dictionary.bindTrigger(Submission.class, Operation.CREATE, TransactionPhase.POSTCOMMIT, sub_hook, false);
        dictionary.bindTrigger(Submission.class, Operation.UPDATE, TransactionPhase.POSTCOMMIT, sub_hook, false);

        dictionary.bindTrigger(Deposit.class, Operation.CREATE, TransactionPhase.POSTCOMMIT, deposit_hook, false);
        dictionary.bindTrigger(Deposit.class, Operation.UPDATE, TransactionPhase.POSTCOMMIT, deposit_hook, false);
    }

    private String createMessage(Submission s) {
        return Json.createObjectBuilder().add(SUBMISSION_KEY, s.getId().toString()).add(TYPE_KEY,
                SUBMISSION_MESSAGE_TYPE).build().toString();
    }

    private String getInvitationLink(SubmissionEvent ev, TokenFactory userTokenFactory) {
        Submission sub = ev.getSubmission();

        if (sub != null && sub.getSubmitterEmail() != null && ev.getLink() != null) {
            URI uri = userTokenFactory.forPassResource("submission", sub.getId(), sub.getSubmitterEmail())
                    .addTo(ev.getLink());

            return uri.toString();
        } else {
            LOG.warn("Cannot create an invitation link, missing required information: " + sub);
        }

        return null;
    }

    private String createMessage(SubmissionEvent ev, TokenFactory userTokenFactory) {
        JsonObjectBuilder ob = Json.createObjectBuilder().add(SUBMISSION_EVENT_KEY, ev.getId().toString()).add(TYPE_KEY,
                SUBMISSION_EVENT_MESSAGE_TYPE);

        if (ev.getEventType() == EventType.APPROVAL_REQUESTED_NEWUSER) {
            String link = getInvitationLink(ev, userTokenFactory);

            if (link != null) {
                ob.add(APPROVAL_LINK_KEY, link);
            }
        }

        return ob.build().toString();
    }

    private String createMessage(Deposit dep) {
        return Json.createObjectBuilder().add(DEPOSIT_KEY, dep.getId().toString()).add(TYPE_KEY,
                DEPOSIT_MESSAGE_TYPE).build().toString();
    }

    private void send(JmsTemplate jms, String queue, String text, String type) {
        jms.send(queue, ses -> {
            TextMessage msg = ses.createTextMessage(text);
            msg.setStringProperty(MESSAGE_PROPERTY_TYPE_KEY, type);
            return msg;
        });
    }
}
