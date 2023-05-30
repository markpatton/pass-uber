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

package org.eclipse.pass.notification.service;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.empty;
import static org.eclipse.pass.notification.service.LinksUtil.concat;
import static org.eclipse.pass.notification.service.LinksUtil.deserialize;
import static org.eclipse.pass.notification.service.LinksUtil.optional;
import static org.eclipse.pass.notification.service.LinksUtil.required;
import static org.eclipse.pass.notification.service.LinksUtil.serialized;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.eclipse.pass.notification.model.Link;
import org.junit.jupiter.api.Test;

/**
 * @author apb@jhu.edu
 */
public class LinksTest {

    @Test
    public void serializationRoundTripTest() {
        final Collection<Link> links = asList(
                new Link(randomUri(), "rel1"),
                new Link(randomUri(), "rel2"));

        final Collection<Link> roundTrippedLinks = deserialize(links.stream().collect(serialized()));

        assertEquals(links.size(), roundTrippedLinks.size());
        assertTrue(roundTrippedLinks.containsAll(links));

    }

    // Make sure nothing bad happens with the edge case of serializing zero links
    @Test
    public void concatZeroMembersSerializationTest() {
        assertTrue(deserialize(concat().collect(serialized())).isEmpty());
    }

    // If an "optional" link is present, it should form a stream of exactly one member.
    @Test
    public void optionalLinkPresentTest() {

        final URI uri = randomUri();
        final String rel = "rel1";

        final List<Link> links = optional(uri, rel).collect(toList());
        assertEquals(1, links.size());
        assertEquals(uri, links.get(0).getHref());
        assertEquals(rel, links.get(0).getRel());
    }

    @Test
    public void optionalLinkNotPresentTest() {
        assertTrue(optional(null, "rel").collect(toList()).isEmpty());
    }

    @Test
    public void optionalLinkNotPresentNulRelTest() {
        assertTrue(optional(null, null).collect(toList()).isEmpty());
    }

    @Test
    public void requiredLinkPresentTest() {
        final URI uri = randomUri();
        final String rel = "rel1";

        final List<Link> links = required(uri, rel).collect(toList());
        assertEquals(1, links.size());
        assertEquals(uri, links.get(0).getHref());
        assertEquals(rel, links.get(0).getRel());
    }

    @Test
    public void requiredLinkNotPresentTest() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            required(null, "abc123");
        });
        assertTrue(ex.getMessage().contains("abc123"));
    }

    @Test
    public void requiredLinkRelNotPresentTest() {
        final URI uri = randomUri();
        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            required(uri, null);
        });
        assertTrue(ex.getMessage().contains(uri.toString()));
    }

    @Test
    public void concatenateTest() {
        final Collection<Link> links1 = asList(
                new Link(randomUri(), "rel1"),
                new Link(randomUri(), "rel2"));

        final Collection<Link> links2 = asList(
                new Link(randomUri(), "rel1"),
                new Link(randomUri(), "rel2"));

        final List<Link> concatenated = concat(links1.stream(), links2.stream(), empty(), null).collect(toList());

        assertEquals(links1.size() + links2.size(), concatenated.size());
        assertTrue(concatenated.containsAll(links1));
        assertTrue(concatenated.containsAll(links2));
    }

    static URI randomUri() {
        return URI.create("urn:uuid:" + UUID.randomUUID().toString());
    }
}
