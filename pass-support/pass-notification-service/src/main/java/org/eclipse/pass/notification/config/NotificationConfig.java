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

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.pass.notification.config.smtp.SmtpServerConfig;
import org.eclipse.pass.notification.model.Notification;
import org.eclipse.pass.notification.model.NotificationTemplate;

/**
 * The Notification Services runtime configuration.
 * <p>
 * The configuration is parsed from a JSON resource identified by the environment variable or system property
 * {@code pass.notification.configuration}.  The resource must be identified as a Spring Resource URI (e.g.
 * {@code classpath:/notification.json}, {@code file:///notification.json}).
 * </p>
 *
 * @see <a href="https://docs.spring.io/spring/docs/5.1.1.RELEASE/spring-framework-reference/core.html#resources">
 *     Spring Resources</a>
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Getter
@Setter
@EqualsAndHashCode
public class NotificationConfig {

    /**
     * Runtime mode of Notification Services (e.g. "disabled", "demo", "production")
     * The runtime mode of Notification Services.  Portions of the configuration may be differentiated by mode,
     * for example, there can be a {@link RecipientConfig} per {@link Mode}.
     * <p>
     * The mode may be set in the configuration file, or set with an environment or system property variable named
     * {@code pass.notification.mode}.
     *  </p>
     */
    public Mode mode;

    /**
     * The email templates used for each {@link Notification#getType() notification type}.
     */
    private Collection<NotificationTemplate> templates;

    /**
     * Each Notification Service mode has a recipientConfig
     * The recipient configuration used for each {@link Mode} of Notification Services.
     * The active recipient configuration will have a {@link RecipientConfig#getMode() mode}
     * equal to {@link #getMode()}.
     */
    @JsonProperty("recipient-config")
    private Collection<RecipientConfig> recipientConfigs;

    /**
     * The settings used to send email notifications using SMTP.  Unlike other portions of this configuration, it is
     * <em>not</em> a function of {@link #getMode() the runtime mode}.
     */
    @JsonProperty("smtp")
    private SmtpServerConfig smtpConfig;

    @JsonProperty("link-validators")
    private Collection<LinkValidationRule> linkValidatorConfig;

}
