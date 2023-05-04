/*
 * Copyright 2020 Johns Hopkins University
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
package integration;

import static java.lang.Thread.sleep;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.dataconservancy.pass.client.PassClient;
import org.dataconservancy.pass.client.PassClientFactory;
import org.dataconservancy.pass.grant.data.BasicPassEntityUtil;
import org.dataconservancy.pass.grant.data.BasicPassUpdater;
import org.dataconservancy.pass.grant.data.CoeusPassEntityUtil;
import org.dataconservancy.pass.grant.data.PassUpdateStatistics;
import org.dataconservancy.pass.grant.data.PassUpdater;
import org.dataconservancy.pass.model.Funder;
import org.dataconservancy.pass.model.Grant;
import org.dataconservancy.pass.model.Policy;
import org.dataconservancy.pass.model.User;
import org.dataconservancy.pass.model.support.Identifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * An integration test class for the BasicPassUpdater.
 */
@RunWith(MockitoJUnitRunner.class)
public class BasicPassUpdaterIT {

    private final List<Map<String, String>> resultSet = new ArrayList<>();
    private static final String DOMAIN = "default.domain";
    private static final String employeeidPrefix = DOMAIN + ":employeeid:";
    private final CoeusPassEntityUtil passEntityUtil = new CoeusPassEntityUtil();
    private final Map<String, URI> funderPolicyUriMap = new HashMap<>();

    private String directFunderPolicyUriString1;
    private String primaryFunderPolicyUriString1;


    private final PassClient passClient = PassClientFactory.getPassClient();
    PassUpdater passUpdater = new BasicPassUpdater(new BasicPassEntityUtil(), passClient);
    PassUpdateStatistics statistics = passUpdater.getStatistics();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setup() {

        for (int i = 0; i < 10; i++) {

            String prefix = System.getProperty("pass.fedora.baseurl");
            if (!prefix.endsWith("/")) {
                prefix = prefix + "/";
            }

            Policy policy = new Policy();
            policy.setTitle("Primary Policy" + i);
            policy.setDescription("MOO");
            URI policyURI = passClient.createResource(policy);
            String primaryPolicyUriString = policyURI.toString().substring(prefix.length());
            funderPolicyUriMap.put("PrimaryFunderPolicy" + i, policyURI);

            policy = new Policy();
            policy.setTitle("Direct Policy" + i);
            policy.setDescription("MOO");
            policyURI = passClient.createResource(policy);
            String directPolicyUriString = policyURI.toString().substring(prefix.length());
            funderPolicyUriMap.put("DirectFunderPolicy" + i, policyURI);

            if (i == 1) {
                directFunderPolicyUriString1 = directPolicyUriString;
                primaryFunderPolicyUriString1 = primaryPolicyUriString;
            }

            Map<String, String> rowMap = new HashMap<>();
            rowMap.put(C_GRANT_AWARD_NUMBER, C_GRANT_AWARD_NUMBER + i);
            rowMap.put(C_GRANT_AWARD_STATUS, "Active");
            rowMap.put(C_GRANT_LOCAL_KEY, C_GRANT_LOCAL_KEY + i);
            rowMap.put(C_GRANT_PROJECT_NAME, C_GRANT_PROJECT_NAME + i);
            rowMap.put(C_GRANT_AWARD_DATE, "01/01/2000");
            rowMap.put(C_GRANT_START_DATE, "01/01/2001");
            rowMap.put(C_GRANT_END_DATE, "01/01/2002");

            rowMap.put(C_DIRECT_FUNDER_LOCAL_KEY, C_DIRECT_FUNDER_LOCAL_KEY + i);
            rowMap.put(C_DIRECT_FUNDER_NAME, C_DIRECT_FUNDER_NAME + i);
            rowMap.put(C_PRIMARY_FUNDER_LOCAL_KEY, C_PRIMARY_FUNDER_LOCAL_KEY + i);
            rowMap.put(C_PRIMARY_FUNDER_NAME, C_PRIMARY_FUNDER_NAME + i);

            rowMap.put(C_USER_FIRST_NAME, C_USER_FIRST_NAME + i);
            rowMap.put(C_USER_MIDDLE_NAME, C_USER_MIDDLE_NAME + i);
            rowMap.put(C_USER_LAST_NAME, C_USER_LAST_NAME + i);
            rowMap.put(C_USER_EMAIL, C_USER_EMAIL + i);
            rowMap.put(C_USER_EMPLOYEE_ID, C_USER_EMPLOYEE_ID + i);

            rowMap.put(C_UPDATE_TIMESTAMP, "2018-01-01 0" + i + ":00:00.0");
            rowMap.put(C_ABBREVIATED_ROLE, (i % 2 == 0 ? "P" : "C"));
            rowMap.put(C_DIRECT_FUNDER_POLICY, directPolicyUriString);
            rowMap.put(C_PRIMARY_FUNDER_POLICY, primaryPolicyUriString);

            resultSet.add(rowMap);
        }

    }

