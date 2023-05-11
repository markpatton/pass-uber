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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.pass.support.client.PassClient;
import org.eclipse.pass.support.client.PassClientResult;
import org.eclipse.pass.support.client.model.AwardStatus;
import org.eclipse.pass.support.client.model.Funder;
import org.eclipse.pass.support.client.model.Grant;
import org.eclipse.pass.support.client.model.PassEntity;
import org.eclipse.pass.support.client.model.User;
import org.junit.Assert;
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
    public void testUpdatePassGrant_Success_NewGrant() throws IOException {

        List<Map<String, String>> resultSet = buildTestInputResultSet();
        preparePassClientMockCalls();

        JhuPassUpdater passUpdater = new JhuPassUpdater(passClientMock);
        passUpdater.updatePass(resultSet, "grant");

        Map<String, Grant> grantMap = passUpdater.getGrantResultMap();
        assertEquals(1, grantMap.size());
        Grant grant = grantMap.get("8675309");
        assertEquals(1, grant.getCoPis().size());
        assertEquals(2, passUpdater.getFunderMap().size());
        assertEquals(grant.getDirectFunder(), passUpdater.getFunderMap().get("000029282"));
        assertEquals(grant.getPrimaryFunder(), passUpdater.getFunderMap().get("8675309"));
        assertEquals(grant.getPi(), passUpdater.getUserMap().get("0000333"));
        assertEquals(grant.getCoPis().get(0), passUpdater.getUserMap().get("0000222"));

        assertEquals("12345678", grant.getAwardNumber());
        assertEquals(AwardStatus.ACTIVE, grant.getAwardStatus());
        assertEquals("johnshopkins.edu:grant:8675309", grant.getLocalKey());
        assertEquals(DateTimeUtil.createZonedDateTime("01/01/2000"), grant.getAwardDate());
        assertEquals(DateTimeUtil.createZonedDateTime("01/01/2001"), grant.getStartDate());
        assertEquals(DateTimeUtil.createZonedDateTime("01/01/2002"), grant.getEndDate());
        assertEquals("Moo Project", grant.getProjectName());

        assertEquals("johnshopkins.edu:grant:8675309", grant.getLocalKey());
    }

    private List<Map<String, String>> buildTestInputResultSet() {
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
        return resultSet;
    }

    private void preparePassClientMockCalls() throws IOException {
        Funder directFunder = new Funder("000029282");
        directFunder.setLocalKey("johnshopkins.edu:funder:000029282");
        PassClientResult<PassEntity> mockFunderResult1 = new PassClientResult<>(List.of(directFunder), 1);
        doReturn(mockFunderResult1)
                .when(passClientMock)
                .selectObjects(argThat(passClientSelector ->
                        passClientSelector.getFilter().equals("localKey=='johnshopkins.edu:funder:000029282'")));

        Funder primaryFunder = new Funder("8675309");
        directFunder.setLocalKey("johnshopkins.edu:funder:8675309");
        PassClientResult<PassEntity> mockFunderResult2 = new PassClientResult<>(List.of(primaryFunder), 1);
        doReturn(mockFunderResult2)
                .when(passClientMock)
                .selectObjects(
                        argThat(passClientSelector2 ->
                                passClientSelector2.getFilter().equals("localKey=='johnshopkins.edu:funder:8675309'")));

        User user1 = new User("0000333");
        PassClientResult<PassEntity> mockFunderResult3 = new PassClientResult<>(List.of(user1), 1);
        doReturn(mockFunderResult3)
                .when(passClientMock)
                .selectObjects(
                        argThat(passClientSelector3 ->
                                passClientSelector3.getFilter().equals(
                                        "locatorIds=hasmember='johnshopkins.edu:employeeid:0000333'")));

        User user2 = new User("0000222");
        PassClientResult<PassEntity> mockFunderResult4 = new PassClientResult<>(List.of(user2), 1);
        doReturn(mockFunderResult4)
                .when(passClientMock)
                .selectObjects(
                        argThat(passClientSelector4 ->
                                passClientSelector4.getFilter().equals(
                                        "locatorIds=hasmember='johnshopkins.edu:employeeid:0000222'")));

        PassClientResult<PassEntity> mockFunderResult5 = new PassClientResult<>(Collections.emptyList(), 0);
        doReturn(mockFunderResult5)
                .when(passClientMock)
                .selectObjects(
                        argThat(passClientSelector5 ->
                                passClientSelector5.getFilter().equals(
                                        "localKey=='johnshopkins.edu:grant:8675309'")));
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
        assertEquals("policy1", newFunder.getPolicy().getId());

    }

    @Test(expected = RuntimeException.class)
    public void testUpdatePassUser_Fail_ModeCheck() {
        List<Map<String, String>> grantResultSet = new ArrayList<>();
        Map<String, String> rowMap = new HashMap<>();
        rowMap.put(CoeusFieldNames.C_GRANT_LOCAL_KEY, CoeusFieldNames.C_GRANT_LOCAL_KEY);
        grantResultSet.add(rowMap);

        JhuPassUpdater passUpdater = new JhuPassUpdater(passClientMock);

        passUpdater.updatePass(grantResultSet, "user");

    }

    @Test(expected = RuntimeException.class)
    public void testUpdatePassGrant_Fail_ModeCheck() {
        List<Map<String, String>> userResultSet = new ArrayList<>();
        Map<String, String> rowMap = new HashMap<>();
        rowMap.put(CoeusFieldNames.C_USER_EMPLOYEE_ID, CoeusFieldNames.C_USER_EMPLOYEE_ID);
        userResultSet.add(rowMap);

        JhuPassUpdater passUpdater = new JhuPassUpdater(passClientMock);

        passUpdater.updatePass(userResultSet, "grant");
    }

    @Test(expected = RuntimeException.class)
    public void testUpdatePassFunder_Fail_ModeCheck() {
        List<Map<String, String>> userResultSet = new ArrayList<>();
        Map<String, String> rowMap = new HashMap<>();
        rowMap.put(CoeusFieldNames.C_USER_EMPLOYEE_ID, CoeusFieldNames.C_USER_EMPLOYEE_ID);
        userResultSet.add(rowMap);

        JhuPassUpdater passUpdater = new JhuPassUpdater(passClientMock);

        passUpdater.updatePass(userResultSet, "funder");
    }

}
