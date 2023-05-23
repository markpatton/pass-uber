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

package org.dataconservancy.pass.notification.model.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * @author apb@jhu.edu
 */
public class LinkValidationRule {

    String requiredBaseURI;

    boolean throwExceptionOnFailure;

    Collection<String> rels = new HashSet<>();

    public String getRequiredBaseURI() {
        return requiredBaseURI;
    }

    public void setRequiredBaseURI(String requiredBaseUri) {
        this.requiredBaseURI = requiredBaseUri;
    }

    public boolean getThrowExceptionWhenInvalid() {
        return throwExceptionOnFailure;
    }

    public void setThrowExceptionWhenInvalid(boolean throwException) {
        this.throwExceptionOnFailure = throwException;
    }

    public Collection<String> getRels() {
        return rels;
    }

    public void setRels(Collection<String> rels) {
        this.rels = rels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LinkValidationRule that = (LinkValidationRule) o;
        return Objects.equals(this.requiredBaseURI, that.requiredBaseURI) &&
                Objects.equals(this.rels, that.rels) &&
                Objects.equals(this.throwExceptionOnFailure, that.throwExceptionOnFailure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requiredBaseURI, rels, throwExceptionOnFailure);
    }

}
