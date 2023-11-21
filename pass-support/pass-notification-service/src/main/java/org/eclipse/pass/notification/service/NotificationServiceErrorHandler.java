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
package org.eclipse.pass.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.pass.notification.dispatch.DispatchException;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

/**
 * Global error handler which logs unhandled exceptions.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Slf4j
@Component
public class NotificationServiceErrorHandler implements ErrorHandler {

    @Override
    public void handleError(Throwable throwable) {
        if (!(throwable instanceof DispatchException)) {
            log.error("Encountered an unrecoverable error: {}", throwable.getMessage(), throwable);
        }
    }

}
