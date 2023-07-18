/*
 * Copyright 2023 Johns Hopkins University
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
package org.eclipse.pass.main;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import javax.jms.ConnectionFactory;

import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

@TestPropertySource(properties = {
    "pass.jms.sqs=true",
    "pass.jms.embed=false",
    "aws.sqs.endpoint-override=http://testhost:8080"
})
public class JmsSqsEndpointConfigurationTest extends IntegrationTest {

    @Autowired private ConnectionFactory connectionFactory;

    @Test
    public void testSqsEndpointOverrideConfig()  {
        // WHEN
        SQSConnectionFactory sqsConnectionFactory = (SQSConnectionFactory) connectionFactory;
        Object amazonSQSClientSupplier = ReflectionTestUtils.getField(sqsConnectionFactory,
            "amazonSQSClientSupplier");
        Object sqsClient = ReflectionTestUtils.invokeGetterMethod(amazonSQSClientSupplier, "get");
        URI endpoint = (URI) ReflectionTestUtils.getField(sqsClient, "endpoint");

        // THEN
        assertEquals(URI.create("http://testhost:8080"), endpoint);
    }
}