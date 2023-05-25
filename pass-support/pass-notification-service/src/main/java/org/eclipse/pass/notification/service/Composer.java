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
package org.eclipse.pass.notification.service;

import static java.lang.String.join;
import static org.eclipse.pass.notification.service.Composer.RecipientConfigFilter.modeFilter;
import static org.eclipse.pass.notification.service.LinksUtil.concat;
import static org.eclipse.pass.notification.service.LinksUtil.serialized;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.pass.notification.config.NotificationConfig;
import org.eclipse.pass.notification.config.RecipientConfig;
import org.eclipse.pass.notification.model.Notification;
import org.eclipse.pass.notification.model.NotificationParam;
import org.eclipse.pass.notification.model.NotificationType;
import org.eclipse.pass.support.client.model.EventType;
import org.eclipse.pass.support.client.model.Submission;
import org.eclipse.pass.support.client.model.SubmissionEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * Composes a {@link Notification} from a {@link SubmissionEvent} and its corresponding {@link Submission}, according
 * to a {@link RecipientConfig}.  Responsible for determining the type of notification, and who the recipients and
 * sender of the notification are.  It is also responsible for populating other parameters of the notification such as
 * resource metadata, link metadata, or event metadata.
 * <p>
 * The implementation applies a whitelist to the recipients of the notification according to the recipient
 * configuration.  If the recipient configuration has a null or empty whitelist, that means that *all* recipients are
 * whitelisted (each recipient will receive the notification).  If the recipient configuration has a non-empty
 * whitelist, then only those users specified in the whitelist will receive a notification.  (N.B. the global CC field
 * of the recipient configuration is not run through the whitelist).
 * </p>
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 * @see RecipientConfig
 * @see NotificationType
 * @see NotificationParam
 */
@Slf4j
@AllArgsConstructor
@Component
public class Composer implements BiFunction<Submission, SubmissionEvent, Notification> {

    private final RecipientConfig recipientConfig;
    private final RecipientAnalyzer recipientAnalyzer;
    private final SubmissionLinkAnalyzer submissionLinkAnalyzer;
    private final LinkValidator linkValidator;
    private final ObjectMapper mapper;

    /**
     * Composes a {@code Notification} from a {@code Submission} and {@code SubmissionEvent}.
     *
     * @param submission
     * @param event
     * @return
     */
    @Override
    public Notification apply(Submission submission, SubmissionEvent event) {
        Objects.requireNonNull(submission, "Submission must not be null.");
        Objects.requireNonNull(event, "Event must not be null.");

        if (!event.getSubmission().getId().equals(submission.getId())) {
            // todo: exception?
            log.warn("Composing a Notification for tuple [{},{}] but {} references a different Submission: {}.",
                    submission.getId(), event.getId(), event.getId(), event.getSubmission());
        }

        Notification notification = new Notification();
        HashMap<NotificationParam, String> params = new HashMap<>();
        notification.setParameters(params);

        notification.setEventId(event.getId());
        params.put(NotificationParam.EVENT_METADATA, eventMetadata(event, mapper));

        Collection<String> cc = recipientConfig.getGlobalCc();
        if (cc != null && !cc.isEmpty()) {
            notification.setCc(cc);
            params.put(NotificationParam.CC, join(",", cc));
        }

        Collection<String> bcc = recipientConfig.getGlobalBcc();
        if (bcc != null && !bcc.isEmpty()) {
            notification.setBcc(bcc);
            params.put(NotificationParam.BCC, join(",", bcc));
        }

        notification.setResourceId(submission.getId());
        params.put(NotificationParam.RESOURCE_METADATA, resourceMetadata(submission, mapper));

        String from = recipientConfig.getFromAddress();
        notification.setSender(from);
        params.put(NotificationParam.FROM, from);

        Collection<String> recipients = recipientAnalyzer.apply(submission, event);
        notification.setRecipients(recipients);
        params.put(NotificationParam.TO, join(",", recipients));

        params.put(NotificationParam.LINKS, concat(submissionLinkAnalyzer.apply(submission, event))
                .filter(linkValidator)
                .collect(serialized()));

        EventType eventType = event.getEventType();
        NotificationType notificationType = NotificationType.findForEventType(eventType);
        notification.setType(notificationType);

        return notification;
    }

    static RecipientConfig getRecipientConfig(NotificationConfig config) {
        return config.getRecipientConfigs().stream()
                .filter(modeFilter(config)).findAny()
                .orElseThrow(() ->
                        new RuntimeException("Missing recipient configuration for Mode '" + config.getMode() + "'"));
    }

    public static String resourceMetadata(Submission submission, ObjectMapper mapper) {
        String metadata = submission.getMetadata();
        if (metadata == null || metadata.trim().length() == 0) {
            return "{}";
        }
        JsonNode metadataNode = null;
        try {
            metadataNode = mapper.readTree(metadata);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ObjectNode resourceMetadata = mapper.createObjectNode();
        resourceMetadata.put("title", field("title", metadataNode).orElse(""));
        resourceMetadata.put("journal-title", field("journal-title", metadataNode).orElse(""));
        resourceMetadata.put("volume", field("volume", metadataNode).orElse(""));
        resourceMetadata.put("issue", field("issue", metadataNode).orElse(""));
        resourceMetadata.put("abstract", field("abstract", metadataNode).orElse(""));
        resourceMetadata.put("doi", field("doi", metadataNode).orElse(""));
        resourceMetadata.put("publisher", field("publisher", metadataNode).orElse(""));
        resourceMetadata.put("authors", field("authors", metadataNode).orElse(""));
        return resourceMetadata.toString();
    }

    public static String eventMetadata(SubmissionEvent event, ObjectMapper mapper) {
        ObjectNode eventMetadata = mapper.createObjectNode();
        eventMetadata.put("id", field("id", event).orElse(""));
        eventMetadata.put("comment", field("comment", event).orElse(""));
        eventMetadata.put("performedDate", field("performedDate", event).orElse(""));
        eventMetadata.put("performedBy", field("performedBy", event).orElse(""));
        eventMetadata.put("performerRole", field("performerRole", event).orElse(""));
        eventMetadata.put("performedDate", field("performedDate", event).orElse(""));
        eventMetadata.put("eventType", field("eventType", event).orElse(""));
        return eventMetadata.toString();
    }

    static class RecipientConfigFilter {

        /**
         * Selects the correct {@link RecipientConfig} for the current {@link NotificationConfig#mode mode} of Notification Services.
         * @param config the Notification Services runtime configuration
         * @return the current mode's {@code RecipientConfig}
         */
        static Predicate<RecipientConfig> modeFilter(NotificationConfig config) {
            return (rc) -> config.getMode() == rc.getMode();
        }

    }

    static Optional<String> field(String fieldname, JsonNode metadata) {
        Optional<JsonNode> node = Optional.ofNullable(metadata.findValue(fieldname));
        if (node.isPresent() && node.get().isArray()) {
            return node.map(Objects::toString);
        }
        return node.map(JsonNode::asText);
    }

    static Optional<String> field(String fieldname, SubmissionEvent event) {
        Optional<Field> field = Optional.ofNullable(ReflectionUtils.findField(SubmissionEvent.class, fieldname));
        return field
                .map(f -> {
                    ReflectionUtils.makeAccessible(f);
                    return f;
                })
                .map(f -> ReflectionUtils.getField(f, event))
                .map(Object::toString);
    }
}
