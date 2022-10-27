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

package org.eclipse.pass.object.model.support;

/**
 * This is a convenience class for handling identifiers which we use for locating objects in Fedora.
 * These identifiers are serialized to work well with our JSON-LD transport. We localize these identifiers by
 * domain so that a Fedora instance may support more than one institution. We add a type so that different
 * identifiers for the same PASS object can be looked for, as the JHU use cases illuminate several edge cases
 * where a single identifier will not be enough to determine whether a user object is present in Fedora.
 * This has to do with the variable availability of identifiers in different systems, and for users with different
 * status (active or inactive member of the JHU community; active or inactive JHU employee).
 */
public class Identifier {

    private String domain;
    private String type;
    private String value;

    public Identifier(String domain, String type, String value) {
        this.domain = domain;
        this.type = type;
        this.value = value;
    }

    public String serialize() {
        if (domain != null && type != null && value != null) {
            return String.join(":", domain, type, value);
        }
        return null;
    }

    public static Identifier deserialize(String serializedIdentifier) {
        String[] parts = serializedIdentifier.split(":", 3);
        if (parts.length == 3) {
            return new Identifier(parts[0], parts[1], parts[2]);
        }
        return null;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Identifier that = (Identifier) o;

        if (domain != null ? !domain.equals(that.domain) : that.domain != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 31;
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

}
