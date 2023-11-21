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

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * A utility class to provide real-life json responses
 *
 * @author jrm
 */
public class JsonTestObjects {

    private JsonTestObjects() {
        //empty constructor
    }

    //a real-life JSON metadata response for a DOI, from Crossref
    private static final String xrefJson = "{\"status\":\"ok\",\"message-type\":\"work\"," +
                                    "\"message-version\":\"1.0.0\",\"message\":" +
                                    "{\"indexed\":{\"date-parts\":[[2018,9,11]]," +
                                    "\"date-time\":\"2018-09-11T22:02:39Z\"," +
                                    "\"timestamp\":" +
                                    "1536703359538},\"reference-count\":74,\"publisher\":\"SAGE Publications\"," +
                                    "\"license\":[{\"URL\":" +
                                    "\"http:\\/\\/journals.sagepub.com\\/page\\/policies\\/text-and-data-mining" +
                                    "-license\"," +
                                    "\"start\":" +
                                    "{\"date-parts\":[[2016,1,1]],\"date-time\":\"2016-01-01T00:00:00Z\"," +
                                    "\"timestamp\":" +
                                    "1451606400000},\"delay-in-days\":0,\"content-version\":\"tdm\"}]," +
                                    "\"content-domain\":{\"domain\":" +
                                    "[\"journals.sagepub.com\"],\"crossmark-restriction\":true}," +
                                    "\"short-container-title\":" +
                                    "[\"Clinical Medicine Insights: Cardiology\"]," +
                                    "\"published-print\":{\"date-parts\":" +
                                    "[[2016,1]]},\"DOI\":\"10.4137\\/cmc.s38446\",\"type\":\"journal-article\"," +
                                    "\"created\":" +
                                    "{\"date-parts\":[[2016,10,19]],\"date-time\":\"2016-10-19T21:18:54Z\"," +
                                    "\"timestamp\":" +
                                    "1476911934000},\"page\":\"CMC.S38446\",\"update-policy\":" +
                                    "\"http:\\/\\/dx.doi.org\\/10.1177\\/sage-journals-update-policy\",\"source\":" +
                                    "\"Crossref\",\"is-referenced-by-count\":1,\"title\":" +
                                    "[\"Arrhythmogenic Right Ventricular Dysplasia in Neuromuscular Disorders\"]," +
                                    "\"prefix\":" +
                                    "\"10.4137\",\"volume\":\"10\",\"author\":[{\"given\":\"Josef\"," +
                                    "\"family\":\"Finsterer\",\"sequence\":" +
                                    "\"first\",\"affiliation\":[{\"name\":\"Krankenanstalt Rudolfstiftung, Vienna, " +
                                    "Austria" +
                                    ".\"}]},{\"given\":" +
                                    "\"Claudia\",\"family\":\"St\\u00f6llberger\",\"sequence\":\"additional\"," +
                                    "\"affiliation\":[{\"name\":" +
                                    "\"Krankenanstalt Rudolfstiftung, Vienna, Austria.\"}]}],\"member\":\"179\"," +
                                    "\"published-online\":" +
                                    "{\"date-parts\":[[2016,10,19]]},\"container-title\":[\"Clinical Medicine " +
                                    "Insights: " +
                                    "Cardiology\"],\"original-title\":" +
                                    "[],\"language\":\"en\",\"link\":[{\"URL\":\"http:\\/\\/journals.sagepub" +
                                    ".com\\/doi\\/pdf\\/10.4137\\/CMC.S38446\",\"content-type\":" +
                                    "\"application\\/pdf\",\"content-version\":\"vor\"," +
                                    "\"intended-application\":\"text-mining\"},{\"URL\":" +
                                    "\"http:\\/\\/journals.sagepub.com\\/doi\\/full-xml\\/10.4137\\/CMC.S38446\"," +
                                    "\"content-type\":\"application\\/xml\",\"content-version\":" +
                                    "\"vor\",\"intended-application\":\"text-mining\"},{\"URL\":" +
                                    "\"http:\\/\\/journals.sagepub.com\\/doi\\/pdf\\/10.4137\\/CMC.S38446\"," +
                                    "\"content-type\":\"unspecified\",\"content-version\":" +
                                    "\"vor\",\"intended-application\":\"similarity-checking\"}]," +
                                    "\"deposited\":{\"date-parts\":[[2017,12,13]],\"date-time\":" +
                                    "\"2017-12-13T00:51:44Z\",\"timestamp\":1513126304000},\"score\":1.0," +
                                    "\"subtitle\":[]," +
                                    "\"short-title\":[],\"issued\":" +
                                    "{\"date-parts\":[[2016,1]]},\"references-count\":74,\"alternative-id\":[\"10" +
                                    ".4137\\/CMC.S38446\"],\"URL\":" +
                                    "\"http:\\/\\/dx.doi.org\\/10.4137\\/cmc.s38446\",\"relation\":{}," +
                                    "\"ISSN\":[\"1179-5468\",\"1179-5468\"],\"issn-type\":[{\"value\":" +
                                    "\"1179-5468\",\"type\":\"print\"},{\"value\":\"1179-5468\"," +
                                    "\"type\":\"electronic\"}]}}";

