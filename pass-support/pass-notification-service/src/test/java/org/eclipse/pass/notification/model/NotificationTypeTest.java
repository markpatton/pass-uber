package org.eclipse.pass.notification.model;

import org.eclipse.pass.support.client.model.EventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

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
