package org.eclipse.pass.object.security;

import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.checks.prefab.Role.RoleMemberCheck;

/**
 * BackendCheck class is responsible for checking if user is a Backend user
 */
@SecurityCheck(BackendCheck.USER_IS_BACKEND)
public class BackendCheck extends RoleMemberCheck {
    /**
     * Constant for User is Backend
     */
    public static final String USER_IS_BACKEND = "User is Backend";

    /**
     * Constructor for BackendCheck
     */
    public BackendCheck() {
        super(WebSecurityRole.BACKEND.getValue());
    }
}
