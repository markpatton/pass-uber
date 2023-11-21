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
import java.util.HashMap;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HandlebarsTest {

    private Handlebars handlebars;

    @BeforeEach
    public void setUp() throws Exception {
        handlebars = new Handlebars();
    }

    @Test
    public void simpleThis() throws IOException {
        Template t = handlebars.compileInline("Hello {{this}}");
        assertEquals("Hello world", t.apply("world"));
    }

    @Test
    public void simpleJson() throws IOException {
        HashMap<String, String> model = new HashMap<>() {
            {
                put("world", "foo");
            }
        };

        Template t = handlebars.compileInline("Hello {{world}}");
        assertEquals("Hello foo", t.apply(model));
    }
}
