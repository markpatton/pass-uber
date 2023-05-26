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
package org.eclipse.pass.notification.dispatch.email;

import static java.lang.String.join;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.pass.notification.dispatch.DispatchException;
import org.eclipse.pass.notification.model.Notification;
import org.eclipse.pass.notification.config.NotificationTemplateName;
import org.eclipse.pass.support.client.PassClient;
import org.eclipse.pass.support.client.model.User;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Slf4j
@AllArgsConstructor
@Component
public class EmailComposer {

    public static final String SUBMISSION_SMTP_HEADER = "X-PASS-Submission-ID";
    public static final String NOTIFICATION_TYPE_SMTP_HEADER = "X-PASS-Notification-Type";

    // TODO create a PassClientFactory that gets passed in and used to create PassClient
    private final PassClient passClient;
    private final SimpleWhitelist whitelist;
    private final JavaMailSender javaMailSender;

    MimeMessage compose(Notification n, Map<NotificationTemplateName, String> templates) throws MessagingException {
        if (n.getSender() == null || n.getSender().trim().length() == 0) {
            throw new DispatchException("Notification must not have a null or empty sender!", n);
        }

        // Resolve the Notification Recipient URIs to email addresses, then apply the whitelist of email addresses

        Set<URI> recipientUris = n.getRecipients()
                .stream().map(URI::create).collect(Collectors.toSet());

        log.debug("Initial recipients: [{}]", join(",",
                   recipientUris.stream().map(URI::toString).collect(Collectors.toSet())));

        Collection<String> resolvedRecipients = recipientUris
                .stream()
                .map(uri -> {
                    if (uri.getScheme() != null && uri.getScheme().startsWith("http")) {
                        // TODO come back to this
//                        User user = passClient.readResource(uri, User.class);
                        User user = new User("foobar");
                        return user.getEmail();
                    }

                    return uri.getSchemeSpecificPart();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        log.debug("Applying whitelist to resolved recipients: [{}]", join(",", resolvedRecipients));

        Collection<String> whitelistedRecipients = whitelist.apply(resolvedRecipients);

        log.debug("Whitelisted recipients: [{}]", join(", ", whitelistedRecipients));

        String[] emailToAddresses = whitelistedRecipients.toArray(String[]::new);

        if (emailToAddresses.length == 0) {
            throw new DispatchException("Notification must not have a null or empty to address!", n);
        }

        // Build the email
        String subject = templates.getOrDefault(NotificationTemplateName.SUBJECT, "");
        String body = templates.getOrDefault(NotificationTemplateName.BODY, "");
        String footer = templates.getOrDefault(NotificationTemplateName.FOOTER, "");
        String from = n.getSender();
        Optional<String> cc = buildCc(n);
        Optional<String> bcc = buildBcc(n);

        log.debug("Building email with the following:\n" +
                " {}: {}\n" +
                " {}: {}\n" +
                " {}: {}\n" +
                " {}: {}\n" +
                " {}: {}\n" +
                " {}: {}\n" +
                " {}: {}\n" +
                " {}: {}\n" +
                " {}: {}\n",
                SUBMISSION_SMTP_HEADER, n.getResourceId(),
                NOTIFICATION_TYPE_SMTP_HEADER, n.getType().toString(),
                "From", from,
                "To", emailToAddresses,
                "CC", cc.orElse("<CC not specified, will not be included in email>"),
                "BCC", bcc.orElse("<BCC not specified, will not be included in email>"),
                "Subject", subject,
                "body text", body,
                "footer text", footer);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);
        helper.setTo(emailToAddresses);
        helper.setSubject(subject);
        helper.setText(join("\n\n", body, footer));

        // These should never be null in production; being defensive because some tests may not set them
        if (n.getResourceId() != null) {
            message.setHeader(SUBMISSION_SMTP_HEADER, n.getResourceId());
        }

        if (n.getType() != null) {
            message.setHeader(NOTIFICATION_TYPE_SMTP_HEADER, n.getType().toString());
        }

        // TODO fix this
//        cc.ifPresent(ccValue -> {
//            helper.setCc(ccValue);
//        });
//        bcc.ifPresent(helper::setBcc);

        return message;
    }

    /**
     * Returns a comma-separated string of carbon copy addresses.  Handles the case of a CC configuration property being
     * present with no value.
     *
     * @param n the notification which may or may not contain carbon copy recipients
     * @return an optional string containing comma separated carbon copy recipients
     */
    private Optional<String> buildCc(Notification n) {
        // builder refuses to build the cc with an empty collection
        if (n.getCc() != null && n.getCc().size() > 0) {
            return csvString(n.getCc().stream());
        }

        return Optional.empty();
    }

    /**
     * Returns a comma-separated string of blind carbon copy addresses.  Handles the case of a BCC configuration
     * property being present with no value.
     *
     * @param n the notification which may or may not contain blind carbon copy recipients
     * @return an optional string containing comma separated blind carbon copy recipients
     */
    private Optional<String> buildBcc(Notification n) {
        // builder refuses to build the bcc with an empty collection
        if (n.getBcc() != null && n.getBcc().size() > 0) {
            return csvString(n.getBcc().stream());
        }

        return Optional.empty();
    }

    /**
     * A configuration with a non-existent "${pass.notification.demo.global.cc.address}" property will
     * result in a list with one element, an empty string.  Filter out any empty string values before
     * continuing.
     *
     * @param values stream of values, some of which may be the empty string
     * @return values joined by commas, with any empty values present in {@code values} removed
     */
    private static Optional<String> csvString(Stream<String> values) {
        String filtered = values
                .filter(v -> v.length() > 0)
                .collect(Collectors.joining(","));
        return Optional.of(filtered);
    }

}
