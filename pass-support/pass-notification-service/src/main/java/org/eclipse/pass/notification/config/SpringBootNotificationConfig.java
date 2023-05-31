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
package org.eclipse.pass.notification.config;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import org.eclipse.pass.notification.service.NotificationServiceErrorHandler;
import org.eclipse.pass.support.client.PassClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import javax.jms.Session;

/**
 * Primary Spring Boot configuration class for Notification Services
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Configuration
public class SpringBootNotificationConfig {

    @Value("${pass.notification.mode}")
    private Mode notificationMode;

    @Value("${pass.notification.configuration}")
    private Resource notificationConfigResource;

    @Bean
    public PassClient passClient() {
        return PassClient.newInstance();
    }

    @Bean
    public NotificationConfig notificationConfiguration(ObjectMapper objectMapper) throws IOException {
        NotificationConfig notificationConfig = objectMapper.readValue(notificationConfigResource.getInputStream(),
            NotificationConfig.class);
        notificationConfig.setMode(notificationMode);
        return notificationConfig;
    }

    @Bean
    public RecipientConfig recipientConfig(NotificationConfig config) {
        return config.getRecipientConfigs().stream()
            .filter(rc -> config.getMode() == rc.getMode()).findAny()
            .orElseThrow(() ->
                new RuntimeException("Missing recipient configuration for Mode '" + config.getMode() + "'"));
    }

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

}
