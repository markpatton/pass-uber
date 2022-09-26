package org.eclipse.pass.object.serde;

import org.eclipse.pass.object.model.Deposit.DepositStatus;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

@ElideTypeConverter(type = DepositStatus.class, name = "DepositStatus")
public class DepositStatusSerde implements Serde<String, DepositStatus> {

    @Override
    public DepositStatus deserialize(String val) {
        return DepositStatus.of(val);
    }

    @Override
    public String serialize(DepositStatus val) {
        return val.getValue();
    }
}

