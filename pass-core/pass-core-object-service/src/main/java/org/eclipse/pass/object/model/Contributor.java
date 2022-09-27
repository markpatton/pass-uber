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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;
import org.eclipse.pass.object.converter.SetToStringConverter;

/**
 * A Contributor is a person who contributed to a Publication. The contributor
 * model captures the person information as well as the roles they played in
 * creating the publication (e.g. author).
 *
 * @author Karen Hanson
 */

@Include
@Entity
@Table(name = "pass_contributor")
public class Contributor extends PassEntity {
    /**
     * First name(s) of person
     */
    private String firstName;

    /**
     * Middle name(s) of person
     */
    private String middleName;

    /**
     * Last name(s) of person
     */
    private String lastName;

    /**
     * Name for display. Separate names may not be available, but a person should
     * always at least have a display name.
     */
    private String displayName;

    /**
     * Contact email for person
     */
    private String email;

    /**
     * ORCID ID for person
     */
    private String orcidId;

    /**
     * Affiliation string for person. Where Person is embedded in Submission or
     * Grant, this is the affiliation relevant to that item
     */
    @Convert(converter = SetToStringConverter.class)
    private Set<String> affiliation = new HashSet<>();

    /**
     * One or more roles that this Contributor performed for the associated
     * Publication
     */
    @Convert(converter = RoleListToStringConverter.class)
    private List<ContributorRole> roles = new ArrayList<ContributorRole>();

    /**
     * URI of the publication that this contributor is associated with
     */
    @ManyToOne
    private Publication publication;

    /**
     * URI of the user that represents the same person as this Contributor, where
     * relevant
     */
    @ManyToOne
    private User user;

    /**
     * Contributor constructor
     */
    public Contributor() {
    }

    /**
     * Copy constructor, this will copy the values of the object provided into the new object
     *
     * @param contributor the contributor to copy
     */
    public Contributor(Contributor contributor) {
        super(contributor);
        this.firstName = contributor.firstName;
        this.middleName = contributor.middleName;
        this.lastName = contributor.lastName;
        this.displayName = contributor.displayName;
        this.email = contributor.email;
        this.orcidId = contributor.orcidId;
        this.affiliation = contributor.affiliation;
        this.roles = new ArrayList<ContributorRole>(contributor.roles);
        this.publication = contributor.publication;
        this.user = contributor.user;
    }

    /**
     * list of possible contributor Roles
     */
    public enum ContributorRole {

        /**
         * Author role
         */
        AUTHOR("author"),

        /**
         * First author role
         */
        FIRST_AUTHOR("first-author"),

        /**
         * Last author role
         */
        LAST_AUTHOR("last-author"),

        /**
         * Corresponding author role
         */
        CORRESPONDING_AUTHOR("corresponding-author");

        private static final Map<String, ContributorRole> map = new HashMap<>(values().length, 1);

        static {
            for (ContributorRole r : values()) {
                map.put(r.value, r);
            }
        }

        private String value;

        private ContributorRole(String value) {
            this.value = value;
        }

        /**
         * Parse the role.
         *
         * @param role Serialized role string
         * @return The parsed value.
         */
        public static ContributorRole of(String role) {
            ContributorRole result = map.get(role);
            if (result == null) {
                throw new IllegalArgumentException("Invalid Role: " + role);
            }
            return result;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the middleName
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * @param middleName the middleName to set
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the affiliation
     */
    public Set<String> getAffiliation() {
        return affiliation;
    }

    /**
     * @param affiliation the affiliation to set
     */
    public void setAffiliation(Set<String> affiliation) {
        this.affiliation = affiliation;
    }

    /**
     * @return the orcidId
     */
    public String getOrcidId() {
        return orcidId;
    }

    /**
     * @param orcidId the orcidId to set
     */
    public void setOrcidId(String orcidId) {
        this.orcidId = orcidId;
    }

    /**
     * @return the list of roles
     */
    public List<ContributorRole> getRoles() {
        return roles;
    }

    /**
     * @param roles the roles list to set
     */
    public void setRoles(List<ContributorRole> roles) {
        this.roles = roles;
    }

    /**
     * @return the publication
     */
    public Publication getPublication() {
        return publication;
    }

    /**
     * @param publication the publication to set
     */
    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        Contributor that = (Contributor) o;

        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) {
            return false;
        }
        if (middleName != null ? !middleName.equals(that.middleName) : that.middleName != null) {
            return false;
        }
        if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null) {
            return false;
        }
        if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) {
            return false;
        }
        if (email != null ? !email.equals(that.email) : that.email != null) {
            return false;
        }
        if (orcidId != null ? !orcidId.equals(that.orcidId) : that.orcidId != null) {
            return false;
        }
        if (affiliation != null ? !affiliation.equals(that.affiliation) : that.affiliation != null) {
            return false;
        }
        if (roles != null ? !roles.equals(that.roles) : that.roles != null) {
            return false;
        }
        if (publication != null ? !publication.equals(that.publication) : that.publication != null) {
            return false;
        }
        if (user != null ? !user.equals(that.user) : that.user != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (middleName != null ? middleName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (orcidId != null ? orcidId.hashCode() : 0);
        result = 31 * result + (affiliation != null ? affiliation.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        result = 31 * result + (publication != null ? publication.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }

    public static class RoleListToStringConverter implements AttributeConverter<List<ContributorRole>, String> {
        @Override
        public String convertToDatabaseColumn(List<ContributorRole> attribute) {
            return attribute == null ? null
                    : String.join(",", attribute.stream().map(ContributorRole::getValue).collect(Collectors.toList()));
        }

        @Override
        public List<ContributorRole> convertToEntityAttribute(String dbData) {
            return dbData == null ? Collections.emptyList() :
                Stream.of(dbData.split(",")).map(ContributorRole::of).collect(Collectors.toList());
        }
    }
}
