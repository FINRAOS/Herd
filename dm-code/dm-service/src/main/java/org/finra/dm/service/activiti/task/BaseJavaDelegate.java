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

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.finra.dm.dao.helper.DmStringHelper;
import org.finra.dm.dao.helper.JsonHelper;
import org.finra.dm.dao.helper.XmlHelper;
import org.finra.dm.service.activiti.ActivitiHelper;
import org.finra.dm.service.activiti.ActivitiRuntimeHelper;
import org.finra.dm.service.helper.DmDaoHelper;
import org.finra.dm.service.helper.DmErrorInformationExceptionHandler;
import org.finra.dm.service.helper.DmHelper;

/**
 * This class handles the core flow for our Activiti "JavaDelegate" tasks and calls back sub-classes for the actual task implementation. All of our custom tasks
 * should extend this class.
 * <p/>
 * WARNING: When Java Delegates make service calls, those service calls should all take place within a new transaction to ensure Activiti can set workflow
 * variables upon errors and have those workflow variables committed to the database. If they don't take place within a new transaction, it is possible that the
 * calling code could roll back the entire transaction and the workflow variables wouldn't get updated. Service methods can occur within a new transaction by
 * annotating the service method with "@Transactional(propagation = Propagation.REQUIRES_NEW)". Note that JUnit invocations of those same services don't require
 * their own transaction since we usually want JUnits to roll back all their data. For those situations, we can provide an alternate service implementation that
 * extends the normal service implementation and simply doesn't annotate the service method with the "requires new" annotation.
 */
public abstract class BaseJavaDelegate implements JavaDelegate
{
    private static final Logger LOGGER = Logger.getLogger(BaseJavaDelegate.class);

    @Autowired
    protected DmHelper dmHelper;

    @Autowired
    protected XmlHelper xmlHelper;

    @Autowired
    protected JsonHelper jsonHelper;

    @Autowired
    protected DmDaoHelper dmDaoHelper;

    @Autowired
    protected DmStringHelper daoHelper;

    /**
     * Variable that is set in workflow for the json response.
     */
    public static final String VARIABLE_JSON_RESPONSE = "jsonResponse";

    // A variable we use to know whether this class (i.e. sub-classes) have had Spring initialized (e.g. auto-wiring) since we need to do it manually
    // given that the delegate tasks are created by Activiti as non-Spring beans. The DmDelegateInterceptor performs the initialization.
    private boolean springInitialized;

    @Autowired
    protected ActivitiHelper activitiHelper;

    @Autowired
    protected ActivitiRuntimeHelper activitiRuntimeHelper;

    @Autowired
    @Qualifier("dmErrorInformationExceptionHandler") // This is to ensure we get the base class bean rather than any classes that extend it.
    private DmErrorInformationExceptionHandler errorInformationExceptionHandler;

    /**
     * The execution implementation. Sub-classes should override this method for their specific task implementation.
     *
     * @param execution the delegation execution.
     *
     * @throws Exception when a problem is encountered. A BpmnError should be thrown when there is a problem that should be handled by the workflow. All other
     *             errors will be considered system errors that will be logged.
     */
    public abstract void executeImpl(DelegateExecution execution) throws Exception;

    /**
     * This is what Activiti will call to execute this task. Sub-classes should override the executeImpl method to supply the actual implementation.
     *
     * @param execution the execution information.
     *
     * @throws Exception if any errors were encountered.
     */
    @Override
    public final void execute(DelegateExecution execution) throws Exception
    {
        try
        {
            // Check if method is not allowed.
            dmDaoHelper.checkNotAllowedMethod(this.getClass().getCanonicalName());

            // Perform the execution implementation handled in the sub-class.
            executeImpl(execution);

            // Set a success status as a workflow variable.
            activitiRuntimeHelper.setTaskSuccessInWorkflow(execution);
        }
        catch (Exception ex)
        {
            handleException(execution, ex);
        }
    }

    /**
     * Handles any exception thrown by an Activiti task.
     * 
     * @param execution The execution which identifies the task.
     * @param exception The exception that has been thrown
     * @throws Exception Some exceptions may choose to bubble up the exception
     */
    protected void handleException(DelegateExecution execution, Exception exception) throws Exception
    {
        // Set the error status and error message as workflow variables.
        activitiRuntimeHelper.setTaskErrorInWorkflow(execution, exception.getMessage());

        // Continue throwing the original exception and let workflow handle it with a Boundary event handler.
        if (exception instanceof BpmnError)
        {
            throw exception;
        }

        // Log the error if the exception should be reported.
        if (errorInformationExceptionHandler.isReportableError(exception))
        {
            LOGGER.error(
                activitiHelper.getProcessIdentifyingInformation(execution) + " Unexpected error occurred during task \"" + getClass().getSimpleName() +
                    "\".", exception);
        }
    }

    /**
     * Sets a JSON response object as a workflow variable.
     *
     * @param responseObject the JSON object.
     * @param execution the delegate execution.
     *
     * @throws Exception if any problems were encountered.
     */
    public void setJsonResponseAsWorkflowVariable(Object responseObject, DelegateExecution execution) throws Exception
    {
        String jsonResponse = jsonHelper.objectToJson(responseObject);
        setTaskWorkflowVariable(execution, VARIABLE_JSON_RESPONSE, jsonResponse);
    }

    public void setJsonResponseAsWorkflowVariable(Object responseObject, String executionId, String activitiId) throws Exception
    {
        String jsonResponse = jsonHelper.objectToJson(responseObject);
        setTaskWorkflowVariable(executionId, activitiId, VARIABLE_JSON_RESPONSE, jsonResponse);
    }

    public boolean isSpringInitialized()
    {
        return springInitialized;
    }

    public void setSpringInitialized(boolean springInitialized)
    {
        this.springInitialized = springInitialized;
    }

    /**
     * Sets the workflow variable with task id prefixed.
     *
     * @param execution, the delegate execution.
     * @param variableName, the variable name
     * @param variableValue, the variable value
     */
    protected void setTaskWorkflowVariable(DelegateExecution execution, String variableName, Object variableValue)
    {
        activitiRuntimeHelper.setTaskWorkflowVariable(execution, variableName, variableValue);
    }

    protected void setTaskWorkflowVariable(String executionId, String activitiId, String variableName, Object variableValue)
    {
        activitiRuntimeHelper.setTaskWorkflowVariable(executionId, activitiId, variableName, variableValue);
    }

    /**
     * Converts the request string to xsd object.
     *
     * @param contentType the content type "xml" or "json"
     * @param requestString the request string
     * @param xsdClass the xsd class of the object to convert to
     * @param <T> the type of the returned object.
     *
     * @return the request object.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <T> T getRequestObject(String contentType, String requestString, Class xsdClass)
    {
        T request = null;
        if (contentType.equalsIgnoreCase("xml"))
        {
            try
            {
                request = (T) xmlHelper.unmarshallXmlToObject(xsdClass, requestString);
            }
            catch (Exception ex)
            {
                throw new IllegalArgumentException("\"" + xsdClass.getSimpleName() + "\" must be valid xml string.", ex);
            }
        }
        else if (contentType.equalsIgnoreCase("json"))
        {
            try
            {
                request = (T) jsonHelper.unmarshallJsonToObject(xsdClass, requestString);
            }
            catch (Exception ex)
            {
                throw new IllegalArgumentException("\"" + xsdClass.getSimpleName() + "\" must be valid json string.", ex);
            }
        }
        else
        {
            throw new IllegalArgumentException("\"ContentType\" must be a valid value of either \"xml\" or \"json\".");
        }

        return request;
    }
}