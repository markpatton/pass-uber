package org.eclipse.pass.object.security;

public enum WebSecurityRole {
    BACKEND("ROLE_BACKEND"), GRANT_ADMIN("ROLE_GRANT_ADMIN"), SUBMITTER("ROLE_SUBMITTER");

    private final String value;

    WebSecurityRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}