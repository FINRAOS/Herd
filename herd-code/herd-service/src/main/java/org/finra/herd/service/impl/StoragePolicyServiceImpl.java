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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import org.finra.herd.dao.StoragePolicyDao;
import org.finra.herd.dao.config.DaoSpringModuleConfig;
import org.finra.herd.model.AlreadyExistsException;
import org.finra.herd.model.annotation.NamespacePermission;
import org.finra.herd.model.annotation.NamespacePermissions;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionKey;
import org.finra.herd.model.api.xml.NamespacePermissionEnum;
import org.finra.herd.model.api.xml.StoragePolicy;
import org.finra.herd.model.api.xml.StoragePolicyCreateRequest;
import org.finra.herd.model.api.xml.StoragePolicyFilter;
import org.finra.herd.model.api.xml.StoragePolicyKey;
import org.finra.herd.model.api.xml.StoragePolicyRule;
import org.finra.herd.model.api.xml.StoragePolicyTransition;
import org.finra.herd.model.api.xml.StoragePolicyUpdateRequest;
import org.finra.herd.model.jpa.BusinessObjectDefinitionEntity;
import org.finra.herd.model.jpa.FileTypeEntity;
import org.finra.herd.model.jpa.NamespaceEntity;
import org.finra.herd.model.jpa.StorageEntity;
import org.finra.herd.model.jpa.StoragePolicyEntity;
import org.finra.herd.model.jpa.StoragePolicyRuleTypeEntity;
import org.finra.herd.model.jpa.StoragePolicyStatusEntity;
import org.finra.herd.model.jpa.StoragePolicyTransitionTypeEntity;
import org.finra.herd.service.StoragePolicyService;
import org.finra.herd.service.helper.AlternateKeyHelper;
import org.finra.herd.service.helper.BusinessObjectDefinitionDaoHelper;
import org.finra.herd.service.helper.FileTypeDaoHelper;
import org.finra.herd.service.helper.NamespaceDaoHelper;
import org.finra.herd.service.helper.StorageDaoHelper;
import org.finra.herd.service.helper.StoragePolicyDaoHelper;
import org.finra.herd.service.helper.StoragePolicyHelper;
import org.finra.herd.service.helper.StoragePolicyRuleTypeDaoHelper;
import org.finra.herd.service.helper.StoragePolicyStatusDaoHelper;
import org.finra.herd.service.helper.StoragePolicyTransitionTypeDaoHelper;

/**
 * The storage policy service implementation.
 */
@Service
@Transactional(value = DaoSpringModuleConfig.HERD_TRANSACTION_MANAGER_BEAN_NAME)
public class StoragePolicyServiceImpl implements StoragePolicyService
{
    @Autowired
    private AlternateKeyHelper alternateKeyHelper;

    @Autowired
    private BusinessObjectDefinitionDaoHelper businessObjectDefinitionDaoHelper;

    @Autowired
    private FileTypeDaoHelper fileTypeDaoHelper;

    @Autowired
    private NamespaceDaoHelper namespaceDaoHelper;

    @Autowired
    private StorageDaoHelper storageDaoHelper;

    @Autowired
    private StoragePolicyDao storagePolicyDao;

    @Autowired
    private StoragePolicyDaoHelper storagePolicyDaoHelper;

    @Autowired
    private StoragePolicyHelper storagePolicyHelper;

    @Autowired
    private StoragePolicyRuleTypeDaoHelper storagePolicyRuleTypeDaoHelper;

    @Autowired
    private StoragePolicyStatusDaoHelper storagePolicyStatusDaoHelper;

    @Autowired
    private StoragePolicyTransitionTypeDaoHelper storagePolicyTransitionTypeDaoHelper;

