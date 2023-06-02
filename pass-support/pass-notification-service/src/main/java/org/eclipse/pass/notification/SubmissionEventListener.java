package org.eclipse.pass.notification;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.pass.notification.config.Mode;
import org.eclipse.pass.notification.config.NotificationConfig;
import org.eclipse.pass.notification.model.SubmissionEventMessage;
import org.eclipse.pass.notification.service.NotificationService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * @author Russ Poetker (rpoetke1@jh.edu)
 */
@AllArgsConstructor
@Slf4j
@Component
public class SubmissionEventListener {

    private final NotificationService notificationService;
    private final NotificationConfig notificationConfig;

    /**
     * Listen for submission even messages and process them.
     * @param submissionEventMessage the message
     */
    @JmsListener(destination = "${pass.notification.queue.event.name}",
        containerFactory = "jmsListenerContainerFactory")
    public void processMessage(SubmissionEventMessage submissionEventMessage) {
        log.trace("Receiving SubmissionEvent: {}", submissionEventMessage.getSubmissionEventId());
        if (Mode.DISABLED == notificationConfig.getMode()) {
            log.trace("Discarding message {}, mode is {}",
                submissionEventMessage.getSubmissionEventId(),
                notificationConfig.getMode());
            return;
        }

        notificationService.notify(submissionEventMessage);
    }
}
