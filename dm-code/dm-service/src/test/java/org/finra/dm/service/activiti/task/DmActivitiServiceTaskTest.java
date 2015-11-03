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
package org.finra.dm.service.activiti.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.history.HistoricProcessInstance;

import org.finra.dm.model.api.xml.Job;
import org.finra.dm.model.api.xml.Parameter;
import org.finra.dm.service.AbstractServiceTest;
import org.finra.dm.service.activiti.ActivitiRuntimeHelper;

/**
 * Base class to tests the Activiti tasks for services.
 */
public abstract class DmActivitiServiceTaskTest extends AbstractServiceTest
{
    private final String serviceTaskId = "testServiceTask";

    public final String VARIABLE_VALUE_NOT_NULL = "NOT_NULL";
    public final String VARIABLE_VALUE_IS_NULL = "IS_NULL";

    protected FieldExtension buildFieldExtension(String name, String expression)
    {
        FieldExtension exceptionField = new FieldExtension();
        exceptionField.setFieldName(name);
        exceptionField.setExpression(expression);
        return exceptionField;
    }

    protected Parameter buildParameter(String name, String value)
    {
        return new Parameter(name, value);
    }

    protected String buildActivitiXml(String implementation, List<FieldExtension> fieldExtensionList) throws Exception
    {
        BpmnModel bpmnModel = getBpmnModelForXmlResource(ACTIVITI_XML_TEST_SERVICE_TASK_WITH_CLASSPATH);

        ServiceTask serviceTask = (ServiceTask) bpmnModel.getProcesses().get(0).getFlowElement(serviceTaskId);

        serviceTask.setImplementation(implementation);
        serviceTask.getFieldExtensions().addAll(fieldExtensionList);

        return new String(new BpmnXMLConverter().convertToXML(bpmnModel));
    }

    protected Job testActivitiServiceTaskSuccess(String implementation, List<FieldExtension> fieldExtensionList, List<Parameter> parameters,
        Map<String, Object> variableValuesToValidate) throws Exception
    {
        String activitiXml = buildActivitiXml(implementation, fieldExtensionList);
        return createJobAndCheckTaskStatusSuccess(activitiXml, parameters, variableValuesToValidate);
    }

    private Job createJobAndCheckTaskStatusSuccess(String activitiXml, List<Parameter> parameters, Map<String, Object> variableValuesToValidate)
        throws Exception
    {
        Job job = createJobFromActivitiXml(activitiXml, parameters);
        assertNotNull(job);

        HistoricProcessInstance hisInstance =
            activitiHistoryService.createHistoricProcessInstanceQuery().processInstanceId(job.getId()).includeProcessVariables().singleResult();
        Map<String, Object> variables = hisInstance.getProcessVariables();

        String serviceTaskStatus = (String) variables.get(getServiceTaskVariableName(ActivitiRuntimeHelper.VARIABLE_STATUS));
        assertEquals(ActivitiRuntimeHelper.TASK_STATUS_SUCCESS, serviceTaskStatus);

        if (variableValuesToValidate != null)
        {
            for (Map.Entry<String, Object> varEntry : variableValuesToValidate.entrySet())
            {
                Object wfVariableValue = variables.get(getServiceTaskVariableName(varEntry.getKey()));
                Object expectedVariableValue = varEntry.getValue();
                if (expectedVariableValue.equals(VARIABLE_VALUE_NOT_NULL))
                {
                    assertNotNull(wfVariableValue);
                }
                else if (expectedVariableValue.equals(VARIABLE_VALUE_IS_NULL))
                {
                    assertNull(wfVariableValue);
                }
                else
                {
                    assertEquals(expectedVariableValue, wfVariableValue);
                }
            }
        }

        return job;
    }

    protected Job testActivitiServiceTaskFailure(String implementation, List<FieldExtension> fieldExtensionList, List<Parameter> parameters,
        Map<String, Object> variableValuesToValidate) throws Exception
    {
        String activitiXml = buildActivitiXml(implementation, fieldExtensionList);
        return createJobAndCheckTaskStatusFailure(activitiXml, parameters, variableValuesToValidate);
    }

    private Job createJobAndCheckTaskStatusFailure(String activitiXml, List<Parameter> parameters, Map<String, Object> variableValuesToValidate)
        throws Exception
    {
        Job job = createJobFromActivitiXml(activitiXml, parameters);
        assertNotNull(job);

        HistoricProcessInstance hisInstance =
            activitiHistoryService.createHistoricProcessInstanceQuery().processInstanceId(job.getId()).includeProcessVariables().singleResult();
        Map<String, Object> variables = hisInstance.getProcessVariables();

        String serviceTaskStatus = (String) variables.get(getServiceTaskVariableName(ActivitiRuntimeHelper.VARIABLE_STATUS));
        assertEquals(ActivitiRuntimeHelper.TASK_STATUS_ERROR, serviceTaskStatus);

        if (variableValuesToValidate != null)
        {
            for (Map.Entry<String, Object> varEntry : variableValuesToValidate.entrySet())
            {
                Object wfVariableValue = variables.get(getServiceTaskVariableName(varEntry.getKey()));
                Object expectedVariableValue = varEntry.getValue();
                if (expectedVariableValue.equals(VARIABLE_VALUE_NOT_NULL))
                {
                    assertNotNull(wfVariableValue);
                }
                else if (expectedVariableValue.equals(VARIABLE_VALUE_IS_NULL))
                {
                    assertNull(wfVariableValue);
                }
                else
                {
                    assertEquals(expectedVariableValue, wfVariableValue);
                }
            }
        }

        return job;
    }

    protected String getServiceTaskVariableName(String variableName)
    {
        return serviceTaskId + ActivitiRuntimeHelper.TASK_VARIABLE_MARKER + variableName;
    }
}