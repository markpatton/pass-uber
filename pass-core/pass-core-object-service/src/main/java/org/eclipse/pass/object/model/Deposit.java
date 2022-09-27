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

import java.util.HashMap;
import java.util.Map;
import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.yahoo.elide.annotation.Include;

/**
 * A Submission can have multiple Deposits, each to a different Repository. This describes a single deposit to a
 * Repository and captures
 * its current status.
 *
 * @author Karen Hanson
 */

@Include
@Entity
@Table(name = "pass_deposit")
public class Deposit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * A URL or some kind of reference that can be dereferenced, entity body parsed, and used to determine the status
     * of Deposit
     */
    private String depositStatusRef;

    /**
     * Status of deposit
     */
    @Convert(converter = DepositStatusToStringConverter.class)
    private DepositStatus depositStatus;

    /**
     * URI of the Submission that this Deposit is a part of
     */
    @ManyToOne
    private Submission submission;

    /**
     * URI of Repository being deposited to
     */
    @ManyToOne
    private Repository repository;

    /**
     * URI of the Repository Copy representing the copy that is reltaed to this Deposit. The value is null if there
     * is no copy
     */
    @ManyToOne
    private RepositoryCopy repositoryCopy;

    /**
     * Possible deposit statuses. Note that some repositories may not go through every status.
     */
    public enum DepositStatus {
        /**
         * PASS has sent a package to the target Repository and is waiting for an update on the status
         */
        SUBMITTED("submitted"),
        /**
         * The target Repository has rejected the Deposit
         */
        ACCEPTED("accepted"),
        /**
         * The target Repository has accepted the files into the repository. More steps may be performed by the
         * Repository, but the
         * requirements of the Deposit have been satisfied
         */
        REJECTED("rejected"),
        /**
         * A failure occurred performing the deposit; it may be re-tried later.
         */
        FAILED("failed");

        private static final Map<String, DepositStatus> map = new HashMap<>(values().length, 1);

        static {
            for (DepositStatus d : values()) {
                map.put(d.value, d);
            }
        }

        private String value;

        private DepositStatus(String value) {
            this.value = value;
        }

        /**
         * Parse deposit status
         *
         * @param status status string
         * @return parsed status
         */
        public static DepositStatus of(String status) {
            DepositStatus result = map.get(status);
            if (result == null) {
                throw new IllegalArgumentException("Invalid Deposit Status: " + status);
            }
            return result;
        }

        public String getValue() {
            return value;
        }
    }

    public static class DepositStatusToStringConverter implements AttributeConverter<DepositStatus, String> {
        @Override
        public String convertToDatabaseColumn(DepositStatus attribute) {
            return attribute == null ? null : attribute.value;
        }

        @Override
        public DepositStatus convertToEntityAttribute(String dbData) {
            return dbData == null ? null : DepositStatus.of(dbData);
        }
    }
}
