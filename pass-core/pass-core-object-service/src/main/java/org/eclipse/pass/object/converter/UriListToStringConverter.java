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
package org.eclipse.pass.object.converter;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.AttributeConverter;

public class UriListToStringConverter implements AttributeConverter<List<URI>, String> {
    @Override
    public String convertToDatabaseColumn(List<URI> attribute) {
        return attribute == null ? null
                : String.join(",", attribute.stream().map(URI::toString).collect(Collectors.toList()));
    }

    @Override
    public List<URI> convertToEntityAttribute(String dbData) {
        return dbData == null ? Collections.emptyList() :
            Stream.of(dbData.split(",")).map(URI::create).collect(Collectors.toList());
    }
}
