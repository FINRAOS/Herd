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
package org.finra.herd.service.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import org.finra.herd.core.helper.ConfigurationHelper;
import org.finra.herd.dao.StorageFileDao;
import org.finra.herd.dao.StorageUnitDao;
import org.finra.herd.model.ObjectNotFoundException;
import org.finra.herd.model.api.xml.BusinessObjectDataDdlOutputFormatEnum;
import org.finra.herd.model.api.xml.BusinessObjectDataDdlRequest;
import org.finra.herd.model.api.xml.BusinessObjectDataKey;
import org.finra.herd.model.api.xml.BusinessObjectFormat;
import org.finra.herd.model.api.xml.BusinessObjectFormatDdlRequest;
import org.finra.herd.model.api.xml.BusinessObjectFormatKey;
import org.finra.herd.model.api.xml.SchemaColumn;
import org.finra.herd.model.dto.ConfigurationValue;
import org.finra.herd.model.dto.HivePartitionDto;
import org.finra.herd.model.jpa.BusinessObjectDataEntity;
import org.finra.herd.model.jpa.BusinessObjectDataStatusEntity;
import org.finra.herd.model.jpa.BusinessObjectFormatEntity;
import org.finra.herd.model.jpa.CustomDdlEntity;
import org.finra.herd.model.jpa.FileTypeEntity;
import org.finra.herd.model.jpa.StorageEntity;
import org.finra.herd.model.jpa.StoragePlatformEntity;
import org.finra.herd.model.jpa.StorageUnitEntity;

/**
 * The DDL generator for Hive 13.
 */
@Component
@SuppressFBWarnings(value = "VA_FORMAT_STRING_USES_NEWLINE", justification = "We will use the standard carriage return character.")
public class Hive13DdlGenerator extends DdlGenerator
{
    /**
     * The partition key value for business object data without partitioning.
     */
    public static final String NO_PARTITIONING_PARTITION_KEY = "partition";

    /**
     * The partition value for business object data without partitioning.
     */
    public static final String NO_PARTITIONING_PARTITION_VALUE = "none";

    /**
     * Hive file format for ORC files.
     */
    public static final String ORC_HIVE_FILE_FORMAT = "ORC";

    /**
     * Hive file format for PARQUET files.
     */
    public static final String PARQUET_HIVE_FILE_FORMAT = "PARQUET";

    /**
     * Hive file format for text files.
     */
    public static final String TEXT_HIVE_FILE_FORMAT = "TEXTFILE";

    @Autowired
    private BusinessObjectDataDaoHelper businessObjectDataDaoHelper;

    @Autowired
    private BusinessObjectDataHelper businessObjectDataHelper;

    @Autowired
    private BusinessObjectFormatHelper businessObjectFormatHelper;

    @Autowired
    private ConfigurationHelper configurationHelper;

    @Autowired
    private S3KeyPrefixHelper s3KeyPrefixHelper;

    @Autowired
    private StorageFileDao storageFileDao;

    @Autowired
    private StorageFileHelper storageFileHelper;

    @Autowired
    private StorageHelper storageHelper;

    @Autowired
    private StorageUnitDao storageUnitDao;

    @Autowired
    private StorageUnitHelper storageUnitHelper;

    /**
     * Escapes single quote characters, if not already escaped, with an extra backslash.
     *
     * @param string the input text
     *
     * @return the output text with all single quote characters escaped by an extra backslash
     */
    public String escapeSingleQuotes(String string)
    {
        Pattern pattern = Pattern.compile("(?<!\\\\)(')");
        Matcher matcher = pattern.matcher(string);
        StringBuffer stringBuffer = new StringBuffer();

        while (matcher.find())
        {
            matcher.appendReplacement(stringBuffer, matcher.group(1).replace("'", "\\\\'"));
        }
        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }

    /**
     * Generates the create table Hive 13 DDL as per specified business object data DDL request.
     *
     * @param request the business object data DDL request
     * @param businessObjectFormatEntity the business object format entity
     * @param customDdlEntity the optional custom DDL entity
     * @param storageNames the list of storage names
     * @param storageEntities the list of storage entities
     * @param s3BucketNames the map of storage entities to the relative S3 bucket names
     *
     * @return the create table Hive DDL
     */
    @Override
    public String generateCreateTableDdl(BusinessObjectDataDdlRequest request, BusinessObjectFormatEntity businessObjectFormatEntity,
        CustomDdlEntity customDdlEntity, List<String> storageNames, List<StorageEntity> storageEntities, Map<StorageEntity, String> s3BucketNames)
    {
        // Get business object format key from the request.
        BusinessObjectFormatKey businessObjectFormatKey =
            new BusinessObjectFormatKey(request.getNamespace(), request.getBusinessObjectDefinitionName(), request.getBusinessObjectFormatUsage(),
                request.getBusinessObjectFormatFileType(), request.getBusinessObjectFormatVersion());

        // Build partition filters based on the specified partition value filters.
        // We do validate that all specified storages are of "S3" storage platform type, so we specify S3 storage platform type in
        // the call below, so we select storage units only from all S3 storages, when the specified list of storages is empty.
        List<List<String>> partitionFilters = businessObjectDataDaoHelper
            .buildPartitionFilters(request.getPartitionValueFilters(), request.getPartitionValueFilter(), businessObjectFormatKey,
                request.getBusinessObjectDataVersion(), storageNames, StoragePlatformEntity.S3, null, businessObjectFormatEntity);

        // If the partitionKey="partition" and partitionValue="none", then DDL should
        // return a DDL which treats business object data as a table, not a partition.
        boolean isPartitioned = !businessObjectFormatEntity.getPartitionKey().equalsIgnoreCase(NO_PARTITIONING_PARTITION_KEY) ||
            partitionFilters.size() != 1 ||
            !partitionFilters.get(0).get(0).equalsIgnoreCase(NO_PARTITIONING_PARTITION_VALUE);

        // Generate the create table Hive 13 DDL.
        GenerateDdlRequest generateDdlRequest = new GenerateDdlRequest();
        generateDdlRequest.allowMissingData = request.isAllowMissingData();
        generateDdlRequest.businessObjectDataVersion = request.getBusinessObjectDataVersion();
        generateDdlRequest.businessObjectFormatEntity = businessObjectFormatEntity;
        generateDdlRequest.businessObjectFormatVersion = request.getBusinessObjectFormatVersion();
        generateDdlRequest.customDdlEntity = customDdlEntity;
        generateDdlRequest.includeAllRegisteredSubPartitions = request.isIncludeAllRegisteredSubPartitions();
        generateDdlRequest.includeDropPartitions = request.isIncludeDropPartitions();
        generateDdlRequest.includeDropTableStatement = request.isIncludeDropTableStatement();
        generateDdlRequest.includeIfNotExistsOption = request.isIncludeIfNotExistsOption();
        generateDdlRequest.isPartitioned = isPartitioned;
        generateDdlRequest.partitionFilters = partitionFilters;
        generateDdlRequest.s3BucketNames = s3BucketNames;
        generateDdlRequest.storageEntities = storageEntities;
        generateDdlRequest.storageNames = storageNames;
        generateDdlRequest.suppressScanForUnregisteredSubPartitions = request.isSuppressScanForUnregisteredSubPartitions();
        generateDdlRequest.tableName = request.getTableName();
        return generateCreateTableDdlHelper(generateDdlRequest);
    }

