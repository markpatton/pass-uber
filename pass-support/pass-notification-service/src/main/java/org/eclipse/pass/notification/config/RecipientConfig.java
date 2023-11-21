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

/**
 * Allows recipients of a notification to be configured depending on the Notification Service {@link #mode mode}.
 * Each Notification Service mode can have its own RecipientConfig, which can be used to control the dispatch of
 * notifications to users during testing or production.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 * @see Mode
 */
@Getter
@Setter
@EqualsAndHashCode
public class RecipientConfig {

    /**
     * The Notification Service mode (e.g. "demo", "prod") that this {@code RecipientConfiguration} applies to.
     * <p>
     * This configuration is considered active if the mode matches {@link NotificationConfig#getMode() the runtime
     * mode}.
     * </p>
     */
    private Mode mode;

    /**
     * All notifications for {@link #mode} will be blind carbon copied to these recipients.  Differs from
     * {@link #getGlobalCc()} as this method blind carbon-copies recipients.
     */
    @JsonProperty("global_cc")
    private Collection<String> globalCc;

    /**
     * All notifications for {@link #mode} will be blind carbon copied to these recipients.  Differs from
     * {@link #getGlobalCc()} as this method blind carbon-copies recipients.
     */
    @JsonProperty("global_bcc")
    private Collection<String> globalBcc;

    /**
     * Whitelisted recipients for {@link #mode} will receive notifications directly.  If the recipient on the
     * {@code Notification} matches a recipient on the whitelist, then the notification is delivered.
     * <p>
     * If a whitelist does not exist in the configuration, then it is treated as "allow all", or "matching *".  Any
     * recipient configured on the {@code Notification} will have notifications delivered.  A {@code null} whitelist is
     * what would be normally desired in production: each recipient will receive notifications.
     * </p>
     * <p>
     * If a whitelist does exist, then the recipient configured on the {@code Notification} will be used <em>only</em>
     * if they are on the whitelist.  This is a handy mode for demo purposes: only the identified recipients on the
     * whitelist can receive notifications.
     * </p>
     * <p>
     * In all cases, notifications are also sent to {@link #globalCc}.
     * </p>
     */
    private Collection<String> whitelist;

    /**
     * All notifications will appear to be delivered from this address
     */
    private String fromAddress;

}
