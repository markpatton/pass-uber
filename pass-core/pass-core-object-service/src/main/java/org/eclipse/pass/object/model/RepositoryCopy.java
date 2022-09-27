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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.eclipse.pass.object.converter.ListToStringConverter;

/**
 * A Repository Copy represents a copy of a Publication that exists in a target Repository.
 *
 * @author Karen Hanson
 */
@Include
@Entity
@Table(name = "pass_repository_copy")
public class RepositoryCopy {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * IDs assigned by the repository
     */
    @Convert(converter = ListToStringConverter.class)
    private List<String> externalIds = new ArrayList<String>();

    /**
     * Status of deposit
     */
    @Convert(converter = CopyStatusToStringConverter.class)
    private CopyStatus copyStatus;

    /**
     * URL to access the item in the repository
     */
    private String accessUrl;

    /**
     * URI of the Publication that this Repository Copy is a copy of
     */
    @ManyToOne
    private Publication publication;

    /**
     * URI of Repository the Copy is in
     */
    @ManyToOne
    private Repository repository;

    /**
     * Possible repository copy statuses. Note that some repositories may not go through every status.
     */
    public enum CopyStatus {
        /**
         * The target Repository has rejected the Deposit
         */
        ACCEPTED("accepted"),
        /**
         * PASS has sent a package to the target Repository and is waiting for an update on the status
         */
        IN_PROGRESS("in-progress"),
        /**
         * The target [Repository](Repository.md) has detected a problem that has caused the progress to stall.
         */
        STALLED("stalled"),
        /**
         * The target Repository has rejected the Deposit
         */
        COMPLETE("complete"),

        /**
         * The RepositoryCopy has been rejected by the remote Repository.
         */
        REJECTED("rejected");

        private static final Map<String, CopyStatus> map = new HashMap<>(values().length, 1);

        static {
            for (CopyStatus c : values()) {
                map.put(c.value, c);
            }
        }

        private String value;

        private CopyStatus(String value) {
            this.value = value;
        }

        /**
         * Parse the copy status.
         *
         * @param status Serialized status.
         * @return Parsed status.
         */
        public static CopyStatus of(String status) {
            CopyStatus result = map.get(status);
            if (result == null) {
                throw new IllegalArgumentException("Invalid Copy Status: " + status);
            }
            return result;
        }

        public String getValue() {
            return value;
        }
    }

    @Converter
    public static class CopyStatusToStringConverter implements AttributeConverter<CopyStatus, String> {
        @Override
        public String convertToDatabaseColumn(CopyStatus attribute) {
            return attribute == null ? null : attribute.value;
        }

        @Override
        public CopyStatus convertToEntityAttribute(String dbData) {
            return dbData == null ? null : CopyStatus.of(dbData);
        }
    }
}
