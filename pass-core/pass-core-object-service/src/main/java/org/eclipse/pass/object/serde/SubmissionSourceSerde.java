package org.eclipse.pass.object.serde;

import org.eclipse.pass.object.model.Submission.Source;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

@ElideTypeConverter(type = Source.class, name = "SubmissionSource")
public class SubmissionSourceSerde implements Serde<String, Source> {

    @Override
    public Source deserialize(String val) {
        return Source.of(val);
    }

    @Override
    public String serialize(Source val) {
        return val.getValue();
    }
}

