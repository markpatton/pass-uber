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

import org.eclipse.pass.object.model.PassEntity;

/**
 * PassClientSelector is used to select objects in the repository.
 * See <a href="https://elide.io/pages/guide/v6/10-jsonapi.html">Elide JSON-API</a> for information on the
 * sort and filter syntax.
 */
public class PassClientSelector<T extends PassEntity> {
    private static final int DEFAULT_LIMIT = 500;

    private int offset;
    private int limit;
    private Class<T> type;
    private String sorting;
    private String filter;

    /**
     * Match all objects of the given type.
     *
     * @param type object type to match
     */
    public PassClientSelector(Class<T> type) {
        this(type, 0, DEFAULT_LIMIT, null, null);
    }

    /**
     * Match objects in the repository.
     *
     *
     * @param type Match objects of this type
     * @param offset Return objects starting at this location in the list of total results
     * @param limit Return at most this many matching objects
     * @param filter Return objects which match this RSQL filter or null for no filter
     * @param sorting Sort objects in this fashion or null for no sorting
     */
    public PassClientSelector(Class<T> type, int offset, int limit, String filter, String sorting) {
        this.offset = offset;
        this.limit = limit;
        this.type = type;
        this.filter = filter;
        this.sorting = sorting;
    }

    /**
     *
     * @return The offset into the list of total objects to return.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Set the offset into the list of total objects to return.
     * @param offset The offset into the list of total objects to return.
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     *
     * @return The maximum number of PassEntity objects to return.
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Set the maximum number of PassEntity objects to return.
     * @param limit The maximum number of PassEntity objects to return.
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     *
     * @return The type of PassEntity objects to return.
     */
    public Class<? extends PassEntity> getType() {
        return type;
    }

    /**
     * Set the type of PassEntity objects to return.
     * @param type The type of PassEntity objects to return.
     */
    public void setType(Class<T> type) {
        this.type = type;
    }

    /**
     *
     * @return The sorting used to order the PassEntity objects.
     */
    public String getSorting() {
        return sorting;
    }

    /**
     * Set the sorting used to order the PassEntity objects.
     * @param sorting The sorting used to order the PassEntity objects.
     */
    public void setSorting(String sorting) {
        this.sorting = sorting;
    }

    /**
     *
     * @return The filter used to select the PassEntity objects.
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Set the filter used to select the PassEntity objects.
     * @param filter The filter used to select the PassEntity objects.
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }
}