    /**
     * The behavior of PassUpdate's updateGrants() method is to compare the data coming in on the ResultSet with
     * the existing data in Pass, and create objects if Pass does not yet have them, and update them if they exist in
     * Pass but
     * there are differences in the fields for which the pull source is the authoritative source, or COEUS has a clue
     * about other fields which are null
     * on the PASS object.
     *
     * @throws InterruptedException - the exception
     */
    @Test
    public void updateGrantsIT() throws InterruptedException {

        passUpdater.updatePass(resultSet, "grant");

        assertEquals(5, statistics.getPisAdded());
        assertEquals(5, statistics.getCoPisAdded());
        assertEquals(20, statistics.getFundersCreated());
        assertEquals(0, statistics.getFundersUpdated());
        assertEquals(10, statistics.getGrantsCreated());
        assertEquals(0, statistics.getGrantsUpdated());
        assertEquals("2018-01-01 09:00:00.0", statistics.getLatestUpdateString());
        assertEquals(10, statistics.getUsersCreated());
        assertEquals(0, statistics.getUsersUpdated());

        assertEquals(10, passUpdater.getGrantUriMap().size());

        for (URI grantUri : passUpdater.getGrantUriMap().keySet()) {
            Grant grant = passUpdater.getGrantUriMap().get(grantUri);
            Grant passGrant = passUpdater.getPassClient().readResource(grantUri, Grant.class);
            assertNull(passEntityUtil.update(grant, passGrant)); //this means grants are "coeus-equal"

        }

        sleep(20000);
        //try depositing the exact same resultSet. nothing should happen in Pass
        passUpdater.updatePass(resultSet, "grant");

        assertEquals(0, statistics.getFundersCreated());
        assertEquals(0, statistics.getFundersUpdated());
        assertEquals(0, statistics.getGrantsCreated());
        assertEquals(0, statistics.getGrantsUpdated());
        assertEquals(0, statistics.getUsersCreated());
        assertEquals(0, statistics.getUsersUpdated());

        //now let's monkey with a few things; we expect to update the changed objects
        Map<String, String> rowMap = new HashMap<>();
        rowMap.put(C_GRANT_AWARD_NUMBER, C_GRANT_AWARD_NUMBER + 1);
        rowMap.put(C_GRANT_AWARD_STATUS, "Active");
        rowMap.put(C_GRANT_LOCAL_KEY, C_GRANT_LOCAL_KEY + 1);
        rowMap.put(C_GRANT_PROJECT_NAME, C_GRANT_PROJECT_NAME + 1 + "MOO");
        rowMap.put(C_GRANT_AWARD_DATE, "01/01/1999");
        rowMap.put(C_GRANT_START_DATE, "01/01/1999");
        rowMap.put(C_GRANT_END_DATE, "01/01/2002");

        rowMap.put(C_DIRECT_FUNDER_LOCAL_KEY, C_DIRECT_FUNDER_LOCAL_KEY + 1);
        rowMap.put(C_DIRECT_FUNDER_NAME, C_DIRECT_FUNDER_NAME + 1 + "MOO");
        rowMap.put(C_PRIMARY_FUNDER_LOCAL_KEY, C_PRIMARY_FUNDER_LOCAL_KEY + 1);
        rowMap.put(C_PRIMARY_FUNDER_NAME, C_PRIMARY_FUNDER_NAME + 1);

        rowMap.put(C_USER_FIRST_NAME, C_USER_FIRST_NAME + 1);
        rowMap.put(C_USER_MIDDLE_NAME, C_USER_MIDDLE_NAME + 1 + "MOOO");
        rowMap.put(C_USER_LAST_NAME, C_USER_LAST_NAME + 1 + "MOOOOO");
        rowMap.put(C_USER_EMAIL, C_USER_EMAIL + 1);
        //rowMap.put(C_USER_INSTITUTIONAL_ID, C_USER_INSTITUTIONAL_ID + 1);
        rowMap.put(C_USER_EMPLOYEE_ID, C_USER_EMPLOYEE_ID + 1);
        //rowMap.put(C_USER_HOPKINS_ID, C_USER_HOPKINS_ID + 1);

        rowMap.put(C_UPDATE_TIMESTAMP, "2018-01-01 0" + 1 + ":00:00.0");
        rowMap.put(C_ABBREVIATED_ROLE, ("C"));

        rowMap.put(C_DIRECT_FUNDER_POLICY, directFunderPolicyUriString1);
        rowMap.put(C_PRIMARY_FUNDER_POLICY, primaryFunderPolicyUriString1);

        resultSet.clear();
        resultSet.add(rowMap);

        passUpdater.updatePass(resultSet, "grant");
        assertEquals(0, statistics.getFundersCreated());
        assertEquals(1, statistics.getFundersUpdated());
        assertEquals(0, statistics.getGrantsCreated());
        assertEquals(1, statistics.getGrantsUpdated());
        assertEquals(1, statistics.getUsersUpdated());

        sleep(20000);

        for (int i = 0; i < 10; i++) {
            Grant grant = new Grant();
            grant.setAwardNumber(C_GRANT_AWARD_NUMBER + i);
            grant.setAwardStatus(Grant.AwardStatus.ACTIVE);
            String grantIdPrefix = DOMAIN + ":grant:";
            grant.setLocalKey(grantIdPrefix + C_GRANT_LOCAL_KEY + i);
            grant.setProjectName(C_GRANT_PROJECT_NAME + i);
            grant.setAwardDate(createJodaDateTime("01/01/2000"));
            grant.setStartDate(createJodaDateTime("01/01/2001"));
            grant.setEndDate(createJodaDateTime("01/01/2002"));

            URI passGrantUri = passClient.findByAttribute(Grant.class, "localKey", grant.getLocalKey());
            Grant passGrant = passClient.readResource(passGrantUri, Grant.class);

            assertEquals(grant.getAwardNumber(), passGrant.getAwardNumber());
            assertEquals(grant.getAwardStatus(), passGrant.getAwardStatus());
            assertEquals(grant.getLocalKey(), passGrant.getLocalKey());
            if (i == 1) {
                assertEquals(grant.getProjectName() + "MOO", passGrant.getProjectName());
                assertEquals(createJodaDateTime("01/01/1999"), passGrant.getStartDate());
                assertEquals(createJodaDateTime("01/01/1999"), passGrant.getStartDate());
            } else {
                assertEquals(grant.getProjectName(), passGrant.getProjectName());
                assertEquals(grant.getAwardDate(), passGrant.getAwardDate());
                assertEquals(grant.getStartDate(), passGrant.getStartDate());
            }
            assertEquals(grant.getEndDate(), passGrant.getEndDate());

            //let's check funder stuff
            Funder directFunder = new Funder();
            String funderIdPrefix = DOMAIN + ":funder:";
            directFunder.setLocalKey(funderIdPrefix + C_DIRECT_FUNDER_LOCAL_KEY + i);
            directFunder.setName(C_DIRECT_FUNDER_NAME + i);
            directFunder.setPolicy(funderPolicyUriMap.get("DirectFunderPolicy" + i));

            URI directFunderUri = passClient.findByAttribute(Funder.class, "localKey", directFunder.getLocalKey());
            Funder passDirectFunder = passClient.readResource(directFunderUri, Funder.class);
            if (i == 1) {
                assertEquals(directFunder.getName() + "MOO", passDirectFunder.getName());
                assertEquals(directFunder.getLocalKey(), passDirectFunder.getLocalKey());
                assertEquals(passDirectFunder.getId(), passGrant.getDirectFunder());
            } else {
                assertEquals(directFunder.getName(), passDirectFunder.getName());
            }

            Funder primaryFunder = new Funder();
            primaryFunder.setLocalKey(funderIdPrefix + C_PRIMARY_FUNDER_LOCAL_KEY + i);
            primaryFunder.setName(C_PRIMARY_FUNDER_NAME + i);
            primaryFunder.setPolicy(funderPolicyUriMap.get("PrimaryFunderPolicy" + i));

            URI primaryFunderUri = passClient.findByAttribute(Funder.class, "localKey", primaryFunder.getLocalKey());
            Funder passPrimaryFunder = passClient.readResource(primaryFunderUri, Funder.class);
            assertEquals(primaryFunder.getName(), passPrimaryFunder.getName());
            assertEquals(passPrimaryFunder.getId(), passGrant.getPrimaryFunder());
            assertEquals(primaryFunder.getLocalKey(), passPrimaryFunder.getLocalKey());
            assertEquals(primaryFunder.getPolicy(), passPrimaryFunder.getPolicy());

            User user = new User();

            //employeeId and localKey were localized by the grant loader
            user.getLocatorIds().add(employeeidPrefix + C_USER_EMPLOYEE_ID + i);
            user.setFirstName(C_USER_FIRST_NAME + i);
            user.setMiddleName(C_USER_MIDDLE_NAME + i);
            user.setLastName(C_USER_LAST_NAME + i);
            user.setEmail(C_USER_EMAIL + i);

            URI userUri = null;
            ListIterator<String> idIterator = user.getLocatorIds().listIterator();

            while (userUri == null && idIterator.hasNext()) {
                String id = String.valueOf(idIterator.next());
                if (id != null) {
                    userUri = passClient.findByAttribute(User.class, "locatorIds", id);
                }
            }

            User passUser = passClient.readResource(userUri, User.class);
            assertEquals(user.getFirstName(), passUser.getFirstName());
            if (i == 1) {
                assertEquals(user.getMiddleName() + "MOOO", passUser.getMiddleName());
                assertEquals(user.getLastName() + "MOOOOO", passUser.getLastName());
            } else {
                assertEquals(user.getMiddleName(), passUser.getMiddleName());
                assertEquals(user.getLastName(), passUser.getLastName());
            }

            assertEquals(user.getEmail(), passUser.getEmail());
            assertTrue(user.getLocatorIds().containsAll(passUser.getLocatorIds()));
            assertTrue(passUser.getLocatorIds().containsAll(user.getLocatorIds()));
            assertEquals(passUser.getLocatorIds().size(), user.getLocatorIds().size());

            if (i % 2 == 0) {
                assertNotNull(passGrant.getPi());
                assertEquals(0, passGrant.getCoPis().size());
            } else {
                assertNull(passGrant.getPi());
                assertEquals(1, passGrant.getCoPis().size());
            }

        }
    }

