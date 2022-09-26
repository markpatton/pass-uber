package org.eclipse.pass.object.serde;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

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
