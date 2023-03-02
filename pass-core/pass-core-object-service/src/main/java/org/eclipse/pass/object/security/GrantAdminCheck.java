package org.eclipse.pass.object.security;

import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.checks.prefab.Role.RoleMemberCheck;

@SecurityCheck(GrantAdminCheck.USER_IS_GRANT_ADMIN)
public class GrantAdminCheck extends RoleMemberCheck {
    public static final String USER_IS_GRANT_ADMIN = "User is Grant Admin";

    public GrantAdminCheck() {
        super(WebSecurityRole.GRANT_ADMIN.getValue());
    }
}