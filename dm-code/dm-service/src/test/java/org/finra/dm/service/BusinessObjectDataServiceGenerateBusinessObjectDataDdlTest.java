/*
* Copyright 2015 herd contributors
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
package org.finra.dm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.finra.dm.model.ObjectNotFoundException;
import org.finra.dm.model.api.xml.BusinessObjectDataDdl;
import org.finra.dm.model.api.xml.BusinessObjectDataDdlOutputFormatEnum;
import org.finra.dm.model.api.xml.BusinessObjectDataDdlRequest;
import org.finra.dm.model.api.xml.LatestAfterPartitionValue;
import org.finra.dm.model.api.xml.LatestBeforePartitionValue;
import org.finra.dm.model.api.xml.PartitionValueFilter;
import org.finra.dm.model.api.xml.PartitionValueRange;
import org.finra.dm.model.api.xml.SchemaColumn;
import org.finra.dm.model.dto.ConfigurationValue;
import org.finra.dm.model.jpa.BusinessObjectDataEntity;
import org.finra.dm.model.jpa.BusinessObjectDataStatusEntity;
import org.finra.dm.model.jpa.BusinessObjectFormatEntity;
import org.finra.dm.model.jpa.FileTypeEntity;
import org.finra.dm.model.jpa.StorageEntity;
import org.finra.dm.model.jpa.StoragePlatformEntity;
import org.finra.dm.model.jpa.StorageUnitEntity;
import org.finra.dm.service.helper.BusinessObjectDataHelper;
import org.finra.dm.service.helper.DmDaoHelper;
import org.finra.dm.service.helper.Hive13DdlGenerator;

/**
 * This class tests generateBusinessObjectDataDdl functionality within the business object data REST controller.
 */
public class BusinessObjectDataServiceGenerateBusinessObjectDataDdlTest extends AbstractServiceTest
{
    private static Logger logger = Logger.getLogger(BusinessObjectDataServiceGenerateBusinessObjectDataDdlTest.class);

    @Autowired
    private BusinessObjectDataHelper businessObjectDataHelper;

    @Autowired
    private DmDaoHelper dmDaoHelper;

    @Test
    public void testGenerateBusinessObjectDataDdlPartitionValueList()
    {
        // Prepare test data.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting();

        BusinessObjectDataDdlRequest request;
        BusinessObjectDataDdl resultDdl;

        // Retrieve business object data ddl.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        validateBusinessObjectDataDdl(request, getExpectedDdl(), resultDdl);

        // Retrieve business object data ddl when request partition value list has duplicate values.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        request.getPartitionValueFilters().get(0).getPartitionValues().add(UNSORTED_PARTITION_VALUES.get(0));
        resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        validateBusinessObjectDataDdl(request, getExpectedDdl(), resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlPartitionValueListStandalonePartitionValueFilter()
    {
        // Prepare test data.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting();

        BusinessObjectDataDdlRequest request;
        BusinessObjectDataDdl resultDdl;

        // Retrieve business object data ddl using request with a standalone partition value filter.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        request.setPartitionValueFilter(request.getPartitionValueFilters().get(0));
        request.setPartitionValueFilters(null);
        resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        validateBusinessObjectDataDdl(request, getExpectedDdl(), resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlPartitionValueRange()
    {
        // Prepare test data.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting();
        createExpectedPartitionValueProcessDatesForApril2014(PARTITION_KEY_GROUP);

        BusinessObjectDataDdlRequest request;
        BusinessObjectDataDdl resultDdl;
        String expectedDdl;

        // Retrieve business object data ddl when start partition value is less than the end partition value.
        request = getTestBusinessObjectDataDdlRequest(START_PARTITION_VALUE, END_PARTITION_VALUE, CUSTOM_DDL_NAME);
        resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        expectedDdl = getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
            FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, PROCESS_DATE_AVAILABLE_PARTITION_VALUES,
            SUBPARTITION_VALUES, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);

        // Retrieve business object data ddl when start partition value is equal to the end partition value.
        request = getTestBusinessObjectDataDdlRequest(START_PARTITION_VALUE, START_PARTITION_VALUE, CUSTOM_DDL_NAME);
        resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        expectedDdl = getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
            FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, Arrays.asList(START_PARTITION_VALUE), SUBPARTITION_VALUES,
            false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlLatestBeforePartitionValue()
    {
        // Prepare database entities required for testing.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(PARTITION_VALUE);

        // Check an availability using a latest before partition value filter option.
        for (String upperBoundPartitionValue : Arrays.asList(PARTITION_VALUE, PARTITION_VALUE_2))
        {
            BusinessObjectDataDdl resultBusinessObjectDataDdl = businessObjectDataService.generateBusinessObjectDataDdl(
                new BusinessObjectDataDdlRequest(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, Arrays.asList(
                    new PartitionValueFilter(PARTITION_KEY, NO_PARTITION_VALUES, NO_PARTITION_VALUE_RANGE,
                        new LatestBeforePartitionValue(upperBoundPartitionValue), NO_LATEST_AFTER_PARTITION_VALUE)), NO_STANDALONE_PARTITION_VALUE_FILTER,
                    DATA_VERSION, NO_STORAGE_NAMES, STORAGE_NAME, BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME,
                    INCLUDE_DROP_TABLE_STATEMENT, INCLUDE_IF_NOT_EXISTS_OPTION, INCLUDE_DROP_PARTITIONS, NO_ALLOW_MISSING_DATA));

            // Validate the response object.
            assertEquals(new BusinessObjectDataDdl(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, Arrays.asList(
                new PartitionValueFilter(PARTITION_KEY, NO_PARTITION_VALUES, NO_PARTITION_VALUE_RANGE, new LatestBeforePartitionValue(upperBoundPartitionValue),
                    NO_LATEST_AFTER_PARTITION_VALUE)), NO_STANDALONE_PARTITION_VALUE_FILTER, DATA_VERSION, NO_STORAGE_NAMES, STORAGE_NAME,
                BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, getExpectedHiveDdl(PARTITION_VALUE)),
                resultBusinessObjectDataDdl);
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlLatestAfterPartitionValue()
    {
        // Prepare database entities required for testing.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(PARTITION_VALUE_2);

        // Check an availability using a latest after partition value filter option.
        for (String lowerBoundPartitionValue : Arrays.asList(PARTITION_VALUE, PARTITION_VALUE_2))
        {
            BusinessObjectDataDdl resultBusinessObjectDataDdl = businessObjectDataService.generateBusinessObjectDataDdl(
                new BusinessObjectDataDdlRequest(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, Arrays.asList(
                    new PartitionValueFilter(PARTITION_KEY, NO_PARTITION_VALUES, NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
                        new LatestAfterPartitionValue(lowerBoundPartitionValue))), NO_STANDALONE_PARTITION_VALUE_FILTER, DATA_VERSION, NO_STORAGE_NAMES,
                    STORAGE_NAME, BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, INCLUDE_DROP_TABLE_STATEMENT,
                    INCLUDE_IF_NOT_EXISTS_OPTION, INCLUDE_DROP_PARTITIONS, NO_ALLOW_MISSING_DATA));

            // Validate the response object.
            assertEquals(new BusinessObjectDataDdl(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, Arrays.asList(
                new PartitionValueFilter(PARTITION_KEY, NO_PARTITION_VALUES, NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
                    new LatestAfterPartitionValue(lowerBoundPartitionValue))), NO_STANDALONE_PARTITION_VALUE_FILTER, DATA_VERSION, NO_STORAGE_NAMES,
                STORAGE_NAME, BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, getExpectedHiveDdl(PARTITION_VALUE_2)),
                resultBusinessObjectDataDdl);
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlMissingRequiredParameters()
    {
        BusinessObjectDataDdlRequest request;

        // Try to retrieve business object data ddl when business object definition name parameter is not specified.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.setBusinessObjectDefinitionName(BLANK_TEXT);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when business object definition name parameter is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A business object definition name must be specified.", e.getMessage());
        }

        // Try to retrieve business object data ddl when business object format usage parameter is not specified.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.setBusinessObjectFormatUsage(BLANK_TEXT);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when business object format usage parameter is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A business object format usage name must be specified.", e.getMessage());
        }

        // Try to retrieve business object data ddl when business object format file type parameter is not specified.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.setBusinessObjectFormatFileType(BLANK_TEXT);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when business object format file type parameter is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A business object format file type must be specified.", e.getMessage());
        }

        // Try to retrieve business object data ddl when partition key is not specified.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.getPartitionValueFilters().get(0).setPartitionKey(BLANK_TEXT);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when partition key is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A partition key must be specified.", e.getMessage());
        }

        // Try to retrieve business object data ddl when start partition value is not specified.
        request = getTestBusinessObjectDataDdlRequest(BLANK_TEXT, END_PARTITION_VALUE);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when start partition values is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A start partition value for the partition value range must be specified.", e.getMessage());
        }

        // Try to retrieve business object data ddl when end partition value is not specified.
        request = getTestBusinessObjectDataDdlRequest(START_PARTITION_VALUE, BLANK_TEXT, null);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when end partition values is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("An end partition value for the partition value range must be specified.", e.getMessage());
        }

        // Try to retrieve business object data ddl when partition value list has no partition values specified.
        request = getTestBusinessObjectDataDdlRequest(new ArrayList<String>());
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when partition value list has no partition values specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("At least one partition value must be specified.", e.getMessage());
        }

        // Try to retrieve business object data ddl when one of the partition values in the partition value list is not specified.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.getPartitionValueFilters().get(0).getPartitionValues().add(BLANK_TEXT);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when one of the partition values in the partition value list is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A partition value must be specified.", e.getMessage());
        }

        // Try to retrieve business object data ddl when the latest before partition value filter option has no partition value specified.
        request = getTestBusinessObjectDataDdlRequest(NO_PARTITION_VALUES);
        for (String partitionValue : Arrays.asList(null, BLANK_TEXT))
        {
            request.getPartitionValueFilters().get(0).setLatestBeforePartitionValue(new LatestBeforePartitionValue(partitionValue));
            try
            {
                businessObjectDataService.generateBusinessObjectDataDdl(request);
                fail("Should throw an IllegalArgumentException when the latest before partition value filter option has no partition value specified.");
            }
            catch (IllegalArgumentException e)
            {
                assertEquals("A partition value must be specified.", e.getMessage());
            }
        }

        // Try to retrieve business object data ddl when the latest after partition value filter option has no partition value specified.
        request = getTestBusinessObjectDataDdlRequest(NO_PARTITION_VALUES);
        for (String partitionValue : Arrays.asList(null, BLANK_TEXT))
        {
            request.getPartitionValueFilters().get(0).setLatestAfterPartitionValue(new LatestAfterPartitionValue(partitionValue));
            try
            {
                businessObjectDataService.generateBusinessObjectDataDdl(request);
                fail("Should throw an IllegalArgumentException when the latest after partition value filter option has no partition value specified.");
            }
            catch (IllegalArgumentException e)
            {
                assertEquals("A partition value must be specified.", e.getMessage());
            }
        }

        // Try to retrieve business object data ddl when standalone storage name parameter is not specified.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.setStorageName(BLANK_TEXT);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when standalone storage name parameter is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A storage name must be specified.", e.getMessage());
        }

