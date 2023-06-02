package org.eclipse.pass.notification.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Russ Poetker (rpoetke1@jh.edu)
 */
@Getter
@AllArgsConstructor
public enum NotificationTemplateName {

    /**
     * The template for composing the subject of a notification
     */
    SUBJECT("subject"),

    /**
     * The template for composing the body of a notification
     */
    BODY("body"),

    /**
     * The template for composing the footer of a notification
     */
    FOOTER("footer");

    private final String templateName;

}
