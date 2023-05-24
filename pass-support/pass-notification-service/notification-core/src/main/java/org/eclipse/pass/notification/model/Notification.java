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

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Encapsulates {@link Notification} metadata used to dispatch the notification.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
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

    public Collection<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(Collection<String> recipients) {
        this.recipients = recipients;
    }

    public Collection<String> getCc() {
        return cc;
    }

    public void setCc(Collection<String> cc) {
        this.cc = cc;
    }

    public Collection<String> getBcc() {
        return bcc;
    }

    public void setBcc(Collection<String> bcc) {
        this.bcc = bcc;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Map<NotificationParam, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<NotificationParam, String> parameters) {
        this.parameters = parameters;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return new StringJoiner("\n  ", Notification.class.getSimpleName() + "[", "]")
                .add("recipients=" + recipients)
                .add("sender='" + sender + "'")
                .add("cc=" + cc)
                .add("bcc=" + bcc)
                .add("type=" + type)
                .add("parameters=" + parameters)
                .add("eventUri=" + eventUri)
                .add("resourceUri=" + resourceUri)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Notification that = (Notification) o;
        return Objects.equals(recipients, that.recipients) &&
                Objects.equals(sender, that.sender) &&
                Objects.equals(cc, that.cc) &&
                Objects.equals(bcc, that.bcc) &&
                type == that.type &&
                Objects.equals(parameters, that.parameters) &&
                Objects.equals(eventUri, that.eventUri) &&
                Objects.equals(resourceUri, that.resourceUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipients, sender, cc, bcc, type, parameters, eventUri, resourceUri);
    }
}
