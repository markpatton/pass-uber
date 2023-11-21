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
package org.eclipse.pass.notification.dispatch.email;

import java.io.InputStream;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.pass.notification.config.NotificationTemplateName;
import org.springframework.stereotype.Component;

/**
 * Attempts to resolve a named template using a list of template resolvers.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Slf4j
@Component
public class CompositeResolver implements TemplateResolver {

    private final List<TemplateResolver> resolvers;

    /**
     * Constructor.
     * @param springUriTemplateResolver spring uri template resolver
     * @param inlineTemplateResolver inline template resolver
     */
    public CompositeResolver(SpringUriTemplateResolver springUriTemplateResolver,
                             InlineTemplateResolver inlineTemplateResolver) {
        this.resolvers = List.of(springUriTemplateResolver, inlineTemplateResolver);
    }

    @Override
    public InputStream resolve(NotificationTemplateName name, String template) {
        InputStream in;
        for (TemplateResolver resolver : resolvers) {
            try {
                log.debug("Attempting resolution of template value '{}', for named template '{}'", template, name);
                in = resolver.resolve(name, template);
                if (in != null) {
                    return in;
                }
            } catch (Exception e) {
                log.debug("Unable to resolve template '{}' {}: ", name, e.getMessage(), e);
            }
        }

        throw new RuntimeException("Unable to resolve template name '" + name + "', '" + template + "'");
    }
}
