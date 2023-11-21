/**
    Elide permission annotations for the PASS object service.
 */
@CreatePermission(expression = "User is Backend")
@ReadPermission(expression = "Prefab.Role.All")
@UpdatePermission(expression = "User is Backend")
@DeletePermission(expression = "User is Backend")
package org.eclipse.pass.object.model;

import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
