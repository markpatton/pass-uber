/*
 *
 *  * Copyright 2018 Johns Hopkins University
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.eclipse.pass.notification.config;

import javax.jms.Session;

import jakarta.jms.ConnectionFactory;
import org.eclipse.pass.notification.service.NotificationService;
import org.eclipse.pass.notification.service.NotificationServiceErrorHandler;
import org.eclipse.pass.support.client.model.SubmissionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

/**
 * JMS configuration for Notification Services.  Primary entry point to the Notification Services stack when in
 * production.
 * <p>
 * If {@link Mode} is equal to {@link Mode#DISABLED}, then Notification Services will drain any JMS messages in the
 * queue, and acknowledge (and immediately discard) any new messages it receives.
 * </p>
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
//@EnableJms
//@Configuration
public class JmsConfig {

    private static final Logger LOG = LoggerFactory.getLogger(JmsConfig.class);

    @Autowired
    private NotificationService notificationService;

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
        @Value("${spring.jms.listener.concurrency}")
            String concurrency,
        @Value("${spring.jms.listener.auto-startup}")
            boolean autoStart,
        ConnectionFactory connectionFactory,
        DefaultJmsListenerContainerFactoryConfigurer configurer,
        NotificationServiceErrorHandler errorHandler) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setErrorHandler(errorHandler);
        factory.setConcurrency(concurrency);
        factory.setAutoStartup(autoStart);
        return factory;
    }

    // TODO use selector prop to only select submission event messages
    @JmsListener(destination = "${pass.notification.queue.event.name}",
        containerFactory = "jmsListenerContainerFactory")
    public void processMessage(SubmissionEvent submissionEvent) {

        LOG.trace("Receiving SubmissionEvent: {}", submissionEvent.getId());

        // TODO do we need to keep the DISABLED mode?
//        if (Mode.DISABLED == config.getMode()) {
//            try {
//                LOG.trace("Discarding message {}, mode is {}", id, config.getMode());
//                jmsMessage.acknowledge();
//            } catch (JMSException e) {
//                LOG.warn("Error acknowledging JMS message {}: {}", id, e.getMessage(), e);
//            }
//            return;
//        }
        notificationService.notify(submissionEvent);
    }

}
