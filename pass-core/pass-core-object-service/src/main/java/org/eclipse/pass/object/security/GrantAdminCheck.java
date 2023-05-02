package org.eclipse.pass.object.security;

import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.checks.prefab.Role.RoleMemberCheck;

/**
 * Class responsible for checking if the user is a grant admin. Decorating classes with the SecurityCheck annotation
 * will ensure that the user is a grant admin before allowing access.
 */
@SecurityCheck(GrantAdminCheck.USER_IS_GRANT_ADMIN)
public class GrantAdminCheck extends RoleMemberCheck {

    /**
     * Name of the role which grants admin access.
     */
    public static final String USER_IS_GRANT_ADMIN = "User is Grant Admin";

    /**
     * Constructor for the GrantAdminCheck class.
     */
    public GrantAdminCheck() {
        super(WebSecurityRole.GRANT_ADMIN.getValue());
    }
}