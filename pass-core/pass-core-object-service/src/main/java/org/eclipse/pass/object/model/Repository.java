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
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.eclipse.pass.object.converter.ListToStringConverter;

import com.yahoo.elide.annotation.Include;

/**
 * Describes a Repository. A Repository is the target of a Deposit.
 *
 * @author Karen Hanson
 */

@Include
@Entity
@Table(name = "pass_repository")
public class Repository {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    /**
     * Name of repository e.g. "PubMed Central"
     */
    private String name;

    /**
     * Several sentence description of repository
     */
    private String description;

    /**
     * URL to the homepage of the repository so that PASS users can view the platform before deciding whether to
     * participate in it
     */
    private String url;

    /**
     * The legal text that a submitter must agree to in order to submit a publication to this repository
     */
    @Column(columnDefinition = "text")
    private String agreementText;

    /**
     * Stringified JSON representing a form template to be loaded by the front-end when this Repository is selected
     */
    @Column(columnDefinition = "text")
    private String formSchema;

    /**
     * Type of integration PASS has with the Repository
     */
    @Convert(converter = IntegrationTypeToStringConverter.class)
    private IntegrationType integrationType;

    /**
     * Key that is unique to this {@code Repository} instance.  Used to reference the {@code Repository} when its URI
     * is not available (e.g. prior to the creation of a {@code Repository} resource in Fedora).
     */
    private String repositoryKey;

    /**
     * URLs that link to JSON schema documents describing the repository's metadata requirements
     */
    @Convert(converter = ListToStringConverter.class)
    private List<String> schemas = new ArrayList<>();

    /**
     * Possible deposit statuses. Note that some repositories may not go through every status.
     */
    public enum IntegrationType {
        /**
         * PASS can make Deposits to this Repository, and will received updates about its status
         */
        FULL("full"),
        /**
         * PASS can make Deposits to this Repository but will not automatically receive updates about its status
         */
        ONE_WAY("one-way"),
        /**
         * A deposit cannot automatically be made to this Repository from PASS, only a web link can be created.
         */
        WEB_LINK("web-link");

        private static final Map<String, IntegrationType> map = new HashMap<>(values().length, 1);

        static {
            for (IntegrationType d : values()) {
                map.put(d.value, d);
            }
        }

        private String value;

        private IntegrationType(String value) {
            this.value = value;
        }

        /**
         * Parse the integration type.
         *
         * @param integrationType String serialized integration type
         * @return parsed integration type.
         */
        public static IntegrationType of(String integrationType) {
            IntegrationType result = map.get(integrationType);
            if (result == null) {
                throw new IllegalArgumentException("Invalid Integration Type: " + integrationType);
            }
            return result;
        }
    }

    private static class IntegrationTypeToStringConverter implements AttributeConverter<IntegrationType, String> {
        @Override
        public String convertToDatabaseColumn(IntegrationType attribute) {
            return attribute == null ? null : attribute.value;
        }

        @Override
        public IntegrationType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : IntegrationType.of(dbData);
        }
    }
}
