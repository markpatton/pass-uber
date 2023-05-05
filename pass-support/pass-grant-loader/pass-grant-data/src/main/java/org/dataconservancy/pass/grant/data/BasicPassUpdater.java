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

package org.dataconservancy.pass.grant.data;

import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_ABBREVIATED_ROLE;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_DIRECT_FUNDER_LOCAL_KEY;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_DIRECT_FUNDER_NAME;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_DIRECT_FUNDER_POLICY;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_GRANT_AWARD_DATE;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_GRANT_AWARD_NUMBER;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_GRANT_AWARD_STATUS;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_GRANT_END_DATE;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_GRANT_LOCAL_KEY;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_GRANT_PROJECT_NAME;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_GRANT_START_DATE;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_PRIMARY_FUNDER_LOCAL_KEY;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_PRIMARY_FUNDER_NAME;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_PRIMARY_FUNDER_POLICY;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_UPDATE_TIMESTAMP;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_USER_EMAIL;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_USER_EMPLOYEE_ID;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_USER_FIRST_NAME;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_USER_LAST_NAME;
import static org.dataconservancy.pass.grant.data.CoeusFieldNames.C_USER_MIDDLE_NAME;
import static org.dataconservancy.pass.grant.data.DateTimeUtil.createJodaDateTime;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.pass.support.client.PassClient;
import org.eclipse.pass.support.client.model.Funder;
import org.eclipse.pass.support.client.model.Grant;
import org.eclipse.pass.support.client.model.User;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for taking the Set of Maps derived from the ResultSet from the database query and
 * constructing a corresponding Collection of Grant or User objects, which it then sends to PASS to update.
 *
 * This represents the state of things for the default updater of version 1.3
 *
 * @author jrm@jhu.edu
 */

public class BasicPassUpdater implements PassUpdater {

    private String DOMAIN = "default.domain";

    private static final Logger LOG = LoggerFactory.getLogger(BasicPassUpdater.class);
    private String latestUpdateString = "";

    private final PassClient passClient;
    private final PassUpdateStatistics statistics = new PassUpdateStatistics();
    private final PassEntityUtil passEntityUtil;

    //used in test classes
    private final Map<URI, Grant> grantUriMap = new HashMap<>();

    //used in unit test
    //some entities may be referenced many times during an update, but just need to be updated the first time
    //they are encountered. these include Users and Funders. we save the overhead of redundant updates
    //of these by looking them up here; if they are on the Map, they have already been processed
    //
    private final Map<String, URI> funderMap = new HashMap<>();
    private final Map<String, URI> userMap = new HashMap<>();

    private String mode;

    public BasicPassUpdater(PassEntityUtil passEntityUtil) {
        this.passEntityUtil = passEntityUtil;
        this.passClient = PassClient.newInstance();
    }

    //used in unit testing for injecting a mock client
    public BasicPassUpdater(PassEntityUtil passEntityUtil, PassClient passClient) {
        this.passEntityUtil = passEntityUtil;
        this.passClient = passClient;
    }

    public void updatePass(Collection<Map<String, String>> results, String mode) {
        this.mode = mode;
        userMap.clear();
        funderMap.clear();
        statistics.reset();
        statistics.setType(mode);
        switch (mode) {
            case "grant":
                updateGrants(results);
                break;
            case "user":
                updateUsers(results);
                break;
            case "funder":
                updateFunders(results);
                break;
            default:
                break;
        }
    }

