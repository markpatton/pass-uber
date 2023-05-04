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

import com.jayway.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

/**
 * Run Elide with in memory database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class,
    properties = {"PASS_CORE_BACKEND_PASSWORD=test", "PASS_CORE_USERTOKEN_KEY=MRLDGRJUZ7DPVZEHLNXD7ACMC4"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTest {
    public static final String BACKEND_USER = "backend";
    public static final String BACKEND_PASSWORD = "test";
    public static final String USERTOKEN_KEY = "MRLDGRJUZ7DPVZEHLNXD7ACMC4";

    @LocalServerPort
    private int port;

    public String getBaseUrl() {
        return "http://localhost:" + port + "/";
    }

    public int getPort() {
        return port;
    }

    @BeforeAll
    public void setup() {
        RestAssured.port = port;
    }
}
