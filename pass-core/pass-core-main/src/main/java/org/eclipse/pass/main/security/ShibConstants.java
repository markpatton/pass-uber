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
package org.eclipse.pass.main.security;

/**
 * Various constants associated with Shib authentication
 */
public final class ShibConstants {

    private ShibConstants() {}

    /** Display name http header */
    public static final String DISPLAY_NAME_HEADER = "Displayname";

    /** Email http header */
    public static final String EMAIL_HEADER = "Mail";

    /** EPPN http header */
    public static final String EPPN_HEADER = "Eppn";

    /** Given name. */
    public static final String GIVENNAME_HEADER = "Givenname";

    /** Surname */
    public static final String SN_HEADER = "Sn";

    /** Scoped affiliation http header */
    public static final String SCOPED_AFFILIATION_HEADER = "Affiliation";

    /** Employee number http header */
    public static final String EMPLOYEE_ID_HEADER = "Employeenumber";

    /** Unique ID header */
    public static final String UNIQUE_ID_HEADER = "unique-id";

    /** Employee ID Identifier type */
    public static final String EMPLOYEE_ID_TYPE = "employeeid";

    /** hopkins id identifier type */
    public static final String UNIQUE_ID_TYPE = "unique-id";

    /** JHED id type */
    public static final String JHED_ID_TYPE = "eppn";
}
