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

import static java.util.Optional.ofNullable;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.eclipse.pass.notification.config.NotificationConfig;
import org.eclipse.pass.notification.dispatch.DispatchException;
import org.eclipse.pass.notification.model.Notification;
import org.eclipse.pass.notification.model.NotificationTemplate;
import org.eclipse.pass.notification.model.NotificationTemplateName;
import org.eclipse.pass.notification.model.NotificationType;
import org.springframework.stereotype.Component;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@AllArgsConstructor
@Component
public class Parameterizer {

    private final NotificationConfig notificationConfig;
    private final CompositeResolver compositeResolver;
    private final HandlebarsParameterizer parameterizer;

    Map<NotificationTemplateName, String> resolveAndParameterize(
            Notification notification, NotificationType notificationType) {
        NotificationTemplate template = notificationConfig.getTemplates().stream()
                .filter(candidate -> candidate.getNotificationType() == notificationType)
                .findAny()
                .orElseThrow(() ->
                        new DispatchException("Missing notification template for mode '" + notificationType + "'",
                                notification));

        Map<NotificationTemplateName, InputStream> templates =
                template.getTemplates()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                entry -> compositeResolver.resolve(entry.getKey(), entry.getValue())));

        // perform pararmeterization on all templates

        return templates.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> parameterizer.parameterize(
                                entry.getKey(),
                                ofNullable(notification.getParameters())
                                    .orElseGet(Collections::emptyMap),
                                entry.getValue())));
    }
}
