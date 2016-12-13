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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import org.finra.herd.core.helper.ConfigurationHelper;
import org.finra.herd.dao.BusinessObjectDefinitionDao;
import org.finra.herd.dao.config.DaoSpringModuleConfig;
import org.finra.herd.model.AlreadyExistsException;
import org.finra.herd.model.annotation.NamespacePermission;
import org.finra.herd.model.api.xml.Attribute;
import org.finra.herd.model.api.xml.BusinessObjectDefinition;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionCreateRequest;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionDescriptiveInformationUpdateRequest;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionKey;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionKeys;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionSearchFilter;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionSearchKey;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionSearchRequest;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionSearchResponse;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionUpdateRequest;
import org.finra.herd.model.api.xml.BusinessObjectFormatKey;
import org.finra.herd.model.api.xml.DescriptiveBusinessObjectFormat;
import org.finra.herd.model.api.xml.DescriptiveBusinessObjectFormatUpdateRequest;
import org.finra.herd.model.api.xml.NamespacePermissionEnum;
import org.finra.herd.model.api.xml.SampleDataFile;
import org.finra.herd.model.dto.BusinessObjectDefinitionSampleFileUpdateDto;
import org.finra.herd.model.dto.ConfigurationValue;
import org.finra.herd.model.jpa.BusinessObjectDefinitionAttributeEntity;
import org.finra.herd.model.jpa.BusinessObjectDefinitionEntity;
import org.finra.herd.model.jpa.BusinessObjectDefinitionSampleDataFileEntity;
import org.finra.herd.model.jpa.BusinessObjectFormatEntity;
import org.finra.herd.model.jpa.DataProviderEntity;
import org.finra.herd.model.jpa.NamespaceEntity;
import org.finra.herd.model.jpa.StorageEntity;
import org.finra.herd.model.jpa.TagEntity;
import org.finra.herd.service.BusinessObjectDefinitionService;
import org.finra.herd.service.SearchableService;
import org.finra.herd.service.helper.AlternateKeyHelper;
import org.finra.herd.service.helper.AttributeHelper;
import org.finra.herd.service.helper.BusinessObjectDefinitionDaoHelper;
import org.finra.herd.service.helper.BusinessObjectDefinitionHelper;
import org.finra.herd.service.helper.BusinessObjectFormatDaoHelper;
import org.finra.herd.service.helper.DataProviderDaoHelper;
import org.finra.herd.service.helper.NamespaceDaoHelper;
import org.finra.herd.service.helper.StorageDaoHelper;
import org.finra.herd.service.helper.TagDaoHelper;
import org.finra.herd.service.helper.TagHelper;

/**
 * The business object definition service implementation.
 */
@Service
@Transactional(value = DaoSpringModuleConfig.HERD_TRANSACTION_MANAGER_BEAN_NAME)
public class BusinessObjectDefinitionServiceImpl implements BusinessObjectDefinitionService, SearchableService
{
    @Autowired
    private AlternateKeyHelper alternateKeyHelper;

    @Autowired
    private AttributeHelper attributeHelper;

    @Autowired
    private BusinessObjectDefinitionDao businessObjectDefinitionDao;

    @Autowired
    private BusinessObjectDefinitionDaoHelper businessObjectDefinitionDaoHelper;

    @Autowired
    private BusinessObjectDefinitionHelper businessObjectDefinitionHelper;

    @Autowired
    private DataProviderDaoHelper dataProviderDaoHelper;

    @Autowired
    private BusinessObjectFormatDaoHelper businessObjectFormatDaoHelper;

    @Autowired
    private NamespaceDaoHelper namespaceDaoHelper;

    @Autowired
    private ConfigurationHelper configurationHelper;

    @Autowired
    private TagHelper tagHelper;

    @Autowired
    private TagDaoHelper tagDaoHelper;
    
    @Autowired
    private StorageDaoHelper storageDaoHelper;

    // Constant to hold the data provider name option for the business object definition search
    private static final String DATA_PROVIDER_NAME_FIELD = "dataprovidername";

    // Constant to hold the short description option for the business object definition search
    private static final String SHORT_DESCRIPTION_FIELD = "shortdescription";

    // Constant to hold the display name option for the business object definition search
    private static final String DISPLAY_NAME_FIELD = "displayname";

