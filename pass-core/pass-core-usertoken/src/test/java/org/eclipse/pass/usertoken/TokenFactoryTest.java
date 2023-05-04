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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * @author apb@jhu.edu
 */
public class TokenFactoryTest {

    private static URI randomUri() {
        return URI.create("urn:uuid:" + UUID.randomUUID().toString());
    }

    @Test
    public void fromEncodedTokenTest() throws BadTokenException {
        final TokenFactory toTest = new TokenFactory(Key.generate());

        final URI resource = randomUri();

        final URI reference = randomUri();

        final Token token = new Token(toTest.codec, resource, reference);

        final Token fromString = toTest.from(token.toString());

        assertEquals(reference, fromString.getReference());
        assertEquals(resource, fromString.getPassResource());
    }

    @Test
    public void buildTokenTest() {

        final String type = "submission";
        final long id = new Random().nextLong();

        final URI reference = randomUri();

        final TokenFactory toTest = new TokenFactory(Key.generate());

        final Token created = toTest.forPassResource(type, id, reference);

        assertEquals(reference, created.getReference());
        assertEquals(type, created.getPassResourceType());
        assertEquals(id, created.getPassResourceIdentifier());
    }

    @Test
    public void initializeWithStringTest() throws BadTokenException {

        final Key key = Key.generate();

        final TokenFactory factory = new TokenFactory(key.toString());

        final Codec withKnownKey = new Codec(key);

        final String TEST = "test";

        assertEquals(TEST, factory.codec.decrypt(withKnownKey.encrypt(TEST)));

    }

    @Test
    public void fromUriTest() throws BadTokenException {

        final Key key = Key.generate();

        final TokenFactory toTest = new TokenFactory(key);

        final String type = "cow";
        final long id = 234234;

        final URI reference = randomUri();

        final Token token = toTest.forPassResource(type, id, reference);

        final URI uriWithToken = URI.create("https://fedoraAdmin:moo@pass.local:8080/path/to/whatever?"
                + Token.USER_TOKEN_PARAM + "=" + token.toString() + "#part");

        assertTrue(toTest.hasToken(uriWithToken));

        final Token decoded = toTest.fromUri(uriWithToken);

        assertEquals(type, decoded.getPassResourceType());
        assertEquals(id, decoded.getPassResourceIdentifier());
        assertEquals(reference, decoded.getReference());
    }

    @Test
    public void fromUriWithOtherParamsTest() throws BadTokenException {
        final Key key = Key.generate();

        final TokenFactory toTest = new TokenFactory(key);

        final String type = "blah";
        final long id = 34;

        final URI reference = randomUri();

        final Token token = toTest.forPassResource(type, id, reference);

        final URI uriWithToken = URI.create("https://fedoraAdmin:moo@pass.local:8080/path/to/whatever?whatever=foo&"
                + Token.USER_TOKEN_PARAM + "=" + token.toString() + "&bar=keep#part");

        assertTrue(toTest.hasToken(uriWithToken));

        final Token decoded = toTest.fromUri(uriWithToken);

        assertEquals(type, decoded.getPassResourceType());
        assertEquals(id, decoded.getPassResourceIdentifier());
        assertEquals(reference, decoded.getReference());
    }

    @Test
    public void fromUriWithNoTokenTest() throws BadTokenException {
        final Key key = Key.generate();

        final TokenFactory toTest = new TokenFactory(key);

        final URI uriWithToken = URI
                .create("https://fedoraAdmin:moo@pass.local:8080/path/to/whatever?whatever=foo&bar=whatever#part");

        assertFalse(toTest.hasToken(uriWithToken));
        assertNull(toTest.fromUri(uriWithToken));
    }
}
