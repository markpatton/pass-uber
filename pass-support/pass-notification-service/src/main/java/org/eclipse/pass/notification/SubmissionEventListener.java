package org.eclipse.pass.notification;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.pass.notification.config.Mode;
import org.eclipse.pass.notification.config.NotificationConfig;
import org.eclipse.pass.notification.service.NotificationService;
import org.eclipse.pass.support.client.model.SubmissionEvent;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Slf4j
@Component
public class SubmissionEventListener {

    private final NotificationService notificationService;
    private final NotificationConfig notificationConfig;

    // TODO there will be a queue for submissionevents
    @JmsListener(destination = "${pass.notification.queue.event.name}",
        containerFactory = "jmsListenerContainerFactory")
    public void processMessage(SubmissionEvent submissionEvent) {
        log.trace("Receiving SubmissionEvent: {}", submissionEvent.getId());
        if (Mode.DISABLED == notificationConfig.getMode()) {
            log.trace("Discarding message {}, mode is {}", submissionEvent.getId(), notificationConfig.getMode());
            return;
        }

        notificationService.notify(submissionEvent);
    }
}