    /**
     * Build a Collection of Grants from a ResultSet, then update the grants in Pass
     * Because we need to make sure we catch any updates to fields referenced by URIs, we construct
     * these and update these as well
     */
    private void updateGrants(Collection<Map<String, String>> results) {

        //a grant will have several rows in the ResultSet if there are co-pis. so we put the grant on this
        //Map and add to it as additional rows add information.
        Map<String, Grant> grantMap = new HashMap<>();

        LOG.info("Processing result set with {} rows", results.size());
        boolean modeChecked = false;

        for (Map<String, String> rowMap : results) {

            String grantLocalKey;

            if (!modeChecked) {
                if (!rowMap.containsKey(C_GRANT_LOCAL_KEY)) { //we always have this for grants
                    throw new RuntimeException("Mode of grant was supplied, but data does not seem to match.");
                } else {
                    modeChecked = true;
                }
            }

            grantLocalKey = rowMap.get(C_GRANT_LOCAL_KEY);

            String directFunderLocalKey = rowMap.get(C_DIRECT_FUNDER_LOCAL_KEY);
            String primaryFunderLocalKey = rowMap.get(C_PRIMARY_FUNDER_LOCAL_KEY);
            primaryFunderLocalKey = (primaryFunderLocalKey == null ? directFunderLocalKey : primaryFunderLocalKey);
            Grant grant;
            LOG.debug("Processing grant with localKey {}", grantLocalKey);
            //if this is the first record for this Grant, it will not be on the Map
            //we process all data which is common to every record for this grant
            //i.e., everything except the investigator(s)
            if (!grantMap.containsKey(grantLocalKey)) {
                grant = new Grant();
                grant.setAwardNumber(rowMap.get(C_GRANT_AWARD_NUMBER));

                String status = rowMap.getOrDefault(C_GRANT_AWARD_STATUS, null);

                if (status != null) {
                    switch (status) {
                        case "Active":
                            grant.setAwardStatus(Grant.AwardStatus.ACTIVE);
                            break;
                        case "Pre-Award":
                            grant.setAwardStatus(Grant.AwardStatus.PRE_AWARD);
                            break;
                        case "Terminated":
                            grant.setAwardStatus(Grant.AwardStatus.TERMINATED);
                            break;
                        default:
                            break;
                    }
                } else {
                    grant.setAwardStatus(null);
                }

                grant.setLocalKey(grantLocalKey);
                grant.setProjectName(rowMap.get(C_GRANT_PROJECT_NAME));
                grant.setAwardDate(createJodaDateTime(rowMap.getOrDefault(C_GRANT_AWARD_DATE, null)));
                grant.setStartDate(createJodaDateTime(rowMap.getOrDefault(C_GRANT_START_DATE, null)));
                grant.setEndDate(createJodaDateTime(rowMap.getOrDefault(C_GRANT_END_DATE, null)));

                //process direct funder, and primary funder if we have one
                //update funder(s) in Pass as needed
                if (funderMap.containsKey(directFunderLocalKey)) {
                    grant.setDirectFunder(funderMap.get(directFunderLocalKey));
                } else {
                    Funder updatedFunder = buildDirectFunder(rowMap);
                    URI passFunderURI = updateFunderInPass(updatedFunder);
                    funderMap.put(directFunderLocalKey, passFunderURI);
                    grant.setDirectFunder(passFunderURI);
                }

                if (funderMap.containsKey(primaryFunderLocalKey)) {
                    grant.setPrimaryFunder(funderMap.get(primaryFunderLocalKey));
                } else {
                    Funder updatedFunder = buildPrimaryFunder(rowMap);
                    URI passFunderURI = updateFunderInPass(updatedFunder);
                    funderMap.put(primaryFunderLocalKey, passFunderURI);
                    grant.setPrimaryFunder(passFunderURI);
                }

                grant.setCoPis(new ArrayList<>());//we will build this from scratch in either case
                grantMap.put(grantLocalKey, grant);//save the state of this Grant
            }

            //now we process the User (investigator)
            grant = grantMap.get(grantLocalKey);
            String employeeId = rowMap.get(C_USER_EMPLOYEE_ID);
            String abbreviatedRole = rowMap.get(C_ABBREVIATED_ROLE);

            if (abbreviatedRole.equals("C") || abbreviatedRole.equals("K") || grant.getPi() == null) {
                if (!userMap.containsKey(employeeId)) {
                    User updatedUser = buildUser(rowMap);
                    URI passUserURI = updateUserInPass(updatedUser);
                    userMap.put(employeeId, passUserURI);
                }

                //now our User URI is on the map - let's process:
                if (abbreviatedRole.equals("P")) {
                    if (grant.getPi() == null) {
                        grant.setPi(userMap.get(employeeId));
                        statistics.addPi();
                    } else { // handle data with duplicate PIs
                        grant.getCoPis().add(userMap.get(employeeId));
                        statistics.addCoPi();
                    }
                } else if ((abbreviatedRole.equals("C") || abbreviatedRole.equals("K")) && !grant.getCoPis().contains(
                    userMap.get(employeeId))) {
                    grant.getCoPis().add(userMap.get(employeeId));
                    statistics.addCoPi();
                }
            }
            //we are done with this record, let's save the state of this Grant
            grantMap.put(grantLocalKey, grant);
            //see if this is the latest grant updated
            if (rowMap.containsKey(C_UPDATE_TIMESTAMP)) {
                String grantUpdateString = rowMap.get(C_UPDATE_TIMESTAMP);
                latestUpdateString = latestUpdateString.length() == 0 ? grantUpdateString : returnLaterUpdate(
                    grantUpdateString, latestUpdateString);
            }
        }

        //now put updated grant objects in pass
        for (Grant grant : grantMap.values()) {
            grantUriMap.put(updateGrantInPass(grant), grant);
        }

        //success - we capture some information to report
        if (grantMap.size() > 0) {
            statistics.setLatestUpdateString(latestUpdateString);
            statistics.setReport(results.size(), grantMap.size());
        } else {
            System.out.println("No records were processed in this update");
        }
    }

