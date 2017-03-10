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
package org.finra.herd.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;

import org.finra.herd.model.ObjectNotFoundException;
import org.finra.herd.model.api.xml.Job;
import org.finra.herd.model.api.xml.JobActionEnum;
import org.finra.herd.model.api.xml.JobCreateRequest;
import org.finra.herd.model.api.xml.JobDefinitionCreateRequest;
import org.finra.herd.model.api.xml.JobDeleteRequest;
import org.finra.herd.model.api.xml.JobSignalRequest;
import org.finra.herd.model.api.xml.JobStatusEnum;
import org.finra.herd.model.api.xml.JobSummaries;
import org.finra.herd.model.api.xml.JobSummary;
import org.finra.herd.model.api.xml.JobUpdateRequest;
import org.finra.herd.model.api.xml.NamespaceAuthorization;
import org.finra.herd.model.api.xml.NamespacePermissionEnum;
import org.finra.herd.model.api.xml.Parameter;
import org.finra.herd.model.dto.ApplicationUser;
import org.finra.herd.model.dto.ConfigurationValue;
import org.finra.herd.model.dto.SecurityUserWrapper;

/**
 * This class tests various functionality within the Job REST controller.
 */
public class JobRestControllerTest extends AbstractRestTest
{
    @Test
    public void testCreateJob() throws Exception
    {
        // Create the namespace entity.
        namespaceDaoTestHelper.createNamespaceEntity(TEST_ACTIVITI_NAMESPACE_CD);

        // Create a job definition create request using hard coded test values.
        JobDefinitionCreateRequest jobDefinitionCreateRequest = jobDefinitionServiceTestHelper.createJobDefinitionCreateRequest();

        // Create the job definition.
        jobDefinitionService.createJobDefinition(jobDefinitionCreateRequest, false);

        // Create a job create request using hard coded test values.
        JobCreateRequest jobCreateRequest = jobServiceTestHelper.createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME);

        // Create the job.
        Job resultJob = jobRestController.createJob(jobCreateRequest);

        // Validate the results.
        assertNotNull(resultJob);
        assertNotNull(resultJob.getId());
        assertTrue(!resultJob.getId().isEmpty());
        assertEquals(TEST_ACTIVITI_NAMESPACE_CD, resultJob.getNamespace());
        assertEquals(TEST_ACTIVITI_JOB_NAME, resultJob.getJobName());
        assertEquals(jobDefinitionCreateRequest.getParameters().size() + jobCreateRequest.getParameters().size() + 1, resultJob.getParameters().size());
        List<String> expectedParameters = new ArrayList<>();
        expectedParameters.addAll(parametersToStringList(jobDefinitionCreateRequest.getParameters()));
        expectedParameters.addAll(parametersToStringList(jobCreateRequest.getParameters()));
        expectedParameters.addAll(parametersToStringList(
            Arrays.asList(new Parameter(HERD_WORKFLOW_ENVIRONMENT, configurationHelper.getProperty(ConfigurationValue.HERD_ENVIRONMENT)))));

        List<String> resultParameters = parametersToStringList(resultJob.getParameters());
        assertTrue(expectedParameters.containsAll(resultParameters));
        assertTrue(resultParameters.containsAll(expectedParameters));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateJobDuplicateParameterName() throws Exception
    {
        // Create a job create request using hard coded test values.
        JobCreateRequest jobCreateRequest = jobServiceTestHelper.createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME);

        // Add a duplicate parameter.
        Parameter parameter = new Parameter();
        parameter.setName(addWhitespace(jobCreateRequest.getParameters().get(0).getName().toUpperCase()));
        parameter.setValue(jobCreateRequest.getParameters().get(0).getValue());
        jobCreateRequest.getParameters().add(parameter);

