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

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * This interface defines methods for connecting to a grant datasource for us with PASS
 */
public interface GrantConnector {

    /**
     * If the grant data source is a database, we will need a query string
     *
     * @param startDate - the date of the earlieat record we wish to get on this pull
     * @param mode      - indicates whether the data pull is for grants, or users
     * @return the query string
     */
    String buildQueryString(String startDate, String awardEndDate, String mode, String grant);

    /**
     * This method retrieves the data from a data source. The format is a List of Maps - one List element for each
     * grant or user record.
     *
     * @param queryString - a query string, if required
     * @param mode        - indicates whether the data pull is for grants, or users
     * @return the query string
     * @throws ClassNotFoundException if the driver is not found
     * @throws SQLException           if there is an SQL exception
     * @throws IOException            if there is an IO exception
     */
    List<Map<String, String>> retrieveUpdates(String queryString, String mode) throws
        ClassNotFoundException, SQLException, IOException;

}