    /**
     * Generates the create table Hive 13 DDL as per specified business object format DDL request.
     *
     * @param request the business object format DDL request
     * @param businessObjectFormatEntity the business object format entity
     * @param customDdlEntity the optional custom DDL entity
     *
     * @return the create table Hive DDL
     */
    @Override
    public String generateCreateTableDdl(BusinessObjectFormatDdlRequest request, BusinessObjectFormatEntity businessObjectFormatEntity,
        CustomDdlEntity customDdlEntity)
    {
        // If the partitionKey="partition", then DDL should return a DDL which treats business object data as a table, not a partition.
        Boolean isPartitioned = !businessObjectFormatEntity.getPartitionKey().equalsIgnoreCase(NO_PARTITIONING_PARTITION_KEY);

        // Generate the create table Hive 13 DDL.
        GenerateDdlRequest generateDdlRequest = new GenerateDdlRequest();
        generateDdlRequest.businessObjectFormatEntity = businessObjectFormatEntity;
        generateDdlRequest.customDdlEntity = customDdlEntity;
        generateDdlRequest.isPartitioned = isPartitioned;
        generateDdlRequest.tableName = request.getTableName();
        generateDdlRequest.includeDropTableStatement = request.isIncludeDropTableStatement();
        generateDdlRequest.includeIfNotExistsOption = request.isIncludeIfNotExistsOption();
        return generateCreateTableDdlHelper(generateDdlRequest);
    }

    @Override
    public String generateReplaceColumnsStatement(BusinessObjectFormatDdlRequest request, BusinessObjectFormatEntity businessObjectFormatEntity)
    {
        BusinessObjectFormat businessObjectFormat = businessObjectFormatHelper.createBusinessObjectFormatFromEntity(businessObjectFormatEntity);

        assertSchemaColumnsNotEmpty(businessObjectFormat, businessObjectFormatEntity);

        StringBuilder builder = new StringBuilder(34);
        builder.append("ALTER TABLE `");
        builder.append(request.getTableName());
        builder.append("` REPLACE COLUMNS (\n");
        builder.append(generateDdlColumns(businessObjectFormatEntity, businessObjectFormat));
        return builder.toString().trim() + ';';
    }

    /**
     * Gets the DDL character value based on the specified configured character value. This method supports UTF-8 encoded strings and will "Hive" escape any
     * non-ASCII printable characters using '\(value)'.
     *
     * @param string the configured character value.
     *
     * @return the DDL character value.
     */
    public String getDdlCharacterValue(String string)
    {
        return getDdlCharacterValue(string, false);
    }

    /**
     * Gets the DDL character value based on the specified configured character value. This method supports UTF-8 encoded strings and will "Hive" escape any
     * non-ASCII printable characters using '\(value)'.
     *
     * @param string the configured character value.
     * @param escapeSingleBackslash specifies if we need to escape a single backslash character with an extra backslash
     *
     * @return the DDL character value.
     */
    public String getDdlCharacterValue(String string, boolean escapeSingleBackslash)
    {
        // Assume the empty string for the return value.
        StringBuilder returnValueStringBuilder = new StringBuilder();

        // If we have an actual character, set the return value based on our rules.
        if (StringUtils.isNotEmpty(string))
        {
            // Convert the string to UTF-8 so we can the proper characters that were sent via XML.
            String utf8String = new String(string.getBytes(Charsets.UTF_8), Charsets.UTF_8);

            // Loop through each character and add each one to the return value.
            for (int i = 0; i < utf8String.length(); i++)
            {
                // Default to the character itself.
                Character character = string.charAt(i);
                String nextValue = character.toString();

                // If the character isn't ASCII printable, then "Hive" escape it.
                if (!CharUtils.isAsciiPrintable(character))
                {
                    // If the character is unprintable, then display it as the ASCII octal value in \000 format.
                    nextValue = String.format("\\%03o", (int) character);
                }

                // Add this character to the return value.
                returnValueStringBuilder.append(nextValue);
            }

            // Check if we need to escape a single backslash character with an extra backslash.
            if (escapeSingleBackslash && returnValueStringBuilder.toString().equals("\\"))
            {
                returnValueStringBuilder.append('\\');
            }
        }

        // Return the value.
        return returnValueStringBuilder.toString();
    }

    @Override
    public BusinessObjectDataDdlOutputFormatEnum getDdlOutputFormat()
    {
        return BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL;
    }

    /**
     * Gets a list of Hive partitions. For single level partitioning, no auto-discovery of sub-partitions (sub-directories) is needed - the business object data
     * will be represented by a single Hive partition instance. For multiple level partitioning, this method performs an auto-discovery of all sub-partitions
     * (sub-directories) and creates a Hive partition object instance for each partition.
     *
     * @param businessObjectDataKey the business object data key.
     * @param autoDiscoverableSubPartitionColumns the auto-discoverable sub-partition columns.
     * @param s3KeyPrefix the S3 key prefix.
     * @param storageFiles the storage files.
     * @param businessObjectDataEntity the business object data entity.
     * @param storageName the storage name.
     *
     * @return the list of Hive partitions
     */
    public List<HivePartitionDto> getHivePartitions(BusinessObjectDataKey businessObjectDataKey, List<SchemaColumn> autoDiscoverableSubPartitionColumns,
        String s3KeyPrefix, Collection<String> storageFiles, BusinessObjectDataEntity businessObjectDataEntity, String storageName)
    {
        // We are using linked hash map to preserve the order of the discovered partitions.
        LinkedHashMap<List<String>, HivePartitionDto> linkedHashMap = new LinkedHashMap<>();

        Pattern pattern = getHivePathPattern(autoDiscoverableSubPartitionColumns);
        for (String storageFile : storageFiles)
        {
            // Remove S3 key prefix from the file path. Please note that the storage files are already validated to start with S3 key prefix.
            String relativeFilePath = storageFile.substring(s3KeyPrefix.length());

            // Try to match the relative file path to the expected subpartition folders.
            Matcher matcher = pattern.matcher(relativeFilePath);
            Assert.isTrue(matcher.matches(), String.format("Registered storage file or directory does not match the expected Hive sub-directory pattern. " +
                "Storage: {%s}, file/directory: {%s}, business object data: {%s}, S3 key prefix: {%s}, pattern: {^%s$}", storageName, storageFile,
                businessObjectDataHelper.businessObjectDataEntityAltKeyToString(businessObjectDataEntity), s3KeyPrefix, pattern.pattern()));

            // Add the top level partition value.
            HivePartitionDto newHivePartition = new HivePartitionDto();
            newHivePartition.getPartitionValues().add(businessObjectDataKey.getPartitionValue());
            newHivePartition.getPartitionValues().addAll(businessObjectDataKey.getSubPartitionValues());
            // Extract relative partition values.
            for (int i = 1; i <= matcher.groupCount(); i++)
            {
                newHivePartition.getPartitionValues().add(matcher.group(i));
            }

            // Remove the trailing "/" plus an optional file name from the file path and store the result string as this partition relative path.
            newHivePartition.setPath(relativeFilePath.replaceAll("/[^/]*$", ""));

            // Check if we already have that partition discovered - that would happen if partition contains multiple data files.
            HivePartitionDto hivePartition = linkedHashMap.get(newHivePartition.getPartitionValues());

            if (hivePartition != null)
            {
                // Partition is already discovered, so just validate that the relative file paths match.
                Assert.isTrue(hivePartition.getPath().equals(newHivePartition.getPath()), String.format(
                    "Found two different locations for the same Hive partition. Storage: {%s}, business object data: {%s}, " +
                        "S3 key prefix: {%s}, path[1]: {%s}, path[2]: {%s}", storageName,
                    businessObjectDataHelper.businessObjectDataEntityAltKeyToString(businessObjectDataEntity), s3KeyPrefix, hivePartition.getPath(),
                    newHivePartition.getPath()));
            }
            else
            {
                // Add this partition to the hash map of discovered partitions.
                linkedHashMap.put(newHivePartition.getPartitionValues(), newHivePartition);
            }
        }

        List<HivePartitionDto> hivePartitions = new ArrayList<>();
        hivePartitions.addAll(linkedHashMap.values());

        return hivePartitions;
    }

