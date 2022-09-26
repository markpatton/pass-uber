package org.eclipse.pass.object.serde;

import org.eclipse.pass.object.model.RepositoryCopy.CopyStatus;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

@ElideTypeConverter(type = CopyStatus.class, name = "CopyStatus")
public class CopyStatusSerde implements Serde<String, CopyStatus> {

    @Override
    public CopyStatus deserialize(String val) {
        return CopyStatus.of(val);
    }

    @Override
    public String serialize(CopyStatus val) {
        return val.getValue();
    }
}

