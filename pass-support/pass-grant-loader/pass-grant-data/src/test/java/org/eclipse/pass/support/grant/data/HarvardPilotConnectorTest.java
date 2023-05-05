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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class HarvardPilotConnectorTest {

    private HarvardPilotConnector connector;
    private File policyPropertiesFile = new File(
        getClass().getClassLoader().getResource("policy.properties").getFile());
    private Properties policyProperties = new Properties();
    private Properties connectionProperties = new Properties();

    @Before
    public void setup() throws Exception {

        System.setProperty("pass.fedora.baseurl", "https://localhost:8080/fcrepo/rest");

        try (InputStream resourceStream = new FileInputStream(policyPropertiesFile)) {
            policyProperties.load(resourceStream);
        }

        File dataFile = new File(getClass().getClassLoader().getResource("HarvardPASSTestData.xlsx").getFile());
        connectionProperties.setProperty(connector.HARVARD_DATA_FILE_PATH_PROPERTY, dataFile.getAbsolutePath());
        connector = new HarvardPilotConnector(connectionProperties, policyProperties);
    }

    @Test
    public void testRetrieveGrantUpdates() throws IOException {

        List<Map<String, String>> grantResultSet = connector.retrieveUpdates(null, "grant");
        assertEquals(8, grantResultSet.size());

        String[] grantIds = {"A-01",
                             "A-02",
                             "A-03",
                             "A-04",
                             "A-05",
                             "A-06",
                             "A-07",
                             "A-07",
                             "A-07"
        };
        String[] funderGrantIds = {"M00-00-0001",
                                   "M00-00-0002",
                                   "M00-00-0003",
                                   "M00-00-0003",
                                   "M00-00-0004",
                                   "M00-00-0004",
                                   "M00-00-0005",
                                   "M00-00-0005",
                                   "M00-00-0005"
        };
        String[] grantNames = {"Bovine Fellowships",
                               "Urban Cow Studies",
                               "Full Lectureship in Cow-speak",
                               "Full Lectureship in Cow-speak",
                               "Moo Said the Cow",
                               "Moo Said the Cow",
                               "Medical Research",
                               "Medical Research",
                               "Medical Research"
        };
        String[] firstNames = {"William",
                               "George",
                               "Daniel",
                               "Allan",
                               "Elizabeth",
                               "Albert",
                               "Carol",
                               "David",
                               "Bessie"
        };
        String[] lastNames = {"Drumstick",
                              "Flanksteak",
                              "Brisket",
                              "Bacon",
                              "Farmer",
                              "Calf",
                              "Steer",
                              "Bovine",
                              "Cow"
        };
        String[] roles = {"P",
                          "P",
                          "P",
                          "C",
                          "P",
                          "C",
                          "C",
                          "C",
                          "P"
        };
        String[] harvardIds = {"wdrumstick",
                               "gflanksteak",
                               "ddbrisket",
                               "abacon",
                               "efarmer",
                               "",
                               "csteer",
                               "dbovine",
                               "bcow"
        };
   /*     String[] harvardIds = {"wdrumstick@harvard.edu",
                "gflanksteak@harvard.edu",
                "ddbrisket@harvard.edu",
                "abacon@harvard.edu",
                "efarmer@harvard.edu",
                "",
                "csteer@harvard.edu",
                "dbovine@harvard.edu",
                "bcow@harvard.edu"
        }; */
        String[] emails = {"wdrumstick@harvard.edu",
                           "gflanksteak@harvard.edu",
                           "ddbrisket@harvard.edu",
                           "abacon@harvard.edu",
                           "efarmer@harvard.edu",
                           "",
                           "csteer@harvard.edu",
                           "dbovine@harvard.edu",
                           "bcow@harvard.edu"
        };
        String[] funderIds = {"1",
                              "2",
                              "3",
                              "3",
                              "4",
                              "4",
                              "5",
                              "5",
                              "5"
        };
        String[] grantStartDates = {"09/01/1984",
                                    "09/01/2000",
                                    "09/01/2001",
                                    "07/01/2003",
                                    "12/20/2004",
                                    "01/01/2005",
                                    "07/01/2006",
                                    "11/01/2006",
                                    "06/01/2007"
        };
        String[] grantEndDates = {"08/31/2019",
                                  "01/31/2020",
                                  "08/31/2019",
                                  "06/30/2023",
                                  "06/30/2020",
                                  "12/31/2019",
                                  "06/30/2030",
                                  "12/31/2019",
                                  "06/30/2020"
        };

        for (int i = 0; i < grantResultSet.size(); i++) {
            int j = i > 4 ? i + 1 : i; // we skip over 6th row in the data because there is no employee id
            assertEquals(grantIds[j], grantResultSet.get(i).get(CoeusFieldNames.C_GRANT_LOCAL_KEY));
            assertEquals(funderGrantIds[j], grantResultSet.get(i).get(CoeusFieldNames.C_GRANT_AWARD_NUMBER));
            assertEquals(grantNames[j], grantResultSet.get(i).get(CoeusFieldNames.C_GRANT_PROJECT_NAME));
            assertEquals(firstNames[j], grantResultSet.get(i).get(CoeusFieldNames.C_USER_FIRST_NAME));
            assertEquals(lastNames[j], grantResultSet.get(i).get(CoeusFieldNames.C_USER_LAST_NAME));
            assertEquals(roles[j], grantResultSet.get(i).get(CoeusFieldNames.C_ABBREVIATED_ROLE));
            assertEquals(harvardIds[j], grantResultSet.get(i).get(CoeusFieldNames.C_USER_EMPLOYEE_ID));
            assertEquals(emails[j], grantResultSet.get(i).get(CoeusFieldNames.C_USER_EMAIL));
            assertEquals(funderIds[j], grantResultSet.get(i).get(CoeusFieldNames.C_DIRECT_FUNDER_LOCAL_KEY));
            assertEquals(funderIds[j], grantResultSet.get(i).get(CoeusFieldNames.C_PRIMARY_FUNDER_LOCAL_KEY));
            assertEquals(grantStartDates[j], grantResultSet.get(i).get(CoeusFieldNames.C_GRANT_START_DATE));
            assertEquals(grantEndDates[j], grantResultSet.get(i).get(CoeusFieldNames.C_GRANT_END_DATE));
        }

    }

    @Test
    public void testRetrieveFunderUpdates() throws IOException {

        List<Map<String, String>> funderResultSet = connector.retrieveUpdates(null, "funder");
        assertEquals(5, funderResultSet.size());

        String[] funderIds = {"1", "2", "3", "4", "5"};
        String[] funderNames = {"Rockegal Foundation",
                                "LSMFT",
                                "National Science Foundation",
                                "Department of Housing and Urban Development",
                                "Godot Foundation"
        };

        for (int i = 0; i < funderResultSet.size(); i++) {
            assertEquals(funderIds[i], funderResultSet.get(i).get(CoeusFieldNames.C_PRIMARY_FUNDER_LOCAL_KEY));
            assertEquals(funderNames[i], funderResultSet.get(i).get(CoeusFieldNames.C_PRIMARY_FUNDER_NAME));
        }
    }

}