    @NamespacePermissions({@NamespacePermission(fields = "#request?.storagePolicyKey?.namespace", permissions = NamespacePermissionEnum.WRITE),
        @NamespacePermission(fields = "#request?.storagePolicyFilter?.namespace", permissions = NamespacePermissionEnum.READ)})
    @Override
    public StoragePolicy createStoragePolicy(StoragePolicyCreateRequest request)
    {
        // Validate and trim the request parameters.
        validateStoragePolicyCreateRequest(request);

        // Get the storage policy key.
        StoragePolicyKey storagePolicyKey = request.getStoragePolicyKey();

        // Ensure a storage policy with the specified name doesn't already exist for the specified namespace.
        StoragePolicyEntity storagePolicyEntity = storagePolicyDao.getStoragePolicyByAltKey(storagePolicyKey);
        if (storagePolicyEntity != null)
        {
            throw new AlreadyExistsException(String.format("Unable to create storage policy with name \"%s\" because it already exists for namespace \"%s\".",
                storagePolicyKey.getStoragePolicyName(), storagePolicyKey.getNamespace()));
        }

        // Retrieve and ensure that namespace exists with the specified storage policy namespace code.
        NamespaceEntity namespaceEntity = namespaceDaoHelper.getNamespaceEntity(storagePolicyKey.getNamespace());

        // Retrieve and ensure that storage policy rule type exists.
        StoragePolicyRuleTypeEntity storagePolicyRuleTypeEntity =
            storagePolicyRuleTypeDaoHelper.getStoragePolicyRuleTypeEntity(request.getStoragePolicyRule().getRuleType());

        // Get the storage policy filter.
        StoragePolicyFilter storagePolicyFilter = request.getStoragePolicyFilter();

        // If specified, retrieve and ensure that the business object definition exists.
        BusinessObjectDefinitionEntity businessObjectDefinitionEntity = null;
        if (StringUtils.isNotBlank(storagePolicyFilter.getBusinessObjectDefinitionName()))
        {
            businessObjectDefinitionEntity = businessObjectDefinitionDaoHelper.getBusinessObjectDefinitionEntity(
                new BusinessObjectDefinitionKey(storagePolicyFilter.getNamespace(), storagePolicyFilter.getBusinessObjectDefinitionName()));
        }

        // If specified, retrieve and ensure that file type exists.
        FileTypeEntity fileTypeEntity = null;
        if (StringUtils.isNotBlank(storagePolicyFilter.getBusinessObjectFormatFileType()))
        {
            fileTypeEntity = fileTypeDaoHelper.getFileTypeEntity(storagePolicyFilter.getBusinessObjectFormatFileType());
        }

        // Retrieve and ensure that storage policy filter storage exists.
        StorageEntity storageEntity = storageDaoHelper.getStorageEntity(storagePolicyFilter.getStorageName());

        // Validate the source storage.
        storagePolicyDaoHelper.validateStoragePolicyFilterStorage(storageEntity);

        // Retrieve and ensure that storage policy transition type exists.
        StoragePolicyTransitionTypeEntity storagePolicyTransitionTypeEntity =
            storagePolicyTransitionTypeDaoHelper.getStoragePolicyTransitionTypeEntity(request.getStoragePolicyTransition().getTransitionType());

        // Retrieve and ensure that specified storage policy status exists.
        StoragePolicyStatusEntity storagePolicyStatusEntity = storagePolicyStatusDaoHelper.getStoragePolicyStatusEntity(request.getStatus());

        // Create and persist a new storage policy entity from the request information.
        storagePolicyEntity = createStoragePolicyEntity(namespaceEntity, storagePolicyKey.getStoragePolicyName(), storageEntity, storagePolicyRuleTypeEntity,
            request.getStoragePolicyRule().getRuleValue(), businessObjectDefinitionEntity, request.getStoragePolicyFilter().getBusinessObjectFormatUsage(),
            fileTypeEntity, storagePolicyTransitionTypeEntity, storagePolicyStatusEntity, StoragePolicyEntity.STORAGE_POLICY_INITIAL_VERSION, true);

        // Create and return the storage policy object from the persisted entity.
        return createStoragePolicyFromEntity(storagePolicyEntity);
    }

