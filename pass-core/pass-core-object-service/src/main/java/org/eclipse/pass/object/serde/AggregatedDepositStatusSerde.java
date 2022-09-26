package org.eclipse.pass.object.serde;

import org.eclipse.pass.object.model.Submission.AggregatedDepositStatus;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

@ElideTypeConverter(type = AggregatedDepositStatus.class, name = "AggregatedDepositStatus")
public class AggregatedDepositStatusSerde implements Serde<String, AggregatedDepositStatus> {

    @Override
    public AggregatedDepositStatus deserialize(String val) {
        return AggregatedDepositStatus.of(val);
    }

    @Override
    public String serialize(AggregatedDepositStatus val) {
        return val.getValue();
    }
}

