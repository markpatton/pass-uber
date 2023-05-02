package org.eclipse.pass.object.security;

/**
 * The WebSecurityRole enum represents the different roles that a user can have in the PASS system. The roles are
 * 'ROLE_BACKEND', 'ROLE_GRANT_ADMIN', and 'ROLE_SUBMITTER'
 */
public enum WebSecurityRole {
    /**
     * 'ROLE_BACKEND' role is for users who have access to the backend of the PASS system.
     */
    BACKEND("ROLE_BACKEND"),
    /**
     * 'ROLE_GRANT_ADMIN' role is for users who have access to the grant admin of the PASS system.
     */
    GRANT_ADMIN("ROLE_GRANT_ADMIN"),
    /**
     * 'ROLE_SUBMITTER' role is for users who have access to submit in the PASS system.
     */
    SUBMITTER("ROLE_SUBMITTER");

    private final String value;

    WebSecurityRole(String value) {
        this.value = value;
    }

    /**
     * Get the string value of the WebSecurityRole. Can be either an 'ROLE_BACKEND', 'ROLE_GRANT_ADMIN', or
     * 'ROLE_SUBMITTER'
     * @return string value of the WebSecurityRole.
     */
    public String getValue() {
        return value;
    }
}