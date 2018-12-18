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

import java.util.Collections;
import java.util.stream.Collectors;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import org.finra.herd.dao.JobDefinitionDao;
import org.finra.herd.dao.helper.HerdStringHelper;
import org.finra.herd.dao.helper.JsonHelper;
import org.finra.herd.dao.helper.XmlHelper;
import org.finra.herd.model.ObjectNotFoundException;
import org.finra.herd.model.dto.ApplicationUser;
import org.finra.herd.model.dto.JobDefinitionAlternateKeyDto;
import org.finra.herd.model.dto.SecurityUserWrapper;
import org.finra.herd.model.jpa.JobDefinitionEntity;
import org.finra.herd.service.ActivitiService;
import org.finra.herd.service.activiti.ActivitiHelper;
import org.finra.herd.service.activiti.ActivitiRuntimeHelper;
import org.finra.herd.service.helper.ConfigurationDaoHelper;
import org.finra.herd.service.helper.HerdErrorInformationExceptionHandler;
import org.finra.herd.service.helper.JobDefinitionDaoHelper;
import org.finra.herd.service.helper.JobDefinitionHelper;
import org.finra.herd.service.helper.UserNamespaceAuthorizationHelper;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseJavaDelegate.class);

    // MDC property key.  It can be referenced in a log4j.xml configuration.
    private static final String ACTIVITI_PROCESS_INSTANCE_ID_KEY = "activitiProcessInstanceId";

    private static final String USER_ID_KEY = "uid";

    private static final String ACTIVITI_LOG_MESSAGE_PREFIX = "HerdTimingLog timingSource=Activiti";

    @Autowired
    protected ActivitiService activitiService;

    @Autowired
    protected ConfigurationDaoHelper configurationDaoHelper;

    @Autowired
    protected HerdStringHelper daoHelper;

    @Autowired
    protected HerdStringHelper herdStringHelper;

    @Autowired
    protected JobDefinitionDao jobDefinitionDao;

    @Autowired
    protected JobDefinitionDaoHelper jobDefinitionDaoHelper;

    @Autowired
    protected JobDefinitionHelper jobDefinitionHelper;

    @Autowired
    protected JsonHelper jsonHelper;

    @Autowired
    protected UserNamespaceAuthorizationHelper userNamespaceAuthorizationHelper;

    @Autowired
    protected XmlHelper xmlHelper;

    /**
     * Variable that is set in workflow for the json response.
     */
    public static final String VARIABLE_JSON_RESPONSE = "jsonResponse";

    // A variable we use to know whether this class (i.e. sub-classes) have had Spring initialized (e.g. auto-wiring) since we need to do it manually
    // given that the delegate tasks are created by Activiti as non-Spring beans. The HerdDelegateInterceptor performs the initialization.
    private boolean springInitialized;

    @Autowired
    protected ActivitiHelper activitiHelper;

    @Autowired
    protected ActivitiRuntimeHelper activitiRuntimeHelper;

    @Autowired
    @Qualifier("herdErrorInformationExceptionHandler") // This is to ensure we get the base class bean rather than any classes that extend it.
    private HerdErrorInformationExceptionHandler errorInformationExceptionHandler;

    /**
     * The execution implementation. Sub-classes should override this method for their specific task implementation.
     *
     * @param execution the delegation execution.
     *
     * @throws Exception when a problem is encountered. A BpmnError should be thrown when there is a problem that should be handled by the workflow. All other
     * errors will be considered system errors that will be logged.
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
        long taskBeginTimeMillis = 0;
        ApplicationUser applicationUser;
        boolean taskSuccessFlag = false;
        try
        {
            // Need to clear the security context here since the current thread may have been reused,
            // which may might have left over its security context. If we do not clear the security
            // context, any subsequent calls may be restricted by the permissions given
            // to the previous thread's security context.
            SecurityContextHolder.clearContext();

            // Check if method is not allowed.
            configurationDaoHelper.checkNotAllowedMethod(this.getClass().getCanonicalName());

            // Set the security context per last updater of the current process instance's job definition.
            applicationUser = getApplicationUser(execution);
            setSecurityContext(applicationUser);

            // Set the MDC property for the Activiti process instance ID and user ID.
            MDC.put(ACTIVITI_PROCESS_INSTANCE_ID_KEY, "activitiProcessInstanceId=" + execution.getProcessInstanceId());
            MDC.put(USER_ID_KEY, "userId=" + (applicationUser.getUserId() == null ? "" : applicationUser.getUserId()));

            // Log all input variables from the execution (before the execution starts).
            logInputParameters(execution);

            // Set the task begin time
            taskBeginTimeMillis = System.currentTimeMillis();

            // Perform the execution implementation handled in the sub-class.
            executeImpl(execution);

            // Set a success status as a workflow variable.
            activitiRuntimeHelper.setTaskSuccessInWorkflow(execution);

            // Set the flag to true since there is no exception thrown
            taskSuccessFlag = true;
        }
        catch (Exception ex)
        {
            handleException(execution, ex);
        }
        finally
        {
            // Log the task execution time
            logTaskExecutionTime(taskBeginTimeMillis, taskSuccessFlag);

            // Remove the MDC property to ensure they don't accidentally get used by anybody else.
            MDC.remove(ACTIVITI_PROCESS_INSTANCE_ID_KEY);
            MDC.remove(USER_ID_KEY);

            // Clear up the security context.
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Logs the Activiti task execution time
     *
     * @param taskBeginTimeMillis the task begin time in millisecond
     * @param taskSuccessFlag the success flag for the task
     */
    protected void logTaskExecutionTime(long taskBeginTimeMillis, boolean taskSuccessFlag)
    {
        StringBuilder message = new StringBuilder();

        // Append the log message prefix.
        message.append(ACTIVITI_LOG_MESSAGE_PREFIX);

        // Append the Activiti task name
        message.append(" task=" + this.getClass().getName());

        // Append the task success flag
        message.append(" success=").append(taskSuccessFlag);

        // Append response time
        message.append(" responseTimeMillis=").append(System.currentTimeMillis() - taskBeginTimeMillis);

        LOGGER.info(message.toString());
    }

    /**
     * Sets the security context per last updater of the current process instance's job definition.
     *
     * @param applicationUser the application user
     */
    protected void setSecurityContext(ApplicationUser applicationUser)
    {
        userNamespaceAuthorizationHelper.buildNamespaceAuthorizations(applicationUser);
        SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken(
            new SecurityUserWrapper(applicationUser.getUserId(), "", true, true, true, true, Collections.emptyList(), applicationUser), null));
    }

    /**
     * Retrieves application user per last updater of the current process instance's job definition.
     *
     * @param execution the delegate execution
     *
     * @return the application user
     */
    protected ApplicationUser getApplicationUser(DelegateExecution execution)
    {
        String processDefinitionId = execution.getProcessDefinitionId();

        // Get process definition by process definition ID from Activiti.
        ProcessDefinition processDefinition = activitiService.getProcessDefinitionById(processDefinitionId);

        // Validate that we retrieved the process definition from Activiti.
        if (processDefinition == null)
        {
            throw new ObjectNotFoundException(String.format("Failed to find Activiti process definition for processDefinitionId=\"%s\".", processDefinitionId));
        }

        // Retrieve the process definition key.
        String processDefinitionKey = processDefinition.getKey();

        // Get the job definition key.
        JobDefinitionAlternateKeyDto jobDefinitionKey = jobDefinitionHelper.getJobDefinitionKey(processDefinitionKey);

        // Get the job definition from the Herd repository and validate that it exists.
        JobDefinitionEntity jobDefinitionEntity = jobDefinitionDaoHelper.getJobDefinitionEntity(jobDefinitionKey.getNamespace(), jobDefinitionKey.getJobName());

        // Set the security context per last updater of the job definition.
        String updatedByUserId = jobDefinitionEntity.getUpdatedBy();
        ApplicationUser applicationUser = new ApplicationUser(getClass());
        applicationUser.setUserId(updatedByUserId);

        return applicationUser;
    }

    /**
     * Handles any exception thrown by an Activiti task.
     *
     * @param execution The execution which identifies the task.
     * @param exception The exception that has been thrown
     *
     * @throws Exception Some exceptions may choose to bubble up the exception
     */
    protected void handleException(DelegateExecution execution, Exception exception) throws Exception
    {
        // Set the error status and stack trace as workflow variables.
        activitiRuntimeHelper.setTaskErrorInWorkflow(execution, exception.getMessage(), exception);

        // Continue throwing the original exception and let workflow handle it with a Boundary event handler.
        if (exception instanceof BpmnError)
        {
            throw exception;
        }

        // Log the error if the exception should be reported.
        if (errorInformationExceptionHandler.isReportableError(exception))
        {
            LOGGER.error("{} Unexpected error occurred during task. activitiTaskName=\"{}\"", activitiHelper.getProcessIdentifyingInformation(execution),
                getClass().getSimpleName(), exception);
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
     * @param execution the delegate execution.
     * @param variableName the variable name
     * @param variableValue the variable value
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
        T request;
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

    /**
     * Loops through all process variables and logs them.
     *
     * @param execution the execution information
     */
    protected void logInputParameters(DelegateExecution execution)
    {
        LOGGER.info("{} Input parameters for {}: {}", activitiHelper.getProcessIdentifyingInformation(execution), this.getClass().getName(),
            execution.getVariables().entrySet().stream().map(entry -> entry.getKey() + "=" + jsonHelper.objectToJson(entry.getValue()))
                .collect(Collectors.joining(" ")));
    }
}
