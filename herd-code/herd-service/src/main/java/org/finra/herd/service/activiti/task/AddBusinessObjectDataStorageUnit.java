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

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.finra.herd.model.api.xml.BusinessObjectDataStorageUnitCreateRequest;
import org.finra.herd.model.api.xml.BusinessObjectDataStorageUnitCreateResponse;
import org.finra.herd.service.BusinessObjectDataStorageUnitService;

/**
 * An Activiti task that adds storage unit to an existing business object data for an existing storage.
 * <p/>
 * <p/>
 * <pre>
 * <extensionElements>
 *   <activiti:field name="contentType" stringValue=""/>
 *   <activiti:field name="businessObjectDataStorageUnitCreateRequest" stringValue=""/>
 * </extensionElements>
 * </pre>
 */
@Component
public class AddBusinessObjectDataStorageUnit extends BaseJavaDelegate
{
    private Expression businessObjectDataStorageUnitCreateRequest;

    @Autowired
    private BusinessObjectDataStorageUnitService businessObjectDataStorageUnitService;

    private Expression contentType;

    @Override
    public void executeImpl(DelegateExecution execution) throws Exception
    {
        // Get expression variables from the execution.
        String contentTypeString = activitiHelper.getRequiredExpressionVariableAsString(contentType, execution, "ContentType").trim();
        String requestString = activitiHelper
            .getRequiredExpressionVariableAsString(businessObjectDataStorageUnitCreateRequest, execution, "BusinessObjectDataStorageUnitCreateRequest").trim();

        // Create a business object data storage unit create request.
        BusinessObjectDataStorageUnitCreateRequest request =
            getRequestObject(contentTypeString, requestString, BusinessObjectDataStorageUnitCreateRequest.class);

        // Call the business object data storage unit service.
        BusinessObjectDataStorageUnitCreateResponse businessObjectDataStorageUnitCreateResponse =
            businessObjectDataStorageUnitService.createBusinessObjectDataStorageUnit(request);

        // Set the JSON response as a workflow variable.
        setJsonResponseAsWorkflowVariable(businessObjectDataStorageUnitCreateResponse, execution);
    }
}