    private void updateUsers(Collection<Map<String, String>> results) {

        boolean modeChecked = false;

        for (Map<String, String> rowMap : results) {

            if (!modeChecked) {
                if (!rowMap.containsKey(C_USER_EMPLOYEE_ID)) { //we always have this for users
                    throw new RuntimeException("Mode of user was supplied, but data does not seem to match.");
                } else {
                    modeChecked = true;
                }
            }

            LOG.info("Processing result set with {} rows", results.size());
            User updatedUser = buildUser(rowMap);
            updateUserInPass(updatedUser);
            if (rowMap.containsKey(C_UPDATE_TIMESTAMP)) {
                String userUpdateString = rowMap.get(C_UPDATE_TIMESTAMP);
                latestUpdateString = latestUpdateString.length() == 0 ? userUpdateString : returnLaterUpdate(
                    userUpdateString, latestUpdateString);
            }
        }

        if (results.size() > 0) {
            statistics.setLatestUpdateString(latestUpdateString);
            statistics.setReport(results.size(), results.size());
        } else {
            System.out.println("No records were processed in this update");
        }

    }

    /**
     * This method is called for the "funder" mode - the column names will have the values for primary funders
     *
     * @param results the data row map containing funder information
     */
    private void updateFunders(Collection<Map<String, String>> results) {

        boolean modeChecked = false;
        LOG.info("Processing result set with {} rows", results.size());
        for (Map<String, String> rowMap : results) {

            if (!modeChecked) {
                if (!rowMap.containsKey(C_PRIMARY_FUNDER_LOCAL_KEY) && !rowMap.containsKey(C_PRIMARY_FUNDER_NAME)) {
                    throw new RuntimeException("Mode of funder was supplied, but data does not seem to match.");
                } else {
                    modeChecked = true;
                }
            }

            Funder updatedFunder = buildPrimaryFunder(rowMap);
            updateFunderInPass(updatedFunder);

        }
        statistics.setReport(results.size(), results.size());
    }

    User buildUser(Map<String, String> rowMap) {
        User user = new User();
        user.setFirstName(rowMap.get(C_USER_FIRST_NAME));
        user.setMiddleName(rowMap.getOrDefault(C_USER_MIDDLE_NAME, null));
        user.setLastName(rowMap.get(C_USER_LAST_NAME));
        user.setDisplayName(rowMap.get(C_USER_FIRST_NAME) + " " + rowMap.get(C_USER_LAST_NAME));
        user.setEmail(rowMap.get(C_USER_EMAIL));
        String employeeId = rowMap.get(C_USER_EMPLOYEE_ID);
        //Build the List of locatorIds - put the most reliable ids first
        if (employeeId != null) {
            String EMPLOYEE_ID_TYPE = "employeeid";
            user.getLocatorIds().add(new Identifier(DOMAIN, EMPLOYEE_ID_TYPE, employeeId).serialize());
        }
        user.getRoles().add(User.Role.SUBMITTER);
        LOG.debug("Built user with employee ID {}", employeeId);
        return user;
    }

    /**
     * this method gets called on a grant mode process if the primary funder is different from direct, and also
     * any time the updater is called in funder mode
     *
     * @param rowMap the funder data map
     * @return the funder
     */
    Funder buildPrimaryFunder(Map<String, String> rowMap) {
        Funder funder = new Funder();
        funder.setName(rowMap.getOrDefault(C_PRIMARY_FUNDER_NAME, null));
        funder.setLocalKey(rowMap.get(C_PRIMARY_FUNDER_LOCAL_KEY));
        String policy = rowMap.get(C_PRIMARY_FUNDER_POLICY);
        if (policy != null && policy.length() > 0) {
            String fedoraBaseUrl = System.getProperty("pass.fedora.baseurl");
            fedoraBaseUrl = fedoraBaseUrl.endsWith("/") ? fedoraBaseUrl : fedoraBaseUrl + "/";
            funder.setPolicy(URI.create(fedoraBaseUrl + policy));
            LOG.debug("Processing Funder with localKey {}", funder.getLocalKey() +
                                                            " and Policy " + policy);
        }
        LOG.debug("Built Funder with localKey {}", funder.getLocalKey());

        return funder;
    }

