package org.eclipse.pass.object.serde;

import org.eclipse.pass.object.model.Repository.IntegrationType;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

@ElideTypeConverter(type = IntegrationType.class, name = "IntegrationType")
public class IntegrationTypeSerde implements Serde<String, IntegrationType> {

    @Override
    public IntegrationType deserialize(String val) {
        return IntegrationType.of(val);
    }

    @Override
    public String serialize(IntegrationType val) {
        return val.getValue();
    }
}