    @Test
    public void updateUsersIT() throws InterruptedException {

        User user10 = new User();
        user10.getLocatorIds().add(employeeidPrefix + C_USER_EMPLOYEE_ID + 10);
        user10.setFirstName(C_USER_FIRST_NAME + 10);
        user10.setMiddleName(C_USER_MIDDLE_NAME + 10);
        user10.setLastName(C_USER_LAST_NAME + 10);

        URI passUserURI = passUpdater.getPassClient().createResource(user10);

        User passUser = passClient.readResource(passUserURI, User.class);
        assertNull(passUser.getDisplayName());
        assertEquals(1, passUser.getLocatorIds().size());
        assertNull(passUser.getEmail());

        sleep(20000);

        List<Map<String, String>> userResultSet = new ArrayList<>();

        for (int i = 10; i < 12; i++) {
            Map<String, String> rowMap = new HashMap<>();

            rowMap.put(C_USER_FIRST_NAME, C_USER_FIRST_NAME + i);
            rowMap.put(C_USER_MIDDLE_NAME, C_USER_MIDDLE_NAME + i);
            rowMap.put(C_USER_LAST_NAME, C_USER_LAST_NAME + i);
            rowMap.put(C_USER_EMAIL, C_USER_EMAIL + i);
            rowMap.put(C_USER_EMPLOYEE_ID, C_USER_EMPLOYEE_ID + i);
            //rowMap.put(C_USER_HOPKINS_ID, C_USER_HOPKINS_ID + i);
            rowMap.put(C_UPDATE_TIMESTAMP, "2018-01-01 0" + 1 + ":00:00.0");
            userResultSet.add(rowMap);
        }

        passUpdater.updatePass(userResultSet, "user");

        //now update from the set of two users - the second one is not in PASS, but is not created
        //the first (user10) should be updated, with new fields added
        assertEquals(0, statistics.getUsersCreated());
        assertEquals(1, statistics.getUsersUpdated());

        assertNotNull(passUserURI);
        User updatedUser = passUpdater.getPassClient().readResource(passUserURI, User.class);

        assertNotNull(updatedUser.getEmail());
        assertNotNull(updatedUser.getDisplayName());
        assertNotNull(updatedUser.getLocatorIds());
        assertTrue(updatedUser.getLocatorIds().contains(employeeidPrefix + C_USER_EMPLOYEE_ID + 10));
        assertEquals(C_USER_EMAIL + 10, updatedUser.getEmail());
    }

