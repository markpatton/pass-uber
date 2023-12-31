/*
 * Copyright 2023 Johns Hopkins University
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
package org.eclipse.pass.notification.model;

import static org.eclipse.pass.notification.model.NotificationType.SUBMISSION_APPROVAL_INVITE;
import static org.eclipse.pass.notification.model.NotificationType.SUBMISSION_APPROVAL_REQUESTED;
import static org.eclipse.pass.notification.model.NotificationType.SUBMISSION_CHANGES_REQUESTED;
import static org.eclipse.pass.notification.model.NotificationType.SUBMISSION_SUBMISSION_CANCELLED;
import static org.eclipse.pass.notification.model.NotificationType.SUBMISSION_SUBMISSION_SUBMITTED;
import static org.eclipse.pass.support.client.model.EventType.APPROVAL_REQUESTED;
import static org.eclipse.pass.support.client.model.EventType.APPROVAL_REQUESTED_NEWUSER;
import static org.eclipse.pass.support.client.model.EventType.CANCELLED;
import static org.eclipse.pass.support.client.model.EventType.CHANGES_REQUESTED;
import static org.eclipse.pass.support.client.model.EventType.SUBMITTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.eclipse.pass.support.client.model.EventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class NotificationTypeTest {

    private static Stream<Arguments> provideEventTypeMapping() {
        return Stream.of(
            Arguments.of(APPROVAL_REQUESTED, SUBMISSION_APPROVAL_REQUESTED),
            Arguments.of(APPROVAL_REQUESTED_NEWUSER, SUBMISSION_APPROVAL_INVITE),
            Arguments.of(CHANGES_REQUESTED, SUBMISSION_CHANGES_REQUESTED),
            Arguments.of(SUBMITTED, SUBMISSION_SUBMISSION_SUBMITTED),
            Arguments.of(CANCELLED, SUBMISSION_SUBMISSION_CANCELLED)
        );
    }

    /**
     * Ensure that event types are properly mapped to notification types
     *  APPROVAL_REQUESTED_NEWUSER -> SUBMISSION_APPROVAL_INVITE
     *  APPROVAL_REQUESTED -> SUBMISSION_APPROVAL_REQUESTED
     *  CHANGES_REQUESTED -> SUBMISSION_CHANGES_REQUESTED
     *  SUBMITTED -> SUBMISSION_SUBMISSION_SUBMITTED
     *  CANCELLED -> SUBMISSION_SUBMISSION_CANCELLED
     */
    @ParameterizedTest
    @MethodSource("provideEventTypeMapping")
    void testFindForEventType(EventType eventType, NotificationType expectedType) {
        NotificationType actualType = NotificationType.findForEventType(eventType);
        assertEquals(expectedType, actualType);
    }

    @Test
    void testFindForEventType_Fail_UnknownEventType() {
        assertThrows(IllegalArgumentException.class, () -> {
            NotificationType.findForEventType(null);
        });
    }
}
