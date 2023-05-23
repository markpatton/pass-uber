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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.dataconservancy.pass.notification.model.Link.Rels.SUBMISSION_REVIEW_INVITE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;

import org.dataconservancy.pass.authz.usertoken.Key;
import org.dataconservancy.pass.authz.usertoken.Token;
import org.dataconservancy.pass.authz.usertoken.TokenFactory;
import org.dataconservancy.pass.model.Submission;
import org.dataconservancy.pass.notification.model.Link;
import org.dataconservancy.pass.notification.model.config.NotificationConfig;
import org.dataconservancy.pass.notification.model.config.UserTokenGeneratorConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author apb@jhu.edu
 */
@RunWith(MockitoJUnitRunner.class)
public class UserTokenGeneratorTest {

    @Mock
    TokenFactory tokenFactory;

    @Mock
    TokenFactory.Builder tokenBuilder;

    @Mock
    Token token;

    URI submissionUri = URI.create("http://example.org/test/submission");

    URI submitterEmail = URI.create("mailto:test");

    URI emberLink = URI.create("http://example.org/test/emberLink");

    URI emberLinkWithToken = URI.create("http://example.org/test/emberLink?withToken=true");

    Submission submission = new Submission();

    UserTokenGenerator toTest;

    @Before
    public void setUp() {
        submission.setId(submissionUri);
        submission.setSubmitterEmail(submitterEmail);

        when(tokenFactory.forPassResource(eq(submissionUri))).thenReturn(tokenBuilder);
        when(tokenBuilder.withReference(eq(submitterEmail))).thenReturn(token);
        when(token.addTo(eq(emberLink))).thenReturn(emberLinkWithToken);

        toTest = new UserTokenGenerator(tokenFactory);
    }

    // Verify that a user token can be properly attached to a submission invite link
    @Test
    public void tokenAttachmentTest() {
        final Link inviteLink = new Link(emberLink, SUBMISSION_REVIEW_INVITE);

        assertEquals(new Link(emberLinkWithToken, SUBMISSION_REVIEW_INVITE),
                toTest.forSubmission(submission).apply(inviteLink));

        verify(tokenFactory, times(1)).forPassResource(eq(submissionUri));
        verify(tokenBuilder, times(1)).withReference(eq(submitterEmail));
        verify(token, times(1)).addTo(eq(emberLink));
    }

    // Verify that links that are NOT submission invite links are not touched.
    @Test
    public void doNotAttachTokensUnnecessarilyTest() {

        final List<Link> links = asList(
                new Link(emberLink, Link.Rels.SUBMISSION_REVIEW),
                new Link(emberLink, Link.Rels.SUBMISSION_VIEW),
                new Link(emberLink, "someOtherRel"));

        final List<Link> processedLinks = links.stream()
                .map(toTest.forSubmission(submission))
                .collect(toList());

        // No links should have been modified, removed, or added.
        assertEquals(links.size(), processedLinks.size());
        assertTrue(processedLinks.containsAll(links));
    }

    // Throw an exception if the submitter email is null
    @Test
    public void noSubmitterEmailTest() {
        submission.setSubmitterEmail(null);

        try {
            toTest.forSubmission(submission).apply(new Link(emberLink, SUBMISSION_REVIEW_INVITE));
            fail("Token attachment should have failed due to missing submitter");
        } catch (final NullPointerException e) {
            assertTrue("Null pointer message should indicate the URI of the offending submission", e.getMessage()
                    .contains(submissionUri.toString()));
        }
    }

    // Throw an exception if the submission is null
    @Test(expected = NullPointerException.class)
    public void nullSubmissionTest() {
        toTest.forSubmission(null).apply(new Link(emberLink, SUBMISSION_REVIEW_INVITE));
    }

    // Throw an exception if the submission ID is null
    @Test(expected = NullPointerException.class)
    public void nullSubmissionIdTest() {
        submission.setId(null);
        toTest.forSubmission(submission).apply(new Link(emberLink, SUBMISSION_REVIEW_INVITE));
    }

    // Verify that the user token generator can bootstrap itself from config, add a token to a link,
    // and assure that the key specified in the config was truly the one used by the token generator service.
    @Test
    public void configurationConstructorRoundTripTest() {
        final NotificationConfig config = new NotificationConfig();
        final Link inviteLink = new Link(emberLink, SUBMISSION_REVIEW_INVITE);

        // Create a new key and save it in the config.
        final Key key = Key.generate();

        config.setUserTokenGeneratorConfig(new UserTokenGeneratorConfig());
        config.getUserTokenGeneratorConfig().setKey(key.toString());

        // Create an instance using notification config
        toTest = new UserTokenGenerator(config);

        // Generate an invte link with token.
        final Link inviteWithUserToken = toTest.forSubmission(submission).apply(inviteLink);

        // Now, we should be able to independently read the token from the invite link using
        // a TokenFactory instance that uses the same key from the config.
        final TokenFactory decoder = new TokenFactory(key);
        final Token decodedToken = decoder.fromUri(inviteWithUserToken.getHref());

        assertEquals(submission.getId(), decodedToken.getPassResource());
        assertEquals(submission.getSubmitterEmail(), decodedToken.getReference());
    }
}
