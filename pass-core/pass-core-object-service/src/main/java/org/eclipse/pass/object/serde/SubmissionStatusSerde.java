package org.eclipse.pass.object.serde;

import org.eclipse.pass.object.model.Submission.SubmissionStatus;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

@ElideTypeConverter(type = SubmissionStatus.class, name = "SubmissionStatus")
public class SubmissionStatusSerde implements Serde<String, SubmissionStatus> {

    @Override
    public SubmissionStatus deserialize(String val) {
        return SubmissionStatus.of(val);
    }

    @Override
    public String serialize(SubmissionStatus val) {
        return val.getValue();
    }
}