        // Try to create a job.
        jobRestController.createJob(jobCreateRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateJobJobNameEmpty() throws Exception
    {
        // Try to create a job by passing an empty job name.
        jobRestController.createJob(jobServiceTestHelper.createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, " "));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testCreateJobJobNameNoExists() throws Exception
    {
        // Create the namespace entity.
        namespaceDaoTestHelper.createNamespaceEntity(TEST_ACTIVITI_NAMESPACE_CD);

        // Try to create a job using non-existing job definition name.
        jobRestController.createJob(jobServiceTestHelper.createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, "I_DO_NOT_EXIST"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateJobNamespaceEmpty() throws Exception
    {
        // Try to create a job by passing an empty namespace code.
        jobRestController.createJob(jobServiceTestHelper.createJobCreateRequest(" ", TEST_ACTIVITI_JOB_NAME));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testCreateJobNamespaceNoExists() throws Exception
    {
        // Try to create a job using non-existing namespace code.
        jobRestController.createJob(jobServiceTestHelper.createJobCreateRequest("I_DO_NOT_EXIST", TEST_ACTIVITI_JOB_NAME));
    }

    @Test
    public void testCreateJobNoParams() throws Exception
    {
        // Create the namespace entity.
        namespaceDaoTestHelper.createNamespaceEntity(TEST_ACTIVITI_NAMESPACE_CD);

        // Create a job definition create request using hard coded test values.
        JobDefinitionCreateRequest jobDefinitionCreateRequest = jobDefinitionServiceTestHelper.createJobDefinitionCreateRequest();
        jobDefinitionCreateRequest.setParameters(null);

        // Create the job definition.
        jobDefinitionService.createJobDefinition(jobDefinitionCreateRequest, false);

        // Create a job create request using hard coded test values.
        JobCreateRequest jobCreateRequest = jobServiceTestHelper.createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME);
        jobCreateRequest.setParameters(null);

        // Create the job.
        Job resultJob = jobRestController.createJob(jobCreateRequest);

        //expected default parameter
        List<Parameter> expectedParameters =
            Arrays.asList(new Parameter(HERD_WORKFLOW_ENVIRONMENT, configurationHelper.getProperty(ConfigurationValue.HERD_ENVIRONMENT)));

        // Validate the results.
        assertNotNull(resultJob);
        assertNotNull(resultJob.getId());
        assertTrue(!resultJob.getId().isEmpty());
        assertEquals(TEST_ACTIVITI_NAMESPACE_CD, resultJob.getNamespace());
        assertEquals(TEST_ACTIVITI_JOB_NAME, resultJob.getJobName());
        assertNotNull(resultJob.getParameters());
        assertTrue(resultJob.getParameters().size() == 1);
        assertTrue(expectedParameters.containsAll(resultJob.getParameters()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateJobParameterNameEmpty() throws Exception
    {
        // Create a job create request using hard coded test values.
        JobCreateRequest jobCreateRequest = jobServiceTestHelper.createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME);

        // Add a parameter with an empty name.
        Parameter parameter = new Parameter(" ", ATTRIBUTE_VALUE_1);
        jobCreateRequest.getParameters().add(parameter);

        // Try to create a job.
        jobRestController.createJob(jobCreateRequest);
    }

    @Test
    public void testDeleteJob() throws Exception
    {
        jobDefinitionServiceTestHelper.createJobDefinition(ACTIVITI_XML_TEST_RECEIVE_TASK_WITH_CLASSPATH);
        Job job = jobService.createAndStartJob(jobServiceTestHelper.createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME));
        JobDeleteRequest jobDeleteRequest = new JobDeleteRequest();
        jobDeleteRequest.setDeleteReason("test delete reason");
        Job deletedJob = jobRestController.deleteJob(job.getId(), jobDeleteRequest);
        assertNotNull(deletedJob);
    }

    @Test
    public void testGetJob() throws Exception
    {
        jobDefinitionServiceTestHelper.createJobDefinition(ACTIVITI_XML_TEST_USER_TASK_WITH_CLASSPATH);

        Job job = jobService.createAndStartJob(jobServiceTestHelper.createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME));

        String activitiXml = IOUtils.toString(resourceLoader.getResource(ACTIVITI_XML_TEST_USER_TASK_WITH_CLASSPATH).getInputStream());
        // Job should be waiting at User task.

        // Running job with verbose
        Job jobGet = jobRestController.getJob(job.getId(), true);
        assertEquals(JobStatusEnum.RUNNING, jobGet.getStatus());
        assertNotNull(jobGet.getActivitiJobXml());
        assertEquals(activitiXml, jobGet.getActivitiJobXml());
        assertTrue(jobGet.getCompletedWorkflowSteps().size() > 0);
        assertEquals("usertask1", jobGet.getCurrentWorkflowStep().getId());

        // Running job with non verbose
        jobGet = jobRestController.getJob(job.getId(), false);
        assertEquals(JobStatusEnum.RUNNING, jobGet.getStatus());
        assertNull(jobGet.getActivitiJobXml());
        assertTrue(CollectionUtils.isEmpty(jobGet.getCompletedWorkflowSteps()));
        assertEquals("usertask1", jobGet.getCurrentWorkflowStep().getId());

        // Query the pending task and complete it
        List<Task> tasks = activitiTaskService.createTaskQuery().processInstanceId(job.getId()).list();

        activitiTaskService.complete(tasks.get(0).getId());

        // Job should have been completed.

        // Completed job with verbose
        jobGet = jobRestController.getJob(job.getId(), true);
        assertEquals(JobStatusEnum.COMPLETED, jobGet.getStatus());
        assertNotNull(jobGet.getActivitiJobXml());
        assertEquals(activitiXml, jobGet.getActivitiJobXml());
        assertTrue(jobGet.getCompletedWorkflowSteps().size() > 0);
        assertNull(jobGet.getCurrentWorkflowStep());

        // Completed job with non verbose
        jobGet = jobRestController.getJob(job.getId(), false);
        assertEquals(JobStatusEnum.COMPLETED, jobGet.getStatus());
        assertNull(jobGet.getActivitiJobXml());
        assertTrue(CollectionUtils.isEmpty(jobGet.getCompletedWorkflowSteps()));
        assertNull(jobGet.getCurrentWorkflowStep());
    }

    @Test
    public void testGetJobIntermediateTimer() throws Exception
    {
        jobDefinitionServiceTestHelper.createJobDefinition(ACTIVITI_XML_HERD_INTERMEDIATE_TIMER_WITH_CLASSPATH);

        Job job = jobService.createAndStartJob(jobServiceTestHelper.createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME));

        String activitiXml = IOUtils.toString(resourceLoader.getResource(ACTIVITI_XML_HERD_INTERMEDIATE_TIMER_WITH_CLASSPATH).getInputStream());
        // Job should be waiting at User task.

        // Get job status
        Job jobGet = jobRestController.getJob(job.getId(), true);
        assertEquals(JobStatusEnum.RUNNING, jobGet.getStatus());
        assertNotNull(jobGet.getActivitiJobXml());
        assertEquals(activitiXml, jobGet.getActivitiJobXml());
        assertTrue(jobGet.getCompletedWorkflowSteps().size() > 0);
        // Current workflow step will be null
        assertNull(jobGet.getCurrentWorkflowStep());

        org.activiti.engine.runtime.Job timer = activitiManagementService.createJobQuery().processInstanceId(job.getId()).timers().singleResult();
        if (timer != null)
        {
            activitiManagementService.executeJob(timer.getId());
        }

        // Get the job status again. job should have completed now.
        jobGet = jobRestController.getJob(job.getId(), false);
        assertEquals(JobStatusEnum.COMPLETED, jobGet.getStatus());
        assertNull(jobGet.getCurrentWorkflowStep());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetJobNoJobFound() throws Exception
    {
        jobRestController.getJob("job_not_submitted", true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetJobNoJobId() throws Exception
    {
        jobRestController.getJob(null, false);
    }

    /**
     * This is a happy path test for the REST endpoint. All the detailed tests are in the service tier.
     */
    @Test
    public void testGetJobs() throws Exception
    {
        // Delete all jobs from the history table so we start clean.
        deleteAllHistoricJobs();

        // There should be no jobs initially.
        JobSummaries jobSummaries = jobRestController.getJobs(NO_NAMESPACE, NO_ACTIVITI_JOB_NAME, NO_ACTIVITI_JOB_STATUS, NO_START_TIME, NO_END_TIME);
        assertEquals(0, jobSummaries.getJobSummaries().size());

        // Create a "standard" job definition we can run a job against.
        jobDefinitionServiceTestHelper.createJobDefinition(ACTIVITI_XML_TEST_USER_TASK_WITH_CLASSPATH);

        ApplicationUser applicationUser = new ApplicationUser(getClass());
        applicationUser.setNamespaceAuthorizations(
            new HashSet<>(Arrays.asList(new NamespaceAuthorization(TEST_ACTIVITI_NAMESPACE_CD, Arrays.asList(NamespacePermissionEnum.values())))));
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken(new SecurityUserWrapper("username", "", true, true, true, true, Collections.emptyList(), applicationUser), null));

        // Create and start a job that will wait at a User task which will keep it running. Then complete the wait task which will cause the job to complete.
        Job completedJob = jobService.createAndStartJob(jobServiceTestHelper.createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME));
        List<Task> tasks = activitiTaskService.createTaskQuery().processInstanceId(completedJob.getId()).list();
        activitiTaskService.complete(tasks.get(0).getId());

        // Query all the jobs which will have all 3 jobs created.
        jobSummaries = jobRestController.getJobs(NO_NAMESPACE, NO_ACTIVITI_JOB_NAME, NO_ACTIVITI_JOB_STATUS, NO_START_TIME, NO_END_TIME);
        assertEquals(1, jobSummaries.getJobSummaries().size());

        // Get the job summary and perform some validation.
        JobSummary jobSummary = jobSummaries.getJobSummaries().get(0);
        assertNotNull(jobSummary.getStartTime()); // Ensure a start time is present (all jobs must have a start time).
        assertNotNull(jobSummary.getEndTime()); // Ensure that the job has an end time since it's completed.
        assertEquals(TEST_ACTIVITI_NAMESPACE_CD, jobSummary.getNamespace()); // Namespace should match the query filter.
        assertEquals(TEST_ACTIVITI_JOB_NAME, jobSummary.getJobName()); // Job name should match the query filter.
        assertEquals(JobStatusEnum.COMPLETED, jobSummary.getStatus()); // Make sure the status is running to match the filter.
        assertEquals(0, jobSummary.getTotalExceptions()); // No exceptions should be present.
    }

    @Test
    public void testSignalJob() throws Exception
    {
        jobDefinitionServiceTestHelper.createJobDefinition(ACTIVITI_XML_TEST_RECEIVE_TASK_WITH_CLASSPATH);

        // Start the job.
        Job job = jobService.createAndStartJob(jobServiceTestHelper.createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME));

        // Job should be waiting at Receive task.
        Job jobGet = jobRestController.getJob(job.getId(), false);
        assertEquals(JobStatusEnum.RUNNING, jobGet.getStatus());
        assertEquals("receivetask1", jobGet.getCurrentWorkflowStep().getId());

        // Signal job to continue.
        List<Parameter> signalParameters = new ArrayList<>();
        Parameter signalPameter1 = new Parameter("UT_SIGNAL_PARAM_1", "UT_SIGNAL_VALUE_1");
        signalParameters.add(signalPameter1);

        JobSignalRequest jobSignalRequest = new JobSignalRequest(job.getId(), "receivetask1", signalParameters, null);

        Job signalJob = jobRestController.signalJob(jobSignalRequest);

        assertEquals(JobStatusEnum.RUNNING, signalJob.getStatus());
        assertEquals("receivetask1", signalJob.getCurrentWorkflowStep().getId());
        assertTrue(signalJob.getParameters().contains(signalPameter1));

        // Job should have been completed.
        jobGet = jobRestController.getJob(job.getId(), true);
        assertEquals(JobStatusEnum.COMPLETED, jobGet.getStatus());
        assertTrue(jobGet.getParameters().contains(signalPameter1));
    }

    @Test
    public void testSignalJobNoExists() throws Exception
    {
        // Signal job with job id that does not exist.
        try
        {
            jobRestController.signalJob(new JobSignalRequest("job_does_not_exist", "receivetask1", null, null));
            fail("Should throw an ObjectNotFoundException.");
        }
        catch (ObjectNotFoundException ex)
        {
            assertEquals(String.format("No job found for matching job id: \"%s\" and receive task id: \"%s\".", "job_does_not_exist", "receivetask1"),
                ex.getMessage());
        }

        // Signal job with receive task that does not exist.
        jobDefinitionServiceTestHelper.createJobDefinition(ACTIVITI_XML_TEST_RECEIVE_TASK_WITH_CLASSPATH);

        // Start the job.
        Job job = jobService.createAndStartJob(jobServiceTestHelper.createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME));

        try
        {
            // Job should be waiting at Receive task.
            Job jobGet = jobRestController.getJob(job.getId(), false);
            assertEquals(JobStatusEnum.RUNNING, jobGet.getStatus());
            assertEquals("receivetask1", jobGet.getCurrentWorkflowStep().getId());

            jobRestController.signalJob(new JobSignalRequest(job.getId(), "receivetask_does_not_exist", null, null));
            fail("Should throw an ObjectNotFoundException.");
        }
        catch (ObjectNotFoundException ex)
        {
            assertEquals(String.format("No job found for matching job id: \"%s\" and receive task id: \"%s\".", job.getId(), "receivetask_does_not_exist"),
                ex.getMessage());
        }
    }

    @Test
    public void testSignalJobNoParameters() throws Exception
    {
        jobDefinitionServiceTestHelper.createJobDefinition(ACTIVITI_XML_TEST_RECEIVE_TASK_WITH_CLASSPATH);

        // Start the job.
        Job job = jobService.createAndStartJob(jobServiceTestHelper.createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME));

        // Job should be waiting at Receive task.
        Job jobGet = jobRestController.getJob(job.getId(), false);
        assertEquals(JobStatusEnum.RUNNING, jobGet.getStatus());
        assertEquals("receivetask1", jobGet.getCurrentWorkflowStep().getId());

        // Signal job to continue.
        JobSignalRequest jobSignalRequest = new JobSignalRequest(job.getId(), "receivetask1", null, null);

        Job signalJob = jobRestController.signalJob(jobSignalRequest);

        assertEquals(JobStatusEnum.RUNNING, signalJob.getStatus());
        assertEquals("receivetask1", signalJob.getCurrentWorkflowStep().getId());

        // Job should have been completed.
        jobGet = jobRestController.getJob(job.getId(), true);
        assertEquals(JobStatusEnum.COMPLETED, jobGet.getStatus());
    }

