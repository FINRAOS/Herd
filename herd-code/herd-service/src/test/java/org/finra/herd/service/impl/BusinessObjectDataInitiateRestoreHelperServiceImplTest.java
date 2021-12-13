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

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.Tier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.finra.herd.core.helper.ConfigurationHelper;
import org.finra.herd.dao.StorageUnitDao;
import org.finra.herd.dao.helper.HerdStringHelper;
import org.finra.herd.dao.helper.JsonHelper;
import org.finra.herd.model.api.xml.BusinessObjectDataKey;
import org.finra.herd.model.api.xml.StorageFile;
import org.finra.herd.model.dto.BusinessObjectDataRestoreDto;
import org.finra.herd.model.dto.ConfigurationValue;
import org.finra.herd.model.dto.S3FileTransferRequestParamsDto;
import org.finra.herd.model.jpa.BusinessObjectDataEntity;
import org.finra.herd.model.jpa.BusinessObjectFormatEntity;
import org.finra.herd.model.jpa.StorageEntity;
import org.finra.herd.model.jpa.StoragePlatformEntity;
import org.finra.herd.model.jpa.StorageUnitEntity;
import org.finra.herd.model.jpa.StorageUnitStatusEntity;
import org.finra.herd.service.AbstractServiceTest;
import org.finra.herd.service.S3Service;
import org.finra.herd.service.helper.BusinessObjectDataDaoHelper;
import org.finra.herd.service.helper.BusinessObjectDataHelper;
import org.finra.herd.service.helper.S3KeyPrefixHelper;
import org.finra.herd.service.helper.StorageFileHelper;
import org.finra.herd.service.helper.StorageHelper;
import org.finra.herd.service.helper.StorageUnitDaoHelper;
import org.finra.herd.service.helper.StorageUnitStatusDaoHelper;

/**
 * This class tests functionality within the business object data initiate restore helper service implementation.
 */
public class BusinessObjectDataInitiateRestoreHelperServiceImplTest extends AbstractServiceTest
{
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private BusinessObjectDataDaoHelper businessObjectDataDaoHelper;

    @Mock
    private BusinessObjectDataHelper businessObjectDataHelper;

    @InjectMocks
    private BusinessObjectDataInitiateRestoreHelperServiceImpl businessObjectDataInitiateRestoreHelperServiceImpl;

    @Mock
    private ConfigurationHelper configurationHelper;

    @Mock
    private HerdStringHelper herdStringHelper;

    @Mock
    private JsonHelper jsonHelper;

    @Mock
    private S3KeyPrefixHelper s3KeyPrefixHelper;

    @Mock
    private S3Service s3Service;

    @Mock
    private StorageFileHelper storageFileHelper;

    @Mock
    private StorageHelper storageHelper;

    @Mock
    private StorageUnitDao storageUnitDao;

    @Mock
    private StorageUnitDaoHelper storageUnitDaoHelper;