    @NamespacePermissions({@NamespacePermission(fields = "#storagePolicyKey?.namespace", permissions = NamespacePermissionEnum.WRITE), @NamespacePermission(
        fields = "#request?.storagePolicyFilter?.namespace", permissions = NamespacePermissionEnum.READ)})
    @Override
    public StoragePolicy updateStoragePolicy(StoragePolicyKey storagePolicyKey, StoragePolicyUpdateRequest request)
    {
        // Validate and trim the key.
        storagePolicyHelper.validateStoragePolicyKey(storagePolicyKey);

        // Retrieve and ensure that a storage policy exists with the specified key.
        StoragePolicyEntity storagePolicyEntity = storagePolicyDaoHelper.getStoragePolicyEntityByKey(storagePolicyKey);

        // Validate and trim the request parameters.
        validateStoragePolicyUpdateRequest(request);

        // Retrieve and ensure that storage policy type exists.
        StoragePolicyRuleTypeEntity storagePolicyRuleTypeEntity =
            storagePolicyRuleTypeDaoHelper.getStoragePolicyRuleTypeEntity(request.getStoragePolicyRule().getRuleType());

        // Get the storage policy filter.
        StoragePolicyFilter storagePolicyFilter = request.getStoragePolicyFilter();

        // If specified, retrieve and ensure that the business object definition exists.
        BusinessObjectDefinitionEntity businessObjectDefinitionEntity = null;
        if (StringUtils.isNotBlank(storagePolicyFilter.getBusinessObjectDefinitionName()))
        {
            businessObjectDefinitionEntity = businessObjectDefinitionDaoHelper.getBusinessObjectDefinitionEntity(
                new BusinessObjectDefinitionKey(storagePolicyFilter.getNamespace(), storagePolicyFilter.getBusinessObjectDefinitionName()));
        }

        // If specified, retrieve and ensure that file type exists.
        FileTypeEntity fileTypeEntity = null;
        if (StringUtils.isNotBlank(storagePolicyFilter.getBusinessObjectFormatFileType()))
        {
            fileTypeEntity = fileTypeDaoHelper.getFileTypeEntity(storagePolicyFilter.getBusinessObjectFormatFileType());
        }

        // Retrieve and ensure that storage policy filter storage exists.
        StorageEntity storageEntity = storageDaoHelper.getStorageEntity(storagePolicyFilter.getStorageName());

        // Validate the source storage.
        storagePolicyDaoHelper.validateStoragePolicyFilterStorage(storageEntity);

        // Retrieve and ensure that storage policy transition type exists.
        StoragePolicyTransitionTypeEntity storagePolicyTransitionTypeEntity =
            storagePolicyTransitionTypeDaoHelper.getStoragePolicyTransitionTypeEntity(request.getStoragePolicyTransition().getTransitionType());

        // Retrieve and ensure that specified storage policy status exists.
        StoragePolicyStatusEntity storagePolicyStatusEntity = storagePolicyStatusDaoHelper.getStoragePolicyStatusEntity(request.getStatus());

        // Create and persist a new storage policy entity from the request information.
        // Please note that simply adding 1 to the latest version without "DB locking" is sufficient here,
        // even for multi-threading, since we are relying on the DB having version as part of the alternate key.
        StoragePolicyEntity newVersionStoragePolicyEntity =
            createStoragePolicyEntity(storagePolicyEntity.getNamespace(), storagePolicyEntity.getName(), storageEntity, storagePolicyRuleTypeEntity,
                request.getStoragePolicyRule().getRuleValue(), businessObjectDefinitionEntity, request.getStoragePolicyFilter().getBusinessObjectFormatUsage(),
                fileTypeEntity, storagePolicyTransitionTypeEntity, storagePolicyStatusEntity, storagePolicyEntity.getVersion() + 1, true);

        // Update the existing latest version storage policy entity, so it would not be flagged as the latest version anymore.
        storagePolicyEntity.setLatestVersion(false);
        storagePolicyDao.saveAndRefresh(storagePolicyEntity);

        // Create and return the storage policy object from the new version entity.
        return createStoragePolicyFromEntity(newVersionStoragePolicyEntity);
    }

