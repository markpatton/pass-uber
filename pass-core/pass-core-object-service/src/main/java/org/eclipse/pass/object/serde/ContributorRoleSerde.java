package org.eclipse.pass.object.serde;

import org.eclipse.pass.object.model.Contributor.ContributorRole;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

@ElideTypeConverter(type = ContributorRole.class, name = "ContributorRole")
public class ContributorRoleSerde implements Serde<String, ContributorRole> {

  @Override
    public ContributorRole deserialize(String val) {
        return ContributorRole.of(val);
    }

    @Override
    public String serialize(ContributorRole val) {
        return val.getValue();
    }
  
}