    @Mock
    private StorageUnitStatusDaoHelper storageUnitStatusDaoHelper;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecuteS3SpecificSteps()
    {
        // Create a business object data key.
        BusinessObjectDataKey businessObjectDataKey =
            new BusinessObjectDataKey(BDEF_NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, SUBPARTITION_VALUES,
                DATA_VERSION);

        // Create a list of storage files to be passed as an input.
        List<StorageFile> storageFiles = Collections.singletonList(new StorageFile(S3_KEY, FILE_SIZE, ROW_COUNT));

        // Create a DTO for business object data restore parameters.
        BusinessObjectDataRestoreDto businessObjectDataRestoreDto =
            new BusinessObjectDataRestoreDto(businessObjectDataKey, STORAGE_NAME, S3_ENDPOINT, S3_BUCKET_NAME, S3_KEY_PREFIX, NO_STORAGE_UNIT_STATUS,
                NO_STORAGE_UNIT_STATUS, storageFiles, NO_EXCEPTION, ARCHIVE_RETRIEVAL_OPTION, NO_BUSINESS_OBJECT_DATA);

        // Create an S3 file transfer parameters DTO to access the S3 bucket.
        S3FileTransferRequestParamsDto initialS3FileTransferRequestParamsDto = new S3FileTransferRequestParamsDto();

        // Create an updated version of the S3 file transfer request parameters DTO.
        S3FileTransferRequestParamsDto updatedS3FileTransferRequestParamsDto = new S3FileTransferRequestParamsDto();
        updatedS3FileTransferRequestParamsDto.setS3BucketName(S3_BUCKET_NAME);
        updatedS3FileTransferRequestParamsDto.setS3Endpoint(S3_ENDPOINT);
        updatedS3FileTransferRequestParamsDto.setS3KeyPrefix(S3_KEY_PREFIX + "/");

        // Create a mock S3 object summary for an S3 object that does belong to Glacier storage class.
        S3ObjectSummary glacierS3ObjectSummary = mock(S3ObjectSummary.class);
        when(glacierS3ObjectSummary.getStorageClass()).thenReturn(StorageClass.Glacier.toString());

        // Create a list of actual S3 files.
        List<S3ObjectSummary> actualS3Files = Collections.singletonList(glacierS3ObjectSummary);

        // Create a list of storage files that represent actual S3 objects.
        List<StorageFile> storageFilesCreatedFromActualS3Files = Collections.singletonList(new StorageFile(S3_KEY, FILE_SIZE, ROW_COUNT));

        // Create a list of files selected for S3 object tagging.
        List<File> filesToBeRestored = Collections.singletonList(new File(S3_KEY));

        // Create a final version of DTO for business object data restore parameters.
        S3FileTransferRequestParamsDto finalS3FileTransferRequestParamsDto = new S3FileTransferRequestParamsDto();
        finalS3FileTransferRequestParamsDto.setS3BucketName(S3_BUCKET_NAME);
        finalS3FileTransferRequestParamsDto.setS3Endpoint(S3_ENDPOINT);
        finalS3FileTransferRequestParamsDto.setS3KeyPrefix(S3_KEY_PREFIX + "/");
        finalS3FileTransferRequestParamsDto.setFiles(filesToBeRestored);

        // Mock the external calls.
        when(storageHelper.getS3FileTransferRequestParamsDto()).thenReturn(initialS3FileTransferRequestParamsDto);
        when(s3Service.listDirectory(updatedS3FileTransferRequestParamsDto, true)).thenReturn(actualS3Files);
        when(storageFileHelper.createStorageFilesFromS3ObjectSummaries(actualS3Files)).thenReturn(storageFilesCreatedFromActualS3Files);
        when(storageFileHelper.getFiles(storageFilesCreatedFromActualS3Files)).thenReturn(filesToBeRestored);

        // Call the method under test.
        businessObjectDataInitiateRestoreHelperServiceImpl.executeS3SpecificSteps(businessObjectDataRestoreDto);

        // Verify the external calls.
        verify(storageHelper).getS3FileTransferRequestParamsDto();
        verify(s3Service).listDirectory(any(S3FileTransferRequestParamsDto.class), eq(true));
        verify(storageFileHelper).validateRegisteredS3Files(storageFiles, actualS3Files, STORAGE_NAME, businessObjectDataKey);
        verify(storageFileHelper).createStorageFilesFromS3ObjectSummaries(actualS3Files);
        verify(storageFileHelper).getFiles(storageFilesCreatedFromActualS3Files);
        verify(s3Service).restoreObjects(finalS3FileTransferRequestParamsDto, 36135, ARCHIVE_RETRIEVAL_OPTION);
        verifyNoMoreInteractionsHelper();

        // Validate the results. The business object data restore DTO is expected not to be updated.
        assertEquals(new BusinessObjectDataRestoreDto(businessObjectDataKey, STORAGE_NAME, S3_ENDPOINT, S3_BUCKET_NAME, S3_KEY_PREFIX, NO_STORAGE_UNIT_STATUS,
            NO_STORAGE_UNIT_STATUS, storageFiles, NO_EXCEPTION, ARCHIVE_RETRIEVAL_OPTION, NO_BUSINESS_OBJECT_DATA), businessObjectDataRestoreDto);
    }