    @Test
    public void testUpdateJob() throws Exception
    {
        // Create a test job definition.
        jobDefinitionServiceTestHelper.createJobDefinition(ACTIVITI_XML_TEST_USER_TASK_WITH_CLASSPATH);

        // Create and start the job.
        Job job = jobService.createAndStartJob(jobServiceTestHelper.createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME));

        // Job should be waiting at User task.

        // Get the running job with non verbose.
        Job jobGet = jobRestController.getJob(job.getId(), false);
        assertEquals(JobStatusEnum.RUNNING, jobGet.getStatus());
        assertNull(jobGet.getActivitiJobXml());
        assertTrue(CollectionUtils.isEmpty(jobGet.getCompletedWorkflowSteps()));
        assertEquals("usertask1", jobGet.getCurrentWorkflowStep().getId());

        // Suspend the job.
        jobRestController.updateJob(job.getId(), new JobUpdateRequest(JobActionEnum.SUSPEND));

        // Validate that the job is suspended.
        jobGet = jobRestController.getJob(job.getId(), false);
        assertEquals(JobStatusEnum.SUSPENDED, jobGet.getStatus());
        assertNull(jobGet.getActivitiJobXml());
        assertTrue(CollectionUtils.isEmpty(jobGet.getCompletedWorkflowSteps()));
        assertEquals("usertask1", jobGet.getCurrentWorkflowStep().getId());