    /**
     * Gets a pattern to match Hive partition sub-directories.
     *
     * @param partitionColumns the list of partition columns
     *
     * @return the newly created pattern to match Hive partition sub-directories.
     */
    public Pattern getHivePathPattern(List<SchemaColumn> partitionColumns)
    {
        StringBuilder sb = new StringBuilder(26);

        // For each partition column, add a regular expression to match "<COLUMN_NAME|COLUMN-NAME>=<VALUE>" sub-directory.
        for (SchemaColumn partitionColumn : partitionColumns)
        {
            String partitionColumnName = partitionColumn.getName();
            // We are using a non-capturing group for the partition column names here - this is done by adding "?:" to the beginning of a capture group.
            sb.append("\\/(?:");
            sb.append(Matcher.quoteReplacement(partitionColumnName));
            // Please note that for subpartition folder, we do support partition column names having all underscores replaced with hyphens.
            sb.append('|');
            sb.append(Matcher.quoteReplacement(partitionColumnName.replace("_", "-")));
            sb.append(")=([^/]+)");
        }

        // Add a regular expression for a trailing "/" and an optional file name.
        sb.append("\\/[^/]*");

        // We do a case-insensitive match for partition column names.
        return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
    }

    /**
     * Asserts that there exists at least one column specified in the business object format schema.
     *
     * @param businessObjectFormat The {@link BusinessObjectFormat} containing schema columns.
     * @param businessObjectFormatEntity The entity used to generate the error message.
     */
    private void assertSchemaColumnsNotEmpty(BusinessObjectFormat businessObjectFormat, BusinessObjectFormatEntity businessObjectFormatEntity)
    {
        Assert.notEmpty(businessObjectFormat.getSchema().getColumns(), String.format("No schema columns specified for business object format {%s}.",
            businessObjectFormatHelper.businessObjectFormatEntityAltKeyToString(businessObjectFormatEntity)));
    }

    /**
     * Generates and append to the string builder the create table Hive 13 DDL as per specified parameters.
     */
    private String generateCreateTableDdlHelper(GenerateDdlRequest generateDdlRequest)
    {
        // TODO: We might want to consider using a template engine such as Velocity to generate this DDL so we don't wind up just doing string manipulation.

        StringBuilder sb = new StringBuilder();

        // For custom DDL, we would need to substitute the custom DDL tokens with their relative values.
        HashMap<String, String> replacements = new HashMap<>();

        // Validate that partition values passed in the list of partition filters do not contain '/' character.
        if (generateDdlRequest.isPartitioned && !CollectionUtils.isEmpty(generateDdlRequest.partitionFilters))
        {
            // Validate that partition values do not contain '/' characters.
            for (List<String> partitionFilter : generateDdlRequest.partitionFilters)
            {
                for (String partitionValue : partitionFilter)
                {
                    Assert.doesNotContain(partitionValue, "/", String.format("Partition value \"%s\" can not contain a '/' character.", partitionValue));
                }
            }
        }

        // Get business object format model object to directly access schema columns and partitions.
        BusinessObjectFormat businessObjectFormat =
            businessObjectFormatHelper.createBusinessObjectFormatFromEntity(generateDdlRequest.businessObjectFormatEntity);

        // Validate that we have at least one column specified in the business object format schema.
        assertSchemaColumnsNotEmpty(businessObjectFormat, generateDdlRequest.businessObjectFormatEntity);

        if (generateDdlRequest.isPartitioned)
        {
            // Validate that we have at least one partition column specified in the business object format schema.
            Assert.notEmpty(businessObjectFormat.getSchema().getPartitions(), String.format("No schema partitions specified for business object format {%s}.",
                businessObjectFormatHelper.businessObjectFormatEntityAltKeyToString(generateDdlRequest.businessObjectFormatEntity)));

            // Validate that partition column names do not contain '/' characters.
            for (SchemaColumn partitionColumn : businessObjectFormat.getSchema().getPartitions())
            {
                Assert.doesNotContain(partitionColumn.getName(), "/", String
                    .format("Partition column name \"%s\" can not contain a '/' character. Business object format: {%s}", partitionColumn.getName(),
                        businessObjectFormatHelper.businessObjectFormatEntityAltKeyToString(generateDdlRequest.businessObjectFormatEntity)));
            }
        }

        // Add drop table if requested.
        if (BooleanUtils.isTrue(generateDdlRequest.includeDropTableStatement))
        {
            sb.append(String.format("DROP TABLE IF EXISTS `%s`;\n\n", generateDdlRequest.tableName));
        }

        // Depending on the flag, prepare "if not exists" option text or leave it an empty string.
        String ifNotExistsOption = BooleanUtils.isTrue(generateDdlRequest.includeIfNotExistsOption) ? "IF NOT EXISTS " : "";

        // Only generate the create table DDL statement, if custom DDL was not specified.
        if (generateDdlRequest.customDdlEntity == null)
        {
            generateStandardBaseDdl(generateDdlRequest, sb, businessObjectFormat, ifNotExistsOption);
        }
        else
        {
            // Use the custom DDL in place of the create table statement.
            sb.append(String.format("%s\n\n", generateDdlRequest.customDdlEntity.getDdl()));

            // We need to substitute the relative custom DDL token with an actual table name.
            replacements.put(TABLE_NAME_CUSTOM_DDL_TOKEN, generateDdlRequest.tableName);
        }

        // Add alter table statements only if the list of partition filters is not empty - this is applicable to generating DDL for business object data only.
        if (!CollectionUtils.isEmpty(generateDdlRequest.partitionFilters))
        {
            processPartitionFiltersForGenerateDdl(generateDdlRequest, sb, replacements, generateDdlRequest.businessObjectFormatEntity, businessObjectFormat,
                ifNotExistsOption);
        }
        // Add a location statement with a token if this is format dll that does not use custom ddl.
        else if (!generateDdlRequest.isPartitioned && generateDdlRequest.customDdlEntity == null)
        {
            // Since custom DDL is not specified, there are no partition values, and this table is not partitioned, add a LOCATION clause with a token.
            sb.append(String.format("LOCATION '%s';", NON_PARTITIONED_TABLE_LOCATION_CUSTOM_DDL_TOKEN));
        }

        // Trim to remove unnecessary end-of-line characters, if any, from the end of the generated DDL.
        String resultDdl = sb.toString().trim();

        // For custom DDL, substitute the relative custom DDL tokens with their values.
        if (generateDdlRequest.customDdlEntity != null)
        {
            for (Map.Entry<String, String> entry : replacements.entrySet())
            {
                String token = entry.getKey();
                String value = entry.getValue();
                resultDdl = resultDdl.replaceAll(Pattern.quote(token), value);
            }
        }

        return resultDdl;
    }

