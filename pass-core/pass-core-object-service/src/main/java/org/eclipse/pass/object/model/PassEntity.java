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
package org.eclipse.pass.object.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Abstract method that all PASS model entities inherit from. All entities can include
 * a unique ID, type, and context
 *
 * @author Karen Hanson
 */
@MappedSuperclass
public abstract class PassEntity {

    /**
     * Unique id for the resource.
     */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    /**
     * PassEntity constructor
     */
    protected PassEntity() {
    }

    /**
     * Copy constructor, this will copy the values of the object provided into the new object
     *
     * @param passEntity the PassEntity to copy
     */
    protected PassEntity(PassEntity passEntity) {
        if (passEntity == null) {
            throw new IllegalArgumentException("Null object provided. When creating a copy of "
                                               + "an object, the model object cannot be null");
        }
        this.id = passEntity.id;
    }

    /**
     * Retrieves the unique URI representing the resource.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique ID for an object. Note that when creating a new resource, this should be left
     * blank as the ID will be autogenerated and populated by the repository. When performing a
     * PUT, this URI will be used as the target resource.
     *
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PassEntity that = (PassEntity) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
