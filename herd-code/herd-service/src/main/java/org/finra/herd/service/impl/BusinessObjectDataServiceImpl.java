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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import org.finra.herd.core.helper.ConfigurationHelper;
import org.finra.herd.dao.BusinessObjectDataDao;
import org.finra.herd.dao.StorageUnitDao;
import org.finra.herd.dao.config.DaoSpringModuleConfig;
import org.finra.herd.dao.helper.JsonHelper;
import org.finra.herd.model.annotation.NamespacePermission;
import org.finra.herd.model.annotation.PublishJmsMessages;
import org.finra.herd.model.api.xml.BusinessObjectData;
import org.finra.herd.model.api.xml.BusinessObjectDataAvailability;
import org.finra.herd.model.api.xml.BusinessObjectDataAvailabilityCollectionRequest;
import org.finra.herd.model.api.xml.BusinessObjectDataAvailabilityCollectionResponse;
import org.finra.herd.model.api.xml.BusinessObjectDataAvailabilityRequest;
import org.finra.herd.model.api.xml.BusinessObjectDataCreateRequest;
import org.finra.herd.model.api.xml.BusinessObjectDataDdl;
import org.finra.herd.model.api.xml.BusinessObjectDataDdlCollectionRequest;
import org.finra.herd.model.api.xml.BusinessObjectDataDdlCollectionResponse;
import org.finra.herd.model.api.xml.BusinessObjectDataDdlRequest;
import org.finra.herd.model.api.xml.BusinessObjectDataInvalidateUnregisteredRequest;
import org.finra.herd.model.api.xml.BusinessObjectDataInvalidateUnregisteredResponse;
import org.finra.herd.model.api.xml.BusinessObjectDataKey;
import org.finra.herd.model.api.xml.BusinessObjectDataRetryStoragePolicyTransitionRequest;
import org.finra.herd.model.api.xml.BusinessObjectDataSearchRequest;
import org.finra.herd.model.api.xml.BusinessObjectDataSearchResult;
import org.finra.herd.model.api.xml.BusinessObjectDataStatus;
import org.finra.herd.model.api.xml.BusinessObjectDataVersion;
import org.finra.herd.model.api.xml.BusinessObjectDataVersions;
import org.finra.herd.model.api.xml.BusinessObjectFormatKey;
import org.finra.herd.model.api.xml.CustomDdlKey;
import org.finra.herd.model.api.xml.NamespacePermissionEnum;
import org.finra.herd.model.dto.BusinessObjectDataRestoreDto;
import org.finra.herd.model.dto.BusinessObjectDataRetryStoragePolicyTransitionDto;
import org.finra.herd.model.dto.ConfigurationValue;
import org.finra.herd.model.dto.S3FileTransferRequestParamsDto;
import org.finra.herd.model.jpa.BusinessObjectDataEntity;
import org.finra.herd.model.jpa.BusinessObjectDataStatusEntity;
import org.finra.herd.model.jpa.BusinessObjectFormatEntity;
import org.finra.herd.model.jpa.CustomDdlEntity;
import org.finra.herd.model.jpa.NotificationEventTypeEntity;
import org.finra.herd.model.jpa.StorageEntity;
import org.finra.herd.model.jpa.StorageFileEntity;
import org.finra.herd.model.jpa.StoragePlatformEntity;
import org.finra.herd.model.jpa.StorageUnitEntity;
import org.finra.herd.service.BusinessObjectDataInitiateRestoreHelperService;
import org.finra.herd.service.BusinessObjectDataRetryStoragePolicyTransitionHelperService;
import org.finra.herd.service.BusinessObjectDataService;
import org.finra.herd.service.NotificationEventService;
import org.finra.herd.service.S3Service;
import org.finra.herd.service.helper.BusinessObjectDataDaoHelper;
import org.finra.herd.service.helper.BusinessObjectDataHelper;
import org.finra.herd.service.helper.BusinessObjectDataInvalidateUnregisteredHelper;
import org.finra.herd.service.helper.BusinessObjectDataSearchHelper;
import org.finra.herd.service.helper.BusinessObjectDataStatusDaoHelper;
import org.finra.herd.service.helper.BusinessObjectFormatDaoHelper;
import org.finra.herd.service.helper.CustomDdlDaoHelper;
import org.finra.herd.service.helper.DdlGeneratorFactory;
import org.finra.herd.service.helper.S3KeyPrefixHelper;
import org.finra.herd.service.helper.StorageDaoHelper;
import org.finra.herd.service.helper.StorageHelper;
import org.finra.herd.service.helper.StorageUnitHelper;

/**
 * The business object data service implementation.
 */
