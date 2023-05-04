package org.eclipse.pass.main;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import com.yahoo.elide.RefreshableElide;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.eclipse.pass.main.security.ShibConstants;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.model.User;
import org.eclipse.pass.object.model.UserRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provide a Shib user to make requests.
 */
public class ShibIntegrationTest extends IntegrationTest {
    protected static String SUBMITTER_NAME = "Sally M. Submitter";
    protected static String SUBMITTER_SUR_NAME = "Submitter";
    protected static String SUBMITTER_GIVEN_NAME = "Sally";
    protected static String SUBMITTER_EMAIL = "sally232@jhu.edu";
    protected static String SUBMITTER_SCOPED_AFFILIATION = "FACULTY@johnshopkins.edu";
    protected static String[] SUBMITTER_AFFILIATIONS = { "johnshopkins.edu", "FACULTY@johnshopkins.edu" };


    protected static OkHttpClient client;
    protected static User submitter;
    protected static String submitter_key;

    @Autowired
    protected RefreshableElide refreshableElide;

    @BeforeAll
    void setupTests() {
        client = new OkHttpClient.Builder().followRedirects(false).build();
        submitter = new User();

        // Unique fields are parameterized by a random key to ensure a new object is created
        submitter_key = UUID.randomUUID().toString();

        submitter.setDisplayName(SUBMITTER_NAME);
        submitter.setEmail(SUBMITTER_EMAIL);
        submitter.setUsername(getSubmitterEppn());
        submitter.setFirstName(SUBMITTER_GIVEN_NAME);
        submitter.setLastName(SUBMITTER_SUR_NAME);
        submitter.getAffiliation().addAll(Arrays.asList(SUBMITTER_AFFILIATIONS));
        submitter.setLocatorIds(Arrays.asList(getSubmitterLocatorIds()));
        submitter.getRoles().add(UserRole.SUBMITTER);
    }

    @BeforeEach
    void persistSubmitter() throws IOException {
        // Create the submitter once
        if (submitter.getId() == null) {
            try (PassClient pass_client = PassClient.newInstance(refreshableElide)) {
                pass_client.createObject(submitter);
            }
        }
    }

    protected static String getSubmitterEppn() {
        return "sallysubmitter" + submitter_key + "@johnshopkins.edu";
    }

    protected static String getSubmitterUniqueId() {
        return "sms" + submitter_key + "@johnshopkins.edu";
    }

    protected static String getSubmitterEmployeeId() {
        return submitter_key;
    }

    protected static String[] getSubmitterLocatorIds() {
        return new String[] {
            "johnshopkins.edu:unique-id:sms" + submitter_key,
            "johnshopkins.edu:eppn:sallysubmitter" + submitter_key,
            "johnshopkins.edu:employeeid:" + submitter_key
        };
    }

    protected void setShibHeaders(Request.Builder builder) {
        builder.addHeader(ShibConstants.SCOPED_AFFILIATION_HEADER, SUBMITTER_SCOPED_AFFILIATION);
        builder.addHeader(ShibConstants.SN_HEADER, SUBMITTER_SUR_NAME);
        builder.addHeader(ShibConstants.GIVENNAME_HEADER, SUBMITTER_GIVEN_NAME);
        builder.addHeader(ShibConstants.UNIQUE_ID_HEADER, getSubmitterUniqueId());
        builder.addHeader(ShibConstants.EMAIL_HEADER, SUBMITTER_EMAIL);
        builder.addHeader(ShibConstants.EMPLOYEE_ID_HEADER, getSubmitterEmployeeId());
        builder.addHeader(ShibConstants.DISPLAY_NAME_HEADER, SUBMITTER_NAME);
        builder.addHeader(ShibConstants.EPPN_HEADER, getSubmitterEppn());
    }
}
