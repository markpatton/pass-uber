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

import org.eclipse.pass.support.client.PassClient;
import org.eclipse.pass.support.client.model.Grant;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

public interface PassUpdater {
    void updatePass(Collection<Map<String, String>> results, String mode);

    String getLatestUpdate();

    String getReport();

    PassUpdateStatistics getStatistics();

    Map<URI, Grant> getGrantUriMap();

    PassClient getPassClient();
}