    /**
     * Creates a new business object definition.
     *
     * @param request the business object definition create request.
     *
     * @return the created business object definition.
     */
    @NamespacePermission(fields = "#request.namespace", permissions = NamespacePermissionEnum.WRITE)
    @Override
    public BusinessObjectDefinition createBusinessObjectDefinition(BusinessObjectDefinitionCreateRequest request)
    {
        // Perform the validation.
        validateBusinessObjectDefinitionCreateRequest(request);

        // Get the namespace and ensure it exists.
        NamespaceEntity namespaceEntity = namespaceDaoHelper.getNamespaceEntity(request.getNamespace());

        // Get the data provider and ensure it exists.
        DataProviderEntity dataProviderEntity = dataProviderDaoHelper.getDataProviderEntity(request.getDataProviderName());

        // Get business object definition key.
        BusinessObjectDefinitionKey businessObjectDefinitionKey =
            new BusinessObjectDefinitionKey(request.getNamespace(), request.getBusinessObjectDefinitionName());

        // Ensure a business object definition with the specified key doesn't already exist.
        BusinessObjectDefinitionEntity businessObjectDefinitionEntity =
            businessObjectDefinitionDao.getBusinessObjectDefinitionByKey(businessObjectDefinitionKey);
        if (businessObjectDefinitionEntity != null)
        {
            throw new AlreadyExistsException(String
                .format("Unable to create business object definition with name \"%s\" because it already exists for namespace \"%s\".",
                    businessObjectDefinitionKey.getBusinessObjectDefinitionName(), businessObjectDefinitionKey.getNamespace()));
        }

        // Create a business object definition entity from the request information.
        businessObjectDefinitionEntity = createBusinessObjectDefinitionEntity(request, namespaceEntity, dataProviderEntity);

        // Create and return the business object definition object from the persisted entity.
        return createBusinessObjectDefinitionFromEntity(businessObjectDefinitionEntity);
    }

    /**
     * Updates a business object definition.
     *
     * @param businessObjectDefinitionKey the business object definition key
     * @param request the business object definition update request
     *
     * @return the updated business object definition
     */
    @NamespacePermission(fields = "#businessObjectDefinitionKey.namespace", permissions = NamespacePermissionEnum.WRITE)
    @Override
    public BusinessObjectDefinition updateBusinessObjectDefinition(BusinessObjectDefinitionKey businessObjectDefinitionKey,
        BusinessObjectDefinitionUpdateRequest request)
    {
        // Perform validation and trim.
        businessObjectDefinitionHelper.validateBusinessObjectDefinitionKey(businessObjectDefinitionKey);
        validateBusinessObjectDefinitionUpdateRequest(request);

        // Retrieve and ensure that a business object definition already exists with the specified key.
        BusinessObjectDefinitionEntity businessObjectDefinitionEntity =
            businessObjectDefinitionDaoHelper.getBusinessObjectDefinitionEntity(businessObjectDefinitionKey);

        // Update and persist the entity.
        updateBusinessObjectDefinitionEntity(businessObjectDefinitionEntity, request);

        // Create and return the business object definition object from the persisted entity.
        return createBusinessObjectDefinitionFromEntity(businessObjectDefinitionEntity);
    }

