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

package org.eclipse.pass.object.model.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IdentifierTests {

    String domain = "example.org";
    String type = "unique";
    String value = "007";

    @Test
    public void testConstructor() {
        Identifier identifier = new Identifier(domain, type, value);

        assertEquals(identifier.getDomain(), domain);
        assertEquals(identifier.getType(), type);
        assertEquals(identifier.getValue(), value);

        identifier.setValue("008");

        assertEquals(identifier.getValue(), "008");
    }

    @Test
    public void testSerializer() {
        Identifier identifier = new Identifier(domain, type, value);
        String ser = identifier.serialize();
        assertEquals(String.join(":", domain, type, value), ser);
    }

    @Test
    public void testSerializerMissingParts() {
        Identifier identifier = new Identifier(null, type, value);
        assertNull(identifier.serialize());

        identifier = new Identifier(domain, null, value);
        assertNull(identifier.serialize());

        identifier = new Identifier(domain, type, null);
        assertNull(identifier.serialize());

    }

    @Test
    public void testDeserialize() {
        Identifier expected = new Identifier(domain, type, value);
        String ser = String.join(":", domain, type, value);

        assertTrue(expected.equals(Identifier.deserialize(ser)));
    }

    @Test
    public void testRoundTrip() {
        Identifier expected = new Identifier(domain, type, value);
        assertEquals(expected, Identifier.deserialize(expected.serialize()));

        String ser = String.join(":", domain, type, value);
        assertEquals(ser, Identifier.deserialize(ser).serialize());
    }
}
