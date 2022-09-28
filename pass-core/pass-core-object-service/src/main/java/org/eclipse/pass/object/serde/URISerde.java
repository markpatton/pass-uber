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
package org.eclipse.pass.object.serde;

import java.net.URI;
import java.net.URISyntaxException;

import com.yahoo.elide.core.exceptions.InvalidValueException;
import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

@ElideTypeConverter(type = URI.class, name = "URI")
public class URISerde implements Serde<String, URI> {

    @Override
    public URI deserialize(String val) {
        try {
            return new URI(val);
        } catch (URISyntaxException e) {
            throw new InvalidValueException("Invalid URI " + val);
        }
    }

    @Override
    public String serialize(URI val) {
        return val.toString();
    }
}
