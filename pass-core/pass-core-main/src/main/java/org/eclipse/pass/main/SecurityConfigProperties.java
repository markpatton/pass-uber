/*
 * Copyright 2020, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */

package org.eclipse.pass.main;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "security")
public class SecurityConfigProperties {
    private String origin = "*";

    public String getOrigin() {
        return origin;
    }
}