    /**
     * Generates the DDL column definitions based on the given business object format. The generated column definitions look like:
     * <p/>
     * <pre>
     *     `COL_NAME1` VARCHAR(2) COMMENT 'some comment',
     *     `COL_NAME2` VARCHAR(2),
     *     `ORIG_COL_NAME3` DATE
     * )
     * </pre>
     * <p/>
     * Each column definition is indented using 4 spaces. If a column is also a partition, the text 'ORIG_' will be prefixed in the column name. Note the
     * closing parenthesis at the end of the statement.
     *
     * @param businessObjectFormatEntity The persistent entity of business object format
     * @param businessObjectFormat The {@link BusinessObjectFormat}
     *
     * @return String containing the generated column definitions.
     */
    private String generateDdlColumns(BusinessObjectFormatEntity businessObjectFormatEntity, BusinessObjectFormat businessObjectFormat)
    {
        StringBuilder sb = new StringBuilder();
        // Add schema columns.
        Boolean firstRow = true;
        for (SchemaColumn schemaColumn : businessObjectFormat.getSchema().getColumns())
        {
            if (!firstRow)
            {
                sb.append(",\n");
            }
            else
            {
                firstRow = false;
            }

            // Add a schema column declaration. Check if a schema column is also a partition column and prepend "ORGNL_" prefix if this is the case.
            sb.append(String.format("    `%s%s` %s%s", (!CollectionUtils.isEmpty(businessObjectFormat.getSchema().getPartitions()) &&
                businessObjectFormat.getSchema().getPartitions().contains(schemaColumn) ? "ORGNL_" : ""), schemaColumn.getName(),
                getHiveDataType(schemaColumn, businessObjectFormatEntity),
                StringUtils.isNotBlank(schemaColumn.getDescription()) ? String.format(" COMMENT '%s'", escapeSingleQuotes(schemaColumn.getDescription())) :
                    ""));
        }
        sb.append(")\n");
        return sb.toString();
    }

    private void generateStandardBaseDdl(GenerateDdlRequest generateDdlRequest, StringBuilder sb, BusinessObjectFormat businessObjectFormat,
        String ifNotExistsOption)
    {
        // Please note that we escape table name and all column names to avoid Hive reserved words in DDL statement generation.
        sb.append(String.format("CREATE EXTERNAL TABLE %s`%s` (\n", ifNotExistsOption, generateDdlRequest.tableName));

        // Add schema columns.
        sb.append(generateDdlColumns(generateDdlRequest.businessObjectFormatEntity, businessObjectFormat));

        if (generateDdlRequest.isPartitioned)
        {
            // Add a partitioned by clause.
            sb.append("PARTITIONED BY (");
            // List all partition columns.
            List<String> partitionColumnDeclarations = new ArrayList<>();
            for (SchemaColumn partitionColumn : businessObjectFormat.getSchema().getPartitions())
            {
                partitionColumnDeclarations
                    .add(String.format("`%s` %s", partitionColumn.getName(), getHiveDataType(partitionColumn, generateDdlRequest.businessObjectFormatEntity)));
            }
            sb.append(StringUtils.join(partitionColumnDeclarations, ", "));
            sb.append(")\n");
        }

        // We output delimiter character, escape character, and null value only when they are defined in the business object format schema.
        sb.append("ROW FORMAT DELIMITED");
        if (!StringUtils.isEmpty(generateDdlRequest.businessObjectFormatEntity.getDelimiter()))
        {
            // Note that the escape character is only output when the delimiter is present.
            sb.append(String.format(" FIELDS TERMINATED BY '%s'%s",
                escapeSingleQuotes(getDdlCharacterValue(generateDdlRequest.businessObjectFormatEntity.getDelimiter(), true)),
                StringUtils.isEmpty(generateDdlRequest.businessObjectFormatEntity.getEscapeCharacter()) ? "" : String.format(" ESCAPED BY '%s'",
                    escapeSingleQuotes(getDdlCharacterValue(generateDdlRequest.businessObjectFormatEntity.getEscapeCharacter(), true)))));
        }
        sb.append(
            String.format(" NULL DEFINED AS '%s'\n", escapeSingleQuotes(getDdlCharacterValue(generateDdlRequest.businessObjectFormatEntity.getNullValue()))));

        // If this table is not partitioned, then STORED AS clause will be followed by LOCATION. Otherwise, the CREATE TABLE is complete.
        sb.append(
            String.format("STORED AS %s%s\n", getHiveFileFormat(generateDdlRequest.businessObjectFormatEntity), generateDdlRequest.isPartitioned ? ";\n" : ""));
    }

    /**
     * Returns the corresponding Hive data type per specified schema column entity.
     *
     * @param schemaColumn the schema column that we want to get the corresponding Hive data type for
     * @param businessObjectFormatEntity the business object format entity that schema column belongs to
     *
     * @return the Hive data type
     * @throws IllegalArgumentException if schema column data type is not supported
     */
    private String getHiveDataType(SchemaColumn schemaColumn, BusinessObjectFormatEntity businessObjectFormatEntity)
    {
        String hiveDataType;

        if (schemaColumn.getType().equalsIgnoreCase("TINYINT") || schemaColumn.getType().equalsIgnoreCase("SMALLINT") ||
            schemaColumn.getType().equalsIgnoreCase("INT") || schemaColumn.getType().equalsIgnoreCase("BIGINT") ||
            schemaColumn.getType().equalsIgnoreCase("FLOAT") || schemaColumn.getType().equalsIgnoreCase("DOUBLE") ||
            schemaColumn.getType().equalsIgnoreCase("TIMESTAMP") || schemaColumn.getType().equalsIgnoreCase("DATE") ||
            schemaColumn.getType().equalsIgnoreCase("STRING") || schemaColumn.getType().equalsIgnoreCase("BOOLEAN") ||
            schemaColumn.getType().equalsIgnoreCase("BINARY"))
        {
            hiveDataType = schemaColumn.getType().toUpperCase();
        }
        else if (schemaColumn.getType().equalsIgnoreCase("DECIMAL") || schemaColumn.getType().equalsIgnoreCase("NUMBER"))
        {
            hiveDataType = StringUtils.isNotBlank(schemaColumn.getSize()) ? String.format("DECIMAL(%s)", schemaColumn.getSize()) : "DECIMAL";
        }
        else if (schemaColumn.getType().equalsIgnoreCase("VARCHAR") || schemaColumn.getType().equalsIgnoreCase("CHAR"))
        {
            hiveDataType = String.format("%s(%s)", schemaColumn.getType().toUpperCase(), schemaColumn.getSize());
        }
        else if (schemaColumn.getType().equalsIgnoreCase("VARCHAR2"))
        {
            hiveDataType = String.format("VARCHAR(%s)", schemaColumn.getSize());
        }
        else
        {
            throw new IllegalArgumentException(String
                .format("Column \"%s\" has an unsupported data type \"%s\" in the schema for business object format {%s}.", schemaColumn.getName(),
                    schemaColumn.getType(), businessObjectFormatHelper.businessObjectFormatEntityAltKeyToString(businessObjectFormatEntity)));
        }

        return hiveDataType;
    }

