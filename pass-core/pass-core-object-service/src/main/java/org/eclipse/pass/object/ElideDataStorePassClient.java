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

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideSettings;
import com.yahoo.elide.RefreshableElide;
import com.yahoo.elide.core.RequestScope;
import com.yahoo.elide.core.datastore.DataStoreIterable;
import com.yahoo.elide.core.datastore.DataStoreTransaction;
import com.yahoo.elide.core.dictionary.EntityDictionary;
import com.yahoo.elide.core.filter.dialect.ParseException;
import com.yahoo.elide.core.filter.expression.FilterExpression;
import com.yahoo.elide.core.pagination.PaginationImpl;
import com.yahoo.elide.core.request.EntityProjection;
import com.yahoo.elide.core.request.Pagination;
import com.yahoo.elide.core.type.ClassType;
import org.eclipse.pass.object.model.PassEntity;

/**
 * Use the internal Elide DataStore for implementation. There is a complicated
 * interplay between the RequestScope, PersistentResource, and
 * DataStoreTransaction. This approach means that life cycle hooks wills not be
 * called because they can only be added in PersistentResource.
 *
 * Objects retrieved using this client may not work after the client has been closed.
 * This is because relationships are loaded lazily.
 */
public class ElideDataStorePassClient implements PassClient {
    private final Elide elide;
    private final ElideSettings settings;
    private final DataStoreTransaction read_tx;

    public ElideDataStorePassClient(RefreshableElide refreshableElide) {
        this.elide = refreshableElide.getElide();
        this.settings = elide.getElideSettings();

        // Keep a read transaction open for interacting with objects which have lazy loading relationships
        this.read_tx = elide.getDataStore().beginReadTransaction();
    }

    private RequestScope get_scope(String path, DataStoreTransaction tx) {
        String version = settings.getDictionary().getApiVersions().iterator().next();
        RequestScope scope = new RequestScope(settings.getBaseUrl(), path, version, null, tx, null, null, null,
                UUID.randomUUID(), settings);

        return scope;
    }

    private EntityProjection get_projection(RequestScope scope, PassClientSelector<?> selector) throws IOException {
        Pagination pagination = new PaginationImpl(selector.getType(), selector.getOffset(), selector.getLimit(),
                settings.getDefaultPageSize(), settings.getDefaultMaxPageSize(), true, false);

        FilterExpression filter = null;

        if (selector.getFilter() != null) {
            try {
                MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
                params.add("filter", selector.getFilter());
                filter = scope.getFilterDialect().parseGlobalExpression(scope.getPath(), params, scope.getApiVersion());
            } catch (ParseException e) {
                throw new IOException("Failed to parse filter of selector: " + selector.getFilter(), e);
            }
        }

        return EntityProjection.builder().type(selector.getType()).pagination(pagination).filterExpression(filter)
                .build();
    }

    private String get_path(Class<?> type, Long id) {
        StringBuilder result = new StringBuilder();

        result.append('/');
        result.append(EntityDictionary.getEntityName(ClassType.of(type)));

        if (id != null) {
            result.append('/');
            result.append(id);
        }

        return result.toString();
    }

    @Override
    public <T extends PassEntity> void createObject(T obj) throws IOException {
        try (DataStoreTransaction tx = elide.getDataStore().beginTransaction()) {
            String path = get_path(obj.getClass(), null);
            RequestScope scope = get_scope(path, tx);

            tx.preCommit(scope);
            tx.createObject(obj, scope);
            tx.flush(scope);
            tx.commit(scope);
        }
    }

    @Override
    public <T extends PassEntity> void updateObject(T obj) throws IOException {
        try (DataStoreTransaction tx = elide.getDataStore().beginTransaction()) {
            String path = get_path(obj.getClass(), obj.getId());
            RequestScope scope = get_scope(path, tx);

            tx.preCommit(scope);
            tx.save(obj, scope);
            tx.commit(scope);
            tx.flush(scope);
        }
    }

    @Override
    public <T extends PassEntity> T getObject(Class<T> type, Long id) throws IOException {
        String path = get_path(type, id);
        RequestScope scope = get_scope(path, read_tx);
        EntityProjection projection = EntityProjection.builder().type(type).build();

        return read_tx.loadObject(projection, id, scope);
    }

    @Override
    public <T extends PassEntity> PassClientResult<T> selectObjects(PassClientSelector<T> selector) throws IOException {
        String path = get_path(selector.getType(), null);
        RequestScope scope = get_scope(path, read_tx);
        EntityProjection proj = get_projection(scope, selector);

        DataStoreIterable<T> iterable = read_tx.loadObjects(proj, scope);

        long total = -1;
        Pagination pagination = proj.getPagination();
        if (pagination != null) {
            total = pagination.getPageTotals();
        }

        PassClientResult<T> result = new PassClientResult<>(total);
        List<T> entities = result.getEntities();
        iterable.forEach(entities::add);

        return result;
    }

    @Override
    public <T extends PassEntity> void deleteObject(Class<T> type, Long id) throws IOException {
        try (DataStoreTransaction tx = elide.getDataStore().beginTransaction()) {
            String path = get_path(type, id);
            RequestScope scope = get_scope(path, tx);
            EntityProjection projection = EntityProjection.builder().type(type).build();

            tx.preCommit(scope);
            tx.delete(tx.loadObject(projection, id, scope), scope);
            tx.commit(scope);
            tx.flush(scope);
        }
    }

    @Override
    public void close() throws IOException {
        read_tx.close();
    }
}