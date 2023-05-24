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
package org.eclipse.pass.notification.app.config;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import org.eclipse.pass.notification.dispatch.impl.email.HandlebarsParameterizer;
import org.eclipse.pass.notification.dispatch.impl.email.SimpleWhitelist;
import org.eclipse.pass.notification.model.config.Mode;
import org.eclipse.pass.notification.model.config.NotificationConfig;
import org.eclipse.pass.notification.model.config.RecipientConfig;
import org.eclipse.pass.notification.model.config.smtp.SmtpServerConfig;
import org.eclipse.pass.support.client.PassClient;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

/**
 * Primary Spring Boot configuration class for Notification Services
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Configuration
public class SpringBootNotificationConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SpringBootNotificationConfig.class);

    @Value("${pass.notification.configuration}")
    private Resource notificationConfiguration;

    @Value("${pass.notification.mailer.debug}")
    private boolean mailerDebug;

    @Bean
    public ObjectMapper springEnvObjectMapper(Environment env) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        SpringEnvStringDeserializer envDeserializer = new SpringEnvStringDeserializer(env);
        SpringEnvModeDeserializer enumDeserializer = new SpringEnvModeDeserializer(env);

        module.addDeserializer(String.class, envDeserializer);

        module.addDeserializer(Mode.class, enumDeserializer);
        mapper.registerModule(module);

        return mapper;
    }

    @Bean
    public NotificationConfig notificationConfig(ObjectMapper springEnvObjectMapper) throws IOException {
        return springEnvObjectMapper
                .readValue(notificationConfiguration.getInputStream(), NotificationConfig.class);
    }

    @Bean
    public PassClient passClient() {

        // PassClientDefault can't be injected with configuration; requires system properties be set.
        // If a system property is already set, allow it to override what is resolved by the Spring environment.

        // TODO will need pass client env vars

        return PassClient.newInstance();
    }

    @Bean
    public HandlebarsParameterizer handlebarsParameterizer(Handlebars handlebars, ObjectMapper objectMapper) {
        return new HandlebarsParameterizer(handlebars, objectMapper);
    }

    @Bean
    public Handlebars handlebars() {
        Handlebars handlebars = new Handlebars();
        handlebars.registerHelper("eq", ConditionalHelpers.eq);
        return handlebars.with(EscapingStrategy.NOOP);
    }

    @Bean
    public Mailer mailer(NotificationConfig config) {
        SmtpServerConfig smtpConfig = config.getSmtpConfig();
        Objects.requireNonNull(smtpConfig,
                               "Missing SMTP server configuration from '" + notificationConfiguration + "'");
        MailerBuilder.MailerRegularBuilder builder = MailerBuilder
                .withSMTPServerHost(smtpConfig.getHost())
                .withSMTPServerPort(Integer.parseInt(smtpConfig.getPort()))
                .withTransportStrategy(TransportStrategy.valueOf(smtpConfig.getSmtpTransport().toUpperCase()))
                .withDebugLogging(mailerDebug);

        if (smtpConfig.getSmtpUser() != null && smtpConfig.getSmtpUser().trim().length() > 0) {
            builder = builder.withSMTPServerUsername(smtpConfig.getSmtpUser())
                    .withSMTPServerPassword(smtpConfig.getSmtpPassword());
        }

        return builder.buildMailer();
    }

    @Bean
    public SimpleWhitelist simpleWhitelist(NotificationConfig config) {
        RecipientConfig recipientConfig = config.getRecipientConfigs().stream()
                .filter(rc -> config.getMode() == rc.getMode()).findAny()
                .orElseThrow(() ->
                        new RuntimeException("Missing recipient configuration for Mode '" + config.getMode() + "'"));

        return new SimpleWhitelist(recipientConfig);
    }
}
