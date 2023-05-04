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

package org.dataconservancy.pass.grant.data;

public class HarvardFieldNames {
    private HarvardFieldNames() {
        //never called
    }

    public static final String H_FUNDER_ID = "Funder ID";//harvard's local key
    public static final String H_GRANT_ID = "Harvard grant ID";// harvard's local key
    public static final String H_FUNDER_GRANT_ID = "Funder grant ID";//ID assigned by funder
    public static final String H_GRANT_NAME = "Grant name";
    public static final String H_INV_FIRST_NAME = "PI First Name";
    public static final String H_INV_LAST_NAME = "PI Last Name";
    //public static final String H_INV_ID = "PI Harvard ID";//guaranteed to be in grant data
    public static final String H_INV_EMAIL = "PI Email";
    //public static final String H_INV_ROLE =
    public static final String H_GRANT_START_DATE = "Grant start date";
    public static final String H_GRANT_END_DATE = "Grant end date";

}
