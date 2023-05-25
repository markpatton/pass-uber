package org.eclipse.pass.notification.model;

import static org.eclipse.pass.support.client.model.EventType.APPROVAL_REQUESTED;
import static org.eclipse.pass.support.client.model.EventType.APPROVAL_REQUESTED_NEWUSER;
import static org.eclipse.pass.support.client.model.EventType.CANCELLED;
import static org.eclipse.pass.support.client.model.EventType.CHANGES_REQUESTED;
import static org.eclipse.pass.support.client.model.EventType.SUBMITTED;

import java.util.stream.Stream;

import org.eclipse.pass.support.client.model.EventType;

public enum NotificationType {
    /**
     * Preparer has requested approval of a Submission by an Authorized Submitter
     */
    SUBMISSION_APPROVAL_REQUESTED(APPROVAL_REQUESTED_NEWUSER),

    /**
     * Preparer has requested approval of a Submission by an Authorized Submitter who does not have a {@code User}
     * in PASS.  The notification will include an invitation to join PASS (and upon login, a {@code User} created
     * for the Authorized Submitter)
     */
    SUBMISSION_APPROVAL_INVITE(APPROVAL_REQUESTED),

    /**
     * Authorized Submitter has requested changes to the Submission by the Preparer.
     */
    SUBMISSION_CHANGES_REQUESTED(CHANGES_REQUESTED),

    /**
     * Submission was submitted by the Authorized Submitter
     */
    SUBMISSION_SUBMISSION_SUBMITTED(SUBMITTED),

    /**
     * Submission was cancelled by either the Authorized Submitter or Preparer
     */
    SUBMISSION_SUBMISSION_CANCELLED(CANCELLED);

    private final EventType eventType;

    NotificationType(EventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Find NotificationType for EventType.
     * @param eventType the event type
     * @return the associated NotificationType
     * @throws IllegalArgumentException if eventType is not associated to NotificationType
     */
    public static NotificationType findForEventType(EventType eventType) {
        return Stream.of(NotificationType.values())
            .filter(notificationType -> notificationType.eventType == eventType)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown SubmissionEvent type '" + eventType + "'"));
    }
}
