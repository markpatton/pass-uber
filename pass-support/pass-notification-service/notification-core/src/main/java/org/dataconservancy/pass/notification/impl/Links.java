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

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collector.Characteristics.UNORDERED;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dataconservancy.pass.notification.model.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for working with streams of {@link Link}
 *
 * @author apb@jhu.edu
 */
public class Links {

    private Links() {
    }

    private static Logger LOG = LoggerFactory.getLogger(Links.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    static final Collector<Link, Collection<Link>, String> LINK_COLLECTOR =
            new Collector<Link, Collection<Link>, String>() {

                @Override
                public BiConsumer<Collection<Link>, Link> accumulator() {
                    return (links, link) -> links.add(link);
                }

                @Override
                public Set<Characteristics> characteristics() {
                    return new HashSet<>(asList(UNORDERED));
                }

                @Override
                public BinaryOperator<Collection<Link>> combiner() {
                    return (c1, c2) -> {
                        c1.addAll(c2);
                        return c1;
                    };
                }

                @Override
                public Function<Collection<Link>, String> finisher() {
                    return links -> {
                        try {
                            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(links);
                        } catch (final JsonProcessingException e) {
                            throw new RuntimeException("Could not serialize links! ", e);
                        }
                    };
                }

                @Override
                public Supplier<Collection<Link>> supplier() {
                    return () -> new HashSet<>();
                }

            };

    /**
     * Convenience method to concatenate several link streams into one.
     *
     * @param streams zero or more streams of links
     * @return Single stream of links.
     */
    @SafeVarargs
    public static Stream<Link> concat(Stream<Link>... streams) {
        return Stream.of(streams)
                .filter(s -> s != null)
                .flatMap(s -> s);
    }

    /**
     * Create a stream containing a single link that is required to exist.
     * <p>
     * Verifies that the provided URI is not null.
     * </p>
     *
     * @param href URI being linked to.
     * @param rel The link relation.
     * @return Stream containing the required link.
     */
    public static Stream<Link> required(URI href, String rel) {
        return required("", href, rel);
    }

    /**
     * Create a stream containing a single link that is required to exist.
     * <p>
     * Verifies that the provided URI is not null.
     * </p>
     *
     * @param message Error message to be included if an exception is thrown.
     * @param href URI being linked to.
     * @param rel The link relation.
     * @return Stream containing the required link.
     */
    public static Stream<Link> required(String message, URI href, String rel) {
        requireNonNull(rel, format("%s Link relation for <%s> must not be null", message, href));
        requireNonNull(href, format("%s Required link %s is null", message, rel));

        return Stream.of(new Link(href, rel));
    }

    /**
     * Create a stream containing zero or one links.
     * <p>
     * If the provided URI is not null, then the stream will contain one link.
     * </p>
     *
     * @param href URI being linked to.
     * @param rel The link relation.
     * @return Stream containing zero or one links.
     */
    public static Stream<Link> optional(URI href, String rel) {
        if (href != null) {
            return required(href, rel);
        } else {
            LOG.debug("Optional link {} is null, ignoring", rel);
            return Stream.empty();
        }
    }

    /**
     * Collect a stream of links into a single JSON array.
     *
     * @return The stream collector.
     */
    public static Collector<Link, Collection<Link>, String> serialized() {
        return LINK_COLLECTOR;
    }

    /**
     * Deserialize a JSON array of links.
     *
     * @param json Serialized JSON
     * @return collection of links.
     */
    @SuppressWarnings("unchecked")
    public static Collection<Link> deserialize(String json) {
        try {
            return asList(mapper.readValue(json, Link[].class));
        } catch (final Exception e) {
            throw new RuntimeException(format("Could not deserialize json: '%s'", json), e);
        }
    }

}
