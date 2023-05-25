/*
 *
 *  * Copyright 2018 Johns Hopkins University
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.eclipse.pass.notification.service;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;

import org.eclipse.pass.support.client.model.Submission;
import org.eclipse.pass.support.client.model.SubmissionEvent;
import org.eclipse.pass.support.client.model.User;
import org.springframework.stereotype.Component;

/**
 * Examines the {@link Submission} and {@link SubmissionEvent}, and determines who should receive the notification.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Component
public class RecipientAnalyzer implements BiFunction<Submission, SubmissionEvent, Collection<String>> {

    @Override
    public Collection<String> apply(Submission submission, SubmissionEvent event) {
        switch (event.getEventType()) {
            case APPROVAL_REQUESTED_NEWUSER, APPROVAL_REQUESTED -> {
                // to: authorized submitter
                String submitterUriOrEmail = submitterEmail(submission)
                    .orElseThrow(() ->
                        new RuntimeException(
                            "Submitter URI and email are null for " + submission.getId()));
                return singleton(submitterUriOrEmail);
            }
            case CHANGES_REQUESTED, SUBMITTED -> {
                // to: submission.preparers
                return submission.getPreparers().stream().map(User::getEmail).collect(toSet());
            }
            case CANCELLED -> {
                String performedBy = event.getPerformedBy().toString();
                Collection<String> recipients;
                if (submission.getSubmitter().toString().equals(performedBy)) {
                    recipients =
                        submission.getPreparers().stream().map(User::getEmail).collect(toSet());
                } else {
                    recipients =
                        singleton(submission.getSubmitter().toString());
                }

                return recipients;
            }
            default -> {
                throw new RuntimeException("Unhandled SubmissionEvent type '" + event.getEventType() + "'");
            }
        }
    }

    private static Optional<String> submitterEmail(Submission s) {
        return Optional.ofNullable(s.getSubmitterEmail()).map(URI::toString);
    }

}