    /**
     * Returns the corresponding Hive file format.
     *
     * @param businessObjectFormatEntity the business object format entity that schema column belongs to
     *
     * @return the Hive file format
     * @throws IllegalArgumentException if business object format file type is not supported
     */
    private String getHiveFileFormat(BusinessObjectFormatEntity businessObjectFormatEntity)
    {
        String fileFormat = businessObjectFormatEntity.getFileType().getCode();
        String hiveFileFormat;

        if (fileFormat.equalsIgnoreCase(FileTypeEntity.BZ_FILE_TYPE) || fileFormat.equalsIgnoreCase(FileTypeEntity.GZ_FILE_TYPE) ||
            fileFormat.equalsIgnoreCase(FileTypeEntity.TXT_FILE_TYPE))
        {
            hiveFileFormat = TEXT_HIVE_FILE_FORMAT;
        }
        else if (fileFormat.equalsIgnoreCase(FileTypeEntity.PARQUET_FILE_TYPE))
        {
            hiveFileFormat = PARQUET_HIVE_FILE_FORMAT;
        }
        else if (fileFormat.equalsIgnoreCase(FileTypeEntity.ORC_FILE_TYPE))
        {
            hiveFileFormat = ORC_HIVE_FILE_FORMAT;
        }
        else
        {
            throw new IllegalArgumentException(String.format("Unsupported format file type for business object format {%s}.",
                businessObjectFormatHelper.businessObjectFormatEntityAltKeyToString(businessObjectFormatEntity)));
        }

        return hiveFileFormat;
    }

    /**
     * Processes partition filters for DDL generation as per generate DDL request.
     *
     * @param generateDdlRequest the generate DDL request
     * @param sb the string builder to be updated with the "alter table add partition" statements
     * @param replacements the hash map of string values to be used to substitute the custom DDL tokens with their actual values
     * @param businessObjectFormatEntity the business object format entity
     * @param businessObjectFormat the business object format
     * @param ifNotExistsOption specifies if generated DDL contains "if not exists" option
     */
    private void processPartitionFiltersForGenerateDdl(GenerateDdlRequest generateDdlRequest, StringBuilder sb, HashMap<String, String> replacements,
        BusinessObjectFormatEntity businessObjectFormatEntity, BusinessObjectFormat businessObjectFormat, String ifNotExistsOption)
    {
        // Get the business object format key from the entity.
        BusinessObjectFormatKey businessObjectFormatKey = businessObjectFormatHelper.getBusinessObjectFormatKey(generateDdlRequest.businessObjectFormatEntity);

        // Override the business object format version with the original (optional) value from the request.
        businessObjectFormatKey.setBusinessObjectFormatVersion(generateDdlRequest.businessObjectFormatVersion);

        // Retrieve a list of storage unit entities for the specified list of partition filters. The entities will be sorted by partition values and storages.
        // For a non-partitioned table, there should only exist a single business object data entity (with partitionValue equals to "none"). We do validate that
        // all specified storages are of "S3" storage platform type, so we specify S3 storage platform type in the herdDao call below, so we select storage
        // units only from all S3 storages, when the specified list of storages is empty. We also specify to select only "available" storage units.
        List<StorageUnitEntity> storageUnitEntities = storageUnitDao
            .getStorageUnitsByPartitionFiltersAndStorages(businessObjectFormatKey, generateDdlRequest.partitionFilters,
                generateDdlRequest.businessObjectDataVersion, BusinessObjectDataStatusEntity.VALID, generateDdlRequest.storageNames, StoragePlatformEntity.S3,
                null, true);

        // Exclude duplicate business object data per specified list of storage names.
        // If storage names are not specified, the method fails on business object data instances registered with multiple storages.
        storageUnitEntities = excludeDuplicateBusinessObjectData(storageUnitEntities, generateDdlRequest.storageNames, generateDdlRequest.storageEntities);

        // Build a list of matched partition filters. Please note that each request partition
        // filter might result in multiple available business object data entities.
        List<List<String>> matchedAvailablePartitionFilters = new ArrayList<>();
        List<List<String>> availablePartitions = new ArrayList<>();
        for (StorageUnitEntity storageUnitEntity : storageUnitEntities)
        {
            BusinessObjectDataKey businessObjectDataKey = businessObjectDataHelper.getBusinessObjectDataKey(storageUnitEntity.getBusinessObjectData());
            matchedAvailablePartitionFilters
                .add(businessObjectDataHelper.getPartitionFilter(businessObjectDataKey, generateDdlRequest.partitionFilters.get(0)));
            availablePartitions.add(businessObjectDataHelper.getPrimaryAndSubPartitionValues(businessObjectDataKey));
        }

        // If request specifies to include all registered sub-partitions, fail if any of "non-available" registered sub-partitions are found.
        if (generateDdlRequest.businessObjectDataVersion == null && BooleanUtils.isTrue(generateDdlRequest.includeAllRegisteredSubPartitions) &&
            !CollectionUtils.isEmpty(matchedAvailablePartitionFilters))
        {
            notAllowNonAvailableRegisteredSubPartitions(businessObjectFormatKey, matchedAvailablePartitionFilters, availablePartitions,
                generateDdlRequest.storageNames);
        }

        // Fail on any missing business object data unless the flag is set to allow missing business object data.
        if (!BooleanUtils.isTrue(generateDdlRequest.allowMissingData))
        {
            // Get a list of unmatched partition filters.
            List<List<String>> unmatchedPartitionFilters = new ArrayList<>(generateDdlRequest.partitionFilters);
            unmatchedPartitionFilters.removeAll(matchedAvailablePartitionFilters);

            // Throw an exception if we have any unmatched partition filters.
            if (!unmatchedPartitionFilters.isEmpty())
            {
                // Get the first unmatched partition filter and throw exception.
                List<String> unmatchedPartitionFilter = getFirstUnmatchedPartitionFilter(unmatchedPartitionFilters);
                throw new ObjectNotFoundException(
                    String.format("Business object data {namespace: \"%s\", businessObjectDefinitionName: \"%s\", businessObjectFormatUsage: \"%s\", " +
                        "businessObjectFormatFileType: \"%s\", businessObjectFormatVersion: %d, partitionValue: \"%s\", " +
                        "subpartitionValues: \"%s\", businessObjectDataVersion: %d} is not available in \"%s\" storage(s).",
                        businessObjectFormatKey.getNamespace(), businessObjectFormatKey.getBusinessObjectDefinitionName(),
                        businessObjectFormatKey.getBusinessObjectFormatUsage(), businessObjectFormatKey.getBusinessObjectFormatFileType(),
                        businessObjectFormatKey.getBusinessObjectFormatVersion(), unmatchedPartitionFilter.get(0),
                        StringUtils.join(unmatchedPartitionFilter.subList(1, unmatchedPartitionFilter.size()), ","),
                        generateDdlRequest.businessObjectDataVersion, StringUtils.join(generateDdlRequest.storageNames, ",")));
            }
        }

        // We still need to close/complete the create table statement when there is no custom DDL,
        // the table is non-partitioned, and there is no business object data found.
        if (generateDdlRequest.customDdlEntity == null && !generateDdlRequest.isPartitioned && CollectionUtils.isEmpty(storageUnitEntities))
        {
            // Add a LOCATION clause with a token.
            sb.append(String.format("LOCATION '%s';", NON_PARTITIONED_TABLE_LOCATION_CUSTOM_DDL_TOKEN));
        }
        // The table is partitioned, custom DDL is specified, or there is at least one business object data instance found.
        else
        {
            // If drop partitions flag is set and the table is partitioned, drop partitions specified by the partition filters.
            if (generateDdlRequest.isPartitioned && BooleanUtils.isTrue(generateDdlRequest.includeDropPartitions))
            {
                // Add a drop partition statement for each partition filter entry.
                for (List<String> partitionFilter : generateDdlRequest.partitionFilters)
                {
                    sb.append(String.format("ALTER TABLE `%s` DROP IF EXISTS PARTITION (", generateDdlRequest.tableName));
                    // Specify all partition column values as per this partition filter.
                    List<String> partitionKeyValuePairs = new ArrayList<>();
                    for (int i = 0; i < partitionFilter.size(); i++)
                    {
                        if (StringUtils.isNotBlank(partitionFilter.get(i)))
                        {
                            // We cannot hit ArrayIndexOutOfBoundsException on getPartitions() since partitionFilter would
                            // not have a value set at an index that is greater or equal than the number of partitions in the schema.
                            String partitionColumnName = businessObjectFormat.getSchema().getPartitions().get(i).getName();
                            partitionKeyValuePairs.add(String.format("`%s`='%s'", partitionColumnName, partitionFilter.get(i)));
                        }
                    }
                    sb.append(StringUtils.join(partitionKeyValuePairs, ", "));
                    sb.append(");\n");
                }
                sb.append('\n');
            }

            // Process storage unit entities.
            if (!CollectionUtils.isEmpty(storageUnitEntities))
            {
                processStorageUnitsForGenerateDdl(generateDdlRequest, sb, replacements, businessObjectFormatEntity, businessObjectFormat, ifNotExistsOption,
                    storageUnitEntities);
            }
        }
    }

