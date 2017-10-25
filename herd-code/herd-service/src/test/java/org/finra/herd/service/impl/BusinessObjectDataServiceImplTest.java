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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.finra.herd.core.helper.ConfigurationHelper;
import org.finra.herd.dao.BusinessObjectDataDao;
import org.finra.herd.dao.StorageUnitDao;
import org.finra.herd.dao.helper.JsonHelper;
import org.finra.herd.model.api.xml.Attribute;
import org.finra.herd.model.api.xml.BusinessObjectData;
import org.finra.herd.model.api.xml.BusinessObjectDataAttributesUpdateRequest;
import org.finra.herd.model.api.xml.BusinessObjectDataKey;
import org.finra.herd.model.jpa.BusinessObjectDataAttributeDefinitionEntity;
import org.finra.herd.model.jpa.BusinessObjectDataEntity;
import org.finra.herd.model.jpa.BusinessObjectFormatEntity;
import org.finra.herd.service.AbstractServiceTest;
import org.finra.herd.service.BusinessObjectDataInitiateRestoreHelperService;
import org.finra.herd.service.NotificationEventService;
import org.finra.herd.service.S3Service;
import org.finra.herd.service.helper.AttributeDaoHelper;
import org.finra.herd.service.helper.AttributeHelper;
import org.finra.herd.service.helper.BusinessObjectDataDaoHelper;
import org.finra.herd.service.helper.BusinessObjectDataHelper;
import org.finra.herd.service.helper.BusinessObjectDataInvalidateUnregisteredHelper;
import org.finra.herd.service.helper.BusinessObjectDataRetryStoragePolicyTransitionHelper;
import org.finra.herd.service.helper.BusinessObjectDataSearchHelper;
import org.finra.herd.service.helper.BusinessObjectDataStatusDaoHelper;
import org.finra.herd.service.helper.BusinessObjectDefinitionDaoHelper;
import org.finra.herd.service.helper.BusinessObjectDefinitionHelper;
import org.finra.herd.service.helper.BusinessObjectFormatDaoHelper;
import org.finra.herd.service.helper.BusinessObjectFormatHelper;
import org.finra.herd.service.helper.CustomDdlDaoHelper;
import org.finra.herd.service.helper.DdlGeneratorFactory;
import org.finra.herd.service.helper.S3KeyPrefixHelper;
import org.finra.herd.service.helper.StorageDaoHelper;
import org.finra.herd.service.helper.StorageHelper;
import org.finra.herd.service.helper.StorageUnitHelper;

/**
 * This class tests functionality within the business object data service implementation.
 */
public class BusinessObjectDataServiceImplTest extends AbstractServiceTest
{
    @Mock
    private AttributeDaoHelper attributeDaoHelper;

    @Mock
    private AttributeHelper attributeHelper;

    @Mock
    private BusinessObjectDataDao businessObjectDataDao;

    @Mock
    private BusinessObjectDataDaoHelper businessObjectDataDaoHelper;

    @Mock
    private BusinessObjectDataHelper businessObjectDataHelper;

    @Mock
    private BusinessObjectDataInitiateRestoreHelperService businessObjectDataInitiateRestoreHelperService;

    @Mock
    private BusinessObjectDataInvalidateUnregisteredHelper businessObjectDataInvalidateUnregisteredHelper;

    @Mock
    private BusinessObjectDataRetryStoragePolicyTransitionHelper businessObjectDataRetryStoragePolicyTransitionHelper;

    @Mock
    private BusinessObjectDataSearchHelper businessObjectDataSearchHelper;

    @InjectMocks
    private BusinessObjectDataServiceImpl businessObjectDataServiceImpl;

    @Mock
    private BusinessObjectDataStatusDaoHelper businessObjectDataStatusDaoHelper;

    @Mock
    private BusinessObjectDefinitionDaoHelper businessObjectDefinitionDaoHelper;

    @Mock
    private BusinessObjectDefinitionHelper businessObjectDefinitionHelper;

    @Mock
    private BusinessObjectFormatDaoHelper businessObjectFormatDaoHelper;

    @Mock
    private BusinessObjectFormatHelper businessObjectFormatHelper;

    @Mock
    private ConfigurationHelper configurationHelper;

    @Mock
    private CustomDdlDaoHelper customDdlDaoHelper;

    @Mock
    private DdlGeneratorFactory ddlGeneratorFactory;

    @Mock
    private JsonHelper jsonHelper;

    @Mock
    private NotificationEventService notificationEventService;

    @Mock
    private S3KeyPrefixHelper s3KeyPrefixHelper;

    @Mock
    private S3Service s3Service;

    @Mock
    private StorageDaoHelper storageDaoHelper;

    @Mock
    private StorageHelper storageHelper;

    @Mock
    private StorageUnitDao storageUnitDao;

