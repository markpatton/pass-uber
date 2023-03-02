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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Configure Spring Security filter chain.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Autowired
    private ShibAuthenticationFilter shibAuthFilter;

    /**
     * Return a configured Spring Security filter chain
     *
     * @param http HttpSecurity
     * @return filter chain
     * @throws Exception on error
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.formLogin().disable();
        http.logout().disable();
        http.anonymous().disable();
        http.exceptionHandling().disable();
        http.headers().disable();
        http.requestCache().disable();

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests().anyRequest().authenticated();
        http.httpBasic();
        http.addFilterBefore(shibAuthFilter, BasicAuthenticationFilter.class);

        return http.build();
    }
}