    /**
     * Updates a business object definition.
     *
     * @param businessObjectDefinitionKey the business object definition key
     * @param request the business object definition update request
     *
     * @return the updated business object definition
     */
    @Override
    public BusinessObjectDefinition updateBusinessObjectDefinitionDescriptiveInformation(BusinessObjectDefinitionKey businessObjectDefinitionKey,
        BusinessObjectDefinitionDescriptiveInformationUpdateRequest request)
    {
        // Perform validation and trim.
        businessObjectDefinitionHelper.validateBusinessObjectDefinitionKey(businessObjectDefinitionKey);
        validateBusinessObjectDefinitionDescriptiveInformationUpdateRequest(request);

        // Retrieve and ensure that a business object definition already exists with the specified key.
        BusinessObjectDefinitionEntity businessObjectDefinitionEntity =
            businessObjectDefinitionDaoHelper.getBusinessObjectDefinitionEntity(businessObjectDefinitionKey);

        BusinessObjectFormatEntity businessObjectFormatEntity = null;
        DescriptiveBusinessObjectFormatUpdateRequest descriptiveFormat = request.getDescriptiveBusinessObjectFormat();
        if (descriptiveFormat != null)
        {
            BusinessObjectFormatKey businessObjectFormatKey = new BusinessObjectFormatKey();
            businessObjectFormatKey.setBusinessObjectDefinitionName(businessObjectDefinitionEntity.getName());
            businessObjectFormatKey.setNamespace(businessObjectDefinitionEntity.getNamespace().getCode());
            businessObjectFormatKey.setBusinessObjectFormatFileType(descriptiveFormat.getBusinessObjectFormatFileType());
            businessObjectFormatKey.setBusinessObjectFormatUsage(descriptiveFormat.getBusinessObjectFormatUsage());
            businessObjectFormatEntity = businessObjectFormatDaoHelper.getBusinessObjectFormatEntity(businessObjectFormatKey);
        }
        businessObjectDefinitionEntity.setDescriptiveBusinessObjectFormat(businessObjectFormatEntity);

        // Update and persist the entity.
        updateBusinessObjectDefinitionEntityDescriptiveInformation(businessObjectDefinitionEntity, request);

        // Create and return the business object definition object from the persisted entity.
        return createBusinessObjectDefinitionFromEntity(businessObjectDefinitionEntity);
    }

    /**
     * Gets a business object definition for the specified key. This method starts a new transaction.
     *
     * @param businessObjectDefinitionKey the business object definition key
     *
     * @return the business object definition.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BusinessObjectDefinition getBusinessObjectDefinition(BusinessObjectDefinitionKey businessObjectDefinitionKey)
    {
        return getBusinessObjectDefinitionImpl(businessObjectDefinitionKey);
    }

    /**
     * Gets a business object definition for the specified key.
     *
     * @param businessObjectDefinitionKey the business object definition key
     *
     * @return the business object definition.
     */
    protected BusinessObjectDefinition getBusinessObjectDefinitionImpl(BusinessObjectDefinitionKey businessObjectDefinitionKey)
    {
        // Perform validation and trim.
        businessObjectDefinitionHelper.validateBusinessObjectDefinitionKey(businessObjectDefinitionKey);

        // Retrieve and ensure that a business object definition already exists with the specified key.
        BusinessObjectDefinitionEntity businessObjectDefinitionEntity =
            businessObjectDefinitionDaoHelper.getBusinessObjectDefinitionEntity(businessObjectDefinitionKey);

        // Create and return the business object definition object from the persisted entity.
        return createBusinessObjectDefinitionFromEntity(businessObjectDefinitionEntity);
    }

    /**
     * Deletes a business object definition for the specified name.
     *
     * @param businessObjectDefinitionKey the business object definition key
     *
     * @return the business object definition that was deleted.
     */
    @NamespacePermission(fields = "#businessObjectDefinitionKey.namespace", permissions = NamespacePermissionEnum.WRITE)
    @Override
    public BusinessObjectDefinition deleteBusinessObjectDefinition(BusinessObjectDefinitionKey businessObjectDefinitionKey)
    {
        // Perform validation and trim.
        businessObjectDefinitionHelper.validateBusinessObjectDefinitionKey(businessObjectDefinitionKey);

        // Retrieve and ensure that a business object definition already exists with the specified key.
        BusinessObjectDefinitionEntity businessObjectDefinitionEntity =
            businessObjectDefinitionDaoHelper.getBusinessObjectDefinitionEntity(businessObjectDefinitionKey);

        // Delete the business object definition.
        businessObjectDefinitionDao.delete(businessObjectDefinitionEntity);

        // Create and return the business object definition object from the deleted entity.
        return createBusinessObjectDefinitionFromEntity(businessObjectDefinitionEntity);
    }

    /**
     * Gets the list of all business object definitions defined in the system.
     *
     * @return the business object definition keys.
     */
    @Override
    public BusinessObjectDefinitionKeys getBusinessObjectDefinitions()
    {
        BusinessObjectDefinitionKeys businessObjectDefinitionKeys = new BusinessObjectDefinitionKeys();
        businessObjectDefinitionKeys.getBusinessObjectDefinitionKeys().addAll(businessObjectDefinitionDao.getBusinessObjectDefinitionKeys());
        return businessObjectDefinitionKeys;
    }