    //a real life JSON metadata response for a DOI, from unpaywall
    private static final String unpaywallJson =
        "{\"doi\":\"10.4137/cmc.s38446\"," +
        "\"doi_url\":\"https://doi.org/10.4137/cmc.s38446\"," +
        "\"title\":\"Arrhythmogenic Right Ventricular Dysplasia in Neuromuscular Disorders\"," +
        "\"genre\":\"journal-article\",\"is_paratext\":false," +
        "\"published_date\":\"2016-01-01\",\"year\":2016," +
        "\"journal_name\":\"Clinical Medicine Insights: Cardiology\",\"journal_issns\":\"1179-5468,1179-5468\"," +
        "\"journal_issn_l\":\"1179-5468\",\"journal_is_oa\":true,\"journal_is_in_doaj\":true," +
        "\"publisher\":\"SAGE Publications\",\"is_oa\":true,\"oa_status\":\"gold\",\"has_repository_copy\":true," +
        "\"best_oa_location\":{\"updated\":\"2022-12-14T21:03:17.169317\"," +
        "\"url\":\"https:\\/\\/journals.sagepub.com/doi/pdf/10.4137/CMC.S38446\"," +
        "\"url_for_pdf\":\"https:\\/\\/journals.sagepub.com\\/doi\\/pdf\\/10.4137\\/CMC.S38446\"," +
        "\"url_for_landing_page\":\"https:\\/\\/doi.org\\/10.4137\\/cmc.s38446\"," +
        "\"evidence\":\"oa journal (via doaj)\",\"license\":\"cc-by-nc\",\"version\":\"publishedVersion\"," +
        "\"host_type\":\"publisher\",\"is_best\":true,\"pmh_id\":null,\"endpoint_id\":null," +
        "\"repository_institution\":null,\"oa_date\":\"2016-01-01\"}," +
        "\"first_oa_location\":{\"updated\":\"2022-12-14T21:03:17.169317\"," +
        "\"url\":\"https:\\/\\/journals.sagepub.com\\/doi\\/pdf\\/10.4137\\/CMC.S38446\"," +
        "\"url_for_pdf\":\"https:\\/\\/journals.sagepub.com\\/doi\\/pdf\\/10.4137\\/CMC.S38446\"," +
        "\"url_for_landing_page\":\"https:\\/\\/doi.org\\/10.4137\\/cmc.s38446\"," +
        "\"evidence\":\"oa journal (via doaj)\",\"license\":\"cc-by-nc\",\"version\":\"publishedVersion\"," +
        "\"host_type\":\"publisher\",\"is_best\":true,\"pmh_id\":null,\"endpoint_id\":null," +
        "\"repository_institution\":null,\"oa_date\":\"2016-01-01\"}," +
        "\"oa_locations\":[{\"updated\":\"2022-12-14T21:03:17.169317\"," +
        "\"url\":\"https:\\/\\/journals.sagepub.com\\/doi\\/pdf\\/10.4137\\/CMC.S38446\"," +
        "\"url_for_pdf\":\"https:\\/\\/journals.sagepub.com\\/doi\\/pdf\\/10.4137\\/CMC.S38446\"," +
        "\"url_for_landing_page\":\"https:\\/\\/doi.org\\/10.4137\\/cmc.s38446\"," +
        "\"evidence\":\"oa journal (via doaj)\",\"license\":\"cc-by-nc\",\"version\":\"publishedVersion\"," +
        "\"host_type\":\"publisher\",\"is_best\":true,\"pmh_id\":null,\"endpoint_id\":null," +
        "\"repository_institution\":null,\"oa_date\":\"2016-01-01\"}," +
        "{\"updated\":\"2022-06-10T11:46:53.484862\"," +
        "\"url\":\"https:\\/\\/europepmc.org\\/articles\\/pmc5072460?pdf=render\","  +
        "\"url_for_pdf\":\"https:\\/\\/europepmc.org\\/articles\\/pmc5072460?pdf=render\"," +
        "\"url_for_landing_page\":\"https:\\/\\/europepmc.org\\/articles\\/pmc5072460\"," +
        "\"evidence\":\"oa repository (via OAI-PMH doi match)\",\"license\":\"implied-oa\"," +
        "\"version\":\"publishedVersion\"," +
        "\"host_type\":\"repository\",\"is_best\":false,\"pmh_id\":\"oai:europepmc.org:o4XNeKpNbeRdWobq6BX7\"," +
        "\"endpoint_id\":\"b5e840539009389b1a6\",\"repository_institution\":\"PubMed Central - Europe PMC\"," +
        "\"oa_date\":null},{\"updated\":\"2022-12-14T21:03:17.169410\"," +
        "\"url\":\"https:\\/\\/www.ncbi.nlm.nih.gov\\/pmc\\/articles\\/PMC5072460\",\"url_for_pdf\":null," +
        "\"url_for_landing_page\":\"https:\\/\\/www.ncbi.nlm.nih.gov\\/pmc\\/articles\\/PMC5072460\"," +
        "\"evidence\":\"oa repository (via pmcid lookup)\",\"license\":null,\"version\":\"publishedVersion\"," +
        "\"host_type\":\"repository\",\"is_best\":false,\"pmh_id\":null,\"endpoint_id\":null," +
        "\"repository_institution\":null,\"oa_date\":null}],\"oa_locations_embargoed\":[]," +
        "\"updated\":\"2021-11-28T21:57:53.965749\",\"data_standard\":2,\"z_authors\":[{\"given\":\"Josef\"," +
        "\"family\":\"Finsterer\",\"sequence\":\"first\",\"affiliation\":" +
        "[{\"name\":\"Krankenanstalt Rudolfstiftung, Vienna, Austria.\"}]}," +
        "{\"given\":\"Claudia\",\"family\":\"Stöllberger\",\"sequence\":\"additional\"," +
        "\"affiliation\":[{\"name\":\"Krankenanstalt Rudolfstiftung, Vienna, Austria.\"}]}]}";

    static JsonObject xrefTestJsonObject() {
        JsonReader reader = Json.createReader(new StringReader(xrefJson));
        JsonObject object = reader.readObject();
        reader.close();
        return object;
    }

    static JsonObject unpaywallTestJsonObject() {
        JsonReader reader = Json.createReader(new StringReader(unpaywallJson));
        JsonObject object = reader.readObject();
        reader.close();
        return object;
    }
}
