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
 * Files are associated with a Submissions to be used to form Deposits into Repositories
 *
 * @author Karen Hanson
 */

@Include
@Entity
@Table(name = "pass_file")
public class File {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    /**
     * Name of file, defaults to filesystem.name
     */
    private String name;

    /**
     * URI to the bytestream that Deposit services will use to retrieve the bytestream for Deposit
     */
    private String uri;

    /**
     * Description of file provided by User
     */
    private String description;

    /**
     * Role of the file e.g. manuscript, supplemental
     */
    @Convert(converter = FileRoleToStringConverter.class)
    private FileRole fileRole;

    /**
     * Mime-type of file
     */
    private String mimeType;

    /**
     * URI of the Submission the File is a part of
     */
    @ManyToOne
    private Submission submission;

    /**
     * list of possible File Roles
     */
    public enum FileRole {
        /**
         * Author accepted manuscript
         */
        MANUSCRIPT("manuscript"),

        /**
         * Supplemental material for the Publication
         */
        SUPPLEMENTAL("supplemental"),

        /**
         * An image, data plot, map, or schematic
         */
        FIGURE("figure"),

        /**
         * Tabular data
         */
        TABLE("table");

        private static final Map<String, FileRole> map = new HashMap<>(values().length, 1);

        static {
            for (FileRole r : values()) {
                map.put(r.value, r);
            }
        }

        private String value;

        private FileRole(String value) {
            this.value = value;
        }

        /**
         * Parse file role.
         *
         * @param role Role string
         * @return parsed file role.
         */
        public static FileRole of(String role) {
            FileRole result = map.get(role);
            if (result == null) {
                throw new IllegalArgumentException("Invalid File Role: " + role);
            }
            return result;
        }

        public String getValue() {
            return value;
        }
    }

    public static class FileRoleToStringConverter implements AttributeConverter<FileRole, String> {
        @Override
        public String convertToDatabaseColumn(FileRole attribute) {
            return attribute == null ? null : attribute.value;
        }

        @Override
        public FileRole convertToEntityAttribute(String dbData) {
            return dbData == null ? null : FileRole.of(dbData);
        }
    }
}
