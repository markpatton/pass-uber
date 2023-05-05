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
package org.eclipse.pass.support.grant;

import static java.lang.Thread.sleep;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_ABBREVIATED_ROLE;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_DIRECT_FUNDER_LOCAL_KEY;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_DIRECT_FUNDER_NAME;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_DIRECT_FUNDER_POLICY;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_GRANT_AWARD_DATE;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_GRANT_AWARD_NUMBER;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_GRANT_AWARD_STATUS;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_GRANT_END_DATE;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_GRANT_LOCAL_KEY;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_GRANT_PROJECT_NAME;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_GRANT_START_DATE;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_PRIMARY_FUNDER_LOCAL_KEY;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_PRIMARY_FUNDER_NAME;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_PRIMARY_FUNDER_POLICY;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_UPDATE_TIMESTAMP;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_USER_EMAIL;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_USER_EMPLOYEE_ID;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_USER_FIRST_NAME;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_USER_HOPKINS_ID;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_USER_INSTITUTIONAL_ID;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_USER_LAST_NAME;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_USER_MIDDLE_NAME;
import static org.eclipse.pass.support.grant.data.DateTimeUtil.createJodaDateTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.pass.support.client.PassClient;
import org.eclipse.pass.support.client.model.Grant;
import org.eclipse.pass.support.client.model.Policy;
import org.eclipse.pass.support.grant.data.JhuPassUpdater;
import org.eclipse.pass.support.grant.data.PassUpdateStatistics;
import org.junit.Before;
import org.junit.Test;

public class JhuPassUpdaterIT {

    String[] grantAwardNumber = {"B10000000", "B10000001", "B10000002"};
    String[] grantLocalKey = {"10000001", "10000001", "10000001"}; //all the same, different from other ITs tho
    String[] grantProjectName = {"Stupendous Research Project I", "Stupendous Research Project II", "Stupendous " +
                                                                                                    "Research Project" +
                                                                                                    " III"};
    String[] grantAwardDate = {"01/01/1999", "01/01/2001", "01/01/2003"};
    String[] grantStartDate = {"07/01/2000", "07/01/2000", "07/01/2000"}; //these appear to ge the same for all awards
    String[] grantEndDate = {"06/30/2004", "06/30/2004", "06/30/2004"};//these seem to be the same for all awards
    String[] grantUpdateTimestamp = {"2006-03-11 00:00:00.0", "2010-04-05 00:00:00.0", "2015-11-11 00:00:00.0"};
    String[] userEmployeeId = {"31000000", "31000001", "31000002"};
    String[] userInstitutionalId = {"arecko1", "sclass1", "jgunn1"};
    String[] userHopkinsId = {"DOMNAR", "NROAD", "ROMAND"};
    String[] userFirstName = {"Amanda", "Skip", "Janie"};
    String[] userMiddleName = {"Bea", "Avery", "Gotta"};
    String[] userLastName = {"Reckondwith", "Class", "Gunn"};
    String[] userEmail = {"arecko1@jhu.edu", "sclass1@jhu.edu", "jgunn1@jhu.edu"};


    String primaryFunderPolicyUriString;
    String directFunderPolicyUriString;

    String grantIdPrefix = "johnshopkins.edu:grant:";
    //String funderIdPrefix = "johnshopkins.edu:funder:";
    //String hopkinsidPrefix = "johnshopkins.edu:hopkinsid:";
    String employeeidPrefix = "johnshopkins.edu:employeeid:";
    //String jhedidPrefis = "johnshopkins.edu:jhed:";