    /**
     * Gets a list of all business object definitions defined in the system for a specified namespace.
     *
     * @param namespaceCode the namespace code
     *
     * @return the business object definition keys
     */
    @Override
    public BusinessObjectDefinitionKeys getBusinessObjectDefinitions(String namespaceCode)
    {
        // Validate and trim the namespace code.
        Assert.hasText(namespaceCode, "A namespace must be specified.");

        // Retrieve and return the list of business object definitions
        BusinessObjectDefinitionKeys businessObjectDefinitionKeys = new BusinessObjectDefinitionKeys();
        businessObjectDefinitionKeys.getBusinessObjectDefinitionKeys()
            .addAll(businessObjectDefinitionDao.getBusinessObjectDefinitionKeys(namespaceCode.trim()));
        return businessObjectDefinitionKeys;
    }

    /**
     * Gets a list of all business object definitions defined in the system.
     *
     * @return the business object definitions.
     */
    @Override
    public BusinessObjectDefinitionSearchResponse searchBusinessObjectDefinitions(BusinessObjectDefinitionSearchRequest request, Set<String> fields)
    {
        // Validate the business object definition search fields.
        validateSearchResponseFields(fields);

        BusinessObjectDefinitionSearchKey businessObjectDefinitionSearchKey = null;
        List<TagEntity> tagEntities = new ArrayList<>();

        if (!CollectionUtils.isEmpty(request.getBusinessObjectDefinitionSearchFilters()))
        {
            // Validate the search request.
            validateBusinessObjectDefinitionSearchRequest(request);

            businessObjectDefinitionSearchKey = request.getBusinessObjectDefinitionSearchFilters().get(0).getBusinessObjectDefinitionSearchKeys().get(0);

            TagEntity tagEntity = tagDaoHelper.getTagEntity(businessObjectDefinitionSearchKey.getTagKey());

            // If includeTagHierarchy is true, get list of children tag entities down the hierarchy of the specified tag.
            tagEntities.add(tagEntity);
            if (BooleanUtils.isTrue(businessObjectDefinitionSearchKey.isIncludeTagHierarchy()))
            {
                tagEntities.addAll(tagDaoHelper.getTagChildrenEntities(tagEntity));
            }
        }

        // Construct business object search response.
        BusinessObjectDefinitionSearchResponse searchResponse = new BusinessObjectDefinitionSearchResponse();
        List<BusinessObjectDefinition> businessObjectDefinitions = new ArrayList<>();
        searchResponse.setBusinessObjectDefinitions(businessObjectDefinitions);

        // Retrieve all unique business object definition entities and construct a list of business object definitions based on the requested fields.
        for (BusinessObjectDefinitionEntity businessObjectDefinition : ImmutableSet
            .copyOf(businessObjectDefinitionDao.getBusinessObjectDefinitions(tagEntities)))
        {
            businessObjectDefinitions.add(createBusinessObjectDefinitionFromEntity(businessObjectDefinition, fields));
        }

        return searchResponse;
    }

    /**
     * Validates the business object definition create request. This method also trims request parameters.
     *
     * @param request the request
     *
     */
    private void validateBusinessObjectDefinitionCreateRequest(BusinessObjectDefinitionCreateRequest request)
    {
        request.setNamespace(alternateKeyHelper.validateStringParameter("namespace", request.getNamespace()));
        request.setBusinessObjectDefinitionName(
            alternateKeyHelper.validateStringParameter("business object definition name", request.getBusinessObjectDefinitionName()));
        request.setDataProviderName(alternateKeyHelper.validateStringParameter("data provider name", request.getDataProviderName()));

        if (request.getDisplayName() != null)
        {
            request.setDisplayName(request.getDisplayName().trim());
        }

        // Validate attributes.
        attributeHelper.validateAttributes(request.getAttributes());
    }

    /**
     * Validates the business object definition update request. This method also trims request parameters.
     *
     * @param request the request.
     *
     * @throws IllegalArgumentException if any validation errors were found.
     */
    private void validateBusinessObjectDefinitionUpdateRequest(BusinessObjectDefinitionUpdateRequest request)
    {
        if (request.getDisplayName() != null)
        {
            request.setDisplayName(request.getDisplayName().trim());
        }

        // Validate attributes.
        attributeHelper.validateAttributes(request.getAttributes());
    }

