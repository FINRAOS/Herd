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
package org.finra.herd.service.activiti.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.FieldExtension;
import org.junit.Test;

import org.finra.herd.model.api.xml.BusinessObjectDataKey;
import org.finra.herd.model.api.xml.BusinessObjectDataStatusUpdateResponse;
import org.finra.herd.model.api.xml.Parameter;
import org.finra.herd.service.activiti.ActivitiRuntimeHelper;

/**
 * Test suite for Update Business Object Data Status Activiti wrapper.
 */
public class UpdateBusinessObjectDataStatusTest extends HerdActivitiServiceTaskTest
{
    @Test
    public void testUpdateBusinessObjectDataStatus() throws Exception
    {
        // Create a business object data key.
        BusinessObjectDataKey businessObjectDataKey =
            new BusinessObjectDataKey(BDEF_NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, SUBPARTITION_VALUES,
                DATA_VERSION);

        // Create a business object data entity.
        businessObjectDataDaoTestHelper.createBusinessObjectDataEntity(businessObjectDataKey, LATEST_VERSION_FLAG_SET, BDATA_STATUS);

        // Create a business object data status entity.
        businessObjectDataStatusDaoTestHelper.createBusinessObjectDataStatusEntity(BDATA_STATUS_2);

        List<FieldExtension> fieldExtensionList = new ArrayList<>();

        fieldExtensionList.add(buildFieldExtension("namespace", "${namespace}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectDefinitionName", "${businessObjectDefinitionName}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectFormatUsage", "${businessObjectFormatUsage}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectFormatFileType", "${businessObjectFormatFileType}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectFormatVersion", "${businessObjectFormatVersion}"));
        fieldExtensionList.add(buildFieldExtension("partitionValue", "${partitionValue}"));
        fieldExtensionList.add(buildFieldExtension("subPartitionValues", "${subPartitionValues}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectDataVersion", "${businessObjectDataVersion}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectDataStatus", "${businessObjectDataStatus}"));

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(buildParameter("namespace", BDEF_NAMESPACE));
        parameters.add(buildParameter("businessObjectDefinitionName", BDEF_NAME));
        parameters.add(buildParameter("businessObjectFormatUsage", FORMAT_USAGE_CODE));
        parameters.add(buildParameter("businessObjectFormatFileType", FORMAT_FILE_TYPE_CODE));
        parameters.add(buildParameter("businessObjectFormatVersion", FORMAT_VERSION.toString()));
        parameters.add(buildParameter("partitionValue", PARTITION_VALUE));
        parameters.add(buildParameter("subPartitionValues", herdStringHelper.buildStringWithDefaultDelimiter(SUBPARTITION_VALUES)));
        parameters.add(buildParameter("businessObjectDataVersion", DATA_VERSION.toString()));
        parameters.add(buildParameter("businessObjectDataStatus", BDATA_STATUS_2));

        // Build the expected response object.
        BusinessObjectDataStatusUpdateResponse expectedBusinessObjectDataStatusUpdateResponse =
            new BusinessObjectDataStatusUpdateResponse(businessObjectDataKey, BDATA_STATUS_2, BDATA_STATUS);

        // Run the activiti task and validate the returned response object.
        Map<String, Object> variableValuesToValidate = new HashMap<>();
        variableValuesToValidate
            .put(UpdateBusinessObjectDataStatus.VARIABLE_JSON_RESPONSE, jsonHelper.objectToJson(expectedBusinessObjectDataStatusUpdateResponse));
        testActivitiServiceTaskSuccess(UpdateBusinessObjectDataStatus.class.getCanonicalName(), fieldExtensionList, parameters, variableValuesToValidate);
    }

    @Test
    public void testUpdateBusinessObjectDataStatusInvalidBusinessObjectDataVersion() throws Exception
    {
        List<FieldExtension> fieldExtensionList = new ArrayList<>();
        fieldExtensionList.add(buildFieldExtension("businessObjectFormatVersion", "${businessObjectFormatVersion}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectDataVersion", "${businessObjectDataVersion}"));
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(buildParameter("businessObjectFormatVersion", FORMAT_VERSION.toString()));
        parameters.add(buildParameter("businessObjectDataVersion", "NOT_AN_INTEGER"));

        Map<String, Object> variableValuesToValidate = new HashMap<>();
        variableValuesToValidate.put(ActivitiRuntimeHelper.VARIABLE_ERROR_MESSAGE, "\"businessObjectDataVersion\" must be a valid integer value.");

        executeWithoutLogging(ActivitiRuntimeHelper.class, () -> {
            testActivitiServiceTaskFailure(UpdateBusinessObjectDataStatus.class.getCanonicalName(), fieldExtensionList, parameters, variableValuesToValidate);
        });
    }

