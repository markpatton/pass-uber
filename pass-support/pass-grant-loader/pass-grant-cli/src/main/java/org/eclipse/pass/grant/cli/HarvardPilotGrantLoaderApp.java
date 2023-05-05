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

package org.eclipse.pass.grant.cli;

import java.util.Properties;

import org.eclipse.pass.grant.data.GrantConnector;
import org.eclipse.pass.grant.data.HarvardPilotConnector;
import org.eclipse.pass.grant.data.HarvardPilotPassUpdater;
import org.eclipse.pass.grant.data.PassUpdater;

class HarvardPilotGrantLoaderApp extends BaseGrantLoaderApp {
    HarvardPilotGrantLoaderApp(String startDate, String awardEndDate, boolean email, String mode, String action,
                               String dataFileName) {
        super(startDate, awardEndDate, email, mode, action, dataFileName, null);
        super.setTimestamp(false);
    }

    @Override
    boolean checkMode(String s) {
        return (s.equals("grant") || s.equals("funder"));
    }

    @Override
    GrantConnector configureConnector(Properties connectionProperties, Properties policyProperties) {
        return new HarvardPilotConnector(connectionProperties, policyProperties);
    }

    @Override
    PassUpdater configureUpdater() {
        return new HarvardPilotPassUpdater();
    }

}