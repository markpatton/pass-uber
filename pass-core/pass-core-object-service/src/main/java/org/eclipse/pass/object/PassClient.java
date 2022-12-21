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

import com.yahoo.elide.RefreshableElide;
import org.eclipse.pass.object.model.PassEntity;

/**
 * PassClient represents a session with the PASS repository. It should not be kept open long term.
 * Objects retrieved by a PassClient instance may only be used while that instance is open.
 */
public interface PassClient extends Closeable {

    /**
     * Return a new PassClient instance.
     *
     * @param elide Elide client will connect to
     * @return new instance
     */
    static PassClient newInstance(RefreshableElide elide) {
        return new ElideDataStorePassClient(elide);
    }

    /**
     * Create a new object in the repository.
     * The id of the object must be null and will be set by the method.
     *
     * @param <T> object type
     * @param obj object to create
     * @throws IOException if operation fails
     */
    <T extends PassEntity> void createObject(T obj) throws IOException;

    /**
     * Update an existing object.
     *
     * @param <T> object type
     * @param obj object to persist
     * @throws IOException if operation fails
     */
    <T extends PassEntity> void updateObject(T obj) throws IOException;

    /**
     * Retrieve an object from the repository.
     *
     * @param <T> object type
     * @param type class of the object
     * @param id identifier of the object
     * @return Persisted object or null
     * @throws IOException if operation fails
     */
    <T extends PassEntity> T getObject(Class<T> type, Long id) throws IOException;

    /**
     * Delete the object in the repository with the given type and id.
     *
     * @param <T> object type
     * @param type class of the object
     * @param id identifier of the object
     * @throws IOException if operation fails
     */
    <T extends PassEntity> void deleteObject(Class<T> type, Long id) throws IOException;

    /**
     * Delete the object in the repository.
     *
     * @param <T> object type
     * @param obj object to delete
     * @throws IOException if operation fails
     */
    default <T extends PassEntity> void deleteObject(T obj) throws IOException {
        deleteObject(obj.getClass(), obj.getId());
    }

    /**
     * Select objects from the repository matching the selector.
     *
     * @param <T> object type
     * @param selector determines which objects to retrieve
     * @return Matching objects
     * @throws IOException if operation fails
     */
    <T extends PassEntity> PassClientResult<T> selectObjects(PassClientSelector<T> selector) throws IOException;

    /**
     * Stream all objects in the repository matching the selector starting from the selector offset.
     *
     * @param <T> object type
     * @param selector determines which objects to retrieve
     * @return Stream of matching objects
     * @throws IOException if operation fails
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
                if (next == result.getObjects().size()) {
                    try {
                        selector.setOffset(selector.getOffset() + selector.getLimit());
                        result = selectObjects(selector);
                        next = 0;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (result.getObjects().size() == 0) {
                        return false;
                    }
                }

                consumer.accept(result.getObjects().get(next++));
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