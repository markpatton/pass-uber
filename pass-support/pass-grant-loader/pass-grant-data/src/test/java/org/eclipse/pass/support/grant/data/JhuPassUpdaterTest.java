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

package org.eclipse.pass.support.grant.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.pass.client.PassClient;
import org.dataconservancy.pass.client.PassClientFactory;
import org.dataconservancy.pass.model.Funder;
import org.dataconservancy.pass.model.Grant;
import org.dataconservancy.pass.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test class for building the {@code List} of {@code Grant}s
 *
 * @author jrm@jhu.edu
 */
@RunWith(MockitoJUnitRunner.class)
public class JhuPassUpdaterTest {

    @Mock
    private PassClient passClientMock;

    // private PassEntityUtil passEntityUtil = new CoeusPassEntityUtil();
    // private String domain = "johnshopkins.edu";

    private URI grantUri;

    @Before
    public void setup() {
        System.setProperty("pass.fedora.baseurl", "https://localhost:8080/fcrepo/rest/");

        grantUri = URI.create("grantUri");
        URI userUri1 = URI.create("piUri");
        URI useruri2 = URI.create("coPiUri");
        URI funderUri1 = URI.create("funderuri1");
        URI funderUri2 = URI.create("funderuri2");

        when(passClientMock.createResource(any(Grant.class))).thenReturn(grantUri);
        when(passClientMock.createResource(any(Funder.class))).thenReturn(funderUri1, funderUri2);
        when(passClientMock.createResource(any(User.class))).thenReturn(userUri1, useruri2);

        // when(directoryServiceUtilMock.getHopkinsIdForEmployeeId("0000222")).thenReturn("A1A1A1");
        // when(directoryServiceUtilMock.getHopkinsIdForEmployeeId("0000333")).thenReturn("B2B2B2");
    }

    /**
     * Test static timestamp utility method to verify it returns the later of two supplied timestamps
     */
    @Test
    public void testReturnLatestUpdate() {
        String baseString = "1980-01-01 00:00:00.0";
        String earlyDate = "2018-01-02 03:04:05.0";
        String laterDate = "2018-01-02 04:08:09.0";

        String latestDate = DefaultPassUpdater.returnLaterUpdate(baseString, earlyDate);
        assertEquals(earlyDate, latestDate);
        latestDate = DefaultPassUpdater.returnLaterUpdate(latestDate, laterDate);
        assertEquals(laterDate, latestDate);

        Assert.assertEquals(earlyDate, DefaultPassUpdater.returnLaterUpdate(earlyDate, earlyDate));
    }

