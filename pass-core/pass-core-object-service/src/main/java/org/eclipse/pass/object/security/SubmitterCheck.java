package org.eclipse.pass.object.security;

import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.checks.prefab.Role.RoleMemberCheck;

@SecurityCheck(SubmitterCheck.USER_IS_SUBMITTER)
public class SubmitterCheck extends RoleMemberCheck {
    public static final String USER_IS_SUBMITTER = "User is Submitter";

    public SubmitterCheck() {
        super(WebSecurityRole.SUBMITTER.getValue());
    }
}