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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideResponse;
import com.yahoo.elide.ElideSettings;
import com.yahoo.elide.RefreshableElide;
import com.yahoo.elide.core.RequestScope;
import com.yahoo.elide.core.datastore.DataStoreTransaction;
import com.yahoo.elide.core.dictionary.EntityDictionary;
import com.yahoo.elide.core.security.User;
import com.yahoo.elide.core.type.ClassType;
import com.yahoo.elide.jsonapi.models.Data;
import com.yahoo.elide.jsonapi.models.JsonApiDocument;
import com.yahoo.elide.jsonapi.models.Relationship;
import com.yahoo.elide.jsonapi.models.Resource;
import com.yahoo.elide.jsonapi.models.ResourceIdentifier;
import org.eclipse.pass.object.model.PassEntity;

/**
 * PASS client which uses the HTTP verb methods of the main Elide class.
 * Hooks should be triggered and permissions will be checked.
 *
 * Objects retrieved using this client may not work after the client has been closed.
 * This is because relationships are loaded lazily.
 */
public class ElidePassClient implements PassClient {
    private final Elide elide;
    private final ElideSettings settings;
    private final User user;
    private final String api_version;
    private final DataStoreTransaction read_tx;

    public ElidePassClient(RefreshableElide refreshableElide, User user) {
        this.elide = refreshableElide.getElide();
        this.settings = elide.getElideSettings();
        this.user = user;
        this.api_version = settings.getDictionary().getApiVersions().iterator().next();
        this.read_tx = elide.getDataStore().beginReadTransaction();
    }

    private RequestScope get_scope(String path, DataStoreTransaction tx) {
        RequestScope scope = new RequestScope(settings.getBaseUrl(), path, api_version, null, tx, user, null, null,
                UUID.randomUUID(), settings);

        return scope;
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

    private JsonApiDocument to_json_api_doc(PassEntity obj) {
        EntityDictionary dict = settings.getDictionary();

        String typeName = EntityDictionary.getEntityName(ClassType.of(obj.getClass()));

        Resource resource = new Resource(typeName, obj.getId() == null ? "-1" : obj.getId().toString());

        Map<String, Relationship> relationships = new HashMap<>();

        for (String name : dict.getRelationships(obj)) {
            Object value = dict.getValue(obj, name, null);
            Data<Resource> data;

            if (value == null) {
                data = new Data<>((Resource) null);
            } else if (value instanceof List) {
                List<Resource> targets = new ArrayList<>();

                for (Object o : List.class.cast(value)) {
                    PassEntity entity = PassEntity.class.cast(o);
                    String target_type = EntityDictionary.getEntityName(ClassType.of(entity.getClass()));
                    String target_id = entity.getId().toString();
                    ResourceIdentifier target = new ResourceIdentifier(target_type, target_id);

                    targets.add(target.castToResource());
                }

                data = new Data<>(targets);
            } else if (value instanceof PassEntity) {
                PassEntity entity = PassEntity.class.cast(value);
                String target_type = EntityDictionary.getEntityName(ClassType.of(entity.getClass()));
                String target_id = entity.getId().toString();
                ResourceIdentifier target = new ResourceIdentifier(target_type, target_id);
                data = new Data<>(target.castToResource());
            } else {
                throw new RuntimeException("Unknown relationship target: " + value);
            }

            relationships.put(name, new Relationship(null, data));
        }

        resource.setRelationships(relationships);

        Map<String, Object> attributes = new HashMap<>();

        for (String name : dict.getAttributes(obj)) {
            attributes.put(name, dict.getValue(obj, name, null));
        }

        resource.setAttributes(attributes);

        return new JsonApiDocument(new Data<>(resource));
    }

    @Override
    public <T extends PassEntity> void createObject(T obj) throws IOException {
        String path = get_path(obj.getClass(), null);

        String json = elide.getMapper().writeJsonApiDocument(to_json_api_doc(obj));
        ElideResponse response = elide.post(settings.getBaseUrl(), path, json, user, api_version);

        if (response.getResponseCode() != 201) {
            throw new IOException("Failed to create object: " + response.getResponseCode() + " " + response.getBody());
        }

        String id = elide.getMapper().readJsonApiDocument(response.getBody()).getData().getSingleValue().getId();
        settings.getDictionary().setId(obj, id);
    }

    @Override
    public <T extends PassEntity> void updateObject(T obj) throws IOException {
        String path = get_path(obj.getClass(), obj.getId());

        String json = elide.getMapper().writeJsonApiDocument(to_json_api_doc(obj));
        ElideResponse response = elide.patch(settings.getBaseUrl(), Elide.JSONAPI_CONTENT_TYPE,
                Elide.JSONAPI_CONTENT_TYPE, path, json, user, api_version);

        int code = response.getResponseCode();

        if (code < 200 || code > 204) {
            throw new IOException("Failed to update object: " + code + " " + response.getBody());
        }
    }

    @Override
    public <T extends PassEntity> T getObject(Class<T> type, Long id) throws IOException {
        String path = get_path(type, id);

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        ElideResponse response = elide.get(settings.getBaseUrl(), path, params, user, api_version);

        if (response.getResponseCode() == 404) {
            return null;
        }

        if (response.getResponseCode() != 200) {
            throw new IOException("Failed to get object: " + response.getResponseCode() + " " + response.getBody());
        }

        JsonApiDocument doc = elide.getMapper().readJsonApiDocument(response.getBody());

        return type.cast(doc.getData().getSingleValue().toPersistentResource(get_scope(path, read_tx)).getObject());
    }

    @Override
    public <T extends PassEntity> void deleteObject(Class<T> type, Long id) throws IOException {
        String path = get_path(type, id);

        ElideResponse response = elide.delete(settings.getBaseUrl(), path, api_version, user, api_version);

        if (response.getResponseCode() != 204) {
            throw new IOException("Failed to delete object: " + response.getResponseCode() + " " + response.getBody());
        }
    }

    @Override
    public <T extends PassEntity> PassClientResult<T> selectObjects(PassClientSelector<T> selector) throws IOException {
        String path = get_path(selector.getType(), null);

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();

        if (selector.getFilter() != null) {
            params.add("filter", selector.getFilter());
        }

        if (selector.getSorting() != null) {
            params.add("sort", selector.getSorting());
        }

        params.add("page[offset]", "" + selector.getOffset());
        params.add("page[limit]", "" + selector.getLimit());
        params.add("page[totals]", null);

        ElideResponse response = elide.get(settings.getBaseUrl(), path, params, user, api_version);

        if (response.getResponseCode() == 404) {
            return null;
        }

        if (response.getResponseCode() != 200) {
            throw new IOException("Failed to get object: " + response.getResponseCode() + " " + response.getBody());
        }

        JsonApiDocument doc = elide.getMapper().readJsonApiDocument(response.getBody());

        Object totalval = doc.getMeta().getValue("page", Map.class).get("totalRecords");
        long total = -1;

        if (totalval != null) {
            total = Long.parseLong(totalval.toString());
        }

        PassClientResult<T> result = new PassClientResult<>(total);

        RequestScope scope = get_scope(path, read_tx);

        doc.getData().get().forEach(r -> {
            @SuppressWarnings("unchecked")
            T o = (T) r.toPersistentResource(scope).getObject();
            result.getObjects().add(o);
        });

        return result;
    }

    @Override
    public void close() throws IOException {
        read_tx.close();
    }
}