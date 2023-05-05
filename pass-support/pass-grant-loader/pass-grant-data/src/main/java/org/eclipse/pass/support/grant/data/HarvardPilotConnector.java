/*
 * Copyright 2023 Johns Hopkins University
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
package org.eclipse.pass.support.grant.data;

import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_ABBREVIATED_ROLE;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_DIRECT_FUNDER_LOCAL_KEY;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_DIRECT_FUNDER_NAME;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_DIRECT_FUNDER_POLICY;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_GRANT_AWARD_NUMBER;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_GRANT_END_DATE;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_GRANT_LOCAL_KEY;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_GRANT_PROJECT_NAME;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_GRANT_START_DATE;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_PRIMARY_FUNDER_LOCAL_KEY;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_PRIMARY_FUNDER_NAME;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_PRIMARY_FUNDER_POLICY;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_USER_EMAIL;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_USER_EMPLOYEE_ID;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_USER_FIRST_NAME;
import static org.eclipse.pass.support.grant.data.CoeusFieldNames.C_USER_LAST_NAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation of the Grant Connector interface processes data given to us in an Excel spreadsheet. We take
 * in the information to produce
 * an intermediate data object which is compatible with our PASS data loading setup.
 *
 * @author jrm
 */
public class HarvardPilotConnector implements GrantConnector {

    protected static final String HARVARD_DATA_FILE_PATH_PROPERTY = "harvard.data.file.path";

    private String xlsxDataFilePath;
    private final Properties funderPolicyProperties;

    private static final Logger LOG = LoggerFactory.getLogger(HarvardPilotConnector.class);

    public HarvardPilotConnector(Properties connectionProperties, Properties funderPolicyProperties) {
        if (connectionProperties.getProperty(HARVARD_DATA_FILE_PATH_PROPERTY) != null) {
            this.xlsxDataFilePath = connectionProperties.getProperty(HARVARD_DATA_FILE_PATH_PROPERTY);
        }
        this.funderPolicyProperties = funderPolicyProperties;
    }

    /**
     * We don't consult a database, so this required method is null
     *
     * @param startDate    - the date of the earliest record we wish to get on this pull
     * @param awardEndDate - the date the award ends
     * @param mode         - indicates whether the data pull is for grants, or users
     * @return null
     */
    public String buildQueryString(String startDate, String awardEndDate, String mode, String grant) {
        return null;
    }

    public List<Map<String, String>> retrieveUpdates(String queryString, String mode) throws IOException {

        Sheet grantSheet;
        //First associate funder IDs with their names
        Map<String, String> funderNameMap = new HashMap<>();
        try (FileInputStream excelFile = new FileInputStream(new File(xlsxDataFilePath))
        ) {

            XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
            Sheet funderSheet = workbook.getSheetAt(1);
            for (Row cells : funderSheet) {
                if (cells.getRowNum() > 0) { //skip header
                    funderNameMap.put(stringify(cells.getCell(0)),
                                      stringify(cells.getCell(1)));
                }
            }
            grantSheet = workbook.getSheetAt(0);
        }

        List<Map<String, String>> resultSet = new ArrayList<>();

        if (mode.equals("funder")) {

            for (Object localKey : funderNameMap.keySet()) {
                LOG.debug("Processing funder object with localKey {}", localKey);
                Map<String, String> rowMap = new HashMap<>();
                rowMap.put(C_PRIMARY_FUNDER_LOCAL_KEY, localKey.toString());
                rowMap.put(C_PRIMARY_FUNDER_NAME, funderNameMap.get(localKey.toString()));
                if (funderPolicyProperties.containsKey(localKey)) {
                    rowMap.put(C_PRIMARY_FUNDER_POLICY, funderPolicyProperties.getProperty(localKey.toString()));
                }
                resultSet.add(rowMap);
            }

        } else { //"grant" mode is default
            for (Row cells : grantSheet) {
                if (cells.getRowNum() > 0) { //skip header

                    //we only process rows with a Harvard ID
                    String employeeId = stringify(cells.getCell(6));
                    //String email = stringify(cells.getCell(7));

                    if (employeeId != null && employeeId.length() > 0) {
                        Map<String, String> rowMap = new HashMap<>();

                        rowMap.put(C_GRANT_LOCAL_KEY, stringify(cells.getCell(0))); //A: Harvard grant ID
                        rowMap.put(C_GRANT_AWARD_NUMBER, stringify(cells.getCell(1))); //B: Funder grant ID
                        rowMap.put(C_GRANT_PROJECT_NAME, stringify(cells.getCell(2))); //C: Grant Name
                        rowMap.put(C_USER_FIRST_NAME, stringify(cells.getCell(3))); //D: PI First Name
                        rowMap.put(C_USER_LAST_NAME, stringify(cells.getCell(4))); //E: PI Last Name

                        String role = stringify(cells.getCell(5)); //F: Role
                        rowMap.put(C_ABBREVIATED_ROLE, sortRole(role));

                        rowMap.put(C_USER_EMPLOYEE_ID,
                                   stringify(cells.getCell(6))); //row G used to be Harvard id, we hack it for now
                        rowMap.put(C_USER_EMAIL, stringify(cells.getCell(7))); //H: PI Email

                        String funderLocalKey = stringify(cells.getCell(8)); //I: Funder ID
                        if (funderLocalKey != null) {
                            rowMap.put(C_DIRECT_FUNDER_LOCAL_KEY, funderLocalKey);
                            rowMap.put(C_DIRECT_FUNDER_NAME, funderNameMap.get(funderLocalKey));
                            rowMap.put(C_PRIMARY_FUNDER_LOCAL_KEY, funderLocalKey);
                            rowMap.put(C_PRIMARY_FUNDER_NAME, funderNameMap.get(funderLocalKey));
                            if (funderPolicyProperties.stringPropertyNames().contains(funderLocalKey)) {
                                rowMap.put(C_DIRECT_FUNDER_POLICY, funderPolicyProperties.getProperty(funderLocalKey));
                                rowMap.put(C_PRIMARY_FUNDER_POLICY, funderPolicyProperties.getProperty(funderLocalKey));
                            }
                        }

                        rowMap.put(C_GRANT_START_DATE, stringifyDate(cells.getCell(9))); //J: Grant Start Date
                        rowMap.put(C_GRANT_END_DATE, stringifyDate(cells.getCell(10))); //K: Grant End Date
                        resultSet.add(rowMap);
                        LOG.debug("Added row to result set: {}", rowMap);
                    }
                }
            }
        }

        return resultSet;

    }

    /**
     * Stringify a cell's contents. Since our numerical cells which are dates are all integers but are interpreted
     * by the POI framework as doubles, we correct these to integers
     *
     * @param cell spreadsheet cell
     * @return a string representing a cell's contents
     */
    private String stringify(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType().equals(CellType.STRING)) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType().equals(CellType.NUMERIC)) {
            return String.valueOf((int) cell.getNumericCellValue());//it's an integer
        }
        return null;
    }

    /**
     * Stringify a date cell
     *
     * @param cell a date cell from our spreadsheet
     * @return A date string of the form MM/dd/yyyy
     */
    private String stringifyDate(Cell cell) {
        String pattern = "MM/dd/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(cell.getDateCellValue());
    }

    private String sortRole(String role) {
        if ("Principal Investigator".equals(role)) {
            return "P";
        }
        return "C";
    }

}
