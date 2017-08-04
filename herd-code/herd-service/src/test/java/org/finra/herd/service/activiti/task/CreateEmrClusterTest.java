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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.finra.herd.model.api.xml.EmrClusterDefinition;
import org.finra.herd.model.api.xml.Job;
import org.finra.herd.model.api.xml.Parameter;
import org.finra.herd.model.dto.AwsParamsDto;
import org.finra.herd.service.AbstractServiceTest;
import org.finra.herd.service.EmrServiceTest;
import org.finra.herd.service.activiti.ActivitiRuntimeHelper;

/**
 * Tests the CreateEmrCluster class. Most of the functionality is already tested in the {@link EmrServiceTest}. This test suite will test whether the activiti
 * task accepts the variables correctly, and sets return variables correctly.
 */
public class CreateEmrClusterTest extends AbstractServiceTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateEmrClusterTest.class);

    @Before
    public void createDatabaseEntities()
    {
        // Create EC2 on-demand pricing entities required for testing.
        ec2OnDemandPricingDaoTestHelper.createEc2OnDemandPricingEntities();
    }

    private Map<String, Object> createJob(String clusterName, String dryRun, String contentType, String emrClusterDefinitionOverride) throws Exception
    {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter("clusterName", clusterName));
        parameters.add(new Parameter("dryRun", dryRun));
        parameters.add(new Parameter("contentType", contentType));
        parameters.add(new Parameter("emrClusterDefinitionOverride", emrClusterDefinitionOverride));

        // Run a job with Activiti XML that will start cluster.
        Job job = jobServiceTestHelper.createJobForCreateCluster(ACTIVITI_XML_CREATE_CLUSTER_WITH_CLASSPATH, parameters);
        assertNotNull(job);

        HistoricProcessInstance hisInstance =
            activitiHistoryService.createHistoricProcessInstanceQuery().processInstanceId(job.getId()).includeProcessVariables().singleResult();
        return hisInstance.getProcessVariables();
    }

    private void terminateCluster(String namespace, String clusterDefinitionName, String clusterName)
    {
        try
        {
            String fullClusterName = emrHelper.buildEmrClusterName(namespace, clusterDefinitionName, clusterName);

            AwsParamsDto awsParams = emrHelper.getAwsParamsDto();
            emrDao.terminateEmrCluster(fullClusterName, true, awsParams);
        }
        catch (Exception e)
        {
            /*
             * Ignore the error.
             * Most of the cases the failures are because the cluster terminated by itself, in which case what this method was trying to achieve has been
             * accomplished.
             * If cluster termination legitimately fails, it is not part of this test suite.
             */
            LOGGER.warn(String
                .format("Failed to terminate cluster namespace = %s, clusterDefinitionName = %s, clusterName = %s", namespace, clusterDefinitionName,
                    clusterName));
        }
    }

    /**
     * This method tests the happy path scenario to create cluster.
     */
    @Test
    public void testCreateCluster() throws Exception
    {
        String clusterName = "testCluster" + Math.random();

        Map<String, Object> variables = createJob(clusterName, null, null, null);

        assertEquals("taskStatus", "SUCCESS", variables.get("createClusterServiceTask_taskStatus"));
        assertNotNull("emrClusterId", variables.get("createClusterServiceTask_emrClusterId"));
        assertNotNull("emrClusterStatus", variables.get("createClusterServiceTask_emrClusterStatus"));
        assertNotNull("emrClusterCreated", variables.get("createClusterServiceTask_emrClusterCreated"));
        assertNotNull("emrClusterDefinition", variables.get("createClusterServiceTask_emrClusterDefinition"));

        terminateCluster(TEST_ACTIVITI_NAMESPACE_CD, EMR_CLUSTER_DEFINITION_NAME, clusterName);
    }

    @Test
    public void testCreateClusterDryRunTrue() throws Exception
    {
        String clusterName = "testCluster" + Math.random();

        Map<String, Object> variables = createJob(clusterName, "true", null, null);

        assertEquals("taskStatus", "SUCCESS", variables.get("createClusterServiceTask_taskStatus"));
        assertNull("emrClusterId", variables.get("createClusterServiceTask_emrClusterId"));
        assertNull("emrClusterStatus", variables.get("createClusterServiceTask_emrClusterStatus"));
        assertNotNull("emrClusterCreated", variables.get("createClusterServiceTask_emrClusterCreated"));
        assertNotNull("emrClusterDefinition", variables.get("createClusterServiceTask_emrClusterDefinition"));

        terminateCluster(TEST_ACTIVITI_NAMESPACE_CD, EMR_CLUSTER_DEFINITION_NAME, clusterName);
    }

    @Test
    public void testCreateClusterDryRunFalse() throws Exception
    {
        String clusterName = "testCluster" + Math.random();

        Map<String, Object> variables = createJob(clusterName, "false", null, null);

        assertEquals("taskStatus", "SUCCESS", variables.get("createClusterServiceTask_taskStatus"));
        assertNotNull("emrClusterId", variables.get("createClusterServiceTask_emrClusterId"));
        assertNotNull("emrClusterStatus", variables.get("createClusterServiceTask_emrClusterStatus"));
        assertNotNull("emrClusterCreated", variables.get("createClusterServiceTask_emrClusterCreated"));
        assertNotNull("emrClusterDefinition", variables.get("createClusterServiceTask_emrClusterDefinition"));

        terminateCluster(TEST_ACTIVITI_NAMESPACE_CD, EMR_CLUSTER_DEFINITION_NAME, clusterName);
    }

    @Test
    public void testCreateClusterOverrideJson() throws Exception
    {
        String clusterName = "testCluster" + Math.random();

        String override = jsonHelper.objectToJson(new EmrClusterDefinition());
        Map<String, Object> variables = createJob(clusterName, "false", "json", override);

        assertEquals("taskStatus", "SUCCESS", variables.get("createClusterServiceTask_taskStatus"));
        assertNotNull("emrClusterId", variables.get("createClusterServiceTask_emrClusterId"));
        assertNotNull("emrClusterStatus", variables.get("createClusterServiceTask_emrClusterStatus"));
        assertNotNull("emrClusterCreated", variables.get("createClusterServiceTask_emrClusterCreated"));
        assertNotNull("emrClusterDefinition", variables.get("createClusterServiceTask_emrClusterDefinition"));

        terminateCluster(TEST_ACTIVITI_NAMESPACE_CD, EMR_CLUSTER_DEFINITION_NAME, clusterName);
    }

    @Test
    public void testCreateClusterOverrideXml() throws Exception
    {
        String clusterName = "testCluster" + Math.random();

        String override = xmlHelper.objectToXml(new EmrClusterDefinition());
        Map<String, Object> variables = createJob(clusterName, "false", "xml", override);

        assertEquals("taskStatus", "SUCCESS", variables.get("createClusterServiceTask_taskStatus"));
        assertNotNull("emrClusterId", variables.get("createClusterServiceTask_emrClusterId"));
        assertNotNull("emrClusterStatus", variables.get("createClusterServiceTask_emrClusterStatus"));
        assertNotNull("emrClusterCreated", variables.get("createClusterServiceTask_emrClusterCreated"));
        assertNotNull("emrClusterDefinition", variables.get("createClusterServiceTask_emrClusterDefinition"));

        terminateCluster(TEST_ACTIVITI_NAMESPACE_CD, EMR_CLUSTER_DEFINITION_NAME, clusterName);
    }

    @Test
    public void testCreateClusterContentTypeNullOverrideNotNull() throws Exception
    {
        String clusterName = "testCluster" + Math.random();

        String override = jsonHelper.objectToJson(new EmrClusterDefinition());

        executeWithoutLogging(ActivitiRuntimeHelper.class, () -> {
            Map<String, Object> variables = createJob(clusterName, "false", null, override);
            assertEquals("taskStatus", "ERROR", variables.get("createClusterServiceTask_taskStatus"));
        });
    }

    @Test
    public void testCreateClusterContentTypeNotNullOverrideNull() throws Exception
    {
        String clusterName = "testCluster" + Math.random();

        executeWithoutLogging(ActivitiRuntimeHelper.class, () -> {
            Map<String, Object> variables = createJob(clusterName, "false", "xml", null);
            assertEquals("taskStatus", "ERROR", variables.get("createClusterServiceTask_taskStatus"));
        });
    }

    @Test
    public void testCreateClusterContentTypeInvalid() throws Exception
    {
        String clusterName = "testCluster" + Math.random();

        String override = jsonHelper.objectToJson(new EmrClusterDefinition());

        executeWithoutLogging(ActivitiRuntimeHelper.class, () -> {
            Map<String, Object> variables = createJob(clusterName, "false", "invalid", override);
            assertEquals("taskStatus", "ERROR", variables.get("createClusterServiceTask_taskStatus"));
        });
    }

    /**
     * This method tests the scenario where any other exception other than BpmnError is not re-thrown.
     */
    @Test
    public void testDelegateCallExceptionNotThrown() throws Exception
    {
        List<Parameter> parameters = new ArrayList<>();

        executeWithoutLogging(ActivitiRuntimeHelper.class, () -> {
            // Run a job with Activiti XML that will start cluster.
            // clusterName is not set as parameter, hence error will occur but will not be re-thrown
            jobServiceTestHelper.createJobForCreateCluster(ACTIVITI_XML_CREATE_CLUSTER_WITH_CLASSPATH, parameters);
        });
    }
}