    @Test
    public void testExecuteS3SpecificStepsNonGlacierObjectFound()
    {
        // Create a business object data key.
        BusinessObjectDataKey businessObjectDataKey =
            new BusinessObjectDataKey(BDEF_NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, SUBPARTITION_VALUES,
                DATA_VERSION);

        // Create a list of storage files to be passed as an input.
        List<StorageFile> storageFiles = Collections.singletonList(new StorageFile(S3_KEY, FILE_SIZE, ROW_COUNT));

        // Create a DTO for business object data restore parameters.
        BusinessObjectDataRestoreDto businessObjectDataRestoreDto =
            new BusinessObjectDataRestoreDto(businessObjectDataKey, STORAGE_NAME, S3_ENDPOINT, S3_BUCKET_NAME, S3_KEY_PREFIX, NO_STORAGE_UNIT_STATUS,
                NO_STORAGE_UNIT_STATUS, storageFiles, NO_EXCEPTION, ARCHIVE_RETRIEVAL_OPTION, NO_BUSINESS_OBJECT_DATA);

        // Create an S3 file transfer parameters DTO to access the S3 bucket.
        S3FileTransferRequestParamsDto initialS3FileTransferRequestParamsDto = new S3FileTransferRequestParamsDto();

        // Create an updated version of the S3 file transfer request parameters DTO.
        S3FileTransferRequestParamsDto updatedS3FileTransferRequestParamsDto = new S3FileTransferRequestParamsDto();
        updatedS3FileTransferRequestParamsDto.setS3BucketName(S3_BUCKET_NAME);
        updatedS3FileTransferRequestParamsDto.setS3Endpoint(S3_ENDPOINT);
        updatedS3FileTransferRequestParamsDto.setS3KeyPrefix(S3_KEY_PREFIX + "/");

        // Create a mock S3 object summary for S3 object that does not belong to Glacier storage class.
        S3ObjectSummary standardS3ObjectSummary = mock(S3ObjectSummary.class);
        when(standardS3ObjectSummary.getKey()).thenReturn(S3_KEY);
        when(standardS3ObjectSummary.getStorageClass()).thenReturn(StorageClass.Standard.toString());

        // Create a list of actual S3 files.
        List<S3ObjectSummary> actualS3Files = Collections.singletonList(standardS3ObjectSummary);

        // Mock the external calls.
        when(storageHelper.getS3FileTransferRequestParamsDto()).thenReturn(initialS3FileTransferRequestParamsDto);
        when(s3Service.listDirectory(updatedS3FileTransferRequestParamsDto, true)).thenReturn(actualS3Files);

        // Call the method under test.
        businessObjectDataInitiateRestoreHelperServiceImpl.executeS3SpecificSteps(businessObjectDataRestoreDto);

        // Verify the external calls.
        verify(storageHelper).getS3FileTransferRequestParamsDto();
        verify(s3Service).listDirectory(any(S3FileTransferRequestParamsDto.class), eq(true));
        verify(storageFileHelper).validateRegisteredS3Files(storageFiles, actualS3Files, STORAGE_NAME, businessObjectDataKey);
        verify(jsonHelper).objectToJson(businessObjectDataKey);
        verifyNoMoreInteractionsHelper();

        // Validate the results. The business object data restore DTO is expected to be updated with an exception resulted from a non-Glacier S3 object.
        assertNotNull(businessObjectDataRestoreDto.getException());
        assertEquals(IllegalArgumentException.class, businessObjectDataRestoreDto.getException().getClass());
        assertEquals(String.format("S3 file \"%s\" is not archived (found %s storage class when expecting %s or %s). S3 Bucket Name: \"%s\"", S3_KEY,
                StorageClass.Standard.toString(), StorageClass.Glacier.toString(), StorageClass.DeepArchive.toString(), S3_BUCKET_NAME),
            businessObjectDataRestoreDto.getException().getMessage());
        businessObjectDataRestoreDto.setException(NO_EXCEPTION);
        assertEquals(new BusinessObjectDataRestoreDto(businessObjectDataKey, STORAGE_NAME, S3_ENDPOINT, S3_BUCKET_NAME, S3_KEY_PREFIX, NO_STORAGE_UNIT_STATUS,
            NO_STORAGE_UNIT_STATUS, storageFiles, NO_EXCEPTION, ARCHIVE_RETRIEVAL_OPTION, NO_BUSINESS_OBJECT_DATA), businessObjectDataRestoreDto);
    }

    @Test
    public void testGetStorageUnit()
    {
        // Create a business object data entity.
        BusinessObjectDataEntity businessObjectDataEntity = new BusinessObjectDataEntity();

        // Create a storage unit status entity.
        StorageUnitStatusEntity storageUnitStatusEntity = new StorageUnitStatusEntity();
        storageUnitStatusEntity.setCode(StorageUnitStatusEntity.ARCHIVED);

        // Create a storage unit entity.
        StorageUnitEntity storageUnitEntity = new StorageUnitEntity();
        storageUnitEntity.setStatus(storageUnitStatusEntity);

        // Mock the external calls.
        when(storageUnitDao.getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity)).thenReturn(
            Collections.singletonList(storageUnitEntity));