    @NamespacePermission(fields = "#storagePolicyKey?.namespace", permissions = NamespacePermissionEnum.READ)
    @Override
    public StoragePolicy getStoragePolicy(StoragePolicyKey storagePolicyKey)
    {
        // Validate and trim the key.
        storagePolicyHelper.validateStoragePolicyKey(storagePolicyKey);

        // Retrieve and ensure that a storage policy exists with the specified key.
        StoragePolicyEntity storagePolicyEntity = storagePolicyDaoHelper.getStoragePolicyEntityByKey(storagePolicyKey);

        // Create and return the storage policy object from the persisted entity.
        return createStoragePolicyFromEntity(storagePolicyEntity);
    }

    /**
     * Validates the storage policy create request. This method also trims the request parameters.
     *
     * @param request the storage policy create request
     */
    private void validateStoragePolicyCreateRequest(StoragePolicyCreateRequest request)
    {
        Assert.notNull(request, "A storage policy create request must be specified.");

        storagePolicyHelper.validateStoragePolicyKey(request.getStoragePolicyKey());
        validateStoragePolicyRule(request.getStoragePolicyRule());
        validateStoragePolicyFilter(request.getStoragePolicyFilter());
        validateStoragePolicyTransition(request.getStoragePolicyTransition());

        // Validate storage policy status.
        Assert.hasText(request.getStatus(), "A storage policy status must be specified.");
        request.setStatus(request.getStatus().trim());
    }

    /**
     * Validates the storage policy update request. This method also trims the request parameters.
     *
     * @param request the storage policy update request
     */
    private void validateStoragePolicyUpdateRequest(StoragePolicyUpdateRequest request)
    {
        Assert.notNull(request, "A storage policy update request must be specified.");

        validateStoragePolicyRule(request.getStoragePolicyRule());
        validateStoragePolicyFilter(request.getStoragePolicyFilter());
        validateStoragePolicyTransition(request.getStoragePolicyTransition());

        // Validate storage policy status.
        Assert.hasText(request.getStatus(), "A storage policy status must be specified.");
        request.setStatus(request.getStatus().trim());
    }

    /**
     * Validates the storage policy rule. This method also trims the key parameters.
     *
     * @param storagePolicyRule the storage policy rule
     *
     * @throws IllegalArgumentException if any validation errors were found
     */
    private void validateStoragePolicyRule(StoragePolicyRule storagePolicyRule) throws IllegalArgumentException
    {
        Assert.notNull(storagePolicyRule, "A storage policy rule must be specified.");

        Assert.hasText(storagePolicyRule.getRuleType(), "A storage policy rule type must be specified.");
        storagePolicyRule.setRuleType(storagePolicyRule.getRuleType().trim());

        Assert.notNull(storagePolicyRule.getRuleValue(), "A storage policy rule value must be specified.");

        // Ensure that storage policy rule value is not negative.
        Assert.isTrue(storagePolicyRule.getRuleValue() >= 0, "Storage policy rule value must be a positive integer or zero.");
    }

