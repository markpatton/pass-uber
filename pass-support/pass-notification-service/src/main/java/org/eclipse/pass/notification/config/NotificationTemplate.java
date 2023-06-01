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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.pass.notification.model.NotificationType;

/**
 * Allows for the customization of notification subject, body, and footer for each notification type.  Each notification
 * type has exactly one {@code NotificationTemplate}.
 * <p>
 * Importantly, templates may be specified inline, or as references to a template body.  For example:
 * </p>
 * <pre>
 * NotificationConfig config = ...;
 * NotificationTemplate approvalRequestedTemplate = config.getTemplates()
 *                                       .stream()
 *                                       .filter(t -&gt; t.getNotificationType() == SUBMISSION_APPROVAL_REQUESTED)
 *                                       .findAny()
 *                                       .orElseThrow(() -&gt; new RuntimeException("Missing template for " +
 *                                       SUBMISSION_APPROVAL_REQUESTED));
 *
 * String subjectTemplate = approvalRequestedTemplate.getTemplates().get(SUBJECT);
 * // subjectTemplate can be an inline value:
 * //   "Approval Requested"
 * // or it can be a reference to a resource containing the subject value:
 * //   "classpath:/approval-requested-subject.txt"
 * </pre>
 * <p>
 * References must be specified as Spring Resource URIs.
 * </p>
 *
 * @see <a href="https://docs.spring.io/spring/docs/5.1.1.RELEASE/spring-framework-reference/core.html#resources">
 *     Spring Resources</a>
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Getter
@Setter
@EqualsAndHashCode
public class NotificationTemplate {

    /**
     * Inline bodies or a URI to the bodies
     */
    private Map<NotificationTemplateName, String> templates = new HashMap<>(NotificationTemplateName.values().length);

    /**
     * The type of notification this template is associated with
     */
    @JsonProperty("notification")
    private NotificationType notificationType;

}