        // Call the method under test.
        StorageUnitEntity result = businessObjectDataInitiateRestoreHelperServiceImpl.getStorageUnit(businessObjectDataEntity);

        // Verify the external calls.
        verify(storageUnitDao).getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity);
        verifyNoMoreInteractionsHelper();

        // Validate the result.
        assertEquals(result, storageUnitEntity);
    }

    @Test
    public void testGetStorageUnitMultipleStorageUnitsExist()
    {
        // Create a business object data entity.
        BusinessObjectDataEntity businessObjectDataEntity = new BusinessObjectDataEntity();

        // Create a list of storage unit entities.
        List<StorageUnitEntity> storageUnitEntities = Arrays.asList(new StorageUnitEntity(), new StorageUnitEntity());

        // Mock the external calls.
        when(storageUnitDao.getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity)).thenReturn(
            storageUnitEntities);
        when(businessObjectDataHelper.businessObjectDataEntityAltKeyToString(businessObjectDataEntity)).thenReturn(BUSINESS_OBJECT_DATA_KEY_AS_STRING);

        // Try to call the method under test.
        try
        {
            businessObjectDataInitiateRestoreHelperServiceImpl.getStorageUnit(businessObjectDataEntity);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Business object data has multiple (%d) S3 storage units. Business object data: {%s}", storageUnitEntities.size(),
                BUSINESS_OBJECT_DATA_KEY_AS_STRING), e.getMessage());
        }

        // Verify the external calls.
        verify(storageUnitDao).getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity);
        verify(businessObjectDataHelper).businessObjectDataEntityAltKeyToString(businessObjectDataEntity);
        verifyNoMoreInteractionsHelper();
    }

    @Test
    public void testGetStorageUnitStorageUnitAlreadyEnabled()
    {
        // Create a business object data entity.
        BusinessObjectDataEntity businessObjectDataEntity = new BusinessObjectDataEntity();

        // Create a storage unit status entity.
        StorageUnitStatusEntity storageUnitStatusEntity = new StorageUnitStatusEntity();
        storageUnitStatusEntity.setCode(StorageUnitStatusEntity.ENABLED);

        // Create a storage entity.
        StorageEntity storageEntity = new StorageEntity();
        storageEntity.setName(STORAGE_NAME);

        // Create a storage unit entity.
        StorageUnitEntity storageUnitEntity = new StorageUnitEntity();
        storageUnitEntity.setStorage(storageEntity);
        storageUnitEntity.setBusinessObjectData(businessObjectDataEntity);
        storageUnitEntity.setStatus(storageUnitStatusEntity);

        // Mock the external calls.
        when(storageUnitDao.getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity)).thenReturn(
            Collections.singletonList(storageUnitEntity));
        when(businessObjectDataHelper.businessObjectDataEntityAltKeyToString(businessObjectDataEntity)).thenReturn(BUSINESS_OBJECT_DATA_KEY_AS_STRING);

        // Try to call the method under test.
        try
        {
            businessObjectDataInitiateRestoreHelperServiceImpl.getStorageUnit(businessObjectDataEntity);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Business object data is already available in \"%s\" S3 storage. Business object data: {%s}", STORAGE_NAME,
                BUSINESS_OBJECT_DATA_KEY_AS_STRING), e.getMessage());
        }

        // Verify the external calls.
        verify(storageUnitDao).getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity);
        verify(businessObjectDataHelper).businessObjectDataEntityAltKeyToString(businessObjectDataEntity);
        verifyNoMoreInteractionsHelper();
    }

    @Test
    public void testGetStorageUnitStorageUnitAlreadyRestoring()
    {
        // Create a business object data entity.
        BusinessObjectDataEntity businessObjectDataEntity = new BusinessObjectDataEntity();

        // Create a storage unit status entity.
        StorageUnitStatusEntity storageUnitStatusEntity = new StorageUnitStatusEntity();
        storageUnitStatusEntity.setCode(StorageUnitStatusEntity.RESTORING);

        // Create a storage entity.
        StorageEntity storageEntity = new StorageEntity();
        storageEntity.setName(STORAGE_NAME);

        // Create a storage unit entity.
        StorageUnitEntity storageUnitEntity = new StorageUnitEntity();
        storageUnitEntity.setStorage(storageEntity);
        storageUnitEntity.setBusinessObjectData(businessObjectDataEntity);
        storageUnitEntity.setStatus(storageUnitStatusEntity);

        // Mock the external calls.
        when(storageUnitDao.getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity)).thenReturn(
            Collections.singletonList(storageUnitEntity));
        when(businessObjectDataHelper.businessObjectDataEntityAltKeyToString(businessObjectDataEntity)).thenReturn(BUSINESS_OBJECT_DATA_KEY_AS_STRING);

        // Try to call the method under test.
        try
        {
            businessObjectDataInitiateRestoreHelperServiceImpl.getStorageUnit(businessObjectDataEntity);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Business object data is already being restored in \"%s\" S3 storage. Business object data: {%s}", STORAGE_NAME,
                BUSINESS_OBJECT_DATA_KEY_AS_STRING), e.getMessage());
        }

        // Verify the external calls.
        verify(storageUnitDao).getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity);
        verify(businessObjectDataHelper).businessObjectDataEntityAltKeyToString(businessObjectDataEntity);
        verifyNoMoreInteractionsHelper();
    }

    @Test
    public void testGetStorageUnitStorageUnitNoExists()
    {
        // Create a business object data entity.
        BusinessObjectDataEntity businessObjectDataEntity = new BusinessObjectDataEntity();

        // Mock the external calls.
        when(storageUnitDao.getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity)).thenReturn(
            new ArrayList<>());
        when(businessObjectDataHelper.businessObjectDataEntityAltKeyToString(businessObjectDataEntity)).thenReturn(BUSINESS_OBJECT_DATA_KEY_AS_STRING);

        // Try to call the method under test.
        try
        {
            businessObjectDataInitiateRestoreHelperServiceImpl.getStorageUnit(businessObjectDataEntity);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Business object data has no S3 storage unit. Business object data: {%s}", BUSINESS_OBJECT_DATA_KEY_AS_STRING),
                e.getMessage());
        }

        // Verify the external calls.
        verify(storageUnitDao).getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity);
        verify(businessObjectDataHelper).businessObjectDataEntityAltKeyToString(businessObjectDataEntity);
        verifyNoMoreInteractionsHelper();
    }

    @Test
    public void testGetStorageUnitStorageUnitNotArchivedOrRestored()
    {
        // Create a business object data entity.
        BusinessObjectDataEntity businessObjectDataEntity = new BusinessObjectDataEntity();

        // Create a storage unit status entity.
        StorageUnitStatusEntity storageUnitStatusEntity = new StorageUnitStatusEntity();
        storageUnitStatusEntity.setCode(STORAGE_UNIT_STATUS);

        // Create a storage entity.
        StorageEntity storageEntity = new StorageEntity();
        storageEntity.setName(STORAGE_NAME);

        // Create a storage unit entity.
        StorageUnitEntity storageUnitEntity = new StorageUnitEntity();
        storageUnitEntity.setStorage(storageEntity);
        storageUnitEntity.setBusinessObjectData(businessObjectDataEntity);
        storageUnitEntity.setStatus(storageUnitStatusEntity);

        // Mock the external calls.
        when(storageUnitDao.getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity)).thenReturn(
            Collections.singletonList(storageUnitEntity));
        when(businessObjectDataHelper.businessObjectDataEntityAltKeyToString(businessObjectDataEntity)).thenReturn(BUSINESS_OBJECT_DATA_KEY_AS_STRING);

        // Try to call the method under test.
        try
        {
            businessObjectDataInitiateRestoreHelperServiceImpl.getStorageUnit(businessObjectDataEntity);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(String.format("Business object data is not archived or restored. " +
                        "S3 storage unit in \"%s\" storage must have \"%s\" or \"%s\" status, but it actually has \"%s\" status. Business object data: {%s}",
                    STORAGE_NAME, StorageUnitStatusEntity.ARCHIVED, StorageUnitStatusEntity.RESTORED, STORAGE_UNIT_STATUS, BUSINESS_OBJECT_DATA_KEY_AS_STRING),
                e.getMessage());
        }

        // Verify the external calls.
        verify(storageUnitDao).getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity);
        verify(businessObjectDataHelper).businessObjectDataEntityAltKeyToString(businessObjectDataEntity);
        verifyNoMoreInteractionsHelper();
    }

    @Test
    public void testPrepareToInitiateRestoreNullArchiveRetrievalOption()
    {
        validatePrepareToInitiateRestoreWithValidArchiveRetrievalOption(null);
    }

    @Test
    public void testPrepareToInitiateRestoreBulkArchiveRetrievalOption()
    {
        validatePrepareToInitiateRestoreWithValidArchiveRetrievalOption(Tier.Bulk.toString());
    }

    @Test
    public void testPrepareToInitiateRestoreStandardArchiveRetrievalOption()
    {
        validatePrepareToInitiateRestoreWithValidArchiveRetrievalOption(Tier.Standard.toString());
    }

    @Test
    public void testPrepareToInitiateRestoreExpeditedArchiveRetrievalOption()
    {
        validatePrepareToInitiateRestoreWithValidArchiveRetrievalOption(Tier.Expedited.toString());
    }

    @Test
    public void testPrepareToInitiateRestoreWhitespacesArchiveRetrievalOption()
    {
        // Create an archive retrieval option with whitespaces
        String archiveRetrievalOption = "  " + Tier.Expedited.toString() + "  ";

        // Create a business object data key.
        BusinessObjectDataKey businessObjectDataKey =
            new BusinessObjectDataKey(BDEF_NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, SUBPARTITION_VALUES,
                DATA_VERSION);

        BusinessObjectDataEntity businessObjectDataEntity = new BusinessObjectDataEntity();

        // Create a single storage unit
        StorageEntity storageEntity = new StorageEntity();
        storageEntity.setName(STORAGE_NAME);
        StorageUnitStatusEntity storageUnitStatusEntity = new StorageUnitStatusEntity();
        storageUnitStatusEntity.setCode(StorageUnitStatusEntity.ARCHIVED);
        StorageUnitEntity storageUnitEntity = new StorageUnitEntity();
        storageUnitEntity.setStorage(storageEntity);
        storageUnitEntity.setStatus(storageUnitStatusEntity);
        List<StorageUnitEntity> storageUnitEntities = Collections.singletonList(storageUnitEntity);

        List<StorageFile> storageFiles = Collections.singletonList(new StorageFile(S3_KEY, FILE_SIZE, ROW_COUNT));
        StorageUnitStatusEntity newStorageUnitStatusEntity = new StorageUnitStatusEntity();
        newStorageUnitStatusEntity.setCode(StorageUnitStatusEntity.RESTORING);

        // Mock the external calls.
        when(businessObjectDataDaoHelper.getBusinessObjectDataEntity(businessObjectDataKey)).thenReturn(businessObjectDataEntity);
        when(storageUnitDao.getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity)).thenReturn(
            storageUnitEntities);
        when(configurationHelper.getProperty(ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME)).thenReturn(
            (String) ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME.getDefaultValue());
        when(storageHelper.getStorageAttributeValueByName((String) ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME.getDefaultValue(), storageEntity,
            true)).thenReturn(S3_BUCKET_NAME);
        when(s3KeyPrefixHelper.buildS3KeyPrefix(storageEntity, businessObjectDataEntity.getBusinessObjectFormat(), businessObjectDataKey)).thenReturn(
            S3_KEY_PREFIX);
        when(storageFileHelper.getAndValidateStorageFiles(storageUnitEntity, S3_KEY_PREFIX, STORAGE_NAME, businessObjectDataKey, false)).thenReturn(
            storageFiles);
        when(storageUnitStatusDaoHelper.getStorageUnitStatusEntity(StorageUnitStatusEntity.RESTORING)).thenReturn(newStorageUnitStatusEntity);
        when(businessObjectDataHelper.getBusinessObjectDataKey(businessObjectDataEntity)).thenReturn(businessObjectDataKey);
        when(configurationHelper.getProperty(ConfigurationValue.S3_ENDPOINT)).thenReturn(S3_ENDPOINT);

        // Make the archive retrieval option null
        BusinessObjectDataRestoreDto businessObjectDataRestoreDto =
            businessObjectDataInitiateRestoreHelperServiceImpl.prepareToInitiateRestore(businessObjectDataKey, EXPIRATION_IN_DAYS, archiveRetrievalOption,
                BATCH_RESTORE_MODE);

        // Validate the businessObjectDataRestoreDto
        assertEquals(businessObjectDataKey, businessObjectDataRestoreDto.getBusinessObjectDataKey());
        assertEquals(STORAGE_NAME, businessObjectDataRestoreDto.getStorageName());
        assertEquals(S3_ENDPOINT, businessObjectDataRestoreDto.getS3Endpoint());
        assertEquals(S3_BUCKET_NAME, businessObjectDataRestoreDto.getS3BucketName());
        assertEquals(S3_KEY_PREFIX, businessObjectDataRestoreDto.getS3KeyPrefix());
        assertEquals(storageFiles, businessObjectDataRestoreDto.getStorageFiles());
        // Verify the whitespaces are trimmed
        assertEquals(Tier.Expedited.toString(), businessObjectDataRestoreDto.getArchiveRetrievalOption());
        assertEquals(StorageUnitStatusEntity.RESTORING, businessObjectDataRestoreDto.getNewStorageUnitStatus());
        assertEquals(StorageUnitStatusEntity.ARCHIVED, businessObjectDataRestoreDto.getOldStorageUnitStatus());
    }

    @Test
    public void testPrepareToInitiateRestoreInvalidArchiveRetrievalOption()
    {
        // Create a business object data key.
        BusinessObjectDataKey businessObjectDataKey =
            new BusinessObjectDataKey(BDEF_NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, SUBPARTITION_VALUES,
                DATA_VERSION);

        // Specify the expected exception.
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is(String.format(
            String.format("The archive retrieval option value \"%s\" is invalid. Valid archive retrieval option values are:%s",
                INVALID_ARCHIVE_RETRIEVAL_OPTION, Stream.of(Tier.values()).map(Enum::name).collect(Collectors.toList())))));

        businessObjectDataInitiateRestoreHelperServiceImpl.prepareToInitiateRestore(businessObjectDataKey, EXPIRATION_IN_DAYS, INVALID_ARCHIVE_RETRIEVAL_OPTION,
            BATCH_RESTORE_MODE);

        // Verify the external calls
        verify(businessObjectDataHelper).validateBusinessObjectDataKey(businessObjectDataKey, true, true);
        verifyNoMoreInteractionsHelper();
    }

    /**
     * Validate the method PrepareToInitiateRestore. The archive retrieval option needs to be valid.
     *
     * @param archiveRetrievalOption the valid archive retrieval option
     */
    private void validatePrepareToInitiateRestoreWithValidArchiveRetrievalOption(String archiveRetrievalOption)
    {
        // Create a business object data key.
        BusinessObjectDataKey businessObjectDataKey =
            new BusinessObjectDataKey(BDEF_NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE, SUBPARTITION_VALUES,
                DATA_VERSION);

        // Create a business object format entity.
        BusinessObjectFormatEntity businessObjectFormatEntity = new BusinessObjectFormatEntity();

        // Create a business object data entity.
        BusinessObjectDataEntity businessObjectDataEntity = new BusinessObjectDataEntity();
        businessObjectDataEntity.setBusinessObjectFormat(businessObjectFormatEntity);

        // Create a single storage unit.
        StorageEntity storageEntity = new StorageEntity();
        storageEntity.setName(STORAGE_NAME);
        StorageUnitStatusEntity storageUnitStatusEntity = new StorageUnitStatusEntity();
        storageUnitStatusEntity.setCode(StorageUnitStatusEntity.ARCHIVED);
        StorageUnitEntity storageUnitEntity = new StorageUnitEntity();
        storageUnitEntity.setStorage(storageEntity);
        storageUnitEntity.setStatus(storageUnitStatusEntity);
        List<StorageUnitEntity> storageUnitEntities = Collections.singletonList(storageUnitEntity);

        List<StorageFile> storageFiles = Collections.singletonList(new StorageFile(S3_KEY, FILE_SIZE, ROW_COUNT));
        StorageUnitStatusEntity newStorageUnitStatusEntity = new StorageUnitStatusEntity();
        newStorageUnitStatusEntity.setCode(StorageUnitStatusEntity.RESTORING);

        // Mock the external calls.
        when(businessObjectDataDaoHelper.getBusinessObjectDataEntity(businessObjectDataKey)).thenReturn(businessObjectDataEntity);
        when(storageUnitDao.getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity)).thenReturn(
            storageUnitEntities);
        when(configurationHelper.getProperty(ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME)).thenReturn(
            (String) ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME.getDefaultValue());
        when(storageHelper.getStorageAttributeValueByName((String) ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME.getDefaultValue(), storageEntity,
            true)).thenReturn(S3_BUCKET_NAME);
        when(s3KeyPrefixHelper.buildS3KeyPrefix(storageEntity, businessObjectDataEntity.getBusinessObjectFormat(), businessObjectDataKey)).thenReturn(
            S3_KEY_PREFIX);
        when(storageFileHelper.getAndValidateStorageFiles(storageUnitEntity, S3_KEY_PREFIX, STORAGE_NAME, businessObjectDataKey, false)).thenReturn(
            storageFiles);
        when(storageUnitStatusDaoHelper.getStorageUnitStatusEntity(StorageUnitStatusEntity.RESTORING)).thenReturn(newStorageUnitStatusEntity);
        when(businessObjectDataHelper.getBusinessObjectDataKey(businessObjectDataEntity)).thenReturn(businessObjectDataKey);
        when(configurationHelper.getProperty(ConfigurationValue.S3_ENDPOINT)).thenReturn(S3_ENDPOINT);

        // Make the archive retrieval option null
        BusinessObjectDataRestoreDto businessObjectDataRestoreDto =
            businessObjectDataInitiateRestoreHelperServiceImpl.prepareToInitiateRestore(businessObjectDataKey, EXPIRATION_IN_DAYS, archiveRetrievalOption,
                BATCH_RESTORE_MODE);

        // Validate the businessObjectDataRestoreDto
        assertEquals(businessObjectDataKey, businessObjectDataRestoreDto.getBusinessObjectDataKey());
        assertEquals(STORAGE_NAME, businessObjectDataRestoreDto.getStorageName());
        assertEquals(S3_ENDPOINT, businessObjectDataRestoreDto.getS3Endpoint());
        assertEquals(S3_BUCKET_NAME, businessObjectDataRestoreDto.getS3BucketName());
        assertEquals(S3_KEY_PREFIX, businessObjectDataRestoreDto.getS3KeyPrefix());
        assertEquals(storageFiles, businessObjectDataRestoreDto.getStorageFiles());
        assertEquals(archiveRetrievalOption, businessObjectDataRestoreDto.getArchiveRetrievalOption());
        assertEquals(StorageUnitStatusEntity.RESTORING, businessObjectDataRestoreDto.getNewStorageUnitStatus());
        assertEquals(StorageUnitStatusEntity.ARCHIVED, businessObjectDataRestoreDto.getOldStorageUnitStatus());

        // Verify the external calls
        verify(businessObjectDataHelper).validateBusinessObjectDataKey(businessObjectDataKey, true, true);
        verify(businessObjectDataDaoHelper).getBusinessObjectDataEntity(businessObjectDataKey);
        verify(storageUnitDao).getStorageUnitsByStoragePlatformAndBusinessObjectData(StoragePlatformEntity.S3, businessObjectDataEntity);
        verify(configurationHelper).getProperty(ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME);
        verify(storageHelper).getStorageAttributeValueByName((String) ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME.getDefaultValue(), storageEntity, true);
        verify(s3KeyPrefixHelper).buildS3KeyPrefix(storageEntity, businessObjectDataEntity.getBusinessObjectFormat(), businessObjectDataKey);
        verify(storageFileHelper).getAndValidateStorageFiles(storageUnitEntity, S3_KEY_PREFIX, STORAGE_NAME, businessObjectDataKey, false);
        verify(storageUnitDaoHelper).validateNoExplicitlyRegisteredSubPartitionInStorageForBusinessObjectData(storageEntity, businessObjectFormatEntity,
            businessObjectDataKey, S3_KEY_PREFIX);
        verify(storageUnitStatusDaoHelper).getStorageUnitStatusEntity(StorageUnitStatusEntity.RESTORING);
        verify(storageUnitDaoHelper).updateStorageUnitStatus(storageUnitEntity, newStorageUnitStatusEntity, StorageUnitStatusEntity.RESTORING);
        verify(businessObjectDataHelper).getBusinessObjectDataKey(businessObjectDataEntity);
        verify(configurationHelper).getProperty(ConfigurationValue.S3_ENDPOINT);
        verifyNoMoreInteractionsHelper();
    }

    /**
     * Checks if any of the mocks has any interaction.
     */
    private void verifyNoMoreInteractionsHelper()
    {
        verifyNoMoreInteractions(businessObjectDataDaoHelper, businessObjectDataHelper, configurationHelper, herdStringHelper, jsonHelper, s3KeyPrefixHelper,
            s3Service, storageFileHelper, storageHelper, storageUnitDao, storageUnitDaoHelper, storageUnitStatusDaoHelper);
    }
}