    PassClient passClient = PassClientFactory.getPassClient();
    JhuPassUpdater passUpdater = new JhuPassUpdater(passClient);
    PassUpdateStatistics statistics = passUpdater.getStatistics();

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
     * we put an initial award for a grant into fedora, then simulate a pull of all subsequent records
     *
     * We expect to see some fields retained from the initial award, and others updated. The most
     * interesting fields are the investigator fields: all CO-PIs ever on the grant should stay on the
     * co-pi field throughout iterations. If a PI is changed, they should appear on the CO-PI field
     *
     * @throws InterruptedException from joda date time creation
     */
    @Test
    public void processGrantIT() throws InterruptedException {
        List<Map<String, String>> resultSet = new ArrayList<>();

        //put in initial iteration as a correct existing record - PI is Reckondwith, Co-pi is Class
        Map<String, String> piRecord0 = makeRowMap(0, 0, "P");
        Map<String, String> coPiRecord0 = makeRowMap(0, 1, "C");

        resultSet.add(piRecord0);
        resultSet.add(coPiRecord0);

        passUpdater.updatePass(resultSet, "grant");
        sleep(10000);
        URI passUser0Uri = passClient.findByAttribute(User.class, "locatorIds", employeeidPrefix + userEmployeeId[0]);
        assertNotNull(passUser0Uri);
        URI passGrantUri = passClient.findByAttribute(Grant.class, "localKey", grantIdPrefix + grantLocalKey[2]);
        assertNotNull(passGrantUri);
        URI passUser1Uri = passClient.findByAttribute(User.class, "locatorIds", employeeidPrefix + userEmployeeId[1]);
        assertNotNull(passUser1Uri);

        Grant passGrant = passClient.readResource(passGrantUri, Grant.class);

        assertEquals(grantAwardNumber[0], passGrant.getAwardNumber());
        assertEquals(Grant.AwardStatus.ACTIVE, passGrant.getAwardStatus());
        assertEquals(grantIdPrefix + grantLocalKey[0], passGrant.getLocalKey());
        assertEquals(grantProjectName[0], passGrant.getProjectName());
        assertEquals(createJodaDateTime(grantAwardDate[0]), passGrant.getAwardDate());
        assertEquals(createJodaDateTime(grantStartDate[0]), passGrant.getStartDate());
        assertEquals(createJodaDateTime(grantEndDate[0]), passGrant.getEndDate());
        assertEquals(passUser0Uri, passGrant.getPi()); //Reckondwith
        assertEquals(1, passGrant.getCoPis().size());
        assertEquals(passUser1Uri, passGrant.getCoPis().get(0));

        //check statistics
        assertEquals(1, statistics.getGrantsCreated());
        assertEquals(2, statistics.getUsersCreated());
        assertEquals(1, statistics.getPisAdded());
        assertEquals(1, statistics.getCoPisAdded());

        //now simulate an incremental pull since the initial,  adjust the stored grant
        //we add a new co-pi Jones in the "1" iteration, and change the pi to Einstein in the "2" iteration
        //we drop co-pi jones in the last iteration

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
        sleep(10000);

        passGrant = passClient.readResource(passGrantUri, Grant.class);

        URI passUser2Uri = passClient.findByAttribute(User.class, "locatorIds", employeeidPrefix + userEmployeeId[2]);
        assertNotNull(passUser2Uri);

        assertEquals(grantAwardNumber[0], passGrant.getAwardNumber());//initial
        assertEquals(Grant.AwardStatus.ACTIVE, passGrant.getAwardStatus());
        assertEquals(grantIdPrefix + grantLocalKey[0], passGrant.getLocalKey());
        assertEquals(grantProjectName[0], passGrant.getProjectName());//initial
        assertEquals(createJodaDateTime(grantAwardDate[0]), passGrant.getAwardDate());//initial
        assertEquals(createJodaDateTime(grantStartDate[0]), passGrant.getStartDate());//initial
        assertEquals(createJodaDateTime(grantEndDate[2]), passGrant.getEndDate());//latest
        assertEquals(passUser1Uri, passGrant.getPi());//Class
        assertEquals(2, passGrant.getCoPis().size());
        assertTrue(passGrant.getCoPis().contains(passUser0Uri));//Reckondwith
        assertTrue(passGrant.getCoPis().contains(passUser2Uri));//Gunn
    }

    /**
     * utility method to produce data as it would look coming from COEUS
     *
     * @param iteration the iteration of the (multi-award) grant
     * @param user      the user supplied in the record
     * @param abbrRole  the role: Pi ("P") or co-pi (C" or "K")
     * @return row map for pull record
     */
    private Map<String, String> makeRowMap(int iteration, int user, String abbrRole) {
        Map<String, String> rowMap = new HashMap<>();
        rowMap.put(C_GRANT_AWARD_NUMBER, grantAwardNumber[iteration]);
        rowMap.put(C_GRANT_AWARD_STATUS, "Active");
        rowMap.put(C_GRANT_LOCAL_KEY, grantLocalKey[iteration]);
        rowMap.put(C_GRANT_PROJECT_NAME, grantProjectName[iteration]);
        rowMap.put(C_GRANT_AWARD_DATE, grantAwardDate[iteration]);
        rowMap.put(C_GRANT_START_DATE, grantStartDate[iteration]);
        rowMap.put(C_GRANT_END_DATE, grantEndDate[iteration]);

        rowMap.put(C_DIRECT_FUNDER_LOCAL_KEY, "20000000");
        rowMap.put(C_DIRECT_FUNDER_NAME, "Enormous State University");
        rowMap.put(C_PRIMARY_FUNDER_LOCAL_KEY, "20000001");
        rowMap.put(C_PRIMARY_FUNDER_NAME, "J L Gotrocks Foundation");

        rowMap.put(C_USER_FIRST_NAME, userFirstName[user]);
        rowMap.put(C_USER_MIDDLE_NAME, userMiddleName[user]);
        rowMap.put(C_USER_LAST_NAME, userLastName[user]);
        rowMap.put(C_USER_EMAIL, userEmail[user]);
        rowMap.put(C_USER_INSTITUTIONAL_ID, userInstitutionalId[user]);
        rowMap.put(C_USER_EMPLOYEE_ID, userEmployeeId[user]);
        rowMap.put(C_USER_HOPKINS_ID, userHopkinsId[user]);

        rowMap.put(C_UPDATE_TIMESTAMP, grantUpdateTimestamp[iteration]);
        rowMap.put(C_ABBREVIATED_ROLE, abbrRole);

        rowMap.put(C_DIRECT_FUNDER_POLICY, directFunderPolicyUriString);
        rowMap.put(C_PRIMARY_FUNDER_POLICY, primaryFunderPolicyUriString);

        return rowMap;
    }

}