    /**
     * Validates the business object definition update request. This method also trims request parameters.
     *
     * @param request the request.
     *
     * @throws IllegalArgumentException if any validation errors were found.
     */
    private void validateBusinessObjectDefinitionDescriptiveInformationUpdateRequest(BusinessObjectDefinitionDescriptiveInformationUpdateRequest request)
    {
        if (request.getDisplayName() != null)
        {
            request.setDisplayName(request.getDisplayName().trim());
        }

        if (request.getDescriptiveBusinessObjectFormat() != null)
        {
            DescriptiveBusinessObjectFormatUpdateRequest descriptiveFormat = request.getDescriptiveBusinessObjectFormat();

            descriptiveFormat.setBusinessObjectFormatUsage(
                alternateKeyHelper.validateStringParameter("business object format usage", descriptiveFormat.getBusinessObjectFormatUsage()));
            descriptiveFormat.setBusinessObjectFormatFileType(
                alternateKeyHelper.validateStringParameter("business object format file type", descriptiveFormat.getBusinessObjectFormatFileType()));
        }
    }

    /**
     * Creates and persists a new business object definition entity from the request information.
     *
     * @param request the request.
     * @param namespaceEntity the namespace.
     * @param dataProviderEntity the data provider.
     *
     * @return the newly created business object definition entity.
     */
    private BusinessObjectDefinitionEntity createBusinessObjectDefinitionEntity(BusinessObjectDefinitionCreateRequest request, NamespaceEntity namespaceEntity,
        DataProviderEntity dataProviderEntity)
    {
        // Create a new entity.
        BusinessObjectDefinitionEntity businessObjectDefinitionEntity = new BusinessObjectDefinitionEntity();
        businessObjectDefinitionEntity.setNamespace(namespaceEntity);
        businessObjectDefinitionEntity.setName(request.getBusinessObjectDefinitionName());
        businessObjectDefinitionEntity.setDescription(request.getDescription());
        businessObjectDefinitionEntity.setDataProvider(dataProviderEntity);
        businessObjectDefinitionEntity.setDisplayName(request.getDisplayName());

        // Create the attributes if they are specified.
        if (!CollectionUtils.isEmpty(request.getAttributes()))
        {
            List<BusinessObjectDefinitionAttributeEntity> attributeEntities = new ArrayList<>();
            businessObjectDefinitionEntity.setAttributes(attributeEntities);
            for (Attribute attribute : request.getAttributes())
            {
                BusinessObjectDefinitionAttributeEntity attributeEntity = new BusinessObjectDefinitionAttributeEntity();
                attributeEntities.add(attributeEntity);
                attributeEntity.setBusinessObjectDefinition(businessObjectDefinitionEntity);
                attributeEntity.setName(attribute.getName());
                attributeEntity.setValue(attribute.getValue());
            }
        }

        // Persist and return the new entity.
        return businessObjectDefinitionDao.saveAndRefresh(businessObjectDefinitionEntity);
    }

