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
package org.eclipse.pass.support.grant.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.pass.support.client.model.Funder;
import org.eclipse.pass.support.client.model.Grant;
import org.eclipse.pass.support.client.model.User;

/**
 * A utility class for handling Grants, Users or Funders. One function performed is comparison of two instances of
 * these PASS entity classes. These comparisons are reduced to only those fields which are updatable by
 * data from the source pull. Stored objects are evaluated and updated according to the PassEntityUtil implementation
 * supplied to this updater
 * <p>
 * This basic implementation deals with a smallish set of fields - other implementations will have to address more
 * fields if they are to be stored on PASS objects.
 *
 * @author jrm@jhu.edu
 */

public class BasicPassEntityUtil implements PassEntityUtil {

    /**
     * This method takes a Funder, calculates whether it needs to be updated, and if so, returns the updated object
     * to be be ingested into the repository. if not, returns null.
     *
     * @param stored the Funder as it is stored in the PASS backend
     * @param system the version of the Funder from the Harvard pull
     * @return the updated Funder - null if the Funder does not need to be updated
     */
    public Funder update(Funder system, Funder stored) {
        if (funderNeedsUpdate(system, stored)) {
            return updateFunder(system, stored);
        }
        return null;
    }

    /**
     * This method takes a User, calculates whether it needs to be updated, and if so, returns the updated object
     * to be be ingested into the repository. if not, returns null.
     *
     * @param stored the User as it is stored in the PASS backend
     * @param system the version of the user from the pull
     * @return the updated User - null if the User does not need to be updated
     */
    public User update(User system, User stored) {
        if (userNeedsUpdate(system, stored)) {
            return updateUser(system, stored);
        }
        return null;
    }

    /**
     * This method takes a Harvard Grant, calculates whether it needs to be updated, and if so, returns the updated
     * object
     * to be be ingested into the repository. if not, returns null.
     *
     * @param stored the Grant as it is stored in the PASS backend
     * @param system the version of the Grant from the pull
     * @return the updated object - null if the Grant does not need to be updated
     */
    public Grant update(Grant system, Grant stored) {
        if (grantNeedsUpdate(system, stored)) {
            return updateGrant(system, stored);
        }
        return null;
    }

    /**
     * Compare two Funder objects
     *
     * @param system the version of the Funder as seen in the system pull
     * @param stored the version of the Funder as read from Pass
     * @return a boolean which asserts whether the pulled object contains information which can
     * update the stored object
     */
    private boolean funderNeedsUpdate(Funder system, Funder stored) {

        //this adjustment handles the case where we take data from policy.properties file, which has no name info
        if (system.getName() != null && !system.getName().equals(stored.getName())) {
            return true;
        }
        if (system.getLocalKey() != null ? !system.getLocalKey()
                                                  .equals(stored.getLocalKey()) : stored.getLocalKey() != null) {
            return true;
        }
        if (system.getPolicy() != null ? !system.getPolicy().equals(stored.getPolicy()) : stored.getPolicy() != null) {
            return true;
        }
        return false;
    }

    /**
     * Update a Pass Funder object with new information from a system pull
     *
     * @param system the version of the Funder as seen in the  system pull
     * @param stored the version of the Funder as read from Pass
     * @return the Funder object which represents the Pass object, with any new information from the pull merged in
     */
    private Funder updateFunder(Funder system, Funder stored) {
        stored.setLocalKey(system.getLocalKey());
        stored.setName(system.getName());
        stored.setPolicy(system.getPolicy());
        return stored;
    }

    /**
     * Compare two User objects. We only care about those fields for which  is the authoritative source
     * After recent changes.
     *
     * @param system the version of the User as seen in the Harvard system pull
     * @param stored the version of the User as read from Pass
     * @return a boolean which asserts whether the stored object needs updating
     */
    private boolean userNeedsUpdate(User system, User stored) {
        if (system.getFirstName() != null ? !system.getFirstName()
                                                   .equals(stored.getFirstName()) : stored.getFirstName() != null) {
            return true;
        }
        if (system.getMiddleName() != null ? !system.getMiddleName()
                                                    .equals(stored.getMiddleName()) : stored.getMiddleName() != null) {
            return true;
        }
        if (system.getLastName() != null ? !system.getLastName()
                                                  .equals(stored.getLastName()) : stored.getLastName() != null) {
            return true;
        }
        if (system.getLocatorIds() != null ? !stored.getLocatorIds().containsAll(
            system.getLocatorIds()) : stored.getLocatorIds() != null) {
            return true;
        }
        if (system.getEmail() != null && stored.getEmail() == null) {
            return true;
        }
        if (system.getDisplayName() != null && stored.getDisplayName() == null) {
            return true;
        }
        return false;
    }

