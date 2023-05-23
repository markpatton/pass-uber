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

package org.dataconservancy.pass.notification.impl;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dataconservancy.pass.notification.model.Link;
import org.dataconservancy.pass.notification.model.config.LinkValidationRule;
import org.dataconservancy.pass.notification.model.config.NotificationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author apb@jhu.edu
 */
public class LinkValidator implements Predicate<Link> {

    private static final Logger LOG = LoggerFactory.getLogger(LinkValidator.class);

    private final Collection<LinkValidationRule> rules;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public LinkValidator(NotificationConfig config) {

        this.rules = config.getLinkValidatorConfigs();
        requireNonNull(this.rules, "No configuration supplied to link validator");

    }

    /**
     * Test a link against the set of link validation configs.
     * <p>
     * Any rules that pertain to the matching link relation will be applied. Rules that contain a "*" will match all
     * links
     * <p>
     *
     * @param link Link to test.
     */
    @Override
    public boolean test(Link link) {
        return rules.stream()
                .filter(rule -> rule.getRels().contains(link.getRel()) || rule.getRels().contains("*"))
                .allMatch(rule -> apply(rule, link));
    }

    private static boolean apply(LinkValidationRule cfg, Link link) {
        if (cfg.getRequiredBaseURI() != null) {
            final boolean isValid = link.getHref().toString().startsWith(cfg.getRequiredBaseURI());

            if (cfg.getThrowExceptionWhenInvalid() && !isValid) {
                try {
                    throw new RuntimeException(format("Invalid link %s violates rule %s", link, objectMapper
                            .writerWithDefaultPrettyPrinter().writeValueAsString(cfg)));
                } catch (final IOException e) {
                    // Should never happen. This is a fallback in case deserializing the rule in JSON in the error
                    // message fails.
                    throw new RuntimeException("Invalid link " + link);
                }
            } else {
                return isValid;
            }
        } else {
            return true;
        }
    }

}
