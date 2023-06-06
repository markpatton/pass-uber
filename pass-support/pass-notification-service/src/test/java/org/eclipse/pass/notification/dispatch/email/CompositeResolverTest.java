/*
 *
 *  * Copyright 2018 Johns Hopkins University
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package org.eclipse.pass.notification.dispatch.email;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.eclipse.pass.notification.AbstractNotificationSpringTest;
import org.eclipse.pass.notification.config.NotificationTemplateName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

public class CompositeResolverTest extends AbstractNotificationSpringTest {

    @Autowired private CompositeResolver compositeResolver;

    @Test
    public void testSpringResolution() throws IOException {
        // GIVEN
        String templatePath = "/templates/pass-body-submission-approval-invite-template.hbr";
        String expectedTemplate = IOUtils.toString(new ClassPathResource(templatePath).getInputStream(),
            StandardCharsets.UTF_8);

        // WHEN
        InputStream inputStream = compositeResolver.resolve(NotificationTemplateName.BODY,
            "classpath:" + templatePath);

        // THEN
        assertEquals(IOUtils.toString(inputStream, StandardCharsets.UTF_8), expectedTemplate);
    }

    @Test
    public void testInlineResolution() throws IOException {
        // GIVEN
        String template = "a template";

        // WHEN
        InputStream inputStream = compositeResolver.resolve(null, template);

        // THEN
        assertEquals(IOUtils.toString(inputStream, StandardCharsets.UTF_8), template);
    }
}