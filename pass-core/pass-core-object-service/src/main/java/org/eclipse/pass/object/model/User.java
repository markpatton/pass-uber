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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;
import org.eclipse.pass.object.converter.ListToStringConverter;
import org.eclipse.pass.object.converter.SetToStringConverter;
import org.eclipse.pass.object.converter.UserRoleListToStringConverter;

/**
 * User model for users logging into PASS
 *
 * @author Karen Hanson
 */

@Include
@Entity
@Table(name = "pass_user")
public class User extends PassEntity {

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
    @Convert(converter = UserRoleListToStringConverter.class)
    private List<UserRole> roles = new ArrayList<UserRole>();

    /**
     * User constructor
     */
    public User() {
    }

    /**
     * Copy constructor, this will copy the values of the object provided into the new object
     *
     * @param user the user to copy
     */
    public User(User user) {
        super(user);
        this.username = user.username;
        this.firstName = user.firstName;
        this.middleName = user.middleName;
        this.lastName = user.lastName;
        this.displayName = user.displayName;
        this.email = user.email;
        this.affiliation = new HashSet<>(user.affiliation);
        this.locatorIds = new ArrayList<String>(user.locatorIds);
        this.orcidId = user.orcidId;
        this.roles = new ArrayList<UserRole>(user.roles);
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
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
     * @return the locatorIds
     */
    public List<String> getLocatorIds() {
        return locatorIds;
    }

    /**
     * @param locatorIds List of locator IDs
     */
    public void setLocatorIds(List<String> locatorIds) {
        this.locatorIds = locatorIds;
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
    public List<UserRole> getRoles() {
        return roles;
    }

    /**
     * @param roles the roles list to set
     */
    public void setRoles(List<UserRole> roles) {
        this.roles = roles;
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

        User that = (User) o;

        if (username != null ? !username.equals(that.username) : that.username != null) {
            return false;
        }
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
        if (affiliation != null ? !affiliation.equals(that.affiliation) : that.affiliation != null) {
            return false;
        }
        if (locatorIds != null ? !locatorIds.equals(that.locatorIds) : that.locatorIds != null) {
            return false;
        }
        if (orcidId != null ? !orcidId.equals(that.orcidId) : that.orcidId != null) {
            return false;
        }
        if (roles != null ? !roles.equals(that.roles) : that.roles != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (middleName != null ? middleName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (affiliation != null ? affiliation.hashCode() : 0);
        result = 31 * result + (locatorIds != null ? locatorIds.hashCode() : 0);
        result = 31 * result + (orcidId != null ? orcidId.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        return result;
    }
}