        // Try to check business object data availability when standalone storage name parameter value is not specified.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.setStorageName(null);
        request.setStorageNames(Arrays.asList(BLANK_TEXT));
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when storage name parameter in the list of storage names is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A storage name must be specified.", e.getMessage());
        }

        // Try to retrieve business object data ddl when output format parameter is not specified.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.setOutputFormat(null);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when output format parameter is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("An output format must be specified.", e.getMessage());
        }

        // Try to retrieve business object data ddl when table name parameter is not specified.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.setTableName(BLANK_TEXT);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when table name parameter is not specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A table name must be specified.", e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlMissingOptionalParametersPartitionValueList()
    {
        // Prepare test data without custom ddl.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, PARTITION_KEY_GROUP,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), getTestPartitionColumns(), false, CUSTOM_DDL_NAME, true);

        // Retrieve business object data ddl request without optional parameters.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(STORAGE_1_AVAILABLE_PARTITION_VALUES);
        request.setBusinessObjectFormatVersion(null);
        request.setBusinessObjectDataVersion(null);
        request.setIncludeDropTableStatement(null);
        request.setIncludeIfNotExistsOption(null);
        request.setAllowMissingData(null);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, false, false);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlMissingOptionalParametersPartitionValueListStandalonePartitionValueFilter()
    {
        // Prepare test data without custom ddl.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, PARTITION_KEY_GROUP,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), getTestPartitionColumns(), false, CUSTOM_DDL_NAME, true);

        // Retrieve business object data ddl using request with a standalone partition value filter and without optional parameters.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(STORAGE_1_AVAILABLE_PARTITION_VALUES);
        request.setPartitionValueFilter(request.getPartitionValueFilters().get(0));
        request.setPartitionValueFilters(null);
        request.setBusinessObjectFormatVersion(null);
        request.setBusinessObjectDataVersion(null);
        request.getPartitionValueFilter().setPartitionKey(BLANK_TEXT);
        request.setIncludeDropTableStatement(null);
        request.setIncludeIfNotExistsOption(null);
        request.setAllowMissingData(null);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, false, false);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlPartitionValueListLegacy()
    {
        // Create a legacy business object definition.
        createBusinessObjectDefinitionEntity(NAMESPACE_CD, BOD_NAME, DATA_PROVIDER_NAME, BOD_DESCRIPTION, true);

        // Prepare test data without custom ddl.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, PARTITION_KEY_GROUP,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), getTestPartitionColumns(), false, CUSTOM_DDL_NAME, true);

        // Retrieve business object data ddl request without optional parameters.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);

        // Clear the namespace in request.
        request.setNamespace(null);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlMissingOptionalParametersPartitionValueRange()
    {
        // Prepare test data without custom ddl.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, PARTITION_KEY_GROUP,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), getTestPartitionColumns(), false, CUSTOM_DDL_NAME, true);
        createExpectedPartitionValueProcessDatesForApril2014(PARTITION_KEY_GROUP);

        // Retrieve business object data ddl request without optional parameters.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(START_PARTITION_VALUE, END_PARTITION_VALUE);
        request.setBusinessObjectFormatVersion(null);
        request.setBusinessObjectDataVersion(null);
        request.setIncludeDropTableStatement(null);
        request.setIncludeIfNotExistsOption(null);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, PROCESS_DATE_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, false, false);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlTrimParametersPartitionValueList()
    {
        // Prepare test data.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting();

        // Retrieve business object data ddl request with all string values requiring trimming.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        request.setBusinessObjectDefinitionName(addWhitespace(request.getBusinessObjectDefinitionName()));
        request.setBusinessObjectFormatUsage(addWhitespace(request.getBusinessObjectFormatUsage()));
        request.setBusinessObjectFormatFileType(addWhitespace(request.getBusinessObjectFormatFileType()));
        request.getPartitionValueFilters().get(0).setPartitionKey(addWhitespace(request.getPartitionValueFilters().get(0).getPartitionKey()));
        for (int i = 0; i < request.getPartitionValueFilters().get(0).getPartitionValues().size(); i++)
        {
            request.getPartitionValueFilters().get(0).getPartitionValues()
                .set(i, addWhitespace(request.getPartitionValueFilters().get(0).getPartitionValues().get(i)));
        }
        request.setStorageName(addWhitespace(request.getStorageName()));
        request.setTableName(addWhitespace(request.getTableName()));
        request.setCustomDdlName(addWhitespace(request.getCustomDdlName()));
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        validateBusinessObjectDataDdl(request, getExpectedDdl(), resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlTrimParametersPartitionValueRange()
    {
        // Prepare test data.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting();
        createExpectedPartitionValueProcessDatesForApril2014(PARTITION_KEY_GROUP);

        // Retrieve business object data ddl request with all string values requiring trimming.
        BusinessObjectDataDdlRequest request =
            getTestBusinessObjectDataDdlRequest(addWhitespace(START_PARTITION_VALUE), addWhitespace(END_PARTITION_VALUE), addWhitespace(CUSTOM_DDL_NAME));
        request.setBusinessObjectDefinitionName(addWhitespace(request.getBusinessObjectDefinitionName()));
        request.setBusinessObjectFormatUsage(addWhitespace(request.getBusinessObjectFormatUsage()));
        request.setBusinessObjectFormatFileType(addWhitespace(request.getBusinessObjectFormatFileType()));
        request.getPartitionValueFilters().get(0).setPartitionKey(addWhitespace(request.getPartitionValueFilters().get(0).getPartitionKey()));
        request.setStorageName(addWhitespace(request.getStorageName()));
        request.setTableName(addWhitespace(request.getTableName()));
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, PROCESS_DATE_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlUpperCaseParameters()
    {
        // Prepare test data.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting();

        // Retrieve business object data ddl request with all parameter values in upper case (except for case-sensitive partition values).
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        request.setBusinessObjectDefinitionName(request.getBusinessObjectDefinitionName().toUpperCase());
        request.setBusinessObjectFormatUsage(request.getBusinessObjectFormatUsage().toUpperCase());
        request.setBusinessObjectFormatFileType(request.getBusinessObjectFormatFileType().toUpperCase());
        request.getPartitionValueFilters().get(0).setPartitionKey(request.getPartitionValueFilters().get(0).getPartitionKey().toUpperCase());
        request.setStorageName(request.getStorageName().toUpperCase());
        request.setCustomDdlName(request.getCustomDdlName().toUpperCase());
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        validateBusinessObjectDataDdl(request, getExpectedDdl(), resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlLowerCaseParameters()
    {
        // Prepare test data.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting();

        // Retrieve business object data ddl request with all parameter values in lower case (except for case-sensitive partition values).
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        request.setBusinessObjectDefinitionName(request.getBusinessObjectDefinitionName().toLowerCase());
        request.setBusinessObjectFormatUsage(request.getBusinessObjectFormatUsage().toLowerCase());
        request.setBusinessObjectFormatFileType(request.getBusinessObjectFormatFileType().toLowerCase());
        request.getPartitionValueFilters().get(0).setPartitionKey(request.getPartitionValueFilters().get(0).getPartitionKey().toLowerCase());
        request.setStorageName(request.getStorageName().toLowerCase());
        request.setCustomDdlName(request.getCustomDdlName().toLowerCase());
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        validateBusinessObjectDataDdl(request, getExpectedDdl(), resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlInvalidParameters()
    {
        // Prepare test data.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting();

        BusinessObjectDataDdlRequest request;

        // Try to retrieve business object data ddl using non-existing format.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        request.setBusinessObjectDefinitionName("I_DO_NOT_EXIST");
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an ObjectNotFoundException when non-existing business object format is used.");
        }
        catch (ObjectNotFoundException e)
        {
            assertEquals(getExpectedBusinessObjectFormatNotFoundErrorMessage(request.getNamespace(), request.getBusinessObjectDefinitionName(),
                request.getBusinessObjectFormatUsage(), request.getBusinessObjectFormatFileType(), request.getBusinessObjectFormatVersion()), e.getMessage());
        }

        // Try to retrieve business object data ddl using non-existing partition key (partition column).
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        request.getPartitionValueFilters().get(0).setPartitionKey("I_DO_NOT_EXIST");
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when non-existing partition key is used.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("The partition key \"%s\" does not exist in first %d partition columns in the schema for business object format " +
                "{namespace: \"%s\", businessObjectDefinitionName: \"%s\", businessObjectFormatUsage: \"%s\", businessObjectFormatFileType: \"%s\", " +
                "businessObjectFormatVersion: %d}.", request.getPartitionValueFilters().get(0).getPartitionKey(),
                BusinessObjectDataEntity.MAX_SUBPARTITIONS + 1, request.getNamespace(), request.getBusinessObjectDefinitionName(),
                request.getBusinessObjectFormatUsage(), request.getBusinessObjectFormatFileType(), request.getBusinessObjectFormatVersion()), e.getMessage());
        }

        // Try to retrieve business object data ddl when both partition value filter and partition value filter list are specified.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        request.setPartitionValueFilter(
            new PartitionValueFilter(FIRST_PARTITION_COLUMN_NAME.toUpperCase(), new ArrayList<>(UNSORTED_PARTITION_VALUES), NO_PARTITION_VALUE_RANGE,
                NO_LATEST_BEFORE_PARTITION_VALUE, NO_LATEST_AFTER_PARTITION_VALUE));
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when both a list of partition value filters and a standalone partition value filter are specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A list of partition value filters and a standalone partition value filter cannot be both specified.", e.getMessage());
        }

        // Try to retrieve business object data ddl when partition value filter has none or more than one partition value filter option specified.
        for (PartitionValueFilter partitionValueFilter : getInvalidPartitionValueFilters())
        {
            request = new BusinessObjectDataDdlRequest(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION,
                Arrays.asList(partitionValueFilter), NO_STANDALONE_PARTITION_VALUE_FILTER, DATA_VERSION, NO_STORAGE_NAMES, STORAGE_NAME,
                BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, INCLUDE_DROP_TABLE_STATEMENT, INCLUDE_IF_NOT_EXISTS_OPTION,
                NO_INCLUDE_DROP_PARTITIONS, NO_ALLOW_MISSING_DATA);

            try
            {
                businessObjectDataService.generateBusinessObjectDataDdl(request);
                fail("Should throw an IllegalArgumentException when partition value filter has more than one partition value filter option.");
            }
            catch (IllegalArgumentException e)
            {
                assertEquals("Exactly one partition value filter option must be specified.", e.getMessage());
            }
        }

        // Try to retrieve business object data ddl when partition value token is specified with a partition value range.
        request = getTestBusinessObjectDataDdlRequest(BusinessObjectDataService.MAX_PARTITION_VALUE_TOKEN, END_PARTITION_VALUE);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when partition value token is specified with a partition value range.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A partition value token cannot be specified with a partition value range.", e.getMessage());
        }

        // Try to retrieve business object data ddl when partition value token is specified with a partition value range.
        request = getTestBusinessObjectDataDdlRequest(BusinessObjectDataService.MIN_PARTITION_VALUE_TOKEN, END_PARTITION_VALUE);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when partition value token is specified with a partition value range.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A partition value token cannot be specified with a partition value range.", e.getMessage());
        }

        // Try to retrieve business object data ddl when partition value token is specified with a partition value range.
        request = getTestBusinessObjectDataDdlRequest(START_PARTITION_VALUE, BusinessObjectDataService.MAX_PARTITION_VALUE_TOKEN);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when partition value token is specified with a partition value range.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A partition value token cannot be specified with a partition value range.", e.getMessage());
        }

        // Try to retrieve business object data ddl when partition value token is specified with a partition value range.
        request = getTestBusinessObjectDataDdlRequest(END_PARTITION_VALUE, BusinessObjectDataService.MIN_PARTITION_VALUE_TOKEN);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when partition value token is specified with a partition value range.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A partition value token cannot be specified with a partition value range.", e.getMessage());
        }

        // Try to retrieve business object data ddl when partition value filter has start partition value which is greater than the end partition value.
        request = getTestBusinessObjectDataDdlRequest(END_PARTITION_VALUE, START_PARTITION_VALUE, CUSTOM_DDL_NAME);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when start partition value which is greater than the end partition value.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String
                .format("The start partition value \"%s\" cannot be greater than the end partition value \"%s\".", END_PARTITION_VALUE, START_PARTITION_VALUE),
                e.getMessage());
        }

        // Try to retrieve business object data ddl when partition value filter has specifies a range that results in no valid partition values.
        request = getTestBusinessObjectDataDdlRequest("I_DO_NOT_EXIST_1", "I_DO_NOT_EXIST_2", null, CUSTOM_DDL_NAME);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when partition value filter has specifies a range that results in no valid partition values.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Partition value range [\"%s\", \"%s\"] contains no valid partition values.",
                request.getPartitionValueFilters().get(0).getPartitionValueRange().getStartPartitionValue(),
                request.getPartitionValueFilters().get(0).getPartitionValueRange().getEndPartitionValue()), e.getMessage());
        }

        // Try to retrieve business object data ddl when partition value filter has specifies a range that results in no valid partition values.
        String invalidPartitionValue = "INVALID_PARTITION_VALUE_/";
        request = getTestBusinessObjectDataDdlRequest(Arrays.asList(invalidPartitionValue), CUSTOM_DDL_NAME);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when partition value contains a '/' character.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(
                String.format("Partition value \"%s\" can not contain a '/' character.", request.getPartitionValueFilters().get(0).getPartitionValues().get(0)),
                e.getMessage());
        }

        // Try to retrieve business object data ddl when partition value token is specified as a partition value.
        request = getTestBusinessObjectDataDdlRequest(Arrays.asList(BusinessObjectDataService.MAX_PARTITION_VALUE_TOKEN), CUSTOM_DDL_NAME);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when partition value token is specified as a partition value.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A partition value token cannot be specified as one of partition values.", e.getMessage());
        }

        // Try to retrieve business object data ddl when partition value token is specified as a partition value.
        request = getTestBusinessObjectDataDdlRequest(Arrays.asList(BusinessObjectDataService.MIN_PARTITION_VALUE_TOKEN), CUSTOM_DDL_NAME);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when partition value token is specified as a partition value.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A partition value token cannot be specified as one of partition values.", e.getMessage());
        }

        // Try to retrieve business object data ddl when both a list of storage names and standalone storage name are specified.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.setStorageName(BLANK_TEXT);
        request.setStorageNames(STORAGE_NAMES);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when both a list of storage names and standalone storage name are specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A list of storage names and a standalone storage name cannot be both specified.", e.getMessage());
        }

        // Try to retrieve business object data ddl passing a non-existing storage as a standalone storage name.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        request.setStorageName("I_DO_NOT_EXIST");
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an ObjectNotFoundException when non-existing storage is used as a standalone storage name.");
        }
        catch (ObjectNotFoundException e)
        {
            assertEquals(String.format("Storage with name \"%s\" doesn't exist.", request.getStorageName()), e.getMessage());
        }

        // Try to retrieve business object data ddl passing a non-existing storage in the list of storage names.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.setStorageName(null);
        request.setStorageNames(Arrays.asList("I_DO_NOT_EXIST"));
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an ObjectNotFoundException when non-existing storage is used in the list of storage names.");
        }
        catch (ObjectNotFoundException e)
        {
            assertEquals(String.format("Storage with name \"%s\" doesn't exist.", request.getStorageNames().get(0)), e.getMessage());
        }

        // Try to retrieve business object data ddl using non-existing custom ddl.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        request.setCustomDdlName("I_DO_NOT_EXIST");
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an ObjectNotFoundException when non-existing custom ddl is used.");
        }
        catch (ObjectNotFoundException e)
        {
            assertEquals(String.format("Custom DDL with name \"%s\" does not exist for business object format with namespace \"%s\", " +
                "business object definition name \"%s\", format usage \"%s\", format file type \"%s\", and format version \"%d\".", request.getCustomDdlName(),
                request.getNamespace(), request.getBusinessObjectDefinitionName(), request.getBusinessObjectFormatUsage(),
                request.getBusinessObjectFormatFileType(), request.getBusinessObjectFormatVersion()), e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlTwoPartitionValueRanges()
    {
        // Prepare test data.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting();

        // Try to retrieve business object data ddl when two partition value ranges are specified.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        List<PartitionValueFilter> partitionValueFilters = new ArrayList<>();
        request.setPartitionValueFilters(partitionValueFilters);
        partitionValueFilters.add(
            new PartitionValueFilter(FIRST_PARTITION_COLUMN_NAME, null, new PartitionValueRange(START_PARTITION_VALUE, END_PARTITION_VALUE),
                NO_LATEST_BEFORE_PARTITION_VALUE, NO_LATEST_AFTER_PARTITION_VALUE));
        partitionValueFilters.add(
            new PartitionValueFilter(FIRST_PARTITION_COLUMN_NAME, null, new PartitionValueRange(START_PARTITION_VALUE, END_PARTITION_VALUE),
                NO_LATEST_BEFORE_PARTITION_VALUE, NO_LATEST_AFTER_PARTITION_VALUE));
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when more than one partition value range is specified.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Cannot specify more than one partition value range.", e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlDuplicatePartitionColumns()
    {
        // Prepare test data.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting();

        // Try to retrieve business object data ddl using partition value filters with duplicate partition columns.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        List<PartitionValueFilter> partitionValueFilters = new ArrayList<>();
        request.setPartitionValueFilters(partitionValueFilters);
        partitionValueFilters.add(
            new PartitionValueFilter(FIRST_PARTITION_COLUMN_NAME.toUpperCase(), new ArrayList<>(UNSORTED_PARTITION_VALUES), NO_PARTITION_VALUE_RANGE,
                NO_LATEST_BEFORE_PARTITION_VALUE, NO_LATEST_AFTER_PARTITION_VALUE));
        partitionValueFilters.add(
            new PartitionValueFilter(FIRST_PARTITION_COLUMN_NAME.toLowerCase(), new ArrayList<>(UNSORTED_PARTITION_VALUES), NO_PARTITION_VALUE_RANGE,
                NO_LATEST_BEFORE_PARTITION_VALUE, NO_LATEST_AFTER_PARTITION_VALUE));
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when partition value filters use duplicate partition columns.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Partition value filters specify duplicate partition columns.", e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlNoSchemaExists()
    {
        // Prepare test data without schema.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, PARTITION_KEY, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, NO_SUBPARTITION_VALUES, null, null, null, null, null, false,
            null, true);

        // Try to retrieve business object data ddl when the business object format has no schema.
        // Retrieve business object data ddl without specifying custom ddl name.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when business object format has no schema.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Business object format with namespace \"%s\", business object definition name \"%s\", format usage \"%s\", " +
                "format file type \"%s\", and format version \"%s\" doesn't have schema information.", request.getNamespace(),
                request.getBusinessObjectDefinitionName(), request.getBusinessObjectFormatUsage(), request.getBusinessObjectFormatFileType(),
                request.getBusinessObjectFormatVersion()), e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlPartitionColumnIsAlsoRegularColumn()
    {
        // Prepare test data without custom ddl.
        List<SchemaColumn> schemaColumns = getTestSchemaColumns();
        List<SchemaColumn> partitionColumns = getTestPartitionColumns();
        // Override the first schema column to be a partition column.
        schemaColumns.set(0, partitionColumns.get(0));
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, schemaColumns, partitionColumns, false, null, true);

        // Retrieve business object data ddl without specifying custom ddl name.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedDdl = getExpectedDdl(partitionColumns.size(), "ORGNL_PRTN_CLMN001", "DATE", ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
            FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES, SUBPARTITION_VALUES,
            false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlPartitionValueRangeFormatWithoutPartitionKeyGroup()
    {
        // Prepare test data with business object format having no partition key group value specified.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), getTestPartitionColumns(), false, CUSTOM_DDL_NAME, true);

        // Try to generate business object data ddl using partition value range when business object format has no partition key group.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(START_PARTITION_VALUE, END_PARTITION_VALUE, CUSTOM_DDL_NAME);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when generating ddl for a partition value range and " +
                "business object format has no partition key group.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("A partition key group, which is required to use partition value ranges, " +
                "is not specified for the business object format {namespace: \"%s\", businessObjectDefinitionName: \"%s\", " +
                "businessObjectFormatUsage: \"%s\", businessObjectFormatFileType: \"%s\", businessObjectFormatVersion: %d}.", request.getNamespace(),
                request.getBusinessObjectDefinitionName(), request.getBusinessObjectFormatUsage(), request.getBusinessObjectFormatFileType(),
                request.getBusinessObjectFormatVersion()), e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlPartitionValueRangeExpectedPartitionValueMatchesMaxPartitionValueToken()
    {
        // Prepare test data with expected partition value set to the maximum partition value token.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting();
        String startPartitionValue = BusinessObjectDataService.MAX_PARTITION_VALUE_TOKEN.replace("maximum", "a");
        String endPartitionValue = BusinessObjectDataService.MAX_PARTITION_VALUE_TOKEN.replace("maximum", "z");
        createExpectedPartitionValueEntities(PARTITION_KEY_GROUP,
            Arrays.asList(startPartitionValue, BusinessObjectDataService.MAX_PARTITION_VALUE_TOKEN, endPartitionValue));

        // Try to generate business object data ddl when expected partition value matches to the maximum partition value token.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(startPartitionValue, endPartitionValue, CUSTOM_DDL_NAME);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when expected partition value matches to the maximum partition value token.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A partition value token cannot be specified as one of the expected partition values.", e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlPartitionValueRangeExpectedPartitionValueMatchesMinPartitionValueToken()
    {
        // Prepare test data with expected partition value set to minimum partition value token.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting();
        String startPartitionValue = BusinessObjectDataService.MIN_PARTITION_VALUE_TOKEN.replace("minimum", "a");
        String endPartitionValue = BusinessObjectDataService.MIN_PARTITION_VALUE_TOKEN.replace("minimum", "z");
        createExpectedPartitionValueEntities(PARTITION_KEY_GROUP,
            Arrays.asList(startPartitionValue, BusinessObjectDataService.MIN_PARTITION_VALUE_TOKEN, endPartitionValue));

        // Try to generate business object data ddl when expected partition value matches to the maximum partition value token.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(startPartitionValue, endPartitionValue, CUSTOM_DDL_NAME);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when expected partition value matches to the minimum partition value token.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("A partition value token cannot be specified as one of the expected partition values.", e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlSingleLevelPartitioningPartitionValueList()
    {
        // Prepare test data without custom ddl.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns().subList(0, 1);
        String partitionKey = partitionColumns.get(0).getName();
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, partitionKey, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, NO_SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), partitionColumns, false, null, true);

        // Retrieve business object data ddl without specifying custom ddl name.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedDdl =
            getExpectedDdl(partitionColumns.size(), FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);

        // Retrieve business object data ddl when request partition value list has duplicate values.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.getPartitionValueFilters().get(0).getPartitionValues().add(UNSORTED_PARTITION_VALUES.get(0));
        resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlSingleLevelPartitioningPartitionValueRange()
    {
        // Prepare test data without custom ddl and with partition key using NO_PARTITIONING_PARTITION_KEY.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns().subList(0, 1);
        String partitionKey = Hive13DdlGenerator.NO_PARTITIONING_PARTITION_KEY;
        partitionColumns.get(0).setName(partitionKey);
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, partitionKey, PARTITION_KEY_GROUP,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, NO_SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), partitionColumns, false, null, true);
        createExpectedPartitionValueProcessDatesForApril2014(PARTITION_KEY_GROUP);

        // Retrieve business object data ddl without specifying custom ddl name.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(START_PARTITION_VALUE, END_PARTITION_VALUE);
        request.getPartitionValueFilters().get(0).setPartitionKey(partitionKey);
        businessObjectDataService.generateBusinessObjectDataDdl(request);

        // TODO: Validate the results.
        getExpectedDdl(partitionColumns.size(), FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
            FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, PROCESS_DATE_PARTITION_VALUES, NO_SUBPARTITION_VALUES,
            false, true, true);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoPartitioning()
    {
        // Prepare non-partitioned test business object data with custom ddl.
        List<String> partitionValues = Arrays.asList(Hive13DdlGenerator.NO_PARTITIONING_PARTITION_VALUE);
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, Hive13DdlGenerator.NO_PARTITIONING_PARTITION_KEY, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, partitionValues, NO_SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), null, false, CUSTOM_DDL_NAME, true);

        // Retrieve business object data ddl for a non-partitioned table.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        request.getPartitionValueFilters().get(0).setPartitionKey(Hive13DdlGenerator.NO_PARTITIONING_PARTITION_KEY);
        request.getPartitionValueFilters().get(0).setPartitionValues(partitionValues);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedDdl =
            getExpectedDdl(0, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT, FileTypeEntity.TXT_FILE_TYPE,
                BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, STORAGE_1_AVAILABLE_PARTITION_VALUES, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlNoPartitioning()
    {
        // Prepare test data without custom ddl.
        List<String> partitionValues = Arrays.asList(Hive13DdlGenerator.NO_PARTITIONING_PARTITION_VALUE);
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, Hive13DdlGenerator.NO_PARTITIONING_PARTITION_KEY, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, partitionValues, NO_SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), null, false, null, true);

        // Retrieve business object data ddl for a non-partitioned table and without specifying custom ddl name.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.getPartitionValueFilters().get(0).setPartitionKey(Hive13DdlGenerator.NO_PARTITIONING_PARTITION_KEY);
        request.getPartitionValueFilters().get(0).setPartitionValues(partitionValues);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedDdl =
            getExpectedDdl(0, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT, FileTypeEntity.TXT_FILE_TYPE,
                BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, NO_SUBPARTITION_VALUES, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlSubpartitionKeysHaveHyphens()
    {
        // Prepare test data with subpartition using key values with hyphens instead of underscores.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns();
        String partitionKey = partitionColumns.get(0).getName();
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, partitionKey, PARTITION_KEY_GROUP,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), partitionColumns, true, CUSTOM_DDL_NAME, true);

        // Retrieve business object data ddl.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.  Please note that we expect hyphens in subpartition key values.
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, true, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlMissingSchemaDelimiterCharacter()
    {
        // Prepare test data.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns();
        String partitionKey = partitionColumns.get(0).getName();
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, partitionKey, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, null, SCHEMA_ESCAPE_CHARACTER_BACKSLASH,
            SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), partitionColumns, false, null, true);

        // Retrieve business object data ddl.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedRowFormat = "ROW FORMAT DELIMITED NULL DEFINED AS '\\N'";
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, expectedRowFormat, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlMissingSchemaEscapeCharacter()
    {
        // Prepare test data without custom ddl.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns();
        String partitionKey = partitionColumns.get(0).getName();
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, partitionKey, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE, null,
            SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), partitionColumns, false, null, true);

        // Retrieve business object data ddl.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedRowFormat = "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' NULL DEFINED AS '\\N'";
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, expectedRowFormat, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlMissingSchemaNullValue()
    {
        // Prepare test data without custom ddl.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns();
        String partitionKey = partitionColumns.get(0).getName();
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, partitionKey, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, null, getTestSchemaColumns(), partitionColumns, false, null, true);

        // Retrieve business object data ddl.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedRowFormat = "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' ESCAPED BY '\\\\' NULL DEFINED AS ''";
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, expectedRowFormat, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlEscapeSingleQuoteInRowFormat()
    {
        // Prepare test data without custom ddl.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns();
        String partitionKey = partitionColumns.get(0).getName();
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, partitionKey, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SINGLE_QUOTE, SINGLE_QUOTE, SINGLE_QUOTE,
            getTestSchemaColumns(), partitionColumns, false, null, true);

        // Retrieve business object data ddl.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedRowFormat = "ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\'' ESCAPED BY '\\'' NULL DEFINED AS '\\''";
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, expectedRowFormat, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlEscapeBackslashInRowFormat()
    {
        // Prepare test data without custom ddl.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns();
        String partitionKey = partitionColumns.get(0).getName();
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, partitionKey, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, BACKSLASH, BACKSLASH, BACKSLASH,
            getTestSchemaColumns(), partitionColumns, false, null, true);

        // Retrieve business object data ddl.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results - please note that we do not escape single backslash in null value.
        String expectedRowFormat = "ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\\\' ESCAPED BY '\\\\' NULL DEFINED AS '\\'";
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, expectedRowFormat, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlUnprintableCharactersInRowFormat()
    {
        // Prepare test data without custom ddl.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns();
        String partitionKey = partitionColumns.get(0).getName();
        // Set schemaDelimiterCharacter to char(1), schemaEscapeCharacter to char(10), and schemaNullValue to char(128).
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, partitionKey, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, String.valueOf((char) 1),
            String.valueOf((char) 10), String.valueOf((char) 128), getTestSchemaColumns(), partitionColumns, false, null, true);

        // Retrieve business object data ddl request without business object format and data versions.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results - please note that 1 decimal = 1 octal, 10 decimal = 12 octal, and 128 decimal = 200 octal.
        String expectedRowFormat = "ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\001' ESCAPED BY '\\012' NULL DEFINED AS '\\200'";
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, expectedRowFormat, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlMissingBusinessObjectDataDoNotAllowMissingData()
    {
        // Prepare test data.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns();
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, PARTITION_KEY_GROUP,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), partitionColumns, false, CUSTOM_DDL_NAME, true);

        // Try to retrieve business object data ddl for the non-existing business object data with "allow missing data" flag set to "false".
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        request.getPartitionValueFilters().get(0).setPartitionValues(Arrays.asList("I_DO_NOT_EXIST"));
        request.setAllowMissingData(false);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an ObjectNotFoundException when business object data does not exist.");
        }
        catch (ObjectNotFoundException e)
        {
            assertEquals(String.format("Business object data {namespace: \"%s\", businessObjectDefinitionName: \"%s\", businessObjectFormatUsage: \"%s\", " +
                "businessObjectFormatFileType: \"%s\", businessObjectFormatVersion: %s, partitionValue: \"%s\", " +
                "subpartitionValues: \",,,\", businessObjectDataVersion: %d} is not available in \"%s\" storage(s).", request.getNamespace(),
                request.getBusinessObjectDefinitionName(), request.getBusinessObjectFormatUsage(), request.getBusinessObjectFormatFileType(),
                request.getBusinessObjectFormatVersion(),
                request.getPartitionValueFilters().get(0).getPartitionValues().get(request.getPartitionValueFilters().get(0).getPartitionValues().size() - 1),
                request.getBusinessObjectDataVersion(), request.getStorageName()), e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlMissingBusinessObjectDataAllowMissingDataSomeDataNoExists()
    {
        // Prepare test data.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns();
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, PARTITION_KEY_GROUP,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), partitionColumns, false, CUSTOM_DDL_NAME, true);

        // Retrieve business object data ddl when some of the business object data is not available and "allow missing data" flag is set to "true".
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        request.getPartitionValueFilters().get(0).getPartitionValues().add("I_DO_NOT_EXIST");
        assertTrue(request.isAllowMissingData());
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        validateBusinessObjectDataDdl(request, getExpectedDdl(), resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlMissingBusinessObjectDataAllowMissingDataAllDataNoExists()
    {
        // Prepare test data.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns();
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, PARTITION_KEY_GROUP,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), partitionColumns, false, CUSTOM_DDL_NAME, true);

        // Retrieve business object data ddl when all of the business object data is not available and "allow missing data" flag is set to "true".
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(Arrays.asList("I_DO_NOT_EXIST"), CUSTOM_DDL_NAME);
        assertTrue(request.isAllowMissingData());
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, null, null, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlNoPartitioningMissingBusinessObjectDataAllowMissingData()
    {
        // Prepare test data without custom ddl.  Please note that we do not use NO_PARTITIONING_PARTITION_VALUE for the test
        // business object data instance, so we can use that special value and find no data once we send a generate DDL request.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, Hive13DdlGenerator.NO_PARTITIONING_PARTITION_KEY, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, Arrays.asList(PARTITION_VALUE), NO_SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), null, false, null, true);

        // Retrieve business object data ddl for a non-partitioned table for a missing business object data instance and without specifying custom ddl name.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.getPartitionValueFilters().get(0).setPartitionKey(Hive13DdlGenerator.NO_PARTITIONING_PARTITION_KEY);
        request.getPartitionValueFilters().get(0).setPartitionValues(Arrays.asList(Hive13DdlGenerator.NO_PARTITIONING_PARTITION_VALUE));
        assertTrue(request.isAllowMissingData());
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedDdl =
            getExpectedDdl(0, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT, FileTypeEntity.TXT_FILE_TYPE,
                BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, null, null, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlForwardSlashInPartitionColumnName()
    {
        // Prepare test data without custom ddl.
        String invalidPartitionColumnName = "INVALID_/_PRTN_CLMN";
        List<SchemaColumn> partitionColumns = getTestPartitionColumns();
        partitionColumns.get(0).setName(invalidPartitionColumnName);
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, PARTITION_KEY, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), partitionColumns, false, null, true);

        // Try to retrieve business object data ddl for the format that uses unsupported schema column data type.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.getPartitionValueFilters().get(0).setPartitionKey(invalidPartitionColumnName);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when partition column name contains a '/' character.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Partition column name \"%s\" can not contain a '/' character. Business object format: " +
                "{namespace: \"%s\", businessObjectDefinitionName: \"%s\", businessObjectFormatUsage: \"%s\", " +
                "businessObjectFormatFileType: \"%s\", businessObjectFormatVersion: %d}", invalidPartitionColumnName, request.getNamespace(),
                request.getBusinessObjectDefinitionName(), request.getBusinessObjectFormatUsage(), request.getBusinessObjectFormatFileType(),
                request.getBusinessObjectFormatVersion()), e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlNotSupportedSchemaColumnDataType()
    {
        // Prepare test data without custom ddl.
        List<SchemaColumn> schemaColumns = getTestSchemaColumns();
        SchemaColumn schemaColumn = new SchemaColumn();
        schemaColumns.add(schemaColumn);
        schemaColumn.setName("COLUMN");
        schemaColumn.setType("UNKNOWN");
        String partitionKey = schemaColumns.get(0).getName();
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, partitionKey, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, schemaColumns, getTestPartitionColumns(), false, null, true);

        // Try to retrieve business object data ddl for the format that uses unsupported schema column data type.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when business object format has a column with an unsupported data type.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Column \"%s\" has an unsupported data type \"%s\" in the schema for business object format " +
                "{namespace: \"%s\", businessObjectDefinitionName: \"%s\", businessObjectFormatUsage: \"%s\", " +
                "businessObjectFormatFileType: \"%s\", businessObjectFormatVersion: %d}.", schemaColumn.getName(), schemaColumn.getType(),
                request.getNamespace(), request.getBusinessObjectDefinitionName(), request.getBusinessObjectFormatUsage(),
                request.getBusinessObjectFormatFileType(), request.getBusinessObjectFormatVersion()), e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlAllKnownFileTypes()
    {
        // Create an S3 storage entity with a "bucket.name" attribute.
        StorageEntity storageEntity =
            createStorageEntity(STORAGE_NAME, StoragePlatformEntity.S3, configurationHelper.getProperty(ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME),
                S3_BUCKET_NAME);

        // Expected business object format file type to Hive file format mapping.
        HashMap<String, String> businessObjectFormatFileTypeMap = new HashMap<>();
        businessObjectFormatFileTypeMap.put(FileTypeEntity.BZ_FILE_TYPE, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT);
        businessObjectFormatFileTypeMap.put(FileTypeEntity.GZ_FILE_TYPE, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT);
        businessObjectFormatFileTypeMap.put(FileTypeEntity.ORC_FILE_TYPE, Hive13DdlGenerator.ORC_HIVE_FILE_FORMAT);
        businessObjectFormatFileTypeMap.put(FileTypeEntity.PARQUET_FILE_TYPE, Hive13DdlGenerator.PARQUET_HIVE_FILE_FORMAT);
        businessObjectFormatFileTypeMap.put(FileTypeEntity.TXT_FILE_TYPE, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT);

        for (String businessObjectFormatFileType : businessObjectFormatFileTypeMap.keySet())
        {
            // Prepare test data for the respective business object format file type.
            List<SchemaColumn> partitionColumns = getTestPartitionColumns().subList(0, 1);
            String partitionKey = partitionColumns.get(0).getName();
            BusinessObjectFormatEntity businessObjectFormatEntity =
                createBusinessObjectFormatEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, businessObjectFormatFileType, FORMAT_VERSION, FORMAT_DESCRIPTION,
                    LATEST_VERSION_FLAG_SET, partitionKey, NO_PARTITION_KEY_GROUP, NO_ATTRIBUTES, SCHEMA_DELIMITER_PIPE, SCHEMA_ESCAPE_CHARACTER_BACKSLASH,
                    SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), partitionColumns);

            for (String partitionValue : STORAGE_1_AVAILABLE_PARTITION_VALUES)
            {
                BusinessObjectDataEntity businessObjectDataEntity =
                    createBusinessObjectDataEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, businessObjectFormatFileType, FORMAT_VERSION, partitionValue,
                        NO_SUBPARTITION_VALUES, DATA_VERSION, true, BDATA_STATUS);
                String s3KeyPrefix =
                    businessObjectDataHelper.buildS3KeyPrefix(businessObjectFormatEntity, dmDaoHelper.getBusinessObjectDataKey(businessObjectDataEntity));
                StorageUnitEntity storageUnitEntity = createStorageUnitEntity(storageEntity, businessObjectDataEntity);

                // Create two storage files.
                for (int i = 0; i < 2; i++)
                {
                    createStorageFileEntity(storageUnitEntity, String.format("%s/data%d.dat", s3KeyPrefix, i), FILE_SIZE_1_KB, ROW_COUNT_1000);
                }

                dmDao.saveAndRefresh(storageUnitEntity);
                dmDao.saveAndRefresh(businessObjectDataEntity);
            }

            // Retrieve business object data ddl.
            BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
            request.setBusinessObjectFormatFileType(businessObjectFormatFileType);
            BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

            // Validate the results.
            String expectedHiveFileFormat = businessObjectFormatFileTypeMap.get(businessObjectFormatFileType);
            String expectedDdl = getExpectedDdl(partitionColumns.size(), FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, expectedHiveFileFormat,
                businessObjectFormatFileType, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, true, true);
            validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoCustomDdlNotSupportedFileType()
    {
        // Prepare test data without custom ddl.
        String businessObjectFileType = "UNKNOWN";
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(businessObjectFileType, PARTITION_KEY, null,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), getTestPartitionColumns(), false, null, true);

        // Try to retrieve business object data ddl for the format without custom ddl and that uses unsupported file type.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.setBusinessObjectFormatFileType(businessObjectFileType);
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(request);
            fail("Should throw an IllegalArgumentException when business object format has an unsupported file type.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Unsupported format file type for business object format {namespace: \"%s\", businessObjectDefinitionName: \"%s\", " +
                "businessObjectFormatUsage: \"%s\", businessObjectFormatFileType: \"%s\", businessObjectFormatVersion: %d}.", request.getNamespace(),
                request.getBusinessObjectDefinitionName(), request.getBusinessObjectFormatUsage(), request.getBusinessObjectFormatFileType(),
                request.getBusinessObjectFormatVersion()), e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlNoDropTable()
    {
        // Prepare test data without custom ddl.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, PARTITION_KEY_GROUP,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), getTestPartitionColumns(), false, CUSTOM_DDL_NAME, true);

        // Retrieve business object data ddl request without drop table statement.
        BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES);
        request.setIncludeDropTableStatement(false);
        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, false, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlFilterOnSubPartitionValues()
    {
        List<SchemaColumn> columns = getTestSchemaColumns();
        List<SchemaColumn> partitionColumns = getTestPartitionColumns();

        // Test generate business object data ddl using primary partition and each of the available subpartition columns.
        for (int i = 0; i < Math.min(BusinessObjectDataEntity.MAX_SUBPARTITIONS + 1, PARTITION_COLUMNS.length); i++)
        {
            // Prepare test data without custom ddl.
            createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, PARTITION_KEY_GROUP, i + 1,
                STORAGE_1_AVAILABLE_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE, SCHEMA_ESCAPE_CHARACTER_BACKSLASH,
                SCHEMA_NULL_VALUE_BACKSLASH_N, columns, partitionColumns, false, null, true);

            // Retrieve business object data ddl request without drop table statement.
            BusinessObjectDataDdlRequest request = getTestBusinessObjectDataDdlRequest(STORAGE_1_AVAILABLE_PARTITION_VALUES);
            request.getPartitionValueFilters().get(0).setPartitionKey(partitionColumns.get(i).getName());
            request.setIncludeDropPartitions(INCLUDE_DROP_PARTITIONS);
            request.setAllowMissingData(NO_ALLOW_MISSING_DATA);
            BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

            // Validate the results.
            String expectedDdl =
                getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                    FileTypeEntity.TXT_FILE_TYPE, i + 1, STORAGE_1_AVAILABLE_PARTITION_VALUES, SUBPARTITION_VALUES, false, true, true, INCLUDE_DROP_PARTITIONS);
            validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlUsingStorageDirectoriesNoAutoDiscovery()
    {
        // Prepare test data with storage units having no storage files, but only the relative storage directory path values.
        // For auto-discovery not to occur, number of partition columns is equal to the number of partition values.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns().subList(0, 1 + SUBPARTITION_VALUES.size());
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, PARTITION_KEY_GROUP,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), partitionColumns, false, CUSTOM_DDL_NAME, false);

        BusinessObjectDataDdlRequest request;
        BusinessObjectDataDdl resultDdl;

        // Retrieve business object data ddl.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        String expectedDdl =
            getExpectedDdl(partitionColumns.size(), FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, true, true);
        validateBusinessObjectDataDdl(request, expectedDdl, resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlStorageDirectoryMismatchesS3KeyPrefix()
    {
        // Prepare test data with a storage unit having no storage files and storage directory path value not matching the expected S3 key prefix.
        String invalidS3KeyPrefix = "INVALID_S3_KEY_PREFIX";
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(PARTITION_VALUE, invalidS3KeyPrefix);

        // Try to retrieve business object data ddl when storage unit has no storage
        // files and storage directory path not matching the expected S3 key prefix.
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(
                new BusinessObjectDataDdlRequest(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, Arrays.asList(
                    new PartitionValueFilter(PARTITION_KEY, Arrays.asList(PARTITION_VALUE), NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
                        NO_LATEST_AFTER_PARTITION_VALUE)), NO_STANDALONE_PARTITION_VALUE_FILTER, DATA_VERSION, NO_STORAGE_NAMES, STORAGE_NAME,
                    BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, INCLUDE_DROP_TABLE_STATEMENT,
                    INCLUDE_IF_NOT_EXISTS_OPTION, NO_INCLUDE_DROP_PARTITIONS, NO_ALLOW_MISSING_DATA));
            fail("Should throw an IllegalArgumentException when storage directory path does not match the expected S3 key prefix.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Storage directory path \"%s\" registered with business object data {%s} " +
                "in \"%s\" storage does not match the expected S3 key prefix \"%s\".", invalidS3KeyPrefix,
                getExpectedBusinessObjectDataKeyAsString(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION,
                    PARTITION_VALUE, NO_SUBPARTITION_VALUES, DATA_VERSION), STORAGE_NAME,
                getExpectedS3KeyPrefix(NAMESPACE_CD, DATA_PROVIDER_NAME, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION,
                    PARTITION_KEY, PARTITION_VALUE, null, null, DATA_VERSION)), e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlStorageDirectoryIsNull()
    {
        // Prepare test data with a storage unit having no storage files and storage directory path is null.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(PARTITION_VALUE, null);

        // Try to retrieve business object data ddl when storage unit has no storage files and storage directory path is null.
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(
                new BusinessObjectDataDdlRequest(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, Arrays.asList(
                    new PartitionValueFilter(PARTITION_KEY, Arrays.asList(PARTITION_VALUE), NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
                        NO_LATEST_AFTER_PARTITION_VALUE)), NO_STANDALONE_PARTITION_VALUE_FILTER, DATA_VERSION, NO_STORAGE_NAMES, STORAGE_NAME,
                    BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, INCLUDE_DROP_TABLE_STATEMENT,
                    INCLUDE_IF_NOT_EXISTS_OPTION, NO_INCLUDE_DROP_PARTITIONS, NO_ALLOW_MISSING_DATA));
            fail("Should throw an IllegalArgumentException when storage directory path is null.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Storage directory path \"%s\" registered with business object data {%s} " +
                "in \"%s\" storage does not match the expected S3 key prefix \"%s\".", null,
                getExpectedBusinessObjectDataKeyAsString(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION,
                    PARTITION_VALUE, NO_SUBPARTITION_VALUES, DATA_VERSION), STORAGE_NAME,
                getExpectedS3KeyPrefix(NAMESPACE_CD, DATA_PROVIDER_NAME, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION,
                    PARTITION_KEY, PARTITION_VALUE, null, null, DATA_VERSION)), e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlLatestFormatVersionDataNotAvailableInStorage()
    {
        // Create database entities for two versions of a business object format.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns().subList(0, 1 + SUBPARTITION_VALUES.size());
        BusinessObjectFormatEntity businessObjectFormatV0Entity =
            createBusinessObjectFormatEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, INITIAL_FORMAT_VERSION,
                FORMAT_DESCRIPTION, NO_LATEST_VERSION_FLAG_SET, FIRST_PARTITION_COLUMN_NAME, PARTITION_KEY_GROUP, NO_ATTRIBUTES, SCHEMA_DELIMITER_PIPE,
                SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), partitionColumns);
        BusinessObjectFormatEntity businessObjectFormatV1Entity =
            createBusinessObjectFormatEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, SECOND_FORMAT_VERSION, FORMAT_DESCRIPTION,
                LATEST_VERSION_FLAG_SET, FIRST_PARTITION_COLUMN_NAME, PARTITION_KEY_GROUP, NO_ATTRIBUTES, SCHEMA_DELIMITER_PIPE,
                SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), partitionColumns);

        // Create two storage entities.
        StorageEntity storage1Entity = dmDao.getStorageByName(StorageEntity.MANAGED_STORAGE);
        StorageEntity storage2Entity = createStorageEntity(STORAGE_NAME_2);

        // Register initial version of the business object data for initial format version in both storages.
        BusinessObjectDataEntity businessObjectDataV0V0Entity =
            createBusinessObjectDataEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, INITIAL_FORMAT_VERSION, PARTITION_VALUE,
                SUBPARTITION_VALUES, INITIAL_DATA_VERSION, true, BDATA_STATUS);

        String s3KeyPrefix =
            businessObjectDataHelper.buildS3KeyPrefix(businessObjectFormatV0Entity, dmDaoHelper.getBusinessObjectDataKey(businessObjectDataV0V0Entity));

        for (StorageEntity storageEntity : Arrays.asList(storage1Entity, storage2Entity))
        {
            StorageUnitEntity storageUnitEntity = createStorageUnitEntity(storageEntity, businessObjectDataV0V0Entity);
            storageUnitEntity.setDirectoryPath(s3KeyPrefix);
            dmDao.saveAndRefresh(storageUnitEntity);
        }
        dmDao.saveAndRefresh(businessObjectDataV0V0Entity);

        // Register initial version of the business object data for second format version, but only in the second storage.
        BusinessObjectDataEntity businessObjectDataV1V0Entity =
            createBusinessObjectDataEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, SECOND_FORMAT_VERSION, PARTITION_VALUE,
                SUBPARTITION_VALUES, INITIAL_DATA_VERSION, true, BDATA_STATUS);

        s3KeyPrefix =
            businessObjectDataHelper.buildS3KeyPrefix(businessObjectFormatV1Entity, dmDaoHelper.getBusinessObjectDataKey(businessObjectDataV1V0Entity));

        StorageUnitEntity storageUnitEntity = createStorageUnitEntity(storage2Entity, businessObjectDataV1V0Entity);
        storageUnitEntity.setDirectoryPath(s3KeyPrefix);
        dmDao.saveAndRefresh(storageUnitEntity);
        dmDao.saveAndRefresh(businessObjectDataV1V0Entity);

        // Retrieve business object data ddl for the first storage without specifying business object format version.
        BusinessObjectDataDdlRequest request = new BusinessObjectDataDdlRequest();

        request.setNamespace(NAMESPACE_CD);
        request.setBusinessObjectDefinitionName(BOD_NAME);
        request.setBusinessObjectFormatUsage(FORMAT_USAGE_CODE);
        request.setBusinessObjectFormatFileType(FileTypeEntity.TXT_FILE_TYPE);
        request.setBusinessObjectFormatVersion(null);

        PartitionValueFilter partitionValueFilter = new PartitionValueFilter();
        request.setPartitionValueFilter(partitionValueFilter);
        partitionValueFilter.setPartitionKey(FIRST_PARTITION_COLUMN_NAME);
        partitionValueFilter.setPartitionValues(Arrays.asList(PARTITION_VALUE));

        request.setBusinessObjectDataVersion(INITIAL_DATA_VERSION);
        request.setStorageName(StorageEntity.MANAGED_STORAGE);
        request.setOutputFormat(BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL);
        request.setTableName(TABLE_NAME);
        request.setCustomDdlName(null);
        request.setIncludeDropTableStatement(true);

        BusinessObjectDataDdl resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        assertNotNull(resultDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlLargePartitionValueListPrimaryPartitionOnly()
    {
        final int PRIMARY_PARTITION_VALUES_SIZE = 10000;

        // Prepare test data.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting();

        BusinessObjectDataDdlRequest request;
        BusinessObjectDataDdl resultDdl;

        // Retrieve business object data ddl by passing a large set of partition values.
        List<String> partitionValues = new ArrayList<>();
        for (int i = 0; i < PRIMARY_PARTITION_VALUES_SIZE; i++)
        {
            partitionValues.add(String.format("%s-%s", PARTITION_VALUE, i));
        }
        partitionValues.addAll(UNSORTED_PARTITION_VALUES);
        request = getTestBusinessObjectDataDdlRequest(partitionValues, CUSTOM_DDL_NAME);
        resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        validateBusinessObjectDataDdl(request, getExpectedDdl(), resultDdl);

        // Retrieve business object data ddl when request partition value list has duplicate values.
        request = getTestBusinessObjectDataDdlRequest(UNSORTED_PARTITION_VALUES, CUSTOM_DDL_NAME);
        request.getPartitionValueFilters().get(0).getPartitionValues().add(UNSORTED_PARTITION_VALUES.get(0));
        resultDdl = businessObjectDataService.generateBusinessObjectDataDdl(request);

        // Validate the results.
        validateBusinessObjectDataDdl(request, getExpectedDdl(), resultDdl);
    }

    @Test
    @Ignore
    public void testGenerateBusinessObjectDataDdlLargePartitionValueListWithAutoDiscovery()
    {
        final int PRIMARY_PARTITION_VALUE_LIST_SIZE = 10000;
        final int SECOND_LEVEL_PARTITION_VALUES_PER_BUSINESS_OBJECT_DATA = 1;
        final int STORAGE_FILES_PER_PARTITION = 1;

        // Prepare test data and build a list of partition values to generate business object data DDL for.

        logger.info(String.format("Preparing test data for %d partition values ...", PRIMARY_PARTITION_VALUE_LIST_SIZE));

        // Build a list of schema columns.
        List<SchemaColumn> schemaColumns = new ArrayList<>();
        schemaColumns.add(new SchemaColumn(PARTITION_KEY, "DATE", NO_COLUMN_SIZE, COLUMN_REQUIRED, NO_COLUMN_DEFAULT_VALUE, NO_COLUMN_DESCRIPTION));
        schemaColumns.add(new SchemaColumn(COLUMN_NAME, "NUMBER", COLUMN_SIZE, NO_COLUMN_REQUIRED, NO_COLUMN_DEFAULT_VALUE, COLUMN_DESCRIPTION));
        schemaColumns.add(new SchemaColumn(COLUMN_NAME_2, "STRING", NO_COLUMN_SIZE, NO_COLUMN_REQUIRED, NO_COLUMN_DEFAULT_VALUE, NO_COLUMN_DESCRIPTION));

        // Use the first two columns as partition columns.
        List<SchemaColumn> partitionColumns = schemaColumns.subList(0, 2);

        // Create a business object format entity with the schema.
        BusinessObjectFormatEntity businessObjectFormatEntity =
            createBusinessObjectFormatEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, FORMAT_DESCRIPTION,
                LATEST_VERSION_FLAG_SET, PARTITION_KEY, NO_PARTITION_KEY_GROUP, NO_ATTRIBUTES, SCHEMA_DELIMITER_PIPE, SCHEMA_ESCAPE_CHARACTER_BACKSLASH,
                SCHEMA_NULL_VALUE_BACKSLASH_N, schemaColumns, partitionColumns);

        // Create an S3 storage entity.
        StorageEntity storageEntity = createStorageEntity(STORAGE_NAME, StoragePlatformEntity.S3);

        // Add a bucket name attribute to the storage.
        createStorageAttributeEntity(storageEntity, configurationHelper.getProperty(ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME), S3_BUCKET_NAME);
        dmDao.saveAndRefresh(storageEntity);

        // Create relative business object data, storage unit, and storage file entities.
        List<String> partitionValues = new ArrayList<>();
        for (int i = 0; i < PRIMARY_PARTITION_VALUE_LIST_SIZE; i++)
        {
            String partitionValue = String.format("%s-%03d", PARTITION_VALUE, i);
            partitionValues.add(partitionValue);

            logger.info(String.format("Creating database entities for partition value = \"%s\" ...", partitionValue));

            // Create a business object data entity.
            BusinessObjectDataEntity businessObjectDataEntity =
                createBusinessObjectDataEntity(businessObjectFormatEntity, partitionValue, NO_SUBPARTITION_VALUES, DATA_VERSION, true,
                    BusinessObjectDataStatusEntity.VALID);

            // Build an S3 key prefix according to the Data Management S3 naming convention.
            String s3KeyPrefix =
                getExpectedS3KeyPrefix(NAMESPACE_CD, DATA_PROVIDER_NAME, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION,
                    PARTITION_KEY, partitionValue, null, null, DATA_VERSION);

            // Create a storage unit with a storage directory path.
            StorageUnitEntity storageUnitEntity = createStorageUnitEntity(storageEntity, businessObjectDataEntity, s3KeyPrefix);

            // Create storage file entities.
            for (int j = 0; j < SECOND_LEVEL_PARTITION_VALUES_PER_BUSINESS_OBJECT_DATA; j++)
            {
                // Build a storage file directory path that includes the relative second level partition value - needed for auto discovery.
                String storageFileDirectoryPath = String.format("%s/%s=%s-%03d", s3KeyPrefix, COLUMN_NAME, PARTITION_VALUE_2, j);

                for (int k = 0; k < STORAGE_FILES_PER_PARTITION; k++)
                {
                    String storageFilePath = String.format("%s/%03d.data", storageFileDirectoryPath, k);
                    createStorageFileEntity(storageUnitEntity, storageFilePath, FILE_SIZE_1_KB, ROW_COUNT_1000);
                }
            }

            dmDao.saveAndRefresh(storageUnitEntity);
        }

        logger.info(String.format("Calling businessObjectDataService.generateBusinessObjectDataDdl() ..."));

        // Retrieve business object data ddl for the entire list of partition values.
        BusinessObjectDataDdl businessObjectDataDdl = businessObjectDataService.generateBusinessObjectDataDdl(
            new BusinessObjectDataDdlRequest(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, Arrays.asList(
                new PartitionValueFilter(PARTITION_KEY, partitionValues, NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
                    NO_LATEST_AFTER_PARTITION_VALUE)), NO_STANDALONE_PARTITION_VALUE_FILTER, DATA_VERSION, NO_STORAGE_NAMES, STORAGE_NAME,
                BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, INCLUDE_DROP_TABLE_STATEMENT, INCLUDE_IF_NOT_EXISTS_OPTION,
                NO_INCLUDE_DROP_PARTITIONS, NO_ALLOW_MISSING_DATA));

        // Validate the results.
        assertNotNull(businessObjectDataDdl);
    }

    @Test
    public void testGenerateBusinessObjectDataDdlLatestBeforePartitionValueNoExists()
    {
        // Prepare database entities required for testing.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(PARTITION_VALUE_2);

        // Try to retrieve business object data ddl using a latest before partition value filter option when the latest partition value does not exist.
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(
                new BusinessObjectDataDdlRequest(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, Arrays.asList(
                    new PartitionValueFilter(PARTITION_KEY, NO_PARTITION_VALUES, NO_PARTITION_VALUE_RANGE, new LatestBeforePartitionValue(PARTITION_VALUE),
                        NO_LATEST_AFTER_PARTITION_VALUE)), NO_STANDALONE_PARTITION_VALUE_FILTER, DATA_VERSION, NO_STORAGE_NAMES, STORAGE_NAME,
                    BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, INCLUDE_DROP_TABLE_STATEMENT,
                    INCLUDE_IF_NOT_EXISTS_OPTION, NO_INCLUDE_DROP_PARTITIONS, NO_ALLOW_MISSING_DATA));
            fail("Suppose to throw an ObjectNotFoundException when failed to find the latest before partition value.");
        }
        catch (ObjectNotFoundException e)
        {
            assertEquals(String.format("Failed to find partition value which is the latest before partition value = \"%s\" " +
                "for partition key = \"%s\" due to no available business object data " +
                "in \"%s\" storage that satisfies the search criteria. Business object data {namespace: \"%s\", " +
                "businessObjectDefinitionName: \"%s\", businessObjectFormatUsage: \"%s\", businessObjectFormatFileType: \"%s\", " +
                "businessObjectFormatVersion: %d, businessObjectDataVersion: %d}", PARTITION_VALUE, PARTITION_KEY, STORAGE_NAME, NAMESPACE_CD, BOD_NAME,
                FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, DATA_VERSION), e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlLatestAfterPartitionValueNoExists()
    {
        // Prepare database entities required for testing.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(PARTITION_VALUE);

        // Try retrieve business object data ddl using a latest before partition value filter option when the latest partition value does not exist.
        try
        {
            businessObjectDataService.generateBusinessObjectDataDdl(
                new BusinessObjectDataDdlRequest(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, Arrays.asList(
                    new PartitionValueFilter(PARTITION_KEY, NO_PARTITION_VALUES, NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
                        new LatestAfterPartitionValue(PARTITION_VALUE_2))), NO_STANDALONE_PARTITION_VALUE_FILTER, DATA_VERSION, NO_STORAGE_NAMES, STORAGE_NAME,
                    BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, INCLUDE_DROP_TABLE_STATEMENT,
                    INCLUDE_IF_NOT_EXISTS_OPTION, NO_INCLUDE_DROP_PARTITIONS, NO_ALLOW_MISSING_DATA));
            fail("Suppose to throw an ObjectNotFoundException when failed to find the latest after partition value.");
        }
        catch (ObjectNotFoundException e)
        {
            assertEquals(String.format("Failed to find partition value which is the latest after partition value = \"%s\" " +
                "for partition key = \"%s\" due to no available business object data " +
                "in \"%s\" storage that satisfies the search criteria. Business object data {namespace: \"%s\", " +
                "businessObjectDefinitionName: \"%s\", businessObjectFormatUsage: \"%s\", businessObjectFormatFileType: \"%s\", " +
                "businessObjectFormatVersion: %d, businessObjectDataVersion: %d}", PARTITION_VALUE_2, PARTITION_KEY, STORAGE_NAME, NAMESPACE_CD, BOD_NAME,
                FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, DATA_VERSION), e.getMessage());
        }
    }

    @Test
    public void testGenerateBusinessObjectDataDdlMultipleStorages()
    {
        // Prepare database entities required for testing.
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, PARTITION_KEY_GROUP,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), getTestPartitionColumns(), false, CUSTOM_DDL_NAME, true);

        // Retrieve business object data ddl for data located in multiple storages.
        BusinessObjectDataDdl resultBusinessObjectDataDdl = businessObjectDataService.generateBusinessObjectDataDdl(
            new BusinessObjectDataDdlRequest(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, Arrays.asList(
                new PartitionValueFilter(FIRST_PARTITION_COLUMN_NAME, UNSORTED_PARTITION_VALUES, NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
                    NO_LATEST_AFTER_PARTITION_VALUE)), NO_STANDALONE_PARTITION_VALUE_FILTER, DATA_VERSION, STORAGE_NAMES, NO_STORAGE_NAME,
                BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, INCLUDE_DROP_TABLE_STATEMENT, INCLUDE_IF_NOT_EXISTS_OPTION,
                NO_INCLUDE_DROP_PARTITIONS, ALLOW_MISSING_DATA));

        // Validate the response object.
        String expectedDdl =
            getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
                FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, MULTI_STORAGE_AVAILABLE_PARTITION_VALUES,
                SUBPARTITION_VALUES, false, true, true);
        assertEquals(new BusinessObjectDataDdl(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, Arrays.asList(
            new PartitionValueFilter(FIRST_PARTITION_COLUMN_NAME, UNSORTED_PARTITION_VALUES, NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
                NO_LATEST_AFTER_PARTITION_VALUE)), NO_STANDALONE_PARTITION_VALUE_FILTER, DATA_VERSION, STORAGE_NAMES, NO_STORAGE_NAME,
            BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, expectedDdl), resultBusinessObjectDataDdl);
    }
}