    /**
     * Create some policies, deposit them into Fedora
     * Then create a java data object linking funders to them
     * this basically tests what happens when pulling in data from a policy properties file first,
     * or from a coeus pull second
     */
    @Test
    public void updateFundersIT() throws InterruptedException {
        Policy policy1 = new Policy();
        policy1.setTitle("Policy One");
        policy1.setDescription("Policy one Description");
        Policy policy2 = new Policy();
        policy2.setTitle("Policy Two");
        policy2.setDescription("Policy Two Description");

        URI policy1Uri = passClient.createResource(policy1);
        URI policy2Uri = passClient.createResource(policy2);

        assertNotNull(passClient.readResource(policy1Uri, Policy.class));
        assertNotNull(passClient.readResource(policy2Uri, Policy.class));

        Funder funder1 = new Funder();
        String DOMAIN = "default.domain";
        String fullLocalKey = new Identifier(DOMAIN, "funder", "22229999").serialize();
        funder1.setLocalKey(fullLocalKey);
        funder1.setName("Funder One");
        Funder funder2 = new Funder();
        fullLocalKey = new Identifier(DOMAIN, "funder", "33330000").serialize();
        funder2.setLocalKey(fullLocalKey);//use full localKey
        funder2.setName("Funder Two");

        URI funder1Uri = passClient.createResource(funder1);
        URI funder2Uri = passClient.createResource(funder2);

        assertNotNull(passClient.readResource(funder1Uri, Funder.class));
        assertNotNull(passClient.readResource(funder2Uri, Funder.class));

        String policyString1 = policy1Uri.getPath().substring("/fcrepo/rest/".length());
        assertTrue(policyString1.startsWith("policies"));
        String policyString2 = policy2Uri.getPath().substring("/fcrepo/rest/".length());
        assertTrue(policyString2.startsWith("policies"));

        List<Map<String, String>> funderResultSet = new ArrayList<>();

        Map<String, String> rowMap = new HashMap<>();
        rowMap.put(C_PRIMARY_FUNDER_LOCAL_KEY, "22229999");
        rowMap.put(C_PRIMARY_FUNDER_POLICY, policyString1);
        funderResultSet.add(rowMap);

        rowMap = new HashMap<>();
        rowMap.put(C_PRIMARY_FUNDER_LOCAL_KEY, "33330000");
        rowMap.put(C_PRIMARY_FUNDER_POLICY, policyString2);
        funderResultSet.add(rowMap);

        rowMap = new HashMap<>();
        rowMap.put(C_PRIMARY_FUNDER_LOCAL_KEY, "88888888"); // this one does not exist in pass
        rowMap.put(C_PRIMARY_FUNDER_POLICY, policyString2);
        funderResultSet.add(rowMap);

        sleep(20000); //allow indexer to index stuff - java client has to use elasticsearch

        passUpdater.updatePass(funderResultSet, "funder");
        PassUpdateStatistics statistics = passUpdater.getStatistics();

        assertNotNull(passClient.readResource(funder1Uri, Funder.class));
        assertNotNull(passClient.readResource(funder1Uri, Funder.class).getPolicy());
        assertNotNull(passClient.readResource(funder2Uri, Funder.class));
        assertNotNull(passClient.readResource(funder2Uri, Funder.class).getPolicy());
        assertEquals(policy1Uri, passClient.readResource(funder1Uri, Funder.class).getPolicy());
        assertEquals(policy2Uri, passClient.readResource(funder2Uri, Funder.class).getPolicy());

        assertEquals(0, statistics.getFundersCreated());
        assertEquals(2, statistics.getFundersUpdated());

        //coeus pulls will have the funder names, we should be able to add one we don't know about

        funderResultSet = new ArrayList<>();

        rowMap = new HashMap<>();
        rowMap.put(C_PRIMARY_FUNDER_LOCAL_KEY, "22229999");
        rowMap.put(C_PRIMARY_FUNDER_NAME, "Funder Name 1");
        rowMap.put(C_PRIMARY_FUNDER_POLICY, policyString2); //let's change policies for this one
        funderResultSet.add(rowMap);

        rowMap = new HashMap<>();
        rowMap.put(C_PRIMARY_FUNDER_LOCAL_KEY, "33330000");
        rowMap.put(C_PRIMARY_FUNDER_NAME, "Funder Name 2");
        rowMap.put(C_PRIMARY_FUNDER_POLICY, policyString2);
        funderResultSet.add(rowMap);

        rowMap = new HashMap<>();
        rowMap.put(C_PRIMARY_FUNDER_LOCAL_KEY, "88888888"); // this one does not exist in pass
        rowMap.put(C_PRIMARY_FUNDER_NAME, "Funder Name 3");
        rowMap.put(C_PRIMARY_FUNDER_POLICY, policyString2);
        funderResultSet.add(rowMap);

        sleep(20000); //allow indexer to index stuff - java client has to use elasticsearch

        passUpdater.updatePass(funderResultSet, "funder");
        statistics = passUpdater.getStatistics();

        assertNotNull(passClient.readResource(funder1Uri, Funder.class));
        assertNotNull(passClient.readResource(funder1Uri, Funder.class).getPolicy());
        assertNotNull(passClient.readResource(funder2Uri, Funder.class));
        assertNotNull(passClient.readResource(funder2Uri, Funder.class).getPolicy());
        assertEquals(policy2Uri, passClient.readResource(funder1Uri, Funder.class).getPolicy());
        assertEquals(policy2Uri, passClient.readResource(funder2Uri, Funder.class).getPolicy());

        assertEquals(1, statistics.getFundersCreated());
        assertEquals(2, statistics.getFundersUpdated());

        //DO AGAIN!! DO AGAIN!!

        sleep(20000); //allow indexer to index stuff - java client has to use elasticsearch

        passUpdater.updatePass(funderResultSet, "funder");
        statistics = passUpdater.getStatistics();

        assertEquals(0, statistics.getFundersCreated());
        assertEquals(0, statistics.getFundersUpdated());

    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        File serialized = folder.newFile("serializedData");

        try (FileOutputStream fos = new FileOutputStream(serialized);
             ObjectOutputStream out = new ObjectOutputStream(fos)
        ) {
            out.writeObject(resultSet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Map<String, String>> input = null;
        try (FileInputStream fis = new FileInputStream(serialized);
             ObjectInputStream in = new ObjectInputStream(fis)
        ) {
            input = (List<Map<String, String>>) in.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        assertEquals(resultSet, input);
    }

}