    /**
     * Validates the storage policy filter. This method also trims the filter parameters.
     *
     * @param storagePolicyFilter the storage policy filter
     *
     * @throws IllegalArgumentException if any validation errors were found
     */
    private void validateStoragePolicyFilter(StoragePolicyFilter storagePolicyFilter) throws IllegalArgumentException
    {
        Assert.notNull(storagePolicyFilter, "A storage policy filter must be specified.");

        if (storagePolicyFilter.getNamespace() != null)
        {
            storagePolicyFilter.setNamespace(storagePolicyFilter.getNamespace().trim());
        }

        if (storagePolicyFilter.getBusinessObjectDefinitionName() != null)
        {
            storagePolicyFilter.setBusinessObjectDefinitionName(storagePolicyFilter.getBusinessObjectDefinitionName().trim());
        }

        // Validate that business object definition namespace and name are specified together.
        Assert.isTrue(
            (StringUtils.isNotBlank(storagePolicyFilter.getNamespace()) && StringUtils.isNotBlank(storagePolicyFilter.getBusinessObjectDefinitionName())) ||
                (StringUtils.isBlank(storagePolicyFilter.getNamespace()) && StringUtils.isBlank(storagePolicyFilter.getBusinessObjectDefinitionName())),
            "Business object definition name and namespace must be specified together.");

        if (storagePolicyFilter.getBusinessObjectFormatUsage() != null)
        {
            storagePolicyFilter.setBusinessObjectFormatUsage(storagePolicyFilter.getBusinessObjectFormatUsage().trim());
        }

        if (storagePolicyFilter.getBusinessObjectFormatFileType() != null)
        {
            storagePolicyFilter.setBusinessObjectFormatFileType(storagePolicyFilter.getBusinessObjectFormatFileType().trim());
        }

        // Validate that business object format usage and file type are specified together.
        Assert.isTrue((StringUtils.isNotBlank(storagePolicyFilter.getBusinessObjectFormatUsage()) &&
            StringUtils.isNotBlank(storagePolicyFilter.getBusinessObjectFormatFileType())) ||
            (StringUtils.isBlank(storagePolicyFilter.getBusinessObjectFormatUsage()) &&
                StringUtils.isBlank(storagePolicyFilter.getBusinessObjectFormatFileType())),
            "Business object format usage and file type must be specified together.");

        Assert.hasText(storagePolicyFilter.getStorageName(), "A storage name must be specified.");
        storagePolicyFilter.setStorageName(storagePolicyFilter.getStorageName().trim());
    }

    /**
     * Validates the storage policy transition. This method also trims the filter parameters.
     *
     * @param storagePolicyTransition the storage policy transition
     *
     * @throws IllegalArgumentException if any validation errors were found
     */
    private void validateStoragePolicyTransition(StoragePolicyTransition storagePolicyTransition) throws IllegalArgumentException
    {
        Assert.notNull(storagePolicyTransition, "A storage policy transition must be specified.");
        storagePolicyTransition
            .setTransitionType(alternateKeyHelper.validateStringParameter("storage policy transition type", storagePolicyTransition.getTransitionType()));
    }

