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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.finra.herd.dao.BusinessObjectFormatDao;
import org.finra.herd.dao.PartitionKeyGroupDao;
import org.finra.herd.dao.config.DaoSpringModuleConfig;
import org.finra.herd.model.AlreadyExistsException;
import org.finra.herd.model.api.xml.PartitionKeyGroup;
import org.finra.herd.model.api.xml.PartitionKeyGroupCreateRequest;
import org.finra.herd.model.api.xml.PartitionKeyGroupKey;
import org.finra.herd.model.api.xml.PartitionKeyGroupKeys;
import org.finra.herd.model.jpa.PartitionKeyGroupEntity;
import org.finra.herd.service.PartitionKeyGroupService;
import org.finra.herd.service.helper.PartitionKeyGroupDaoHelper;
import org.finra.herd.service.helper.PartitionKeyGroupHelper;

/**
 * The partition key group service implementation.
 */
@Service
@Transactional(value = DaoSpringModuleConfig.HERD_TRANSACTION_MANAGER_BEAN_NAME)
public class PartitionKeyGroupServiceImpl implements PartitionKeyGroupService
{
    @Autowired
    private BusinessObjectFormatDao businessObjectFormatDao;

    @Autowired
    private PartitionKeyGroupDao partitionKeyGroupDao;

    @Autowired
    private PartitionKeyGroupDaoHelper partitionKeyGroupDaoHelper;

    @Autowired
    private PartitionKeyGroupHelper partitionKeyGroupHelper;

    /**
     * Creates a new partition key group.
     *
     * @param request the information needed to create a partition key group
     *
     * @return the newly created partition key group information
     */
    @Override
    public PartitionKeyGroup createPartitionKeyGroup(PartitionKeyGroupCreateRequest request)
    {
        // Perform the validation.
        partitionKeyGroupHelper.validatePartitionKeyGroupKey(request.getPartitionKeyGroupKey());

        // Ensure a partition key group with the specified name doesn't already exist.
        PartitionKeyGroupEntity partitionKeyGroupEntity = partitionKeyGroupDao.getPartitionKeyGroupByKey(request.getPartitionKeyGroupKey());
        if (partitionKeyGroupEntity != null)
        {
            throw new AlreadyExistsException(String.format("Unable to create partition key group with name \"%s\" because it already exists.",
                request.getPartitionKeyGroupKey().getPartitionKeyGroupName()));
        }

        // Create a partition key group entity from the request information.
        partitionKeyGroupEntity = createPartitionKeyGroupEntity(request);

        // Persist the new entity.
        partitionKeyGroupEntity = partitionKeyGroupDao.saveAndRefresh(partitionKeyGroupEntity);

        // Create and return the partition key group object from the persisted entity.
        return createPartitionKeyGroupFromEntity(partitionKeyGroupEntity);
    }

    /**
     * Gets an existing partition key group by key.
     *
     * @param partitionKeyGroupKey the partition key group key
     *
     * @return the partition key group information
     */
    @Override
    public PartitionKeyGroup getPartitionKeyGroup(PartitionKeyGroupKey partitionKeyGroupKey)
    {
        // Perform validation and trim.
        partitionKeyGroupHelper.validatePartitionKeyGroupKey(partitionKeyGroupKey);

        // Retrieve and ensure that a partition key group exists with the specified name.
        PartitionKeyGroupEntity partitionKeyGroupEntity = partitionKeyGroupDaoHelper.getPartitionKeyGroupEntity(partitionKeyGroupKey);

        // Create and return the partition key group object from the persisted entity.
        return createPartitionKeyGroupFromEntity(partitionKeyGroupEntity);
    }

    /**
     * Deletes an existing partition key group by key.
     *
     * @param partitionKeyGroupKey the partition key group key
     *
     * @return the partition key group that got deleted
     */
    @Override
    public PartitionKeyGroup deletePartitionKeyGroup(PartitionKeyGroupKey partitionKeyGroupKey)
    {
        // Perform validation and trim.
        partitionKeyGroupHelper.validatePartitionKeyGroupKey(partitionKeyGroupKey);

        // Retrieve and ensure that a partition key group already exists with the specified name.
        PartitionKeyGroupEntity partitionKeyGroupEntity = partitionKeyGroupDaoHelper.getPartitionKeyGroupEntity(partitionKeyGroupKey);

        // Check if we are allowed to delete this business object format.
        if (businessObjectFormatDao.getBusinessObjectFormatCountByPartitionKeyGroup(partitionKeyGroupEntity) > 0L)
        {
            throw new IllegalArgumentException(String.format("Can not delete \"%s\" partition key group since it is being used by a business object format.",
                partitionKeyGroupKey.getPartitionKeyGroupName()));
        }

        // Delete the partition key group.
        partitionKeyGroupDao.delete(partitionKeyGroupEntity);

        // Create and return the partition key group object from the deleted entity.
        return createPartitionKeyGroupFromEntity(partitionKeyGroupEntity);
    }

    /**
     * Gets a list of keys for all existing partition key groups.
     *
     * @return the partition key group keys
     */
    @Override
    public PartitionKeyGroupKeys getPartitionKeyGroups()
    {
        // Create and populate a list of partition key group keys.
        PartitionKeyGroupKeys partitionKeyGroupKeys = new PartitionKeyGroupKeys();
        partitionKeyGroupKeys.getPartitionKeyGroupKeys().addAll(partitionKeyGroupDao.getPartitionKeyGroups());

        return partitionKeyGroupKeys;
    }

    /**
     * Creates a new partition key group entity from the request information.
     *
     * @param request the partition key group create request
     *
     * @return the newly created partition key group entity
     */
    private PartitionKeyGroupEntity createPartitionKeyGroupEntity(PartitionKeyGroupCreateRequest request)
    {
        // Create a new entity.
        PartitionKeyGroupEntity partitionKeyGroupEntity = new PartitionKeyGroupEntity();
        partitionKeyGroupEntity.setPartitionKeyGroupName(request.getPartitionKeyGroupKey().getPartitionKeyGroupName());

        return partitionKeyGroupEntity;
    }

    /**
     * Creates the partition key group from the persisted entity.
     *
     * @param partitionKeyGroupEntity the partition key group entity
     *
     * @return the partition key group
     */
    private PartitionKeyGroup createPartitionKeyGroupFromEntity(PartitionKeyGroupEntity partitionKeyGroupEntity)
    {
        // Create the partition key group.
        PartitionKeyGroup partitionKeyGroup = new PartitionKeyGroup();
        PartitionKeyGroupKey partitionKeyGroupKey = new PartitionKeyGroupKey();
        partitionKeyGroup.setPartitionKeyGroupKey(partitionKeyGroupKey);
        partitionKeyGroupKey.setPartitionKeyGroupName(partitionKeyGroupEntity.getPartitionKeyGroupName());

        return partitionKeyGroup;
    }
}