    private Funder buildDirectFunder(Map<String, String> rowMap) {
        Funder funder = new Funder();
        if (rowMap.containsKey(C_DIRECT_FUNDER_NAME)) {
            funder.setName(rowMap.get(C_DIRECT_FUNDER_NAME));
        }
        funder.setLocalKey(rowMap.get(C_DIRECT_FUNDER_LOCAL_KEY));
        String policy = rowMap.get(C_DIRECT_FUNDER_POLICY);
        if (policy != null) {
            String fedoraBaseUrl = System.getProperty("pass.fedora.baseurl");
            fedoraBaseUrl = fedoraBaseUrl.endsWith("/") ? fedoraBaseUrl : fedoraBaseUrl + "/";
            funder.setPolicy(URI.create(fedoraBaseUrl + policy));
            LOG.debug("Processing Funder with localKey {} and policy {}", funder.getLocalKey(), policy);
        }
        LOG.debug("Built Funder with localKey {}", funder.getLocalKey());

        return funder;
    }

    /**
     * Take a new Funder object populated as fully as possible from the COEUS pull, and use this
     * new information to update an object for the same Funder in Pass (if it exists)
     *
     * @param systemFunder the new Funder object populated from COEUS
     * @return the URI for the resource representing the updated Funder in Pass
     */
    private URI updateFunderInPass(Funder systemFunder) {
        String baseLocalKey = systemFunder.getLocalKey();
        String FUNDER_ID_TYPE = "funder";
        String fullLocalKey = new Identifier(DOMAIN, FUNDER_ID_TYPE, baseLocalKey).serialize();
        systemFunder.setLocalKey(fullLocalKey);

        URI passFunderURI = passClient.findByAttribute(Funder.class, "localKey", fullLocalKey);
        if (passFunderURI != null) {
            Funder storedFunder = passClient.readResource(passFunderURI, Funder.class);
            if (storedFunder == null) {
                throw new RuntimeException("Could not read Funder object with URI " + passFunderURI);
            }
            Funder updatedFunder;
            if ((updatedFunder = passEntityUtil.update(systemFunder, storedFunder)) != null) { //need to update
                passClient.updateResource(updatedFunder);
                statistics.addFundersUpdated();
            } //if the Pass version is COEUS-equal to our version from the update, we don't have to do anything
            //this can happen if the Grant was updated in COEUS only with information we don't consume here
        } else { //don't have a stored Funder for this URI - this one is new to Pass
            if (systemFunder.getName() != null) { //only add if we have a name
                passFunderURI = passClient.createResource(systemFunder);
                statistics.addFundersCreated();
            }
        }
        return passFunderURI;
    }

    /**
     * Take a new User object populated as fully as possible from the COEUS pull, and use this
     * new information to update an object for the same User in Pass (if it exists)
     *
     * @param systemUser the new User object populated from COEUS
     * @return the URI for the resource representing the updated User in Pass
     */
    private URI updateUserInPass(User systemUser) {
        //we first check to see if the user is known by the Hopkins ID. If not, we check the employee ID.
        //last attempt is the JHED ID. this order is specified by the order of the List as constructed on updatedUser
        URI passUserUri = null;
        ListIterator<String> idIterator = systemUser.getLocatorIds().listIterator();

        while (passUserUri == null && idIterator.hasNext()) {
            String id = String.valueOf(idIterator.next());
            if (id != null) {
                passUserUri = passClient.findByAttribute(User.class, "locatorIds", id);
            }
        }

        if (passUserUri != null) {
            User storedUser = passClient.readResource(passUserUri, User.class);
            if (storedUser == null) {
                throw new RuntimeException("Could not read User object with URI " + passUserUri);
            }
            User updatedUser;
            if ((updatedUser = passEntityUtil.update(systemUser, storedUser)) != null) { //need to update
                //post COEUS processing goes here
                if (!storedUser.getRoles().contains(User.Role.SUBMITTER)) {
                    storedUser.getRoles().add(User.Role.SUBMITTER);
                }
                passClient.updateResource(updatedUser);
                statistics.addUsersUpdated();
            } //if the Pass version is COEUS-equal to our version from the update, and there are no null fields we
            // care about,
            //we don't have to do anything. this can happen if the User was updated in COEUS only with information we
            // don't consume here
        } else if (!mode.equals("user")) { //don't have a stored User for this URI - this one is new to Pass
            //but don't update if we are in user mode - just update existing users
            passUserUri = passClient.createResource(systemUser);
            statistics.addUsersCreated();
        }
        return passUserUri;
    }

