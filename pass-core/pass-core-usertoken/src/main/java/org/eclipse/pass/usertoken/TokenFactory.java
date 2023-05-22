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

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory for creating tokens, or extracting them from URIs.
 *
 * @author apb@jhu.edu
 */
public class TokenFactory {
    private final Codec codec;

    private static final Pattern userTokenPattern = Pattern.compile(".*" + Token.USER_TOKEN_PARAM + "=([A-Z2-7]+).*");

    /**
     * Instantiate a TokenFactory that will (de)serialize tokens using the given encryption key
     *
     * @param key Key to use for serialization and deserialization of the token,
     */
    public TokenFactory(Key key) {
        this.codec = new Codec(key);
    }

    /**
     * Instantiate the TokenFactory with a base32-encoded key.
     * <p>
     * This is typically how a TokenFactory is instantiated from configuration, using an
     * easily-serializable-as-a-string key.
     * </p>
     *
     * @param key Base32 encoded encryption key.
     */
    public TokenFactory(String key) {
        this.codec = new Codec(Key.fromString(key));
    }

    /**
     * Create a {@link Token} for a PASS resource.
     *
     * @param type of PASS resource
     * @param id of PASS resource
     * @param reference URI/reference associated with this token (e.g. in the proxy use case, the mailto URI).
     * @return the token
     */
    public Token forPassResource(String type, long id, URI reference) {
        URI res = URI.create(Token.PASS_RESOURCE_SCHEME + ":" + type + ":" + id);
        return new Token(codec, res, reference);
    }

    /**
     * Decode a token from an encoded string.
     * <p>
     * Included for completeness, just in case a token outside of the context of a URL needs decoding. Prefer
     * {@link #fromUri(URI)}
     * </p>
     *
     * @param encoded String containing the encoded token.
     * @return The token.
     * @throws BadTokenException thrown if encoded token is invalid
     */
    public Token from(String encoded) throws BadTokenException {
        return new Token(codec, encoded);
    }

    /**
     * Decode a token from a URI containing a userToken parameter.
     *
     * @param uri The URI to inspect
     * @return the Token, or null if none are present.
     * @throws BadTokenException thrown if token is invalid
     */
    public Token fromUri(URI uri) throws BadTokenException {
        final Matcher tokenMatcher = userTokenPattern.matcher(uri.getQuery());
        if (tokenMatcher.matches()) {
            return new Token(codec, tokenMatcher.group(1));
        } else {
            return null;
        }
    }

    /**
     * Decode a token from a query string containing a userToken parameter.
     *
     * @param query The query to inspect
     * @return the Token, or null if none are present.
     * @throws BadTokenException thrown if token is invalid
     */
    public Token fromUri(String query) throws BadTokenException {
        final Matcher tokenMatcher = userTokenPattern.matcher(query);
        if (tokenMatcher.matches()) {
            return new Token(codec, tokenMatcher.group(1));
        } else {
            return null;
        }
    }

    /**
     * Determine if a URI has a token in it.
     *
     * @param uri URI to inspect.
     * @return true, if the URI has a token parameter in it.
     */
    public boolean hasToken(URI uri) {
        return hasToken(uri.getQuery());
    }

    /**
     * Determing if a query string has a token in it.
     *
     * @param query the query to check for token
     * @return true if the query has a token parameter in it
     */
    public boolean hasToken(String query) {
        return userTokenPattern.matcher(query).matches();
    }

    Codec getCodec() {
        return codec;
    }
}
