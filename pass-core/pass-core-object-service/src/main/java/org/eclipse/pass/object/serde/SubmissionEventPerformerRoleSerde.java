package org.eclipse.pass.object.serde;

import org.eclipse.pass.object.model.SubmissionEvent.PerformerRole;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

@ElideTypeConverter(type = PerformerRole.class, name = "PerformerRole")
public class SubmissionEventPerformerRoleSerde implements Serde<String, PerformerRole> {

    @Override
    public PerformerRole deserialize(String val) {
        return PerformerRole.of(val);
    }

    @Override
    public String serialize(PerformerRole val) {
        return val.getValue();
    }
}

