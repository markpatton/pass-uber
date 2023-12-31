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
package org.eclipse.pass.notification.logging;

import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import org.eclipse.pass.notification.AbstractNotificationSpringTest;
import org.eclipse.pass.notification.dispatch.DispatchService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Simply checks to see that the Dispatch implementation is an Advised instance.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class LoggingAspectTest extends AbstractNotificationSpringTest {

    @Autowired
    private DispatchService dispatchService;

    @Test
    public void dispatchIsAdvised() {
        assertNotNull("DispatchService was not autowired.", dispatchService);
        assertTrue("DispatchService is not an instance of an Advised class.",
                dispatchService instanceof Advised);
    }
}