    /**
     * Gets a first unmatched partition filter from the list of unmatched filters.
     *
     * @param unmatchedPartitionFilters the list of unmatchedPartitionFilters
     *
     * @return the first unmatched partition filter
     */
    private List<String> getFirstUnmatchedPartitionFilter(List<List<String>> unmatchedPartitionFilters)
    {
        // Get the first unmatched partition filter from the list.
        List<String> unmatchedPartitionFilter = unmatchedPartitionFilters.get(0);

        // Replace all null partition values with an empty string.
        for (int i = 0; i < unmatchedPartitionFilter.size(); i++)
        {
            if (unmatchedPartitionFilter.get(i) == null)
            {
                unmatchedPartitionFilter.set(i, "");
            }
        }

        return unmatchedPartitionFilter;
    }

    /**
     * Adds the relative "alter table add partition" statements for each storage unit entity. Please note that each request partition value might result in
     * multiple available storage unit entities (subpartitions).
     *
     * @param sb the string builder to be updated with the "alter table add partition" statements
     * @param replacements the hash map of string values to be used to substitute the custom DDL tokens with their actual values
     * @param businessObjectFormatEntity the business object format entity
     * @param businessObjectFormat the business object format
     * @param ifNotExistsOption specifies if generated DDL contains "if not exists" option
     * @param storageUnitEntities the list of storage unit entities
     */
    private void processStorageUnitsForGenerateDdl(GenerateDdlRequest generateDdlRequest, StringBuilder sb, HashMap<String, String> replacements,
        BusinessObjectFormatEntity businessObjectFormatEntity, BusinessObjectFormat businessObjectFormat, String ifNotExistsOption,
        List<StorageUnitEntity> storageUnitEntities)
    {
        // If flag is not set to suppress scan for unregistered sub-partitions, retrieve all storage
        // file paths for the relative storage units loaded in a multi-valued map for easy access.
        MultiValuedMap<Integer, String> storageUnitIdToStorageFilePathsMap =
            BooleanUtils.isTrue(generateDdlRequest.suppressScanForUnregisteredSubPartitions) ? new ArrayListValuedHashMap<>() :
                storageFileDao.getStorageFilePathsByStorageUnits(storageUnitEntities);

        // Process all available business object data instances.
        for (StorageUnitEntity storageUnitEntity : storageUnitEntities)
        {
            // Get business object data key and S3 key prefix for this business object data.
            BusinessObjectDataKey businessObjectDataKey = businessObjectDataHelper.getBusinessObjectDataKey(storageUnitEntity.getBusinessObjectData());
            String s3KeyPrefix = s3KeyPrefixHelper
                .buildS3KeyPrefix(storageUnitEntity.getStorage(), storageUnitEntity.getBusinessObjectData().getBusinessObjectFormat(), businessObjectDataKey);

            // If flag is set to suppress scan for unregistered sub-partitions, use the directory path or the S3 key prefix
            // as the partition's location, otherwise, use storage files to discover all unregistered sub-partitions.
            Collection<String> storageFilePaths = new ArrayList<>();
            if (BooleanUtils.isTrue(generateDdlRequest.suppressScanForUnregisteredSubPartitions))
            {
                // Validate the directory path value if it is present.
                if (storageUnitEntity.getDirectoryPath() != null)
                {
                    Assert.isTrue(storageUnitEntity.getDirectoryPath().equals(s3KeyPrefix), String.format(
                        "Storage directory path \"%s\" registered with business object data {%s} " +
                            "in \"%s\" storage does not match the expected S3 key prefix \"%s\".", storageUnitEntity.getDirectoryPath(),
                        businessObjectDataHelper.businessObjectDataEntityAltKeyToString(storageUnitEntity.getBusinessObjectData()),
                        storageUnitEntity.getStorage().getName(), s3KeyPrefix));
                }

                // Add the S3 key prefix to the list of storage files.
                // We add a trailing '/' character to the prefix, since it represents a directory.
                storageFilePaths.add(StringUtils.appendIfMissing(s3KeyPrefix, "/"));
            }
            else
            {
                // Retrieve storage file paths registered with this business object data in the specified storage.
                storageFilePaths = storageUnitIdToStorageFilePathsMap.containsKey(storageUnitEntity.getId()) ?
                    storageUnitIdToStorageFilePathsMap.get(storageUnitEntity.getId()) : new ArrayList<>();

                // Validate storage file paths registered with this business object data in the specified storage.
                // The validation check below is required even if we have no storage files registered.
                storageFileHelper
                    .validateStorageFiles(storageFilePaths, s3KeyPrefix, storageUnitEntity.getBusinessObjectData(), storageUnitEntity.getStorage().getName());

                // If there are no storage files registered for this storage unit, we should use the storage directory path value.
                if (storageFilePaths.isEmpty())
                {
                    // Validate that directory path value is present and it matches the S3 key prefix.
                    Assert.isTrue(storageUnitEntity.getDirectoryPath() != null && storageUnitEntity.getDirectoryPath().startsWith(s3KeyPrefix), String.format(
                        "Storage directory path \"%s\" registered with business object data {%s} " +
                            "in \"%s\" storage does not match the expected S3 key prefix \"%s\".", storageUnitEntity.getDirectoryPath(),
                        businessObjectDataHelper.businessObjectDataEntityAltKeyToString(storageUnitEntity.getBusinessObjectData()),
                        storageUnitEntity.getStorage().getName(), s3KeyPrefix));
                    // Add storage directory path the empty storage files list.
                    // We add a trailing '/' character to the path, since it represents a directory.
                    storageFilePaths.add(storageUnitEntity.getDirectoryPath() + "/");
                }
            }

            // Retrieve the s3 bucket name.
            String s3BucketName = getS3BucketName(storageUnitEntity.getStorage(), generateDdlRequest.s3BucketNames);

            // For partitioned table, add the relative partitions to the generated DDL.
            if (generateDdlRequest.isPartitioned)
            {
                // If flag is set to suppress scan for unregistered sub-partitions, validate that the number of primary and sub-partition values specified for
                // the business object data equals to the number of partition columns defined in schema for the format selected for DDL generation.
                if (BooleanUtils.isTrue(generateDdlRequest.suppressScanForUnregisteredSubPartitions))
                {
                    int businessObjectDataRegisteredPartitions = 1 + CollectionUtils.size(businessObjectDataKey.getSubPartitionValues());
                    Assert.isTrue(businessObjectFormat.getSchema().getPartitions().size() == businessObjectDataRegisteredPartitions,
                        String.format("Number of primary and sub-partition values (%d) specified for the business object data is not equal to " +
                            "the number of partition columns (%d) defined in the schema of the business object format selected for DDL generation. " +
                            "Business object data: {%s},  business object format: {%s}", businessObjectDataRegisteredPartitions,
                            businessObjectFormat.getSchema().getPartitions().size(),
                            businessObjectDataHelper.businessObjectDataKeyToString(businessObjectDataKey),
                            businessObjectFormatHelper.businessObjectFormatEntityAltKeyToString(businessObjectFormatEntity)));
                }
                // Otherwise, since the format version selected for DDL generation might not match the relative business object format version that business
                // object data is registered against, validate that the number of sub-partition values specified for the business object data is less than
                // the number of partition columns defined in schema for the format selected for DDL generation.
                else
                {
                    Assert.isTrue(businessObjectFormat.getSchema().getPartitions().size() > CollectionUtils.size(businessObjectDataKey.getSubPartitionValues()),
                        String.format("Number of subpartition values specified for the business object data is greater than or equal to " +
                            "the number of partition columns defined in the schema of the business object format selected for DDL generation. " +
                            "Business object data: {%s},  business object format: {%s}",
                            businessObjectDataHelper.businessObjectDataKeyToString(businessObjectDataKey),
                            businessObjectFormatHelper.businessObjectFormatEntityAltKeyToString(businessObjectFormatEntity)));
                }

                // Get partition information. For multiple level partitioning, auto-discover subpartitions (subdirectories) not already included into the S3 key
                // prefix. Each discovered partition requires a standalone "add partition" clause. Please note that due to the above validation check, there
                // should be no auto discoverable sub-partition columns, when flag is set to suppress scan for unregistered sub-partitions.
                List<SchemaColumn> autoDiscoverableSubPartitionColumns = businessObjectFormat.getSchema().getPartitions()
                    .subList(1 + CollectionUtils.size(businessObjectDataKey.getSubPartitionValues()), businessObjectFormat.getSchema().getPartitions().size());

                for (HivePartitionDto hivePartition : getHivePartitions(businessObjectDataKey, autoDiscoverableSubPartitionColumns, s3KeyPrefix,
                    storageFilePaths, storageUnitEntity.getBusinessObjectData(), storageUnitEntity.getStorage().getName()))
                {
                    sb.append(String.format("ALTER TABLE `%s` ADD %sPARTITION (", generateDdlRequest.tableName, ifNotExistsOption));
                    // Specify all partition column values.
                    List<String> partitionKeyValuePairs = new ArrayList<>();
                    for (int i = 0; i < businessObjectFormat.getSchema().getPartitions().size(); i++)
                    {
                        String partitionColumnName = businessObjectFormat.getSchema().getPartitions().get(i).getName();
                        String partitionValue = hivePartition.getPartitionValues().get(i);
                        partitionKeyValuePairs.add(String.format("`%s`='%s'", partitionColumnName, partitionValue));
                    }
                    sb.append(StringUtils.join(partitionKeyValuePairs, ", "));
                    sb.append(String.format(") LOCATION 's3n://%s/%s%s';\n", s3BucketName, s3KeyPrefix,
                        StringUtils.isNotBlank(hivePartition.getPath()) ? hivePartition.getPath() : ""));
                }
            }
            else // This is a non-partitioned table.
            {
                // Get location for this non-partitioned table.
                String tableLocation = String.format("s3n://%s/%s", s3BucketName, s3KeyPrefix);

                if (generateDdlRequest.customDdlEntity == null)
                {
                    // Since custom DDL was not specified and this table is not partitioned, add a LOCATION clause.
                    // This is the last line in the non-partitioned table DDL.
                    sb.append(String.format("LOCATION '%s';", tableLocation));
                }
                else
                {
                    // Since custom DDL was used for a non-partitioned table, substitute the relative custom DDL token with the actual table location.
                    replacements.put(NON_PARTITIONED_TABLE_LOCATION_CUSTOM_DDL_TOKEN, tableLocation);
                }
            }
        }
    }

