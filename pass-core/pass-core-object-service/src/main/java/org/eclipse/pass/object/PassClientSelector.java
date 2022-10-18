package org.eclipse.pass.object;

import org.eclipse.pass.object.model.PassEntity;

/**
 * PassClientSelector is used to select objects in the repository.
 * See https://elide.io/pages/guide/v6/10-jsonapi.html for information on the
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
     * @param type
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

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Class<? extends PassEntity> getType() {
        return type;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }

    public String getSorting() {
        return sorting;
    }

    public void setSorting(String sorting) {
        this.sorting = sorting;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
