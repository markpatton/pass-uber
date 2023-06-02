package org.eclipse.pass.notification.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Russ Poetker (rpoetke1@jh.edu)
 */
@Getter
@AllArgsConstructor
public enum NotificationParam {
    /**
     * Placeholder for the addressee of the notification
     */
    TO("to"),

    /**
     * Placeholder for notification carbon copies
     */
    CC("cc"),

    /**
     * Placeholder for notification blind carbon copies
     */
    BCC("bcc"),

    /**
     * Placeholder for the sender of the notification
     */
    FROM("from"),

    /**
     * Placeholder for the subject of the notification
     */
    SUBJECT("subject"),

    /**
     * Placeholder for a data structure carrying metadata about the submission that relates to this notification
     */
    RESOURCE_METADATA("resource_metadata"),

    /**
     * Placeholder for a data structure carrying metadata about the event prompting the notification
     */
    EVENT_METADATA("event_metadata"),

    /**
     * Placeholder for a data structure carrying links and their descriptions that may be included in the
     * notification
     */
    LINKS("link_metadata");

    /**
     * String representation of the parameter name, suitable for use as a key in a key-value pair.
     */
    private final String paramName;

}