    @Test
    public void testUpdateBusinessObjectDataStatusInvalidBusinessObjectFormatVersion() throws Exception
    {
        List<FieldExtension> fieldExtensionList = new ArrayList<>();
        fieldExtensionList.add(buildFieldExtension("businessObjectFormatVersion", "${businessObjectFormatVersion}"));
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(buildParameter("businessObjectFormatVersion", "NOT_AN_INTEGER"));

        Map<String, Object> variableValuesToValidate = new HashMap<>();
        variableValuesToValidate.put(ActivitiRuntimeHelper.VARIABLE_ERROR_MESSAGE, "\"businessObjectFormatVersion\" must be a valid integer value.");

        executeWithoutLogging(ActivitiRuntimeHelper.class, () -> {
            testActivitiServiceTaskFailure(UpdateBusinessObjectDataStatus.class.getCanonicalName(), fieldExtensionList, parameters, variableValuesToValidate);
        });
    }

    @Test
    public void testUpdateBusinessObjectDataStatusMissingBusinessObjectDataVersion() throws Exception
    {
        List<FieldExtension> fieldExtensionList = new ArrayList<>();
        fieldExtensionList.add(buildFieldExtension("businessObjectFormatVersion", "${businessObjectFormatVersion}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectDataVersion", "${businessObjectDataVersion}"));
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(buildParameter("businessObjectFormatVersion", FORMAT_VERSION.toString()));
        parameters.add(buildParameter("businessObjectDataVersion", BLANK_TEXT));

        Map<String, Object> variableValuesToValidate = new HashMap<>();
        variableValuesToValidate.put(ActivitiRuntimeHelper.VARIABLE_ERROR_MESSAGE, "\"businessObjectDataVersion\" must be specified.");

        executeWithoutLogging(ActivitiRuntimeHelper.class, () -> {
            testActivitiServiceTaskFailure(UpdateBusinessObjectDataStatus.class.getCanonicalName(), fieldExtensionList, parameters, variableValuesToValidate);
        });
    }

    @Test
    public void testUpdateBusinessObjectDataStatusMissingBusinessObjectFormatVersion() throws Exception
    {
        List<FieldExtension> fieldExtensionList = new ArrayList<>();
        fieldExtensionList.add(buildFieldExtension("businessObjectFormatVersion", "${businessObjectFormatVersion}"));
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(buildParameter("businessObjectFormatVersion", BLANK_TEXT));

        Map<String, Object> variableValuesToValidate = new HashMap<>();
        variableValuesToValidate.put(ActivitiRuntimeHelper.VARIABLE_ERROR_MESSAGE, "\"businessObjectFormatVersion\" must be specified.");

        executeWithoutLogging(ActivitiRuntimeHelper.class, () -> {
            testActivitiServiceTaskFailure(UpdateBusinessObjectDataStatus.class.getCanonicalName(), fieldExtensionList, parameters, variableValuesToValidate);
        });
    }

    @Test
    public void testUpdateBusinessObjectDataStatusMissingOptionalParameters() throws Exception
    {
        // Create a business object data key without sub-partition values.
        BusinessObjectDataKey businessObjectDataKey =
            new BusinessObjectDataKey(BDEF_NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE,
                NO_SUBPARTITION_VALUES, DATA_VERSION);

        // Create a business object data entity.
        businessObjectDataDaoTestHelper.createBusinessObjectDataEntity(businessObjectDataKey, LATEST_VERSION_FLAG_SET, BDATA_STATUS);

        // Create a business object data status entity.
        businessObjectDataStatusDaoTestHelper.createBusinessObjectDataStatusEntity(BDATA_STATUS_2);

        List<FieldExtension> fieldExtensionList = new ArrayList<>();

        fieldExtensionList.add(buildFieldExtension("namespace", "${namespace}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectDefinitionName", "${businessObjectDefinitionName}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectFormatUsage", "${businessObjectFormatUsage}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectFormatFileType", "${businessObjectFormatFileType}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectFormatVersion", "${businessObjectFormatVersion}"));
        fieldExtensionList.add(buildFieldExtension("partitionValue", "${partitionValue}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectDataVersion", "${businessObjectDataVersion}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectDataStatus", "${businessObjectDataStatus}"));

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(buildParameter("namespace", BDEF_NAMESPACE));
        parameters.add(buildParameter("businessObjectDefinitionName", BDEF_NAME));
        parameters.add(buildParameter("businessObjectFormatUsage", FORMAT_USAGE_CODE));
        parameters.add(buildParameter("businessObjectFormatFileType", FORMAT_FILE_TYPE_CODE));
        parameters.add(buildParameter("businessObjectFormatVersion", FORMAT_VERSION.toString()));
        parameters.add(buildParameter("partitionValue", PARTITION_VALUE));
        parameters.add(buildParameter("businessObjectDataVersion", DATA_VERSION.toString()));
        parameters.add(buildParameter("businessObjectDataStatus", BDATA_STATUS_2));

        // Build the expected response object.
        BusinessObjectDataStatusUpdateResponse expectedBusinessObjectDataStatusUpdateResponse =
            new BusinessObjectDataStatusUpdateResponse(businessObjectDataKey, BDATA_STATUS_2, BDATA_STATUS);

        // Run the activiti task and validate the returned response object.
        Map<String, Object> variableValuesToValidate = new HashMap<>();
        variableValuesToValidate
            .put(UpdateBusinessObjectDataStatus.VARIABLE_JSON_RESPONSE, jsonHelper.objectToJson(expectedBusinessObjectDataStatusUpdateResponse));
        testActivitiServiceTaskSuccess(UpdateBusinessObjectDataStatus.class.getCanonicalName(), fieldExtensionList, parameters, variableValuesToValidate);
    }

