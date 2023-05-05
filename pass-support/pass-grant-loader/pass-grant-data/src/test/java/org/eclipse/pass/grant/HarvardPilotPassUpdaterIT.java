/*
 * Copyright 2019 Johns Hopkins University
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

package org.eclipse.pass.grant;

import static java.lang.Thread.sleep;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_ABBREVIATED_ROLE;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_DIRECT_FUNDER_LOCAL_KEY;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_DIRECT_FUNDER_NAME;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_DIRECT_FUNDER_POLICY;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_GRANT_AWARD_NUMBER;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_GRANT_AWARD_STATUS;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_GRANT_END_DATE;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_GRANT_LOCAL_KEY;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_GRANT_PROJECT_NAME;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_GRANT_START_DATE;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_PRIMARY_FUNDER_LOCAL_KEY;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_PRIMARY_FUNDER_NAME;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_PRIMARY_FUNDER_POLICY;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_USER_EMAIL;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_USER_EMPLOYEE_ID;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_USER_FIRST_NAME;
import static org.eclipse.pass.grant.data.CoeusFieldNames.C_USER_LAST_NAME;
import static org.eclipse.pass.grant.data.DateTimeUtil.createJodaDateTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.pass.client.PassClient;
import org.dataconservancy.pass.client.PassClientFactory;
import org.eclipse.pass.grant.data.HarvardPilotPassUpdater;
import org.eclipse.pass.grant.data.PassUpdateStatistics;
import org.eclipse.pass.grant.data.PassUpdater;
import org.dataconservancy.pass.model.Grant;
import org.dataconservancy.pass.model.Policy;
import org.dataconservancy.pass.model.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class HarvardPilotPassUpdaterIT {

    PassClient passClient = PassClientFactory.getPassClient();
    PassUpdater passUpdater = new HarvardPilotPassUpdater(passClient);
    PassUpdateStatistics statistics = passUpdater.getStatistics();

    String[] grantAwardNumber = {"C10000000", "C10000001", "C10000002"};
    String[] grantLocalKey = {"10000002", "10000002", "10000002"}; //all the same, different from other ITs tho
    String[] grantProjectName = {"Amazing Research Project I", "Amazing Research Project II", "Amazing Research " +
                                                                                              "Project III"};
    String[] grantStartDate = {"07/01/2000", "07/01/2002", "07/01/2004"};
    String[] grantEndDate = {"06/30/2002", "06/30/2004", "06/30/2006"};
    //   String[] grantUpdateTimestamp = { "2006-03-11 00:00:00.0","2010-04-05 00:00:00.0", "2015-11-11 00:00:00.0" };
    String[] userEmployeeId = {"A0000001", "A0000002", "A0000003"};
    String[] userFirstName = {"John", "Simon", "Rocket"};
    String[] userLastName = {"Public", "Sinister", "Squirrel"};
    String[] userEmail = {"jpubli1@harvard.edu", "ssinis11@harvard.edu", "rsquir1@harvard.edu"};

    String primaryFunderPolicyUriString;
    String directFunderPolicyUriString;

    String grantIdPrefix = "harvard.edu:grant:";
    String institutionalIdPrefix = "harvard.edu:jhed:";


    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setup() {
        String prefix = System.getProperty("pass.fedora.baseurl");
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }

        Policy policy = new Policy();
        policy.setTitle("Primary Policy 2");
        policy.setDescription("BAA");
        URI policyURI = passClient.createResource(policy);
        primaryFunderPolicyUriString = policyURI.toString().substring(prefix.length());

        policy = new Policy();
        policy.setTitle("Direct Policy 2");
        policy.setDescription("BAA");
        policyURI = passClient.createResource(policy);
        directFunderPolicyUriString = policyURI.toString().substring(prefix.length());

    }

    /**
     * The behavior of PassUpdate's updatePass() method is to compare the data coming in on the ResultSet with
     * the existing data in Pass, and create objects if Pass does not yet have them, and update them if they exist in
     * Pass but
     * there are differences in the fields for whichthe pull is the authoritative source, or else has a clue about
     * other fields which are null
     * on the PASS object.
     *
     * For the Harvard Pilot, we use the user name on the email address to stand for the user's eppn fpr the purposes
     * of populating that
     * locator id field
     *
     * @throws InterruptedException - the exception
     */
    @Test
    public void updateGrantsIT() throws InterruptedException {

        List<Map<String, String>> resultSet = new ArrayList<>();

        //put in initial iteration as a correct existing record - PI is Public, Co-pi is Sinister
        Map<String, String> piRecord0 = makeRowMap(0, 0, "P");
        Map<String, String> coPiRecord0 = makeRowMap(0, 1, "C");

        resultSet.add(piRecord0);
        resultSet.add(coPiRecord0);

        passUpdater.updatePass(resultSet, "grant");
        sleep(10000);
        URI passUser0Uri = passClient.findByAttribute(User.class, "locatorIds",
                                                      institutionalIdPrefix + userEmail[0].split("@")[0]);
        assertNotNull(passUser0Uri);
        URI passGrantUri = passClient.findByAttribute(Grant.class, "localKey", grantIdPrefix + grantLocalKey[2]);
        assertNotNull(passGrantUri);
        URI passUser1Uri = passClient.findByAttribute(User.class, "locatorIds",
                                                      institutionalIdPrefix + userEmail[1].split("@")[0]);
        assertNotNull(passUser1Uri);

        Grant passGrant = passClient.readResource(passGrantUri, Grant.class);

        assertEquals(grantAwardNumber[0], passGrant.getAwardNumber());
        assertEquals(Grant.AwardStatus.ACTIVE, passGrant.getAwardStatus());
        assertEquals(grantIdPrefix + grantLocalKey[0], passGrant.getLocalKey());
        assertEquals(grantProjectName[0], passGrant.getProjectName());
        assertEquals(createJodaDateTime(grantStartDate[0]), passGrant.getStartDate());
        assertEquals(createJodaDateTime(grantEndDate[0]), passGrant.getEndDate());
        assertEquals(passUser0Uri, passGrant.getPi()); //Pblic
        assertEquals(1, passGrant.getCoPis().size());
        assertEquals(passUser1Uri, passGrant.getCoPis().get(0));

        //check statistics
        assertEquals(1, statistics.getGrantsCreated());
        assertEquals(2, statistics.getUsersCreated());
        assertEquals(1, statistics.getPisAdded());
        assertEquals(1, statistics.getCoPisAdded());

        //now simulate an incremental pull since the initial,  adjust the stored grant
        //we add a new co-pi Squirrel in the "1" iteration, and change the pi to Einstein in the "2" iteration
        //we drop co-pi Squirrel in the last iteration

        Map<String, String> piRecord1 = makeRowMap(1, 0, "P");
        Map<String, String> coPiRecord1 = makeRowMap(1, 1, "C");
        Map<String, String> newCoPiRecord1 = makeRowMap(1, 2, "C");
        Map<String, String> piRecord2 = makeRowMap(2, 1, "P");

        //add in everything since the initial pull
        resultSet.clear();
        resultSet.add(piRecord1);
        resultSet.add(coPiRecord1);
        resultSet.add(newCoPiRecord1);
        resultSet.add(piRecord2);

        passUpdater.updatePass(resultSet, "grant");
        sleep(12000);

        passGrant = passClient.readResource(passGrantUri, Grant.class);

        URI passUser2Uri = passClient.findByAttribute(User.class, "locatorIds",
                                                      institutionalIdPrefix + userEmail[2].split("@")[0]);
        assertNotNull(passUser2Uri);

        assertEquals(grantAwardNumber[1], passGrant.getAwardNumber());//earliest of the additions
        assertEquals(Grant.AwardStatus.ACTIVE, passGrant.getAwardStatus());
        assertEquals(grantIdPrefix + grantLocalKey[1], passGrant.getLocalKey());//earliest of the additions
        assertEquals(grantProjectName[1], passGrant.getProjectName());//earliest of the additions
        assertEquals(createJodaDateTime(grantStartDate[1]), passGrant.getStartDate());//earliest of the additions
        assertEquals(createJodaDateTime(grantEndDate[1]), passGrant.getEndDate());//earliest of the additions
        assertEquals(passUser0Uri, passGrant.getPi());//first one in the pull
        assertEquals(2, passGrant.getCoPis().size());
        assertTrue(passGrant.getCoPis().contains(passUser1Uri));//Public
        assertTrue(passGrant.getCoPis().contains(passUser2Uri));//Sinister
    }

    /**
     * utility method to produce data as it would look coming from the Harvard spreadsheet
     *
     * @param iteration the iteration of the (multi-award) grant
     * @param user      the user supplied in the record
     * @param abbrRole  the role: Pi ("P") or co-pi (C" or "K")
     * @return the row map for the pull record
     */
    private Map<String, String> makeRowMap(int iteration, int user, String abbrRole) {
        Map<String, String> rowMap = new HashMap<>();
        rowMap.put(C_GRANT_AWARD_NUMBER, grantAwardNumber[iteration]);
        rowMap.put(C_GRANT_AWARD_STATUS, "Active");
        rowMap.put(C_GRANT_LOCAL_KEY, grantLocalKey[iteration]);
        rowMap.put(C_GRANT_PROJECT_NAME, grantProjectName[iteration]);
        rowMap.put(C_GRANT_START_DATE, grantStartDate[iteration]);
        rowMap.put(C_GRANT_END_DATE, grantEndDate[iteration]);

        rowMap.put(C_DIRECT_FUNDER_LOCAL_KEY, "20000002");
        rowMap.put(C_DIRECT_FUNDER_NAME, "Gargantuan State University");
        rowMap.put(C_PRIMARY_FUNDER_LOCAL_KEY, "20000003");
        rowMap.put(C_PRIMARY_FUNDER_NAME, "D Warbucks Foundation");

        rowMap.put(C_USER_FIRST_NAME, userFirstName[user]);
        rowMap.put(C_USER_LAST_NAME, userLastName[user]);
        rowMap.put(C_USER_EMAIL, userEmail[user]);
        rowMap.put(C_USER_EMPLOYEE_ID, userEmployeeId[user]);

        rowMap.put(C_ABBREVIATED_ROLE, abbrRole);

        rowMap.put(C_DIRECT_FUNDER_POLICY, directFunderPolicyUriString);
        rowMap.put(C_PRIMARY_FUNDER_POLICY, primaryFunderPolicyUriString);

        return rowMap;
    }

}
