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
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author apb@jhu.edu
 */
public class KeyTest {

    Base32 base32 = new Base32();

    @Test
    public void nullTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Key.fromString(null);
        });
    }

    @Test
    public void wrongKeyLengthTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Key.fromString(base32.encodeAsString("hello".getBytes()));
        });
    }

    @Test
    public void badEncodingTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Key.fromString("no@#*@$@");
        });
    }

    @Test
    public void generateUniqueTest() {
        assertNotSame(Key.generate().toString(), Key.generate().toString());
    }

    @Test
    public void roundTripTest() {
        final Key initial = Key.generate();
        final Key roundtripped = Key.fromString(initial.toString());

        assertEquals(initial.toString(), roundtripped.toString());
    }
}