    /**
     * Update a Pass User object with new information from the system. We check only those fields for which Harvard is
     * authoritative. Other fields will be managed by other providers (Shibboleth for example). The exceptions are
     * the localKey, which this application and Shibboleth both rely on; and  email, which this application only
     * populates
     * if Shib hasn't done so already.
     *
     * @param system the version of the User as seen in the system pull
     * @param stored the version of the User as read from Pass
     * @return the User object which represents the Pass object, with any new information merged in
     */
    private User updateUser(User system, User stored) {
        stored.setFirstName(system.getFirstName());
        stored.setMiddleName(system.getMiddleName());
        stored.setLastName(system.getLastName());
        stored.setEmail(system.getEmail());
        Set<String> idSet = new HashSet<>();
        idSet.addAll(stored.getLocatorIds());
        idSet.addAll(system.getLocatorIds());
        stored.setLocatorIds(new ArrayList<>(idSet));
        stored.setDisplayName(system.getDisplayName());
        return stored;
    }

    /**
     * Compare two Grant objects. Note that the Lists of Co-Pis are compared as Sets
     *
     * @param system the version of the Grant as seen in the system pull
     * @param stored the version of the Grant as read from Pass
     * @return a boolean which asserts whether the stored ubject needs updating
     */
    private boolean grantNeedsUpdate(Grant system, Grant stored) {
        if (system.getAwardNumber() != null ? !system.getAwardNumber()
                                                     .equals(
                                                         stored.getAwardNumber()) : stored.getAwardNumber() != null) {
            return true;
        }
        if (system.getLocalKey() != null ? !system.getLocalKey()
                                                  .equals(stored.getLocalKey()) : stored.getLocalKey() != null) {
            return true;
        }
        if (system.getProjectName() != null ? !system.getProjectName()
                                                     .equals(
                                                         stored.getProjectName()) : stored.getProjectName() != null) {
            return true;
        }
        if (system.getPrimaryFunder() != null ? !system.getPrimaryFunder().equals(
            stored.getPrimaryFunder()) : stored.getPrimaryFunder() != null) {
            return true;
        }
        if (system.getDirectFunder() != null ? !system.getDirectFunder().equals(
            stored.getDirectFunder()) : stored.getDirectFunder() != null) {
            return true;
        }
        if (system.getPi() != null ? !system.getPi().equals(stored.getPi()) : stored.getPi() != null) {
            return true;
        }
        if (system.getCoPis() != null ? !new HashSet(system.getCoPis()).equals(
            new HashSet(stored.getCoPis())) : stored.getCoPis() != null) {
            return true;
        }
        if (system.getStartDate() != null ? !system.getStartDate()
                                                   .equals(stored.getStartDate()) : stored.getStartDate() != null) {
            return true;
        }
        if (system.getEndDate() != null ? !system.getEndDate()
                                                 .equals(stored.getEndDate()) : stored.getEndDate() != null) {
            return true;
        }
        return false;
    }

    /**
     * Update a Pass Grant object with new information from Harvard
     *
     * @param system the version of the Grant as seen in the system pull
     * @param stored the version of the Grant as read from Pass
     * @return the Grant object which represents the Pass object, with any new information  merged in
     */
    private Grant updateGrant(Grant system, Grant stored) {
        stored.setAwardNumber(system.getAwardNumber());
        stored.setLocalKey(system.getLocalKey());
        stored.setProjectName(system.getProjectName());
        stored.setPrimaryFunder(system.getPrimaryFunder());
        stored.setDirectFunder(system.getDirectFunder());
        stored.setPi(system.getPi());
        stored.setCoPis(system.getCoPis());
        stored.setStartDate(system.getStartDate());
        stored.setEndDate(system.getEndDate());
        return stored;
    }

}
