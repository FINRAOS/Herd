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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.CollectionUtils;

import org.finra.dm.dao.AbstractDaoTest;
import org.finra.dm.dao.helper.AwsHelper;
import org.finra.dm.dao.helper.DmStringHelper;
import org.finra.dm.dao.helper.EmrHelper;
import org.finra.dm.dao.helper.JsonHelper;
import org.finra.dm.dao.helper.XmlHelper;
import org.finra.dm.dao.impl.MockJdbcOperations;
import org.finra.dm.dao.impl.MockStsOperationsImpl;
import org.finra.dm.model.api.xml.Attribute;
import org.finra.dm.model.api.xml.AttributeDefinition;
import org.finra.dm.model.api.xml.BusinessObjectData;
import org.finra.dm.model.api.xml.BusinessObjectDataAttribute;
import org.finra.dm.model.api.xml.BusinessObjectDataAttributeCreateRequest;
import org.finra.dm.model.api.xml.BusinessObjectDataAttributeKey;
import org.finra.dm.model.api.xml.BusinessObjectDataAttributeUpdateRequest;
import org.finra.dm.model.api.xml.BusinessObjectDataAvailability;
import org.finra.dm.model.api.xml.BusinessObjectDataAvailabilityCollectionRequest;
import org.finra.dm.model.api.xml.BusinessObjectDataAvailabilityCollectionResponse;
import org.finra.dm.model.api.xml.BusinessObjectDataAvailabilityRequest;
import org.finra.dm.model.api.xml.BusinessObjectDataCreateRequest;
import org.finra.dm.model.api.xml.BusinessObjectDataDdl;
import org.finra.dm.model.api.xml.BusinessObjectDataDdlCollectionRequest;
import org.finra.dm.model.api.xml.BusinessObjectDataDdlCollectionResponse;
import org.finra.dm.model.api.xml.BusinessObjectDataDdlOutputFormatEnum;
import org.finra.dm.model.api.xml.BusinessObjectDataDdlRequest;
import org.finra.dm.model.api.xml.BusinessObjectDataInvalidateUnregisteredRequest;
import org.finra.dm.model.api.xml.BusinessObjectDataKey;
import org.finra.dm.model.api.xml.BusinessObjectDataNotificationFilter;
import org.finra.dm.model.api.xml.BusinessObjectDataNotificationRegistration;
import org.finra.dm.model.api.xml.BusinessObjectDataNotificationRegistrationCreateRequest;
import org.finra.dm.model.api.xml.BusinessObjectDataNotificationRegistrationKey;
import org.finra.dm.model.api.xml.BusinessObjectDataStatus;
import org.finra.dm.model.api.xml.BusinessObjectDataStatusInformation;
import org.finra.dm.model.api.xml.BusinessObjectDataStatusUpdateRequest;
import org.finra.dm.model.api.xml.BusinessObjectDataStatusUpdateResponse;
import org.finra.dm.model.api.xml.BusinessObjectDataStorageFilesCreateRequest;
import org.finra.dm.model.api.xml.BusinessObjectDataStorageFilesCreateResponse;
import org.finra.dm.model.api.xml.BusinessObjectDefinition;
import org.finra.dm.model.api.xml.BusinessObjectDefinitionCreateRequest;
import org.finra.dm.model.api.xml.BusinessObjectDefinitionKey;
import org.finra.dm.model.api.xml.BusinessObjectDefinitionUpdateRequest;
import org.finra.dm.model.api.xml.BusinessObjectFormat;
import org.finra.dm.model.api.xml.BusinessObjectFormatCreateRequest;
import org.finra.dm.model.api.xml.BusinessObjectFormatDdl;
import org.finra.dm.model.api.xml.BusinessObjectFormatDdlCollectionRequest;
import org.finra.dm.model.api.xml.BusinessObjectFormatDdlCollectionResponse;
import org.finra.dm.model.api.xml.BusinessObjectFormatDdlRequest;
import org.finra.dm.model.api.xml.BusinessObjectFormatKey;
import org.finra.dm.model.api.xml.BusinessObjectFormatUpdateRequest;
import org.finra.dm.model.api.xml.CustomDdl;
import org.finra.dm.model.api.xml.CustomDdlCreateRequest;
import org.finra.dm.model.api.xml.CustomDdlKey;
import org.finra.dm.model.api.xml.CustomDdlUpdateRequest;
import org.finra.dm.model.api.xml.DownloadSingleInitiationResponse;
import org.finra.dm.model.api.xml.EmrClusterDefinition;
import org.finra.dm.model.api.xml.ExpectedPartitionValueInformation;
import org.finra.dm.model.api.xml.ExpectedPartitionValuesCreateRequest;
import org.finra.dm.model.api.xml.ExpectedPartitionValuesDeleteRequest;
import org.finra.dm.model.api.xml.ExpectedPartitionValuesInformation;
import org.finra.dm.model.api.xml.File;
import org.finra.dm.model.api.xml.JdbcConnection;
import org.finra.dm.model.api.xml.JdbcDatabaseType;
import org.finra.dm.model.api.xml.JdbcExecutionRequest;
import org.finra.dm.model.api.xml.JdbcStatement;
import org.finra.dm.model.api.xml.JdbcStatementType;
import org.finra.dm.model.api.xml.Job;
import org.finra.dm.model.api.xml.JobAction;
import org.finra.dm.model.api.xml.JobCreateRequest;
import org.finra.dm.model.api.xml.JobDefinition;
import org.finra.dm.model.api.xml.JobDefinitionCreateRequest;
import org.finra.dm.model.api.xml.LatestAfterPartitionValue;
import org.finra.dm.model.api.xml.LatestBeforePartitionValue;
import org.finra.dm.model.api.xml.Namespace;
import org.finra.dm.model.api.xml.NamespaceCreateRequest;
import org.finra.dm.model.api.xml.Parameter;
import org.finra.dm.model.api.xml.PartitionKeyGroup;
import org.finra.dm.model.api.xml.PartitionKeyGroupCreateRequest;
import org.finra.dm.model.api.xml.PartitionKeyGroupKey;
import org.finra.dm.model.api.xml.PartitionValueFilter;
import org.finra.dm.model.api.xml.PartitionValueRange;
import org.finra.dm.model.api.xml.Schema;
import org.finra.dm.model.api.xml.SchemaColumn;
import org.finra.dm.model.api.xml.StorageDirectory;
import org.finra.dm.model.api.xml.StorageFile;
import org.finra.dm.model.api.xml.StorageUnit;
import org.finra.dm.model.api.xml.StorageUnitCreateRequest;
import org.finra.dm.model.api.xml.SystemJobRunRequest;
import org.finra.dm.model.api.xml.SystemJobRunResponse;
import org.finra.dm.model.api.xml.UploadSingleInitiationRequest;
import org.finra.dm.model.api.xml.UploadSingleInitiationResponse;
import org.finra.dm.model.dto.ConfigurationValue;
import org.finra.dm.model.dto.S3FileTransferRequestParamsDto;
import org.finra.dm.model.dto.S3FileTransferResultsDto;
import org.finra.dm.model.jpa.BusinessObjectDataEntity;
import org.finra.dm.model.jpa.BusinessObjectDataStatusEntity;
import org.finra.dm.model.jpa.BusinessObjectDefinitionEntity;
import org.finra.dm.model.jpa.BusinessObjectFormatEntity;
import org.finra.dm.model.jpa.DataProviderEntity;
import org.finra.dm.model.jpa.EmrClusterDefinitionEntity;
import org.finra.dm.model.jpa.FileTypeEntity;
import org.finra.dm.model.jpa.NamespaceEntity;
import org.finra.dm.model.jpa.SchemaColumnEntity;
import org.finra.dm.model.jpa.StorageEntity;
import org.finra.dm.model.jpa.StoragePlatformEntity;
import org.finra.dm.model.jpa.StorageUnitEntity;
import org.finra.dm.service.activiti.ActivitiHelper;
import org.finra.dm.service.activiti.CreateAndStartProcessInstanceCmd;
import org.finra.dm.service.activiti.task.BaseJavaDelegate;
import org.finra.dm.service.config.ServiceTestSpringModuleConfig;
import org.finra.dm.service.helper.BusinessObjectDataHelper;
import org.finra.dm.service.helper.BusinessObjectDataInvalidateUnregisteredHelper;
import org.finra.dm.service.helper.DmDaoHelper;
import org.finra.dm.service.helper.DmHelper;
import org.finra.dm.service.helper.EmrStepHelperFactory;
import org.finra.dm.service.helper.Hive13DdlGenerator;
import org.finra.dm.service.helper.S3PropertiesLocationHelper;
import org.finra.dm.service.helper.SqsMessageBuilder;
import org.finra.dm.service.helper.VelocityHelper;
import org.finra.dm.service.impl.BusinessObjectDataServiceImpl;
import org.finra.dm.service.impl.UploadDownloadHelperServiceImpl;

/**
 * This is an abstract base class that provides useful methods for service test drivers.
 */
@ContextConfiguration(classes = ServiceTestSpringModuleConfig.class, inheritLocations = false)
public abstract class AbstractServiceTest extends AbstractDaoTest
{
    private static Logger logger = Logger.getLogger(AbstractServiceTest.class);

    // Activiti workflow resources with and without classpath prefix.
    protected static final String ACTIVITI_XML_DM_WORKFLOW = "org/finra/dm/service/testDMWorkflow.bpmn20.xml";
    protected static final String ACTIVITI_XML_DM_WORKFLOW_WITH_CLASSPATH = "classpath:" + ACTIVITI_XML_DM_WORKFLOW;
    protected static final String ACTIVITI_XML_DM_TIMER_WITH_CLASSPATH = "classpath:org/finra/dm/service/testDMTimerWorkflow.bpmn20.xml";
    protected static final String ACTIVITI_XML_DM_INTERMEDIATE_TIMER_WITH_CLASSPATH =
        "classpath:org/finra/dm/service/testDMIntermediateTimerWorkflow.bpmn20.xml";
    protected static final String ACTIVITI_XML_LOG_VARIABLES_WITH_CLASSPATH = "classpath:org/finra/dm/service/activitiWorkflowLogVariables.bpmn20.xml";
    protected static final String ACTIVITI_XML_LOG_VARIABLES_NO_REGEX_WITH_CLASSPATH =
        "classpath:org/finra/dm/service/activitiWorkflowLogVariablesNoRegex.bpmn20.xml";
    protected static final String ACTIVITI_XML_CREATE_CLUSTER_WITH_CLASSPATH = "classpath:org/finra/dm/service/activitiWorkflowCreateEmrCluster.bpmn20.xml";
    protected static final String ACTIVITI_XML_CHECK_CLUSTER_WITH_CLASSPATH = "classpath:org/finra/dm/service/activitiWorkflowCheckEmrCluster.bpmn20.xml";
    protected static final String ACTIVITI_XML_TERMINATE_CLUSTER_WITH_CLASSPATH =
        "classpath:org/finra/dm/service/activitiWorkflowTerminateEmrCluster.bpmn20.xml";
    protected static final String ACTIVITI_XML_ADD_EMR_MASTER_SECURITY_GROUPS_WITH_CLASSPATH =
        "classpath:org/finra/dm/service/activitiWorkflowAddEmrMasterSecurityGroup.bpmn20.xml";
    protected static final String ACTIVITI_XML_RUN_OOZIE_WORKFLOW_WITH_CLASSPATH = "classpath:org/finra/dm/service/activitiWorkflowRunOozieJob.bpmn20.xml";
    protected static final String ACTIVITI_XML_CHECK_OOZIE_WORKFLOW_WITH_CLASSPATH = "classpath:org/finra/dm/service/activitiWorkflowCheckOozieJob.bpmn20.xml";
    protected static final String ACTIVITI_XML_ADD_EMR_STEPS_WITH_CLASSPATH = "classpath:org/finra/dm/service/activitiWorkflowAddEmrStep.bpmn20.xml";
    protected static final String ACTIVITI_XML_TEST_SERVICE_TASK_WITH_CLASSPATH = "classpath:org/finra/dm/service/testActivitiWorkflowServiceTask.bpmn20.xml";
    protected static final String ACTIVITI_XML_TEST_USER_TASK_WITH_CLASSPATH = "classpath:org/finra/dm/service/testDMUserTaskWorkflow.bpmn20.xml";
    protected static final String ACTIVITI_XML_TEST_RECEIVE_TASK_WITH_CLASSPATH = "classpath:org/finra/dm/service/testDMReceiveTaskWorkflow.bpmn20.xml";

    protected static final StorageDirectory NO_STORAGE_DIRECTORY = null;

    protected static final List<StorageFile> NO_STORAGE_FILES = null;

    protected static final Boolean DISCOVER_STORAGE_FILES = true;

    protected static final List<BusinessObjectDataKey> NO_BUSINESS_OBJECT_DATA_PARENTS = new ArrayList<>();
    protected static final List<BusinessObjectDataKey> NO_BUSINESS_OBJECT_DATA_CHILDREN = new ArrayList<>();

    protected static final Boolean CREATE_NEW_VERSION = true;
    protected static final Boolean NO_CREATE_NEW_VERSION = false;

    protected static final String FIRST_PARTITION_COLUMN_NAME = "PRTN_CLMN001";
    protected static final String SECOND_PARTITION_COLUMN_NAME = "PRTN_CLMN002";

    protected static final String FILE_NAME = "TestFileName" + RANDOM_SUFFIX;

    protected static final String[][] PARTITION_COLUMNS =
        new String[][] {{"DATE", null}, {"STRING", null}, {"INT", null}, {"NUMBER", null}, {"BOOLEAN", null}, {"NUMBER", null}, {"NUMBER", null}};

    protected static final String[][] SCHEMA_COLUMNS =
        new String[][] {{"TINYINT", null}, {"SMALLINT", null}, {"INT", null}, {"BIGINT", null}, {"FLOAT", null}, {"DOUBLE", null}, {"DECIMAL", null},
            {"DECIMAL", "p,s"}, {"NUMBER", null}, {"NUMBER", "p"}, {"NUMBER", "p,s"}, {"TIMESTAMP", null}, {"DATE", null}, {"STRING", null}, {"VARCHAR", "n"},
            {"VARCHAR2", "n"}, {"CHAR", "n"}, {"BOOLEAN", null}, {"BINARY", null}};

    protected static final String COLUMN_NAME = "UT_Column_Name" + RANDOM_SUFFIX;
    protected static final String COLUMN_NAME_2 = "UT_Column_Name_2" + RANDOM_SUFFIX;

    protected static final String COLUMN_SIZE = RANDOM_SUFFIX;
    protected static final String NO_COLUMN_SIZE = null;

    protected static final Boolean COLUMN_REQUIRED = true;
    protected static final Boolean NO_COLUMN_REQUIRED = false;

    protected static final String COLUMN_DEFAULT_VALUE = "UT_Column_Default_Value" + RANDOM_SUFFIX;
    protected static final String NO_COLUMN_DEFAULT_VALUE = null;

    protected static final String COLUMN_DESCRIPTION = "UT_Column_Description" + RANDOM_SUFFIX;
    protected static final String NO_COLUMN_DESCRIPTION = null;

    protected static final List<BusinessObjectDataStatus> NO_BUSINESS_OBJECT_DATA_STATUSES = new ArrayList<>();

    protected static final List<PartitionValueFilter> NO_PARTITION_VALUE_FILTERS = new ArrayList<>();
    protected static final PartitionValueFilter NO_STANDALONE_PARTITION_VALUE_FILTER = null;

    protected static final List<String> NO_PARTITION_VALUES = null;
    protected static final PartitionValueRange NO_PARTITION_VALUE_RANGE = null;
    protected static final LatestBeforePartitionValue NO_LATEST_BEFORE_PARTITION_VALUE = null;
    protected static final LatestAfterPartitionValue NO_LATEST_AFTER_PARTITION_VALUE = null;

    protected static final Boolean INCLUDE_DROP_TABLE_STATEMENT = true;
    protected static final Boolean NO_INCLUDE_DROP_TABLE_STATEMENT = false;

    protected static final Boolean INCLUDE_IF_NOT_EXISTS_OPTION = true;
    protected static final Boolean NO_INCLUDE_IF_NOT_EXISTS_OPTION = false;

    protected static final Boolean INCLUDE_DROP_PARTITIONS = true;
    protected static final Boolean NO_INCLUDE_DROP_PARTITIONS = false;

    protected static final Boolean ALLOW_MISSING_DATA = true;
    protected static final Boolean NO_ALLOW_MISSING_DATA = false;

    /**
     * The test namespace code as per the above workflow XML file.
     */
    protected static final String TEST_ACTIVITI_NAMESPACE_CD = "testNamespace";

    /**
     * The test job name as per the above workflow XML file.
     */
    protected static final String TEST_ACTIVITI_JOB_NAME = "testDMWorkflow";

    /**
     * This is the test Activiti workflow Id which is the test app name + "." + the test activity job name.
     */
    protected static final String TEST_ACTIVITY_WORKFLOW_ID = TEST_ACTIVITI_NAMESPACE_CD + "." + TEST_ACTIVITI_JOB_NAME;

    protected static final List<String> PROCESS_DATE_PARTITION_VALUES = Arrays.asList("2014-04-02", "2014-04-03", "2014-04-04", "2014-04-07", "2014-04-08");

    protected final String START_PARTITION_VALUE = PROCESS_DATE_PARTITION_VALUES.get(0);
    protected final String END_PARTITION_VALUE = PROCESS_DATE_PARTITION_VALUES.get(PROCESS_DATE_PARTITION_VALUES.size() - 1);

    protected static final String ROW_FORMAT = "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' ESCAPED BY '\\\\' NULL DEFINED AS '\\N'";

    protected static final String FIRST_COLUMN_NAME = "COLUMN001";
    protected static final String FIRST_COLUMN_DATA_TYPE = "TINYINT";

    protected final String testS3KeyPrefix =
        getExpectedS3KeyPrefix(NAMESPACE_CD, DATA_PROVIDER_NAME, BOD_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, INITIAL_FORMAT_VERSION, PARTITION_KEY,
            PARTITION_VALUE, null, null, INITIAL_DATA_VERSION);

    protected static final int EXPECTED_UUID_SIZE = 36;

    protected static final List<String> PROCESS_DATE_AVAILABLE_PARTITION_VALUES = Arrays.asList("2014-04-02", "2014-04-03", "2014-04-08");
    protected static final List<String> PROCESS_DATE_NOT_AVAILABLE_PARTITION_VALUES = Arrays.asList("2014-04-04", "2014-04-07");

    protected static final Integer MAX_COLUMNS = 10;
    protected static final Integer MAX_PARTITIONS = 5;

    protected static final String SCHEMA_COLUMN_NAME_PREFIX = "Clmn-Name";
    protected static final String SCHEMA_PARTITION_COLUMN_NAME_PREFIX = "Prtn-Clmn-Name";

    protected static final String TEST_SQS_MESSAGE_CORRELATION_ID = "testCorrelationId";
    protected static final String TEST_SQS_CONTEXT_MESSAGE_TYPE_TO_PUBLISH = "testContextMessageTypeToPublish";
    protected static final String TEST_SQS_ENVIRONMENT = "testEnvironment";

    @Autowired
    protected S3Service s3Service;

    @Autowired
    protected StorageService storageService;

    @Autowired
    protected JobDefinitionService jobDefinitionService;

    @Autowired
    protected JobService jobService;

    @Autowired
    protected AwsHelper awsHelper;

    @Autowired
    protected EmrService emrService;

    @Autowired
    protected EmrHelper emrHelper;

    @Autowired
    protected DmStringHelper dmStringHelper;

    @Autowired
    protected DmHelper dmHelper;

    @Autowired
    protected XmlHelper xmlHelper;

    @Autowired
    protected JsonHelper jsonHelper;

    @Autowired
    protected DmDaoHelper dmDaoHelper;

    @Autowired
    protected NotificationEventService notificationEventService;

    @Autowired
    protected SqsNotificationEventService sqsNotificationEventService;

    @Autowired
    protected SpringProcessEngineConfiguration activitiConfiguration;

    @Autowired
    protected RepositoryService activitiRepositoryService;

    @Autowired
    protected SpringProcessEngineConfiguration activitiProcessEngineConfiguration;

    @Autowired
    protected ManagementService activitiManagementService;

    @Autowired
    protected HistoryService activitiHistoryService;

    @Autowired
    protected TaskService activitiTaskService;

    @Autowired
    protected EmrStepHelperFactory emrStepHelperFactory;

    @Autowired
    protected BusinessObjectDataHelper businessObjectDataHelper;

    @Autowired
    protected FileUploadCleanupService fileUploadCleanupService;

    @Autowired
    protected SqsMessageBuilder sqsMessageBuilder;

    @Autowired
    protected JmsPublishingService jmsPublishingService;

    @Autowired
    protected StoragePlatformService storagePlatformService;

    @Autowired
    protected NamespaceService namespaceService;

    @Autowired
    protected BusinessObjectDataService businessObjectDataService;

    @Autowired
    protected BusinessObjectDataAttributeService businessObjectDataAttributeService;

    @Autowired
    protected BusinessObjectDataNotificationRegistrationService businessObjectDataNotificationRegistrationService;

    @Autowired
    protected BusinessObjectDefinitionService businessObjectDefinitionService;

    @Autowired
    protected PartitionKeyGroupService partitionKeyGroupService;

    @Autowired
    protected ExpectedPartitionValueService expectedPartitionValueService;

    @Autowired
    protected FileTypeService fileTypeService;

    @Autowired
    protected BusinessObjectFormatService businessObjectFormatService;

    @Autowired
    protected CustomDdlService customDdlService;

    @Autowired
    protected EmrClusterDefinitionService emrClusterDefinitionService;

    @Autowired
    protected BusinessObjectDataStorageFileService businessObjectDataStorageFileService;

    @Autowired
    protected UploadDownloadService uploadDownloadService;

    @Autowired
    protected SystemJobService systemJobService;

    @Autowired
    protected JdbcService jdbcService;

    @Autowired
    protected BusinessObjectDataInvalidateUnregisteredHelper businessObjectDataInvalidateUnregisteredHelper;

    @Autowired
    protected VelocityHelper velocityHelper;

    @Autowired
    protected ActivitiHelper activitiHelper;

    @Autowired
    protected S3PropertiesLocationHelper s3PropertiesLocationHelper;

    /**
     * Turns off base Java delegate logging for test cases that purposely produce errors and don't want to see unnecessary stack traces in the output.
     */
    protected void turnOffBaseJavaDelegateLogging()
    {
        Logger.getLogger(BaseJavaDelegate.class).setLevel(Level.OFF);
        logger.info("This test driver turns off the logging from BaseJavaDelegate for expected error logging.");
    }

    /**
     * Turns off CreateAndStart Activiti command logging for test cases that purposely produce errors and don't want to see unnecessary stack traces in the
     * output.
     */
    protected void turnOffCreateAndStartProcessInstanceCmdLogging()
    {
        Logger.getLogger(CreateAndStartProcessInstanceCmd.class).setLevel(Level.OFF);
        logger.info("This test driver turns off the logging from CreateAndStartProcessInstanceCmd for expected error logging.");
    }

    /**
     * Creates a job definition based on the specified Activiti XML classpath resource location.
     *
     * @param activitiXmlClasspathResourceName the Activiti XML classpath resource location.
     *
     * @return the job definition.
     * @throws Exception if any errors were encountered.
     */
    public JobDefinition createJobDefinition(String activitiXmlClasspathResourceName) throws Exception
    {
        // Create the namespace entity.
        createNamespaceEntity(TEST_ACTIVITI_NAMESPACE_CD);

        // Create and persist a valid job definition.
        JobDefinitionCreateRequest jobDefinitionCreateRequest = createJobDefinitionCreateRequest(activitiXmlClasspathResourceName);
        JobDefinition jobDefinition = jobDefinitionService.createJobDefinition(jobDefinitionCreateRequest);

        // Validate the returned object against the input.
        assertNotNull(jobDefinition);
        assertTrue(jobDefinition.getNamespace().equals(jobDefinitionCreateRequest.getNamespace()));
        assertTrue(jobDefinition.getJobName().equals(jobDefinitionCreateRequest.getJobName()));
        assertTrue(jobDefinition.getDescription().equals(jobDefinitionCreateRequest.getDescription()));

        return jobDefinition;
    }

    /**
     * Creates a job definition based on the specified Activiti XML.
     *
     * @param activitiXml the Activiti XML.
     *
     * @return the job definition.
     * @throws Exception if any errors were encountered.
     */
    public JobDefinition createJobDefinitionForActivitiXml(String activitiXml) throws Exception
    {
        // Create the namespace entity.
        createNamespaceEntity(TEST_ACTIVITI_NAMESPACE_CD);

        // Create and persist a valid job definition.
        JobDefinitionCreateRequest jobDefinitionCreateRequest = createJobDefinitionCreateRequestFromActivitiXml(activitiXml);
        JobDefinition jobDefinition = jobDefinitionService.createJobDefinition(jobDefinitionCreateRequest);

        // Validate the returned object against the input.
        assertNotNull(jobDefinition);
        assertTrue(jobDefinition.getNamespace().equals(jobDefinitionCreateRequest.getNamespace()));
        assertTrue(jobDefinition.getJobName().equals(jobDefinitionCreateRequest.getJobName()));
        assertTrue(jobDefinition.getDescription().equals(jobDefinitionCreateRequest.getDescription()));

        return jobDefinition;
    }

    /**
     * Creates a new job definition create request based on fixed parameters.
     */
    protected JobDefinitionCreateRequest createJobDefinitionCreateRequest()
    {
        return createJobDefinitionCreateRequest(null);
    }

