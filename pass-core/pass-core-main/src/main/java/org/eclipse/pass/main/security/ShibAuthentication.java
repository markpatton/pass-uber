/*
 * Copyright 2023 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.pass.main.security;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.pass.object.model.User;
import org.eclipse.pass.object.model.UserRole;
import org.eclipse.pass.object.security.WebSecurityRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * A ShibAuthentication wraps information from a PASS user.
 * The PASS user roles are mapped to authorities.
 * The PASS user username becomes the name and can be used to lookup the user object.
 */
public class ShibAuthentication implements Authentication {
    private static final long serialVersionUID = 1L;

    /**
     * The unique username used by the PASS user.
     */
    private final String user_name;

    /**
     * Collection of GrantedAuthority objects of the user roles.
     */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Map a PASS user to Spring Security authentication token.
     *
     * @param user PASS user
     */
    public ShibAuthentication(User user) {
        this.user_name = user.getUsername();
        this.authorities = user.getRoles().stream().map(ShibAuthentication::as_authority).
                filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static GrantedAuthority as_authority(UserRole role) {
        if (role == UserRole.SUBMITTER) {
            return new SimpleGrantedAuthority(WebSecurityRole.SUBMITTER.getValue());
        }

        if (role == UserRole.ADMIN) {
            return new SimpleGrantedAuthority(WebSecurityRole.GRANT_ADMIN.getValue());
        }

        return null;
    }

    @Override
    public String getName() {
        return user_name;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return user_name;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return user_name;
    }
}
