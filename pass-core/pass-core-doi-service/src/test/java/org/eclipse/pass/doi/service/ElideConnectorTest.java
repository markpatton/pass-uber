package org.eclipse.pass.doi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.yahoo.elide.RefreshableElide;
import org.eclipse.pass.object.model.Journal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Unit tests for the elide connector
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ElideConnectorTest {

    @Autowired
    RefreshableElide refreshableElide;

    private final ElideConnector underTest = new ElideConnector(refreshableElide);

    @Test
    public void buildXrefJournal() {
        //a real-life JSON metadata response for a DOI, from Crossref
        final String xrefJson = "{\"status\":\"ok\",\"message-type\":\"work\",\"message-version\":\"1.0.0\"," +
                                "\"message\":" +
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

        JsonReader reader = Json.createReader(new StringReader(xrefJson));
        JsonObject object = reader.readObject();
        reader.close();
        Journal journal = underTest.buildPassJournal(object);
        assertEquals("Clinical Medicine Insights: Cardiology", journal.getJournalName());
        assertEquals(2, journal.getIssns().size());
        assertTrue(journal.getIssns().contains("Print:1179-5468"));
        assertTrue(journal.getIssns().contains("Online:1179-5468"));
    }
}