    @Test
    public void testGrantBuilding() {

        List<Map<String, String>> resultSet = new ArrayList<>();

        String awardNumber = "12345678";
        String awardStatus = "Active";
        String localKey = "8675309";
        String projectName = "Moo Project";
        String awardDate = "01/01/2000";
        String startDate = "01/01/2001";
        String endDate = "01/01/2002";
        String directFunderId = "000029282";
        String directFunderName = "JHU Department of Synergy";
        String primaryFunderId = "8675309";
        String primaryFunderName = "J. L. Gotrocks Foundation";
        String primaryFunderPolicy = "policy1";
        String directFunderPolicy = "policy2";

        Map<String, String> rowMap = new HashMap<>();
        rowMap.put(CoeusFieldNames.C_GRANT_AWARD_NUMBER, awardNumber);
        rowMap.put(CoeusFieldNames.C_GRANT_AWARD_STATUS, awardStatus);
        rowMap.put(CoeusFieldNames.C_GRANT_LOCAL_KEY, localKey);
        rowMap.put(CoeusFieldNames.C_GRANT_PROJECT_NAME, projectName);
        rowMap.put(CoeusFieldNames.C_GRANT_AWARD_DATE, awardDate);
        rowMap.put(CoeusFieldNames.C_GRANT_START_DATE, startDate);
        rowMap.put(CoeusFieldNames.C_GRANT_END_DATE, endDate);

        rowMap.put(CoeusFieldNames.C_DIRECT_FUNDER_LOCAL_KEY, directFunderId);
        rowMap.put(CoeusFieldNames.C_DIRECT_FUNDER_NAME, directFunderName);
        rowMap.put(CoeusFieldNames.C_PRIMARY_FUNDER_LOCAL_KEY, primaryFunderId);
        rowMap.put(CoeusFieldNames.C_PRIMARY_FUNDER_NAME, primaryFunderName);

        rowMap.put(CoeusFieldNames.C_USER_FIRST_NAME, "Amanda");
        rowMap.put(CoeusFieldNames.C_USER_MIDDLE_NAME, "Beatrice");
        rowMap.put(CoeusFieldNames.C_USER_LAST_NAME, "Reckondwith");
        rowMap.put(CoeusFieldNames.C_USER_EMAIL, "areckon3@jhu.edu");
        rowMap.put(CoeusFieldNames.C_USER_INSTITUTIONAL_ID, "ARECKON3");
        rowMap.put(CoeusFieldNames.C_USER_EMPLOYEE_ID, "0000333");
        rowMap.put(CoeusFieldNames.C_USER_HOPKINS_ID, "B2B2B2");

        rowMap.put(CoeusFieldNames.C_UPDATE_TIMESTAMP, "2018-01-01 00:00:00.0");
        rowMap.put(CoeusFieldNames.C_ABBREVIATED_ROLE, "P");

        resultSet.add(rowMap);

        rowMap = new HashMap<>();
        rowMap.put(CoeusFieldNames.C_GRANT_AWARD_NUMBER, awardNumber);
        rowMap.put(CoeusFieldNames.C_GRANT_AWARD_STATUS, awardStatus);
        rowMap.put(CoeusFieldNames.C_GRANT_LOCAL_KEY, localKey);
        rowMap.put(CoeusFieldNames.C_GRANT_PROJECT_NAME, projectName);
        rowMap.put(CoeusFieldNames.C_GRANT_AWARD_DATE, awardDate);
        rowMap.put(CoeusFieldNames.C_GRANT_START_DATE, startDate);
        rowMap.put(CoeusFieldNames.C_GRANT_END_DATE, endDate);

        rowMap.put(CoeusFieldNames.C_DIRECT_FUNDER_LOCAL_KEY, directFunderId);
        rowMap.put(CoeusFieldNames.C_DIRECT_FUNDER_NAME, directFunderName);
        rowMap.put(CoeusFieldNames.C_PRIMARY_FUNDER_LOCAL_KEY, primaryFunderId);
        rowMap.put(CoeusFieldNames.C_PRIMARY_FUNDER_NAME, primaryFunderName);

        rowMap.put(CoeusFieldNames.C_USER_FIRST_NAME, "Marsha");
        rowMap.put(CoeusFieldNames.C_USER_MIDDLE_NAME, null);
        rowMap.put(CoeusFieldNames.C_USER_LAST_NAME, "Lartz");
        rowMap.put(CoeusFieldNames.C_USER_EMAIL, "alartz3@jhu.edu");
        rowMap.put(CoeusFieldNames.C_USER_INSTITUTIONAL_ID, "MLARTZ5");
        rowMap.put(CoeusFieldNames.C_USER_EMPLOYEE_ID, "0000222");
        rowMap.put(CoeusFieldNames.C_USER_HOPKINS_ID, "A1A1A1");

        rowMap.put(CoeusFieldNames.C_UPDATE_TIMESTAMP, "2018-01-01 00:00:00.0");
        rowMap.put(CoeusFieldNames.C_ABBREVIATED_ROLE, "C");

        rowMap.put(CoeusFieldNames.C_DIRECT_FUNDER_POLICY, primaryFunderPolicy);
        rowMap.put(CoeusFieldNames.C_PRIMARY_FUNDER_POLICY, directFunderPolicy);

        resultSet.add(rowMap);

        JhuPassUpdater passUpdater = new JhuPassUpdater(passClientMock);
        passUpdater.updatePass(resultSet, "grant");

        Map<URI, Grant> grantMap = passUpdater.getGrantUriMap();
        assertEquals(1, grantMap.size());
        Grant grant = grantMap.get(grantUri);
        assertEquals(1, grant.getCoPis().size());
        assertEquals(2, passUpdater.getFunderMap().size());
        assertEquals(grant.getDirectFunder(), passUpdater.getFunderMap().get(directFunderId));
        assertEquals(grant.getPrimaryFunder(), passUpdater.getFunderMap().get(primaryFunderId));
        assertEquals(grant.getPi(), passUpdater.getUserMap().get("0000333"));
        assertEquals(grant.getCoPis().get(0), passUpdater.getUserMap().get("0000222"));

        assertEquals(awardNumber, grant.getAwardNumber());
        assertEquals(Grant.AwardStatus.ACTIVE, grant.getAwardStatus());
        assertEquals("johnshopkins.edu:grant:8675309", grant.getLocalKey());
        assertEquals(DateTimeUtil.createJodaDateTime(awardDate), grant.getAwardDate());
        assertEquals(DateTimeUtil.createJodaDateTime(startDate), grant.getStartDate());
        assertEquals(DateTimeUtil.createJodaDateTime(endDate), grant.getEndDate());
        assertEquals(projectName, grant.getProjectName());

        assertEquals("johnshopkins.edu:grant:8675309", grant.getLocalKey());
    }

