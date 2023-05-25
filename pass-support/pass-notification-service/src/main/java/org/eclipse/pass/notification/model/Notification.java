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
package org.eclipse.pass.notification.model;

import java.util.Collection;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Encapsulates {@link Notification} metadata used to dispatch the notification.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Getter
@Setter
@EqualsAndHashCode
public class Notification {

    /**
     * The primary recipients of the notification, may URIs to a PASS {@code User} or 'mailto' URIs
     */
    private Collection<String> recipients;

    /**
     * The sender of the notification.
     */
    private String sender;

    /**
     * Additional recipients, may be URIs to PASS {@code User}s
     */
    private Collection<String> cc;

    /**
     * Additional recipients, must be RFC 822 email addresses.  URIs must not be used.
     */
    private Collection<String> bcc;

    /**
     * The type of {@link Notification}
     */
    private NotificationType type;

    /**
     * Parameter map used for resolving placeholders in notification templates
     *
     * @see NotificationParam
     */
    private Map<NotificationParam, String> parameters;

    /**
     * The ID to the {@code SubmissionEvent} this notification is in response to
     */
    private String eventId;

    /**
     * The ID to the PASS resource this notification is in response to; likely to be the same as the
     * {@code SubmissionEvent#submissionUri}
     */
    private String resourceId;

}
