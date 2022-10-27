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
package org.eclipse.pass.object.model;

import static org.eclipse.pass.object.model.support.TestObjectCreator.createSubmission;
import static org.eclipse.pass.object.model.support.TestObjectCreator.createUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.time.ZonedDateTime;

import org.eclipse.pass.object.model.support.TestValues;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

/**
 * Model has been annotated with JSON tags. These tests do a simple check to ensure the
 * Jackson integration is functional and the equals / hashcode functions work
 *
 * @author Karen Hanson
 * @author Jim Martino
 */
public class SubmissionEventModelTests {

    private DateTimeFormatter dateFormatter = ISODateTimeFormat.dateTime().withZoneUTC();

    /**
     * Creates two identical SubmissionEvents and checks the equals and hashcodes match.
     * Modifies one field on one of the SubmissionEvents and verifies they no longer are
     * equal or have matching hashcodes.
     *
     * @throws Exception
     */
    @Test
    public void testSubmissionEqualsAndHashCode() throws Exception {

        SubmissionEvent submissionEvent1 = createSubmissionEvent();
        SubmissionEvent submissionEvent2 = createSubmissionEvent();

        assertEquals(submissionEvent1, submissionEvent2);
        submissionEvent1.setPerformerRole(PerformerRole.SUBMITTER);
        assertTrue(!submissionEvent1.equals(submissionEvent2));

        assertTrue(submissionEvent1.hashCode() != submissionEvent2.hashCode());
        submissionEvent1 = submissionEvent2;
        assertEquals(submissionEvent1.hashCode(), submissionEvent2.hashCode());

    }

    /**
     * Test copy constructor creates a valid duplicate that is not the same object
     *
     * @throws Exception
     */
    @Test
    public void testSubmissionEventCopyConstructor() throws Exception {
        SubmissionEvent submissionEvent = createSubmissionEvent();
        SubmissionEvent submissionEventCopy = new SubmissionEvent(submissionEvent);
        assertEquals(submissionEvent, submissionEventCopy);

        URI newLink = new URI("different:link");
        submissionEventCopy.setLink(newLink);
        assertEquals(new URI(TestValues.SUBMISSIONEVENT_LINK), submissionEvent.getLink());
        assertEquals(newLink, submissionEventCopy.getLink());

        submissionEventCopy.setEventType(EventType.CANCELLED);
        assertEquals(EventType.of(TestValues.SUBMISSIONEVENT_EVENT_TYPE),
                     submissionEvent.getEventType());
        assertEquals(EventType.CANCELLED, submissionEventCopy.getEventType());
    }

    private SubmissionEvent createSubmissionEvent() throws Exception {
        SubmissionEvent submissionEvent = new SubmissionEvent();
        submissionEvent.setId(TestValues.SUBMISSIONEVENT_ID);
        submissionEvent.setEventType(EventType.of(TestValues.SUBMISSIONEVENT_EVENT_TYPE));
        ZonedDateTime zdt = ZonedDateTime.parse(TestValues.SUBMISSIONEVENT_PERFORMED_DATE_STR);
        submissionEvent.setPerformedDate(zdt);
        submissionEvent.setPerformedBy(createUser(TestValues.USER_ID_1));
        submissionEvent.setPerformerRole(PerformerRole.PREPARER);
        submissionEvent.setSubmission(createSubmission(TestValues.SUBMISSION_ID_1));
        submissionEvent.setComment(TestValues.SUBMISSIONEVENT_COMMENT);
        submissionEvent.setLink(new URI(TestValues.SUBMISSIONEVENT_LINK));

        return submissionEvent;
    }

}
