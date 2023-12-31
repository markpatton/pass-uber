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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

/**
 * Serializer/Deserializer for ZonedDateTime. Serializes a ZonedDateTime to a String with the pattern
 * yyyy-MM-dd'T'HH:mm:ss.SSSX and deserializes a String to a ZonedDateTime.
 */
@ElideTypeConverter(type = ZonedDateTime.class, name = "ZonedDateTime")
public class ZonedDateTimeSerde implements Serde<String, ZonedDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    @Override
    public ZonedDateTime deserialize(String val) {
        return ZonedDateTime.parse(val, formatter);
    }

    @Override
    public String serialize(ZonedDateTime val) {
        return val.format(formatter);
    }
}