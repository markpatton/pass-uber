package org.eclipse.pass.object.serde;

import org.eclipse.pass.object.model.User.UserRole;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

@ElideTypeConverter(type = UserRole.class, name = "UserRole")
public class UserRoleSerde implements Serde<String, UserRole> {

    @Override
    public UserRole deserialize(String val) {
        return UserRole.of(val);
    }

    @Override
    public String serialize(UserRole val) {
        return val.getValue();
    }
}

