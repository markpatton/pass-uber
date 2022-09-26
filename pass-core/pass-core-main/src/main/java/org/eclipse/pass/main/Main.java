/*
 * Copyright 2019, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package org.eclipse.pass.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@ServletComponentScan(basePackages = { "org.dataconservancy.pass.doi.service" })
@ComponentScan(basePackages = {"org.eclipse.pass.main", "org.dataconservancy.pass.doi.service" })
@EntityScan(basePackages = { "org.eclipse.pass.object.model" })
public class Main {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }
}
