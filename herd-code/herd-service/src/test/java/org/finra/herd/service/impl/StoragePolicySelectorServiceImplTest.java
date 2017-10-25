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
package org.finra.herd.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.finra.herd.dao.BusinessObjectDataDao;
import org.finra.herd.dao.SqsDao;
import org.finra.herd.dao.helper.AwsHelper;
import org.finra.herd.dao.helper.HerdStringHelper;
import org.finra.herd.dao.helper.JsonHelper;
import org.finra.herd.dao.impl.HerdDaoImpl;
import org.finra.herd.model.dto.ConfigurationValue;
import org.finra.herd.model.dto.StoragePolicySelection;
import org.finra.herd.model.jpa.BusinessObjectDataEntity;
import org.finra.herd.model.jpa.StoragePolicyEntity;
import org.finra.herd.service.AbstractServiceTest;
import org.finra.herd.service.helper.BusinessObjectDataHelper;

public class StoragePolicySelectorServiceImplTest extends AbstractServiceTest
{
    @Mock
    private AwsHelper awsHelper;

    @Mock
    private BusinessObjectDataDao businessObjectDataDao;

    @Mock
    private BusinessObjectDataHelper businessObjectDataHelper;

    @Mock
    private HerdDaoImpl herdDao;

    @Mock
    private HerdStringHelper herdStringHelper;

    @Mock
    private JsonHelper jsonHelper;

    @Mock
    private SqsDao sqsDao;

    @InjectMocks
    private StoragePolicySelectorServiceImpl storagePolicySelectorServiceImpl;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCompleteStoragePolicyTransitionImpl()
    {
        // Create a current timestamp.
        Timestamp currentTimestamp = new Timestamp(LONG_VALUE);

        // Set a number of maximum results.
        Integer maxResults = 10;

        // Create configuration values required for testing.
        Integer updatedOnThresholdInDays = 90;
        Integer storagePolicyTransitionMaxAllowedAttempts = 3;

        // Create an empty mapping of matched business object data entities.
        Map<BusinessObjectDataEntity, StoragePolicyEntity> noMatchingBusinessObjectDataEntities = new HashMap<>();

        // Create an empty list of storage policy selections.
        List<StoragePolicySelection> storagePolicySelections = new ArrayList<>();

        // Mock the external calls.
        when(herdDao.getCurrentTimestamp()).thenReturn(currentTimestamp);
        when(herdStringHelper.getConfigurationValueAsInteger(ConfigurationValue.STORAGE_POLICY_PROCESSOR_BDATA_UPDATED_ON_THRESHOLD_DAYS))
            .thenReturn(updatedOnThresholdInDays);
        when(businessObjectDataDao.getBusinessObjectDataEntitiesMatchingStoragePolicies(StoragePolicySelectorServiceImpl.STORAGE_POLICY_PRIORITY_LEVELS.get(0),
            StoragePolicySelectorServiceImpl.SUPPORTED_BUSINESS_OBJECT_DATA_STATUSES, storagePolicyTransitionMaxAllowedAttempts, 0, maxResults))
            .thenReturn(noMatchingBusinessObjectDataEntities);
        when(businessObjectDataDao.getBusinessObjectDataEntitiesMatchingStoragePolicies(StoragePolicySelectorServiceImpl.STORAGE_POLICY_PRIORITY_LEVELS.get(1),
            StoragePolicySelectorServiceImpl.SUPPORTED_BUSINESS_OBJECT_DATA_STATUSES, storagePolicyTransitionMaxAllowedAttempts, 0, maxResults))
            .thenReturn(noMatchingBusinessObjectDataEntities);
        when(businessObjectDataDao.getBusinessObjectDataEntitiesMatchingStoragePolicies(StoragePolicySelectorServiceImpl.STORAGE_POLICY_PRIORITY_LEVELS.get(2),
            StoragePolicySelectorServiceImpl.SUPPORTED_BUSINESS_OBJECT_DATA_STATUSES, storagePolicyTransitionMaxAllowedAttempts, 0, maxResults))
            .thenReturn(noMatchingBusinessObjectDataEntities);
        when(businessObjectDataDao.getBusinessObjectDataEntitiesMatchingStoragePolicies(StoragePolicySelectorServiceImpl.STORAGE_POLICY_PRIORITY_LEVELS.get(3),
            StoragePolicySelectorServiceImpl.SUPPORTED_BUSINESS_OBJECT_DATA_STATUSES, storagePolicyTransitionMaxAllowedAttempts, 0, maxResults))
            .thenReturn(noMatchingBusinessObjectDataEntities);
        when(herdStringHelper.getConfigurationValueAsInteger(ConfigurationValue.STORAGE_POLICY_TRANSITION_MAX_ALLOWED_ATTEMPTS))
            .thenReturn(storagePolicyTransitionMaxAllowedAttempts);

        // Call the method under test.
        List<StoragePolicySelection> result = storagePolicySelectorServiceImpl.execute(AWS_SQS_QUEUE_NAME, maxResults);

        // Verify the external calls.
        verify(herdDao).getCurrentTimestamp();
        verify(herdStringHelper).getConfigurationValueAsInteger(ConfigurationValue.STORAGE_POLICY_PROCESSOR_BDATA_UPDATED_ON_THRESHOLD_DAYS);
        verify(herdStringHelper).getConfigurationValueAsInteger(ConfigurationValue.STORAGE_POLICY_TRANSITION_MAX_ALLOWED_ATTEMPTS);
        verify(businessObjectDataDao)
            .getBusinessObjectDataEntitiesMatchingStoragePolicies(StoragePolicySelectorServiceImpl.STORAGE_POLICY_PRIORITY_LEVELS.get(0),
                StoragePolicySelectorServiceImpl.SUPPORTED_BUSINESS_OBJECT_DATA_STATUSES, storagePolicyTransitionMaxAllowedAttempts, 0, maxResults);
        verify(businessObjectDataDao)
            .getBusinessObjectDataEntitiesMatchingStoragePolicies(StoragePolicySelectorServiceImpl.STORAGE_POLICY_PRIORITY_LEVELS.get(1),
                StoragePolicySelectorServiceImpl.SUPPORTED_BUSINESS_OBJECT_DATA_STATUSES, storagePolicyTransitionMaxAllowedAttempts, 0, maxResults);
        verify(businessObjectDataDao)
            .getBusinessObjectDataEntitiesMatchingStoragePolicies(StoragePolicySelectorServiceImpl.STORAGE_POLICY_PRIORITY_LEVELS.get(2),
                StoragePolicySelectorServiceImpl.SUPPORTED_BUSINESS_OBJECT_DATA_STATUSES, storagePolicyTransitionMaxAllowedAttempts, 0, maxResults);
        verify(businessObjectDataDao)
            .getBusinessObjectDataEntitiesMatchingStoragePolicies(StoragePolicySelectorServiceImpl.STORAGE_POLICY_PRIORITY_LEVELS.get(3),
                StoragePolicySelectorServiceImpl.SUPPORTED_BUSINESS_OBJECT_DATA_STATUSES, storagePolicyTransitionMaxAllowedAttempts, 0, maxResults);
        verifyNoMoreInteractionsHelper();

        // Validate the results.
        assertEquals(storagePolicySelections, result);
    }

    /**
     * Checks if any of the mocks has any interaction.
     */
    private void verifyNoMoreInteractionsHelper()
    {
        verifyNoMoreInteractions(awsHelper, businessObjectDataDao, businessObjectDataHelper, herdDao, herdStringHelper, jsonHelper, sqsDao);
    }
}
