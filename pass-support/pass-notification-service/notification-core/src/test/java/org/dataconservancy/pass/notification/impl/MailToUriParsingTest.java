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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;

import org.junit.Test;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 * @see <a href="https://tools.ietf.org/html/rfc6068">RFC 6068</a>
 */
public class MailToUriParsingTest {

    @Test
    public void parseSimpleMailtoUri() {
        String mailto = "mailto:chris@example.com";
        URI uri = URI.create(mailto);

        assertEquals("mailto", uri.getScheme());
        assertEquals("chris@example.com", uri.getSchemeSpecificPart());
    }

    @Test
    public void parseMailtoUriWithSingleParameter() {
        String mailto = "mailto:infobot@example.com?subject=current-issue";
        URI uri = URI.create(mailto);

        assertEquals("infobot@example.com?subject=current-issue", uri.getSchemeSpecificPart());
        assertNull(uri.getQuery());
        assertNull(uri.getPath());
    }

    @Test
    public void parseMailtoWithMultipleParameters() {
        String mailto = "mailto:joe@example.com?cc=bob@example.com&body=hello";
        URI uri = URI.create(mailto);

        assertEquals("joe@example.com?cc=bob@example.com&body=hello", uri.getSchemeSpecificPart());
        assertNull(uri.getQuery());
        assertNull(uri.getPath());
    }

    @Test
    public void parseMailtoWithEscapedParameters() {
        String mailto = "mailto:majordomo@example.com?body=subscribe%20bamboo-l";
        URI uri = URI.create(mailto);

        // note: result is automatically decoded
        assertEquals("majordomo@example.com?body=subscribe bamboo-l", uri.getSchemeSpecificPart());
        assertNull(uri.getQuery());
        assertNull(uri.getPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseMailtoWithTrailingWhitespace() {
        String mailto = "mailto:chris@example.com ";
        URI uri = URI.create(mailto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseMailtoWithPreceedingWhitespace() {
        String mailto = " mailto:chris@example.com";
        URI uri = URI.create(mailto);
    }

    @Test
    public void parseBracketNotEncoded() {
        String mailto = "mailto:John%20Doe%3Cjohndoe%40example.org%3E";
        URI uri = URI.create(mailto);
        assertEquals("mailto", uri.getScheme());
        assertEquals("John Doe<johndoe@example.org>", uri.getSchemeSpecificPart());
    }
}