    /**
     * Take a new Grant object populated as fully as possible from the COEUS pull, and use this
     * new information to update an object for the same Grant in Pass (if it exists)
     *
     * @param systemGrant the new Grant object populated from COEUS
     * @return the PASS identifier for the Grant object
     */
    private URI updateGrantInPass(Grant systemGrant) {
        String baseLocalKey = systemGrant.getLocalKey();
        String GRANT_ID_TYPE = "grant";
        String fullLocalKey = new Identifier(DOMAIN, GRANT_ID_TYPE, baseLocalKey).serialize();
        systemGrant.setLocalKey(fullLocalKey);

        LOG.debug("Looking for grant with localKey {}", fullLocalKey);
        URI passGrantURI = passClient.findByAttribute(Grant.class, "localKey", fullLocalKey);
        if (passGrantURI != null) {
            LOG.debug("Found grant with localKey {} ", fullLocalKey);
            Grant storedGrant = passClient.readResource(passGrantURI, Grant.class);
            if (storedGrant == null) {
                throw new RuntimeException("Could not read Funder object with URI " + passGrantURI);
            }
            Grant updatedGrant;
            if ((updatedGrant = passEntityUtil.update(systemGrant, storedGrant)) != null) { //need to update
                LOG.debug("Updating grant with localKey {} to localKey {}", storedGrant.getLocalKey(),
                          systemGrant.getLocalKey());
                passClient.updateResource(updatedGrant);
                statistics.addGrantsUpdated();
                LOG.debug("Updating grant with award number {}", systemGrant.getLocalKey());
            } //if the Pass version is COEUS-equal to our version from the update, we don't have to do anything
            //this can happen if the Grant was updated in COEUS only with information we don't consume here
        } else { //don't have a stored Grant for this URI - this one is new to Pass
            passGrantURI = passClient.createResource(systemGrant);
            statistics.addGrantsCreated();
            LOG.debug("Creating grant with award number {}", systemGrant.getLocalKey());
        }
        return passGrantURI;
    }

    /**
     * Compare two timestamps and return the later of them
     *
     * @param currentUpdateString the current latest timestamp string
     * @param latestUpdateString  the new timestamp to be compared against the current latest timestamp
     * @return the later of the two parameters
     */
    static String returnLaterUpdate(String currentUpdateString, String latestUpdateString) {
        DateTime grantUpdateTime = createJodaDateTime(currentUpdateString);
        DateTime previousLatestUpdateTime = createJodaDateTime(latestUpdateString);
        return grantUpdateTime.isAfter(previousLatestUpdateTime) ? currentUpdateString : latestUpdateString;
    }

    /**
     * This method provides the latest timestamp of all records processed. After processing, this timestamp
     * will be used to be tha base timestamp for the next run of the app
     *
     * @return the latest update timestamp string
     */
    public String getLatestUpdate() {
        return this.latestUpdateString;
    }

    /**
     * This returns the final statistics of the processing of the Grant or User Set
     *
     * @return the report
     */
    public String getReport() {
        return statistics.getReport();
    }

    /**
     * This returns the final statistics Object - useful in testing
     *
     * @return the statistics object
     */
    public PassUpdateStatistics getStatistics() {
        return statistics;
    }

    public Map<URI, Grant> getGrantUriMap() {
        return grantUriMap;
    }

    //this is used by an integration test
    public PassClient getPassClient() {
        return passClient;
    }

    //used in unit test
    Map<String, URI> getFunderMap() {
        return funderMap;
    }

    //used in unit test
    Map<String, URI> getUserMap() {
        return userMap;
    }

    public void setDomain(String domain) {
        this.DOMAIN = domain;
    }

}