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

package org.eclipse.pass.usertoken;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author apb@jhu.edu
 */
public class TokenTest {

    public void nullReferenceTest() {
        new Token(new Codec(Key.generate()), randomUri(), null);
    }

    public void nullResourceTest() {
        new Token(new Codec(Key.generate()), null, randomUri());
    }

    @Test
    public void roundTripTest() throws BadTokenException {

        final URI resource = randomUri();

        final URI reference = randomUri();

        final Codec codec = new Codec(Key.generate());

        final Token created = new Token(codec, resource, reference);

        final Token fromString = new Token(codec, created.toString());

        assertEquals(resource, fromString.getPassResource());
        assertEquals(reference, fromString.getReference());
    }

    public void garbageTokenTest() {

        final Codec codec = new Codec(Key.generate());

        Assertions.assertThrows(BadTokenException.class, () -> {
            new Token(codec, "blah");
        });
    }

    public void tooFewUrisTest() throws UnsupportedEncodingException {
        final Codec codec = new Codec(Key.generate());

        final String badTokenContent = encode("http://example.org", UTF_8.toString());

        Assertions.assertThrows(BadTokenException.class, () -> {
            new Token(codec, codec.encrypt(badTokenContent));
        });
    }

    public void tooManyUrisTest() throws UnsupportedEncodingException {
        final Codec codec = new Codec(Key.generate());

        Assertions.assertThrows(BadTokenException.class, () -> {
            String badTokenContent = null;
            badTokenContent += encode("http://example.org", UTF_8.toString()) + ",";
            badTokenContent += encode("http://example.org", UTF_8.toString()) + ",";
            badTokenContent += encode("http://example.org", UTF_8.toString());

            new Token(codec, codec.encrypt(badTokenContent));
        });
    }

    @Test
    public void addToUriWithNoParamsTest() throws BadTokenException {
        final URI initialUri = URI.create("https://fedoraAdmin:moo@127.0.0.1:8080/path#fragment");

        final URI resource = randomUri();

        final URI reference = randomUri();

        final Key key = Key.generate();

        final Codec codec = new Codec(key);

        final Token created = new Token(codec, resource, reference);

        final URI uriWithToken = created.addTo(initialUri);

        assertEquals(initialUri.getScheme(), uriWithToken.getScheme());
        assertEquals(initialUri.getAuthority(), uriWithToken.getAuthority());
        assertEquals(initialUri.getHost(), uriWithToken.getHost());
        assertEquals(initialUri.getPort(), uriWithToken.getPort());
        assertEquals(initialUri.getPath(), uriWithToken.getPath());
        assertEquals(initialUri.getFragment(), uriWithToken.getFragment());
        assertNotEquals(initialUri.getQuery(), uriWithToken.getQuery());

        final Token deserializedFromUrl = new TokenFactory(key).fromUri(uriWithToken);

        assertNotNull(deserializedFromUrl);
        assertEquals(resource, deserializedFromUrl.getPassResource());
        assertEquals(reference, deserializedFromUrl.getReference());

    }

    @Test
    public void addToUriWithParamsTest() throws BadTokenException {

        final String[] params = { "foo=bar", "baz=huh" };
        final URI initialUri = URI.create("https://fedoraAdmin:moo@127.0.0.1:8080/path?" + String.join("&", params) +
                "#fragment");

        final URI resource = randomUri();

        final URI reference = randomUri();

        final Key key = Key.generate();

        final Codec codec = new Codec(key);

        final Token created = new Token(codec, resource, reference);

        final URI uriWithToken = created.addTo(initialUri);

        assertEquals(initialUri.getScheme(), uriWithToken.getScheme());
        assertEquals(initialUri.getAuthority(), uriWithToken.getAuthority());
        assertEquals(initialUri.getHost(), uriWithToken.getHost());
        assertEquals(initialUri.getPort(), uriWithToken.getPort());
        assertEquals(initialUri.getPath(), uriWithToken.getPath());
        assertEquals(initialUri.getFragment(), uriWithToken.getFragment());
        assertNotEquals(initialUri.getQuery(), uriWithToken.getQuery());

        assertTrue(uriWithToken.getQuery().contains(params[0]));
        assertTrue(uriWithToken.getQuery().contains(params[1]));

        final Token deserializedFromUrl = new TokenFactory(key).fromUri(uriWithToken);

        assertNotNull(deserializedFromUrl);
        assertEquals(resource, deserializedFromUrl.getPassResource());
        assertEquals(reference, deserializedFromUrl.getReference());

    }

    @Test
    public void addToUriWithEncodedPathsAndParamsTest() throws BadTokenException {
        final String[] params = { "foo=bar", "baz=huh" };
        final String encodedPathPart =
                "https%3A%2F%2Fpass.local%2Ffcrepo%2Frest%2Fsubmissions%2F91%2F75%2Fba%2Fe6%2F9175ba";
        final URI initialUri = URI.create("https://fedoraAdmin:moo@127.0.0.1:8080/path/to/" +
                encodedPathPart + "?" + String.join("&", params) + "#fragment");

        final URI resource = randomUri();

        final URI reference = randomUri();

        final Key key = Key.generate();

        final Codec codec = new Codec(key);

        final Token created = new Token(codec, resource, reference);

        final URI uriWithToken = created.addTo(initialUri);

        assertTrue(initialUri.toString().contains(encodedPathPart));
        assertTrue(uriWithToken.toASCIIString().contains(encodedPathPart));

        assertEquals(initialUri.getScheme(), uriWithToken.getScheme());
        assertEquals(initialUri.getAuthority(), uriWithToken.getAuthority());
        assertEquals(initialUri.getHost(), uriWithToken.getHost());
        assertEquals(initialUri.getPort(), uriWithToken.getPort());
        assertEquals(initialUri.getPath(), uriWithToken.getPath());
        assertEquals(initialUri.getFragment(), uriWithToken.getFragment());
        assertNotEquals(initialUri.getQuery(), uriWithToken.getQuery());

        assertTrue(uriWithToken.getQuery().contains(params[0]));
        assertTrue(uriWithToken.getQuery().contains(params[1]));

        final Token deserializedFromUrl = new TokenFactory(key).fromUri(uriWithToken);

        assertNotNull(deserializedFromUrl);
        assertEquals(resource, deserializedFromUrl.getPassResource());
        assertEquals(reference, deserializedFromUrl.getReference());
    }

    private static URI randomUri() {
        return URI.create("urn:uuid:" + UUID.randomUUID().toString());
    }
}
