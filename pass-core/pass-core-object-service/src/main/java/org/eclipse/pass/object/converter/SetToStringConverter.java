package org.eclipse.pass.object.converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeConverter;


public class SetToStringConverter implements AttributeConverter<Set<String>, String> {
    @Override
    public String convertToDatabaseColumn(Set<String> attribute) {
        return attribute == null || attribute.isEmpty() ? null : String.join(",", attribute);
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isEmpty() ? Collections.emptySet() : new HashSet<String>(Arrays.asList(dbData.split(",")));
    }
}