    @Mock
    private StorageUnitHelper storageUnitHelper;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUpdateBusinessObjectDataAttributes()
    {
        // Create a business object data key.
        BusinessObjectDataKey businessObjectDataKey =
            new BusinessObjectDataKey(BDEF_NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, SUBPARTITION_VALUES,
                DATA_VERSION);

        // Create a list of attributes.
        List<Attribute> attributes = Arrays.asList(new Attribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE));

        // Create a business object data attributes update request.
        BusinessObjectDataAttributesUpdateRequest businessObjectDataAttributesUpdateRequest = new BusinessObjectDataAttributesUpdateRequest(attributes);

        // Create a list of attribute definitions.
        List<BusinessObjectDataAttributeDefinitionEntity> attributeDefinitionEntities = Arrays.asList(new BusinessObjectDataAttributeDefinitionEntity());

        // Create a business object format definition.
        BusinessObjectFormatEntity businessObjectFormatEntity = new BusinessObjectFormatEntity();
        businessObjectFormatEntity.setAttributeDefinitions(attributeDefinitionEntities);

        // Create a business object data entity.
        BusinessObjectDataEntity businessObjectDataEntity = new BusinessObjectDataEntity();
        businessObjectDataEntity.setBusinessObjectFormat(businessObjectFormatEntity);

        // Create a business object data.
        BusinessObjectData businessObjectData = new BusinessObjectData();
        businessObjectData.setId(ID);

        // Mock the external calls.
        when(businessObjectDataDaoHelper.getBusinessObjectDataEntity(businessObjectDataKey)).thenReturn(businessObjectDataEntity);
        when(businessObjectDataDao.saveAndRefresh(businessObjectDataEntity)).thenReturn(businessObjectDataEntity);
        when(businessObjectDataHelper.createBusinessObjectDataFromEntity(businessObjectDataEntity)).thenReturn(businessObjectData);

        // Call the method under test.
        BusinessObjectData result =
            businessObjectDataServiceImpl.updateBusinessObjectDataAttributes(businessObjectDataKey, businessObjectDataAttributesUpdateRequest);

        // Verify the external calls.
        verify(businessObjectDataHelper).validateBusinessObjectDataKey(businessObjectDataKey, true, true);
        verify(attributeHelper).validateAttributes(attributes);
        verify(businessObjectDataDaoHelper).getBusinessObjectDataEntity(businessObjectDataKey);
        verify(attributeDaoHelper).validateAttributesAgainstBusinessObjectDataAttributeDefinitions(attributes, attributeDefinitionEntities);
        verify(attributeDaoHelper).updateBusinessObjectDataAttributes(businessObjectDataEntity, attributes);
        verify(businessObjectDataDao).saveAndRefresh(businessObjectDataEntity);
        verify(businessObjectDataHelper).createBusinessObjectDataFromEntity(businessObjectDataEntity);
        verifyNoMoreInteractionsHelper();

        // Validate the results.
        assertEquals(businessObjectData, result);
    }

    @Test
    public void testUpdateBusinessObjectDataAttributesMissingRequiredParameters()
    {
        // Create a business object data key.
        BusinessObjectDataKey businessObjectDataKey =
            new BusinessObjectDataKey(BDEF_NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, SUBPARTITION_VALUES,
                DATA_VERSION);

        // Try to update business object data attributes when the update request is not specified.
        try
        {
            businessObjectDataServiceImpl.updateBusinessObjectDataAttributes(businessObjectDataKey, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("A business object data attributes update request must be specified."), e.getMessage());
        }

        // Try to update business object data attributes when the list of attributes is not specified.
        try
        {
            businessObjectDataServiceImpl.updateBusinessObjectDataAttributes(businessObjectDataKey, new BusinessObjectDataAttributesUpdateRequest());
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("A list of business object data attributes must be specified."), e.getMessage());
        }

        // Verify the external calls.
        verify(businessObjectDataHelper, times(2)).validateBusinessObjectDataKey(businessObjectDataKey, true, true);
        verifyNoMoreInteractionsHelper();
    }

    /**
     * Checks if any of the mocks has any interaction.
     */
    private void verifyNoMoreInteractionsHelper()
    {
        verifyNoMoreInteractions(attributeDaoHelper, attributeHelper, businessObjectDataDao, businessObjectDataDaoHelper, businessObjectDataHelper,
            businessObjectDataInitiateRestoreHelperService, businessObjectDataInvalidateUnregisteredHelper,
            businessObjectDataRetryStoragePolicyTransitionHelper, businessObjectDataSearchHelper, businessObjectDataStatusDaoHelper,
            businessObjectDefinitionDaoHelper, businessObjectDefinitionHelper, businessObjectFormatDaoHelper, businessObjectFormatHelper, configurationHelper,
            customDdlDaoHelper, ddlGeneratorFactory, jsonHelper, notificationEventService, s3KeyPrefixHelper, s3Service, storageDaoHelper, storageHelper,
            storageUnitDao, storageUnitHelper);
    }
}
