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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import org.finra.herd.model.api.xml.Attribute;
import org.finra.herd.model.api.xml.BusinessObjectFormatExternalInterfaceDescriptiveInformation;
import org.finra.herd.model.api.xml.SchemaColumn;
import org.finra.herd.model.jpa.BusinessObjectDataEntity;
import org.finra.herd.model.jpa.BusinessObjectFormatEntity;
import org.finra.herd.model.jpa.ExternalInterfaceEntity;
import org.finra.herd.model.jpa.SchemaColumnEntity;
import org.finra.herd.service.AbstractServiceTest;

public class BusinessObjectFormatExternalInterfaceDescriptiveInformationHelperVelocityTest extends AbstractServiceTest
{
    @Test
    public void testValidateAndTrimBusinessObjectFormatExternalInterfaceDescriptiveInformationKey()
    {
        // Create a velocity template for the external interface entity.
        String velocityTemplateDescription =
            "${namespace}#${bdefName}#${usage}#${fileType}#${attributes}#${schemaColumns}#${partitions}#${partitionKeyGroup}#${delimiter}#${nullValue}";

        // Get a list of partition columns that is larger than number of partitions supported by business object data registration.
        List<SchemaColumn> partitionColumns = schemaColumnDaoTestHelper.getTestPartitionColumns();
        assertTrue(CollectionUtils.size(partitionColumns) > BusinessObjectDataEntity.MAX_SUBPARTITIONS + 1);

        // Get a list of regular columns.
        List<SchemaColumn> regularColumns = schemaColumnDaoTestHelper.getTestSchemaColumns();

        // Create a list of attributes
        List<Attribute> attributes = Lists.newArrayList(new Attribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE), new Attribute(ATTRIBUTE_NAME_2, ATTRIBUTE_VALUE_2));

        // Create a business object format entity.
        BusinessObjectFormatEntity businessObjectFormatEntity = businessObjectFormatDaoTestHelper
            .createBusinessObjectFormatEntity(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, FORMAT_DESCRIPTION,
                NO_FORMAT_DOCUMENT_SCHEMA, LATEST_VERSION_FLAG_SET, partitionColumns.get(0).getName(), PARTITION_KEY_GROUP, attributes, SCHEMA_DELIMITER_PIPE,
                SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, regularColumns, partitionColumns);

        // Create an external interface entity.
        ExternalInterfaceEntity externalInterfaceEntity = externalInterfaceDaoTestHelper.createExternalInterfaceEntity(EXTERNAL_INTERFACE);
        externalInterfaceEntity.setDisplayName(DISPLAY_NAME_FIELD);
        externalInterfaceEntity.setDescription(velocityTemplateDescription);

        // Create a business object format to external interface mapping entity.
        businessObjectFormatExternalInterfaceDaoTestHelper
            .createBusinessObjectFormatExternalInterfaceEntity(businessObjectFormatEntity, externalInterfaceEntity);

        // Call the method under test.
        BusinessObjectFormatExternalInterfaceDescriptiveInformation result = businessObjectFormatExternalInterfaceDescriptiveInformationHelper
            .createBusinessObjectFormatExternalInterfaceDescriptiveInformationFromEntities(businessObjectFormatEntity, externalInterfaceEntity);

        // Validate the results.
        assertEquals(result.getBusinessObjectFormatExternalInterfaceDescriptiveInformationKey().getNamespace(), NAMESPACE);
        assertEquals(result.getBusinessObjectFormatExternalInterfaceDescriptiveInformationKey().getBusinessObjectDefinitionName(), BDEF_NAME);
        assertEquals(result.getBusinessObjectFormatExternalInterfaceDescriptiveInformationKey().getBusinessObjectFormatUsage(), FORMAT_USAGE_CODE);
        assertEquals(result.getBusinessObjectFormatExternalInterfaceDescriptiveInformationKey().getBusinessObjectFormatFileType(), FORMAT_FILE_TYPE_CODE);
        assertEquals(result.getBusinessObjectFormatExternalInterfaceDescriptiveInformationKey().getExternalInterfaceName(), EXTERNAL_INTERFACE);
        assertEquals(result.getExternalInterfaceDisplayName(), DISPLAY_NAME_FIELD);

        String[] descriptionTemplateReplacementValues = result.getExternalInterfaceDescription().split("#");

        // ${namespace}
        assertEquals("Namespace not equal to replacement.", descriptionTemplateReplacementValues[0], NAMESPACE);

        // ${bdefName}
        assertEquals("Business object definition name not equal to replacement.", descriptionTemplateReplacementValues[1], BDEF_NAME);

        // ${usage}
        assertEquals("Usage not equal to replacement.", descriptionTemplateReplacementValues[2], FORMAT_USAGE_CODE);

        // ${fileType}
        assertEquals("File type not equal to replacement.", descriptionTemplateReplacementValues[3], FORMAT_FILE_TYPE_CODE);

        // ${attributes}
        Map<String, String> attributesMap = Maps.newHashMap();
        for (Attribute attribute : attributes)
        {
            attributesMap.put(attribute.getName(), attribute.getValue());
        }
        assertEquals("Attributes not equal to replacement.", descriptionTemplateReplacementValues[4], attributesMap.toString());

        // ${schemaColumns}
        List<String> columnNames = Lists.newArrayList();
        List<String> partitionColumnNames = Lists.newArrayList();
        for (SchemaColumnEntity schemaColumn : businessObjectFormatEntity.getSchemaColumns())
        {
            if (schemaColumn.getPartitionLevel() == null)
            {
                columnNames.add(schemaColumn.getName());
            }
            else
            {
                partitionColumnNames.add(schemaColumn.getName());
            }
        }
        assertEquals("Schema columns not equal to replacement.", descriptionTemplateReplacementValues[5], String.join(",", columnNames));

        // ${partitions}
        assertEquals("Partitions not equal to replacement.", descriptionTemplateReplacementValues[6], String.join(",", partitionColumnNames));

        // ${partitionKeyGroup}
        assertEquals("Partition key group not equal to replacement.", descriptionTemplateReplacementValues[7], PARTITION_KEY_GROUP);

        // ${delimiter}
        assertEquals("Delimiter not equal to replacement.", descriptionTemplateReplacementValues[8], SCHEMA_DELIMITER_PIPE);

        // ${nullValue}
        assertEquals("Null value not equal to replacement.", descriptionTemplateReplacementValues[9], SCHEMA_NULL_VALUE_BACKSLASH_N);
    }
}
