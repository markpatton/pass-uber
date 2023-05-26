package org.eclipse.pass.notification.config;

import lombok.Getter;

@Getter
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

    NotificationTemplateName(String templateName) {
        this.templateName = templateName;
    }

}
