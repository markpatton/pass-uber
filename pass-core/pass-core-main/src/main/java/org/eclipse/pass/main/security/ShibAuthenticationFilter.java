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

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yahoo.elide.RefreshableElide;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.PassClientResult;
import org.eclipse.pass.object.PassClientSelector;
import org.eclipse.pass.object.RSQL;
import org.eclipse.pass.object.model.User;
import org.eclipse.pass.object.model.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter responsible for mapping a Shib user to a PASS user. The PASS user is
 * created if it does not exist and otherwise updated to reflect the information
 * provided by Shib. The PASS user name becomes the name of the Principal.
 * <p>
 * If the request does not look like it came from Shib, the mapping step is skipped.
 * In any case, the request is passed down the chain.
 * <p>
 * A cache of maximum size pass.auth.max-cache-size of recent authentications is
 * maintained. It is cleared every pass.auth.cache-duration minutes.
 */
@Component
public class ShibAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(ShibAuthenticationFilter.class);

    private final ConcurrentHashMap<String, ShibAuthentication> auth_cache;
    private final RefreshableElide elide;

    @Value("${pass.auth.max-cache-size}")
    private int max_cache_size;

    /**
     * @param refreshableElide RefreshableElide
     */
    public ShibAuthenticationFilter(RefreshableElide refreshableElide) {
        this.auth_cache = new ConcurrentHashMap<>();
        this.elide = refreshableElide;
    }

    @Scheduled(fixedRateString = "${pass.auth.cache-duration}", timeUnit = TimeUnit.MINUTES)
    private void clear_cache() {
        auth_cache.clear();
    }

    // Do authentication and return Authentication object representing success.
    // Throw AuthenticationException if there is trouble with the user credentials
    private Authentication authenticate(HttpServletRequest request)
            throws AuthenticationException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Request headers:");
            request.getHeaderNames().asIterator().forEachRemaining(s -> {
                LOG.debug(s + ": " + request.getHeader(s));
            });
        }

        User shib_user = parseShibHeaders(request);
        ShibAuthentication auth = auth_cache.get(shib_user.getUsername());

        if (auth != null) {
            return auth;
        }

        create_or_update_pass_user(shib_user);

        auth = new ShibAuthentication(shib_user);

        if (auth_cache.size() > max_cache_size) {
            auth_cache.clear();
        }

        auth_cache.put(shib_user.getUsername(), auth);

        return auth;
    }

    // Ensure that only one user is created
    private synchronized void create_or_update_pass_user(User shib_user) throws IOException {
        try (PassClient pass_client = PassClient.newInstance(elide)) {
            User pass_user = find_pass_user(pass_client, shib_user);

            if (pass_user == null) {
                pass_client.createObject(shib_user);

                LOG.info("Created user: {}", shib_user.getUsername());
            } else {
                update_pass_user(pass_client, shib_user, pass_user);
            }
        }
    }

    private void update_pass_user(PassClient pass_client, User shib_user, User pass_user) throws IOException {
        boolean update = false;

        if (!pass_user.getUsername().equals(shib_user.getUsername())) {
            pass_user.setUsername(shib_user.getUsername());
            update = true;
        }

        if (!pass_user.getEmail().equals(shib_user.getEmail())) {
            pass_user.setEmail(shib_user.getEmail());
            update = true;
        }

        if (!pass_user.getDisplayName().equals(shib_user.getDisplayName())) {
            pass_user.setDisplayName(shib_user.getDisplayName());
            update = true;
        }

        if (!pass_user.getFirstName().equals(shib_user.getFirstName())) {
            pass_user.setFirstName(shib_user.getFirstName());
            update = true;
        }

        if (!pass_user.getLastName().equals(shib_user.getLastName())) {
            pass_user.setLastName(shib_user.getLastName());
            update = true;
        }

        if (!pass_user.getLocatorIds().equals(shib_user.getLocatorIds())) {
            pass_user.setLocatorIds(shib_user.getLocatorIds());
            update = true;
        }

        if (!pass_user.getAffiliation().equals(shib_user.getAffiliation())) {
            pass_user.setAffiliation(shib_user.getAffiliation());
            update = true;
        }

        if (update) {
            pass_client.updateObject(pass_user);
            LOG.info("Updated user: {}", shib_user.getUsername());
        }
    }

    private User find_pass_user(PassClient pass_client, User user) throws IOException {
        PassClientSelector<User> selector = new PassClientSelector<>(User.class);

        for (String locator_id : user.getLocatorIds()) {
            selector.setFilter(RSQL.hasMember("locatorIds", locator_id));
            PassClientResult<User> result = pass_client.selectObjects(selector);

            if (result.getTotal() == 1) {
                return result.getObjects().get(0);
            } else if (result.getTotal() > 1) {
                throw new BadCredentialsException("Found multiple users matching locator: " + locator_id);
            }
        }

        return null;
    }

    /**
     * @param request HttpServletRequest
     * @return User representing the information in the request.
     */
    protected static User parseShibHeaders(HttpServletRequest request) {
        User user = new User();

        String display_name = get_shib_attr(request, ShibConstants.DISPLAY_NAME_HEADER, true);
        String given_name = get_shib_attr(request, ShibConstants.GIVENNAME_HEADER, true);
        String surname = get_shib_attr(request, ShibConstants.SN_HEADER, true);
        String email = get_shib_attr(request, ShibConstants.EMAIL_HEADER, true);
        String eppn = get_shib_attr(request, ShibConstants.EPPN_HEADER, true);
        String employee_id = get_shib_attr(request, ShibConstants.EMPLOYEE_ID_HEADER, false);
        String unique_id = get_shib_attr(request, ShibConstants.UNIQUE_ID_HEADER, true);
        String affiliation = get_shib_attr(request, ShibConstants.SCOPED_AFFILIATION_HEADER, false);

        String[] eppn_parts = eppn.split("@");

        if (eppn_parts.length != 2) {
            throw new BadCredentialsException("Shib header malformed: " + ShibConstants.EPPN_HEADER);
        }

        String domain = eppn_parts[1];
        String institutional_id = eppn_parts[0].toLowerCase();

        if (domain.isEmpty() || institutional_id.isEmpty()) {
            throw new BadCredentialsException("Shib header malformed: " + ShibConstants.EPPN_HEADER);
        }

        unique_id = String.join(":", domain, ShibConstants.UNIQUE_ID_TYPE, unique_id.split("@")[0]);

        // The locator id list has durable ids first.
        user.getLocatorIds().add(unique_id);

        institutional_id = String.join(":", domain, ShibConstants.JHED_ID_TYPE, institutional_id);
        user.getLocatorIds().add(institutional_id);

        if (employee_id != null && !employee_id.isEmpty()) {
            employee_id = String.join(":", domain, ShibConstants.EMPLOYEE_ID_TYPE, employee_id);
            user.getLocatorIds().add(employee_id);
        }

        user.getAffiliation().add(domain);

        if (affiliation != null) {
            for (String s : affiliation.split(";")) {
                user.getAffiliation().add(s);
            }
        }

        user.setDisplayName(display_name);
        user.setEmail(email);
        user.setFirstName(given_name);
        user.setLastName(surname);
        user.setUsername(eppn);
        user.getRoles().add(UserRole.SUBMITTER);

        return user;
    }

    /**
     * @param request HttpServletRequest
     * @return whether or not this looks like a Shib request
     */
    protected static boolean isShibRequest(HttpServletRequest request) {
        return has_all_headers(request, ShibConstants.EPPN_HEADER, ShibConstants.UNIQUE_ID_HEADER);
    }

    private static boolean has_all_headers(HttpServletRequest request, String... names) {
        for (String s : names) {
            if (request.getHeader(s) == null) {
                return false;
            }
        }

        return true;
    }

    private static String get_shib_attr(HttpServletRequest request, String name, boolean required)
            throws AuthenticationException {
        String value = request.getHeader(name);

        if (value != null) {
            value = value.trim();
        }

        if (value == null || value.isEmpty()) {
            value = null;

            if (required) {
                throw new BadCredentialsException("Missing shib attribute: " + name);
            }
        }

        return value;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // The filter seems to always get triggered twice.
        // If we are already authenticated, continue on.
        Authentication existing_auth = SecurityContextHolder.getContext().getAuthentication();

        if (existing_auth != null && existing_auth.isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }

        if (isShibRequest(request)) {
            try {
                Authentication auth = authenticate(request);
                SecurityContextHolder.getContext().setAuthentication(auth);

                LOG.debug("Shib user logged in {}", auth.getName());
            } catch (AuthenticationException e) {
                // This should not happen
                LOG.error("Login failed", e);

                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
