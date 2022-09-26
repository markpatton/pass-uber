package org.eclipse.pass.object.serde;

import org.eclipse.pass.object.model.SubmissionEvent.EventType;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

@ElideTypeConverter(type = EventType.class, name = "EventType")
public class SubmissionEventTypeSerde implements Serde<String, EventType> {

    @Override
    public EventType deserialize(String val) {
        return EventType.of(val);
    }

    @Override
    public String serialize(EventType val) {
        return val.getValue();
    }
}