    /**
     * Update and persist the business object definition per specified update request.
     *
     * @param businessObjectDefinitionEntity the business object definition entity
     * @param request the business object definition update request
     */
    private void updateBusinessObjectDefinitionEntity(BusinessObjectDefinitionEntity businessObjectDefinitionEntity,
        BusinessObjectDefinitionUpdateRequest request)
    {
        // Update the entity with the new description value.
        businessObjectDefinitionEntity.setDescription(request.getDescription());
        businessObjectDefinitionEntity.setDisplayName(request.getDisplayName());

        // Update the attributes.
        // Load all existing attribute entities in a map with a "lowercase" attribute name as the key for case insensitivity.
        Map<String, BusinessObjectDefinitionAttributeEntity> existingAttributeEntities = new HashMap<>();
        for (BusinessObjectDefinitionAttributeEntity attributeEntity : businessObjectDefinitionEntity.getAttributes())
        {
            String mapKey = attributeEntity.getName().toLowerCase();
            if (existingAttributeEntities.containsKey(mapKey))
            {
                throw new IllegalStateException(String.format(
                    "Found duplicate attribute with name \"%s\" for business object definition {namespace: \"%s\", businessObjectDefinitionName: \"%s\"}.",
                    mapKey, businessObjectDefinitionEntity.getNamespace().getCode(), businessObjectDefinitionEntity.getName()));
            }
            existingAttributeEntities.put(mapKey, attributeEntity);
        }

        // Process the list of attributes to determine that business object definition attribute entities should be created, updated, or deleted.
        List<BusinessObjectDefinitionAttributeEntity> createdAttributeEntities = new ArrayList<>();
        List<BusinessObjectDefinitionAttributeEntity> retainedAttributeEntities = new ArrayList<>();
        if (!CollectionUtils.isEmpty(request.getAttributes()))
        {
            for (Attribute attribute : request.getAttributes())
            {
                // Use a "lowercase" attribute name for case insensitivity.
                String lowercaseAttributeName = attribute.getName().toLowerCase();
                if (existingAttributeEntities.containsKey(lowercaseAttributeName))
                {
                    // Check if the attribute value needs to be updated.
                    BusinessObjectDefinitionAttributeEntity attributeEntity = existingAttributeEntities.get(lowercaseAttributeName);
                    if (!StringUtils.equals(attribute.getValue(), attributeEntity.getValue()))
                    {
                        // Update the business object attribute entity.
                        attributeEntity.setValue(attribute.getValue());
                    }

                    // Add this entity to the list of business object definition attribute entities to be retained.
                    retainedAttributeEntities.add(attributeEntity);
                }
                else
                {
                    // Create a new business object attribute entity.
                    BusinessObjectDefinitionAttributeEntity attributeEntity = new BusinessObjectDefinitionAttributeEntity();
                    businessObjectDefinitionEntity.getAttributes().add(attributeEntity);
                    attributeEntity.setBusinessObjectDefinition(businessObjectDefinitionEntity);
                    attributeEntity.setName(attribute.getName());
                    attributeEntity.setValue(attribute.getValue());

                    // Add this entity to the list of the newly created business object definition attribute entities.
                    retainedAttributeEntities.add(attributeEntity);
                }
            }
        }

        // Remove any of the currently existing attribute entities that did not get onto the retained entities list.
        businessObjectDefinitionEntity.getAttributes().retainAll(retainedAttributeEntities);

        // Add all of the newly created business object definition attribute entities.
        businessObjectDefinitionEntity.getAttributes().addAll(createdAttributeEntities);

        // Persist the entity.
        businessObjectDefinitionDao.saveAndRefresh(businessObjectDefinitionEntity);
    }

    /**
     * Update and persist the business object definition descriptive information per specified update request.
     *
     * @param businessObjectDefinitionEntity the business object definition entity
     * @param request the business object definition update request
     */
    private void updateBusinessObjectDefinitionEntityDescriptiveInformation(BusinessObjectDefinitionEntity businessObjectDefinitionEntity,
        BusinessObjectDefinitionDescriptiveInformationUpdateRequest request)
    {
        // Update the entity with the new description value.
        businessObjectDefinitionEntity.setDescription(request.getDescription());
        businessObjectDefinitionEntity.setDisplayName(request.getDisplayName());

        businessObjectDefinitionDao.saveAndRefresh(businessObjectDefinitionEntity);
    }

