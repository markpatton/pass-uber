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

import java.util.Collection;
import java.util.Map;

import org.eclipse.pass.support.client.model.Grant;

/**
 * An interface specifying behavior of a class that processes grant data into PASS.
 */
public interface PassUpdater {

    /**
     * Update PASS using the data in results.
     * @param results the source grant data
     * @param mode the mode of update
     */
    void updatePass(Collection<Map<String, String>> results, String mode);

    /**
     * Returns the latest update timestamp string.
     * @return the latest update timestamp string
     */
    String getLatestUpdate();

    /**
     * Returns a string contains report of update results.
     * @return the report
     */
    String getReport();

    /**
     * Returns statistics of update.
     * @return an object containing the statisitics
     */
    PassUpdateStatistics getStatistics();

    /**
     * Returns a Map of the grants that were processed.
     * @return the map of grants.
     */
    Map<String, Grant> getGrantResultMap();
}
