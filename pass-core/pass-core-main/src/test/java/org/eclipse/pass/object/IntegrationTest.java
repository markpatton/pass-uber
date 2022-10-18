/*
 * Copyright 2019, Yahoo
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package org.eclipse.pass.object;

import com.jayway.restassured.RestAssured;
import org.eclipse.pass.main.Main;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

/**
 * Base class for running a set of functional Elide tests.  This class
 * sets up an Elide instance with an in-memory H2 database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTest {

    @LocalServerPort
    int port;

    @BeforeAll
    public void setUp() {
        RestAssured.port = port;
    }
}
