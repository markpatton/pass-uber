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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.empty;
import static org.eclipse.pass.notification.model.Link.SUBMISSION_REVIEW;
import static org.eclipse.pass.notification.model.Link.SUBMISSION_REVIEW_INVITE;
import static org.eclipse.pass.notification.model.Link.SUBMISSION_VIEW;
import static org.eclipse.pass.notification.service.LinksUtil.optional;
import static org.eclipse.pass.notification.service.LinksUtil.required;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.pass.notification.model.Link;
import org.eclipse.pass.notification.model.SubmissionEventMessage;
import org.eclipse.pass.support.client.model.Submission;
import org.eclipse.pass.support.client.model.SubmissionEvent;
import org.springframework.stereotype.Component;

/**
 * Analyzes a submission+event pair and emits a stream of submission-related {@link Link}s
 *
 * @author apb@jhu.edu
 */
@Slf4j
@Component
public class SubmissionLinkAnalyzer implements BiFunction<SubmissionEvent, SubmissionEventMessage, Stream<Link>> {

    /**
     * Return all submission-related links associated with a submission+event pair.
     *
     * @param event The associated submission event.
     * @param submissionEventMessage The associated submission event message.
     * @return a stream of links, likely containing zero or one links.
     */
    @Override
    public Stream<Link> apply(SubmissionEvent event, SubmissionEventMessage submissionEventMessage) {
        requireNonNull(event);

        Submission submission = event.getSubmission();
        requireNonNull(submission);

        if (event.getEventType() == null) {
            log.warn("Submission event type was null for {}.  Ignoring.", event.getId());
            return empty();
        }

        switch (event.getEventType()) {
            case APPROVAL_REQUESTED_NEWUSER -> {
                return required(format("Invalid submissionEvent %s", event.getId()),
                    event.getLink(),
                    SUBMISSION_REVIEW_INVITE)
                    .map((link) -> new Link(submissionEventMessage.getUserApprovalLink(), SUBMISSION_REVIEW_INVITE));
            }
            case APPROVAL_REQUESTED, CHANGES_REQUESTED -> {
                return required(
                    format("Invalid submissionEvent %s", event.getId()),
                    event.getLink(),
                    SUBMISSION_REVIEW);
            }
            case SUBMITTED, CANCELLED -> {
                return optional(event.getLink(), SUBMISSION_VIEW);
            }
            default -> {
                // If there is a link, but an unknown submission type, then just blindly pass
                // along the link.
                log.warn("Encountered unknown submission event type {}", event.getEventType());
                return optional(event.getLink(), SUBMISSION_VIEW);
            }
        }
    }
}