    /**
     * Creates a new job definition create request based on fixed parameters and a specified XML resource location.
     *
     * @param activitiXmlClasspathResourceName the classpath resource location to the Activiti XML. If null is specified, then the default
     * ACTIVITI_XML_DM_WORKFLOW_WITH_CLASSPATH will be used.
     */
    protected JobDefinitionCreateRequest createJobDefinitionCreateRequest(String activitiXmlClasspathResourceName)
    {
        // Create a test list of parameters.
        List<Parameter> parameters = new ArrayList<>();
        Parameter parameter = new Parameter(ATTRIBUTE_NAME_1_MIXED_CASE, ATTRIBUTE_VALUE_1);
        parameters.add(parameter);

        if (activitiXmlClasspathResourceName == null)
        {
            activitiXmlClasspathResourceName = ACTIVITI_XML_DM_WORKFLOW_WITH_CLASSPATH;
        }
        try
        {
            return createJobDefinitionCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME, JOB_DESCRIPTION,
                IOUtils.toString(resourceLoader.getResource(activitiXmlClasspathResourceName).getInputStream()), parameters);
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Unable to load Activiti XML from classpath resource: " + activitiXmlClasspathResourceName);
        }
    }

    /**
     * Creates a new job definition create request based on fixed parameters and a specified activiti XML.
     *
     * @param activitiXml the Activiti XML.
     */
    protected JobDefinitionCreateRequest createJobDefinitionCreateRequestFromActivitiXml(String activitiXml)
    {
        // Create a test list of parameters.
        List<Parameter> parameters = new ArrayList<>();
        Parameter parameter = new Parameter(ATTRIBUTE_NAME_1_MIXED_CASE, ATTRIBUTE_NAME_1_MIXED_CASE);
        parameters.add(parameter);

        return createJobDefinitionCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME, JOB_DESCRIPTION, activitiXml, parameters);
    }

    /**
     * Creates a new job definition create request based on user specified parameters.
     *
     * @param namespaceCd the namespace code.
     * @param jobName the job name.
     * @param jobDescription the job description.
     * @param activitiXml the Activiti XML.
     * @param parameters the parameters.
     *
     * @return the job definition create request.
     */
    protected JobDefinitionCreateRequest createJobDefinitionCreateRequest(String namespaceCd, String jobName, String jobDescription, String activitiXml,
        List<Parameter> parameters)
    {
        // Create and return a new job definition create request.
        JobDefinitionCreateRequest request = new JobDefinitionCreateRequest();
        request.setNamespace(namespaceCd);
        request.setJobName(jobName);
        request.setDescription(jobDescription);
        request.setActivitiJobXml(activitiXml);
        request.setParameters(parameters);
        return request;
    }

    /**
     * Creates job create request using a specified namespace code and job name, but test hard coded parameters will be used.
     *
     * @param namespaceCd the namespace code.
     * @param jobName the job definition name.
     *
     * @return the created job create request.
     */
    protected JobCreateRequest createJobCreateRequest(String namespaceCd, String jobName)
    {
        // Create a test list of parameters.
        List<Parameter> parameters = new ArrayList<>();
        Parameter parameter = new Parameter(ATTRIBUTE_NAME_2_MIXED_CASE, ATTRIBUTE_VALUE_2);
        parameters.add(parameter);
        parameter = new Parameter("Extra Attribute With No Value", null);
        parameters.add(parameter);

        return createJobCreateRequest(namespaceCd, jobName, parameters);
    }

    /**
     * Creates job create request using test hard coded values.
     *
     * @param namespaceCd the namespace code.
     * @param jobName the job definition name.
     * @param parameters the job parameters.
     *
     * @return the created job create request.
     */
    protected JobCreateRequest createJobCreateRequest(String namespaceCd, String jobName, List<Parameter> parameters)
    {
        // Create a job create request.
        JobCreateRequest jobCreateRequest = new JobCreateRequest();
        jobCreateRequest.setNamespace(namespaceCd);
        jobCreateRequest.setJobName(jobName);
        jobCreateRequest.setParameters(parameters);
        return jobCreateRequest;
    }

    /**
     * Creates a job based on the specified Activiti XML classpath resource location.
     *
     * @param activitiXmlClasspathResourceName the Activiti XML classpath resource location.
     *
     * @return the job.
     * @throws Exception if any errors were encountered.
     */
    public Job createJob(String activitiXmlClasspathResourceName) throws Exception
    {
        createJobDefinition(activitiXmlClasspathResourceName);
        return createAndStartJobSync(createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME));
    }

    /**
     * Creates a job based on the specified Activiti XML classpath resource location.
     *
     * @param activitiXmlClasspathResourceName the Activiti XML classpath resource location.
     * @param parameters the job parameters.
     *
     * @return the job.
     * @throws Exception if any errors were encountered.
     */
    public Job createJob(String activitiXmlClasspathResourceName, List<Parameter> parameters) throws Exception
    {
        createJobDefinition(activitiXmlClasspathResourceName);
        return createAndStartJobSync(createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME, parameters));
    }

    /**
     * Creates a job based on the specified Activiti XML.
     *
     * @param activitiXml the Activiti XML.
     * @param parameters the job parameters.
     *
     * @return the job.
     * @throws Exception if any errors were encountered.
     */
    public Job createJobFromActivitiXml(String activitiXml, List<Parameter> parameters) throws Exception
    {
        createJobDefinitionForActivitiXml(activitiXml);
        return createAndStartJobSync(createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME, parameters));
    }

    /**
     * Creates a job based on the specified Activiti XML classpath resource location and defines a EMR cluster definition.
     *
     * @param activitiXmlClasspathResourceName the Activiti XML classpath resource location.
     * @param parameters the job parameters.
     *
     * @return the job.
     * @throws Exception if any errors were encountered.
     */
    public Job createJobForCreateCluster(String activitiXmlClasspathResourceName, List<Parameter> parameters) throws Exception
    {
        return createJobForCreateCluster(activitiXmlClasspathResourceName, parameters, null);
    }

    /**
     * Creates a job based on the specified Activiti XML classpath resource location and defines a EMR cluster definition.
     *
     * @param activitiXmlClasspathResourceName the Activiti XML classpath resource location.
     * @param parameters the job parameters.
     *
     * @return the job.
     * @throws Exception if any errors were encountered.
     */
    public Job createJobForCreateCluster(String activitiXmlClasspathResourceName, List<Parameter> parameters, String amiVersion) throws Exception
    {
        createJobDefinition(activitiXmlClasspathResourceName);

        NamespaceEntity namespaceEntity = dmDao.getNamespaceByCd(TEST_ACTIVITI_NAMESPACE_CD);

        String configXml = IOUtils.toString(resourceLoader.getResource(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH).getInputStream());

        EmrClusterDefinition emrClusterDefinition = xmlHelper.unmarshallXmlToObject(EmrClusterDefinition.class, configXml);
        emrClusterDefinition.setAmiVersion(amiVersion);

        configXml = xmlHelper.objectToXml(emrClusterDefinition);

        EmrClusterDefinitionEntity emrClusterDefinitionEntity = createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME, configXml);

        Parameter parameter = new Parameter("emrClusterDefinitionName", emrClusterDefinitionEntity.getName());
        parameters.add(parameter);
        parameter = new Parameter("namespace", TEST_ACTIVITI_NAMESPACE_CD);
        parameters.add(parameter);

        return createAndStartJobSync(createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME, parameters));
    }

    /**
     * Gets a new list of attributes.
     *
     * @return the list of attributes.
     */
    protected List<Attribute> getNewAttributes()
    {
        List<Attribute> attributes = new ArrayList<>();

        Attribute attribute1 = new Attribute();
        attribute1.setName(ATTRIBUTE_NAME_1_MIXED_CASE);
        attribute1.setValue(ATTRIBUTE_VALUE_1);
        attributes.add(attribute1);

        Attribute attribute2 = new Attribute();
        attribute2.setName(ATTRIBUTE_NAME_2_MIXED_CASE);
        attribute2.setValue(ATTRIBUTE_VALUE_2);
        attributes.add(attribute2);

        Attribute attribute3 = new Attribute();
        attribute3.setName(ATTRIBUTE_NAME_3_MIXED_CASE);
        attribute3.setValue(ATTRIBUTE_VALUE_3);
        attributes.add(attribute3);

        return attributes;
    }

    /**
     * Gets a second set of test attributes.
     *
     * @return the list of attributes
     */
    protected List<Attribute> getNewAttributes2()
    {
        List<Attribute> attributes = new ArrayList<>();

        // Attribute 1 has a new value compared to the first set of test attributes.
        Attribute attribute1 = new Attribute();
        attribute1.setName(ATTRIBUTE_NAME_1_MIXED_CASE);
        attribute1.setValue(ATTRIBUTE_VALUE_1_UPDATED);
        attributes.add(attribute1);

        // Attribute 2 is missing compared to the first set of the test attributes.

        // Attribute 3 is identical to the one from the first set of the test attributes.
        Attribute attribute3 = new Attribute();
        attribute3.setName(ATTRIBUTE_NAME_3_MIXED_CASE);
        attribute3.setValue(ATTRIBUTE_VALUE_3);
        attributes.add(attribute3);

        // Attribute 4 is not present in the first set of the test attributes.
        Attribute attribute4 = new Attribute();
        attribute4.setName(ATTRIBUTE_NAME_4_MIXED_CASE);
        attribute4.setValue(ATTRIBUTE_VALUE_4);
        attributes.add(attribute4);

        return attributes;
    }

    /**
     * Creates a job based on the specified Activiti XML and defines a EMR cluster definition.
     *
     * @param activitiXml the Activiti XML.
     * @param parameters the job parameters.
     *
     * @return the job.
     * @throws Exception if any errors were encountered.
     */
    public Job createJobForCreateClusterForActivitiXml(String activitiXml, List<Parameter> parameters) throws Exception
    {
        createJobDefinitionForActivitiXml(activitiXml);

        NamespaceEntity namespaceEntity = dmDao.getNamespaceByCd(TEST_ACTIVITI_NAMESPACE_CD);
        EmrClusterDefinitionEntity emrClusterDefinitionEntity = createEmrClusterDefinitionEntity(namespaceEntity, EMR_CLUSTER_DEFINITION_NAME,
            IOUtils.toString(resourceLoader.getResource(EMR_CLUSTER_DEFINITION_XML_FILE_WITH_CLASSPATH).getInputStream()));

        Parameter parameter = new Parameter("namespace", namespaceEntity.getCode());
        parameters.add(parameter);

        parameter = new Parameter("emrClusterDefinitionName", emrClusterDefinitionEntity.getName());
        parameters.add(parameter);

        parameter = new Parameter("dryRun", null);
        parameters.add(parameter);

        parameter = new Parameter("contentType", null);
        parameters.add(parameter);

        parameter = new Parameter("emrClusterDefinitionOverride", null);
        parameters.add(parameter);

        return createAndStartJobSync(createJobCreateRequest(TEST_ACTIVITI_NAMESPACE_CD, TEST_ACTIVITI_JOB_NAME, parameters));
    }

    /**
     * Starts a previously created job synchronously.
     *
     * @param jobCreateRequest the job to create and start.
     *
     * @return the started job.
     * @throws Exception if any problems were encountered.
     */
    protected Job createAndStartJobSync(JobCreateRequest jobCreateRequest) throws Exception
    {
        // Start the job synchronously.
        return jobService.createAndStartJob(jobCreateRequest, false);
    }

    /**
     * Generates the BpmnModel for the given Activiti xml resource.
     *
     * @param activitiXmlResource the classpath location of Activiti Xml
     *
     * @return BpmnModel the constructed model
     * @throws Exception if any exception occurs in creation
     */
    protected BpmnModel getBpmnModelForXmlResource(String activitiXmlResource) throws Exception
    {
        String activitiXml = IOUtils.toString(resourceLoader.getResource(activitiXmlResource).getInputStream());

        BpmnModel bpmnModel;
        try
        {
            bpmnModel = activitiHelper.constructBpmnModelFromXmlAndValidate(activitiXml);
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException("Error processing Activiti XML: " + ex.getMessage(), ex);
        }
        return bpmnModel;
    }

    /**
     * Creates relative database entities required for the business object data availability service unit tests.
     */
    protected void createDatabaseEntitiesForBusinessObjectDataAvailabilityTesting(String partitionKeyGroupName)
    {
        createDatabaseEntitiesForBusinessObjectDataAvailabilityTesting(partitionKeyGroupName, getTestSchemaColumns(), getTestPartitionColumns(),
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, NO_SUBPARTITION_VALUES);
    }

    /**
     * Creates relative database entities required for the unit tests.
     */
    protected void createDatabaseEntitiesForBusinessObjectDataDdlTesting()
    {
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, PARTITION_KEY_GROUP,
            BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, UNSORTED_PARTITION_VALUES, SUBPARTITION_VALUES, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), getTestPartitionColumns(), false, CUSTOM_DDL_NAME, true);
    }

    /**
     * Creates relative database entities required for the unit tests.
     */
    protected void createDatabaseEntitiesForBusinessObjectDataDdlTesting(String businessObjectFormatFileType, String partitionKey, String partitionKeyGroupName,
        int partitionColumnPosition, List<String> partitionValues, List<String> subPartitionValues, String schemaDelimiterCharacter,
        String schemaEscapeCharacter, String schemaNullValue, List<SchemaColumn> schemaColumns, List<SchemaColumn> partitionColumns,
        boolean replaceUnderscoresWithHyphens, String customDdlName, boolean generateStorageFileEntities)
    {
        // Create a business object format entity if it does not exist.
        BusinessObjectFormatEntity businessObjectFormatEntity = dmDao.getBusinessObjectFormatByAltKey(
            new BusinessObjectFormatKey(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, businessObjectFormatFileType, FORMAT_VERSION));
        if (businessObjectFormatEntity == null)
        {
            businessObjectFormatEntity =
                createBusinessObjectFormatEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, businessObjectFormatFileType, FORMAT_VERSION, FORMAT_DESCRIPTION,
                    LATEST_VERSION_FLAG_SET, partitionKey, partitionKeyGroupName, NO_ATTRIBUTES, schemaDelimiterCharacter, schemaEscapeCharacter,
                    schemaNullValue, schemaColumns, partitionColumns);
        }

        if (StringUtils.isNotBlank(customDdlName))
        {
            boolean partitioned = (partitionColumns != null);
            createCustomDdlEntity(businessObjectFormatEntity, customDdlName, getTestCustomDdl(partitioned));
        }

        // Create S3 storages with the relative "bucket.name" attribute configured.
        StorageEntity storageEntity1 = dmDao.getStorageByName(STORAGE_NAME);
        if (storageEntity1 == null)
        {
            storageEntity1 =
                createStorageEntity(STORAGE_NAME, StoragePlatformEntity.S3, configurationHelper.getProperty(ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME),
                    S3_BUCKET_NAME);
        }
        StorageEntity storageEntity2 = dmDao.getStorageByName(STORAGE_NAME_2);
        if (storageEntity2 == null)
        {
            storageEntity2 =
                createStorageEntity(STORAGE_NAME_2, StoragePlatformEntity.S3, configurationHelper.getProperty(ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME),
                    S3_BUCKET_NAME_2);
        }

        // Create business object data for each partition value.
        for (String partitionValue : partitionValues)
        {
            BusinessObjectDataEntity businessObjectDataEntity;

            // Create a business object data instance for the specified partition value.
            if (partitionColumnPosition == BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION)
            {
                businessObjectDataEntity =
                    createBusinessObjectDataEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, businessObjectFormatFileType, FORMAT_VERSION, partitionValue,
                        subPartitionValues, DATA_VERSION, true, BusinessObjectDataStatusEntity.VALID);
            }
            else
            {
                List<String> testSubPartitionValues = new ArrayList<>(subPartitionValues);
                // Please note that the second partition column is located at index 0.
                testSubPartitionValues.set(partitionColumnPosition - 2, partitionValue);
                businessObjectDataEntity =
                    createBusinessObjectDataEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, businessObjectFormatFileType, FORMAT_VERSION, PARTITION_VALUE,
                        testSubPartitionValues, DATA_VERSION, true, BusinessObjectDataStatusEntity.VALID);
            }

            // Get the expected S3 key prefix.
            String s3KeyPrefix =
                businessObjectDataHelper.buildS3KeyPrefix(businessObjectFormatEntity, dmDaoHelper.getBusinessObjectDataKey(businessObjectDataEntity));

            // Check if we need to create the relative storage units.
            if (STORAGE_1_AVAILABLE_PARTITION_VALUES.contains(partitionValue) || Hive13DdlGenerator.NO_PARTITIONING_PARTITION_VALUE.equals(partitionValue))
            {
                StorageUnitEntity storageUnitEntity = createStorageUnitEntity(storageEntity1, businessObjectDataEntity);

                // If flag is set, create one storage file for each "auto-discoverable" partition.
                // Please note that is n! - thus we want to keep the number of partition levels small.
                if (generateStorageFileEntities)
                {
                    createStorageFiles(storageUnitEntity, s3KeyPrefix, partitionColumns, subPartitionValues, replaceUnderscoresWithHyphens);
                }
                // Add storage directory path value to the storage unit, since we have no storage files generated.
                else
                {
                    storageUnitEntity.setDirectoryPath(s3KeyPrefix);
                }
            }

            if (STORAGE_2_AVAILABLE_PARTITION_VALUES.contains(partitionValue))
            {
                StorageUnitEntity storageUnitEntity = createStorageUnitEntity(storageEntity2, businessObjectDataEntity);

                // If flag is set, create one storage file for each "auto-discoverable" partition.
                // Please note that is n! - thus we want to keep the number of partition levels small.
                if (generateStorageFileEntities)
                {
                    createStorageFiles(storageUnitEntity, s3KeyPrefix, partitionColumns, subPartitionValues, replaceUnderscoresWithHyphens);
                }
                // Add storage directory path value to the storage unit, since we have no storage files generated.
                else
                {
                    storageUnitEntity.setDirectoryPath(s3KeyPrefix);
                }
            }
        }
    }

    protected void createStorageFiles(StorageUnitEntity storageUnitEntity, String s3KeyPrefix, List<SchemaColumn> partitionColumns,
        List<String> subPartitionValues, boolean replaceUnderscoresWithHyphens)
    {
        int discoverableSubPartitionsCount = partitionColumns != null ? partitionColumns.size() - subPartitionValues.size() - 1 : 0;
        int storageFilesCount = (int) Math.pow(2, discoverableSubPartitionsCount);

        for (int i = 0; i < storageFilesCount; i++)
        {
            // Build a relative sub-directory path.
            StringBuilder subDirectory = new StringBuilder();
            String binaryString = StringUtils.leftPad(Integer.toBinaryString(i), discoverableSubPartitionsCount, "0");
            for (int j = 0; j < discoverableSubPartitionsCount; j++)
            {
                String subpartitionKey = partitionColumns.get(j + subPartitionValues.size() + 1).getName().toLowerCase();
                if (replaceUnderscoresWithHyphens)
                {
                    subpartitionKey = subpartitionKey.replace("_", "-");
                }
                subDirectory.append(String.format("/%s=%s", subpartitionKey, binaryString.substring(j, j + 1)));
            }
            // Create a storage file entity.
            createStorageFileEntity(storageUnitEntity, String.format("%s%s/data.dat", s3KeyPrefix, subDirectory.toString()), FILE_SIZE_1_KB, ROW_COUNT_1000);
        }
    }

    /**
     * Creates and persists database entities required for generating business object format ddl collection testing.
     */
    protected void createDatabaseEntitiesForBusinessObjectFormatDdlCollectionTesting()
    {
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(null);
    }

    /**
     * Creates a generate business object format ddl collection request using hard coded test values.
     *
     * @return the business object format ddl collection request
     */
    protected BusinessObjectFormatDdlCollectionRequest getTestBusinessObjectFormatDdlCollectionRequest()
    {
        // Create a generate business object format ddl collection request.
        BusinessObjectFormatDdlCollectionRequest businessObjectFormatDdlCollectionRequest = new BusinessObjectFormatDdlCollectionRequest();

        // Create a list of generate business object format ddl requests.
        List<BusinessObjectFormatDdlRequest> businessObjectFormatDdlRequests = new ArrayList<>();
        businessObjectFormatDdlCollectionRequest.setBusinessObjectFormatDdlRequests(businessObjectFormatDdlRequests);

        // Create a generate business object format ddl request.
        BusinessObjectFormatDdlRequest businessObjectFormatDdlRequest =
            new BusinessObjectFormatDdlRequest(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION,
                BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, INCLUDE_DROP_TABLE_STATEMENT, INCLUDE_IF_NOT_EXISTS_OPTION,
                null);

        // Add two business object ddl requests to the collection request.
        businessObjectFormatDdlRequests.add(businessObjectFormatDdlRequest);
        businessObjectFormatDdlRequests.add(businessObjectFormatDdlRequest);

        return businessObjectFormatDdlCollectionRequest;
    }

    /**
     * Creates an expected generate business object format ddl collection response using hard coded test values.
     *
     * @return the business object format ddl collection response
     */
    protected BusinessObjectFormatDdlCollectionResponse getExpectedBusinessObjectFormatDdlCollectionResponse()
    {
        // Prepare a generate business object data collection response using hard coded test values.
        BusinessObjectFormatDdlCollectionResponse businessObjectFormatDdlCollectionResponse = new BusinessObjectFormatDdlCollectionResponse();

        // Create a list of business object data ddl responses.
        List<BusinessObjectFormatDdl> businessObjectFormatDdlResponses = new ArrayList<>();
        businessObjectFormatDdlCollectionResponse.setBusinessObjectFormatDdlResponses(businessObjectFormatDdlResponses);

        // Get the actual HIVE DDL expected to be generated.
        String expectedDdl = getExpectedHiveDdl(null);

        // Create a business object data ddl response.
        BusinessObjectFormatDdl expectedBusinessObjectFormatDdl =
            new BusinessObjectFormatDdl(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION,
                BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, expectedDdl);

        // Add two business object ddl responses to the collection response.
        businessObjectFormatDdlResponses.add(expectedBusinessObjectFormatDdl);
        businessObjectFormatDdlResponses.add(expectedBusinessObjectFormatDdl);

        // Set the expected DDL collection value.
        businessObjectFormatDdlCollectionResponse.setDdlCollection(String.format("%s\n\n%s", expectedDdl, expectedDdl));

        return businessObjectFormatDdlCollectionResponse;
    }

    /**
     * Returns the Hive custom DDL.
     *
     * @param partitioned specifies whether the table the custom DDL is for is partitioned or not
     *
     * @return the custom Hive DDL
     */
    protected String getTestCustomDdl(boolean partitioned)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("CREATE EXTERNAL TABLE IF NOT EXISTS `${table.name}` (\n");
        sb.append("    `COLUMN001` TINYINT,\n");
        sb.append("    `COLUMN002` SMALLINT COMMENT 'This is \\'COLUMN002\\' column. ");
        sb.append("Here are \\'single\\' and \"double\" quotes along with a backslash \\.',\n");
        sb.append("    `COLUMN003` INT,\n");
        sb.append("    `COLUMN004` BIGINT,\n");
        sb.append("    `COLUMN005` FLOAT,\n");
        sb.append("    `COLUMN006` DOUBLE,\n");
        sb.append("    `COLUMN007` DECIMAL,\n");
        sb.append("    `COLUMN008` DECIMAL(p,s),\n");
        sb.append("    `COLUMN009` DECIMAL,\n");
        sb.append("    `COLUMN010` DECIMAL(p),\n");
        sb.append("    `COLUMN011` DECIMAL(p,s),\n");
        sb.append("    `COLUMN012` TIMESTAMP,\n");
        sb.append("    `COLUMN013` DATE,\n");
        sb.append("    `COLUMN014` STRING,\n");
        sb.append("    `COLUMN015` VARCHAR(n),\n");
        sb.append("    `COLUMN016` VARCHAR(n),\n");
        sb.append("    `COLUMN017` CHAR(n),\n");
        sb.append("    `COLUMN018` BOOLEAN,\n");
        sb.append("    `COLUMN019` BINARY)\n");

        if (partitioned)
        {
            sb.append("PARTITIONED BY (`PRTN_CLMN001` DATE, `PRTN_CLMN002` STRING, `PRTN_CLMN003` INT, `PRTN_CLMN004` DECIMAL, " +
                "`PRTN_CLMN005` BOOLEAN, `PRTN_CLMN006` DECIMAL, `PRTN_CLMN007` DECIMAL)\n");
        }

        sb.append("ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' ESCAPED BY '\\\\' NULL DEFINED AS '\\N'\n");

        if (partitioned)
        {
            sb.append("STORED AS TEXTFILE;");
        }
        else
        {
            sb.append("STORED AS TEXTFILE\n");
            sb.append("LOCATION '${non-partitioned.table.location}';");
        }

        return sb.toString();
    }

    /**
     * Creates relative database entities required for the unit tests.
     */
    protected void createDatabaseEntitiesForBusinessObjectFormatDdlTesting()
    {
        createDatabaseEntitiesForBusinessObjectFormatDdlTesting(FileTypeEntity.TXT_FILE_TYPE, FIRST_PARTITION_COLUMN_NAME, SCHEMA_DELIMITER_PIPE,
            SCHEMA_ESCAPE_CHARACTER_BACKSLASH, SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), getTestPartitionColumns(), CUSTOM_DDL_NAME);
    }

    /**
     * Creates relative database entities required for the unit tests.
     */
    protected void createDatabaseEntitiesForBusinessObjectFormatDdlTesting(String businessObjectFormatFileType, String partitionKey,
        String schemaDelimiterCharacter, String schemaEscapeCharacter, String schemaNullValue, List<SchemaColumn> schemaColumns,
        List<SchemaColumn> partitionColumns, String customDdlName)
    {
        // Create a business object format entity if it does not exist.
        BusinessObjectFormatEntity businessObjectFormatEntity = dmDao.getBusinessObjectFormatByAltKey(
            new BusinessObjectFormatKey(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, businessObjectFormatFileType, FORMAT_VERSION));
        if (businessObjectFormatEntity == null)
        {
            businessObjectFormatEntity =
                createBusinessObjectFormatEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, businessObjectFormatFileType, FORMAT_VERSION, FORMAT_DESCRIPTION,
                    LATEST_VERSION_FLAG_SET, partitionKey, NO_PARTITION_KEY_GROUP, NO_ATTRIBUTES, schemaDelimiterCharacter, schemaEscapeCharacter,
                    schemaNullValue, schemaColumns, partitionColumns);
        }

        if (StringUtils.isNotBlank(customDdlName))
        {
            boolean partitioned = (partitionColumns != null);
            createCustomDdlEntity(businessObjectFormatEntity, customDdlName, getTestCustomDdl(partitioned));
        }
    }

    /**
     * Creates and returns a business object format ddl request using passed parameters along with some hard-coded test values.
     *
     * @param customDdlName the custom DDL name
     *
     * @return the newly created business object format ddl request
     */
    protected BusinessObjectFormatDdlRequest getTestBusinessObjectFormatDdlRequest(String customDdlName)
    {
        BusinessObjectFormatDdlRequest request = new BusinessObjectFormatDdlRequest();

        request.setNamespace(NAMESPACE_CD);
        request.setBusinessObjectDefinitionName(BOD_NAME);
        request.setBusinessObjectFormatUsage(FORMAT_USAGE_CODE);
        request.setBusinessObjectFormatFileType(FileTypeEntity.TXT_FILE_TYPE);
        request.setBusinessObjectFormatVersion(FORMAT_VERSION);
        request.setOutputFormat(BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL);
        request.setTableName(TABLE_NAME);
        request.setCustomDdlName(customDdlName);
        request.setIncludeDropTableStatement(true);
        request.setIncludeIfNotExistsOption(true);

        return request;
    }

    /**
     * Creates a test "valid" business object data entry with default sub-partition values.
     *
     * @return the newly created business object data.
     */
    protected BusinessObjectDataEntity createTestValidBusinessObjectData()
    {
        return createTestValidBusinessObjectData(SUBPARTITION_VALUES);
    }

    /**
     * Creates a test "valid" business object data entry.
     *
     * @param subPartitionValues the sub-partition values.
     *
     * @return the newly created business object data.
     */
    protected BusinessObjectDataEntity createTestValidBusinessObjectData(List<String> subPartitionValues)
    {
        // Create a persisted business object data entity.
        return createBusinessObjectDataEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE,
            subPartitionValues, DATA_VERSION, true, BusinessObjectDataStatusEntity.VALID);
    }

    /**
     * Gets a new business object data create request with attributes and attribute definitions.
     *
     * @return the business object create request.
     */
    protected BusinessObjectDataCreateRequest getNewBusinessObjectDataCreateRequest()
    {
        return getNewBusinessObjectDataCreateRequest(true);
    }

    /**
     * Gets a newly created business object data create request.
     *
     * @param includeAttributes If true, attribute definitions and attributes will be included. Otherwise, not.
     *
     * @return the business object create request.
     */
    protected BusinessObjectDataCreateRequest getNewBusinessObjectDataCreateRequest(boolean includeAttributes)
    {
        // Crete a test business object format (and associated data).
        BusinessObjectFormatEntity businessObjectFormatEntity = createBusinessObjectFormatEntity(includeAttributes);

        StorageEntity storageEntity = createStorageEntity();

        // Create a request to create business object data.
        BusinessObjectDataCreateRequest businessObjectDataCreateRequest = new BusinessObjectDataCreateRequest();
        businessObjectDataCreateRequest.setNamespace(businessObjectFormatEntity.getBusinessObjectDefinition().getNamespace().getCode());
        businessObjectDataCreateRequest.setBusinessObjectDefinitionName(businessObjectFormatEntity.getBusinessObjectDefinition().getName());
        businessObjectDataCreateRequest.setBusinessObjectFormatUsage(businessObjectFormatEntity.getUsage());
        businessObjectDataCreateRequest.setBusinessObjectFormatFileType(businessObjectFormatEntity.getFileType().getCode());
        businessObjectDataCreateRequest.setBusinessObjectFormatVersion(businessObjectFormatEntity.getBusinessObjectFormatVersion());
        businessObjectDataCreateRequest.setPartitionKey(businessObjectFormatEntity.getPartitionKey());
        businessObjectDataCreateRequest.setPartitionValue(PARTITION_VALUE);
        businessObjectDataCreateRequest.setSubPartitionValues(SUBPARTITION_VALUES);

        List<StorageUnitCreateRequest> storageUnits = new ArrayList<>();
        businessObjectDataCreateRequest.setStorageUnits(storageUnits);

        StorageUnitCreateRequest storageUnit = new StorageUnitCreateRequest();
        storageUnits.add(storageUnit);
        storageUnit.setStorageName(storageEntity.getName());

        StorageDirectory storageDirectory = new StorageDirectory();
        storageUnit.setStorageDirectory(storageDirectory);
        storageDirectory.setDirectoryPath("Folder");

        List<StorageFile> storageFiles = new ArrayList<>();
        storageUnit.setStorageFiles(storageFiles);

        StorageFile storageFile1 = new StorageFile();
        storageFiles.add(storageFile1);
        storageFile1.setFilePath("Folder/file1.gz");
        storageFile1.setFileSizeBytes(0L);
        storageFile1.setRowCount(0L);

        StorageFile storageFile2 = new StorageFile();
        storageFiles.add(storageFile2);
        storageFile2.setFilePath("Folder/file2.gz");
        storageFile2.setFileSizeBytes(2999L);
        storageFile2.setRowCount(1000L);

        StorageFile storageFile3 = new StorageFile();
        storageFiles.add(storageFile3);
        storageFile3.setFilePath("Folder/file3.gz");
        storageFile3.setFileSizeBytes(Long.MAX_VALUE);
        storageFile3.setRowCount(Long.MAX_VALUE);

        if (includeAttributes)
        {
            businessObjectDataCreateRequest.setAttributes(getNewAttributes());
        }

        List<BusinessObjectDataKey> businessObjectDataParents = new ArrayList<>();
        businessObjectDataCreateRequest.setBusinessObjectDataParents(businessObjectDataParents);

        // Create 2 parents.
        for (int i = 0; i < 2; i++)
        {
            BusinessObjectDataEntity parentBusinessObjectDataEntity = createBusinessObjectDataEntity();
            BusinessObjectDataKey businessObjectDataKey = new BusinessObjectDataKey();
            businessObjectDataKey.setNamespace(parentBusinessObjectDataEntity.getBusinessObjectFormat().getBusinessObjectDefinition().getNamespace().getCode());
            businessObjectDataKey
                .setBusinessObjectDefinitionName(parentBusinessObjectDataEntity.getBusinessObjectFormat().getBusinessObjectDefinition().getName());
            businessObjectDataKey.setBusinessObjectFormatUsage(parentBusinessObjectDataEntity.getBusinessObjectFormat().getUsage());
            businessObjectDataKey.setBusinessObjectFormatFileType(parentBusinessObjectDataEntity.getBusinessObjectFormat().getFileType().getCode());
            businessObjectDataKey.setBusinessObjectFormatVersion(parentBusinessObjectDataEntity.getBusinessObjectFormat().getBusinessObjectFormatVersion());
            businessObjectDataKey.setPartitionValue(parentBusinessObjectDataEntity.getPartitionValue());
            businessObjectDataKey.setBusinessObjectDataVersion(parentBusinessObjectDataEntity.getVersion());
            businessObjectDataKey.setSubPartitionValues(dmHelper.getSubPartitionValues(parentBusinessObjectDataEntity));

            businessObjectDataParents.add(businessObjectDataKey);
        }

        return businessObjectDataCreateRequest;
    }

    protected BusinessObjectDataStorageFilesCreateRequest getNewBusinessObjectDataStorageFilesCreateRequest()
    {
        // Crete a test business object format (and associated data).
        BusinessObjectFormatEntity businessObjectFormatEntity = createBusinessObjectFormatEntity(false);
        BusinessObjectDataEntity businessObjectDataEntity =
            createBusinessObjectDataEntity(businessObjectFormatEntity, PARTITION_VALUE, DATA_VERSION, true, BDATA_STATUS);
        StorageEntity storageEntity = createStorageEntity();
        createStorageUnitEntity(storageEntity, businessObjectDataEntity);

        // Create a request to create business object data.
        BusinessObjectDataStorageFilesCreateRequest businessObjectDataStorageFilesCreateRequest = new BusinessObjectDataStorageFilesCreateRequest();
        businessObjectDataStorageFilesCreateRequest.setBusinessObjectDefinitionName(businessObjectFormatEntity.getBusinessObjectDefinition().getName());
        businessObjectDataStorageFilesCreateRequest.setBusinessObjectFormatUsage(businessObjectFormatEntity.getUsage());
        businessObjectDataStorageFilesCreateRequest.setBusinessObjectFormatFileType(businessObjectFormatEntity.getFileType().getCode());
        businessObjectDataStorageFilesCreateRequest.setBusinessObjectFormatVersion(businessObjectFormatEntity.getBusinessObjectFormatVersion());
        businessObjectDataStorageFilesCreateRequest.setPartitionValue(businessObjectDataEntity.getPartitionValue());
        businessObjectDataStorageFilesCreateRequest.setBusinessObjectDataVersion(businessObjectDataEntity.getVersion());

        businessObjectDataStorageFilesCreateRequest.setStorageName(storageEntity.getName());

        List<StorageFile> storageFiles = new ArrayList<>();
        businessObjectDataStorageFilesCreateRequest.setStorageFiles(storageFiles);

        StorageFile storageFile1 = new StorageFile();
        storageFiles.add(storageFile1);
        storageFile1.setFilePath("Folder/file1.gz");
        storageFile1.setFileSizeBytes(0L);
        storageFile1.setRowCount(0L);

        StorageFile storageFile2 = new StorageFile();
        storageFiles.add(storageFile2);
        storageFile2.setFilePath("Folder/file2.gz");
        storageFile2.setFileSizeBytes(2999L);
        storageFile2.setRowCount(1000L);

        StorageFile storageFile3 = new StorageFile();
        storageFiles.add(storageFile3);
        storageFile3.setFilePath("Folder/file3.gz");
        storageFile3.setFileSizeBytes(Long.MAX_VALUE);
        storageFile3.setRowCount(Long.MAX_VALUE);

        return businessObjectDataStorageFilesCreateRequest;
    }

    /**
     * Creates and returns a business object data availability request using passed parameters along with some hard-coded test values.
     *
     * @param partitionValues the list of partition values
     *
     * @return the newly created business object data availability request
     */
    protected BusinessObjectDataAvailabilityRequest getTestBusinessObjectDataAvailabilityRequest(List<String> partitionValues)
    {
        return getTestBusinessObjectDataAvailabilityRequest(FIRST_PARTITION_COLUMN_NAME, null, null, partitionValues);
    }

    /**
     * Creates and returns a business object data availability request using passed parameters along with some hard-coded test values.
     *
     * @param partitionKey the partition key
     * @param partitionValues the list of partition values
     *
     * @return the newly created business object data availability request
     */
    protected BusinessObjectDataAvailabilityRequest getTestBusinessObjectDataAvailabilityRequest(String partitionKey, List<String> partitionValues)
    {
        return getTestBusinessObjectDataAvailabilityRequest(partitionKey, null, null, partitionValues);
    }

    /**
     * Creates and returns a business object data availability request using passed parameters along with some hard-coded test values.
     *
     * @param startPartitionValue the start partition value for the partition value range
     * @param endPartitionValue the end partition value for the partition value range
     *
     * @return the newly created business object data availability request
     */
    protected BusinessObjectDataAvailabilityRequest getTestBusinessObjectDataAvailabilityRequest(String startPartitionValue, String endPartitionValue)
    {
        return getTestBusinessObjectDataAvailabilityRequest(FIRST_PARTITION_COLUMN_NAME, startPartitionValue, endPartitionValue, null);
    }

    /**
     * Creates and returns a business object data availability request using passed parameters along with some hard-coded test values.
     *
     * @param partitionKey the partition key
     * @param startPartitionValue the start partition value for the partition value range
     * @param endPartitionValue the end partition value for the partition value range
     * @param partitionValues the list of partition values
     *
     * @return the newly created business object data availability request
     */
    protected BusinessObjectDataAvailabilityRequest getTestBusinessObjectDataAvailabilityRequest(String partitionKey, String startPartitionValue,
        String endPartitionValue, List<String> partitionValues)
    {
        BusinessObjectDataAvailabilityRequest request = new BusinessObjectDataAvailabilityRequest();

        request.setNamespace(NAMESPACE_CD);
        request.setBusinessObjectDefinitionName(BOD_NAME);
        request.setBusinessObjectFormatUsage(FORMAT_USAGE_CODE);
        request.setBusinessObjectFormatFileType(FORMAT_FILE_TYPE_CODE);
        request.setBusinessObjectFormatVersion(FORMAT_VERSION);

        PartitionValueFilter partitionValueFilter = new PartitionValueFilter();
        request.setPartitionValueFilters(Arrays.asList(partitionValueFilter));
        partitionValueFilter.setPartitionKey(partitionKey);

        if (startPartitionValue != null || endPartitionValue != null)
        {
            PartitionValueRange partitionValueRange = new PartitionValueRange();
            partitionValueFilter.setPartitionValueRange(partitionValueRange);
            partitionValueRange.setStartPartitionValue(startPartitionValue);
            partitionValueRange.setEndPartitionValue(endPartitionValue);
        }

        if (partitionValues != null)
        {
            partitionValueFilter.setPartitionValues(new ArrayList<>(partitionValues));
        }

        request.setBusinessObjectDataVersion(DATA_VERSION);
        request.setStorageName(STORAGE_NAME);

        return request;
    }

    /**
     * Creates and persists database entities required for business object data availability collection testing.
     */
    protected void createDatabaseEntitiesForBusinessObjectDataAvailabilityCollectionTesting()
    {
        // Create a business object data entity.
        BusinessObjectDataEntity businessObjectDataEntity =
            createBusinessObjectDataEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE,
                SUBPARTITION_VALUES, DATA_VERSION, true, BusinessObjectDataStatusEntity.VALID);

        // Create a storage entity and the relative storage unit entity.
        StorageEntity storageEntity = createStorageEntity(STORAGE_NAME);
        createStorageUnitEntity(storageEntity, businessObjectDataEntity);
    }

    /**
     * Creates a check business object data availability collection request using hard coded test values.
     *
     * @return the business object data availability collection request
     */
    protected BusinessObjectDataAvailabilityCollectionRequest getTestBusinessObjectDataAvailabilityCollectionRequest()
    {
        // Create a check business object data availability collection request.
        BusinessObjectDataAvailabilityCollectionRequest businessObjectDataAvailabilityCollectionRequest = new BusinessObjectDataAvailabilityCollectionRequest();

        // Create a list of check business object data availability requests.
        List<BusinessObjectDataAvailabilityRequest> businessObjectDataAvailabilityRequests = new ArrayList<>();
        businessObjectDataAvailabilityCollectionRequest.setBusinessObjectDataAvailabilityRequests(businessObjectDataAvailabilityRequests);

        // Create a business object data availability request.
        BusinessObjectDataAvailabilityRequest businessObjectDataAvailabilityRequest =
            new BusinessObjectDataAvailabilityRequest(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, Arrays.asList(
                new PartitionValueFilter(PARTITION_KEY, Arrays.asList(PARTITION_VALUE), NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
                    NO_LATEST_AFTER_PARTITION_VALUE)), null, DATA_VERSION, NO_STORAGE_NAMES, STORAGE_NAME);
        businessObjectDataAvailabilityRequests.add(businessObjectDataAvailabilityRequest);

        return businessObjectDataAvailabilityCollectionRequest;
    }

    /**
     * Creates an expected business object data availability collection response using hard coded test values.
     *
     * @return the business object data availability collection response
     */
    protected BusinessObjectDataAvailabilityCollectionResponse getExpectedBusinessObjectDataAvailabilityCollectionResponse()
    {
        // Prepare a check availability collection response using hard coded test values.
        BusinessObjectDataAvailabilityCollectionResponse businessObjectDataAvailabilityCollectionResponse =
            new BusinessObjectDataAvailabilityCollectionResponse();

        // Create a list of check business object data availability responses.
        List<BusinessObjectDataAvailability> businessObjectDataAvailabilityResponses = new ArrayList<>();
        businessObjectDataAvailabilityCollectionResponse.setBusinessObjectDataAvailabilityResponses(businessObjectDataAvailabilityResponses);

        // Create a business object data availability response.
        BusinessObjectDataAvailability businessObjectDataAvailability =
            new BusinessObjectDataAvailability(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, Arrays.asList(
                new PartitionValueFilter(PARTITION_KEY, Arrays.asList(PARTITION_VALUE), NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
                    NO_LATEST_AFTER_PARTITION_VALUE)), null, DATA_VERSION, NO_STORAGE_NAMES, STORAGE_NAME, Arrays
                .asList(new BusinessObjectDataStatus(FORMAT_VERSION, PARTITION_VALUE, SUBPARTITION_VALUES, DATA_VERSION, BusinessObjectDataStatusEntity.VALID)),
                new ArrayList<BusinessObjectDataStatus>());
        businessObjectDataAvailabilityResponses.add(businessObjectDataAvailability);

        // Set the expected values for the flags.
        businessObjectDataAvailabilityCollectionResponse.setIsAllDataAvailable(true);
        businessObjectDataAvailabilityCollectionResponse.setIsAllDataNotAvailable(false);

        return businessObjectDataAvailabilityCollectionResponse;
    }

    /**
     * Returns a list of schema columns that use hard coded test values.
     *
     * @return the list of test schema column entities
     */
    protected List<SchemaColumn> getTestSchemaColumns()
    {
        return getTestSchemaColumns("COLUMN", SCHEMA_COLUMNS);
    }

    /**
     * Returns a list of schema columns that use hard coded test values.
     *
     * @return the list of test schema column entities
     */
    protected List<SchemaColumn> getTestPartitionColumns()
    {
        return getTestSchemaColumns("PRTN_CLMN", PARTITION_COLUMNS);
    }

    /**
     * Returns a list of schema columns that use hard coded test values.
     *
     * @return the list of test schema column entities
     */
    protected List<SchemaColumn> getTestSchemaColumns(String columnNamePrefix, String[][] schemaColumnDataTypes)
    {
        // Build a list of schema columns.
        List<SchemaColumn> schemaColumns = new ArrayList<>();

        int index = 1;
        for (String[] schemaColumnDataType : schemaColumnDataTypes)
        {
            SchemaColumn schemaColumn = new SchemaColumn();
            schemaColumns.add(schemaColumn);
            String columnName = String.format("%s%03d", columnNamePrefix, index);
            schemaColumn.setName(columnName);
            schemaColumn.setType(schemaColumnDataType[0]);
            schemaColumn.setSize(schemaColumnDataType[1]);
            index++;
        }

        // Column comment is an optional field, so provide comment for the second column only.
        schemaColumns.get(1).setDescription(
            String.format("This is '%s' column. Here are \\'single\\' and \"double\" quotes along with a backslash \\.", schemaColumns.get(1).getName()));

        return schemaColumns;
    }

    /**
     * Returns S3 key prefix constructed according to the S3 Naming Convention Wiki page.
     *
     * @param namespaceCd the namespace code
     * @param dataProviderName the data provider name
     * @param businessObjectDefinitionName the business object definition name
     * @param formatUsage the format usage
     * @param formatFileType the format file type
     * @param businessObjectFormatVersion the format version
     * @param partitionKey the format partition key
     * @param partitionValue the business object data partition value
     * @param subPartitionKeys the list of subpartition keys for the business object data
     * @param subPartitionValues the list of subpartition values for the business object data
     * @param businessObjectDataVersion the business object data version
     *
     * @return the S3 key prefix constructed according to the S3 Naming Convention Wiki page TODO: Point to GitHub @see <a
     *         href="http://.../confluence/display/DataManagement/S3+Naming+Convention">S3 Naming Convention</a>
     */
    protected String getExpectedS3KeyPrefix(String namespaceCd, String dataProviderName, String businessObjectDefinitionName, String formatUsage,
        String formatFileType, Integer businessObjectFormatVersion, String partitionKey, String partitionValue, SchemaColumn[] subPartitionKeys,
        String[] subPartitionValues, Integer businessObjectDataVersion)
    {
        return getExpectedS3KeyPrefix(namespaceCd, dataProviderName, businessObjectDefinitionName, formatUsage, formatFileType, businessObjectFormatVersion,
            partitionKey, partitionValue, subPartitionKeys, subPartitionValues, businessObjectDataVersion, "frmt-v");
    }

    /**
     * Returns S3 key prefix constructed according to the S3 Naming Convention Wiki page.
     *
     * @param namespaceCd the namespace code
     * @param dataProviderName the data provider name
     * @param businessObjectDefinitionName the business object definition name
     * @param formatUsage the format usage
     * @param formatFileType the format file type
     * @param businessObjectFormatVersion the format version
     * @param partitionKey the format partition key
     * @param partitionValue the business object data partition value
     * @param subPartitionKeys the list of subpartition keys for the business object data
     * @param subPartitionValues the list of subpartition values for the business object data
     * @param businessObjectDataVersion the business object data version
     * @param formatVersionPrefix format version prefix (either "frmt-v" or "schm-v")
     *
     * @return the S3 key prefix constructed according to the S3 Naming Convention
     */
    protected String getExpectedS3KeyPrefix(String namespaceCd, String dataProviderName, String businessObjectDefinitionName, String formatUsage,
        String formatFileType, Integer businessObjectFormatVersion, String partitionKey, String partitionValue, SchemaColumn[] subPartitionKeys,
        String[] subPartitionValues, Integer businessObjectDataVersion, String formatVersionPrefix)
    {
        StringBuilder s3KeyPrefix = new StringBuilder(String
            .format("%s/%s/%s/%s/%s/" + formatVersionPrefix + "%d/data-v%d/%s=%s", namespaceCd.trim().toLowerCase().replace('_', '-'),
                dataProviderName.trim().toLowerCase().replace('_', '-'), formatUsage.trim().toLowerCase().replace('_', '-'),
                formatFileType.trim().toLowerCase().replace('_', '-'), businessObjectDefinitionName.trim().toLowerCase().replace('_', '-'),
                businessObjectFormatVersion, businessObjectDataVersion, partitionKey.trim().toLowerCase().replace('_', '-'), partitionValue.trim()));

        if (subPartitionKeys != null)
        {
            for (int i = 0; i < subPartitionKeys.length; i++)
            {
                s3KeyPrefix.append("/").append(subPartitionKeys[i].getName().trim().toLowerCase().replace('_', '-')).append("=").append(subPartitionValues[i]);
            }
        }

        return s3KeyPrefix.toString();
    }

    /**
     * Creates and returns a business object data ddl request using passed parameters along with some hard-coded test values.
     *
     * @param partitionValues the list of partition values
     *
     * @return the newly created business object data ddl request
     */
    protected BusinessObjectDataDdlRequest getTestBusinessObjectDataDdlRequest(List<String> partitionValues)
    {
        return getTestBusinessObjectDataDdlRequest(null, null, partitionValues, BLANK_TEXT);
    }

    /**
     * Creates and returns a business object data ddl request using passed parameters along with some hard-coded test values.
     *
     * @param startPartitionValue the start partition value for the partition value range
     * @param endPartitionValue the end partition value for the partition value range
     *
     * @return the newly created business object data ddl request
     */
    protected BusinessObjectDataDdlRequest getTestBusinessObjectDataDdlRequest(String startPartitionValue, String endPartitionValue)
    {
        return getTestBusinessObjectDataDdlRequest(startPartitionValue, endPartitionValue, null, BLANK_TEXT);
    }

    /**
     * Creates and returns a business object data ddl request using passed parameters along with some hard-coded test values.
     *
     * @param partitionValues the list of partition values
     * @param customDdlName the custom DDL name
     *
     * @return the newly created business object data ddl request
     */
    protected BusinessObjectDataDdlRequest getTestBusinessObjectDataDdlRequest(List<String> partitionValues, String customDdlName)
    {
        return getTestBusinessObjectDataDdlRequest(null, null, partitionValues, customDdlName);
    }

    /**
     * Creates and returns a business object data ddl request using passed parameters along with some hard-coded test values.
     *
     * @param startPartitionValue the start partition value for the partition value range
     * @param endPartitionValue the end partition value for the partition value range * @param customDdlName the custom DDL name
     * @param customDdlName the custom DDL name
     *
     * @return the newly created business object data ddl request
     */
    protected BusinessObjectDataDdlRequest getTestBusinessObjectDataDdlRequest(String startPartitionValue, String endPartitionValue, String customDdlName)
    {
        return getTestBusinessObjectDataDdlRequest(startPartitionValue, endPartitionValue, null, customDdlName);
    }

    /**
     * Creates and returns a business object data ddl request using passed parameters along with some hard-coded test values.
     *
     * @param startPartitionValue the start partition value for the partition value range
     * @param endPartitionValue the end partition value for the partition value range
     * @param partitionValues the list of partition values
     * @param customDdlName the custom DDL name
     *
     * @return the newly created business object data ddl request
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected BusinessObjectDataDdlRequest getTestBusinessObjectDataDdlRequest(String startPartitionValue, String endPartitionValue,
        List<String> partitionValues, String customDdlName)
    {
        BusinessObjectDataDdlRequest request = new BusinessObjectDataDdlRequest();

        request.setNamespace(NAMESPACE_CD);
        request.setBusinessObjectDefinitionName(BOD_NAME);
        request.setBusinessObjectFormatUsage(FORMAT_USAGE_CODE);
        request.setBusinessObjectFormatFileType(FileTypeEntity.TXT_FILE_TYPE);
        request.setBusinessObjectFormatVersion(FORMAT_VERSION);

        PartitionValueFilter partitionValueFilter = new PartitionValueFilter();
        request.setPartitionValueFilters(Arrays.asList(partitionValueFilter));
        partitionValueFilter.setPartitionKey(FIRST_PARTITION_COLUMN_NAME);

        if (startPartitionValue != null || endPartitionValue != null)
        {
            PartitionValueRange partitionValueRange = new PartitionValueRange();
            partitionValueFilter.setPartitionValueRange(partitionValueRange);
            partitionValueRange.setStartPartitionValue(startPartitionValue);
            partitionValueRange.setEndPartitionValue(endPartitionValue);
        }

        if (partitionValues != null)
        {
            partitionValueFilter.setPartitionValues(new ArrayList(partitionValues));
        }

        request.setBusinessObjectDataVersion(DATA_VERSION);
        request.setStorageName(STORAGE_NAME);
        request.setOutputFormat(BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL);
        request.setTableName(TABLE_NAME);
        request.setCustomDdlName(customDdlName);
        request.setIncludeDropTableStatement(true);
        request.setIncludeIfNotExistsOption(true);
        request.setAllowMissingData(true);

        return request;
    }

    /**
     * Creates and persists database entities required for generating business object data and format ddl testing.
     */
    protected void createDatabaseEntitiesForBusinessObjectDataDdlTesting(String partitionValue)
    {
        if (partitionValue != null)
        {
            // Build an S3 key prefix according to the Data Management S3 naming convention.
            String s3KeyPrefix =
                getExpectedS3KeyPrefix(NAMESPACE_CD, DATA_PROVIDER_NAME, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION,
                    PARTITION_KEY, partitionValue, null, null, DATA_VERSION);

            // Creates and persists database entities required for generating business object data ddl testing.
            createDatabaseEntitiesForBusinessObjectDataDdlTesting(partitionValue, s3KeyPrefix);
        }
        else
        {
            // Creates and persists database entities required for generating business object format ddl testing.
            createDatabaseEntitiesForBusinessObjectDataDdlTesting(null, null);
        }
    }

    /**
     * Creates and persists database entities required for generating business object data ddl testing.
     */
    protected void createDatabaseEntitiesForBusinessObjectDataDdlTesting(String partitionValue, String s3KeyPrefix)
    {
        // Build a list of schema columns.
        List<SchemaColumn> schemaColumns = new ArrayList<>();
        schemaColumns.add(new SchemaColumn(PARTITION_KEY, "DATE", NO_COLUMN_SIZE, COLUMN_REQUIRED, NO_COLUMN_DEFAULT_VALUE, NO_COLUMN_DESCRIPTION));
        schemaColumns.add(new SchemaColumn(COLUMN_NAME, "NUMBER", COLUMN_SIZE, NO_COLUMN_REQUIRED, COLUMN_DEFAULT_VALUE, COLUMN_DESCRIPTION));

        // Use the first column as a partition column.
        List<SchemaColumn> partitionColumns = schemaColumns.subList(0, 1);

        // Create a business object format entity with the schema.
        BusinessObjectFormatEntity businessObjectFormatEntity =
            createBusinessObjectFormatEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, FORMAT_DESCRIPTION,
                LATEST_VERSION_FLAG_SET, PARTITION_KEY, NO_PARTITION_KEY_GROUP, NO_ATTRIBUTES, SCHEMA_DELIMITER_PIPE, SCHEMA_ESCAPE_CHARACTER_BACKSLASH,
                SCHEMA_NULL_VALUE_BACKSLASH_N, schemaColumns, partitionColumns);

        if (partitionValue != null)
        {
            // Create a business object data entity.
            BusinessObjectDataEntity businessObjectDataEntity =
                createBusinessObjectDataEntity(businessObjectFormatEntity, partitionValue, NO_SUBPARTITION_VALUES, DATA_VERSION, true,
                    BusinessObjectDataStatusEntity.VALID);

            // Create an S3 storage entity.
            StorageEntity storageEntity = createStorageEntity(STORAGE_NAME, StoragePlatformEntity.S3);

            // Add a bucket name attribute to the storage.
            createStorageAttributeEntity(storageEntity, configurationHelper.getProperty(ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME), S3_BUCKET_NAME);
            dmDao.saveAndRefresh(storageEntity);

            // Create a storage unit with a storage directory path.
            createStorageUnitEntity(storageEntity, businessObjectDataEntity, s3KeyPrefix);
        }
    }

    /**
     * Creates and persists database entities required for generating business object data ddl collection testing.
     */
    protected void createDatabaseEntitiesForBusinessObjectDataDdlCollectionTesting()
    {
        createDatabaseEntitiesForBusinessObjectDataDdlTesting(PARTITION_VALUE);
    }

    /**
     * Creates a generate business object data ddl collection request using hard coded test values.
     *
     * @return the business object data ddl collection request
     */
    protected BusinessObjectDataDdlCollectionRequest getTestBusinessObjectDataDdlCollectionRequest()
    {
        // Create a generate business object data ddl collection request.
        BusinessObjectDataDdlCollectionRequest businessObjectDataDdlCollectionRequest = new BusinessObjectDataDdlCollectionRequest();

        // Create a list of generate business object data ddl requests.
        List<BusinessObjectDataDdlRequest> businessObjectDataDdlRequests = new ArrayList<>();
        businessObjectDataDdlCollectionRequest.setBusinessObjectDataDdlRequests(businessObjectDataDdlRequests);

        // Create a generate business object data ddl request.
        BusinessObjectDataDdlRequest businessObjectDataDdlRequest =
            new BusinessObjectDataDdlRequest(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, Arrays.asList(
                new PartitionValueFilter(PARTITION_KEY, Arrays.asList(PARTITION_VALUE), NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
                    NO_LATEST_AFTER_PARTITION_VALUE)), NO_STANDALONE_PARTITION_VALUE_FILTER, DATA_VERSION, NO_STORAGE_NAMES, STORAGE_NAME,
                BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, INCLUDE_DROP_TABLE_STATEMENT, INCLUDE_IF_NOT_EXISTS_OPTION,
                INCLUDE_DROP_PARTITIONS, NO_ALLOW_MISSING_DATA);

        // Add two business object ddl requests to the collection request.
        businessObjectDataDdlRequests.add(businessObjectDataDdlRequest);
        businessObjectDataDdlRequests.add(businessObjectDataDdlRequest);

        return businessObjectDataDdlCollectionRequest;
    }

    /**
     * Creates an expected generate business object data ddl collection response using hard coded test values.
     *
     * @return the business object data ddl collection response
     */
    protected BusinessObjectDataDdlCollectionResponse getExpectedBusinessObjectDataDdlCollectionResponse()
    {
        // Prepare a generate business object data collection response using hard coded test values.
        BusinessObjectDataDdlCollectionResponse businessObjectDataDdlCollectionResponse = new BusinessObjectDataDdlCollectionResponse();

        // Create a list of business object data ddl responses.
        List<BusinessObjectDataDdl> businessObjectDataDdlResponses = new ArrayList<>();
        businessObjectDataDdlCollectionResponse.setBusinessObjectDataDdlResponses(businessObjectDataDdlResponses);

        // Get the actual HIVE DDL expected to be generated.
        String expectedDdl = getExpectedHiveDdl(PARTITION_VALUE);

        // Create a business object data ddl response.
        BusinessObjectDataDdl expectedBusinessObjectDataDdl =
            new BusinessObjectDataDdl(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION, Arrays.asList(
                new PartitionValueFilter(PARTITION_KEY, Arrays.asList(PARTITION_VALUE), NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
                    NO_LATEST_AFTER_PARTITION_VALUE)), NO_STANDALONE_PARTITION_VALUE_FILTER, DATA_VERSION, NO_STORAGE_NAMES, STORAGE_NAME,
                BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, NO_CUSTOM_DDL_NAME, expectedDdl);

        // Add two business object ddl responses to the collection response.
        businessObjectDataDdlResponses.add(expectedBusinessObjectDataDdl);
        businessObjectDataDdlResponses.add(expectedBusinessObjectDataDdl);

        // Set the expected DDL collection value.
        businessObjectDataDdlCollectionResponse.setDdlCollection(String.format("%s\n\n%s", expectedDdl, expectedDdl));

        return businessObjectDataDdlCollectionResponse;
    }

    /**
     * Returns the actual HIVE DDL expected to be generated.
     *
     * @return the actual HIVE DDL expected to be generated
     */
    protected String getExpectedHiveDdl(String partitionValue)
    {
        // Build ddl expected to be generated.
        StringBuilder ddlBuilder = new StringBuilder();
        ddlBuilder.append("DROP TABLE IF EXISTS `" + TABLE_NAME + "`;\n");
        ddlBuilder.append("\n");
        ddlBuilder.append("CREATE EXTERNAL TABLE IF NOT EXISTS `" + TABLE_NAME + "` (\n");
        ddlBuilder.append("    `ORGNL_" + PARTITION_KEY + "` DATE,\n");
        ddlBuilder.append("    `" + COLUMN_NAME + "` DECIMAL(" + COLUMN_SIZE + ") COMMENT '" + COLUMN_DESCRIPTION + "')\n");
        ddlBuilder.append("PARTITIONED BY (`" + PARTITION_KEY + "` DATE)\n");
        ddlBuilder.append("ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' ESCAPED BY '\\\\' NULL DEFINED AS '\\N'\n");
        ddlBuilder.append("STORED AS TEXTFILE;");

        if (partitionValue != null)
        {
            // Add the alter table drop partition statement.
            ddlBuilder.append("\n\n");
            ddlBuilder.append("ALTER TABLE `" + TABLE_NAME + "` DROP IF EXISTS PARTITION (`" + PARTITION_KEY + "`='" + partitionValue + "');");

            // Build an expected S3 key prefix.
            String expectedS3KeyPrefix =
                getExpectedS3KeyPrefix(NAMESPACE_CD, DATA_PROVIDER_NAME, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION,
                    PARTITION_KEY, partitionValue, null, null, DATA_VERSION);

            // Add the alter table add partition statement.
            ddlBuilder.append("\n\n");
            ddlBuilder.append("ALTER TABLE `" + TABLE_NAME + "` ADD IF NOT EXISTS PARTITION (`" + PARTITION_KEY + "`='" + partitionValue +
                "') LOCATION 's3n://" + S3_BUCKET_NAME + "/" + expectedS3KeyPrefix + "';");
        }

        String expectedDdl = ddlBuilder.toString();

        return expectedDdl;
    }

    /**
     * Gets a test system monitor incoming message paylog.
     *
     * @return the system monitor incoming message.
     */
    protected String getTestSystemMonitorIncomingMessage()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("<datamgt:monitor xmlns:datamgt=\"http://testDomain/system-monitor\" " + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xsi:schemaLocation=\"http://testDomain/system-monitor.xsd\">\n");
        builder.append("   <header>\n");
        builder.append("      <producer>\n");
        builder.append("         <name>testName</name>\n");
        builder.append("         <environment>" + TEST_SQS_ENVIRONMENT + "</environment>\n");
        builder.append("         <origin>testOrigin</origin>\n");
        builder.append("      </producer>\n");
        builder.append("      <creation>\n");
        builder.append("         <datetime>2015-05-13T11:23:36.217-04:00</datetime>\n");
        builder.append("      </creation>\n");
        builder.append("      <correlation-id>" + TEST_SQS_MESSAGE_CORRELATION_ID + "</correlation-id>\n");
        builder.append("      <context-message-type>testDomain/datamanagement/SysmonTest</context-message-type>\n");
        builder.append("      <system-message-type>testSystemMessageType</system-message-type>\n");
        builder.append("      <xsd>testXsd</xsd>\n");
        builder.append("   </header>\n");
        builder.append("   <payload>\n");
        builder.append("      <contextMessageTypeToPublish>" + TEST_SQS_CONTEXT_MESSAGE_TYPE_TO_PUBLISH + "</contextMessageTypeToPublish>\n");
        builder.append("   </payload>\n");
        builder.append("</datamgt:monitor>");

        return builder.toString();
    }

    /**
     * Validates that the specified system monitor response message is valid. If not, an exception will be thrown.
     *
     * @param systemMonitorResponseMessage the system monitor response message.
     */
    protected void validateSystemMonitorResponse(String systemMonitorResponseMessage)
    {
        // Validate the message.
        assertTrue("Correlation Id \"" + TEST_SQS_MESSAGE_CORRELATION_ID + "\" expected, but not found.",
            systemMonitorResponseMessage.contains("<correlation-id>" + TEST_SQS_MESSAGE_CORRELATION_ID + "</correlation-id>"));
        assertTrue("Context Message Type \"" + TEST_SQS_CONTEXT_MESSAGE_TYPE_TO_PUBLISH + "\" expected, but not found.",
            systemMonitorResponseMessage.contains("<context-message-type>" + TEST_SQS_CONTEXT_MESSAGE_TYPE_TO_PUBLISH + "</context-message-type>"));

        // Note that we don't response with the environment that was specified in the request message. Instead, we respond with the environment configured
        // in our configuration table.
        assertTrue("Environment \"Development\" expected, but not found.", systemMonitorResponseMessage.contains("<environment>Development</environment>"));
    }

    /**
     * Create and persist database entities required for upload download testing.
     */
    protected void createDatabaseEntitiesForUploadDownloadTesting()
    {
        createDatabaseEntitiesForUploadDownloadTesting(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION);
        createDatabaseEntitiesForUploadDownloadTesting(NAMESPACE_CD_2, BOD_NAME_2, FORMAT_USAGE_CODE_2, FORMAT_FILE_TYPE_CODE_2, FORMAT_VERSION_2);
    }

    /**
     * Create and persist database entities required for upload download testing.
     *
     * @param namespaceCode the namespace code
     * @param businessObjectDefinitionName the business object definition name
     * @param businessObjectFormatUsage the business object format file type
     * @param businessObjectFormatFileType the business object format file type
     */
    protected void createDatabaseEntitiesForUploadDownloadTesting(String namespaceCode, String businessObjectDefinitionName, String businessObjectFormatUsage,
        String businessObjectFormatFileType, Integer businessObjectFormatVersion)
    {
        // Create a business object format entity.
        createBusinessObjectFormatEntity(namespaceCode, businessObjectDefinitionName, businessObjectFormatUsage, businessObjectFormatFileType,
            businessObjectFormatVersion, FORMAT_DESCRIPTION, true, PARTITION_KEY);
    }


    protected UploadSingleInitiationRequest createUploadSingleInitiationRequest()
    {
        return createUploadSingleInitiationRequest(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, NAMESPACE_CD_2, BOD_NAME_2,
            FORMAT_USAGE_CODE_2, FORMAT_FILE_TYPE_CODE_2, FORMAT_VERSION_2, FILE_NAME);
    }

    /**
     * Creates a upload single initiation request.
     *
     * @param sourceNamespaceCode the source namespace code
     * @param sourceBusinessObjectDefinitionName the source business object definition name
     * @param sourceBusinessObjectFormatUsage the source business object usage
     * @param sourceBusinessObjectFormatFileType the source business object format file type
     * @param sourceBusinessObjectFormatVersion the source business object format version
     * @param targetNamespaceCode the target namespace code
     * @param targetBusinessObjectDefinitionName the target business object definition name
     * @param targetBusinessObjectFormatUsage the target business object usage
     * @param targetBusinessObjectFormatFileType the target business object format file type
     * @param targetBusinessObjectFormatVersion the target business object format version
     *
     * @return the newly created upload single initiation request
     */
    protected UploadSingleInitiationRequest createUploadSingleInitiationRequest(String sourceNamespaceCode, String sourceBusinessObjectDefinitionName,
        String sourceBusinessObjectFormatUsage, String sourceBusinessObjectFormatFileType, Integer sourceBusinessObjectFormatVersion,
        String targetNamespaceCode, String targetBusinessObjectDefinitionName, String targetBusinessObjectFormatUsage,
        String targetBusinessObjectFormatFileType, Integer targetBusinessObjectFormatVersion)
    {
        return createUploadSingleInitiationRequest(sourceNamespaceCode, sourceBusinessObjectDefinitionName, sourceBusinessObjectFormatUsage,
            sourceBusinessObjectFormatFileType, sourceBusinessObjectFormatVersion, targetNamespaceCode, targetBusinessObjectDefinitionName,
            targetBusinessObjectFormatUsage, targetBusinessObjectFormatFileType, targetBusinessObjectFormatVersion, FILE_NAME);
    }

    /**
     * Creates a upload single initiation request.
     *
     * @param sourceNamespaceCode the source namespace code
     * @param sourceBusinessObjectDefinitionName the source business object definition name
     * @param sourceBusinessObjectFormatUsage the source business object usage
     * @param sourceBusinessObjectFormatFileType the source business object format file type
     * @param sourceBusinessObjectFormatVersion the source business object format version
     * @param targetNamespaceCode the target namespace code
     * @param targetBusinessObjectDefinitionName the target business object definition name
     * @param targetBusinessObjectFormatUsage the target business object usage
     * @param targetBusinessObjectFormatFileType the target business object format file type
     * @param targetBusinessObjectFormatVersion the target business object format version
     * @param fileName the file name
     *
     * @return the newly created upload single initiation request
     */
    protected UploadSingleInitiationRequest createUploadSingleInitiationRequest(String sourceNamespaceCode, String sourceBusinessObjectDefinitionName,
        String sourceBusinessObjectFormatUsage, String sourceBusinessObjectFormatFileType, Integer sourceBusinessObjectFormatVersion,
        String targetNamespaceCode, String targetBusinessObjectDefinitionName, String targetBusinessObjectFormatUsage,
        String targetBusinessObjectFormatFileType, Integer targetBusinessObjectFormatVersion, String fileName)
    {
        UploadSingleInitiationRequest request = new UploadSingleInitiationRequest();

        request.setSourceBusinessObjectFormatKey(
            new BusinessObjectFormatKey(sourceNamespaceCode, sourceBusinessObjectDefinitionName, sourceBusinessObjectFormatUsage,
                sourceBusinessObjectFormatFileType, sourceBusinessObjectFormatVersion));
        request.setTargetBusinessObjectFormatKey(
            new BusinessObjectFormatKey(targetNamespaceCode, targetBusinessObjectDefinitionName, targetBusinessObjectFormatUsage,
                targetBusinessObjectFormatFileType, targetBusinessObjectFormatVersion));
        request.setBusinessObjectDataAttributes(getNewAttributes());
        request.setFile(new File(fileName, FILE_SIZE_1_KB));

        return request;
    }

    /**
     * Returns a copy of the string, with some leading and trailing whitespace added.
     *
     * @param string the string that we want to add leading and trailing whitespace to
     *
     * @return the string with leading and trailing whitespace added
     */
    protected String addWhitespace(String string)
    {
        return String.format("  %s    ", string);
    }

    /**
     * Adds leading and trailing whitespace characters to all members in this list.
     *
     * @param list the list of string values
     *
     * @return the list of string values with leading and trailing whitespace characters
     */
    protected List<String> addWhitespace(List<String> list)
    {
        List<String> whitespaceList = new ArrayList<>();

        for (String value : list)
        {
            whitespaceList.add(addWhitespace(value));
        }

        return whitespaceList;
    }

    /**
     * Converts all of the members in this list to lower case.
     *
     * @param list the list of string values
     *
     * @return the list of lower case strings
     */
    protected List<String> convertListToLowerCase(List<String> list)
    {
        List<String> lowerCaseList = new ArrayList<>();

        for (String value : list)
        {
            lowerCaseList.add(value.toLowerCase());
        }

        return lowerCaseList;
    }

    /**
     * Converts all of the members in this list to upper case.
     *
     * @param list the list of string values
     *
     * @return the list of upper case strings
     */
    protected List<String> convertListToUpperCase(List<String> list)
    {
        List<String> upperCaseList = new ArrayList<>();

        for (String value : list)
        {
            upperCaseList.add(value.toUpperCase());
        }

        return upperCaseList;
    }

    /**
     * Returns the business object format not found error message per specified parameters.
     *
     * @param namespaceCode the namespace code
     * @param businessObjectDefinitionName the business object definition name
     * @param businessObjectFormatUsage the business object format usage
     * @param businessObjectFormatFileType the business object format file type
     * @param businessObjectFormatVersion the business object format version
     *
     * @return the business object data not found error message
     */
    protected String getExpectedBusinessObjectFormatNotFoundErrorMessage(String namespaceCode, String businessObjectDefinitionName,
        String businessObjectFormatUsage, String businessObjectFormatFileType, Integer businessObjectFormatVersion)
    {
        return String.format("Business object format with namespace \"%s\", business object definition name \"%s\"," +
            " format usage \"%s\", format file type \"%s\", and format version \"%d\" doesn't exist.", namespaceCode, businessObjectDefinitionName,
            businessObjectFormatUsage, businessObjectFormatFileType, businessObjectFormatVersion);
    }

    /**
     * Returns the business object data not found error message per specified parameters.
     *
     * @param namespaceCode the namespace code
     * @param businessObjectDefinitionName the business object definition name
     * @param businessObjectFormatUsage the business object format usage
     * @param businessObjectFormatFileType the business object format file type
     * @param businessObjectFormatVersion the business object format version
     * @param partitionValue the partition value
     * @param subPartitionValues the list of subpartition values
     * @param businessObjectDataVersion the business object data version
     * @param businessObjectDataStatus the business object data status
     *
     * @return the business object data not found error message
     */
    protected String getExpectedBusinessObjectDataNotFoundErrorMessage(String namespaceCode, String businessObjectDefinitionName,
        String businessObjectFormatUsage, String businessObjectFormatFileType, Integer businessObjectFormatVersion, String partitionValue,
        List<String> subPartitionValues, Integer businessObjectDataVersion, String businessObjectDataStatus)
    {
        return String.format("Business object data {%s, businessObjectDataStatus: \"%s\"} doesn't exist.",
            getExpectedBusinessObjectDataKeyAsString(namespaceCode, businessObjectDefinitionName, businessObjectFormatUsage, businessObjectFormatFileType,
                businessObjectFormatVersion, partitionValue, subPartitionValues, businessObjectDataVersion), businessObjectDataStatus);
    }

    /**
     * Returns an expected string representation of a specified business object data key.
     *
     * @param businessObjectDataKey the business object data key
     *
     * @return the string representation of the specified business object data key
     */
    protected String getExpectedBusinessObjectDataKeyAsString(BusinessObjectDataKey businessObjectDataKey)
    {
        return getExpectedBusinessObjectDataKeyAsString(businessObjectDataKey.getNamespace(), businessObjectDataKey.getBusinessObjectDefinitionName(),
            businessObjectDataKey.getBusinessObjectFormatUsage(), businessObjectDataKey.getBusinessObjectFormatFileType(),
            businessObjectDataKey.getBusinessObjectFormatVersion(), businessObjectDataKey.getPartitionValue(), businessObjectDataKey.getSubPartitionValues(),
            businessObjectDataKey.getBusinessObjectDataVersion());
    }

    /**
     * Returns an expected string representation of a specified business object data key.
     *
     * @param namespaceCode the namespace code
     * @param businessObjectDefinitionName the business object definition name
     * @param businessObjectFormatUsage the business object format usage
     * @param businessObjectFormatFileType the business object format file type
     * @param businessObjectFormatVersion the business object format version
     * @param partitionValue the partition value
     * @param subPartitionValues the list of subpartition values
     * @param businessObjectDataVersion the business object data version
     *
     * @return the string representation of the specified business object data key
     */
    protected String getExpectedBusinessObjectDataKeyAsString(String namespaceCode, String businessObjectDefinitionName, String businessObjectFormatUsage,
        String businessObjectFormatFileType, Integer businessObjectFormatVersion, String partitionValue, List<String> subPartitionValues,
        Integer businessObjectDataVersion)
    {
        return String.format("namespace: \"%s\", businessObjectDefinitionName: \"%s\", businessObjectFormatUsage: \"%s\", " +
            "businessObjectFormatFileType: \"%s\", businessObjectFormatVersion: %d, businessObjectDataPartitionValue: \"%s\", " +
            "businessObjectDataSubPartitionValues: \"%s\", businessObjectDataVersion: %d", namespaceCode, businessObjectDefinitionName,
            businessObjectFormatUsage, businessObjectFormatFileType, businessObjectFormatVersion, partitionValue,
            CollectionUtils.isEmpty(subPartitionValues) ? "" : org.apache.commons.lang3.StringUtils.join(subPartitionValues, ","), businessObjectDataVersion);
    }

    /**
     * Creates a business object data definition create request.
     *
     * @param namespaceCode the namespace code
     * @param businessObjectDefinitionName the business object definition name
     * @param dataProviderName the data provider name
     * @param businessObjectDefinitionDescription the description of the business object definition
     *
     * @return the newly created business object definition create request
     */
    protected BusinessObjectDefinitionCreateRequest createBusinessObjectDefinitionCreateRequest(String namespaceCode, String businessObjectDefinitionName,
        String dataProviderName, String businessObjectDefinitionDescription)
    {
        return createBusinessObjectDefinitionCreateRequest(namespaceCode, businessObjectDefinitionName, dataProviderName, businessObjectDefinitionDescription,
            null);
    }

    /**
     * Creates a business object data definition create request.
     *
     * @param namespaceCode the namespace code
     * @param businessObjectDefinitionName the business object definition name
     * @param dataProviderName the data provider name
     * @param businessObjectDefinitionDescription the description of the business object definition
     *
     * @return the newly created business object definition create request
     */
    protected BusinessObjectDefinitionCreateRequest createBusinessObjectDefinitionCreateRequest(String namespaceCode, String businessObjectDefinitionName,
        String dataProviderName, String businessObjectDefinitionDescription, List<Attribute> attributes)
    {
        BusinessObjectDefinitionCreateRequest request = new BusinessObjectDefinitionCreateRequest();
        request.setNamespace(namespaceCode);
        request.setBusinessObjectDefinitionName(businessObjectDefinitionName);
        request.setDataProviderName(dataProviderName);
        request.setDescription(businessObjectDefinitionDescription);
        request.setAttributes(attributes);
        return request;
    }

    /**
     * Validates business object data key against specified arguments.
     *
     * @param expectedNamespace the expected namespace
     * @param expectedBusinessObjectDefinitionName the expected business object definition name
     * @param expectedBusinessObjectFormatUsage the expected business object format usage
     * @param expectedBusinessObjectFormatFileType the expected business object format file type
     * @param expectedBusinessObjectFormatVersion the expected business object format version
     * @param expectedBusinessObjectDataPartitionValue the expected partition value for this business object data
     * @param expectedBusinessObjectDataVersion the expected business object data version
     * @param actualBusinessObjectDataKey the business object data availability object instance to be validated
     */
    protected void validateBusinessObjectDataKey(String expectedNamespace, String expectedBusinessObjectDefinitionName,
        String expectedBusinessObjectFormatUsage, String expectedBusinessObjectFormatFileType, Integer expectedBusinessObjectFormatVersion,
        String expectedBusinessObjectDataPartitionValue, List<String> expectedBusinessObjectDataSubPartitionValues, Integer expectedBusinessObjectDataVersion,
        BusinessObjectDataKey actualBusinessObjectDataKey)
    {
        assertNotNull(actualBusinessObjectDataKey);
        assertEquals(expectedNamespace, actualBusinessObjectDataKey.getNamespace());
        assertEquals(expectedBusinessObjectDefinitionName, actualBusinessObjectDataKey.getBusinessObjectDefinitionName());
        assertEquals(expectedBusinessObjectFormatUsage, actualBusinessObjectDataKey.getBusinessObjectFormatUsage());
        assertEquals(expectedBusinessObjectFormatFileType, actualBusinessObjectDataKey.getBusinessObjectFormatFileType());
        assertEquals(expectedBusinessObjectFormatVersion, actualBusinessObjectDataKey.getBusinessObjectFormatVersion());
        assertEquals(expectedBusinessObjectDataPartitionValue, actualBusinessObjectDataKey.getPartitionValue());
        assertEquals(expectedBusinessObjectDataSubPartitionValues, actualBusinessObjectDataKey.getSubPartitionValues());
        assertEquals(expectedBusinessObjectDataVersion, actualBusinessObjectDataKey.getBusinessObjectDataVersion());
    }

    /**
     * Validates business object data against specified arguments and expected (hard coded) test values.
     *
     * @param request the business object data create request
     * @param expectedBusinessObjectDataVersion the expected business object data version
     * @param expectedLatestVersion the expected business
     * @param actualBusinessObjectData the business object data availability object instance to be validated
     */
    protected void validateBusinessObjectData(BusinessObjectDataCreateRequest request, Integer expectedBusinessObjectDataVersion, Boolean expectedLatestVersion,
        BusinessObjectData actualBusinessObjectData)
    {
        BusinessObjectFormatEntity businessObjectFormatEntity = dmDao.getBusinessObjectFormatByAltKey(
            new BusinessObjectFormatKey(org.apache.commons.lang3.StringUtils.isNotBlank(request.getNamespace()) ? request.getNamespace() : NAMESPACE_CD,
                request.getBusinessObjectDefinitionName(), request.getBusinessObjectFormatUsage(), request.getBusinessObjectFormatFileType(),
                request.getBusinessObjectFormatVersion()));

        List<String> expectedSubPartitionValues =
            CollectionUtils.isEmpty(request.getSubPartitionValues()) ? new ArrayList<String>() : request.getSubPartitionValues();

        String expectedStatusCode =
            org.apache.commons.lang3.StringUtils.isNotBlank(request.getStatus()) ? request.getStatus() : BusinessObjectDataStatusEntity.VALID;

        StorageUnitCreateRequest storageUnitCreateRequest = request.getStorageUnits().get(0);

        StorageEntity storageEntity = dmDao.getStorageByName(storageUnitCreateRequest.getStorageName());

        String expectedStorageDirectoryPath =
            storageUnitCreateRequest.getStorageDirectory() != null ? storageUnitCreateRequest.getStorageDirectory().getDirectoryPath() : null;

        List<StorageFile> expectedStorageFiles =
            CollectionUtils.isEmpty(storageUnitCreateRequest.getStorageFiles()) ? null : storageUnitCreateRequest.getStorageFiles();

        List<Attribute> expectedAttributes = CollectionUtils.isEmpty(request.getAttributes()) ? new ArrayList<Attribute>() : request.getAttributes();

        validateBusinessObjectData(businessObjectFormatEntity, request.getPartitionValue(), expectedSubPartitionValues, expectedBusinessObjectDataVersion,
            expectedLatestVersion, expectedStatusCode, storageEntity.getName(), expectedStorageDirectoryPath, expectedStorageFiles, expectedAttributes,
            actualBusinessObjectData);
    }

    /**
     * Validates business object data against specified arguments and expected (hard coded) test values.
     *
     * @param businessObjectFormatEntity the business object format entity that this business object data belongs to
     * @param expectedBusinessObjectDataPartitionValue the expected partition value for this business object data
     * @param expectedBusinessObjectDataSubPartitionValues the expected subpartition values for this business object data
     * @param expectedBusinessObjectDataVersion the expected business object data version
     * @param expectedLatestVersion the expected business
     * @param expectedStatusCode the expected business object data status code
     * @param expectedStorageName the expected storage name
     * @param expectedStorageDirectoryPath the expected storage directory path
     * @param expectedStorageFiles the expected storage files
     * @param expectedAttributes the expected attributes
     * @param actualBusinessObjectData the business object data availability object instance to be validated
     */
    protected void validateBusinessObjectData(BusinessObjectFormatEntity businessObjectFormatEntity, String expectedBusinessObjectDataPartitionValue,
        List<String> expectedBusinessObjectDataSubPartitionValues, Integer expectedBusinessObjectDataVersion, Boolean expectedLatestVersion,
        String expectedStatusCode, String expectedStorageName, String expectedStorageDirectoryPath, List<StorageFile> expectedStorageFiles,
        List<Attribute> expectedAttributes, BusinessObjectData actualBusinessObjectData)
    {
        validateBusinessObjectData(null, businessObjectFormatEntity.getBusinessObjectDefinition().getNamespace().getCode(),
            businessObjectFormatEntity.getBusinessObjectDefinition().getName(), businessObjectFormatEntity.getUsage(),
            businessObjectFormatEntity.getFileType().getCode(), businessObjectFormatEntity.getBusinessObjectFormatVersion(),
            expectedBusinessObjectDataPartitionValue, expectedBusinessObjectDataSubPartitionValues, expectedBusinessObjectDataVersion, expectedLatestVersion,
            expectedStatusCode, expectedStorageName, expectedStorageDirectoryPath, expectedStorageFiles, expectedAttributes, actualBusinessObjectData);
    }

    /**
     * Validates business object data against specified arguments and expected (hard coded) test values.
     *
     * @param expectedBusinessObjectDataId the expected business object data ID
     * @param expectedNamespace the expected namespace
     * @param expectedBusinessObjectDefinitionName the expected business object definition name
     * @param expectedBusinessObjectFormatUsage the expected business object format usage
     * @param expectedBusinessObjectFormatFileType the expected business object format file type
     * @param expectedBusinessObjectFormatVersion the expected business object format version
     * @param expectedBusinessObjectDataPartitionValue the expected partition value for this business object data
     * @param expectedBusinessObjectDataVersion the expected business object data version
     * @param expectedLatestVersion the expected business
     * @param expectedStatusCode the expected business object data status code
     * @param expectedStorageName the expected storage name
     * @param expectedStorageDirectoryPath the expected storage directory path
     * @param expectedStorageFiles the expected storage files
     * @param expectedAttributes the expected attributes
     * @param actualBusinessObjectData the business object data availability object instance to be validated
     */
    protected void validateBusinessObjectData(Integer expectedBusinessObjectDataId, String expectedNamespace, String expectedBusinessObjectDefinitionName,
        String expectedBusinessObjectFormatUsage, String expectedBusinessObjectFormatFileType, Integer expectedBusinessObjectFormatVersion,
        String expectedBusinessObjectDataPartitionValue, List<String> expectedBusinessObjectDataSubPartitionValues, Integer expectedBusinessObjectDataVersion,
        Boolean expectedLatestVersion, String expectedStatusCode, String expectedStorageName, String expectedStorageDirectoryPath,
        List<StorageFile> expectedStorageFiles, List<Attribute> expectedAttributes, BusinessObjectData actualBusinessObjectData)
    {
        validateBusinessObjectData(expectedBusinessObjectDataId, expectedNamespace, expectedBusinessObjectDefinitionName, expectedBusinessObjectFormatUsage,
            expectedBusinessObjectFormatFileType, expectedBusinessObjectFormatVersion, expectedBusinessObjectDataPartitionValue,
            expectedBusinessObjectDataSubPartitionValues, expectedBusinessObjectDataVersion, expectedLatestVersion, expectedStatusCode,
            actualBusinessObjectData);

        // We expected test business object data to contain a single storage unit.
        assertEquals(1, actualBusinessObjectData.getStorageUnits().size());
        StorageUnit actualStorageUnit = actualBusinessObjectData.getStorageUnits().get(0);

        assertEquals(expectedStorageName, actualStorageUnit.getStorage().getName());
        assertEquals(expectedStorageDirectoryPath,
            actualStorageUnit.getStorageDirectory() != null ? actualStorageUnit.getStorageDirectory().getDirectoryPath() : null);
        assertEqualsIgnoreOrder("storage files", expectedStorageFiles, actualStorageUnit.getStorageFiles());

        assertEquals(expectedAttributes, actualBusinessObjectData.getAttributes());
    }

    /**
     * Validates business object data against specified arguments.
     *
     * @param expectedBusinessObjectDataId the expected business object data ID
     * @param expectedNamespace the expected namespace
     * @param expectedBusinessObjectDefinitionName the expected business object definition name
     * @param expectedBusinessObjectFormatUsage the expected business object format usage
     * @param expectedBusinessObjectFormatFileType the expected business object format file type
     * @param expectedBusinessObjectFormatVersion the expected business object format version
     * @param expectedBusinessObjectDataPartitionValue the expected partition value for this business object data
     * @param expectedBusinessObjectDataSubPartitionValues the expected subpartition values for this business object data
     * @param expectedBusinessObjectDataVersion the expected business object data version
     * @param expectedLatestVersion the expected business
     * @param expectedStatusCode the expected business object data status code
     * @param actualBusinessObjectData the business object data availability object instance to be validated
     */
    protected void validateBusinessObjectData(Integer expectedBusinessObjectDataId, String expectedNamespace, String expectedBusinessObjectDefinitionName,
        String expectedBusinessObjectFormatUsage, String expectedBusinessObjectFormatFileType, Integer expectedBusinessObjectFormatVersion,
        String expectedBusinessObjectDataPartitionValue, List<String> expectedBusinessObjectDataSubPartitionValues, Integer expectedBusinessObjectDataVersion,
        Boolean expectedLatestVersion, String expectedStatusCode, BusinessObjectData actualBusinessObjectData)
    {
        assertNotNull(actualBusinessObjectData);

        if (expectedBusinessObjectDataId != null)
        {
            assertEquals(expectedBusinessObjectDataId, Integer.valueOf(actualBusinessObjectData.getId()));
        }

        assertEquals(expectedNamespace, actualBusinessObjectData.getNamespace());
        assertEquals(expectedBusinessObjectDefinitionName, actualBusinessObjectData.getBusinessObjectDefinitionName());
        assertEquals(expectedBusinessObjectFormatUsage, actualBusinessObjectData.getBusinessObjectFormatUsage());
        assertEquals(expectedBusinessObjectFormatFileType, actualBusinessObjectData.getBusinessObjectFormatFileType());
        assertEquals(expectedBusinessObjectFormatVersion, Integer.valueOf(actualBusinessObjectData.getBusinessObjectFormatVersion()));
        assertEquals(expectedBusinessObjectDataPartitionValue, actualBusinessObjectData.getPartitionValue());
        assertEquals(expectedBusinessObjectDataSubPartitionValues, actualBusinessObjectData.getSubPartitionValues());
        assertEquals(expectedBusinessObjectDataVersion, Integer.valueOf(actualBusinessObjectData.getVersion()));
        assertEquals(expectedLatestVersion, actualBusinessObjectData.isLatestVersion());
        assertEquals(expectedStatusCode, actualBusinessObjectData.getStatus());
    }

    /**
     * Creates specified list of files in the local temporary directory and uploads them to the test S3 bucket.
     *
     * @param s3keyPrefix the destination S3 key prefix
     * @param localFilePaths the list of local files that might include sub-directories
     *
     * @throws Exception
     */
    protected void prepareTestS3Files(String s3keyPrefix, List<String> localFilePaths) throws Exception
    {
        prepareTestS3Files(s3keyPrefix, localFilePaths, new ArrayList<String>());
    }

    /**
     * Creates specified list of files in the local temporary directory and uploads them to the test S3 bucket. This method also creates 0 byte S3 directory
     * markers relative to the s3 key prefix.
     *
     * @param s3KeyPrefix the destination S3 key prefix
     * @param localFilePaths the list of local files that might include sub-directories
     * @param directoryPaths the list of directory paths to be created in S3 relative to the S3 key prefix
     *
     * @throws Exception
     */
    protected void prepareTestS3Files(String s3KeyPrefix, List<String> localFilePaths, List<String> directoryPaths) throws Exception
    {
        prepareTestS3Files(null, s3KeyPrefix, localFilePaths, directoryPaths);
    }

    /**
     * Creates specified list of files in the local temporary directory and uploads them to the test S3 bucket. This method also creates 0 byte S3 directory
     * markers relative to the s3 key prefix.
     *
     * @param bucketName the bucket name in S3 to place the files.
     * @param s3KeyPrefix the destination S3 key prefix
     * @param localFilePaths the list of local files that might include sub-directories
     * @param directoryPaths the list of directory paths to be created in S3 relative to the S3 key prefix
     *
     * @throws Exception
     */
    protected void prepareTestS3Files(String bucketName, String s3KeyPrefix, List<String> localFilePaths, List<String> directoryPaths) throws Exception
    {
        // Create local test files.
        for (String file : localFilePaths)
        {
            createLocalFile(localTempPath.toString(), file, FILE_SIZE_1_KB);
        }

        // Upload test file to S3.
        S3FileTransferRequestParamsDto s3FileTransferRequestParamsDto = getTestS3FileTransferRequestParamsDto();
        if (bucketName != null)
        {
            s3FileTransferRequestParamsDto.setS3BucketName(bucketName);
        }
        s3FileTransferRequestParamsDto.setS3KeyPrefix(s3KeyPrefix);
        s3FileTransferRequestParamsDto.setLocalPath(localTempPath.toString());
        s3FileTransferRequestParamsDto.setRecursive(true);
        S3FileTransferResultsDto results = s3Service.uploadDirectory(s3FileTransferRequestParamsDto);

        // Validate the transfer result.
        assertEquals(Long.valueOf(localFilePaths.size()), results.getTotalFilesTransferred());

        // Create 0 byte S3 directory markers.
        for (String directoryPath : directoryPaths)
        {
            // Create 0 byte directory marker.
            s3FileTransferRequestParamsDto.setS3KeyPrefix(s3KeyPrefix + "/" + directoryPath);
            s3Service.createDirectory(s3FileTransferRequestParamsDto);
        }

        // Validate the uploaded S3 files and created directory markers, if any.
        s3FileTransferRequestParamsDto.setS3KeyPrefix(s3KeyPrefix);
        List<StorageFile> actualS3Files = s3Service.listDirectory(s3FileTransferRequestParamsDto);
        assertEquals(localFilePaths.size() + directoryPaths.size(), actualS3Files.size());
    }

    /**
     * Builds and returns a list of test storage file object instances.
     *
     * @param s3KeyPrefix the S3 key prefix
     * @param relativeFilePaths the list of relative file paths that might include sub-directories
     *
     * @return the newly created list of storage files
     */
    protected List<StorageFile> getTestStorageFiles(String s3KeyPrefix, List<String> relativeFilePaths)
    {
        return getTestStorageFiles(s3KeyPrefix, relativeFilePaths, true);
    }

    /**
     * Builds and returns a list of test storage file object instances.
     *
     * @param s3KeyPrefix the S3 key prefix
     * @param relativeFilePaths the list of relative file paths that might include sub-directories,
     * @param setRowCount specifies if some storage files should get row count attribute set to a hard coded test value
     *
     * @return the newly created list of storage files
     */
    protected List<StorageFile> getTestStorageFiles(String s3KeyPrefix, List<String> relativeFilePaths, boolean setRowCount)
    {
        // Build a list of storage files.
        List<StorageFile> storageFiles = new ArrayList<>();

        for (String file : relativeFilePaths)
        {
            StorageFile storageFile = new StorageFile();
            storageFiles.add(storageFile);
            storageFile.setFilePath(s3KeyPrefix + "/" + file.replaceAll("\\\\", "/"));
            storageFile.setFileSizeBytes(FILE_SIZE_1_KB);

            if (setRowCount)
            {
                // Row count is an optional field, so let's not set it for one of the storage files - this is required for code coverage.
                storageFile.setRowCount(file.equals(LOCAL_FILES.get(0)) ? null : ROW_COUNT_1000);
            }
        }

        return storageFiles;
    }

    /**
     * Returns Hive DDL that is expected to be produced by a unit test based on specified parameters and hard-coded test values.
     *
     * @return the Hive DDL
     */
    protected String getBusinessObjectFormatExpectedDdl()
    {
        return getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
            FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, null, null, false, true, true);
    }

    /**
     * Returns Hive DDL that is expected to be produced by a unit test based on specified parameters and hard-coded test values.
     *
     * @return the Hive DDL
     */
    protected String getExpectedDdl()
    {
        return getExpectedDdl(PARTITION_COLUMNS.length, FIRST_COLUMN_NAME, FIRST_COLUMN_DATA_TYPE, ROW_FORMAT, Hive13DdlGenerator.TEXT_HIVE_FILE_FORMAT,
            FileTypeEntity.TXT_FILE_TYPE, BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION, STORAGE_1_AVAILABLE_PARTITION_VALUES, SUBPARTITION_VALUES,
            false, true, true);
    }

    /**
     * Returns Hive DDL that is expected to be produced by a unit test based on specified parameters and hard-coded test values.
     *
     * @return the Hive DDL
     */
    protected String getExpectedDdl(int partitionLevels, String firstColumnName, String firstColumnDataType, String hiveRowFormat, String hiveFileFormat,
        String businessObjectFormatFileType, int partitionColumnPosition, List<String> partitionValues, List<String> subPartitionValues,
        boolean replaceUnderscoresWithHyphens, boolean isDropStatementIncluded, boolean isIfNotExistsOptionIncluded)
    {
        return getExpectedDdl(partitionLevels, firstColumnName, firstColumnDataType, hiveRowFormat, hiveFileFormat, businessObjectFormatFileType,
            partitionColumnPosition, partitionValues, subPartitionValues, replaceUnderscoresWithHyphens, isDropStatementIncluded, isIfNotExistsOptionIncluded,
            NO_INCLUDE_DROP_PARTITIONS);
    }

    /**
     * Returns Hive DDL that is expected to be produced by a unit test based on specified parameters and hard-coded test values.
     *
     * @param partitionLevels the number of partition levels
     * @param firstColumnName the name of the first schema column
     * @param firstColumnDataType the data type of the first schema column
     * @param hiveRowFormat the Hive row format
     * @param hiveFileFormat the Hive file format
     * @param businessObjectFormatFileType the business object format file type
     * @param partitionColumnPosition the position of the partition column
     * @param partitionValues the list of partition values
     * @param subPartitionValues the list of subpartition values
     * @param replaceUnderscoresWithHyphens specifies if we need to replace underscores with hyphens in subpartition key values when building subpartition
     * location path
     * @param isDropStatementIncluded specifies if we need to check for drop table statement
     * @param isDropPartitionsStatementsIncluded
     *
     * @return the Hive DDL
     */
    protected String getExpectedDdl(int partitionLevels, String firstColumnName, String firstColumnDataType, String hiveRowFormat, String hiveFileFormat,
        String businessObjectFormatFileType, int partitionColumnPosition, List<String> partitionValues, List<String> subPartitionValues,
        boolean replaceUnderscoresWithHyphens, boolean isDropStatementIncluded, boolean isIfNotExistsOptionIncluded, boolean isDropPartitionsStatementsIncluded)
    {
        StringBuilder sb = new StringBuilder();

        if (isDropStatementIncluded)
        {
            sb.append("DROP TABLE IF EXISTS `[Table Name]`;\n\n");
        }
        sb.append("CREATE EXTERNAL TABLE [If Not Exists]`[Table Name]` (\n");
        sb.append(String.format("    `%s` %s,\n", firstColumnName, firstColumnDataType));
        sb.append("    `COLUMN002` SMALLINT COMMENT 'This is \\'COLUMN002\\' column. ");
        sb.append("Here are \\'single\\' and \"double\" quotes along with a backslash \\.',\n");
        sb.append("    `COLUMN003` INT,\n");
        sb.append("    `COLUMN004` BIGINT,\n");
        sb.append("    `COLUMN005` FLOAT,\n");
        sb.append("    `COLUMN006` DOUBLE,\n");
        sb.append("    `COLUMN007` DECIMAL,\n");
        sb.append("    `COLUMN008` DECIMAL(p,s),\n");
        sb.append("    `COLUMN009` DECIMAL,\n");
        sb.append("    `COLUMN010` DECIMAL(p),\n");
        sb.append("    `COLUMN011` DECIMAL(p,s),\n");
        sb.append("    `COLUMN012` TIMESTAMP,\n");
        sb.append("    `COLUMN013` DATE,\n");
        sb.append("    `COLUMN014` STRING,\n");
        sb.append("    `COLUMN015` VARCHAR(n),\n");
        sb.append("    `COLUMN016` VARCHAR(n),\n");
        sb.append("    `COLUMN017` CHAR(n),\n");
        sb.append("    `COLUMN018` BOOLEAN,\n");
        sb.append("    `COLUMN019` BINARY)\n");

        if (partitionLevels > 0)
        {
            if (partitionLevels > 1)
            {
                // Multiple level partitioning.
                sb.append("PARTITIONED BY (`PRTN_CLMN001` DATE, `PRTN_CLMN002` STRING, `PRTN_CLMN003` INT, `PRTN_CLMN004` DECIMAL, " +
                    "`PRTN_CLMN005` BOOLEAN, `PRTN_CLMN006` DECIMAL, `PRTN_CLMN007` DECIMAL)\n");
            }
            else
            {
                // Single level partitioning.
                sb.append("PARTITIONED BY (`PRTN_CLMN001` DATE)\n");
            }
        }

        sb.append("[Row Format]\n");
        sb.append(String.format("STORED AS [Hive File Format]%s\n", partitionLevels > 0 ? ";" : ""));

        if (partitionLevels > 0)
        {
            // Add partitions if we have a non-empty list of partition values.
            if (!CollectionUtils.isEmpty(partitionValues))
            {
                // Add drop partition statements.
                if (isDropPartitionsStatementsIncluded)
                {
                    sb.append("\n");

                    for (String partitionValue : partitionValues)
                    {
                        sb.append(String
                            .format("ALTER TABLE `[Table Name]` DROP IF EXISTS PARTITION (`PRTN_CLMN00%d`='%s');\n", partitionColumnPosition, partitionValue));
                    }
                }

                sb.append("\n");

                for (String partitionValue : partitionValues)
                {
                    if (partitionLevels > 1)
                    {
                        // Adjust expected partition values based on the partition column position.
                        String testPrimaryPartitionValue =
                            partitionColumnPosition == BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION ? partitionValue : PARTITION_VALUE;
                        List<String> testSubPartitionValues = new ArrayList<>(subPartitionValues);
                        if (partitionColumnPosition > BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION)
                        {
                            testSubPartitionValues.set(partitionColumnPosition - 2, partitionValue);
                        }

                        // Multiple level partitioning.
                        if (partitionLevels == SUBPARTITION_VALUES.size() + 1)
                        {
                            // No auto-discovery.
                            sb.append(String.format("ALTER TABLE `[Table Name]` ADD [If Not Exists]PARTITION (`PRTN_CLMN001`='%s', " +
                                "`PRTN_CLMN002`='%s', `PRTN_CLMN003`='%s', `PRTN_CLMN004`='%s', `PRTN_CLMN005`='%s') " +
                                "LOCATION 's3n://%s/ut-namespace[Random Suffix]/ut-dataprovider[Random Suffix]/ut-usage[Random Suffix]" +
                                "/[Format File Type]/ut-bodef[Random Suffix]/frmt-v[Format Version]/data-v[Data Version]/prtn-clmn001=%s/" +
                                "prtn-clmn002=%s/prtn-clmn003=%s/prtn-clmn004=%s/prtn-clmn005=%s';\n", testPrimaryPartitionValue, testSubPartitionValues.get(0),
                                testSubPartitionValues.get(1), testSubPartitionValues.get(2), testSubPartitionValues.get(3),
                                getExpectedS3BucketName(partitionValue), testPrimaryPartitionValue, testSubPartitionValues.get(0),
                                testSubPartitionValues.get(1), testSubPartitionValues.get(2), testSubPartitionValues.get(3)));
                        }
                        else
                        {
                            // Auto-discovery test template.
                            for (String binaryString : Arrays.asList("00", "01", "10", "11"))
                            {
                                sb.append(String.format("ALTER TABLE `[Table Name]` ADD [If Not Exists]PARTITION (`PRTN_CLMN001`='%s', " +
                                    "`PRTN_CLMN002`='%s', `PRTN_CLMN003`='%s', `PRTN_CLMN004`='%s', `PRTN_CLMN005`='%s', `PRTN_CLMN006`='%s', " +
                                    "`PRTN_CLMN007`='%s') " +
                                    "LOCATION 's3n://%s/ut-namespace[Random Suffix]/ut-dataprovider[Random Suffix]/ut-usage[Random " +
                                    "Suffix]" +
                                    "/[Format File Type]/ut-bodef[Random Suffix]/frmt-v[Format Version]/data-v[Data Version]/prtn-clmn001=%s/" +
                                    "prtn-clmn002=%s/prtn-clmn003=%s/prtn-clmn004=%s/prtn-clmn005=%s/" +
                                    (replaceUnderscoresWithHyphens ? "prtn-clmn006" : "prtn_clmn006") + "=%s/" +
                                    (replaceUnderscoresWithHyphens ? "prtn-clmn007" : "prtn_clmn007") + "=%s';\n", testPrimaryPartitionValue,
                                    testSubPartitionValues.get(0), testSubPartitionValues.get(1), testSubPartitionValues.get(2), testSubPartitionValues.get(3),
                                    binaryString.substring(0, 1), binaryString.substring(1, 2), getExpectedS3BucketName(partitionValue),
                                    testPrimaryPartitionValue, testSubPartitionValues.get(0), testSubPartitionValues.get(1), testSubPartitionValues.get(2),
                                    testSubPartitionValues.get(3), binaryString.substring(0, 1), binaryString.substring(1, 2)));
                            }
                        }
                    }
                    else
                    {
                        // Single level partitioning.
                        sb.append(String.format("ALTER TABLE `[Table Name]` ADD [If Not Exists]PARTITION (`PRTN_CLMN001`='%s') " +
                            "LOCATION 's3n://%s/ut-namespace[Random Suffix]/ut-dataprovider[Random Suffix]/ut-usage[Random Suffix]" +
                            "/[Format File Type]/ut-bodef[Random Suffix]/frmt-v[Format Version]/data-v[Data Version]/prtn-clmn001=%s';\n", partitionValue,
                            getExpectedS3BucketName(partitionValue), partitionValue));
                    }
                }
            }
        }
        else if (!CollectionUtils.isEmpty(partitionValues))
        {
            // Add a location statement since the table is not partitioned and we have a non-empty list of partition values.
            sb.append(String.format("LOCATION 's3n://%s/ut-namespace[Random Suffix]/ut-dataprovider[Random Suffix]/ut-usage[Random Suffix]" +
                "/txt/ut-bodef[Random Suffix]/frmt-v[Format Version]/data-v[Data Version]/partition=none';",
                getExpectedS3BucketName(Hive13DdlGenerator.NO_PARTITIONING_PARTITION_VALUE)));
        }
        else
        {
            // Add a location statement for a non-partitioned table for the business object format dll unit tests.
            sb.append("LOCATION '${non-partitioned.table.location}';");
        }

        String ddlTemplate = sb.toString().trim();
        Pattern pattern = Pattern.compile("\\[(.+?)\\]");
        Matcher matcher = pattern.matcher(ddlTemplate);
        HashMap<String, String> replacements = new HashMap<>();

        // Populate the replacements map.
        replacements.put("Table Name", TABLE_NAME);
        replacements.put("Random Suffix", RANDOM_SUFFIX);
        replacements.put("Format Version", String.valueOf(FORMAT_VERSION));
        replacements.put("Data Version", String.valueOf(DATA_VERSION));
        replacements.put("Row Format", hiveRowFormat);
        replacements.put("Hive File Format", hiveFileFormat);
        replacements.put("Format File Type", businessObjectFormatFileType.toLowerCase());
        replacements.put("If Not Exists", isIfNotExistsOptionIncluded ? "IF NOT EXISTS " : "");

        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (matcher.find())
        {
            String replacement = replacements.get(matcher.group(1));
            builder.append(ddlTemplate.substring(i, matcher.start()));
            if (replacement == null)
            {
                builder.append(matcher.group(0));
            }
            else
            {
                builder.append(replacement);
            }
            i = matcher.end();
        }

        builder.append(ddlTemplate.substring(i, ddlTemplate.length()));

        return builder.toString();
    }

    protected String getExpectedS3BucketName(String partitionValue)
    {
        if (STORAGE_1_AVAILABLE_PARTITION_VALUES.contains(partitionValue) || Hive13DdlGenerator.NO_PARTITIONING_PARTITION_VALUE.equals(partitionValue))
        {
            return S3_BUCKET_NAME;
        }
        else
        {
            return S3_BUCKET_NAME_2;
        }
    }

    /**
     * Validates a list of StorageFiles against the expected values.
     *
     * @param expectedStorageFiles the list of expected StorageFiles
     * @param actualStorageFiles the list of actual StorageFiles to be validated
     */
    protected void validateStorageFiles(List<StorageFile> expectedStorageFiles, List<StorageFile> actualStorageFiles)
    {
        assertEquals(expectedStorageFiles.size(), actualStorageFiles.size());
        for (int i = 0; i < expectedStorageFiles.size(); i++)
        {
            StorageFile expectedStorageFile = expectedStorageFiles.get(i);
            StorageFile actualStorageFile = actualStorageFiles.get(i);
            assertEquals(expectedStorageFile.getFilePath(), actualStorageFile.getFilePath());
            assertEquals(expectedStorageFile.getFileSizeBytes(), actualStorageFile.getFileSizeBytes());
            assertEquals(expectedStorageFile.getRowCount(), actualStorageFile.getRowCount());
        }
    }

    /**
     * Validates a list of Attributes against the expected values.
     *
     * @param expectedAttributes the list of expected Attributes
     * @param actualAttributes the list of actual Attributes to be validated
     */
    protected void validateAttributes(List<Attribute> expectedAttributes, List<Attribute> actualAttributes)
    {
        assertEquals(expectedAttributes.size(), actualAttributes.size());
        for (int i = 0; i < expectedAttributes.size(); i++)
        {
            Attribute expectedAttribute = expectedAttributes.get(i);
            Attribute actualAttribute = actualAttributes.get(i);
            assertEquals(expectedAttribute.getName(), actualAttribute.getName());
            assertEquals(expectedAttribute.getValue(), actualAttribute.getValue());
        }
    }

    /**
     * Creates a partition key group key.
     *
     * @param partitionKeyGroupName the partition key group name
     *
     * @return the created partition key group key
     */
    protected PartitionKeyGroupKey createPartitionKeyGroupKey(String partitionKeyGroupName)
    {
        PartitionKeyGroupKey partitionKeyGroupKey = new PartitionKeyGroupKey();
        partitionKeyGroupKey.setPartitionKeyGroupName(partitionKeyGroupName);
        return partitionKeyGroupKey;
    }

    /**
     * Converts a list of Parameters to a list of String values.
     *
     * @return the list of string values representing parameter elements.
     */
    protected List<String> parametersToStringList(List<Parameter> parameters)
    {
        List<String> list = new ArrayList<>();

        for (Parameter parameter : parameters)
        {
            list.add(String.format("\"%s\"=\"%s\"", parameter.getName(), parameter.getValue()));
        }

        return list;
    }

    /**
     * Creates a business object data attribute create request.
     *
     * @return the newly created business object data attribute create request
     */
    protected BusinessObjectDataAttributeCreateRequest createBusinessObjectDataAttributeCreateRequest(String namespaceCode, String businessObjectDefinitionName,
        String businessObjectFormatUsage, String businessObjectFormatFileType, Integer businessObjectFormatVersion, String businessObjectDataPartitionValue,
        List<String> businessObjectDataSubPartitionValues, Integer businessObjectDataVersion, String businessObjectDataAttributeName,
        String businessObjectDataAttributeValue)
    {
        BusinessObjectDataAttributeCreateRequest request = new BusinessObjectDataAttributeCreateRequest();

        request.setBusinessObjectDataAttributeKey(
            new BusinessObjectDataAttributeKey(namespaceCode, businessObjectDefinitionName, businessObjectFormatUsage, businessObjectFormatFileType,
                businessObjectFormatVersion, businessObjectDataPartitionValue, businessObjectDataSubPartitionValues, businessObjectDataVersion,
                businessObjectDataAttributeName));
        request.setBusinessObjectDataAttributeValue(businessObjectDataAttributeValue);

        return request;
    }

    /**
     * Creates a business object data attribute update request.
     *
     * @return the newly created business object data attribute update request
     */
    protected BusinessObjectDataAttributeUpdateRequest createBusinessObjectDataAttributeUpdateRequest(String businessObjectDataAttributeValue)
    {
        BusinessObjectDataAttributeUpdateRequest request = new BusinessObjectDataAttributeUpdateRequest();

        request.setBusinessObjectDataAttributeValue(businessObjectDataAttributeValue);

        return request;
    }

    /**
     * Validates business object data attribute contents against specified arguments.
     *
     * @param businessObjectDataAttributeId the expected business object data attribute ID
     * @param expectedNamespace the expected namespace
     * @param expectedBusinessObjectDefinitionName the expected business object definition name
     * @param expectedBusinessObjectFormatUsage the expected business object format usage
     * @param expectedBusinessObjectFormatFileType the expected business object format file type
     * @param expectedBusinessObjectFormatVersion the expected business object format version
     * @param expectedBusinessObjectDataPartitionValue the expected partition value
     * @param expectedBusinessObjectDataSubPartitionValues the expected subpartition values
     * @param expectedBusinessObjectDataVersion the expected business object data version
     * @param expectedBusinessObjectDataAttributeName the expected business object data attribute name
     * @param expectedBusinessObjectDataAttributeValue the expected business object data attribute value
     * @param actualBusinessObjectDataAttribute the business object data attribute object instance to be validated
     */
    protected void validateBusinessObjectDataAttribute(Integer businessObjectDataAttributeId, String expectedNamespace,
        String expectedBusinessObjectDefinitionName, String expectedBusinessObjectFormatUsage, String expectedBusinessObjectFormatFileType,
        Integer expectedBusinessObjectFormatVersion, String expectedBusinessObjectDataPartitionValue, List<String> expectedBusinessObjectDataSubPartitionValues,
        Integer expectedBusinessObjectDataVersion, String expectedBusinessObjectDataAttributeName, String expectedBusinessObjectDataAttributeValue,
        BusinessObjectDataAttribute actualBusinessObjectDataAttribute)
    {
        assertNotNull(actualBusinessObjectDataAttribute);
        if (businessObjectDataAttributeId != null)
        {
            assertEquals(businessObjectDataAttributeId, Integer.valueOf(actualBusinessObjectDataAttribute.getId()));
        }
        validateBusinessObjectDataAttributeKey(expectedNamespace, expectedBusinessObjectDefinitionName, expectedBusinessObjectFormatUsage,
            expectedBusinessObjectFormatFileType, expectedBusinessObjectFormatVersion, expectedBusinessObjectDataPartitionValue,
            expectedBusinessObjectDataSubPartitionValues, expectedBusinessObjectDataVersion, expectedBusinessObjectDataAttributeName,
            actualBusinessObjectDataAttribute.getBusinessObjectDataAttributeKey());
        assertEquals(expectedBusinessObjectDataAttributeValue, actualBusinessObjectDataAttribute.getBusinessObjectDataAttributeValue());
    }

    /**
     * Validates business object data attribute key against specified arguments.
     *
     * @param expectedNamespace the expected namespace
     * @param expectedBusinessObjectDefinitionName the expected business object definition name
     * @param expectedBusinessObjectFormatUsage the expected business object format usage
     * @param expectedBusinessObjectFormatFileType the expected business object format file type
     * @param expectedBusinessObjectFormatVersion the expected business object format version
     * @param expectedBusinessObjectDataPartitionValue the expected partition value
     * @param expectedBusinessObjectDataSubPartitionValues the expected subpartition values
     * @param expectedBusinessObjectDataVersion the expected business object data version
     * @param expectedBusinessObjectDataAttributeName the expected business object data attribute name
     * @param actualBusinessObjectDataAttributeKey the business object data attribute key object instance to be validated
     */
    protected void validateBusinessObjectDataAttributeKey(String expectedNamespace, String expectedBusinessObjectDefinitionName,
        String expectedBusinessObjectFormatUsage, String expectedBusinessObjectFormatFileType, Integer expectedBusinessObjectFormatVersion,
        String expectedBusinessObjectDataPartitionValue, List<String> expectedBusinessObjectDataSubPartitionValues, Integer expectedBusinessObjectDataVersion,
        String expectedBusinessObjectDataAttributeName, BusinessObjectDataAttributeKey actualBusinessObjectDataAttributeKey)
    {
        assertNotNull(actualBusinessObjectDataAttributeKey);

        assertEquals(expectedNamespace, actualBusinessObjectDataAttributeKey.getNamespace());
        assertEquals(expectedBusinessObjectDefinitionName, actualBusinessObjectDataAttributeKey.getBusinessObjectDefinitionName());
        assertEquals(expectedBusinessObjectFormatUsage, actualBusinessObjectDataAttributeKey.getBusinessObjectFormatUsage());
        assertEquals(expectedBusinessObjectFormatFileType, actualBusinessObjectDataAttributeKey.getBusinessObjectFormatFileType());
        assertEquals(expectedBusinessObjectFormatVersion, actualBusinessObjectDataAttributeKey.getBusinessObjectFormatVersion());
        assertEquals(expectedBusinessObjectDataPartitionValue, actualBusinessObjectDataAttributeKey.getPartitionValue());
        assertEquals(expectedBusinessObjectDataSubPartitionValues, actualBusinessObjectDataAttributeKey.getSubPartitionValues());
        assertEquals(expectedBusinessObjectDataVersion, actualBusinessObjectDataAttributeKey.getBusinessObjectDataVersion());
        assertEquals(expectedBusinessObjectDataAttributeName, actualBusinessObjectDataAttributeKey.getBusinessObjectDataAttributeName());
    }

    /**
     * Create and persist database entities required for testing.
     */
    protected void createDatabaseEntitiesForBusinessObjectDataNotificationRegistrationTesting()
    {
        createDatabaseEntitiesForBusinessObjectDataNotificationRegistrationTesting(NAMESPACE_CD, NOTIFICATION_EVENT_TYPE, NAMESPACE_CD, BOD_NAME,
            FORMAT_FILE_TYPE_CODE, STORAGE_NAME, getTestJobActions());
    }

    /**
     * Create and persist database entities required for testing.
     *
     * @param namespaceCode the namespace code
     * @param notificationEventType the notification event type
     * @param businessObjectDefinitionName the business object definition name
     * @param businessObjectFormatFileType the business object format file type
     * @param storageName the storage name
     */
    protected void createDatabaseEntitiesForBusinessObjectDataNotificationRegistrationTesting(String namespaceCode, String notificationEventType,
        String businessObjectDefinitionNamespace, String businessObjectDefinitionName, String businessObjectFormatFileType, String storageName,
        List<JobAction> jobActions)
    {
        // Create a namespace entity.
        NamespaceEntity namespaceEntity = dmDao.getNamespaceByCd(namespaceCode);
        if (namespaceEntity == null)
        {
            createNamespaceEntity(namespaceCode);
        }

        // Create a notification event entity.
        createNotificationEventTypeEntity(notificationEventType);

        BusinessObjectDefinitionEntity businessObjectDefinitionEntity =
            dmDao.getBusinessObjectDefinitionByKey(new BusinessObjectDefinitionKey(businessObjectDefinitionNamespace, businessObjectDefinitionName));
        if (businessObjectDefinitionEntity == null)
        {
            // Create and persist a non-legacy business object definition entity.
            createBusinessObjectDefinitionEntity(namespaceCode, businessObjectDefinitionName, DATA_PROVIDER_NAME, BOD_DESCRIPTION, false);
        }

        // Create and persist a business object format file type entity.
        createFileTypeEntity(businessObjectFormatFileType);

        // Create an S3 storage entity.
        createStorageEntity(storageName, StoragePlatformEntity.S3);

        if (!CollectionUtils.isEmpty(jobActions))
        {
            for (JobAction jobAction : jobActions)
            {
                createJobDefinitionEntity(jobAction.getNamespace(), jobAction.getJobName(),
                    String.format("Description of \"%s.%s\" job definition.", jobAction.getNamespace(), jobAction.getJobName()),
                    String.format("%s.%s.%s", jobAction.getNamespace(), jobAction.getJobName(), ACTIVITI_ID));
            }
        }
    }

    /**
     * Creates a business object data notification create request.
     *
     * @param namespaceCode the namespace code
     * @param notificationName the notification name
     * @param notificationEventType the notification event type
     * @param businessObjectDefinitionName the business object definition name
     * @param businessObjectFormatUsage the business object usage
     * @param businessObjectFormatFileType the business object format file type
     * @param businessObjectFormatVersion the business object format version
     * @param storageName the storage name
     * @param jobActions the list of job actions
     *
     * @return the newly created business object data notification create request
     */
    protected BusinessObjectDataNotificationRegistrationCreateRequest createBusinessObjectDataNotificationRegistrationCreateRequest(String namespaceCode,
        String notificationName, String notificationEventType, String definitionNamespace, String businessObjectDefinitionName,
        String businessObjectFormatUsage, String businessObjectFormatFileType, Integer businessObjectFormatVersion, String storageName,
        List<JobAction> jobActions)
    {
        BusinessObjectDataNotificationRegistrationCreateRequest request = new BusinessObjectDataNotificationRegistrationCreateRequest();

        request.setBusinessObjectDataNotificationRegistrationKey(new BusinessObjectDataNotificationRegistrationKey(namespaceCode, notificationName));
        request.setBusinessObjectDataEventType(notificationEventType);

        BusinessObjectDataNotificationFilter filter = new BusinessObjectDataNotificationFilter();
        request.setBusinessObjectDataNotificationFilter(filter);
        filter.setNamespace(definitionNamespace);
        filter.setBusinessObjectDefinitionName(businessObjectDefinitionName);
        filter.setBusinessObjectFormatUsage(businessObjectFormatUsage);
        filter.setBusinessObjectFormatFileType(businessObjectFormatFileType);
        filter.setBusinessObjectFormatVersion(businessObjectFormatVersion);
        filter.setStorageName(storageName);

        request.setJobActions(jobActions);

        return request;
    }

    /**
     * Returns a list of test business object data notification registration keys.
     *
     * @return the list of test business object data notification registration keys
     */
    protected List<BusinessObjectDataNotificationRegistrationKey> getTestBusinessObjectDataNotificationRegistrationKeys()
    {
        List<BusinessObjectDataNotificationRegistrationKey> keys = new ArrayList<>();

        keys.add(new BusinessObjectDataNotificationRegistrationKey(NAMESPACE_CD, JOB_NAME_2));
        keys.add(new BusinessObjectDataNotificationRegistrationKey(NAMESPACE_CD_2, JOB_NAME_2));
        keys.add(new BusinessObjectDataNotificationRegistrationKey(NAMESPACE_CD, JOB_NAME));
        keys.add(new BusinessObjectDataNotificationRegistrationKey(NAMESPACE_CD_2, JOB_NAME));

        return keys;
    }

    /**
     * Returns a list of test business object data notification registration keys expected to be returned by getBusinessObjectDataNotificationRegistrations()
     * method.
     *
     * @return the list of expected business object data notification registration keys
     */
    protected List<BusinessObjectDataNotificationRegistrationKey> getExpectedBusinessObjectDataNotificationRegistrationKeys()
    {
        List<BusinessObjectDataNotificationRegistrationKey> keys = new ArrayList<>();

        keys.add(new BusinessObjectDataNotificationRegistrationKey(NAMESPACE_CD, JOB_NAME));
        keys.add(new BusinessObjectDataNotificationRegistrationKey(NAMESPACE_CD, JOB_NAME_2));

        return keys;
    }

    /**
     * Validates business object data notification contents against specified arguments.
     *
     * @param businessObjectDataNotificationId the expected business object data notification ID
     * @param expectedNamespaceCode the expected namespace code
     * @param expectedNotificationName the expected notification name
     * @param expectedEventType the expected event type
     * @param expectedDefinitionNamespace the expected business object definition namespace
     * @param expectedBusinessObjectDefinitionName the expected business object definition name
     * @param expectedBusinessObjectFormatUsage the expected business object format usage
     * @param expectedBusinessObjectFormatFileType the expected business object format file type
     * @param expectedBusinessObjectFormatVersion the expected business object format version
     * @param expectedStorageName the expected storage name
     * @param actualBusinessObjectDataNotificationRegistration the business object data notification object instance to be validated
     */
    protected void validateBusinessObjectDataNotificationRegistration(Integer businessObjectDataNotificationId, String expectedNamespaceCode,
        String expectedNotificationName, String expectedEventType, String expectedDefinitionNamespace, String expectedBusinessObjectDefinitionName,
        String expectedBusinessObjectFormatUsage, String expectedBusinessObjectFormatFileType, Integer expectedBusinessObjectFormatVersion,
        String expectedStorageName, List<JobAction> expectedJobActions,
        BusinessObjectDataNotificationRegistration actualBusinessObjectDataNotificationRegistration)
    {
        assertNotNull(actualBusinessObjectDataNotificationRegistration);
        if (businessObjectDataNotificationId != null)
        {
            assertEquals(businessObjectDataNotificationId, Integer.valueOf(actualBusinessObjectDataNotificationRegistration.getId()));
        }
        validateBusinessObjectDataNotificationRegistrationKey(expectedNamespaceCode, expectedNotificationName,
            actualBusinessObjectDataNotificationRegistration.getBusinessObjectDataNotificationRegistrationKey());
        assertEquals(expectedEventType, actualBusinessObjectDataNotificationRegistration.getBusinessObjectDataEventType());
        validateBusinessObjectDataNotificationFilter(expectedDefinitionNamespace, expectedBusinessObjectDefinitionName, expectedBusinessObjectFormatUsage,
            expectedBusinessObjectFormatFileType, expectedBusinessObjectFormatVersion, expectedStorageName,
            actualBusinessObjectDataNotificationRegistration.getBusinessObjectDataNotificationFilter());
        assertEquals(expectedJobActions, actualBusinessObjectDataNotificationRegistration.getJobActions());
    }

    /**
     * Validates business object data notification filter against specified arguments.
     *
     * @param expectedNamespace the expected namespace
     * @param expectedBusinessObjectDefinitionName the expected business object definition name
     * @param expectedBusinessObjectFormatUsage the expected business object format usage
     * @param expectedBusinessObjectFormatFileType the expected business object format file type
     * @param expectedBusinessObjectFormatVersion the expected business object format version
     * @param expectedStorageName the expected storage name
     * @param actualBusinessObjectDataNotificationFilter the business object data notification filter object instance to be validated
     */
    protected void validateBusinessObjectDataNotificationFilter(String expectedNamespace, String expectedBusinessObjectDefinitionName,
        String expectedBusinessObjectFormatUsage, String expectedBusinessObjectFormatFileType, Integer expectedBusinessObjectFormatVersion,
        String expectedStorageName, BusinessObjectDataNotificationFilter actualBusinessObjectDataNotificationFilter)
    {
        assertNotNull(actualBusinessObjectDataNotificationFilter);
        assertEqualsIgnoreNullOrEmpty("namespace", expectedNamespace, actualBusinessObjectDataNotificationFilter.getNamespace());
        assertEqualsIgnoreNullOrEmpty("business object definition name", expectedBusinessObjectDefinitionName,
            actualBusinessObjectDataNotificationFilter.getBusinessObjectDefinitionName());
        assertEqualsIgnoreNullOrEmpty("business object format usage", expectedBusinessObjectFormatUsage,
            actualBusinessObjectDataNotificationFilter.getBusinessObjectFormatUsage());
        assertEqualsIgnoreNullOrEmpty("business object format file type", expectedBusinessObjectFormatFileType,
            actualBusinessObjectDataNotificationFilter.getBusinessObjectFormatFileType());
        assertEquals(expectedBusinessObjectFormatVersion, actualBusinessObjectDataNotificationFilter.getBusinessObjectFormatVersion());
        assertEqualsIgnoreNullOrEmpty("storage name", expectedStorageName, actualBusinessObjectDataNotificationFilter.getStorageName());
    }

    /**
     * Validates business object data notification registration key against specified arguments.
     *
     * @param expectedNamespaceCode the expected namespace code
     * @param expectedNotificationName the expected notification name
     * @param actualBusinessObjectDataNotificationRegistrationKey the business object data notification registration key object instance to be validated
     */
    protected void validateBusinessObjectDataNotificationRegistrationKey(String expectedNamespaceCode, String expectedNotificationName,
        BusinessObjectDataNotificationRegistrationKey actualBusinessObjectDataNotificationRegistrationKey)
    {
        assertNotNull(actualBusinessObjectDataNotificationRegistrationKey);
        assertEquals(expectedNamespaceCode, actualBusinessObjectDataNotificationRegistrationKey.getNamespace());
        assertEquals(expectedNotificationName, actualBusinessObjectDataNotificationRegistrationKey.getNotificationName());
    }

    /**
     * Creates and returns a list of business object data status elements initialised per provided parameters.
     *
     * @param businessObjectFormatVersion the business object format version
     * @param partitionColumnPosition the position of the partition column (one-based numbering)
     * @param partitionValues the list of partition values
     * @param subPartitionValues the list of subpartition values
     * @param businessObjectDataVersion the business object data version
     * @param reason the reason for the not available business object data
     * @param legacy specifies if not available statuses should be generated using legacy logic
     *
     * @return the newly created list of business object data status elements
     */
    protected List<BusinessObjectDataStatus> getTestBusinessObjectDataStatuses(Integer businessObjectFormatVersion, int partitionColumnPosition,
        List<String> partitionValues, List<String> subPartitionValues, Integer businessObjectDataVersion, String reason, boolean legacy)
    {
        List<BusinessObjectDataStatus> businessObjectDataStatuses = new ArrayList<>();

        if (partitionValues != null)
        {
            for (String partitionValue : partitionValues)
            {
                BusinessObjectDataStatus businessObjectDataStatus = new BusinessObjectDataStatus();
                businessObjectDataStatuses.add(businessObjectDataStatus);
                businessObjectDataStatus.setBusinessObjectFormatVersion(businessObjectFormatVersion);
                businessObjectDataStatus.setBusinessObjectDataVersion(businessObjectDataVersion);
                businessObjectDataStatus.setReason(reason);

                if (BusinessObjectDataServiceImpl.REASON_NOT_REGISTERED.equals(reason))
                {
                    // We are generating business object data status for a not registered business object data.
                    if (partitionColumnPosition == BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION)
                    {
                        // This is a not-available not-registered business object data searched on a primary partition.
                        businessObjectDataStatus.setPartitionValue(partitionValue);
                        businessObjectDataStatus.setSubPartitionValues(legacy ? null : Arrays.asList("", "", "", ""));
                    }
                    else
                    {
                        // This is a not-available not-registered business object data searched on a sub-partition value.
                        if (legacy)
                        {
                            // A legacy case.
                            businessObjectDataStatus.setPartitionValue(partitionValue);
                        }
                        else
                        {
                            // A non-legacy case.
                            businessObjectDataStatus.setPartitionValue("");
                            businessObjectDataStatus.setSubPartitionValues(Arrays.asList("", "", "", ""));
                            businessObjectDataStatus.getSubPartitionValues().set(partitionColumnPosition - 2, partitionValue);
                        }
                    }
                }
                else if (partitionColumnPosition == BusinessObjectDataEntity.FIRST_PARTITION_COLUMN_POSITION)
                {
                    // This is a found business object data selected on primary partition value.
                    businessObjectDataStatus.setPartitionValue(partitionValue);
                    businessObjectDataStatus.setSubPartitionValues(subPartitionValues);
                }
                else
                {
                    // This is a found business object data selected on a subpartition column.
                    businessObjectDataStatus.setPartitionValue(PARTITION_VALUE);
                    List<String> testSubPartitionValues = new ArrayList<>(subPartitionValues);
                    // Please note that the value of the second partition column is located at index 0.
                    testSubPartitionValues.set(partitionColumnPosition - 2, partitionValue);
                    businessObjectDataStatus.setSubPartitionValues(testSubPartitionValues);
                }
            }
        }

        return businessObjectDataStatuses;
    }

    /**
     * Validates business object data availability against specified arguments and expected (hard coded) test values.
     *
     * @param request the business object data availability request
     * @param actualBusinessObjectDataAvailability the business object data availability object instance to be validated
     */
    protected void validateBusinessObjectDataAvailability(BusinessObjectDataAvailabilityRequest request,
        List<BusinessObjectDataStatus> expectedAvailableStatuses, List<BusinessObjectDataStatus> expectedNotAvailableStatuses,
        BusinessObjectDataAvailability actualBusinessObjectDataAvailability)
    {
        assertNotNull(actualBusinessObjectDataAvailability);
        assertEquals(request.getNamespace(), actualBusinessObjectDataAvailability.getNamespace());
        assertEquals(request.getBusinessObjectDefinitionName(), actualBusinessObjectDataAvailability.getBusinessObjectDefinitionName());
        assertEquals(request.getBusinessObjectFormatUsage(), actualBusinessObjectDataAvailability.getBusinessObjectFormatUsage());
        assertEquals(request.getBusinessObjectFormatFileType(), actualBusinessObjectDataAvailability.getBusinessObjectFormatFileType());
        assertEquals(request.getBusinessObjectFormatVersion(), actualBusinessObjectDataAvailability.getBusinessObjectFormatVersion());
        assertEquals(request.getPartitionValueFilter(), actualBusinessObjectDataAvailability.getPartitionValueFilter());
        assertEquals(request.getBusinessObjectDataVersion(), actualBusinessObjectDataAvailability.getBusinessObjectDataVersion());
        assertEquals(request.getStorageName(), actualBusinessObjectDataAvailability.getStorageName());
        assertEquals(expectedAvailableStatuses, actualBusinessObjectDataAvailability.getAvailableStatuses());
        assertEquals(expectedNotAvailableStatuses, actualBusinessObjectDataAvailability.getNotAvailableStatuses());
    }

    /**
     * Returns a newly created business object data create request.
     *
     * @param namespaceCode the namespace code
     * @param businessObjectDefinitionName the business object definition name
     * @param businessObjectFormatUsage the business object format usage
     * @param businessObjectFormatFileType the business object format file type
     * @param businessObjectFormatVersion the business object format version
     * @param partitionKey the partition key
     * @param partitionValue the partition value
     * @param storageName the storage name
     * @param storageDirectoryPath the storage directory path
     * @param storageFiles the list of storage files
     *
     * @return the business object create request
     */
    protected BusinessObjectDataCreateRequest createBusinessObjectDataCreateRequest(String namespaceCode, String businessObjectDefinitionName,
        String businessObjectFormatUsage, String businessObjectFormatFileType, Integer businessObjectFormatVersion, String partitionKey, String partitionValue,
        String businessObjectDataStatusCode, String storageName, String storageDirectoryPath, List<StorageFile> storageFiles)
    {
        // Create a business object data create request.
        BusinessObjectDataCreateRequest businessObjectDataCreateRequest = new BusinessObjectDataCreateRequest();
        businessObjectDataCreateRequest.setNamespace(namespaceCode);
        businessObjectDataCreateRequest.setBusinessObjectDefinitionName(businessObjectDefinitionName);
        businessObjectDataCreateRequest.setBusinessObjectFormatUsage(businessObjectFormatUsage);
        businessObjectDataCreateRequest.setBusinessObjectFormatFileType(businessObjectFormatFileType);
        businessObjectDataCreateRequest.setBusinessObjectFormatVersion(businessObjectFormatVersion);
        businessObjectDataCreateRequest.setPartitionKey(partitionKey);
        businessObjectDataCreateRequest.setPartitionValue(partitionValue);
        businessObjectDataCreateRequest.setStatus(businessObjectDataStatusCode);

        List<StorageUnitCreateRequest> storageUnits = new ArrayList<>();
        businessObjectDataCreateRequest.setStorageUnits(storageUnits);

        StorageUnitCreateRequest storageUnit = new StorageUnitCreateRequest();
        storageUnits.add(storageUnit);
        storageUnit.setStorageName(storageName);
        if (storageDirectoryPath != null)
        {
            StorageDirectory storageDirectory = new StorageDirectory();
            storageUnit.setStorageDirectory(storageDirectory);
            storageDirectory.setDirectoryPath(storageDirectoryPath);
        }
        storageUnit.setStorageFiles(storageFiles);

        return businessObjectDataCreateRequest;
    }

    /**
     * Validates business object data ddl object instance against specified arguments and expected (hard coded) test values.
     *
     * @param request the business object ddl request
     * @param actualBusinessObjectDataDdl the business object data ddl object instance to be validated
     */
    protected void validateBusinessObjectDataDdl(BusinessObjectDataDdlRequest request, String expectedDdl, BusinessObjectDataDdl actualBusinessObjectDataDdl)
    {
        assertNotNull(actualBusinessObjectDataDdl);
        assertEquals(request.getNamespace(), actualBusinessObjectDataDdl.getNamespace());
        assertEquals(request.getBusinessObjectDefinitionName(), actualBusinessObjectDataDdl.getBusinessObjectDefinitionName());
        assertEquals(request.getBusinessObjectFormatUsage(), actualBusinessObjectDataDdl.getBusinessObjectFormatUsage());
        assertEquals(request.getBusinessObjectFormatFileType(), actualBusinessObjectDataDdl.getBusinessObjectFormatFileType());
        assertEquals(request.getBusinessObjectFormatVersion(), actualBusinessObjectDataDdl.getBusinessObjectFormatVersion());
        assertEquals(request.getPartitionValueFilter(), actualBusinessObjectDataDdl.getPartitionValueFilter());
        assertEquals(request.getBusinessObjectDataVersion(), actualBusinessObjectDataDdl.getBusinessObjectDataVersion());
        assertEquals(request.getStorageName(), actualBusinessObjectDataDdl.getStorageName());
        assertEquals(request.getOutputFormat(), actualBusinessObjectDataDdl.getOutputFormat());
        assertEquals(request.getTableName(), actualBusinessObjectDataDdl.getTableName());
        assertEquals(expectedDdl, actualBusinessObjectDataDdl.getDdl());
    }

    /**
     * Create and persist database entities required for testing.
     *
     * @param createBusinessObjectDataEntity specifies if a business object data instance should be created or not
     */
    protected void createDatabaseEntitiesForGetS3KeyPrefixTesting(boolean createBusinessObjectDataEntity)
    {
        // Get a list of test schema partition columns and use the first column name as the partition key.
        List<SchemaColumn> partitionColumns = getTestPartitionColumns();
        String partitionKey = partitionColumns.get(0).getName();

        // Create and persist a business object format entity.
        BusinessObjectFormatEntity businessObjectFormatEntity =
            createBusinessObjectFormatEntity(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, FORMAT_DESCRIPTION,
                LATEST_VERSION_FLAG_SET, partitionKey, NO_PARTITION_KEY_GROUP, NO_ATTRIBUTES, SCHEMA_DELIMITER_PIPE, SCHEMA_ESCAPE_CHARACTER_BACKSLASH,
                SCHEMA_NULL_VALUE_BACKSLASH_N, getTestSchemaColumns(), partitionColumns);

        // If requested, create and persist a business object data entity.
        if (createBusinessObjectDataEntity)
        {
            createBusinessObjectDataEntity(businessObjectFormatEntity, PARTITION_VALUE, SUBPARTITION_VALUES, DATA_VERSION, true, BDATA_STATUS);
        }
    }

    /**
     * Creates a business object data status update request.
     *
     * @param businessObjectDataStatus the business object data status
     *
     * @return the newly created business object data status update request
     */
    protected BusinessObjectDataStatusUpdateRequest createBusinessObjectDataStatusUpdateRequest(String businessObjectDataStatus)
    {
        BusinessObjectDataStatusUpdateRequest request = new BusinessObjectDataStatusUpdateRequest();
        request.setStatus(businessObjectDataStatus);
        return request;
    }

    /**
     * Validates the contents of a business object data status update response against the specified parameters.
     *
     * @param expectedBusinessObjectDataKey the expected business object data key
     * @param expectedBusinessObjectDataStatus the expected business object data status
     * @param expectedPreviousBusinessObjectDataStatus the expected previous business object data status
     * @param actualResponse the actual business object data status update response
     */
    protected void validateBusinessObjectDataStatusUpdateResponse(BusinessObjectDataKey expectedBusinessObjectDataKey, String expectedBusinessObjectDataStatus,
        String expectedPreviousBusinessObjectDataStatus, BusinessObjectDataStatusUpdateResponse actualResponse)
    {
        assertNotNull(actualResponse);
        assertEquals(expectedBusinessObjectDataKey, actualResponse.getBusinessObjectDataKey());
        assertEquals(expectedBusinessObjectDataStatus, actualResponse.getStatus());
        assertEquals(expectedPreviousBusinessObjectDataStatus, actualResponse.getPreviousStatus());
    }

    /**
     * Validates the contents of a business object data status information against the specified parameters.
     *
     * @param expectedBusinessObjectDataKey the expected business object data key
     * @param expectedBusinessObjectDataStatus the expected business object data status
     * @param businessObjectDataStatusInformation the actual business object data status information
     */
    protected void validateBusinessObjectDataStatusInformation(BusinessObjectDataKey expectedBusinessObjectDataKey, String expectedBusinessObjectDataStatus,
        BusinessObjectDataStatusInformation businessObjectDataStatusInformation)
    {
        assertNotNull(businessObjectDataStatusInformation);
        assertEquals(expectedBusinessObjectDataKey, businessObjectDataStatusInformation.getBusinessObjectDataKey());
        assertEquals(expectedBusinessObjectDataStatus, businessObjectDataStatusInformation.getStatus());
    }

    protected BusinessObjectDataStorageFilesCreateRequest createBusinessObjectDataStorageFilesCreateRequest(String namespace,
        String businessObjectDefinitionName, String businessObjectFormatUsage, String businessObjectFormatFileType, Integer businessObjectFormatVersion,
        String partitionValue, List<String> subPartitionValues, Integer businessObjectDataVersion, String storageName, List<StorageFile> storageFiles)
    {
        BusinessObjectDataStorageFilesCreateRequest request = new BusinessObjectDataStorageFilesCreateRequest();
        request.setNamespace(namespace);
        request.setBusinessObjectDefinitionName(businessObjectDefinitionName);
        request.setBusinessObjectFormatFileType(businessObjectFormatFileType);
        request.setBusinessObjectFormatUsage(businessObjectFormatUsage);
        request.setBusinessObjectFormatVersion(businessObjectFormatVersion);
        request.setPartitionValue(partitionValue);
        request.setSubPartitionValues(subPartitionValues);
        request.setBusinessObjectDataVersion(businessObjectDataVersion);
        request.setStorageName(storageName);
        request.setStorageFiles(storageFiles);
        return request;
    }

    /**
     * Validates business object data storage files create response contents against specified parameters.
     *
     * @param expectedNamespace the expected namespace
     * @param expectedBusinessObjectDefinitionName the expected business object definition name
     * @param expectedBusinessObjectFormatUsage the expected business object format usage
     * @param expectedBusinessObjectFormatFileType the expected business object format file type
     * @param expectedBusinessObjectFormatVersion the expected business object format version
     * @param expectedPartitionValue the expected partition value
     * @param expectedSubPartitionValues the expected subpartition values
     * @param expectedBusinessObjectDataVersion the expected business object data version
     * @param expectedStorageName the expected storage name
     * @param expectedStorageFiles the list of expected storage files
     * @param actualResponse the business object data storage files create response to be validated
     */
    protected void validateBusinessObjectDataStorageFilesCreateResponse(String expectedNamespace, String expectedBusinessObjectDefinitionName,
        String expectedBusinessObjectFormatUsage, String expectedBusinessObjectFormatFileType, Integer expectedBusinessObjectFormatVersion,
        String expectedPartitionValue, List<String> expectedSubPartitionValues, Integer expectedBusinessObjectDataVersion, String expectedStorageName,
        List<StorageFile> expectedStorageFiles, BusinessObjectDataStorageFilesCreateResponse actualResponse)
    {
        assertNotNull(actualResponse);
        assertEquals(expectedNamespace, actualResponse.getNamespace());
        assertEquals(expectedBusinessObjectDefinitionName, actualResponse.getBusinessObjectDefinitionName());
        assertEquals(expectedBusinessObjectFormatUsage, actualResponse.getBusinessObjectFormatUsage());
        assertEquals(expectedBusinessObjectFormatFileType, actualResponse.getBusinessObjectFormatFileType());
        assertEquals(expectedBusinessObjectFormatVersion, actualResponse.getBusinessObjectFormatVersion());
        assertEquals(expectedPartitionValue, actualResponse.getPartitionValue());
        assertEquals(expectedSubPartitionValues, actualResponse.getSubPartitionValues());
        assertEquals(expectedBusinessObjectDataVersion, actualResponse.getBusinessObjectDataVersion());
        assertEquals(expectedStorageName, actualResponse.getStorageName());
        assertEquals(expectedStorageFiles, actualResponse.getStorageFiles());
    }

    /**
     * Create and persist database entities required for testing.
     */
    protected void createDatabaseEntitiesForBusinessObjectDefinitionTesting()
    {
        createDatabaseEntitiesForBusinessObjectDefinitionTesting(NAMESPACE_CD, DATA_PROVIDER_NAME);
    }

    /**
     * Create and persist database entities required for testing.
     *
     * @param namespaceCode the namespace code
     * @param dataProviderName the data provider name
     */
    protected void createDatabaseEntitiesForBusinessObjectDefinitionTesting(String namespaceCode, String dataProviderName)
    {
        // Create a namespace entity.
        createNamespaceEntity(namespaceCode);

        // Create a data provider entity.
        createDataProviderEntity(dataProviderName);
    }

    /**
     * Creates a business object data definition update request.
     *
     * @param businessObjectDefinitionDescription the description of the business object definition
     *
     * @return the newly created business object definition update request
     */
    protected BusinessObjectDefinitionUpdateRequest createBusinessObjectDefinitionUpdateRequest(String businessObjectDefinitionDescription,
        List<Attribute> attributes)
    {
        BusinessObjectDefinitionUpdateRequest request = new BusinessObjectDefinitionUpdateRequest();
        request.setDescription(businessObjectDefinitionDescription);
        request.setAttributes(attributes);
        return request;
    }

    /**
     * Validates business object definition contents against specified arguments.
     *
     * @param expectedBusinessObjectDefinitionId the expected business object definition ID
     * @param expectedNamespaceCode the expected namespace code
     * @param expectedBusinessObjectDefinitionName the expected business object definition name
     * @param expectedDataProviderName the expected data provider name
     * @param expectedBusinessObjectDefinitionDescription the expected business object definition description
     * @param expectedAttributes the expected list of attributes
     * @param actualBusinessObjectDefinition the business object definition object instance to be validated
     */
    protected void validateBusinessObjectDefinition(Integer expectedBusinessObjectDefinitionId, String expectedNamespaceCode,
        String expectedBusinessObjectDefinitionName, String expectedDataProviderName, String expectedBusinessObjectDefinitionDescription,
        List<Attribute> expectedAttributes, BusinessObjectDefinition actualBusinessObjectDefinition)
    {
        assertNotNull(actualBusinessObjectDefinition);
        if (expectedBusinessObjectDefinitionId != null)
        {
            assertEquals(expectedBusinessObjectDefinitionId, Integer.valueOf(actualBusinessObjectDefinition.getId()));
        }
        assertEquals(expectedNamespaceCode, actualBusinessObjectDefinition.getNamespace());
        assertEquals(expectedBusinessObjectDefinitionName, actualBusinessObjectDefinition.getBusinessObjectDefinitionName());
        assertEquals(expectedDataProviderName, actualBusinessObjectDefinition.getDataProviderName());
        assertEquals(expectedBusinessObjectDefinitionDescription, actualBusinessObjectDefinition.getDescription());
        assertEquals(expectedAttributes, actualBusinessObjectDefinition.getAttributes());
    }

    /**
     * Creates a business object format create request.
     *
     * @param businessObjectDefinitionName the business object format definition name
     * @param businessObjectFormatUsage the business object format usage
     * @param businessObjectFormatFileType the business object format file type
     * @param partitionKey the business object format partition key
     * @param description the business object format description
     * @param attributeDefinitions the list of attribute definitions
     * @param schema the business object format schema
     *
     * @return the created business object format create request
     */
    protected BusinessObjectFormatCreateRequest createBusinessObjectFormatCreateRequest(String namespaceCode, String businessObjectDefinitionName,
        String businessObjectFormatUsage, String businessObjectFormatFileType, String partitionKey, String description,
        List<AttributeDefinition> attributeDefinitions, Schema schema)
    {
        return createBusinessObjectFormatCreateRequest(namespaceCode, businessObjectDefinitionName, businessObjectFormatUsage, businessObjectFormatFileType,
            partitionKey, description, NO_ATTRIBUTES, attributeDefinitions, schema);
    }

    /**
     * Creates a business object format create request.
     *
     * @param businessObjectDefinitionName the business object format definition name
     * @param businessObjectFormatUsage the business object format usage
     * @param businessObjectFormatFileType the business object format file type
     * @param partitionKey the business object format partition key
     * @param description the business object format description
     * @param attributes the list of attributes
     * @param attributeDefinitions the list of attribute definitions
     * @param schema the business object format schema
     *
     * @return the created business object format create request
     */
    protected BusinessObjectFormatCreateRequest createBusinessObjectFormatCreateRequest(String namespaceCode, String businessObjectDefinitionName,
        String businessObjectFormatUsage, String businessObjectFormatFileType, String partitionKey, String description, List<Attribute> attributes,
        List<AttributeDefinition> attributeDefinitions, Schema schema)
    {
        BusinessObjectFormatCreateRequest businessObjectFormatCreateRequest = new BusinessObjectFormatCreateRequest();

        businessObjectFormatCreateRequest.setNamespace(namespaceCode);
        businessObjectFormatCreateRequest.setBusinessObjectDefinitionName(businessObjectDefinitionName);
        businessObjectFormatCreateRequest.setBusinessObjectFormatUsage(businessObjectFormatUsage);
        businessObjectFormatCreateRequest.setBusinessObjectFormatFileType(businessObjectFormatFileType);
        businessObjectFormatCreateRequest.setPartitionKey(partitionKey);
        businessObjectFormatCreateRequest.setDescription(description);
        businessObjectFormatCreateRequest.setAttributes(attributes);
        businessObjectFormatCreateRequest.setAttributeDefinitions(attributeDefinitions);
        businessObjectFormatCreateRequest.setSchema(schema);

        return businessObjectFormatCreateRequest;
    }

    /**
     * Creates a business object format update request.
     *
     * @param description the business object format description
     * @param schema the business object format schema
     *
     * @return the created business object format create request
     */
    protected BusinessObjectFormatUpdateRequest createBusinessObjectFormatUpdateRequest(String description, Schema schema)
    {
        return createBusinessObjectFormatUpdateRequest(description, NO_ATTRIBUTES, schema);
    }

    /**
     * Creates a business object format update request.
     *
     * @param description the business object format description
     * @param attributes the list of attributes
     * @param schema the business object format schema
     *
     * @return the created business object format create request
     */
    protected BusinessObjectFormatUpdateRequest createBusinessObjectFormatUpdateRequest(String description, List<Attribute> attributes, Schema schema)
    {
        BusinessObjectFormatUpdateRequest businessObjectFormatCreateRequest = new BusinessObjectFormatUpdateRequest();

        businessObjectFormatCreateRequest.setDescription(description);
        businessObjectFormatCreateRequest.setAttributes(attributes);
        businessObjectFormatCreateRequest.setSchema(schema);

        return businessObjectFormatCreateRequest;
    }

    /**
     * Creates business object format by calling the relative service method and using hard coded test values.
     *
     * @return the newly created business object format
     */
    protected BusinessObjectFormat createTestBusinessObjectFormat()
    {
        return createTestBusinessObjectFormat(NO_ATTRIBUTES);
    }

    /**
     * Creates business object format by calling the relative service method and using hard coded test values.
     *
     * @param attributes the attributes
     *
     * @return the newly created business object format
     */
    protected BusinessObjectFormat createTestBusinessObjectFormat(List<Attribute> attributes)
    {
        // Create relative database entities.
        createTestDatabaseEntitiesForBusinessObjectFormatTesting();

        // Create an initial version of the business object format.
        BusinessObjectFormatCreateRequest request =
            createBusinessObjectFormatCreateRequest(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, PARTITION_KEY, FORMAT_DESCRIPTION,
                attributes, getTestAttributeDefinitions(), getTestSchema());

        return businessObjectFormatService.createBusinessObjectFormat(request);
    }

    /**
     * Creates relative database entities required for the unit tests.
     */
    protected void createTestDatabaseEntitiesForBusinessObjectFormatTesting()
    {
        createTestDatabaseEntitiesForBusinessObjectFormatTesting(NAMESPACE_CD, DATA_PROVIDER_NAME, BOD_NAME, FORMAT_FILE_TYPE_CODE, PARTITION_KEY_GROUP, null);
    }

    /**
     * Creates relative database entities required for the unit tests.
     *
     * @param namespaceCode the namespace Code
     * @param dataProviderName the data provider name
     * @param businessObjectDefinitionName the business object format definition name
     * @param businessObjectFormatFileType the business object format file type
     * @param partitionKeyGroupName the partition key group name
     */
    protected void createTestDatabaseEntitiesForBusinessObjectFormatTesting(String namespaceCode, String dataProviderName, String businessObjectDefinitionName,
        String businessObjectFormatFileType, String partitionKeyGroupName, Boolean legacy)
    {
        createBusinessObjectDefinitionEntity(namespaceCode, businessObjectDefinitionName, dataProviderName, BOD_DESCRIPTION, legacy);
        createFileTypeEntity(businessObjectFormatFileType, FORMAT_FILE_TYPE_DESCRIPTION);
        createPartitionKeyGroupEntity(partitionKeyGroupName);
    }

    /**
     * Validates business object format contents against specified arguments and expected (hard coded) test values.
     *
     * @param expectedBusinessObjectFormatId the expected business object format ID
     * @param expectedNamespaceCode the expected namespace code
     * @param expectedBusinessObjectDefinitionName the expected business object definition name
     * @param expectedBusinessObjectFormatUsage the expected business object format usage
     * @param expectedBusinessObjectFormatFileType the expected business object format file type
     * @param expectedBusinessObjectFormatVersion the expected business object format version
     * @param expectedIsLatestVersion the expected business object format version
     * @param expectedPartitionKey the expected business object format partition key
     * @param expectedDescription the expected business object format description
     * @param expectedAttributeDefinitions the list of expected attribute definitions
     * @param expectedSchema the expected business object format schema
     * @param actualBusinessObjectFormat the BusinessObjectFormat object instance to be validated
     */
    protected void validateBusinessObjectFormat(Integer expectedBusinessObjectFormatId, String expectedNamespaceCode,
        String expectedBusinessObjectDefinitionName, String expectedBusinessObjectFormatUsage, String expectedBusinessObjectFormatFileType,
        Integer expectedBusinessObjectFormatVersion, Boolean expectedIsLatestVersion, String expectedPartitionKey, String expectedDescription,
        List<AttributeDefinition> expectedAttributeDefinitions, Schema expectedSchema, BusinessObjectFormat actualBusinessObjectFormat)
    {
        validateBusinessObjectFormat(expectedBusinessObjectFormatId, expectedNamespaceCode, expectedBusinessObjectDefinitionName,
            expectedBusinessObjectFormatUsage, expectedBusinessObjectFormatFileType, expectedBusinessObjectFormatVersion, expectedIsLatestVersion,
            expectedPartitionKey, expectedDescription, NO_ATTRIBUTES, expectedAttributeDefinitions, expectedSchema, actualBusinessObjectFormat);
    }

    /**
     * Validates business object format contents against specified arguments and expected (hard coded) test values.
     *
     * @param expectedBusinessObjectFormatId the expected business object format ID
     * @param expectedNamespaceCode the expected namespace code
     * @param expectedBusinessObjectDefinitionName the expected business object definition name
     * @param expectedBusinessObjectFormatUsage the expected business object format usage
     * @param expectedBusinessObjectFormatFileType the expected business object format file type
     * @param expectedBusinessObjectFormatVersion the expected business object format version
     * @param expectedIsLatestVersion the expected business object format version
     * @param expectedPartitionKey the expected business object format partition key
     * @param expectedDescription the expected business object format description
     * @param expectedAttributes the expected attributes
     * @param expectedAttributeDefinitions the list of expected attribute definitions
     * @param expectedSchema the expected business object format schema
     * @param actualBusinessObjectFormat the BusinessObjectFormat object instance to be validated
     */
    protected void validateBusinessObjectFormat(Integer expectedBusinessObjectFormatId, String expectedNamespaceCode,
        String expectedBusinessObjectDefinitionName, String expectedBusinessObjectFormatUsage, String expectedBusinessObjectFormatFileType,
        Integer expectedBusinessObjectFormatVersion, Boolean expectedIsLatestVersion, String expectedPartitionKey, String expectedDescription,
        List<Attribute> expectedAttributes, List<AttributeDefinition> expectedAttributeDefinitions, Schema expectedSchema,
        BusinessObjectFormat actualBusinessObjectFormat)
    {
        assertNotNull(actualBusinessObjectFormat);

        if (expectedBusinessObjectFormatId != null)
        {
            assertEquals(expectedBusinessObjectFormatId, Integer.valueOf(actualBusinessObjectFormat.getId()));
        }

        assertEquals(expectedNamespaceCode, actualBusinessObjectFormat.getNamespace());
        assertEquals(expectedBusinessObjectDefinitionName, actualBusinessObjectFormat.getBusinessObjectDefinitionName());
        assertEquals(expectedBusinessObjectFormatUsage, actualBusinessObjectFormat.getBusinessObjectFormatUsage());
        assertEquals(expectedBusinessObjectFormatFileType, actualBusinessObjectFormat.getBusinessObjectFormatFileType());
        assertEquals(expectedBusinessObjectFormatVersion, Integer.valueOf(actualBusinessObjectFormat.getBusinessObjectFormatVersion()));
        assertEquals(expectedIsLatestVersion, actualBusinessObjectFormat.isLatestVersion());
        assertEquals(expectedPartitionKey, actualBusinessObjectFormat.getPartitionKey());
        assertEqualsIgnoreNullOrEmpty("description", expectedDescription, actualBusinessObjectFormat.getDescription());

        // Ignoring the order, check if the actual list of attributes matches the expected list.
        if (!CollectionUtils.isEmpty(expectedAttributes))
        {
            assertEquals(expectedAttributes, actualBusinessObjectFormat.getAttributes());
        }
        else
        {
            assertEquals(0, actualBusinessObjectFormat.getAttributes().size());
        }

        // Ignoring the order, check if the actual list of attribute definitions matches the expected list.
        if (!CollectionUtils.isEmpty(expectedAttributeDefinitions))
        {
            assertEquals(expectedAttributeDefinitions.size(), actualBusinessObjectFormat.getAttributeDefinitions().size());

            for (int i = 0; i < expectedAttributeDefinitions.size(); i++)
            {
                AttributeDefinition expectedAttributeDefinition = expectedAttributeDefinitions.get(i);
                AttributeDefinition actualAttributeDefinition = actualBusinessObjectFormat.getAttributeDefinitions().get(i);
                assertEquals(expectedAttributeDefinition.getName(), actualAttributeDefinition.getName());
            }
        }
        else
        {
            assertEquals(0, actualBusinessObjectFormat.getAttributeDefinitions().size());
        }

        // Validate the schema.
        if (expectedSchema != null)
        {
            assertNotNull(actualBusinessObjectFormat.getSchema());
            assertEqualsIgnoreNullOrEmpty("null value", expectedSchema.getNullValue(), actualBusinessObjectFormat.getSchema().getNullValue());
            assertEqualsIgnoreNullOrEmpty("delimiter", expectedSchema.getDelimiter(), actualBusinessObjectFormat.getSchema().getDelimiter());
            assertEqualsIgnoreNullOrEmpty("escape character", expectedSchema.getEscapeCharacter(), actualBusinessObjectFormat.getSchema().getEscapeCharacter());
            assertEquals(expectedSchema.getPartitionKeyGroup(), actualBusinessObjectFormat.getSchema().getPartitionKeyGroup());
            assertEquals(expectedSchema.getColumns().size(), actualBusinessObjectFormat.getSchema().getColumns().size());

            for (int i = 0; i < expectedSchema.getColumns().size(); i++)
            {
                SchemaColumn expectedSchemaColumn = expectedSchema.getColumns().get(i);
                SchemaColumn actualSchemaColumn = actualBusinessObjectFormat.getSchema().getColumns().get(i);
                assertEquals(expectedSchemaColumn.getName(), actualSchemaColumn.getName());
                assertEquals(expectedSchemaColumn.getType(), actualSchemaColumn.getType());
                assertEquals(expectedSchemaColumn.getSize(), actualSchemaColumn.getSize());
                assertEquals(expectedSchemaColumn.isRequired(), actualSchemaColumn.isRequired());
                assertEquals(expectedSchemaColumn.getDefaultValue(), actualSchemaColumn.getDefaultValue());
                assertEquals(expectedSchemaColumn.getDescription(), actualSchemaColumn.getDescription());
            }

            if (CollectionUtils.isEmpty(expectedSchema.getPartitions()))
            {
                assertTrue(CollectionUtils.isEmpty(actualBusinessObjectFormat.getSchema().getPartitions()));
            }
            else
            {
                for (int i = 0; i < expectedSchema.getPartitions().size(); i++)
                {
                    SchemaColumn expectedPartitionColumn = expectedSchema.getPartitions().get(i);
                    SchemaColumn actualPartitionColumn = actualBusinessObjectFormat.getSchema().getPartitions().get(i);
                    assertEquals(expectedPartitionColumn.getName(), actualPartitionColumn.getName());
                    assertEquals(expectedPartitionColumn.getType(), actualPartitionColumn.getType());
                    assertEquals(expectedPartitionColumn.getSize(), actualPartitionColumn.getSize());
                    assertEquals(expectedPartitionColumn.isRequired(), actualPartitionColumn.isRequired());
                    assertEquals(expectedPartitionColumn.getDefaultValue(), actualPartitionColumn.getDefaultValue());
                    assertEquals(expectedPartitionColumn.getDescription(), actualPartitionColumn.getDescription());
                }
            }
        }
        else
        {
            assertNull(actualBusinessObjectFormat.getSchema());
        }
    }

    /**
     * Validates business object format ddl object instance against hard coded test values.
     *
     * @param expectedCustomDdlName the expected custom ddl name
     * @param expectedDdl the expected DDL
     * @param actualBusinessObjectFormatDdl the business object format ddl object instance to be validated
     */
    protected void validateBusinessObjectFormatDdl(String expectedCustomDdlName, String expectedDdl, BusinessObjectFormatDdl actualBusinessObjectFormatDdl)
    {
        validateBusinessObjectFormatDdl(NAMESPACE_CD, BOD_NAME, FORMAT_USAGE_CODE, FileTypeEntity.TXT_FILE_TYPE, FORMAT_VERSION,
            BusinessObjectDataDdlOutputFormatEnum.HIVE_13_DDL, TABLE_NAME, expectedCustomDdlName, expectedDdl, actualBusinessObjectFormatDdl);
    }

    /**
     * Validates business object format ddl object instance against specified parameters.
     *
     * @param expectedNamespaceCode the expected namespace code
     * @param expectedBusinessObjectDefinitionName the expected business object definition name
     * @param expectedBusinessObjectFormatUsage the expected business object format usage
     * @param expectedBusinessObjectFormatFileType the expected business object format file type
     * @param expectedBusinessObjectFormatVersion the expected business object format version
     * @param expectedOutputFormat the expected output format
     * @param expectedTableName the expected table name
     * @param expectedCustomDdlName the expected custom ddl name
     * @param expectedDdl the expected DDL
     * @param actualBusinessObjectFormatDdl the business object format ddl object instance to be validated
     */
    protected void validateBusinessObjectFormatDdl(String expectedNamespaceCode, String expectedBusinessObjectDefinitionName,
        String expectedBusinessObjectFormatUsage, String expectedBusinessObjectFormatFileType, Integer expectedBusinessObjectFormatVersion,
        BusinessObjectDataDdlOutputFormatEnum expectedOutputFormat, String expectedTableName, String expectedCustomDdlName, String expectedDdl,
        BusinessObjectFormatDdl actualBusinessObjectFormatDdl)
    {
        assertNotNull(actualBusinessObjectFormatDdl);
        assertEquals(expectedNamespaceCode, actualBusinessObjectFormatDdl.getNamespace());
        assertEquals(expectedBusinessObjectDefinitionName, actualBusinessObjectFormatDdl.getBusinessObjectDefinitionName());
        assertEquals(expectedBusinessObjectFormatUsage, actualBusinessObjectFormatDdl.getBusinessObjectFormatUsage());
        assertEquals(expectedBusinessObjectFormatFileType, actualBusinessObjectFormatDdl.getBusinessObjectFormatFileType());
        assertEquals(expectedBusinessObjectFormatVersion, actualBusinessObjectFormatDdl.getBusinessObjectFormatVersion());
        assertEquals(expectedOutputFormat, actualBusinessObjectFormatDdl.getOutputFormat());
        assertEquals(expectedTableName, actualBusinessObjectFormatDdl.getTableName());
        assertEquals(expectedCustomDdlName, actualBusinessObjectFormatDdl.getCustomDdlName());
        assertEquals(expectedDdl, actualBusinessObjectFormatDdl.getDdl());
    }

    /**
     * Returns a list of attribute definitions that use hard coded test values.
     *
     * @return the list of test attribute definitions
     */
    protected List<AttributeDefinition> getTestAttributeDefinitions()
    {
        // Build a list of attribute definitions.
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();

        AttributeDefinition attribute1 = new AttributeDefinition();
        attribute1.setName(ATTRIBUTE_NAME_1_MIXED_CASE);
        attributeDefinitions.add(attribute1);

        AttributeDefinition attribute2 = new AttributeDefinition();
        attribute2.setName(ATTRIBUTE_NAME_2_MIXED_CASE);
        attributeDefinitions.add(attribute2);

        return attributeDefinitions;
    }

    /**
     * Returns a business object format schema that uses hard coded test values.
     *
     * @return the test business object format schema
     */
    protected Schema getTestSchema()
    {
        Schema schema = new Schema();

        schema.setNullValue(SCHEMA_NULL_VALUE_BACKSLASH_N);
        schema.setDelimiter(SCHEMA_DELIMITER_PIPE);
        schema.setEscapeCharacter(SCHEMA_ESCAPE_CHARACTER_BACKSLASH);
        schema.setPartitionKeyGroup(PARTITION_KEY_GROUP);
        schema.setColumns(getTestSchemaColumns(RANDOM_SUFFIX));
        schema.setPartitions(getTestPartitionColumns(RANDOM_SUFFIX));

        return schema;
    }

    /**
     * Returns a business object format schema that uses hard coded test values.
     *
     * @return the test business object format schema
     */
    protected Schema getTestSchema2()
    {
        Schema schema = new Schema();

        schema.setNullValue(SCHEMA_NULL_VALUE_NULL_WORD);
        schema.setDelimiter(SCHEMA_DELIMITER_COMMA);
        schema.setEscapeCharacter(SCHEMA_ESCAPE_CHARACTER_TILDE);
        schema.setPartitionKeyGroup(PARTITION_KEY_GROUP_2);
        schema.setColumns(getTestSchemaColumns(RANDOM_SUFFIX_2));
        schema.setPartitions(getTestPartitionColumns(RANDOM_SUFFIX_2));

        return schema;
    }

    /**
     * Returns a list of schema columns that use hard coded test values.
     *
     * @return the list of test schema columns
     */
    protected List<SchemaColumn> getTestSchemaColumns(String randomSuffix)
    {
        return getTestSchemaColumns(SCHEMA_COLUMN_NAME_PREFIX, 0, MAX_COLUMNS, randomSuffix);
    }

    /**
     * Returns a list of schema partition columns that use hard coded test values.
     *
     * @return the list of test schema partition columns
     */
    protected List<SchemaColumn> getTestPartitionColumns(String randomSuffix)
    {
        List<SchemaColumn> partitionColumns = new ArrayList<>();

        // Add first 3 partition column matching to regular partition columns.
        partitionColumns.addAll(getTestSchemaColumns(SCHEMA_COLUMN_NAME_PREFIX, 0, 3, randomSuffix));

        // Add the remaining partition columns.
        partitionColumns.addAll(getTestSchemaColumns(SCHEMA_PARTITION_COLUMN_NAME_PREFIX, 3, MAX_PARTITIONS - 3, randomSuffix));

        // Update top level partition column name to match the business object format partition key.
        partitionColumns.get(0).setName(PARTITION_KEY);

        return partitionColumns;
    }

    /**
     * Returns a list of schema columns that use passed attributes and hard coded test values.
     *
     * @param columnNamePrefix the column name prefix to use for the test columns
     * @param offset the offset index to start generating columns with
     * @param numColumns the number of columns
     *
     * @return the list of test schema columns
     */
    protected List<SchemaColumn> getTestSchemaColumns(String columnNamePrefix, Integer offset, Integer numColumns, String randomSuffix)
    {
        // Build a list of schema columns.
        List<SchemaColumn> columns = new ArrayList<>();

        for (int i = 0; i < numColumns; i++)
        {
            SchemaColumn schemaColumn = new SchemaColumn();
            columns.add(schemaColumn);
            // Required fields.
            schemaColumn.setName(String.format("%s-%d%s", columnNamePrefix, i + offset, randomSuffix));
            schemaColumn.setType(String.format("Type-%d", i + offset));
            // Optional fields.
            schemaColumn.setSize(i % 2 == 0 ? null : String.format("Size-%d", i + offset));
            schemaColumn.setRequired(i % 3 == 0 ? null : i % 2 == 0);
            schemaColumn.setDefaultValue(i % 2 == 0 ? null : String.format("Clmn-Dflt-Value-%d%s", i, randomSuffix));
            schemaColumn.setDescription(i % 2 == 0 ? null : String.format("Clmn-Desc-%d%s", i, randomSuffix));
        }

        return columns;
    }

    /**
     * Adds whitespace characters to the relative fields of the business object format schema.
     *
     * @param schema the business object format schema
     *
     * @return the business object format schema with the relative fields having leading and trailing whitespace added
     */
    protected Schema addWhitespace(Schema schema)
    {
        // Add whitespace to the partition key group field.
        schema.setPartitionKeyGroup(addWhitespace(schema.getPartitionKeyGroup()));

        // Add whitespace characters to the relative schema column fields.
        List<SchemaColumn> allSchemaColumns = new ArrayList<>();
        allSchemaColumns.addAll(schema.getColumns());
        allSchemaColumns.addAll(schema.getPartitions());

        for (SchemaColumn schemaColumn : allSchemaColumns)
        {
            schemaColumn.setName(addWhitespace(schemaColumn.getName()));
            schemaColumn.setType(addWhitespace(schemaColumn.getType()));
            schemaColumn.setSize(schemaColumn.getSize() == null ? null : addWhitespace(schemaColumn.getSize()));
            schemaColumn.setDefaultValue(schemaColumn.getDefaultValue() == null ? null : addWhitespace(schemaColumn.getDefaultValue()));
        }

        return schema;
    }


    /**
     * Creates a custom DDL create request.
     *
     * @return the newly created custom DDL create request
     */
    protected CustomDdlCreateRequest createCustomDdlCreateRequest(String namespaceCode, String businessObjectDefinitionName, String businessObjectFormatUsage,
        String businessObjectFormatFileType, Integer businessObjectFormatVersion, String customDdlName, String ddl)
    {
        CustomDdlCreateRequest request = new CustomDdlCreateRequest();
        request.setCustomDdlKey(
            new CustomDdlKey(namespaceCode, businessObjectDefinitionName, businessObjectFormatUsage, businessObjectFormatFileType, businessObjectFormatVersion,
                customDdlName));
        request.setDdl(ddl);
        return request;
    }

    /**
     * Creates a custom DDL update request.
     *
     * @return the newly created custom DDL update request
     */
    protected CustomDdlUpdateRequest createCustomDdlUpdateRequest(String ddl)
    {
        CustomDdlUpdateRequest request = new CustomDdlUpdateRequest();
        request.setDdl(ddl);
        return request;
    }

    /**
     * Validates custom DDL contents against specified parameters.
     *
     * @param customDdlId the expected custom DDL ID
     * @param expectedNamespace the expected namespace
     * @param expectedBusinessObjectDefinitionName the expected business object definition name
     * @param expectedBusinessObjectFormatUsage the expected business object format usage
     * @param expectedBusinessObjectFormatFileType the expected business object format file type
     * @param expectedBusinessObjectFormatVersion the expected business object format version
     * @param expectedCustomDdlName the expected custom DDL name
     * @param expectedDdl the expected DDL
     * @param actualCustomDdl the custom DDL object instance to be validated
     */
    protected void validateCustomDdl(Integer customDdlId, String expectedNamespace, String expectedBusinessObjectDefinitionName,
        String expectedBusinessObjectFormatUsage, String expectedBusinessObjectFormatFileType, Integer expectedBusinessObjectFormatVersion,
        String expectedCustomDdlName, String expectedDdl, CustomDdl actualCustomDdl)
    {
        assertNotNull(actualCustomDdl);
        if (customDdlId != null)
        {
            assertEquals(customDdlId, Integer.valueOf(actualCustomDdl.getId()));
        }
        validateCustomDdlKey(expectedNamespace, expectedBusinessObjectDefinitionName, expectedBusinessObjectFormatUsage, expectedBusinessObjectFormatFileType,
            expectedBusinessObjectFormatVersion, expectedCustomDdlName, actualCustomDdl.getCustomDdlKey());
        assertEquals(expectedDdl, actualCustomDdl.getDdl());
    }

    /**
     * Creates a namespace create request.
     *
     * @param namespaceCode the namespace code
     *
     * @return the newly created namespace create request
     */
    protected NamespaceCreateRequest createNamespaceCreateRequest(String namespaceCode)
    {
        NamespaceCreateRequest request = new NamespaceCreateRequest();
        request.setNamespaceCode(namespaceCode);
        return request;
    }

    /**
     * Validates namespace contents against specified parameters.
     *
     * @param expectedNamespaceCode the expected namespace code
     * @param actualNamespace the namespace object instance to be validated
     */
    protected void validateNamespace(String expectedNamespaceCode, Namespace actualNamespace)
    {
        assertNotNull(actualNamespace);
        assertEquals(expectedNamespaceCode, actualNamespace.getNamespaceCode());
    }

    /**
     * Creates a partition key group create request.
     *
     * @param partitionKeyGroupName the partition key group name
     *
     * @return the created partition key group create request
     */
    protected PartitionKeyGroupCreateRequest createPartitionKeyGroupCreateRequest(String partitionKeyGroupName)
    {
        PartitionKeyGroupCreateRequest partitionKeyGroupCreateRequest = new PartitionKeyGroupCreateRequest();
        partitionKeyGroupCreateRequest.setPartitionKeyGroupKey(createPartitionKeyGroupKey(partitionKeyGroupName));
        return partitionKeyGroupCreateRequest;
    }

    /**
     * Creates partition key group by calling the relative service method.
     *
     * @param partitionKeyGroupName the partition key group name
     *
     * @return the newly created partition key group
     */
    protected PartitionKeyGroup createPartitionKeyGroup(String partitionKeyGroupName)
    {
        PartitionKeyGroupCreateRequest request = createPartitionKeyGroupCreateRequest(partitionKeyGroupName);
        return partitionKeyGroupService.createPartitionKeyGroup(request);
    }

    /**
     * Validates partition key group contents against specified arguments.
     *
     * @param expectedPartitionKeyGroupName the expected partition key group name
     * @param actualPartitionKeyGroup the partition key group object instance to be validated
     */
    protected void validatePartitionKeyGroup(String expectedPartitionKeyGroupName, PartitionKeyGroup actualPartitionKeyGroup)
    {
        assertNotNull(actualPartitionKeyGroup);
        assertEquals(expectedPartitionKeyGroupName, actualPartitionKeyGroup.getPartitionKeyGroupKey().getPartitionKeyGroupName());
    }


    /**
     * Creates an expected partition values create request.
     *
     * @param partitionKeyGroupName the partition key group name
     * @param expectedPartitionValues the list of expected partition values
     *
     * @return the expected partition values create request
     */
    protected ExpectedPartitionValuesCreateRequest createExpectedPartitionValuesCreateRequest(String partitionKeyGroupName,
        List<String> expectedPartitionValues)
    {
        ExpectedPartitionValuesCreateRequest expectedPartitionValuesCreateRequest = new ExpectedPartitionValuesCreateRequest();
        expectedPartitionValuesCreateRequest.setPartitionKeyGroupKey(createPartitionKeyGroupKey(partitionKeyGroupName));
        expectedPartitionValuesCreateRequest.setExpectedPartitionValues(expectedPartitionValues);
        return expectedPartitionValuesCreateRequest;
    }

    /**
     * Creates an expected partition values delete request.
     *
     * @param partitionKeyGroupName the partition key group name
     * @param expectedPartitionValues the list of expected partition values
     *
     * @return the expected partition values delete request
     */
    protected ExpectedPartitionValuesDeleteRequest createExpectedPartitionValuesDeleteRequest(String partitionKeyGroupName,
        List<String> expectedPartitionValues)
    {
        ExpectedPartitionValuesDeleteRequest expectedPartitionValuesDeleteRequest = new ExpectedPartitionValuesDeleteRequest();
        expectedPartitionValuesDeleteRequest.setPartitionKeyGroupKey(createPartitionKeyGroupKey(partitionKeyGroupName));
        expectedPartitionValuesDeleteRequest.setExpectedPartitionValues(expectedPartitionValues);
        return expectedPartitionValuesDeleteRequest;
    }

    /**
     * Validates expected partition value information contents against specified arguments.
     *
     * @param expectedPartitionKeyGroupName the expected partition key group name
     * @param expectedExpectedPartitionValue the expected value of the expected partition value
     * @param actualExpectedPartitionValueInformation the expected partition value information to be validated
     */
    protected void validateExpectedPartitionValueInformation(String expectedPartitionKeyGroupName, String expectedExpectedPartitionValue,
        ExpectedPartitionValueInformation actualExpectedPartitionValueInformation)
    {
        assertNotNull(actualExpectedPartitionValueInformation);
        assertEquals(expectedPartitionKeyGroupName, actualExpectedPartitionValueInformation.getExpectedPartitionValueKey().getPartitionKeyGroupName());
        assertEquals(expectedExpectedPartitionValue, actualExpectedPartitionValueInformation.getExpectedPartitionValueKey().getExpectedPartitionValue());
    }

    /**
     * Validates expected partition values information contents against specified arguments.
     *
     * @param expectedPartitionKeyGroupName the expected partition key group name
     * @param expectedExpectedPartitionValues the expected list of expected partition values
     * @param actualExpectedPartitionValuesInformation the expected partition values information to be validated
     */
    protected void validateExpectedPartitionValuesInformation(String expectedPartitionKeyGroupName, List<String> expectedExpectedPartitionValues,
        ExpectedPartitionValuesInformation actualExpectedPartitionValuesInformation)
    {
        assertNotNull(actualExpectedPartitionValuesInformation);
        assertEquals(expectedPartitionKeyGroupName, actualExpectedPartitionValuesInformation.getPartitionKeyGroupKey().getPartitionKeyGroupName());
        assertEquals(expectedExpectedPartitionValues, actualExpectedPartitionValuesInformation.getExpectedPartitionValues());
    }

    /**
     * Returns an unsorted list of test expected partition values.
     *
     * @return the unsorted list of expected partition values
     */
    protected List<String> getTestUnsortedExpectedPartitionValues(int count)
    {
        List<String> expectedPartitionValues = new ArrayList<>();

        for (int i = 0; i < count; i++)
        {
            expectedPartitionValues.add(String.format("%d%s", i, RANDOM_SUFFIX));
        }

        return expectedPartitionValues;
    }

    /**
     * Returns a sorted list of test expected partition values.
     *
     * @return the list of expected partition values in ascending order
     */
    protected List<String> getTestSortedExpectedPartitionValues(int count)
    {
        List<String> expectedPartitionValues = getTestUnsortedExpectedPartitionValues(count);
        Collections.sort(expectedPartitionValues);
        return expectedPartitionValues;
    }

    /**
     * Validates a download single initiation response as compared to the upload initiation response.
     *
     * @param uploadSingleInitiationResponse the upload single initiation response.
     * @param downloadSingleInitiationResponse the download single initiation response.
     */
    protected void validateDownloadSingleInitiationResponse(UploadSingleInitiationResponse uploadSingleInitiationResponse,
        DownloadSingleInitiationResponse downloadSingleInitiationResponse)
    {
        BusinessObjectData targetBusinessObjectData = uploadSingleInitiationResponse.getTargetBusinessObjectData();

        validateDownloadSingleInitiationResponse(targetBusinessObjectData.getNamespace(), targetBusinessObjectData.getBusinessObjectDefinitionName(),
            targetBusinessObjectData.getBusinessObjectFormatUsage(), targetBusinessObjectData.getBusinessObjectFormatFileType(),
            targetBusinessObjectData.getBusinessObjectFormatVersion(), targetBusinessObjectData.getAttributes(),
            targetBusinessObjectData.getStorageUnits().get(0).getStorageFiles().get(0).getFileSizeBytes(), downloadSingleInitiationResponse);
    }

    /**
     * Validates upload single initiation response contents against specified parameters.
     *
     * @param expectedSourceNamespaceCode the expected source namespace code
     * @param expectedSourceBusinessObjectDefinitionName the expected source business object definition name
     * @param expectedSourceBusinessObjectFormatUsage the expected source business object format usage
     * @param expectedSourceBusinessObjectFormatFileType the expected source business object format file type
     * @param expectedSourceBusinessObjectFormatVersion the expected source business object format version
     * @param expectedTargetNamespaceCode the expected target namespace code
     * @param expectedTargetBusinessObjectDefinitionName the expected target business object definition name
     * @param expectedTargetBusinessObjectFormatUsage the expected target business object format usage
     * @param expectedTargetBusinessObjectFormatFileType the expected target business object format file type
     * @param expectedTargetBusinessObjectFormatVersion the expected target business object format version
     * @param expectedAttributes the expected business object data attributes
     * @param expectedFileName the expected file name
     * @param expectedFileSizeBytes the expected file size in bytes
     * @param actualUploadSingleInitiationResponse the upload single initiation response to be validated
     */
    protected void validateUploadSingleInitiationResponse(String expectedSourceNamespaceCode, String expectedSourceBusinessObjectDefinitionName,
        String expectedSourceBusinessObjectFormatUsage, String expectedSourceBusinessObjectFormatFileType, Integer expectedSourceBusinessObjectFormatVersion,
        String expectedTargetNamespaceCode, String expectedTargetBusinessObjectDefinitionName, String expectedTargetBusinessObjectFormatUsage,
        String expectedTargetBusinessObjectFormatFileType, Integer expectedTargetBusinessObjectFormatVersion, List<Attribute> expectedAttributes,
        String expectedFileName, Long expectedFileSizeBytes, UploadSingleInitiationResponse actualUploadSingleInitiationResponse)
    {
        assertNotNull(actualUploadSingleInitiationResponse);

        // Validate source business object data.
        validateBusinessObjectData(expectedSourceNamespaceCode, expectedSourceBusinessObjectDefinitionName, expectedSourceBusinessObjectFormatUsage,
            expectedSourceBusinessObjectFormatFileType, expectedSourceBusinessObjectFormatVersion, BusinessObjectDataStatusEntity.UPLOADING, expectedAttributes,
            StorageEntity.MANAGED_LOADING_DOCK_STORAGE, expectedFileName, expectedFileSizeBytes,
            actualUploadSingleInitiationResponse.getSourceBusinessObjectData());

        // Validate target business object data.
        validateBusinessObjectData(expectedTargetNamespaceCode, expectedTargetBusinessObjectDefinitionName, expectedTargetBusinessObjectFormatUsage,
            expectedTargetBusinessObjectFormatFileType, expectedTargetBusinessObjectFormatVersion, BusinessObjectDataStatusEntity.UPLOADING, expectedAttributes,
            StorageEntity.MANAGED_EXTERNAL_STORAGE, expectedFileName, expectedFileSizeBytes,
            actualUploadSingleInitiationResponse.getTargetBusinessObjectData());

        // Validate the file element.
        assertNotNull(actualUploadSingleInitiationResponse.getFile());
        assertEquals(expectedFileName, actualUploadSingleInitiationResponse.getFile().getFileName());
        assertEquals(expectedFileSizeBytes, actualUploadSingleInitiationResponse.getFile().getFileSizeBytes());

        // Validate the source uuid element.
        assertEquals(actualUploadSingleInitiationResponse.getSourceBusinessObjectData().getPartitionValue(), actualUploadSingleInitiationResponse.getUuid());

        // Validate the target uuid element.
        assertEquals(actualUploadSingleInitiationResponse.getTargetBusinessObjectData().getPartitionValue(), actualUploadSingleInitiationResponse.getUuid());

        // Validate temporary security credentials.
        assertEquals(MockStsOperationsImpl.MOCK_AWS_ASSUMED_ROLE_ACCESS_KEY, actualUploadSingleInitiationResponse.getAwsAccessKey());
        assertEquals(MockStsOperationsImpl.MOCK_AWS_ASSUMED_ROLE_SECRET_KEY, actualUploadSingleInitiationResponse.getAwsSecretKey());
        assertEquals(MockStsOperationsImpl.MOCK_AWS_ASSUMED_ROLE_SESSION_TOKEN, actualUploadSingleInitiationResponse.getAwsSessionToken());

        // Validate KMS Key ID.
        assertEquals(dmStringHelper.getRequiredConfigurationValue(ConfigurationValue.AWS_KMS_LOADING_DOCK_KEY_ID),
            actualUploadSingleInitiationResponse.getAwsKmsKeyId());
    }

    protected void validateDownloadSingleInitiationResponse(String expectedNamespaceCode, String expectedBusinessObjectDefinitionName,
        String expectedBusinessObjectFormatUsage, String expectedBusinessObjectFormatFileType, Integer expectedBusinessObjectFormatVersion,
        List<Attribute> expectedAttributes, Long expectedFileSizeBytes, DownloadSingleInitiationResponse actualDownloadSingleInitiationResponse)
    {
        assertNotNull(actualDownloadSingleInitiationResponse);

        validateBusinessObjectData(expectedNamespaceCode, expectedBusinessObjectDefinitionName, expectedBusinessObjectFormatUsage,
            expectedBusinessObjectFormatFileType, expectedBusinessObjectFormatVersion, BusinessObjectDataStatusEntity.VALID, expectedAttributes,
            StorageEntity.MANAGED_EXTERNAL_STORAGE, FILE_NAME, expectedFileSizeBytes, actualDownloadSingleInitiationResponse.getBusinessObjectData());

        assertNotNull("aws access key", actualDownloadSingleInitiationResponse.getAwsAccessKey());
        assertNotNull("aws secret key", actualDownloadSingleInitiationResponse.getAwsSecretKey());
        assertNotNull("aws session token", actualDownloadSingleInitiationResponse.getAwsSessionToken());
    }

    protected void validateBusinessObjectData(String expectedNamespaceCode, String expectedBusinessObjectDefinitionName,
        String expectedBusinessObjectFormatUsage, String expectedBusinessObjectFormatFileType, Integer expectedBusinessObjectFormatVersion,
        String expectedBusinessObjectDataStatus, List<Attribute> expectedAttributes, String expectedStorageName, String expectedFileName,
        Long expectedFileSizeBytes, BusinessObjectData businessObjectData)
    {
        assertNotNull(businessObjectData);

        // Validate business object data alternate key values.
        assertEquals(expectedNamespaceCode, businessObjectData.getNamespace());
        assertEquals(expectedBusinessObjectDefinitionName, businessObjectData.getBusinessObjectDefinitionName());
        assertEquals(expectedBusinessObjectFormatUsage, businessObjectData.getBusinessObjectFormatUsage());
        assertEquals(expectedBusinessObjectFormatFileType, businessObjectData.getBusinessObjectFormatFileType());
        assertEquals(expectedBusinessObjectFormatVersion, Integer.valueOf(businessObjectData.getBusinessObjectFormatVersion()));
        // The business object data partition value must contain an UUID value.
        assertNotNull(businessObjectData.getPartitionValue());
        assertEquals(EXPECTED_UUID_SIZE, businessObjectData.getPartitionValue().length());
        assertEquals(NO_SUBPARTITION_VALUES, businessObjectData.getSubPartitionValues());
        assertEquals(INITIAL_DATA_VERSION, Integer.valueOf(businessObjectData.getVersion()));

        // Validate business object data status.
        assertTrue(businessObjectData.isLatestVersion());
        assertEquals(expectedBusinessObjectDataStatus, businessObjectData.getStatus());

        // Validate business object data attributes.
        validateAttributes(expectedAttributes, businessObjectData.getAttributes());

        // Validate storage unit contents.
        assertEquals(1, businessObjectData.getStorageUnits().size());
        StorageUnit storageUnit = businessObjectData.getStorageUnits().get(0);
        assertEquals(expectedStorageName, storageUnit.getStorage().getName());
        String expectedStorageDirectoryPath = String
            .format("%s/%s/%s", ENVIRONMENT_NAME.trim().toLowerCase().replace('_', '-'), NAMESPACE_CD.trim().toLowerCase().replace('_', '-'),
                businessObjectData.getPartitionValue());
        assertEquals(expectedStorageDirectoryPath, storageUnit.getStorageDirectory().getDirectoryPath());
        assertEquals(1, storageUnit.getStorageFiles().size());
        StorageFile storageFile = storageUnit.getStorageFiles().get(0);
        String expectedStorageFilePath = String.format("%s/%s", expectedStorageDirectoryPath, expectedFileName);
        assertEquals(expectedStorageFilePath, storageFile.getFilePath());
        assertEquals(expectedFileSizeBytes, storageFile.getFileSizeBytes());
        assertEquals(null, storageFile.getRowCount());
    }

    /**
     * Creates the appropriate business object data entries for an upload.
     *
     * @param businessObjectDataStatusCode the target business object data status.
     *
     * @return the upload single initiation response created during the upload flow.
     */
    protected UploadSingleInitiationResponse createUploadedFileData(String businessObjectDataStatusCode)
    {
        Logger.getLogger(UploadDownloadHelperServiceImpl.class).setLevel(Level.OFF);

        // Create source and target business object formats database entities which are required to initiate an upload.
        createDatabaseEntitiesForUploadDownloadTesting();

        // Initiate a file upload.
        UploadSingleInitiationResponse resultUploadSingleInitiationResponse = uploadDownloadService.initiateUploadSingle(createUploadSingleInitiationRequest());

        // Complete the upload.
        uploadDownloadService.performCompleteUploadSingleMessage(
            resultUploadSingleInitiationResponse.getSourceBusinessObjectData().getStorageUnits().get(0).getStorageFiles().get(0).getFilePath());

        // Update the target business object data status to valid. Normally this would happen as part of the completion request, but since the status update
        // happens asynchronously, this will not happen within a unit test context which is why we are setting it explicitly.
        dmDao.getBusinessObjectDataByAltKey(dmHelper.getBusinessObjectDataKey(resultUploadSingleInitiationResponse.getTargetBusinessObjectData()))
            .setStatus(dmDao.getBusinessObjectDataStatusByCode(businessObjectDataStatusCode));
        resultUploadSingleInitiationResponse.getTargetBusinessObjectData().setStatus(businessObjectDataStatusCode);

        // Return the initiate upload single response.
        return resultUploadSingleInitiationResponse;
    }

    /**
     * Creates a system job run request.
     *
     * @param jobName the system job name
     * @param parameters the list of parameters
     *
     * @return the newly created upload single initiation request
     */
    protected SystemJobRunRequest createSystemJobRunRequest(String jobName, List<Parameter> parameters)
    {
        SystemJobRunRequest request = new SystemJobRunRequest();
        request.setJobName(jobName);
        request.setParameters(parameters);
        return request;
    }

    /**
     * Validates system job run response contents against specified parameters.
     *
     * @param expectedJobName the expected system job name
     * @param expectedParameters the expected parameters
     * @param actualSystemJobRunResponse the system job run response to be validated
     */
    protected void validateSystemJobRunResponse(String expectedJobName, List<Parameter> expectedParameters, SystemJobRunResponse actualSystemJobRunResponse)
    {
        assertNotNull(actualSystemJobRunResponse);
        assertEquals(expectedJobName, actualSystemJobRunResponse.getJobName());
        assertEquals(expectedParameters, actualSystemJobRunResponse.getParameters());
    }

    /**
     * Creates a default JDBC execution request which is guaranteed to work. The request contains a single statement of UPDATE type.
     *
     * @return a valid JDBC request
     */
    protected JdbcExecutionRequest createDefaultUpdateJdbcExecutionRequest()
    {
        JdbcConnection jdbcConnection = createDefaultJdbcConnection();
        List<JdbcStatement> jdbcStatements = createDefaultUpdateJdbcStatements();
        JdbcExecutionRequest jdbcExecutionRequest = createJdbcExecutionRequest(jdbcConnection, jdbcStatements);
        return jdbcExecutionRequest;
    }

    /**
     * Creates a default JDBC execution request which is guaranteed to work. The request contains a single statement of QUERY type.
     *
     * @return a valid JDBC request
     */
    protected JdbcExecutionRequest createDefaultQueryJdbcExecutionRequest()
    {
        JdbcConnection jdbcConnection = createDefaultJdbcConnection();
        List<JdbcStatement> jdbcStatements = createDefaultQueryJdbcStatements();
        JdbcExecutionRequest jdbcExecutionRequest = createJdbcExecutionRequest(jdbcConnection, jdbcStatements);
        return jdbcExecutionRequest;
    }

    /**
     * Creates a JDBC request with the specified values.
     *
     * @param jdbcConnection JDBC connection
     * @param jdbcStatements JDBC statements
     *
     * @return an execution request.
     */
    protected JdbcExecutionRequest createJdbcExecutionRequest(JdbcConnection jdbcConnection, List<JdbcStatement> jdbcStatements)
    {
        JdbcExecutionRequest jdbcExecutionRequest = new JdbcExecutionRequest();
        jdbcExecutionRequest.setConnection(jdbcConnection);
        jdbcExecutionRequest.setStatements(jdbcStatements);
        return jdbcExecutionRequest;
    }

    /**
     * Returns a valid list of JDBC UPDATE statements. It contains only 1 statement, and the statement is CASE_1_SQL in mock JDBC (success, result 1)
     *
     * @return list of statements.
     */
    protected List<JdbcStatement> createDefaultUpdateJdbcStatements()
    {
        List<JdbcStatement> jdbcStatements = new ArrayList<>();
        {
            JdbcStatement jdbcStatement = new JdbcStatement();
            jdbcStatement.setType(JdbcStatementType.UPDATE);
            jdbcStatement.setSql(MockJdbcOperations.CASE_1_SQL);
            jdbcStatements.add(jdbcStatement);
        }
        return jdbcStatements;
    }

    /**
     * Returns a valid list of JDBC QUERY statements.It contains only 1 statement, and the statement is CASE_1_SQL in mock JDBC (success, result 1)
     *
     * @return list of statements
     */
    protected List<JdbcStatement> createDefaultQueryJdbcStatements()
    {
        List<JdbcStatement> jdbcStatements = new ArrayList<>();
        {
            JdbcStatement jdbcStatement = new JdbcStatement();
            jdbcStatement.setType(JdbcStatementType.QUERY);
            jdbcStatement.setSql(MockJdbcOperations.CASE_1_SQL);
            jdbcStatements.add(jdbcStatement);
        }
        return jdbcStatements;
    }

    /**
     * Creates a default test JDBC connection which is guaranteed to work. The connection points to the in-memory database setup as part of DAO mocks.
     *
     * @return a valid JDBC connection
     */
    protected JdbcConnection createDefaultJdbcConnection()
    {
        JdbcConnection jdbcConnection = new JdbcConnection();
        jdbcConnection.setUrl("jdbc:h2:mem:dmTestDb");
        jdbcConnection.setUsername("");
        jdbcConnection.setPassword("");
        jdbcConnection.setDatabaseType(JdbcDatabaseType.POSTGRES);
        return jdbcConnection;
    }

    /**
     * Creates and persists {@link BusinessObjectFormatEntity} from the given request. Also creates and persists namespace, data provider, bdef, and file type
     * required for the format. If the request has sub-partitions, schema columns will be persisted. Otherwise, no schema will be set for this format.
     *
     * @param request {@link BusinessObjectDataInvalidateUnregisteredRequest} format alt key
     *
     * @return created {@link BusinessObjectFormatEntity}
     */
    protected BusinessObjectFormatEntity createBusinessObjectFormat(BusinessObjectDataInvalidateUnregisteredRequest request)
    {
        // Create namespace
        NamespaceEntity namespaceEntity = createNamespaceEntity(request.getNamespace());

        // Create data provider with a name which is irrelevant for the test cases
        DataProviderEntity dataProviderEntity = createDataProviderEntity(DATA_PROVIDER_NAME);

        // Create business object definition
        BusinessObjectDefinitionEntity businessObjectDefinitionEntity =
            createBusinessObjectDefinitionEntity(namespaceEntity, request.getBusinessObjectDefinitionName(), dataProviderEntity, null, null, null);

        // Create file type
        FileTypeEntity fileTypeEntity = createFileTypeEntity(request.getBusinessObjectFormatFileType());

        // Manually creating format since it is easier than providing large amounts of params to existing method
        // Create format
        BusinessObjectFormatEntity businessObjectFormatEntity = new BusinessObjectFormatEntity();
        businessObjectFormatEntity.setBusinessObjectDefinition(businessObjectDefinitionEntity);
        businessObjectFormatEntity.setUsage(request.getBusinessObjectFormatUsage());
        businessObjectFormatEntity.setFileType(fileTypeEntity);
        businessObjectFormatEntity.setBusinessObjectFormatVersion(request.getBusinessObjectFormatVersion());
        // If sub-partition values exist in the request
        if (!CollectionUtils.isEmpty(request.getSubPartitionValues()))
        {
            // Create schema columns
            List<SchemaColumnEntity> schemaColumnEntities = new ArrayList<>();
            for (int partitionLevel = 0; partitionLevel < request.getSubPartitionValues().size() + 1; partitionLevel++)
            {
                SchemaColumnEntity schemaColumnEntity = new SchemaColumnEntity();
                schemaColumnEntity.setBusinessObjectFormat(businessObjectFormatEntity);
                schemaColumnEntity.setName(PARTITION_KEY + partitionLevel);
                schemaColumnEntity.setType("STRING");
                schemaColumnEntity.setPartitionLevel(partitionLevel);
                schemaColumnEntity.setPosition(partitionLevel);
                schemaColumnEntities.add(schemaColumnEntity);
            }
            businessObjectFormatEntity.setSchemaColumns(schemaColumnEntities);
            businessObjectFormatEntity.setPartitionKey(PARTITION_KEY + "0");
        }
        // If sub-partition values do not exist in the request
        else
        {
            businessObjectFormatEntity.setPartitionKey(PARTITION_KEY);
        }
        businessObjectFormatEntity.setLatestVersion(true);
        dmDao.saveAndRefresh(businessObjectFormatEntity);

        return businessObjectFormatEntity;
    }

    /**
     * Creates a default valid {@link BusinessObjectDataInvalidateUnregisteredRequest} with all parameters except sub-partitions.
     *
     * @return {@link BusinessObjectDataInvalidateUnregisteredRequest}
     */
    protected BusinessObjectDataInvalidateUnregisteredRequest getDefaultBusinessObjectDataInvalidateUnregisteredRequest()
    {
        BusinessObjectDataInvalidateUnregisteredRequest request = new BusinessObjectDataInvalidateUnregisteredRequest();
        request.setNamespace(NAMESPACE_CD);
        request.setBusinessObjectDefinitionName(BOD_NAME);
        request.setBusinessObjectFormatUsage(FORMAT_USAGE_CODE);
        request.setBusinessObjectFormatFileType(FORMAT_FILE_TYPE_CODE);
        request.setBusinessObjectFormatVersion(FORMAT_VERSION);
        request.setPartitionValue(PARTITION_VALUE);
        request.setStorageName(StorageEntity.MANAGED_STORAGE);
        return request;
    }

    /**
     * Validates that the business object data status change message is valid.
     *
     * @param message the message to be validated.
     * @param businessObjectDataKey the business object data key.
     * @param businessObjectDataId the business object data Id.
     * @param username the username.
     * @param newBusinessObjectDataStatus the new business object data status.
     * @param oldBusinessObjectDataStatus the old business object data status.
     */
    protected void validateBusinessObjectDataStatusChangeMessage(String message, BusinessObjectDataKey businessObjectDataKey, Integer businessObjectDataId,
        String username, String newBusinessObjectDataStatus, String oldBusinessObjectDataStatus)
    {
        validateXmlFieldPresent(message, "correlation-id", "BusinessObjectData_" + businessObjectDataId);
        validateXmlFieldPresent(message, "triggered-by-username", username);
        validateXmlFieldPresent(message, "context-message-type", "testDomain/datamanagement/BusinessObjectDataStatusChanged");
        validateXmlFieldPresent(message, "newBusinessObjectDataStatus", newBusinessObjectDataStatus);

        if (oldBusinessObjectDataStatus == null)
        {
            validateXmlFieldNotPresent(message, "oldBusinessObjectDataStatus");
        }
        else
        {
            validateXmlFieldPresent(message, "oldBusinessObjectDataStatus", oldBusinessObjectDataStatus);
        }

        validateXmlFieldPresent(message, "namespace", businessObjectDataKey.getNamespace());
        validateXmlFieldPresent(message, "businessObjectDefinitionName", businessObjectDataKey.getBusinessObjectDefinitionName());
        validateXmlFieldPresent(message, "businessObjectFormatUsage", businessObjectDataKey.getBusinessObjectFormatUsage());
        validateXmlFieldPresent(message, "businessObjectFormatFileType", businessObjectDataKey.getBusinessObjectFormatFileType());
        validateXmlFieldPresent(message, "businessObjectFormatVersion", businessObjectDataKey.getBusinessObjectFormatVersion());
        validateXmlFieldPresent(message, "partitionValue", businessObjectDataKey.getPartitionValue());

        if (org.apache.commons.collections.CollectionUtils.isEmpty(businessObjectDataKey.getSubPartitionValues()))
        {
            assertTrue("<subPartitionValues> tag found, but not expected.", !message.contains("<subPartitionValues>"));
        }
        for (String subPartitionValue : businessObjectDataKey.getSubPartitionValues())
        {
            validateXmlFieldPresent(message, "partitionValue", subPartitionValue);
        }

        validateXmlFieldPresent(message, "businessObjectDataVersion", businessObjectDataKey.getBusinessObjectDataVersion());
    }

    /**
     * Validates that a specified XML tag and value are present in the message.
     *
     * @param message the XML message.
     * @param xmlTagName the XML tag name (without the < and > characters).
     * @param value the value of the data for the tag.
     */
    private void validateXmlFieldPresent(String message, String xmlTagName, Object value)
    {
        assertTrue(xmlTagName + " \"" + value + "\" expected, but not found.",
            message.contains("<" + xmlTagName + ">" + (value == null ? null : value.toString()) + "</" + xmlTagName + ">"));
    }

    /**
     * Validates that a specified XML tag is not present in the message.
     *
     * @param message the XML message.
     * @param xmlTagName the XML tag name (without the < and > characters).
     */
    private void validateXmlFieldNotPresent(String message, String xmlTagName)
    {
        assertTrue("\"" + xmlTagName + " \" tag not expected, but found.", !message.contains("<" + xmlTagName + ">"));
    }

    /**
     * Returns a list of all possible invalid partition filters based on the presence of partition value filter elements. This helper method is for all negative
     * test cases covering partition value filter having none or more than one partition filter option specified.
     */
    protected List<PartitionValueFilter> getInvalidPartitionValueFilters()
    {
        return Arrays.asList(new PartitionValueFilter(PARTITION_KEY, NO_PARTITION_VALUES, NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
            NO_LATEST_AFTER_PARTITION_VALUE),
            new PartitionValueFilter(PARTITION_KEY, NO_PARTITION_VALUES, NO_PARTITION_VALUE_RANGE, new LatestBeforePartitionValue(),
                new LatestAfterPartitionValue()),
            new PartitionValueFilter(PARTITION_KEY, NO_PARTITION_VALUES, new PartitionValueRange(START_PARTITION_VALUE, END_PARTITION_VALUE),
                NO_LATEST_BEFORE_PARTITION_VALUE, new LatestAfterPartitionValue()),
            new PartitionValueFilter(PARTITION_KEY, NO_PARTITION_VALUES, new PartitionValueRange(START_PARTITION_VALUE, END_PARTITION_VALUE),
                new LatestBeforePartitionValue(), NO_LATEST_AFTER_PARTITION_VALUE),
            new PartitionValueFilter(PARTITION_KEY, NO_PARTITION_VALUES, new PartitionValueRange(START_PARTITION_VALUE, END_PARTITION_VALUE),
                new LatestBeforePartitionValue(), new LatestAfterPartitionValue()),
            new PartitionValueFilter(PARTITION_KEY, UNSORTED_PARTITION_VALUES, NO_PARTITION_VALUE_RANGE, NO_LATEST_BEFORE_PARTITION_VALUE,
                new LatestAfterPartitionValue()),
            new PartitionValueFilter(PARTITION_KEY, UNSORTED_PARTITION_VALUES, NO_PARTITION_VALUE_RANGE, new LatestBeforePartitionValue(),
                NO_LATEST_AFTER_PARTITION_VALUE),
            new PartitionValueFilter(PARTITION_KEY, UNSORTED_PARTITION_VALUES, NO_PARTITION_VALUE_RANGE, new LatestBeforePartitionValue(),
                new LatestAfterPartitionValue()),
            new PartitionValueFilter(PARTITION_KEY, UNSORTED_PARTITION_VALUES, new PartitionValueRange(START_PARTITION_VALUE, END_PARTITION_VALUE),
                NO_LATEST_BEFORE_PARTITION_VALUE, NO_LATEST_AFTER_PARTITION_VALUE),
            new PartitionValueFilter(PARTITION_KEY, UNSORTED_PARTITION_VALUES, new PartitionValueRange(START_PARTITION_VALUE, END_PARTITION_VALUE),
                NO_LATEST_BEFORE_PARTITION_VALUE, new LatestAfterPartitionValue()),
            new PartitionValueFilter(PARTITION_KEY, UNSORTED_PARTITION_VALUES, new PartitionValueRange(START_PARTITION_VALUE, END_PARTITION_VALUE),
                new LatestBeforePartitionValue(), NO_LATEST_AFTER_PARTITION_VALUE),
            new PartitionValueFilter(PARTITION_KEY, UNSORTED_PARTITION_VALUES, new PartitionValueRange(START_PARTITION_VALUE, END_PARTITION_VALUE),
                new LatestBeforePartitionValue(), new LatestAfterPartitionValue()));
    }
}