    @Test
    public void testUserBuilding() {

        Map<String, String> rowMap = new HashMap<>();
        rowMap.put(CoeusFieldNames.C_USER_FIRST_NAME, "Marsha");
        rowMap.put(CoeusFieldNames.C_USER_MIDDLE_NAME, null);
        rowMap.put(CoeusFieldNames.C_USER_LAST_NAME, "Lartz");
        rowMap.put(CoeusFieldNames.C_USER_EMAIL, "mlartz3@jhu.edu");
        rowMap.put(CoeusFieldNames.C_USER_INSTITUTIONAL_ID, "MLARTZ5");
        rowMap.put(CoeusFieldNames.C_USER_EMPLOYEE_ID, "0000222");
        rowMap.put(CoeusFieldNames.C_USER_HOPKINS_ID, "A1A1A1");
        rowMap.put(CoeusFieldNames.C_UPDATE_TIMESTAMP, "2018-01-01 0:00:00.0");

        JhuPassUpdater passUpdater = new JhuPassUpdater(passClientMock);
        User newUser = passUpdater.buildUser(rowMap);

        //unusual fields
        assertEquals("Marsha Lartz", newUser.getDisplayName());
        //test ids
        assertEquals("johnshopkins.edu:employeeid:0000222", newUser.getLocatorIds().get(0));
        assertEquals("johnshopkins.edu:hopkinsid:A1A1A1", newUser.getLocatorIds().get(1));
        assertEquals("johnshopkins.edu:jhed:mlartz5", newUser.getLocatorIds().get(2));
    }

    @Test
    public void testPrimaryFunderBuilding() {
        Map<String, String> rowMap = new HashMap<>();
        rowMap.put(CoeusFieldNames.C_PRIMARY_FUNDER_NAME, "Funder Name");
        rowMap.put(CoeusFieldNames.C_PRIMARY_FUNDER_LOCAL_KEY, "8675309");
        rowMap.put(CoeusFieldNames.C_PRIMARY_FUNDER_POLICY, "policy1");

        JhuPassUpdater passUpdater = new JhuPassUpdater(passClientMock);
        Funder newFunder = passUpdater.buildPrimaryFunder(rowMap);

        assertEquals("Funder Name", newFunder.getName());
        assertEquals("8675309", newFunder.getLocalKey());
        assertEquals("https://localhost:8080/fcrepo/rest/policy1", newFunder.getPolicy().toString());

    }

    @Test(expected = RuntimeException.class)
    public void testGrantModeCheck() {
        List<Map<String, String>> grantResultSet = new ArrayList<>();
        Map<String, String> rowMap = new HashMap<>();
        rowMap.put(CoeusFieldNames.C_GRANT_LOCAL_KEY, CoeusFieldNames.C_GRANT_LOCAL_KEY);
        grantResultSet.add(rowMap);

        PassClient passClient = PassClientFactory.getPassClient();
        JhuPassUpdater passUpdater = new JhuPassUpdater(passClient);

        passUpdater.updatePass(grantResultSet, "user");

    }

    @Test(expected = RuntimeException.class)
    public void testUserModeCheck() {
        List<Map<String, String>> userResultSet = new ArrayList<>();
        Map<String, String> rowMap = new HashMap<>();
        rowMap.put(CoeusFieldNames.C_USER_EMPLOYEE_ID, CoeusFieldNames.C_USER_EMPLOYEE_ID);
        userResultSet.add(rowMap);

        PassClient passClient = PassClientFactory.getPassClient();
        JhuPassUpdater passUpdater = new JhuPassUpdater(passClient);

        passUpdater.updatePass(userResultSet, "grant");

    }

}
