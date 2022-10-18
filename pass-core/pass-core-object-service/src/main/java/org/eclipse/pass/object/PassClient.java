/*
 * Copyright 2022 Johns Hopkins University
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
package org.eclipse.pass.object;

import java.io.Closeable;
import java.io.IOException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.pass.object.model.PassEntity;

/**
 * PassClient represents a session with the PASS repository.
 */
interface PassClient extends Closeable {
    /**
     * Create a new object in the repository.
     * The id of the object must be null and will be set by the method.
     *
     * @param <T>
     * @param obj
     * @throws IOException
     */
    <T extends PassEntity> void createObject(T obj) throws IOException;

    /**
     * Update an existing object.
     *
     * @param <T>
     * @param obj
     * @throws IOException
     */
    <T extends PassEntity> void updateObject(T obj) throws IOException;

    /**
     * Retrieve an object from the repository.
     *
     * @param <T>
     * @param type
     * @param id
     * @return Persisted object or null
     * @throws IOException
     */
    <T extends PassEntity> T getObject(Class<T> type, Long id) throws IOException;

    /**
     * Delete an object in the repository.
     *
     * @param <T>
     * @param type
     * @param id
     * @throws IOException
     */
    <T extends PassEntity> void deleteObject(Class<T> type, Long id) throws IOException;

    /**
     * Delete the object in the repository with the given type and id.
     *
     * @param <T>
     * @param obj
     * @throws IOException
     */
    default <T extends PassEntity> void deleteObject(T obj) throws IOException {
        deleteObject(obj.getClass(), obj.getId());
    }

    /**
     * Select objects from the repository matching the selector.
     *
     * @param <T>
     * @param selector
     * @return Matching objects
     * @throws IOException
     */
    <T extends PassEntity> PassClientResult<T> selectObjects(PassClientSelector<T> selector) throws IOException;

    /**
     * Stream all objects in the repository matching the selector starting from the selector offset.
     *
     * @param <T>
     * @param selector
     * @return Stream
     * @throws IOException
     */
    default <T extends PassEntity> Stream<T> streamObjects(PassClientSelector<T> selector) throws IOException {
        Spliterator<T> iter = new Spliterator<T>() {
            PassClientResult<T> result = selectObjects(selector);
            int next = 0;

            @Override
            public int characteristics() {
                return NONNULL | CONCURRENT;
            }

            @Override
            public long estimateSize() {
                return result.getTotal();
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> consumer) {
                if (next == result.getEntities().size()) {
                    try {
                        selector.setOffset(selector.getOffset() + selector.getLimit());
                        result = selectObjects(selector);
                        next = 0;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (result.getEntities().size() == 0) {
                        return false;
                    }
                }

                consumer.accept(result.getEntities().get(next++));
                return true;
            }

            @Override
            public Spliterator<T> trySplit() {
                return null;
            }
        };

        return StreamSupport.stream(iter, false);
    }
}