    /**
     * Creates a business object definition from the persisted entity.
     *
     * @param businessObjectDefinitionEntity the business object definition entity
     *
     * @return the business object definition
     */
    private BusinessObjectDefinition createBusinessObjectDefinitionFromEntity(BusinessObjectDefinitionEntity businessObjectDefinitionEntity)
    {
        // Create a business object definition.
        BusinessObjectDefinition businessObjectDefinition = new BusinessObjectDefinition();
        businessObjectDefinition.setId(businessObjectDefinitionEntity.getId());
        businessObjectDefinition.setNamespace(businessObjectDefinitionEntity.getNamespace().getCode());
        businessObjectDefinition.setBusinessObjectDefinitionName(businessObjectDefinitionEntity.getName());
        businessObjectDefinition.setDescription(businessObjectDefinitionEntity.getDescription());
        businessObjectDefinition.setDataProviderName(businessObjectDefinitionEntity.getDataProvider().getName());
        businessObjectDefinition.setDisplayName(businessObjectDefinitionEntity.getDisplayName());

        // Add attributes.
        List<Attribute> attributes = new ArrayList<>();
        businessObjectDefinition.setAttributes(attributes);
        for (BusinessObjectDefinitionAttributeEntity attributeEntity : businessObjectDefinitionEntity.getAttributes())
        {
            attributes.add(new Attribute(attributeEntity.getName(), attributeEntity.getValue()));
        }

        if (businessObjectDefinitionEntity.getDescriptiveBusinessObjectFormat() != null)
        {
            BusinessObjectFormatEntity descriptiveFormatEntity = businessObjectDefinitionEntity.getDescriptiveBusinessObjectFormat();
            DescriptiveBusinessObjectFormat descriptiveBusinessObjectFormat = new DescriptiveBusinessObjectFormat();
            businessObjectDefinition.setDescriptiveBusinessObjectFormat(descriptiveBusinessObjectFormat);
            descriptiveBusinessObjectFormat.setBusinessObjectFormatUsage(descriptiveFormatEntity.getUsage());
            descriptiveBusinessObjectFormat.setBusinessObjectFormatFileType(descriptiveFormatEntity.getFileType().getCode());
            descriptiveBusinessObjectFormat.setBusinessObjectFormatVersion(descriptiveFormatEntity.getBusinessObjectFormatVersion());
        }

        // Add sample data files.
        List<SampleDataFile> sampleDataFiles = new ArrayList<>();
        businessObjectDefinition.setSampleDataFiles(sampleDataFiles);
        for (BusinessObjectDefinitionSampleDataFileEntity sampleDataFileEntity : businessObjectDefinitionEntity.getSampleDataFiles())
        {
            sampleDataFiles.add(new SampleDataFile(sampleDataFileEntity.getDirectoryPath(), sampleDataFileEntity.getFileName()));
        }

        return businessObjectDefinition;
    }

    /**
     * Creates a light-weight business object definition from its persisted entity based on a set of requested fields.
     *
     * @param businessObjectDefinitionEntity the specified business object definition entity
     * @param fields the set of requested fields
     *
     * @return the light-weight business object definition
     */
    private BusinessObjectDefinition createBusinessObjectDefinitionFromEntity(BusinessObjectDefinitionEntity businessObjectDefinitionEntity, Set<String> fields)
    {
        BusinessObjectDefinition definition = new BusinessObjectDefinition();

        //populate namespace and business object definition name fields by default
        definition.setNamespace(businessObjectDefinitionEntity.getNamespace().getCode());
        definition.setBusinessObjectDefinitionName(businessObjectDefinitionEntity.getName());

        //decorate object with only the required fields
        if (fields.contains(DATA_PROVIDER_NAME_FIELD))
        {
            definition.setDataProviderName(businessObjectDefinitionEntity.getDataProvider().getName());
        }

        if (fields.contains(SHORT_DESCRIPTION_FIELD))
        {
            definition.setShortDescription(getShortDescription(businessObjectDefinitionEntity.getDescription()));
        }

        if (fields.contains(DISPLAY_NAME_FIELD))
        {
            definition.setDisplayName(businessObjectDefinitionEntity.getDisplayName());
        }

        return definition;
    }

    /**
     * Truncates the description field to a configurable value thereby producing a 'short description'
     *
     * @param description the specified description
     *
     * @return truncated (short) description
     */
    private String getShortDescription(String description)
    {
        // Get the configured value for short description's length
        Integer shortDescMaxLength = configurationHelper.getProperty(ConfigurationValue.BUSINESS_OBJECT_DEFINITION_SHORT_DESCRIPTION_LENGTH, Integer.class);

        // Truncate and return
        return StringUtils.left(description, shortDescMaxLength);
    }

    /**
     * Returns valid search response fields
     *
     * @return the set of valid search response fields
     */
    @Override
    public Set<String> getValidSearchResponseFields()
    {
        return ImmutableSet.of(DATA_PROVIDER_NAME_FIELD, SHORT_DESCRIPTION_FIELD, DISPLAY_NAME_FIELD);
    }

