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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.AttributeConverter;

import org.eclipse.pass.object.model.UserRole;

public class UserRoleListToStringConverter implements AttributeConverter<List<UserRole>, String> {
    @Override
    public String convertToDatabaseColumn(List<UserRole> attribute) {
        return attribute == null || attribute.isEmpty() ? null
                : String.join(",", attribute.stream().map(UserRole::getValue).collect(Collectors.toList()));
    }

    @Override
    public List<UserRole> convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isEmpty() ?
                Collections.emptyList() :
                    Stream.of(dbData.split(",")).map(UserRole::of).collect(Collectors.toList());
    }
}