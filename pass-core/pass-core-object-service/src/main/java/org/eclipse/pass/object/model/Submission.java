/*
 * Copyright 2018 Johns Hopkins University
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
package org.eclipse.pass.object.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;

/**
 * Submission model. Contains details of work being submitted, where it is being deposited to, related Grants etc.
 *
 * @author Karen Hanson
 */

@Include
@Entity
@Table(name = "pass_submission")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Stringified JSON representation of metadata captured by the relevant repository forms
     */
    @Column(columnDefinition = "text")
    private String metadata;

    /**
     * Source of Submission record
     */
    @Convert(converter = SourceToStringConverter.class)
    private Source source;

    /**
     * When true, this value signals that the Submission will no longer be edited by the User.
     * It indicates to Deposit services that it can generate Deposits for any Repositories that need one.
     */
    private Boolean submitted;

    /**
     * Date the record was submitted by the User through PASS
     */
    private ZonedDateTime submittedDate;

    /**
     * Status of Submission. Focused on informing User of current state of Submission.
     */
    @Convert(converter = SubmissionStatusToStringConverter.class)
    private SubmissionStatus submissionStatus;

    /**
     * Overall status of Submission's Deposits
     */
    @Convert(converter = AggregatedDepositStatusToStringConverter.class)
    private AggregatedDepositStatus aggregatedDepositStatus;

    /**
     * URI of Publication associated with the Submission
     */
    @ManyToOne
    private Publication publication;

    /**
     * List of repositories that the submission will be deposited to
     * Note that the order of the list does not carry any particular significance
     */
    @ManyToMany
    private List<Repository> repositories = new ArrayList<>();

    /**
     * URI of the User (`User.id`) responsible for managing and submitting the Submission.
     */
    @ManyToOne
    private User submitter;

    /**
     * Name of the submitter. Used with submitterEmail as a temporary store for user information
     * in the absence of a User record
     */
    private String submitterName;

    /**
     * Email of the submitter as URI e.g. "mailto:j.smith@example.com". Used with submitterName
     * as a temporary store of user information in the absence of a User record
     */
    private String submitterEmail;

    /**
     * URI of the User(s) who prepared, or who could contribute to the preparation of, the Submission.
     * Prepares can edit the content of the Submission (describe the Publication, add Grants, add Files,
     * select Repositories) but cannot approve any Repository agreements or submit the Publication.
     * Note that the order of the list does not carry any particular significance
     */
    @ManyToMany
    private List<User> preparers = new ArrayList<>();

    /**
     * List of URIs for grants associated with the submission
     * Note that the order of the list does not carry any particular significance
     */
    @ManyToMany
    private List<Grant> grants = new ArrayList<>();

    /**
     * List of the Policy resources being satisfied upon submission
     */
    @ManyToMany
    private List<Policy> effectivePolicies = new ArrayList<>();

    /**
     * The possible values for the Submission.submissionStatus field.
     * Note that not all Submissions will go through every status.
     */
    public enum SubmissionStatus {
        /**
         * When the PASS system identifies a need for a User to submit a Publication to a particular Repository,
         * it will create a new Submission record with this status in order to prompt the User to provide the
         * document and complete the Submission.
         */
        MANUSCRIPT_REQUIRED("manuscript-required", false),

        /**
         * A Submission was prepared by a preparer but now needs the submitter to approve and submit it or provide
         * feedback.
         */
        APPROVAL_REQUESTED("approval-requested", false),

        /**
         * A Submission was prepared by a preparer, but on review by the submitter, a change was requested.
         * The Submission has been handed back to the preparer for editing.
         */
        CHANGES_REQUESTED("changes-requested", false),

        /**
         * A Submission was prepared and then cancelled by the submitter or preparer without being submitted.
         * No further edits can be made to the Submission.
         */
        CANCELLED("cancelled", false),

        /**
         * The submit button has been pressed through the UI. From this status forward, the Submission
         * becomes read-only to both the submitter and preparers. This status indicates that either
         * (a) the Submission is still being processed, or (b) PASS has finished the Deposit process,
         * but there is not yet confirmation from the Repository that indicates the Submission was valid.
         * Some Submissions may remain in a submitted state indefinitely depending on PASS's capacity to
         * verify completion of the process in the target Repository.
         */
        SUBMITTED("submitted", true),

        /**
         * Indicates that a User action may be required outside of PASS. The Submission is stalled or
         * has been rejected by one or more Repository
         */
        NEEDS_ATTENTION("needs-attention", true),

        /**
         * The target repositories have all received a copy of the Submission, and have indicated that
         * the Submission was successful.
         */
        COMPLETE("complete", true),

        /**
         * Submissions newly created by the UI will have this status.  Submissions with this status have not yet
         * been submitted.
         */
        DRAFT("draft", false);

        private static final Map<String, SubmissionStatus> map = new HashMap<>(values().length, 1);

        static {
            for (SubmissionStatus s : values()) {
                map.put(s.value, s);
            }
        }

        private String value;

        private boolean submitted;

        private SubmissionStatus(String value, boolean submitted) {
            this.value = value;
            this.submitted = submitted;
        }

        /**
         * Parse the submission status.
         *
         * @param status Serialized submission status string
         * @return The submission status
         */
        public static SubmissionStatus of(String status) {
            SubmissionStatus result = map.get(status);
            if (result == null) {
                throw new IllegalArgumentException("Invalid Submission Status: " + status);
            }
            return result;
        }

        public String getValue() {
            return value;
        }

        /**
         * Determine if submitted.
         *
         * @return True if submitted.
         */
        public boolean isSubmitted() {
            return submitted;
        }
    }

    public static class SubmissionStatusToStringConverter implements AttributeConverter<SubmissionStatus, String> {
        @Override
        public String convertToDatabaseColumn(SubmissionStatus attribute) {
            return attribute == null ? null : attribute.value;
        }

        @Override
        public SubmissionStatus convertToEntityAttribute(String dbData) {
            return dbData == null ? null : SubmissionStatus.of(dbData);
        }
    }

    /**
     * Possible aggregatedDepositStatus of a submission, this is dependent on information from the server and
     * is calculated using the status of associated Deposits
     */
    public enum AggregatedDepositStatus {
        /**
         * No Deposits have been initiated for the Submission
         */
        NOT_STARTED("not-started"),

        /**
         * One or more Deposits for the Submission have been initiated, and at least one
         * has not reached the status of "accepted"
         */
        IN_PROGRESS("in-progress"),

        /**
         * One or more Deposits for the Submission has a status of "failed"
         */
        FAILED("failed"),

        /**
         * All related Deposits have a status of "accepted"
         */
        ACCEPTED("accepted"),

        /**
         * One or more Deposits for the Submission has a status of "rejected"
         */
        REJECTED("rejected");

        private static final Map<String, AggregatedDepositStatus> map = new HashMap<>(values().length, 1);

        static {
            for (AggregatedDepositStatus s : values()) {
                map.put(s.value, s);
            }
        }

        private String value;

        private AggregatedDepositStatus(String value) {
            this.value = value;
        }

        /**
         * Parse the aggregated deposit status.
         *
         * @param status Serialized status
         * @return parsed deposit status.
         */
        public static AggregatedDepositStatus of(String status) {
            AggregatedDepositStatus result = map.get(status);
            if (result == null) {
                throw new IllegalArgumentException("Invalid Aggregated Deposit Status: " + status);
            }
            return result;
        }

        public String getValue() {
            return value;
        }
    }

    public static class AggregatedDepositStatusToStringConverter implements
        AttributeConverter<AggregatedDepositStatus, String> {
        @Override
        public String convertToDatabaseColumn(AggregatedDepositStatus attribute) {
            return attribute == null ? null : attribute.value;
        }

        @Override
        public AggregatedDepositStatus convertToEntityAttribute(String dbData) {
            return dbData == null ? null : AggregatedDepositStatus.of(dbData);
        }
    }

    /**
     * Source of the Submission, from a PASS user or imported from another source
     */
    public enum Source {

        /**
         * PASS source
         */
        PASS("pass"),

        /**
         * Other source
         */
        OTHER("other");

        private String value;

        private Source(String value) {
            this.value = value;
        }

        /**
         * Parse performer role
         *
         * @param s status string
         * @return parsed source
         */
        public static Source of(String s) {
            for (Source o: Source.values()) {
                if (o.value.equals(s)) {
                    return o;
                }
            }

            throw new IllegalArgumentException("Invalid performer role: " + s);
        }

        public String getValue() {
            return value;
        }
    }

    public static class SourceToStringConverter implements AttributeConverter<Source, String> {
        @Override
        public String convertToDatabaseColumn(Source attribute) {
            return attribute == null ? null : attribute.value;
        }

        @Override
        public Source convertToEntityAttribute(String dbData) {
            return dbData == null ? null : Source.of(dbData);
        }
    }
}
