/*
 * Copyright 2018 Johns Hopkins University
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
package org.eclipse.pass.object.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.eclipse.pass.object.converter.ListToStringConverter;
import org.eclipse.pass.object.converter.SetToStringConverter;

import com.yahoo.elide.annotation.Include;

/**
 * User model for users logging into PASS
 *
 * @author Karen Hanson
 */

@Include
@Entity
@Table(name = "pass_user")
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    /**
     * Unique login name used by user
     */
    private String username;

    /**
     * First name(s) of User
     */
    private String firstName;

    /**
     * Middle name(s) of User
     */
    private String middleName;

    /**
     * Last name(s) of User
     */
    private String lastName;

    /**
     * Name for display. Separate names may not be available, but a person should always at least
     * have a display name.
     */
    private String displayName;

    /**
     * Contact email for User
     */
    private String email;

    /**
     * Affiliation string for person. Where Person is embedded in Submission or Grant,
     * this is the affiliation relevant to that item
     */
    @Convert(converter = SetToStringConverter.class)
    private Set<String> affiliation = new HashSet<>();

    /**
     * A list of ids associated with the user by various system that PASS interacts with.
     * The value of each entry would be in the form of : {@code domain:type:value}.
     * For example, @{code ["johnshopkins.edu:hopkinsid:DRA2D", "johnshopkins.edu:employeeid:12345",
     * "johnshopkins.edu:jhed:bostaur1"]}
     */
    @Convert(converter = ListToStringConverter.class)
    private List<String> locatorIds = new ArrayList<String>();

    /**
     * ORCID ID for User
     */
    private String orcidId;

    /**
     * User's system roles in PASS
     */
    @Convert(converter = RoleListToStringConverter.class)
    private List<UserRole> roles = new ArrayList<UserRole>();

    /**
     * list of possible user Roles
     */
    public enum UserRole {

        /**
         * Grant admin role
         */
        ADMIN("admin"),

        /**
         * Submitter role
         */
        SUBMITTER("submitter");

        private static final Map<String, UserRole> map = new HashMap<>(values().length, 1);

        static {
            for (UserRole r : values()) {
                map.put(r.value, r);
            }
        }

        private String value;

        private UserRole(String value) {
            this.value = value;
        }

        /**
         * Parse the role.
         *
         * @param role Serialized role
         * @return parsed role.
         */
        public static UserRole of(String role) {
            UserRole result = map.get(role);
            if (result == null) {
                throw new IllegalArgumentException("Invalid Role: " + role);
            }
            return result;
        }

        public String getValue() {
            return value;
        }
    }

    public static class RoleListToStringConverter implements AttributeConverter<List<UserRole>, String> {
        @Override
        public String convertToDatabaseColumn(List<UserRole> attribute) {
            return attribute == null || attribute.isEmpty() ? null
                    : String.join(",", attribute.stream().map(UserRole::getValue).collect(Collectors.toList()));
        }

        @Override
        public List<UserRole> convertToEntityAttribute(String dbData) {
            return dbData == null || dbData.isEmpty() ? Collections.emptyList() : Stream.of(dbData.split(",")).map(UserRole::of).collect(Collectors.toList());
        }
    }
}
