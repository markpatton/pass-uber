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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.pass.notification.dispatch.DispatchService;
import org.eclipse.pass.notification.model.Notification;
import org.eclipse.pass.support.client.PassClient;
import org.eclipse.pass.support.client.model.Submission;
import org.eclipse.pass.support.client.model.SubmissionEvent;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link NotificationService} which processes {@link SubmissionEvent}s relating to proxy
 * submissions.  Self-submitted {@link Submission}s (identified by the lack of a preparer on the {@code Submission}) are
 * not processed by this implementation.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Slf4j
@AllArgsConstructor
@Service
public class DefaultNotificationService implements NotificationService {

    private final PassClient passClient;
    private final DispatchService dispatchService;
    private final Composer composer;

    @Override
    public void notify(SubmissionEvent submissionEvent) {

        // Retrieve Submission
        SubmissionEvent passSubmissionEvent;
        try {
            passSubmissionEvent = passClient.getObject(SubmissionEvent.class, submissionEvent.getId(),
                "submission", "performedBy");
        } catch (Exception e) {
            log.error("Unable to retrieve SubmissionEvent '{}'", submissionEvent.getId(), e);
            return;
        }
        Submission submission = passSubmissionEvent.getSubmission();

        // todo: abstract into a policy of some kind
        if ((submission.getPreparers() == null || submission.getPreparers().isEmpty()) ||
                (submission.getPreparers().contains(
                    submission.getSubmitter()) && submission.getPreparers().size() == 1)
        ) {
            // then we are not dealing with proxy submission, we're dealing with self-submission.
            // in the case of self-submission, notifications are not produced, so short-circuit here
            log.debug("Dropping self-submission SubmissionEvent (Event URI: {}, Resource URI: {})",
                    submissionEvent.getId(),
                    submission.getId());
            return;
        }

        // Compose Notification
        Notification notification = composer.apply(submission, passSubmissionEvent);

        // Invoke Dispatch
        dispatchService.dispatch(notification);
    }

}
