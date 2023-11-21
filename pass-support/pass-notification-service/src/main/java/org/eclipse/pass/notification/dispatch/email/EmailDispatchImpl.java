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

import java.util.Arrays;
import java.util.Map;

import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.pass.notification.config.NotificationConfig;
import org.eclipse.pass.notification.config.NotificationTemplate;
import org.eclipse.pass.notification.config.NotificationTemplateName;
import org.eclipse.pass.notification.dispatch.DispatchException;
import org.eclipse.pass.notification.dispatch.DispatchService;
import org.eclipse.pass.notification.model.Notification;
import org.eclipse.pass.notification.model.NotificationType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Dispatches {@link Notification}s as email messages.  Email templates are configured by {@link NotificationTemplate}s
 * obtained from the {@link NotificationConfig}.
 * <p>
 * Notification recipients are expected to be encoded as email addresses.
 * </p>
 * <p>
 * The whitelist of email addresses is applied to the recipient emails.  If no whitelist
 * is present, or if the whitelist is empty, all recipients are whitelisted.  If the whitelist is not empty, the
 * recipients of the notification are filtered, and only the whitelisted addresses will receive an email.
 * </p>
 * <p>
 * If a notification addresses multiple recipients in the {@code TO} field of a {@code Notification}, this
 * implementation does <em>not</em> send individual emails to each recipient.  It will send a single email, with both
 * recipients listed in the {@code TO} field of the email.
 * </p>
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 * @see <a href="https://tools.ietf.org/html/rfc6068">RFC 6068</a>
 */
@Slf4j
@AllArgsConstructor
@Service
public class EmailDispatchImpl implements DispatchService {

    private final Parameterizer parameterizer;
    private final EmailComposer composer;
    private final JavaMailSender javaMailSender;

    @Override
    public void dispatch(Notification notification) {
        try {
            NotificationType notificationType = notification.getType();
            Map<NotificationTemplateName, String> parameterizedTemplates = parameterizer.
                    resolveAndParameterize(notification, notificationType);

            MimeMessage email = composer.compose(notification, parameterizedTemplates);

            Arrays.stream(email.getRecipients(Message.RecipientType.TO))
                    .findAny()
                    .orElseThrow(() -> new DispatchException(
                        "Cannot dispatch email with an empty To: address for notification tuple [" +
                        notificationTuple(notification) + "]", notification));

            // send email
            javaMailSender.send(email);
            log.trace("Dispatched email with id '{}'", notification.getEventId());
        } catch (DispatchException e) {
            throw e;
        } catch (Exception e) {
            throw new DispatchException(e.getMessage(), e, notification);
        }
    }

    private static String notificationTuple(Notification notification) {
        return join(",", Arrays.asList(notification.getResourceId(), notification.getEventId()));
    }

}
