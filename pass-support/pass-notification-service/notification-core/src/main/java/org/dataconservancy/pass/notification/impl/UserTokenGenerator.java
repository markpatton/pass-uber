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
import static org.dataconservancy.pass.notification.model.Link.Rels.SUBMISSION_REVIEW_INVITE;

import java.util.function.UnaryOperator;

import org.dataconservancy.pass.authz.usertoken.TokenFactory;
import org.dataconservancy.pass.model.Submission;
import org.dataconservancy.pass.notification.model.Link;
import org.dataconservancy.pass.notification.model.config.NotificationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates user tokens for invitation links.
 *
 * @author apb@jhu.edu
 */
public class UserTokenGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(UserTokenGenerator.class);

    final TokenFactory tokenFactory;

    /**
     * Constructor, given a pre-configured token factory
     *
     * @param factory Pre-configured token factory.
     */
    public UserTokenGenerator(TokenFactory factory) {
        this.tokenFactory = factory;
    }

    public UserTokenGenerator(NotificationConfig config) {
        requireNonNull(config.getUserTokenGeneratorConfig(), "User token generator config must not be null");
        requireNonNull(config.getUserTokenGeneratorConfig().getKey(),
                "User token generator must be configured with an encryption key");

        tokenFactory = new TokenFactory(config.getUserTokenGeneratorConfig().getKey());
    }

    /**
     * Create a new user token generator for invitation links associated with the given submission
     * <p>
     * If a submission invite link is encountered by this function (as determined by the presence of the link relation
     * {@link Link.Rels#SUBMISSION_REVIEW_INVITE}), then a new user token will be attached to the link. The token is
     * generated using the identity and contents of the given submission.
     * </p>
     *
     * @param submission A PASS submission entity.
     * @return Function that transforms the URI in a submission invite {@link Link} to add a user token
     */
    public UnaryOperator<Link> forSubmission(Submission submission) {

        requireNonNull(submission, "Cannot create an invitation link for a null submission");
        requireNonNull(submission.getId(),
                "Cannot create an invitation link for a submission with a null ID");

        return link -> {
            if (SUBMISSION_REVIEW_INVITE.equals(link.getRel())) {

                requireNonNull(submission.getSubmitterEmail(),
                        format("Cannot create an invitation link, submitter e-mail is null on %s", submission
                                .getId()));

                LOG.debug("Attaching user token for <{}> to link <{}>",
                        submission.getSubmitterEmail(),
                        link.getHref());

                final Link inviteLink = new Link(
                        tokenFactory
                                .forPassResource(submission.getId())
                                .withReference(submission.getSubmitterEmail())
                                .addTo(link.getHref()),
                        link.getRel());

                LOG.info("Generated invitation link <{}> for <{}>", inviteLink.getHref(), submission
                        .getSubmitterEmail());

                return inviteLink;
            } else {
                LOG.debug("Ignoring link type " + link.getRel());
                return link;
            }
        };
    }
}
