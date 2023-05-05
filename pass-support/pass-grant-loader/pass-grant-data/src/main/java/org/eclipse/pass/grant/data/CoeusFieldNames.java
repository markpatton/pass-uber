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

package org.eclipse.pass.grant.data;

/**
 * constants class containing the column names for fields we need for our COEUS data pulls
 * the names reflect the mapping to our model.
 *
 * the values come from four views in the COEUS database; and the aliases are used in the query string in
 * {@code CoeusConnector} and to refer to the columns in the ResultSet in {@code GrantUpdater}
 *
 *
 * these are consumed in the {@code CoeusConnector} class for the pull from COEUS, and in the {@code GrantUpdater} class
 * for the push into Pass
 *
 * @author jrm@jhu.edu
 */
public class CoeusFieldNames {
    private CoeusFieldNames () {
        //never called
    }

    public static final String C_GRANT_AWARD_NUMBER = "AWARD_ID";
    public static final String C_GRANT_AWARD_STATUS = "AWARD_STATUS";
    public static final String C_GRANT_LOCAL_KEY = "GRANT_NUMBER";
    public static final String C_GRANT_PROJECT_NAME = "TITLE";
    public static final String C_GRANT_AWARD_DATE = "AWARD_DATE";
    public static final String C_GRANT_START_DATE = "AWARD_START";
    public static final String C_GRANT_END_DATE = "AWARD_END";

    public static final String C_DIRECT_FUNDER_LOCAL_KEY = "SPOSNOR_CODE";// misspelling in COEUS view - if this gets
    // corrected
    //it will collide with C_PRIMARY_SPONSOR_CODE below - this field will then have to be aliased in order to
    //access it in the ResultSet
    public static final String C_DIRECT_FUNDER_NAME = "SPONSOR";
    public static final String C_PRIMARY_FUNDER_LOCAL_KEY = "SPONSOR_CODE";
    public static final String C_PRIMARY_FUNDER_NAME = "SPONSOR_NAME";

    public static final String C_USER_FIRST_NAME = "FIRST_NAME";
    public static final String C_USER_MIDDLE_NAME = "MIDDLE_NAME";
    public static final String C_USER_LAST_NAME = "LAST_NAME";
    public static final String C_USER_EMAIL = "EMAIL_ADDRESS";
    public static final String C_USER_INSTITUTIONAL_ID = "JHED_ID";
    public static final String C_USER_EMPLOYEE_ID = "EMPLOYEE_ID";
    //public static final String C_USER_AFFILIATION = "";
    //public static final String C_USER_ORCID_ID = "";

    //these fields are accessed for processing, but are not mapped to PASS objects
    public static final String C_UPDATE_TIMESTAMP = "UPDATE_TIMESTAMP";
    public static final String C_ABBREVIATED_ROLE = "ABBREVIATED_ROLE";

    //this is not a COEUS field, but is a place in our row map to put a hopkins id if it exists
    public static final String C_USER_HOPKINS_ID = "HOPKINS_ID";
    //also not a field name, but something provided in a properties file
    public static final String C_PRIMARY_FUNDER_POLICY = "PRIMARY_FUNDER_POLICY";
    public static final String C_DIRECT_FUNDER_POLICY = "DIRECT_FUNDER_POLICY";

}
