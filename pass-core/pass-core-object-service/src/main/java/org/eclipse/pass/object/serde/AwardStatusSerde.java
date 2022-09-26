package org.eclipse.pass.object.serde;

import org.eclipse.pass.object.model.Grant.AwardStatus;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

@ElideTypeConverter(type = AwardStatus.class, name = "AwardStatus")
public class AwardStatusSerde implements Serde<String, AwardStatus> {

    @Override
    public AwardStatus deserialize(String val) {
        return AwardStatus.of(val);
    }

    @Override
    public String serialize(AwardStatus val) {
        return val.getValue();
    }
}

