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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.pass.notification.config.NotificationTemplateName;
import org.eclipse.pass.notification.dispatch.DispatchException;
import org.eclipse.pass.notification.model.Notification;
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

    private final SimpleWhitelist whitelist;
    private final JavaMailSender javaMailSender;

    MimeMessage compose(Notification notification,
                        Map<NotificationTemplateName, String> templates) throws MessagingException {
        if (notification.getSender() == null || notification.getSender().trim().length() == 0) {
            throw new DispatchException("Notification must not have a null or empty sender!", notification);
        }

        Collection<String> resolvedRecipients = new HashSet<>(notification.getRecipients());
        Collection<String> whitelistedRecipients = whitelist.apply(resolvedRecipients);
        String[] emailToAddresses = whitelistedRecipients.toArray(String[]::new);

        if (emailToAddresses.length == 0) {
            throw new DispatchException("Notification must not have a null or empty to address!", notification);
        }

        // Build the email
        String subject = templates.getOrDefault(NotificationTemplateName.SUBJECT, "");
        String body = templates.getOrDefault(NotificationTemplateName.BODY, "");
        String footer = templates.getOrDefault(NotificationTemplateName.FOOTER, "");
        String from = notification.getSender();

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false);
        helper.setFrom(from);
        helper.setTo(emailToAddresses);
        helper.setSubject(subject);
        helper.setText(join("\n\n", body, footer));

        // These should never be null in production; being defensive because some tests may not set them
        if (notification.getResourceId() != null) {
            message.setHeader(SUBMISSION_SMTP_HEADER, notification.getResourceId());
        }

        if (notification.getType() != null) {
            message.setHeader(NOTIFICATION_TYPE_SMTP_HEADER, notification.getType().toString());
        }

        setCcIfNeeded(helper, notification);
        setBccIfNeeded(helper, notification);

        return message;
    }

    private void setCcIfNeeded(MimeMessageHelper mimeMessageHelper,
                               Notification notification) throws MessagingException {
        if (notification.getCc() != null && notification.getCc().size() > 0) {
            String[] ccs = notification.getCc().toArray(new String[0]);
            mimeMessageHelper.setCc(ccs);
        }
    }

    private void setBccIfNeeded(MimeMessageHelper mimeMessageHelper,
                               Notification notification) throws MessagingException {
        if (notification.getBcc() != null && notification.getBcc().size() > 0) {
            String[] bccs = notification.getBcc().toArray(new String[0]);
            mimeMessageHelper.setBcc(bccs);
        }
    }

}
