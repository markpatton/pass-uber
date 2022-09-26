package org.eclipse.pass.object.serde;

import org.eclipse.pass.object.model.File.FileRole;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;

@ElideTypeConverter(type = FileRole.class, name = "FileRole")
public class FileRoleSerde implements Serde<String, FileRole> {

    @Override
    public FileRole deserialize(String val) {
        return FileRole.of(val);
    }

    @Override
    public String serialize(FileRole val) {
        return val.getValue();
    }
}

