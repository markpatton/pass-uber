package org.eclipse.pass.object.security;

import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.checks.prefab.Role.RoleMemberCheck;

/**
 * SecurityCheck class to check if the user is a Submitter.
 */
@SecurityCheck(SubmitterCheck.USER_IS_SUBMITTER)
public class SubmitterCheck extends RoleMemberCheck {
    /**
     * Name of the role which grants access to a user being a submitter.
     */
    public static final String USER_IS_SUBMITTER = "User is Submitter";

    /**
     * Constructor for SubmitterCheck
     */
    public SubmitterCheck() {
        super(WebSecurityRole.SUBMITTER.getValue());
    }
}