    @Test
    public void testUpdateBusinessObjectDataStatusMissingOptionalParametersSubPartitionValuesAsEmptyString() throws Exception
    {
        // Create a business object data key without sub-partition values.
        BusinessObjectDataKey businessObjectDataKey =
            new BusinessObjectDataKey(BDEF_NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE,
                NO_SUBPARTITION_VALUES, DATA_VERSION);

        // Create a business object data entity.
        businessObjectDataDaoTestHelper.createBusinessObjectDataEntity(businessObjectDataKey, LATEST_VERSION_FLAG_SET, BDATA_STATUS);

        // Create a business object data status entity.
        businessObjectDataStatusDaoTestHelper.createBusinessObjectDataStatusEntity(BDATA_STATUS_2);

        List<FieldExtension> fieldExtensionList = new ArrayList<>();

        fieldExtensionList.add(buildFieldExtension("namespace", "${namespace}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectDefinitionName", "${businessObjectDefinitionName}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectFormatUsage", "${businessObjectFormatUsage}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectFormatFileType", "${businessObjectFormatFileType}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectFormatVersion", "${businessObjectFormatVersion}"));
        fieldExtensionList.add(buildFieldExtension("partitionValue", "${partitionValue}"));
        fieldExtensionList.add(buildFieldExtension("subPartitionValues", "${subPartitionValues}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectDataVersion", "${businessObjectDataVersion}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectDataStatus", "${businessObjectDataStatus}"));

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(buildParameter("namespace", BDEF_NAMESPACE));
        parameters.add(buildParameter("businessObjectDefinitionName", BDEF_NAME));
        parameters.add(buildParameter("businessObjectFormatUsage", FORMAT_USAGE_CODE));
        parameters.add(buildParameter("businessObjectFormatFileType", FORMAT_FILE_TYPE_CODE));
        parameters.add(buildParameter("businessObjectFormatVersion", FORMAT_VERSION.toString()));
        parameters.add(buildParameter("partitionValue", PARTITION_VALUE));
        parameters.add(buildParameter("subPartitionValues", EMPTY_STRING));
        parameters.add(buildParameter("businessObjectDataVersion", DATA_VERSION.toString()));
        parameters.add(buildParameter("businessObjectDataStatus", BDATA_STATUS_2));

        // Build the expected response object.
        BusinessObjectDataStatusUpdateResponse expectedBusinessObjectDataStatusUpdateResponse =
            new BusinessObjectDataStatusUpdateResponse(businessObjectDataKey, BDATA_STATUS_2, BDATA_STATUS);

        // Run the activiti task and validate the returned response object.
        Map<String, Object> variableValuesToValidate = new HashMap<>();
        variableValuesToValidate
            .put(UpdateBusinessObjectDataStatus.VARIABLE_JSON_RESPONSE, jsonHelper.objectToJson(expectedBusinessObjectDataStatusUpdateResponse));
        testActivitiServiceTaskSuccess(UpdateBusinessObjectDataStatus.class.getCanonicalName(), fieldExtensionList, parameters, variableValuesToValidate);
    }

    @Test
    public void testUpdateBusinessObjectDataStatusMissingRequiredParameter() throws Exception
    {
        // Validate that business object data status service fails when we do not pass a namespace value.
        List<FieldExtension> fieldExtensionList = new ArrayList<>();
        fieldExtensionList.add(buildFieldExtension("businessObjectFormatVersion", "${businessObjectFormatVersion}"));
        fieldExtensionList.add(buildFieldExtension("businessObjectDataVersion", "${businessObjectDataVersion}"));
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(buildParameter("businessObjectFormatVersion", FORMAT_VERSION.toString()));
        parameters.add(buildParameter("businessObjectDataVersion", DATA_VERSION.toString()));

        Map<String, Object> variableValuesToValidate = new HashMap<>();
        variableValuesToValidate.put(ActivitiRuntimeHelper.VARIABLE_ERROR_MESSAGE, "A namespace must be specified.");

        executeWithoutLogging(ActivitiRuntimeHelper.class, () -> {
            testActivitiServiceTaskFailure(UpdateBusinessObjectDataStatus.class.getCanonicalName(), fieldExtensionList, parameters, variableValuesToValidate);
        });
    }
}
