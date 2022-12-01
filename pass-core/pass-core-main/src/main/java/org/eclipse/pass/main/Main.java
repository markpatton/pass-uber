/*
 * Copyright 2022 Johns Hopkins University
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
package org.eclipse.pass.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.eclipse.pass.main", "org.eclipse.pass.doi.service",
    "org.eclipse.pass.file.service", "org.eclipse.pass.file.service.storage"})
@EntityScan(basePackages = { "org.eclipse.pass.object.model" })
public class Main {
    protected Main() {}

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }
}