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
import java.util.HashMap;
import java.util.Map;
import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;

/**
 * The SubmissionEvent model captures significant events that are performed by an agent and occur against a Submission.
 *
 * @author Karen Hanson
 */

@Include
@Entity
@Table(name = "pass_submission_event")
public class SubmissionEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The type of event
     */
    @Convert(converter = EventTypeToStringConverter.class)
    private EventType eventType;

    /**
     * Date the event was performed by the User
     */

    private ZonedDateTime performedDate;

    /**
     * URI of the User responsible for performing the event
     */
    @ManyToOne
    private User performedBy;

    /**
     * Role of the person performing the event
     */
    @Convert(converter = PerformerRoleToStringConverter.class)
    private PerformerRole performerRole;

    /**
     * Associated submission.
     */
    @ManyToOne
    private Submission submission;

    /**
     * A comment relevant to the SubmissionEvent. For example, when a `changes-requested` event occurs,
     * this might be added by the User through the UI to communicate what changes should be made
     */
    private String comment;

    /**
     * A resource relevant to the SubmissionEvent. For example, when a `changes-requested` event occurs,
     * this may contain an Ember application URL to the affected Submission.
     */
    private String link;

    /**
     * The types of events that might be recorded as SubmissionEvents
     */
    public enum EventType {
        /**
         * A Submission was prepared by a preparer on behalf of a person who does not yet have a User
         * record in PASS. The preparer is requesting that the submitter join PASS and then approve and
         * submit it or provide feedback.
         */
        APPROVAL_REQUESTED_NEWUSER("approval-requested-newuser"),

        /**
         * A Submission was prepared by a preparer who is now requesting that the submitter approve and
         * submit it or provide feedback
         */
        APPROVAL_REQUESTED("approval-requested"),

        /**
         * A Submission was prepared by a preparer, but on review by the submitter, a change was requested.
         * The Submission has been handed back to the preparer for editing.
         */
        CHANGES_REQUESTED("changes-requested"),

        /**
         * A Submission was prepared and then cancelled by the submitter or preparer without being submitted.
         * No further edits can be made to the Submission.
         */
        CANCELLED("cancelled"),

        /**
         * The submit button has been pressed through the UI.
         */
        SUBMITTED("submitted");

        private static final Map<String, EventType> map = new HashMap<>(values().length, 1);

        static {
            for (EventType s : values()) {
                map.put(s.value, s);
            }
        }

        private String value;

        private EventType(String value) {
            this.value = value;
        }

        public static EventType of(String eventType) {
            EventType result = map.get(eventType);
            if (result == null) {
                throw new IllegalArgumentException("Invalid Event Type: " + eventType);
            }
            return result;
        }

        public String getValue() {
            return value;
        }
    }

    @Converter
    public static class EventTypeToStringConverter implements AttributeConverter<EventType, String> {
        @Override
        public String convertToDatabaseColumn(EventType attribute) {
            return attribute == null ? null : attribute.value;
        }

        @Override
        public EventType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : EventType.of(dbData);
        }
    }

    /**
     * Roles of agents who might perform a SubmissionEvent
     */
    public enum PerformerRole {
        PREPARER("preparer"),
        SUBMITTER("submitter");

        private String value;

        private PerformerRole(String value) {
            this.value = value;
        }

        /**
         * Parse performer role
         *
         * @param s status string
         * @return parsed role
         */
        public static PerformerRole of(String s) {
            for (PerformerRole r: PerformerRole.values()) {
                if (r.value.equals(s)) {
                    return r;
                }
            }

            throw new IllegalArgumentException("Invalid performer role: " + s);
        }

        public String getValue() {
            return value;
        }
    }

    @Converter
    public static class PerformerRoleToStringConverter implements AttributeConverter<PerformerRole, String> {
        @Override
        public String convertToDatabaseColumn(PerformerRole attribute) {
            return attribute == null ? null : attribute.value;
        }

        @Override
        public PerformerRole convertToEntityAttribute(String dbData) {
            return dbData == null ? null : PerformerRole.of(dbData);
        }
    }
}
