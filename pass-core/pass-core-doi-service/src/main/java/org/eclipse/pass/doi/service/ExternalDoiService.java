/*
 *
 * Copyright 2022 Johns Hopkins University
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.eclipse.pass.doi.service;

import static java.lang.Thread.sleep;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ExternalDoiService {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalDoiService.class);
    private Set<String> activeJobs = new HashSet<>();

    public abstract String name();

    public  abstract String baseUrl();

    public  abstract HashMap<String, String> parameterMap();

    public  abstract HashMap<String, String> headerMap();

    public  abstract JsonObject processObject(JsonObject object);

    /**
     * check to see whether supplied DOI is in valid format after splitting off a possible prefix
     *
     * @return the valid suffix, or null if invalid
     */
    String verify(String doi) {
        LOG.debug("Verifying doi format for " + doi );
        if (doi == null) {
            return null;
        }
        String criterion = "doi.org/";
        int i = doi.indexOf(criterion);
        String suffix = i >= 0 ? doi.substring(i + criterion.length()) : doi;

        Pattern pattern = Pattern.compile("^10\\.\\d{4,9}/[-._;()/:a-zA-Z0-9]+$");

        Matcher matcher = pattern.matcher(suffix);
        return matcher.matches() ? suffix : null;
    }

    /**
     * this simply protects the external service from a person hammering on a request thinking
     * it wasn't processed, when it really is just slow coming back
     *
     * @param doi the doi to check active
     * @return whether the doi lookup is still active
     */
    boolean isAlreadyActive(String doi) {
        //check cache map for existence of doi
        //put doi on map if absent
        LOG.debug("Checking to see if doi " + doi + " is already in process");
        if (activeJobs.contains(doi)) {
            return true;
        } else {
            // this DOI is not actively being processed
            // let's temporarily prohibit new requests for this DOI
            activeJobs.add(doi);
            //longest time we expect it should take to create a Journal object, in
            //milliseconds
            int cachePeriod = 30000;
            Thread t = new Thread(new ExternalDoiService.ExpiringLock(doi, cachePeriod));
            t.start();
        }
        return false;
    }

    void unlockDoi(String doi) {
        if (activeJobs.contains(doi)) {
            activeJobs.remove(doi);
        }
    }

    /**
     * A class to manage locking so that an active process for a DOI will finish executing before
     * another one begins
     */
    public class ExpiringLock implements Runnable {
        private String key;
        private int duration;

        ExpiringLock(String key, int duration) {
            this.key = key;
            this.duration = duration;
        }

        public void run() {
            try {
                sleep(duration);
                activeJobs.remove(key);
            } catch (InterruptedException e) {
                activeJobs.remove(key);
            }
        }
    }
}