    /**
     * Validate the business object definition search request. This method also trims the request parameters.
     *
     * @param businessObjectDefinitionSearchRequest the business object definition search request
     */
    private void validateBusinessObjectDefinitionSearchRequest(BusinessObjectDefinitionSearchRequest businessObjectDefinitionSearchRequest)
    {
        if (CollectionUtils.size(businessObjectDefinitionSearchRequest.getBusinessObjectDefinitionSearchFilters()) == 1 &&
            businessObjectDefinitionSearchRequest.getBusinessObjectDefinitionSearchFilters().get(0) != null)
        {
                // Get the business object definition search filter.
                BusinessObjectDefinitionSearchFilter businessObjectDefinitionSearchFilter =
                    businessObjectDefinitionSearchRequest.getBusinessObjectDefinitionSearchFilters().get(0);

                Assert.isTrue(CollectionUtils.size(businessObjectDefinitionSearchFilter.getBusinessObjectDefinitionSearchKeys()) == 1 &&
                        businessObjectDefinitionSearchFilter.getBusinessObjectDefinitionSearchKeys().get(0) != null,
                    "Exactly one business object definition search key must be specified.");

                // Get the tag search key.
                BusinessObjectDefinitionSearchKey businessObjectDefinitionSearchKey =
                    businessObjectDefinitionSearchFilter.getBusinessObjectDefinitionSearchKeys().get(0);

                tagHelper.validateTagKey(businessObjectDefinitionSearchKey.getTagKey());
        }
        else
        {
            Assert.isTrue(CollectionUtils.size(businessObjectDefinitionSearchRequest.getBusinessObjectDefinitionSearchFilters()) ==
                        1 && businessObjectDefinitionSearchRequest.getBusinessObjectDefinitionSearchFilters().get(0) != null,
                    "Exactly one business object definition search filter must be specified.");
        }
    }
    
    /**
    * Update business object definition sample file
    * @param businessObjectDefinitionKey business object definition key
    * @param BusinessObjectDefinitionSampleFileUpdateDto update dto
    */
    @Override
    public void updateBusinessObjectDefinitionEntitySampleFile(BusinessObjectDefinitionKey businessObjectDefinitionKey,
        BusinessObjectDefinitionSampleFileUpdateDto businessObjectDefinitionSampleFileUpdateDto)
    {
        String path = businessObjectDefinitionSampleFileUpdateDto.getPath();
        String fileName = businessObjectDefinitionSampleFileUpdateDto.getFileName();
        long fileSize = businessObjectDefinitionSampleFileUpdateDto.getFileSize();
        
        // validate business object key
        businessObjectDefinitionHelper.validateBusinessObjectDefinitionKey(businessObjectDefinitionKey);
        // validate file name
        Assert.hasText(fileName, "A file name must be specified.");
        BusinessObjectDefinitionEntity businessObjectDefinitionEntity =
                businessObjectDefinitionDaoHelper.getBusinessObjectDefinitionEntity(businessObjectDefinitionKey);
        Collection<BusinessObjectDefinitionSampleDataFileEntity> sampleFiles = businessObjectDefinitionEntity.getSampleDataFiles();
        boolean found = false;
        for (BusinessObjectDefinitionSampleDataFileEntity sampleDataFieEntity : sampleFiles)
        {
            //assume the path is the same for this business object definition
            if (sampleDataFieEntity.getFileName().equals(fileName))
            {
                found = true;
                sampleDataFieEntity.setFileSizeBytes(fileSize);
                businessObjectDefinitionDao.saveAndRefresh(businessObjectDefinitionEntity);
                break;
            }
        }
        // create a new entity when not found
        if (!found)
        {
            StorageEntity storageEntity = storageDaoHelper.getStorageEntity(StorageEntity.SAMPLE_DATA_FILE_STORAGE);
            BusinessObjectDefinitionSampleDataFileEntity sampleDataFileEntity = new BusinessObjectDefinitionSampleDataFileEntity();
            sampleDataFileEntity.setStorage(storageEntity);
            sampleDataFileEntity.setBusinessObjectDefinition(businessObjectDefinitionEntity);
            sampleDataFileEntity.setDirectoryPath(path);
            sampleDataFileEntity.setFileName(fileName);
            sampleDataFileEntity.setFileSizeBytes(fileSize);
            businessObjectDefinitionEntity.getSampleDataFiles().add(sampleDataFileEntity);
            businessObjectDefinitionDao.saveAndRefresh(businessObjectDefinitionEntity);
        }
    }
}
