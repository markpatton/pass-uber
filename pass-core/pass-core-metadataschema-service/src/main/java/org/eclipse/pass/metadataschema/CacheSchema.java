/*
 *
 * Copyright 2023 Johns Hopkins University
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.eclipse.pass.metadataschema;

import com.fasterxml.jackson.databind.JsonNode;

public class CacheSchema {
    private JsonNode schema;
    private long lastModified;

    public CacheSchema(JsonNode schema, long lastModified) {
        this.schema = schema;
        this.lastModified = lastModified;
    }

    public JsonNode getSchema() {
        return schema;
    }

    public long getLastModified() {
        return lastModified;
    }
}