package org.eclipse.pass.notification.service;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazon.sqs.javamessaging.SQSMessageConsumerPrefetch;
import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

public class AwsSqsTestConfig {

    static final String QUEUE_NAME = "test-event-queue";

    @Bean
    @Primary
    public ConnectionFactory testJmsConnectionFactory() {
        SqsClient sqsClient = testSqsClient();
        createTestEventQueue(sqsClient);
        return new SQSConnectionFactory(
            new ProviderConfiguration(),
            sqsClient
        );
    }

    private SqsClient testSqsClient() {
        /*
         * The strange code below to set WAIT_TIME_SECONDS to 2 seconds is so the Sqs message consumer
         * shuts down faster after the tests have finished.
         */
        ReflectionTestUtils.setField(SQSMessageConsumerPrefetch.class, "WAIT_TIME_SECONDS", 1);
        return SqsClient.builder()
            .endpointOverride(SubmissionEventListenerIT.LOCALSTACK_CONTAINER.getEndpoint())
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("localstackkeyid", "dummykey")
                )
            )
            .region(Region.of("us-east-1"))
            .build();
    }

    private void createTestEventQueue(SqsClient sqsClient) {
        CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
            .queueName(QUEUE_NAME)
            .build();
        sqsClient.createQueue(createQueueRequest);
    }

}