@Service
@Transactional(value = DaoSpringModuleConfig.HERD_TRANSACTION_MANAGER_BEAN_NAME)
public class BusinessObjectDataServiceImpl implements BusinessObjectDataService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessObjectDataServiceImpl.class);

    /**
     * A status reason of "not registered".
     */
    public static final String REASON_NOT_REGISTERED = "NOT_REGISTERED";

    /**
     * A status reason of "no enabled storage unit".
     */
    public static final String REASON_NO_ENABLED_STORAGE_UNIT = "NO_ENABLED_STORAGE_UNIT";

    /**
     * A status reason of "archived".
     */
    public static final String REASON_ARCHIVED = "ARCHIVED";

    @Autowired
    private BusinessObjectDataDao businessObjectDataDao;

    @Autowired
    private BusinessObjectDataDaoHelper businessObjectDataDaoHelper;

    @Autowired
    private BusinessObjectDataHelper businessObjectDataHelper;

    @Autowired
    private BusinessObjectDataSearchHelper businessObjectDataSearchHelper;

    @Autowired
    private BusinessObjectDataInitiateRestoreHelperService businessObjectDataInitiateRestoreHelperService;

    @Autowired
    private BusinessObjectDataInvalidateUnregisteredHelper businessObjectDataInvalidateUnregisteredHelper;

    @Autowired
    private BusinessObjectDataRetryStoragePolicyTransitionHelperService businessObjectDataRetryStoragePolicyTransitionHelperService;

    @Autowired
    private BusinessObjectDataStatusDaoHelper businessObjectDataStatusDaoHelper;

    @Autowired
    private BusinessObjectFormatDaoHelper businessObjectFormatDaoHelper;

    @Autowired
    private ConfigurationHelper configurationHelper;

    @Autowired
    private CustomDdlDaoHelper customDdlDaoHelper;

    @Autowired
    private DdlGeneratorFactory ddlGeneratorFactory;

    @Autowired
    private JsonHelper jsonHelper;

    @Autowired
    private S3KeyPrefixHelper s3KeyPrefixHelper;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private StorageDaoHelper storageDaoHelper;

    @Autowired
    private StorageHelper storageHelper;

    @Autowired
    private StorageUnitDao storageUnitDao;

    @Autowired
    private StorageUnitHelper storageUnitHelper;

    @Autowired
    private NotificationEventService notificationEventService;

    @PublishJmsMessages
    @NamespacePermission(fields = "#request.namespace", permissions = NamespacePermissionEnum.WRITE)
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BusinessObjectData createBusinessObjectData(BusinessObjectDataCreateRequest request)
    {
        return businessObjectDataDaoHelper.createBusinessObjectData(request);
    }

    @NamespacePermission(fields = "#businessObjectDataKey.namespace", permissions = NamespacePermissionEnum.READ)
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BusinessObjectData getBusinessObjectData(BusinessObjectDataKey businessObjectDataKey, String businessObjectFormatPartitionKey,
        String businessObjectDataStatus, Boolean includeBusinessObjectDataStatusHistory)
    {
        return getBusinessObjectDataImpl(businessObjectDataKey, businessObjectFormatPartitionKey, businessObjectDataStatus,
            includeBusinessObjectDataStatusHistory);
    }

    /**
     * Retrieves existing business object data entry information. This method does not start a new transaction and instead continues with existing transaction,
     * if any.
     *
     * @param businessObjectDataKey the business object data key
     * @param businessObjectFormatPartitionKey the business object format partition key, may be null
     * @param businessObjectDataStatus the business object data status, may be null
     * @param includeBusinessObjectDataStatusHistory specifies to include business object data status history in the response
     *
     * @return the retrieved business object data information
     */
    protected BusinessObjectData getBusinessObjectDataImpl(BusinessObjectDataKey businessObjectDataKey, String businessObjectFormatPartitionKey,
        String businessObjectDataStatus, Boolean includeBusinessObjectDataStatusHistory)
    {
        // Validate and trim the business object data key.
        businessObjectDataHelper.validateBusinessObjectDataKey(businessObjectDataKey, false, false);

        // If specified, trim the partition key parameter.
        String businessObjectFormatPartitionKeyLocal = businessObjectFormatPartitionKey != null ? businessObjectFormatPartitionKey.trim() : null;

        // If specified, trim the business object data status parameter; otherwise default to VALID status.
        String businessObjectDataStatusLocal = businessObjectDataStatus != null ? businessObjectDataStatus.trim() : BusinessObjectDataStatusEntity.VALID;

        // Validate the business object data status.
        BusinessObjectDataStatusEntity businessObjectDataStatusEntity =
            businessObjectDataStatusDaoHelper.getBusinessObjectDataStatusEntity(businessObjectDataStatusLocal);

        // Get the business object data based on the specified parameters. If a business object data version isn't specified,
        // the latest version of business object data of the specified business object data status is returned.
        BusinessObjectDataEntity businessObjectDataEntity =
            businessObjectDataDaoHelper.getBusinessObjectDataEntityByKeyAndStatus(businessObjectDataKey, businessObjectDataStatusEntity.getCode());

        // If specified, ensure the partition key matches what's configured within the business object format.
        if (StringUtils.isNotBlank(businessObjectFormatPartitionKeyLocal))
        {
            String configuredPartitionKey = businessObjectDataEntity.getBusinessObjectFormat().getPartitionKey();
            Assert.isTrue(configuredPartitionKey.equalsIgnoreCase(businessObjectFormatPartitionKeyLocal), String
                .format("Partition key \"%s\" doesn't match configured business object format partition key \"%s\".", businessObjectFormatPartitionKeyLocal,
                    configuredPartitionKey));
        }

        // Create and return the business object definition object from the persisted entity.
        return businessObjectDataHelper.createBusinessObjectDataFromEntity(businessObjectDataEntity, includeBusinessObjectDataStatusHistory);
    }

    @NamespacePermission(fields = "#businessObjectDataKey.namespace", permissions = NamespacePermissionEnum.READ)
    @Override
    public BusinessObjectDataVersions getBusinessObjectDataVersions(BusinessObjectDataKey businessObjectDataKey)
    {
        // Validate and trim the business object data key.
        businessObjectDataHelper.validateBusinessObjectDataKey(businessObjectDataKey, false, false);

        // Get the business object data versions based on the specified parameters.
        List<BusinessObjectDataEntity> businessObjectDataEntities = businessObjectDataDao.getBusinessObjectDataEntities(businessObjectDataKey);

        // Create the response.
        BusinessObjectDataVersions businessObjectDataVersions = new BusinessObjectDataVersions();
        for (BusinessObjectDataEntity businessObjectDataEntity : businessObjectDataEntities)
        {
            BusinessObjectDataVersion businessObjectDataVersion = new BusinessObjectDataVersion();
            BusinessObjectDataKey businessObjectDataVersionKey = businessObjectDataHelper.getBusinessObjectDataKey(businessObjectDataEntity);
            businessObjectDataVersion.setBusinessObjectDataKey(businessObjectDataVersionKey);
            businessObjectDataVersion.setStatus(businessObjectDataEntity.getStatus().getCode());
            businessObjectDataVersions.getBusinessObjectDataVersions().add(businessObjectDataVersion);
        }

        return businessObjectDataVersions;
    }

    @NamespacePermission(fields = "#businessObjectDataKey.namespace", permissions = NamespacePermissionEnum.WRITE)
    @Override
    public BusinessObjectData deleteBusinessObjectData(BusinessObjectDataKey businessObjectDataKey, Boolean deleteFiles)
    {
        // Validate and trim the business object data key.
        businessObjectDataHelper.validateBusinessObjectDataKey(businessObjectDataKey, true, true);

        // Validate the mandatory deleteFiles flag.
        Assert.notNull(deleteFiles, "A delete files flag must be specified.");

        // Retrieve the business object data and ensure it exists.
        BusinessObjectDataEntity businessObjectDataEntity = businessObjectDataDaoHelper.getBusinessObjectDataEntity(businessObjectDataKey);

        // Check if we are allowed to delete this business object data.
        if (!businessObjectDataEntity.getBusinessObjectDataChildren().isEmpty())
        {
            throw new IllegalArgumentException(String
                .format("Can not delete a business object data that has children associated with it. Business object data: {%s}",
                    businessObjectDataHelper.businessObjectDataEntityAltKeyToString(businessObjectDataEntity)));
        }

        // If the flag is set, clean up the data files from all storages of S3 storage platform type.
        LOGGER.info("deleteFiles={}", deleteFiles);
        if (deleteFiles)
        {
            // Loop over all storage units for this business object data.
            for (StorageUnitEntity storageUnitEntity : businessObjectDataEntity.getStorageUnits())
            {
                StorageEntity storageEntity = storageUnitEntity.getStorage();

                // Currently, we only support data file deletion from S3 platform type.
                if (storageEntity.getStoragePlatform().getName().equals(StoragePlatformEntity.S3))
                {
                    LOGGER.info("Deleting business object data files from the storage... storageName=\"{}\" businessObjectDataKey={}", storageEntity.getName(),
                        jsonHelper.objectToJson(businessObjectDataHelper.getBusinessObjectDataKey(businessObjectDataEntity)));

                    // Get the S3 validation flags.
                    boolean validatePathPrefix = storageHelper
                        .getBooleanStorageAttributeValueByName(configurationHelper.getProperty(ConfigurationValue.S3_ATTRIBUTE_NAME_VALIDATE_PATH_PREFIX),
                            storageEntity, false, true);

                    // If this storage conforms to the path prefix validation, then delete all keys found under the S3 key prefix.
                    if (validatePathPrefix)
                    {
                        // Retrieve S3 key prefix velocity template storage attribute value and store it in memory.
                        // Please note that it is not required, so we pass in a "false" flag.
                        String s3KeyPrefixVelocityTemplate = storageHelper
                            .getStorageAttributeValueByName(configurationHelper.getProperty(ConfigurationValue.S3_ATTRIBUTE_NAME_KEY_PREFIX_VELOCITY_TEMPLATE),
                                storageEntity, false);

                        // Validate that S3 key prefix velocity template is configured.
                        Assert.isTrue(StringUtils.isNotBlank(s3KeyPrefixVelocityTemplate), String
                            .format("Storage \"%s\" has enabled path validation without S3 key prefix velocity template configured.", storageEntity.getName()));

                        // Build the S3 key prefix as per S3 Naming Convention Wiki page.
                        String s3KeyPrefix = s3KeyPrefixHelper
                            .buildS3KeyPrefix(s3KeyPrefixVelocityTemplate, businessObjectDataEntity.getBusinessObjectFormat(), businessObjectDataKey,
                                storageEntity.getName());

                        // Get S3 bucket access parameters, such as bucket name, AWS access key ID, AWS secret access key, etc...
                        S3FileTransferRequestParamsDto params = storageHelper.getS3BucketAccessParams(storageEntity);
                        // Since the S3 key prefix represents a directory, we add a trailing '/' character to it.
                        params.setS3KeyPrefix(s3KeyPrefix + "/");
                        // Delete a list of all keys/objects from S3 managed bucket matching the expected S3 key prefix.
                        // Please note that when deleting S3 files, we also delete all 0 byte objects that represent S3 directories.
                        s3Service.deleteDirectory(params);
                    }
                    // For a non S3 prefixed paths, delete the files explicitly or if only directory is registered, delete all files/subfolders found under it.
                    else
                    {
                        // Get S3 bucket access parameters, such as bucket name, AWS access key ID, AWS secret access key, etc...
                        S3FileTransferRequestParamsDto params = storageHelper.getS3BucketAccessParams(storageEntity);

                        // If only directory is registered delete all files/sub-folders found under it.
                        if (StringUtils.isNotBlank(storageUnitEntity.getDirectoryPath()) && storageUnitEntity.getStorageFiles().isEmpty())
                        {
                            // Since the directory path represents a directory, we add a trailing '/' character to it.
                            params.setS3KeyPrefix(storageUnitEntity.getDirectoryPath() + "/");
                            // Delete a list of all keys/objects from S3 bucket matching the directory path.
                            // Please note that when deleting S3 files, we also delete all 0 byte objects that represent S3 directories.
                            s3Service.deleteDirectory(params);
                        }
                        // Delete the files explicitly.
                        else
                        {
                            // Create a list of files to delete.
                            List<File> files = new ArrayList<>();
                            for (StorageFileEntity storageFileEntity : storageUnitEntity.getStorageFiles())
                            {
                                files.add(new File(storageFileEntity.getPath()));
                            }
                            params.setFiles(files);
                            s3Service.deleteFileList(params);
                        }
                    }
                }
                else
                {
                    LOGGER.info("Skipping business object data file removal for a storage unit from the storage since it is not an S3 storage platform. " +
                        " storageName=\"{}\" businessObjectDataKey={}", storageEntity.getName(),
                        jsonHelper.objectToJson(businessObjectDataHelper.getBusinessObjectDataKey(businessObjectDataEntity)));
                }
            }
        }

        // Create the business object data object from the entity.
        BusinessObjectData deletedBusinessObjectData = businessObjectDataHelper.createBusinessObjectDataFromEntity(businessObjectDataEntity);

        // Delete this business object data.
        businessObjectDataDao.delete(businessObjectDataEntity);

        // If this business object data version is the latest, set the latest flag on the previous version of this object data, if it exists.
        if (businessObjectDataEntity.getLatestVersion())
        {
            // Get the maximum version for this business object data, if it exists.
            Integer maxBusinessObjectDataVersion = businessObjectDataDao.getBusinessObjectDataMaxVersion(businessObjectDataKey);

            if (maxBusinessObjectDataVersion != null)
            {
                // Retrieve the previous version business object data entity. Since we successfully got the maximum
                // version for this business object data, the retrieved entity is not expected to be null.
                BusinessObjectDataEntity previousVersionBusinessObjectDataEntity = businessObjectDataDao.getBusinessObjectDataByAltKey(
                    new BusinessObjectDataKey(businessObjectDataKey.getNamespace(), businessObjectDataKey.getBusinessObjectDefinitionName(),
                        businessObjectDataKey.getBusinessObjectFormatUsage(), businessObjectDataKey.getBusinessObjectFormatFileType(),
                        businessObjectDataKey.getBusinessObjectFormatVersion(), businessObjectDataKey.getPartitionValue(),
                        businessObjectDataKey.getSubPartitionValues(), maxBusinessObjectDataVersion));

                // Update the previous version business object data entity.
                previousVersionBusinessObjectDataEntity.setLatestVersion(true);
                businessObjectDataDao.saveAndRefresh(previousVersionBusinessObjectDataEntity);
            }
        }

        // Return the deleted business object data.
        return deletedBusinessObjectData;
    }

    @NamespacePermission(fields = "#request.namespace", permissions = NamespacePermissionEnum.READ)
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BusinessObjectDataAvailability checkBusinessObjectDataAvailability(BusinessObjectDataAvailabilityRequest request)
    {
        return checkBusinessObjectDataAvailabilityImpl(request);
    }

    @NamespacePermission(fields = "#request?.businessObjectDataAvailabilityRequests?.![namespace]",
        permissions = NamespacePermissionEnum.READ)
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BusinessObjectDataAvailabilityCollectionResponse checkBusinessObjectDataAvailabilityCollection(
        BusinessObjectDataAvailabilityCollectionRequest request)
    {
        return checkBusinessObjectDataAvailabilityCollectionImpl(request);
    }

    /**
     * Performs a search and returns a list of business object data key values and relative statuses for a range of requested business object data.
     *
     * @param request the business object data availability request
     *
     * @return the business object data availability information
     */
    protected BusinessObjectDataAvailability checkBusinessObjectDataAvailabilityImpl(BusinessObjectDataAvailabilityRequest request)
    {
        // By default, validate and trim the request.
        return checkBusinessObjectDataAvailabilityImpl(request, false);
    }

    /**
     * Performs a search and returns a list of business object data key values and relative statuses for a range of requested business object data.
     *
     * @param request the business object data availability request
     * @param skipRequestValidation specifies whether to skip the request validation and trimming
     *
     * @return the business object data availability information
     */
    protected BusinessObjectDataAvailability checkBusinessObjectDataAvailabilityImpl(BusinessObjectDataAvailabilityRequest request,
        boolean skipRequestValidation)
    {
        // Perform the validation.
        if (!skipRequestValidation)
        {
            validateBusinessObjectDataAvailabilityRequest(request);
        }

        // Get business object format key from the request.
        BusinessObjectFormatKey businessObjectFormatKey = getBusinessObjectFormatKey(request);

        // Make sure that specified business object format exists.
        BusinessObjectFormatEntity businessObjectFormatEntity = businessObjectFormatDaoHelper.getBusinessObjectFormatEntity(businessObjectFormatKey);

        // Get the list of storages from the request and validate that specified storages exist.
        List<String> storageNames = getStorageNames(request);
        storageDaoHelper.validateStorageExistence(storageNames);

        // Build partition filters based on the specified partition value filters.
        // Business object data availability works across all storage platform types, so the storage platform type is not specified in the call.
        // Since we want to search across "available" storage units, we exclude Glacier storage platform type.
        List<List<String>> partitionFilters = businessObjectDataDaoHelper
            .buildPartitionFilters(request.getPartitionValueFilters(), request.getPartitionValueFilter(), businessObjectFormatKey,
                request.getBusinessObjectDataVersion(), storageNames, null, StoragePlatformEntity.GLACIER, businessObjectFormatEntity);

        // Retrieve a list of storage unit entities for the specified partition values. The entities will be sorted by partition value that is identified
        // by partition column position. If a business object data version isn't specified, the latest VALID business object data version is returned.
        // Business object data availability works across all storage platform types, so the storage platform type is not specified in the herdDao call.
        // We want to select only "available" storage units, so we exclude Glacier storage platform type (when storage names are not specified) and pass
        // "true" for selectOnlyAvailableStorageUnits parameter.
        List<StorageUnitEntity> availableStorageUnitEntities = storageUnitDao
            .getStorageUnitsByPartitionFiltersAndStorages(businessObjectFormatKey, partitionFilters, request.getBusinessObjectDataVersion(),
                BusinessObjectDataStatusEntity.VALID, storageNames, null, StoragePlatformEntity.GLACIER, true);

        // Create business object data availability object instance and initialise it with request field values.
        BusinessObjectDataAvailability businessObjectDataAvailability = createBusinessObjectDataAvailability(request);

        // Create "available" and "not available" business object data status lists.
        List<BusinessObjectDataStatus> availableStatuses = new ArrayList<>();
        businessObjectDataAvailability.setAvailableStatuses(availableStatuses);
        List<BusinessObjectDataStatus> notAvailableStatuses = new ArrayList<>();
        businessObjectDataAvailability.setNotAvailableStatuses(notAvailableStatuses);

        // Build a list of matched available partition filters and populate the available statuses list. Please note that each request partition filter
        // might result in multiple available business object data entities. If storage names are not specified, fail on "duplicate" business object data
        // (same business object data instance registered with multiple storages). Otherwise, remove possible "duplicates".
        List<List<String>> matchedAvailablePartitionFilters = new ArrayList<>();
        List<List<String>> availablePartitions = new ArrayList<>();
        Map<BusinessObjectDataEntity, StorageUnitEntity> businessObjectDataToStorageUnitMap = new HashMap<>();
        for (StorageUnitEntity storageUnitEntity : availableStorageUnitEntities)
        {
            BusinessObjectDataEntity businessObjectDataEntity = storageUnitEntity.getBusinessObjectData();

            if (businessObjectDataToStorageUnitMap.containsKey(businessObjectDataEntity))
            {
                // If storage names are not specified, fail on a business object data registered in multiple storages. Otherwise, ignore that storage unit.
                if (CollectionUtils.isEmpty(storageNames))
                {
                    throw new IllegalArgumentException(String.format("Found business object data registered in more than one storage. " +
                        "Please specify storage(s) in the request to resolve this. Business object data {%s}",
                        businessObjectDataHelper.businessObjectDataEntityAltKeyToString(businessObjectDataEntity)));
                }
            }
            else
            {
                BusinessObjectDataKey businessObjectDataKey = businessObjectDataHelper.getBusinessObjectDataKey(businessObjectDataEntity);
                matchedAvailablePartitionFilters.add(businessObjectDataHelper.getPartitionFilter(businessObjectDataKey, partitionFilters.get(0)));
                availablePartitions.add(businessObjectDataHelper.getPrimaryAndSubPartitionValues(businessObjectDataKey));

                // For the result storage units, the storage platform could be "Glacier", since Glacier storage name might be specified in the request.
                if (StoragePlatformEntity.GLACIER.equals(storageUnitEntity.getStorage().getStoragePlatform().getName()))
                {
                    // For a Glacier storage, add the storage unit to the "not-available" statuses list with the "ARCHIVED" reason.
                    BusinessObjectDataStatus businessObjectDataStatus = createAvailableBusinessObjectDataStatus(businessObjectDataEntity);
                    businessObjectDataStatus.setReason(REASON_ARCHIVED);
                    notAvailableStatuses.add(businessObjectDataStatus);
                }
                else
                {
                    // For a non-Glacier storage, add the storage unit to the "available" statuses list.
                    availableStatuses.add(createAvailableBusinessObjectDataStatus(businessObjectDataEntity));
                }

                businessObjectDataToStorageUnitMap.put(businessObjectDataEntity, storageUnitEntity);
            }
        }

        // Check if request specifies to include all registered sub-partitions in the response.
        boolean includeAllRegisteredSubPartitions =
            request.getBusinessObjectDataVersion() == null && BooleanUtils.isTrue(request.isIncludeAllRegisteredSubPartitions());

        // If request specifies to include all registered sub-partitions in the response, query all
        // matched partition filters one more time to discover any non-available registered sub-partitions.
        if (includeAllRegisteredSubPartitions && !CollectionUtils.isEmpty(matchedAvailablePartitionFilters))
        {
            addNotAvailableBusinessObjectDataStatuses(notAvailableStatuses, businessObjectFormatKey, matchedAvailablePartitionFilters, availablePartitions,
                storageNames);
        }

        // Get a list of unmatched partition filters.
        List<List<String>> unmatchedPartitionFilters = new ArrayList<>(partitionFilters);
        unmatchedPartitionFilters.removeAll(matchedAvailablePartitionFilters);

        // We still need to try to retrieve business object data per list of unmatched filters regardless of business object data and/or storage unit statuses.
        // This is done to populate not-available statuses with legitimate reasons.
        // Business object data availability works across all storage platform types, so the storage platform type is not specified in the herdDao call.
        // We want to select any existing storage units regardless of their status, so we pass "false" for selectOnlyAvailableStorageUnits parameter.
        List<StorageUnitEntity> notAvailableStorageUnitEntities = storageUnitDao
            .getStorageUnitsByPartitionFiltersAndStorages(businessObjectFormatKey, unmatchedPartitionFilters, request.getBusinessObjectDataVersion(), null,
                storageNames, null, StoragePlatformEntity.GLACIER, false);

        // For all unmatched filters, select "available" storage units in any storages of the GLACIER storage platform type.
        // This is done to be able to check if business object data with a "non-available" storage unit is actually archived.
        // We want to select only "available" storage units, so we pass "true" for selectOnlyAvailableStorageUnits parameter.
        List<StorageUnitEntity> archivedStorageUnitEntities = storageUnitDao
            .getStorageUnitsByPartitionFiltersAndStorages(businessObjectFormatKey, unmatchedPartitionFilters, request.getBusinessObjectDataVersion(), null,
                null, StoragePlatformEntity.GLACIER, null, true);

        // Populate a set of archived business object data entities for easy access. Please note that business object data might be archived in more than
        // one Glacier storage.
        Set<BusinessObjectDataEntity> archivedBusinessObjectDataEntities = storageUnitHelper.getBusinessObjectDataEntitiesSet(archivedStorageUnitEntities);

        // Populate the not-available statuses list.
        addNotAvailableBusinessObjectDataStatuses(notAvailableStatuses, notAvailableStorageUnitEntities, archivedBusinessObjectDataEntities);

        // Build a list of matched "not-available" partition filters.
        // Please note that each request partition filter might result in multiple available business object data entities.
        List<List<String>> matchedNotAvailablePartitionFilters = getPartitionFilters(notAvailableStorageUnitEntities, partitionFilters.get(0));

        // Update the list of unmatched partition filters.
        unmatchedPartitionFilters.removeAll(matchedNotAvailablePartitionFilters);

        // Populate the "not available" statuses per remaining unmatched filters.
        for (List<String> unmatchedPartitionFilter : unmatchedPartitionFilters)
        {
            notAvailableStatuses.add(createNotAvailableBusinessObjectDataStatus(request, unmatchedPartitionFilter, REASON_NOT_REGISTERED));
        }

        return businessObjectDataAvailability;
    }

    /**
     * Performs an availability check for a collection of business object data.
     *
     * @param businessObjectDataAvailabilityCollectionRequest the business object data availability collection requests
     *
     * @return the business object data availability information
     */
    protected BusinessObjectDataAvailabilityCollectionResponse checkBusinessObjectDataAvailabilityCollectionImpl(
        BusinessObjectDataAvailabilityCollectionRequest businessObjectDataAvailabilityCollectionRequest)
    {
        // Perform the validation of the entire request, before we start processing the individual requests that requires the database access.
        validateBusinessObjectDataAvailabilityCollectionRequest(businessObjectDataAvailabilityCollectionRequest);

        // Process the individual requests and build the response.
        BusinessObjectDataAvailabilityCollectionResponse businessObjectDataAvailabilityCollectionResponse =
            new BusinessObjectDataAvailabilityCollectionResponse();
        List<BusinessObjectDataAvailability> businessObjectDataAvailabilityResponses = new ArrayList<>();
        businessObjectDataAvailabilityCollectionResponse.setBusinessObjectDataAvailabilityResponses(businessObjectDataAvailabilityResponses);
        boolean isAllDataAvailable = true;
        boolean isAllDataNotAvailable = true;
        for (BusinessObjectDataAvailabilityRequest request : businessObjectDataAvailabilityCollectionRequest.getBusinessObjectDataAvailabilityRequests())
        {
            // Please note that when calling to process individual availability requests, we ask to skip the request validation and trimming step.
            BusinessObjectDataAvailability businessObjectDataAvailability = checkBusinessObjectDataAvailabilityImpl(request, true);
            businessObjectDataAvailabilityResponses.add(businessObjectDataAvailability);
            isAllDataAvailable = isAllDataAvailable && businessObjectDataAvailability.getNotAvailableStatuses().isEmpty();
            isAllDataNotAvailable = isAllDataNotAvailable && businessObjectDataAvailability.getAvailableStatuses().isEmpty();
        }
        businessObjectDataAvailabilityCollectionResponse.setIsAllDataAvailable(isAllDataAvailable);
        businessObjectDataAvailabilityCollectionResponse.setIsAllDataNotAvailable(isAllDataNotAvailable);

        return businessObjectDataAvailabilityCollectionResponse;
    }

    @NamespacePermission(fields = "#request.namespace", permissions = NamespacePermissionEnum.READ)
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BusinessObjectDataDdl generateBusinessObjectDataDdl(BusinessObjectDataDdlRequest request)
    {
        return generateBusinessObjectDataDdlImpl(request, false);
    }

    @NamespacePermission(fields = "#request?.businessObjectDataDdlRequests?.![namespace]", permissions = NamespacePermissionEnum.READ)
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BusinessObjectDataDdlCollectionResponse generateBusinessObjectDataDdlCollection(BusinessObjectDataDdlCollectionRequest request)
    {
        return generateBusinessObjectDataDdlCollectionImpl(request);
    }

    /**
     * Retrieves the DDL to initialize the specified type of the database system to perform queries for a range of requested business object data in the
     * specified storage.
     *
     * @param request the business object data DDL request
     * @param skipRequestValidation specifies whether to skip the request validation and trimming
     *
     * @return the business object data DDL information
     */
    protected BusinessObjectDataDdl generateBusinessObjectDataDdlImpl(BusinessObjectDataDdlRequest request, boolean skipRequestValidation)
    {
        // Perform the validation.
        if (!skipRequestValidation)
        {
            validateBusinessObjectDataDdlRequest(request);
        }

        // Get the business object format entity for the specified parameters and make sure it exists.
        // Please note that when format version is not specified, we should get back the latest format version.
        BusinessObjectFormatEntity businessObjectFormatEntity = businessObjectFormatDaoHelper.getBusinessObjectFormatEntity(
            new BusinessObjectFormatKey(request.getNamespace(), request.getBusinessObjectDefinitionName(), request.getBusinessObjectFormatUsage(),
                request.getBusinessObjectFormatFileType(), request.getBusinessObjectFormatVersion()));

        // Validate that format has schema information.
        Assert.notEmpty(businessObjectFormatEntity.getSchemaColumns(), String.format(
            "Business object format with namespace \"%s\", business object definition name \"%s\", format usage \"%s\", format file type \"%s\"," +
                " and format version \"%s\" doesn't have schema information.",
            businessObjectFormatEntity.getBusinessObjectDefinition().getNamespace().getCode(),
            businessObjectFormatEntity.getBusinessObjectDefinition().getName(), businessObjectFormatEntity.getUsage(),
            businessObjectFormatEntity.getFileType().getCode(), businessObjectFormatEntity.getBusinessObjectFormatVersion()));

        // If it was specified, retrieve the custom DDL and ensure it exists.
        CustomDdlEntity customDdlEntity = null;
        if (StringUtils.isNotBlank(request.getCustomDdlName()))
        {
            CustomDdlKey customDdlKey = new CustomDdlKey(businessObjectFormatEntity.getBusinessObjectDefinition().getNamespace().getCode(),
                businessObjectFormatEntity.getBusinessObjectDefinition().getName(), businessObjectFormatEntity.getUsage(),
                businessObjectFormatEntity.getFileType().getCode(), businessObjectFormatEntity.getBusinessObjectFormatVersion(), request.getCustomDdlName());
            customDdlEntity = customDdlDaoHelper.getCustomDdlEntity(customDdlKey);
        }

        // Validate that specified storages exist and of a proper storage platform type.
        List<String> storageNames = new ArrayList<>();
        if (StringUtils.isNotBlank(request.getStorageName()))
        {
            storageNames.add(request.getStorageName());
        }
        if (!CollectionUtils.isEmpty(request.getStorageNames()))
        {
            storageNames.addAll(request.getStorageNames());
        }
        List<StorageEntity> storageEntities = new ArrayList<>();
        for (String storageName : storageNames)
        {
            StorageEntity storageEntity = storageDaoHelper.getStorageEntity(storageName);

            // Only S3 storage platform is currently supported.
            Assert.isTrue(storageEntity.getStoragePlatform().getName().equals(StoragePlatformEntity.S3),
                String.format("Cannot generate DDL for \"%s\" storage platform.", storageEntity.getStoragePlatform().getName()));

            storageEntities.add(storageEntity);
        }

        // Validate that all storages have S3 bucket name configured.
        Map<StorageEntity, String> s3BucketNames = new HashMap<>();
        for (StorageEntity storageEntity : storageEntities)
        {
            // Please note that since S3 bucket name attribute value is required we pass a "true" flag.
            String s3BucketName = storageHelper
                .getStorageAttributeValueByName(configurationHelper.getProperty(ConfigurationValue.S3_ATTRIBUTE_NAME_BUCKET_NAME), storageEntity, true);
            s3BucketNames.put(storageEntity, s3BucketName);
        }

        // Create and initialize a business object data DDL object instance.
        BusinessObjectDataDdl businessObjectDataDdl = createBusinessObjectDataDdl(request);
        businessObjectDataDdl.setDdl(ddlGeneratorFactory.getDdlGenerator(request.getOutputFormat())
            .generateCreateTableDdl(request, businessObjectFormatEntity, customDdlEntity, storageNames, storageEntities, s3BucketNames));

        return businessObjectDataDdl;
    }

    /**
     * Retrieves the DDL to initialize the specified type of the database system to perform queries for a collection of business object data in the specified
     * storages.
     *
     * @param businessObjectDataDdlCollectionRequest the business object data DDL collection request
     *
     * @return the business object data DDL information
     */
    protected BusinessObjectDataDdlCollectionResponse generateBusinessObjectDataDdlCollectionImpl(
        BusinessObjectDataDdlCollectionRequest businessObjectDataDdlCollectionRequest)
    {
        // Perform the validation of the entire request, before we start processing the individual requests that requires the database access.
        validateBusinessObjectDataDdlCollectionRequest(businessObjectDataDdlCollectionRequest);

        // Process the individual requests and build the response.
        BusinessObjectDataDdlCollectionResponse businessObjectDataDdlCollectionResponse = new BusinessObjectDataDdlCollectionResponse();
        List<BusinessObjectDataDdl> businessObjectDataDdlResponses = new ArrayList<>();
        businessObjectDataDdlCollectionResponse.setBusinessObjectDataDdlResponses(businessObjectDataDdlResponses);
        List<String> ddls = new ArrayList<>();
        for (BusinessObjectDataDdlRequest request : businessObjectDataDdlCollectionRequest.getBusinessObjectDataDdlRequests())
        {
            // Please note that when calling to process individual ddl requests, we ask to skip the request validation and trimming step.
            BusinessObjectDataDdl businessObjectDataDdl = generateBusinessObjectDataDdlImpl(request, true);
            businessObjectDataDdlResponses.add(businessObjectDataDdl);
            ddls.add(businessObjectDataDdl.getDdl());
        }
        businessObjectDataDdlCollectionResponse.setDdlCollection(StringUtils.join(ddls, "\n\n"));

        return businessObjectDataDdlCollectionResponse;
    }

    /**
     * Validates a business object data availability collection request. This method also trims appropriate request parameters.
     *
     * @param businessObjectDataAvailabilityCollectionRequest the request
     *
     * @throws IllegalArgumentException if any validation errors were found
     */
    private void validateBusinessObjectDataAvailabilityCollectionRequest(
        BusinessObjectDataAvailabilityCollectionRequest businessObjectDataAvailabilityCollectionRequest)
    {
        Assert.notNull(businessObjectDataAvailabilityCollectionRequest, "A business object data availability collection request must be specified.");

        Assert.isTrue(!CollectionUtils.isEmpty(businessObjectDataAvailabilityCollectionRequest.getBusinessObjectDataAvailabilityRequests()),
            "At least one business object data availability request must be specified.");

        for (BusinessObjectDataAvailabilityRequest request : businessObjectDataAvailabilityCollectionRequest.getBusinessObjectDataAvailabilityRequests())
        {
            validateBusinessObjectDataAvailabilityRequest(request);
        }
    }

    /**
     * Validates the business object data availability request. This method also trims appropriate request parameters.
     *
     * @param request the request
     *
     * @throws IllegalArgumentException if any validation errors were found
     */
    private void validateBusinessObjectDataAvailabilityRequest(BusinessObjectDataAvailabilityRequest request)
    {
        Assert.notNull(request, "A business object data availability request must be specified.");

        // Validate and trim the request parameters.
        Assert.hasText(request.getNamespace(), "A namespace must be specified.");
        request.setNamespace(request.getNamespace().trim());

        Assert.hasText(request.getBusinessObjectDefinitionName(), "A business object definition name must be specified.");
        request.setBusinessObjectDefinitionName(request.getBusinessObjectDefinitionName().trim());

        Assert.hasText(request.getBusinessObjectFormatUsage(), "A business object format usage must be specified.");
        request.setBusinessObjectFormatUsage(request.getBusinessObjectFormatUsage().trim());

        Assert.hasText(request.getBusinessObjectFormatFileType(), "A business object format file type must be specified.");
        request.setBusinessObjectFormatFileType(request.getBusinessObjectFormatFileType().trim());

        // Validate the partition value filters. Allow partition value tokens to be specified.
        businessObjectDataHelper.validatePartitionValueFilters(request.getPartitionValueFilters(), request.getPartitionValueFilter(), true);

        // Make sure that request does not contain both a list of storage names and a standalone storage name.
        Assert.isTrue(request.getStorageNames() == null || request.getStorageName() == null,
            "A list of storage names and a standalone storage name cannot be both specified.");

        // Trim the standalone storage name, if specified.
        if (request.getStorageName() != null)
        {
            Assert.hasText(request.getStorageName(), "A storage name must be specified.");
            request.setStorageName(request.getStorageName().trim());
        }

        // Validate and trim the list of storage names.
        if (!CollectionUtils.isEmpty(request.getStorageNames()))
        {
            for (int i = 0; i < request.getStorageNames().size(); i++)
            {
                Assert.hasText(request.getStorageNames().get(i), "A storage name must be specified.");
                request.getStorageNames().set(i, request.getStorageNames().get(i).trim());
            }
        }
    }

    /**
     * Gets business object format key from the business object data availability request.
     *
     * @param request the business object data availability request
     *
     * @return the business object format key
     */
    private BusinessObjectFormatKey getBusinessObjectFormatKey(BusinessObjectDataAvailabilityRequest request)
    {
        return new BusinessObjectFormatKey(request.getNamespace(), request.getBusinessObjectDefinitionName(), request.getBusinessObjectFormatUsage(),
            request.getBusinessObjectFormatFileType(), request.getBusinessObjectFormatVersion());
    }

    /**
     * Gets storage names from the business object data availability request.
     *
     * @param request the business object data availability request
     *
     * @return the list of storage names
     */
    private List<String> getStorageNames(BusinessObjectDataAvailabilityRequest request)
    {
        List<String> storageNames = new ArrayList<>();

        if (StringUtils.isNotBlank(request.getStorageName()))
        {
            storageNames.add(request.getStorageName());
        }

        if (!CollectionUtils.isEmpty(request.getStorageNames()))
        {
            storageNames.addAll(request.getStorageNames());
        }

        return storageNames;
    }

    /**
     * Validates a business object data DDL collection request. This method also trims appropriate request parameters.
     *
     * @param businessObjectDataDdlCollectionRequest the request
     *
     * @throws IllegalArgumentException if any validation errors were found
     */
    private void validateBusinessObjectDataDdlCollectionRequest(BusinessObjectDataDdlCollectionRequest businessObjectDataDdlCollectionRequest)
    {
        Assert.notNull(businessObjectDataDdlCollectionRequest, "A business object data DDL collection request must be specified.");

        Assert.isTrue(!CollectionUtils.isEmpty(businessObjectDataDdlCollectionRequest.getBusinessObjectDataDdlRequests()),
            "At least one business object data DDL request must be specified.");

        for (BusinessObjectDataDdlRequest request : businessObjectDataDdlCollectionRequest.getBusinessObjectDataDdlRequests())
        {
            validateBusinessObjectDataDdlRequest(request);
        }
    }

    /**
     * Validates the business object data DDL request. This method also trims appropriate request parameters.
     *
     * @param request the request
     *
     * @throws IllegalArgumentException if any validation errors were found
     */
    private void validateBusinessObjectDataDdlRequest(BusinessObjectDataDdlRequest request)
    {
        Assert.notNull(request, "A business object data DDL request must be specified.");

        // Validate and trim the request parameters.
        Assert.hasText(request.getNamespace(), "A namespace must be specified.");
        request.setNamespace(request.getNamespace().trim());

        Assert.hasText(request.getBusinessObjectDefinitionName(), "A business object definition name must be specified.");
        request.setBusinessObjectDefinitionName(request.getBusinessObjectDefinitionName().trim());

        Assert.hasText(request.getBusinessObjectFormatUsage(), "A business object format usage must be specified.");
        request.setBusinessObjectFormatUsage(request.getBusinessObjectFormatUsage().trim());

        Assert.hasText(request.getBusinessObjectFormatFileType(), "A business object format file type must be specified.");
        request.setBusinessObjectFormatFileType(request.getBusinessObjectFormatFileType().trim());

        // Validate the partition value filters. Do not allow partition value tokens to be specified.
        businessObjectDataHelper.validatePartitionValueFilters(request.getPartitionValueFilters(), request.getPartitionValueFilter(), false);

        // Make sure that request does not contain both a list of storage names and a standalone storage name.
        Assert.isTrue(request.getStorageNames() == null || request.getStorageName() == null,
            "A list of storage names and a standalone storage name cannot be both specified.");

        // Trim the standalone storage name, if specified.
        if (request.getStorageName() != null)
        {
            Assert.hasText(request.getStorageName(), "A storage name must be specified.");
            request.setStorageName(request.getStorageName().trim());
        }

        // Validate and trim the list of storage names.
        if (!CollectionUtils.isEmpty(request.getStorageNames()))
        {
            for (int i = 0; i < request.getStorageNames().size(); i++)
            {
                Assert.hasText(request.getStorageNames().get(i), "A storage name must be specified.");
                request.getStorageNames().set(i, request.getStorageNames().get(i).trim());
            }
        }

        Assert.notNull(request.getOutputFormat(), "An output format must be specified.");

        Assert.hasText(request.getTableName(), "A table name must be specified.");
        request.setTableName(request.getTableName().trim());

        if (StringUtils.isNotBlank(request.getCustomDdlName()))
        {
            request.setCustomDdlName(request.getCustomDdlName().trim());
        }
    }


    /**
     * Creates business object data availability object instance and initialise it with the business object data availability request field values.
     *
     * @param request the business object data availability request
     *
     * @return the newly created BusinessObjectDataAvailability object instance
     */
    private BusinessObjectDataAvailability createBusinessObjectDataAvailability(BusinessObjectDataAvailabilityRequest request)
    {
        BusinessObjectDataAvailability businessObjectDataAvailability = new BusinessObjectDataAvailability();

        businessObjectDataAvailability.setNamespace(request.getNamespace());
        businessObjectDataAvailability.setBusinessObjectDefinitionName(request.getBusinessObjectDefinitionName());
        businessObjectDataAvailability.setBusinessObjectFormatUsage(request.getBusinessObjectFormatUsage());
        businessObjectDataAvailability.setBusinessObjectFormatFileType(request.getBusinessObjectFormatFileType());
        businessObjectDataAvailability.setBusinessObjectFormatVersion(request.getBusinessObjectFormatVersion());

        businessObjectDataAvailability.setPartitionValueFilters(request.getPartitionValueFilters());
        businessObjectDataAvailability.setPartitionValueFilter(request.getPartitionValueFilter());

        businessObjectDataAvailability.setBusinessObjectDataVersion(request.getBusinessObjectDataVersion());

        businessObjectDataAvailability.setStorageNames(request.getStorageNames());
        businessObjectDataAvailability.setStorageName(request.getStorageName());

        return businessObjectDataAvailability;
    }

    /**
     * Creates a business object data status instance from the business object data entity.
     *
     * @param businessObjectDataEntity the business object data entity
     *
     * @return the business object data status instance
     */
    private BusinessObjectDataStatus createAvailableBusinessObjectDataStatus(BusinessObjectDataEntity businessObjectDataEntity)
    {
        BusinessObjectDataStatus businessObjectDataStatus = new BusinessObjectDataStatus();

        businessObjectDataStatus.setBusinessObjectFormatVersion(businessObjectDataEntity.getBusinessObjectFormat().getBusinessObjectFormatVersion());
        businessObjectDataStatus.setPartitionValue(businessObjectDataEntity.getPartitionValue());
        businessObjectDataStatus.setSubPartitionValues(businessObjectDataHelper.getSubPartitionValues(businessObjectDataEntity));
        businessObjectDataStatus.setBusinessObjectDataVersion(businessObjectDataEntity.getVersion());
        businessObjectDataStatus.setReason(businessObjectDataEntity.getStatus().getCode());

        return businessObjectDataStatus;
    }

    /**
     * Updates the list of not-available statuses by adding business object data status instances created from discovered "non-available" registered
     * sub-partitions as per list of "matched" partition filters to the specified list of not-available statuses.
     *
     * @param notAvailableStatuses the list of not-available statuses to be updated
     * @param businessObjectFormatKey the business object format key
     * @param matchedAvailablePartitionFilters the list of "matched" partition filters
     * @param availablePartitions the list of already discovered "available" partitions, where each partition consists of primary and optional sub-partition
     * values
     * @param storageNames the list of storage names
     */
    protected void addNotAvailableBusinessObjectDataStatuses(List<BusinessObjectDataStatus> notAvailableStatuses,
        BusinessObjectFormatKey businessObjectFormatKey, List<List<String>> matchedAvailablePartitionFilters, List<List<String>> availablePartitions,
        List<String> storageNames)
    {
        // Now try to retrieve latest business object data per list of matched filters regardless of business object data and/or storage unit statuses.
        // This is done to include all registered sub-partitions in the response.
        // Business object data availability works across all storage platform types, so the storage platform type is not specified in the herdDao call.
        // We want to select any existing storage units regardless of their status, so we pass "false" for selectOnlyAvailableStorageUnits parameter.
        List<StorageUnitEntity> matchedNotAvailableStorageUnitEntities = storageUnitDao
            .getStorageUnitsByPartitionFiltersAndStorages(businessObjectFormatKey, matchedAvailablePartitionFilters, null, null, storageNames, null,
                StoragePlatformEntity.GLACIER, false);

        // Exclude all storage units with business object data having "DELETED" status.
        matchedNotAvailableStorageUnitEntities =
            storageUnitHelper.excludeBusinessObjectDataStatus(matchedNotAvailableStorageUnitEntities, BusinessObjectDataStatusEntity.DELETED);

        // Exclude all already discovered "available" partitions. Please note that, since we got here, the list of matched partitions can not be empty.
        matchedNotAvailableStorageUnitEntities = storageUnitHelper.excludePartitions(matchedNotAvailableStorageUnitEntities, availablePartitions);

        // Keep processing the matched "not available" storage units only when the list is not empty.
        if (!CollectionUtils.isEmpty(matchedNotAvailableStorageUnitEntities))
        {
            // Also, for all matched filters, select "available" storage units in any storages of the GLACIER storage platform type.
            // This is done to be able to check if business object data with a "non-available" storage unit is actually archived.
            // We want to select only "available" storage units, so we pass "true" for selectOnlyAvailableStorageUnits parameter.
            List<StorageUnitEntity> matchedArchivedStorageUnitEntities = storageUnitDao
                .getStorageUnitsByPartitionFiltersAndStorages(businessObjectFormatKey, matchedAvailablePartitionFilters, null, null, null,
                    StoragePlatformEntity.GLACIER, null, true);

            // Populate a set of archived business object data entities for easy access.
            Set<BusinessObjectDataEntity> matchedArchivedBusinessObjectDataEntities =
                storageUnitHelper.getBusinessObjectDataEntitiesSet(matchedArchivedStorageUnitEntities);

            // Populate the "not available" statuses with all found "not available" registered sub-partitions.
            addNotAvailableBusinessObjectDataStatuses(notAvailableStatuses, matchedNotAvailableStorageUnitEntities, matchedArchivedBusinessObjectDataEntities);
        }
    }

    /**
     * Adds business object data status instances created from the list of storage unit entities to the specified list of not-available statuses.
     *
     * @param notAvailableStatuses the list of not-available statuses
     * @param storageUnitEntities the list of storage unit entities
     * @param archivedBusinessObjectDataEntities the set of archived business object data entities, not null
     */
    private void addNotAvailableBusinessObjectDataStatuses(List<BusinessObjectDataStatus> notAvailableStatuses, List<StorageUnitEntity> storageUnitEntities,
        Set<BusinessObjectDataEntity> archivedBusinessObjectDataEntities)
    {
        for (StorageUnitEntity storageUnitEntity : storageUnitEntities)
        {
            notAvailableStatuses.add(createNotAvailableBusinessObjectDataStatus(storageUnitEntity, archivedBusinessObjectDataEntities));
        }
    }

    /**
     * Creates a business object data status instance from the storage unit entity.
     *
     * @param storageUnitEntity the storage unit entity
     * @param archivedBusinessObjectDataEntities the set of archived business object data entities, not null
     *
     * @return the business object data status instance
     */
    private BusinessObjectDataStatus createNotAvailableBusinessObjectDataStatus(StorageUnitEntity storageUnitEntity,
        Set<BusinessObjectDataEntity> archivedBusinessObjectDataEntities)
    {
        // Get the business object entity.
        BusinessObjectDataEntity businessObjectDataEntity = storageUnitEntity.getBusinessObjectData();

        // Create and populate the business object data status instance.
        BusinessObjectDataStatus businessObjectDataStatus = new BusinessObjectDataStatus();

        businessObjectDataStatus.setBusinessObjectFormatVersion(businessObjectDataEntity.getBusinessObjectFormat().getBusinessObjectFormatVersion());
        businessObjectDataStatus.setPartitionValue(businessObjectDataEntity.getPartitionValue());
        businessObjectDataStatus.setSubPartitionValues(businessObjectDataHelper.getSubPartitionValues(businessObjectDataEntity));
        businessObjectDataStatus.setBusinessObjectDataVersion(businessObjectDataEntity.getVersion());

        if (storageUnitEntity.getStatus().getAvailable())
        {
            // Storage unit is "available", so business object data is selected as "non-available" due to its business object data status.
            businessObjectDataStatus.setReason(businessObjectDataEntity.getStatus().getCode());
        }
        else if (archivedBusinessObjectDataEntities.contains(storageUnitEntity.getBusinessObjectData()))
        {
            // Storage unit is not "available", but business object data is archived.
            businessObjectDataStatus.setReason(BusinessObjectDataServiceImpl.REASON_ARCHIVED);
        }
        else
        {
            businessObjectDataStatus.setReason(BusinessObjectDataServiceImpl.REASON_NO_ENABLED_STORAGE_UNIT);
        }

        return businessObjectDataStatus;
    }

    /**
     * Creates the business object data status.
     *
     * @param businessObjectDataAvailabilityRequest the business object data availability request
     * @param unmatchedPartitionFilter the partition filter that got no matched business object data instances
     * @param reason the reason for the business object data not being available
     *
     * @return the business object data status
     */
    private BusinessObjectDataStatus createNotAvailableBusinessObjectDataStatus(BusinessObjectDataAvailabilityRequest businessObjectDataAvailabilityRequest,
        List<String> unmatchedPartitionFilter, String reason)
    {
        BusinessObjectDataStatus businessObjectDataStatus = new BusinessObjectDataStatus();

        // Populate business object data status values using the business object data availability request.
        businessObjectDataStatus.setBusinessObjectFormatVersion(businessObjectDataAvailabilityRequest.getBusinessObjectFormatVersion());

        // When list of partition value filters is used, we populate primary and/or sub-partition values.
        if (businessObjectDataAvailabilityRequest.getPartitionValueFilters() != null)
        {
            // Replace all null partition values with an empty string.
            replaceAllNullsWithEmptyString(unmatchedPartitionFilter);

            // Populate primary and sub-partition values from the unmatched partition filter.
            businessObjectDataStatus.setPartitionValue(unmatchedPartitionFilter.get(0));
            businessObjectDataStatus.setSubPartitionValues(unmatchedPartitionFilter.subList(1, unmatchedPartitionFilter.size()));
        }
        // Otherwise, for backwards compatibility, populate primary partition value only per expected single partition value from the unmatched filter.
        else
        {
            // Since the availability request contains a standalone partition value filter,
            // the unmatched partition filter is expected to contain only a single partition value.
            for (String partitionValue : unmatchedPartitionFilter)
            {
                if (partitionValue != null)
                {
                    businessObjectDataStatus.setPartitionValue(partitionValue);
                    break;
                }
            }
        }
        businessObjectDataStatus.setBusinessObjectDataVersion(businessObjectDataAvailabilityRequest.getBusinessObjectDataVersion());
        businessObjectDataStatus.setReason(reason);

        return businessObjectDataStatus;
    }

    /**
     * Gets a list of matched partition filters per specified list of storage unit entities and a sample partition filter.
     *
     * @param storageUnitEntities the list of storage unit entities
     * @param samplePartitionFilter the sample partition filter
     *
     * @return the list of partition filters
     */
    private List<List<String>> getPartitionFilters(List<StorageUnitEntity> storageUnitEntities, List<String> samplePartitionFilter)
    {
        List<List<String>> partitionFilters = new ArrayList<>();

        for (StorageUnitEntity storageUnitEntity : storageUnitEntities)
        {
            BusinessObjectDataKey businessObjectDataKey = businessObjectDataHelper.getBusinessObjectDataKey(storageUnitEntity.getBusinessObjectData());
            partitionFilters.add(businessObjectDataHelper.getPartitionFilter(businessObjectDataKey, samplePartitionFilter));
        }

        return partitionFilters;
    }

    /**
     * Creates business object data ddl object instance and initialise it with the business object data ddl request field values.
     *
     * @param request the business object data ddl request
     *
     * @return the newly created BusinessObjectDataDdl object instance
     */
    private BusinessObjectDataDdl createBusinessObjectDataDdl(BusinessObjectDataDdlRequest request)
    {
        BusinessObjectDataDdl businessObjectDataDdl = new BusinessObjectDataDdl();

        businessObjectDataDdl.setNamespace(request.getNamespace());
        businessObjectDataDdl.setBusinessObjectDefinitionName(request.getBusinessObjectDefinitionName());
        businessObjectDataDdl.setBusinessObjectFormatUsage(request.getBusinessObjectFormatUsage());
        businessObjectDataDdl.setBusinessObjectFormatFileType(request.getBusinessObjectFormatFileType());
        businessObjectDataDdl.setBusinessObjectFormatVersion(request.getBusinessObjectFormatVersion());

        businessObjectDataDdl.setPartitionValueFilters(request.getPartitionValueFilters());
        businessObjectDataDdl.setPartitionValueFilter(request.getPartitionValueFilter());

        businessObjectDataDdl.setBusinessObjectDataVersion(request.getBusinessObjectDataVersion());

        businessObjectDataDdl.setStorageNames(request.getStorageNames());
        businessObjectDataDdl.setStorageName(request.getStorageName());

        businessObjectDataDdl.setOutputFormat(request.getOutputFormat());
        businessObjectDataDdl.setTableName(request.getTableName());
        businessObjectDataDdl.setCustomDdlName(request.getCustomDdlName());

        return businessObjectDataDdl;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Delegates implementation to {@link org.finra.herd.service.helper.BusinessObjectDataInvalidateUnregisteredHelper}. Starts a new transaction. Meant for
     * Activiti wrapper usage.
     */
    @PublishJmsMessages
    @NamespacePermission(fields = "#businessObjectDataInvalidateUnregisteredRequest.namespace", permissions = NamespacePermissionEnum.WRITE)
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BusinessObjectDataInvalidateUnregisteredResponse invalidateUnregisteredBusinessObjectData(
        BusinessObjectDataInvalidateUnregisteredRequest businessObjectDataInvalidateUnregisteredRequest)
    {
        return invalidateUnregisteredBusinessObjectDataImpl(businessObjectDataInvalidateUnregisteredRequest);
    }

    /**
     * Delegates implementation to {@link org.finra.herd.service.helper.BusinessObjectDataInvalidateUnregisteredHelper}. Keeps current transaction context.
     *
     * @param businessObjectDataInvalidateUnregisteredRequest {@link org.finra.herd.model.api.xml.BusinessObjectDataInvalidateUnregisteredRequest}
     *
     * @return {@link BusinessObjectDataInvalidateUnregisteredResponse}
     */
    protected BusinessObjectDataInvalidateUnregisteredResponse invalidateUnregisteredBusinessObjectDataImpl(
        BusinessObjectDataInvalidateUnregisteredRequest businessObjectDataInvalidateUnregisteredRequest)
    {
        return businessObjectDataInvalidateUnregisteredHelper.invalidateUnregisteredBusinessObjectData(businessObjectDataInvalidateUnregisteredRequest);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation executes non-transactionally, suspends the current transaction if one exists.
     */
    @NamespacePermission(fields = "#businessObjectDataKey.namespace", permissions = NamespacePermissionEnum.WRITE)
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BusinessObjectData retryStoragePolicyTransition(BusinessObjectDataKey businessObjectDataKey,
        BusinessObjectDataRetryStoragePolicyTransitionRequest request)
    {
        return retryStoragePolicyTransitionImpl(businessObjectDataKey, request);
    }

    /**
     * Retries a storage policy transition by forcing re-initiation of the archiving process for the specified business object data that is still in progress of
     * a valid archiving operation.
     *
     * @param businessObjectDataKey the business object data key
     * @param request the information needed to retry a storage policy transition
     *
     * @return the business object data information
     */
    protected BusinessObjectData retryStoragePolicyTransitionImpl(BusinessObjectDataKey businessObjectDataKey,
        BusinessObjectDataRetryStoragePolicyTransitionRequest request)
    {
        // Prepare to retry a storage policy transition.
        BusinessObjectDataRetryStoragePolicyTransitionDto businessObjectDataRetryStoragePolicyTransitionDto =
            businessObjectDataRetryStoragePolicyTransitionHelperService.prepareToRetryStoragePolicyTransition(businessObjectDataKey, request);

        // Execute AWS specific steps needed to retry a storage policy transition.
        businessObjectDataRetryStoragePolicyTransitionHelperService.executeAwsSpecificSteps(businessObjectDataRetryStoragePolicyTransitionDto);

        // Execute the after step for the retry a storage policy transition and return the business object data information.
        return businessObjectDataRetryStoragePolicyTransitionHelperService
            .executeRetryStoragePolicyTransitionAfterStep(businessObjectDataRetryStoragePolicyTransitionDto);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation executes non-transactionally, suspends the current transaction if one exists.
     */
    @NamespacePermission(fields = "#businessObjectDataKey.namespace", permissions = NamespacePermissionEnum.WRITE)
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BusinessObjectData restoreBusinessObjectData(BusinessObjectDataKey businessObjectDataKey)
    {
        return restoreBusinessObjectDataImpl(businessObjectDataKey);
    }

    /**
     * Initiates a restore request for a currently archived business object data. Keeps current transaction context.
     *
     * @param businessObjectDataKey the business object data key
     *
     * @return the business object data information
     */
    protected BusinessObjectData restoreBusinessObjectDataImpl(BusinessObjectDataKey businessObjectDataKey)
    {
        // Execute the initiate a restore request before step.
        BusinessObjectDataRestoreDto businessObjectDataRestoreDto =
            businessObjectDataInitiateRestoreHelperService.prepareToInitiateRestore(businessObjectDataKey);

        // Create storage unit notification for the origin storage unit.
        notificationEventService.processStorageUnitNotificationEventAsync(NotificationEventTypeEntity.EventTypesStorageUnit.STRGE_UNIT_STTS_CHG,
            businessObjectDataRestoreDto.getBusinessObjectDataKey(), businessObjectDataRestoreDto.getOriginStorageName(),
            businessObjectDataRestoreDto.getNewOriginStorageUnitStatus(), businessObjectDataRestoreDto.getOldOriginStorageUnitStatus());

        // Initiate the restore request.
        businessObjectDataInitiateRestoreHelperService.executeS3SpecificSteps(businessObjectDataRestoreDto);

        // On failure of the above step, execute the "after" step, and re-throw the exception.
        if (businessObjectDataRestoreDto.getException() != null)
        {
            // On failure, execute the after step that updates the origin storage unit status to DISABLED.
            businessObjectDataInitiateRestoreHelperService.executeInitiateRestoreAfterStep(businessObjectDataRestoreDto);

            // Create storage unit notification for the origin storage unit.
            notificationEventService.processStorageUnitNotificationEventAsync(NotificationEventTypeEntity.EventTypesStorageUnit.STRGE_UNIT_STTS_CHG,
                businessObjectDataRestoreDto.getBusinessObjectDataKey(), businessObjectDataRestoreDto.getOriginStorageName(),
                businessObjectDataRestoreDto.getNewOriginStorageUnitStatus(), businessObjectDataRestoreDto.getOldOriginStorageUnitStatus());

            // Re-throw the original exception.
            throw new IllegalStateException(businessObjectDataRestoreDto.getException());
        }
        else
        {
            // Execute the after step for the initiate a business object data restore request
            // and return the business object data information.
            return businessObjectDataInitiateRestoreHelperService.executeInitiateRestoreAfterStep(businessObjectDataRestoreDto);
        }
    }

    /**
     * Replaces all null values in the specified list with empty strings.
     *
     * @param list the list of strings
     */
    private void replaceAllNullsWithEmptyString(List<String> list)
    {
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) == null)
            {
                list.set(i, "");
            }
        }
    }

    /**
     * Search business object data based on the request
     *
     * @param request search request
     *
     * @return business data search result
     */
    @NamespacePermission(fields = "#request.businessObjectDataSearchFilters[0].BusinessObjectDataSearchKeys[0].namespace",
        permissions = NamespacePermissionEnum.READ)
    @Override
    public BusinessObjectDataSearchResult searchBusinessObjectData(BusinessObjectDataSearchRequest request)
    {
        //TO DO check name space permission for all entries in the request.
        // validate search request
        businessObjectDataSearchHelper.validateBusinesObjectDataSearchRequest(request);

        // search business object data
        List<BusinessObjectData> businessObjectDataList = businessObjectDataDao.searchBusinessObjectData(request.getBusinessObjectDataSearchFilters());
        BusinessObjectDataSearchResult result = new BusinessObjectDataSearchResult();
        result.setBusinessObjectDataElements(businessObjectDataList);

        return result;
    }
}