    /**
     * Creates and persists a new storage policy entity.
     *
     * @param namespaceEntity the namespace entity
     * @param storagePolicyName the storage policy name
     * @param storageEntity the storage entity
     * @param storagePolicyRuleTypeEntity the storage policy rule type entity
     * @param storagePolicyRuleValue the storage policy rule value
     * @param businessObjectDefinitionEntity the business object definition entity
     * @param businessObjectFormatUsage the business object format usage
     * @param fileTypeEntity the file type entity
     * @param storagePolicyTransitionTypeEntity the transition type of the storage policy
     * @param storagePolicyStatusEntity the storage policy status entity
     * @param storagePolicyVersion the storage policy version
     * @param storagePolicyLatestVersion specifies if this storage policy is flagged as latest version or not
     *
     * @return the newly created storage policy entity
     */
    private StoragePolicyEntity createStoragePolicyEntity(NamespaceEntity namespaceEntity, String storagePolicyName, StorageEntity storageEntity,
        StoragePolicyRuleTypeEntity storagePolicyRuleTypeEntity, Integer storagePolicyRuleValue, BusinessObjectDefinitionEntity businessObjectDefinitionEntity,
        String businessObjectFormatUsage, FileTypeEntity fileTypeEntity, StoragePolicyTransitionTypeEntity storagePolicyTransitionTypeEntity,
        StoragePolicyStatusEntity storagePolicyStatusEntity, Integer storagePolicyVersion, Boolean storagePolicyLatestVersion)
    {
        StoragePolicyEntity storagePolicyEntity = new StoragePolicyEntity();

        storagePolicyEntity.setNamespace(namespaceEntity);
        storagePolicyEntity.setName(storagePolicyName);
        storagePolicyEntity.setStorage(storageEntity);
        storagePolicyEntity.setStoragePolicyRuleType(storagePolicyRuleTypeEntity);
        storagePolicyEntity.setStoragePolicyRuleValue(storagePolicyRuleValue);
        storagePolicyEntity.setBusinessObjectDefinition(businessObjectDefinitionEntity);
        if (StringUtils.isNotBlank(businessObjectFormatUsage))
        {
            storagePolicyEntity.setUsage(businessObjectFormatUsage);
        }
        storagePolicyEntity.setFileType(fileTypeEntity);
        storagePolicyEntity.setStoragePolicyTransitionType(storagePolicyTransitionTypeEntity);
        storagePolicyEntity.setStatus(storagePolicyStatusEntity);
        storagePolicyEntity.setVersion(storagePolicyVersion);
        storagePolicyEntity.setLatestVersion(storagePolicyLatestVersion);

        return storagePolicyDao.saveAndRefresh(storagePolicyEntity);
    }

    /**
     * Creates the storage policy registration from the persisted entity.
     *
     * @param storagePolicyEntity the storage policy registration entity
     *
     * @return the storage policy registration
     */
    private StoragePolicy createStoragePolicyFromEntity(StoragePolicyEntity storagePolicyEntity)
    {
        StoragePolicy storagePolicy = new StoragePolicy();

        storagePolicy.setId(storagePolicyEntity.getId());

        StoragePolicyKey storagePolicyKey = new StoragePolicyKey();
        storagePolicy.setStoragePolicyKey(storagePolicyKey);
        storagePolicyKey.setNamespace(storagePolicyEntity.getNamespace().getCode());
        storagePolicyKey.setStoragePolicyName(storagePolicyEntity.getName());

        StoragePolicyRule storagePolicyRule = new StoragePolicyRule();
        storagePolicy.setStoragePolicyRule(storagePolicyRule);
        storagePolicyRule.setRuleType(storagePolicyEntity.getStoragePolicyRuleType().getCode());
        storagePolicyRule.setRuleValue(storagePolicyEntity.getStoragePolicyRuleValue());

        StoragePolicyFilter storagePolicyFilter = new StoragePolicyFilter();
        storagePolicy.setStoragePolicyFilter(storagePolicyFilter);
        storagePolicyFilter.setNamespace(
            storagePolicyEntity.getBusinessObjectDefinition() != null ? storagePolicyEntity.getBusinessObjectDefinition().getNamespace().getCode() : null);
        storagePolicyFilter.setBusinessObjectDefinitionName(
            storagePolicyEntity.getBusinessObjectDefinition() != null ? storagePolicyEntity.getBusinessObjectDefinition().getName() : null);
        storagePolicyFilter.setBusinessObjectFormatUsage(storagePolicyEntity.getUsage());
        storagePolicyFilter.setBusinessObjectFormatFileType(storagePolicyEntity.getFileType() != null ? storagePolicyEntity.getFileType().getCode() : null);
        storagePolicyFilter.setStorageName(storagePolicyEntity.getStorage().getName());

        StoragePolicyTransition storagePolicyTransition = new StoragePolicyTransition();
        storagePolicy.setStoragePolicyTransition(storagePolicyTransition);
        storagePolicyTransition.setTransitionType(storagePolicyEntity.getStoragePolicyTransitionType().getCode());

        storagePolicy.setStatus(storagePolicyEntity.getStatus().getCode());

        return storagePolicy;
    }
}
