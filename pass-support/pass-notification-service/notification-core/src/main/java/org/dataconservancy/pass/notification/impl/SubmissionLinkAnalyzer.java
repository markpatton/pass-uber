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

package org.dataconservancy.pass.notification.impl;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.empty;
import static org.dataconservancy.pass.notification.impl.Links.optional;
import static org.dataconservancy.pass.notification.impl.Links.required;
import static org.dataconservancy.pass.notification.model.Link.Rels.SUBMISSION_REVIEW;
import static org.dataconservancy.pass.notification.model.Link.Rels.SUBMISSION_REVIEW_INVITE;
import static org.dataconservancy.pass.notification.model.Link.Rels.SUBMISSION_VIEW;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.dataconservancy.pass.model.Submission;
import org.dataconservancy.pass.model.SubmissionEvent;
import org.dataconservancy.pass.notification.model.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analyzes a submission+event pair and emits a stream of submission-related {@link Link}s
 *
 * @author apb@jhu.edu
 */
public class SubmissionLinkAnalyzer implements BiFunction<Submission, SubmissionEvent, Stream<Link>> {

    static final Logger LOG = LoggerFactory.getLogger(SubmissionLinkAnalyzer.class);

    final UserTokenGenerator tokenGenerator;

    /**
     * Constructor.
     *
     * @param generator User token generator, for building invite links.
     */
    public SubmissionLinkAnalyzer(final UserTokenGenerator generator) {
        tokenGenerator = generator;
    }

    /**
     * Return all submission-related links associated with a submission+event pair.
     *
     * @param submission The submission.
     * @param event The associated submission event.
     * @return a stream of links, likely containing zero or one links.
     */
    @Override
    public Stream<Link> apply(Submission submission, SubmissionEvent event) {
        requireNonNull(submission);
        requireNonNull(event);

        if (event.getEventType() == null) {
            LOG.warn("Submission event type was null for {}.  Ignoring.", event.getId());
            return empty();
        }

        switch (event.getEventType()) {
            case APPROVAL_REQUESTED_NEWUSER:
                return required(format("Invalid submissionEvent %s", event.getId()),
                                event.getLink(),
                                SUBMISSION_REVIEW_INVITE)
                    .map(tokenGenerator.forSubmission(submission));
            case APPROVAL_REQUESTED:
                return required(
                    format("Invalid submissionEvent %s", event.getId()),
                    event.getLink(),
                    SUBMISSION_REVIEW);
            case CHANGES_REQUESTED:
                return required(
                    format("Invalid submissionEvent %s", event.getId()),
                    event.getLink(),
                    SUBMISSION_REVIEW);
            case SUBMITTED:
                return optional(event.getLink(), SUBMISSION_VIEW);
            case CANCELLED:
                return optional(event.getLink(), SUBMISSION_VIEW);
            default:
                // If there is a link, but an unknown submission type, then just blindly pass
                // along the link.
                LOG.warn("Encountered unknown submission event type {}", event.getEventType());
                return optional(event.getLink(), SUBMISSION_VIEW);
        }
    }
}