        // Resume the job.
        jobRestController.updateJob(job.getId(), new JobUpdateRequest(JobActionEnum.RESUME));

        // Validate that the job is running.
        jobGet = jobRestController.getJob(job.getId(), false);
        assertEquals(JobStatusEnum.RUNNING, jobGet.getStatus());
        assertNull(jobGet.getActivitiJobXml());
        assertTrue(CollectionUtils.isEmpty(jobGet.getCompletedWorkflowSteps()));
        assertEquals("usertask1", jobGet.getCurrentWorkflowStep().getId());

        // Query the pending task and complete it
        List<Task> tasks = activitiTaskService.createTaskQuery().processInstanceId(job.getId()).list();
        activitiTaskService.complete(tasks.get(0).getId());

        // Job should have been completed.

        // Get the completed job with non verbose.
        jobGet = jobRestController.getJob(job.getId(), false);
        assertEquals(JobStatusEnum.COMPLETED, jobGet.getStatus());
        assertNotNull(jobGet.getStartTime());
        assertNotNull(jobGet.getEndTime());
        assertNull(jobGet.getActivitiJobXml());
        assertTrue(CollectionUtils.isEmpty(jobGet.getCompletedWorkflowSteps()));
        assertNull(jobGet.getCurrentWorkflowStep());
    }
}
