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
import org.eclipse.pass.support.client.PassClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * Primary Spring Boot configuration class for Notification Services
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Configuration
public class SpringBootNotificationConfig {

    @Value("${pass.notification.mailer.debug}")
    private boolean mailerDebug;

    @Value("${pass.notification.configuration}")
    private Resource notificationConfigResource;

    @Bean
    public PassClient passClient() {
        // TODO will need pass client env vars
        return PassClient.newInstance();
    }

    @Bean
    public NotificationConfig notificationConfiguration(ObjectMapper objectMapper) throws IOException {
        return objectMapper.readValue(notificationConfigResource.getInputStream(), NotificationConfig.class);
    }

    @Bean
    public RecipientConfig recipientConfig(NotificationConfig config) {
        return config.getRecipientConfigs().stream()
            .filter(rc -> config.getMode() == rc.getMode()).findAny()
            .orElseThrow(() ->
                new RuntimeException("Missing recipient configuration for Mode '" + config.getMode() + "'"));
    }

}
