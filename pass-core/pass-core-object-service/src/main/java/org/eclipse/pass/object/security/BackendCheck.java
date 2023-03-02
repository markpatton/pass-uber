package org.eclipse.pass.object.security;

import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.checks.prefab.Role.RoleMemberCheck;

@SecurityCheck(BackendCheck.USER_IS_BACKEND)
public class BackendCheck extends RoleMemberCheck {
    public static final String USER_IS_BACKEND = "User is Backend";

    public BackendCheck() {
        super(WebSecurityRole.BACKEND.getValue());
    }
}