    /**
     * Gets an S3 bucket name for the specified storage entity. The method memorizes the responses for performance reasons.
     *
     * @param storageEntity the storage entity
     * @param s3BucketNames the map of storage names to their relative S3 bucket names
     *
     * @return the S3 bucket name
     */
    private String getS3BucketName(StorageEntity storageEntity, Map<StorageEntity, String> s3BucketNames)
    {
        String s3BucketName;

        // If bucket name was already retrieved for this storage, use it.
        if (s3BucketNames.containsKey(storageEntity))
        {
            s3BucketName = s3BucketNames.get(storageEntity);
        }
        // Otherwise, retrieve the S3 bucket name attribute value and store it in memory. Please note that it is required, so we pass in a "true" flag.
        else
        {
            s3BucketName = storageHelper
                .getStorageAttributeValueByName(configurationHelper.getProperty(ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME), storageEntity, true);
            s3BucketNames.put(storageEntity, s3BucketName);
        }

        return s3BucketName;
    }

    /**
     * Parameters grouping for {@link Hive13DdlGenerator#generateCreateTableDdlHelper(GenerateDdlRequest)}
     */
    private static class GenerateDdlRequest
    {
        private Boolean allowMissingData;

        private Integer businessObjectDataVersion;

        private BusinessObjectFormatEntity businessObjectFormatEntity;

        private Integer businessObjectFormatVersion;

        private CustomDdlEntity customDdlEntity;

        private Boolean includeAllRegisteredSubPartitions;

        private Boolean includeDropPartitions;

        private Boolean includeDropTableStatement;

        private Boolean includeIfNotExistsOption;

        private Boolean isPartitioned;

        private List<List<String>> partitionFilters;

        private Map<StorageEntity, String> s3BucketNames;

        private List<StorageEntity> storageEntities;

        private List<String> storageNames;

        private Boolean suppressScanForUnregisteredSubPartitions;

        private String tableName;
    }

    /**
     * Eliminate storage units that belong to the same business object data by picking storage unit registered in a storage listed earlier in the list of
     * storage names specified in the request. If storage names are not specified, simply fail on business object data instances registered with multiple
     * storages.
     *
     * @param storageUnitEntities the list of storage unit entities
     * @param storageNames the list of storage names
     * @param storageEntities the list of storage entities
     *
     * @return the updated list of storage unit entities
     * @throws IllegalArgumentException on business object data being registered in multiple storages and storage names are not specified to resolve this
     */
    protected List<StorageUnitEntity> excludeDuplicateBusinessObjectData(List<StorageUnitEntity> storageUnitEntities, List<String> storageNames,
        List<StorageEntity> storageEntities) throws IllegalArgumentException
    {
        // If storage names are not specified, fail on business object data instances registered with multiple storages.
        // Otherwise, in a case when the same business object data is registered with multiple storages,
        // pick storage unit registered in a storage listed earlier in the list of storage names specified in the request.
        Map<BusinessObjectDataEntity, StorageUnitEntity> businessObjectDataToStorageUnitMap = new LinkedHashMap<>();
        for (StorageUnitEntity storageUnitEntity : storageUnitEntities)
        {
            BusinessObjectDataEntity businessObjectDataEntity = storageUnitEntity.getBusinessObjectData();

            if (businessObjectDataToStorageUnitMap.containsKey(businessObjectDataEntity))
            {
                // Duplicate business object data is found, so check if storage names are specified.
                if (CollectionUtils.isEmpty(storageNames))
                {
                    // Fail on business object data registered in multiple storages.
                    throw new IllegalArgumentException(String.format("Found business object data registered in more than one storage. " +
                        "Please specify storage(s) in the request to resolve this. Business object data {%s}",
                        businessObjectDataHelper.businessObjectDataEntityAltKeyToString(businessObjectDataEntity)));
                }
                else
                {
                    // Replace the storage unit entity if it belongs to a "higher priority" storage.
                    StorageEntity currentStorageEntity = businessObjectDataToStorageUnitMap.get(businessObjectDataEntity).getStorage();
                    int currentStorageIndex = storageEntities.indexOf(currentStorageEntity);
                    int newStorageIndex = storageEntities.indexOf(storageUnitEntity.getStorage());
                    if (newStorageIndex < currentStorageIndex)
                    {
                        businessObjectDataToStorageUnitMap.put(storageUnitEntity.getBusinessObjectData(), storageUnitEntity);
                    }
                }
            }
            else
            {
                businessObjectDataToStorageUnitMap.put(storageUnitEntity.getBusinessObjectData(), storageUnitEntity);
            }
        }

        return new ArrayList<>(businessObjectDataToStorageUnitMap.values());
    }

    /**
     * Searches for and fails on any of "non-available" registered sub-partitions as per list of "matched" partition filters.
     *
     * @param businessObjectFormatKey the business object format key
     * @param matchedAvailablePartitionFilters the list of "matched" partition filters
     * @param availablePartitions the list of already discovered "available" partitions, where each partition consists of primary and optional sub-partition
     * values
     * @param storageNames the list of storage names
     */
    protected void notAllowNonAvailableRegisteredSubPartitions(BusinessObjectFormatKey businessObjectFormatKey,
        List<List<String>> matchedAvailablePartitionFilters, List<List<String>> availablePartitions, List<String> storageNames)
    {
        // Query all matched partition filters to discover any non-available registered sub-partitions. Retrieve latest business object data per list of
        // matched filters regardless of business object data and/or storage unit statuses. This is done to discover all registered sub-partitions regardless
        // of business object data or storage unit statuses. We do validate that all specified storages are of "S3" storage platform type, so we specify S3
        // storage platform type in the herdDao call below, so we select storage units only from all S3 storages, when the specified list of storages is empty.
        // We want to select any existing storage units regardless of their status, so we pass "false" for selectOnlyAvailableStorageUnits parameter.
        List<StorageUnitEntity> matchedNotAvailableStorageUnitEntities = storageUnitDao
            .getStorageUnitsByPartitionFiltersAndStorages(businessObjectFormatKey, matchedAvailablePartitionFilters, null, null, storageNames,
                StoragePlatformEntity.S3, null, false);

        // Exclude all storage units with business object data having "DELETED" status.
        matchedNotAvailableStorageUnitEntities =
            storageUnitHelper.excludeBusinessObjectDataStatus(matchedNotAvailableStorageUnitEntities, BusinessObjectDataStatusEntity.DELETED);

        // Exclude all already discovered "available" partitions. Please note that, since we got here, the list of matched partitions can not be empty.
        matchedNotAvailableStorageUnitEntities = storageUnitHelper.excludePartitions(matchedNotAvailableStorageUnitEntities, availablePartitions);

        // Fail on any "non-available" registered sub-partitions.
        if (!CollectionUtils.isEmpty(matchedNotAvailableStorageUnitEntities))
        {
            // Get the business object data key for the first "non-available" registered sub-partition.
            BusinessObjectDataKey businessObjectDataKey =
                businessObjectDataHelper.getBusinessObjectDataKey(matchedNotAvailableStorageUnitEntities.get(0).getBusinessObjectData());

            throw new ObjectNotFoundException(
                String.format("Business object data {namespace: \"%s\", businessObjectDefinitionName: \"%s\", businessObjectFormatUsage: \"%s\", " +
                    "businessObjectFormatFileType: \"%s\", businessObjectFormatVersion: %d, partitionValue: \"%s\", " +
                    "subpartitionValues: \"%s\", businessObjectDataVersion: %d} is not available in \"%s\" storage(s).", businessObjectFormatKey.getNamespace(),
                    businessObjectFormatKey.getBusinessObjectDefinitionName(), businessObjectFormatKey.getBusinessObjectFormatUsage(),
                    businessObjectFormatKey.getBusinessObjectFormatFileType(), businessObjectFormatKey.getBusinessObjectFormatVersion(),
                    businessObjectDataKey.getPartitionValue(), StringUtils.join(businessObjectDataKey.getSubPartitionValues(), ","),
                    businessObjectDataKey.getBusinessObjectDataVersion(), StringUtils.join(storageNames, ",")));
        }
    }
}
