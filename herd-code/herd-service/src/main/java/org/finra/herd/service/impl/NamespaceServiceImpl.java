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

import org.finra.herd.dao.NamespaceDao;
import org.finra.herd.dao.config.DaoSpringModuleConfig;
import org.finra.herd.model.AlreadyExistsException;
import org.finra.herd.model.api.xml.Namespace;
import org.finra.herd.model.api.xml.NamespaceCreateRequest;
import org.finra.herd.model.api.xml.NamespaceKey;
import org.finra.herd.model.api.xml.NamespaceKeys;
import org.finra.herd.model.api.xml.NamespaceUpdateRequest;
import org.finra.herd.model.jpa.NamespaceEntity;
import org.finra.herd.service.NamespaceService;
import org.finra.herd.service.helper.AlternateKeyHelper;
import org.finra.herd.service.helper.NamespaceDaoHelper;
import org.finra.herd.service.helper.NamespaceHelper;
import org.finra.herd.service.helper.S3KeyPrefixHelper;

/**
 * The namespace service implementation.
 */
@Service
@Transactional(value = DaoSpringModuleConfig.HERD_TRANSACTION_MANAGER_BEAN_NAME)
public class NamespaceServiceImpl implements NamespaceService
{
    @Autowired
    private AlternateKeyHelper alternateKeyHelper;

    @Autowired
    private NamespaceDao namespaceDao;

    @Autowired
    private NamespaceDaoHelper namespaceDaoHelper;

    @Autowired
    private NamespaceHelper namespaceHelper;

    @Autowired
    private S3KeyPrefixHelper s3KeyPrefixHelper;

    @Override
    public Namespace createNamespace(NamespaceCreateRequest request)
    {
        // Validation and trim the request.
        validateNamespaceCreateRequest(request);

        // Get the namespace key.
        NamespaceKey namespaceKey = new NamespaceKey(request.getNamespaceCode());

        // Ensure a namespace with the specified namespace key doesn't already exist.
        NamespaceEntity namespaceEntity = namespaceDao.getNamespaceByKey(namespaceKey);
        if (namespaceEntity != null)
        {
            throw new AlreadyExistsException(String.format("Unable to create namespace \"%s\" because it already exists.", namespaceKey.getNamespaceCode()));
        }

        // Create a namespace entity from the request information.
        namespaceEntity = createNamespaceEntity(request);

        // Persist the new entity.
        namespaceEntity = namespaceDao.saveAndRefresh(namespaceEntity);

        // Create and return the namespace object from the persisted entity.
        return createNamespaceFromEntity(namespaceEntity);
    }

    @Override
    public Namespace getNamespace(NamespaceKey namespaceKey)
    {
        // Perform validation and trim.
        namespaceHelper.validateNamespaceKey(namespaceKey);

        // Retrieve and ensure that a namespace already exists with the specified key.
        NamespaceEntity namespaceEntity = namespaceDaoHelper.getNamespaceEntity(namespaceKey.getNamespaceCode());

        // Create and return the namespace object from the persisted entity.
        return createNamespaceFromEntity(namespaceEntity);
    }

    @Override
    public Namespace deleteNamespace(NamespaceKey namespaceKey)
    {
        // Perform validation and trim.
        namespaceHelper.validateNamespaceKey(namespaceKey);

        // Retrieve and ensure that a namespace already exists with the specified key.
        NamespaceEntity namespaceEntity = namespaceDaoHelper.getNamespaceEntity(namespaceKey.getNamespaceCode());

        // Delete the namespace.
        namespaceDao.delete(namespaceEntity);

        // Create and return the namespace object from the deleted entity.
        return createNamespaceFromEntity(namespaceEntity);
    }

    @Override
    public NamespaceKeys getNamespaces()
    {
        NamespaceKeys namespaceKeys = new NamespaceKeys();
        namespaceKeys.getNamespaceKeys().addAll(namespaceDao.getNamespaces());
        return namespaceKeys;
    }

    @Override
    public Namespace updateNamespaces(NamespaceKey namespaceKey, NamespaceUpdateRequest request)
    {
        // Perform validation and trim for namespaceCode.
        namespaceHelper.validateNamespaceKey(namespaceKey);

        // Perform validation and trim for chargeCode.
        validateNamespaceUpdateRequest(request);

        // Retrieve and ensure that a namespace already exists with the specified key.
        NamespaceEntity namespaceEntity = namespaceDaoHelper.getNamespaceEntity(namespaceKey.getNamespaceCode());

        // Update the namespace entity from the request information.
        namespaceEntity = updateNamespaceEntity(namespaceEntity, request);

        // Persist the new entity.
        namespaceEntity = namespaceDao.saveAndRefresh(namespaceEntity);

        // Create and return the namespace object from the deleted entity.
        return createNamespaceFromEntity(namespaceEntity);
    }

    /**
     * Validates the namespace create request. This method also trims request parameters.
     *
     * @param request the request
     *
     * @throws IllegalArgumentException if any validation errors were found
     */
    private void validateNamespaceCreateRequest(NamespaceCreateRequest request)
    {
        request.setNamespaceCode(alternateKeyHelper.validateStringParameter("namespace", request.getNamespaceCode()));

        if (request.getChargeCode() != null)
        {
            request.setChargeCode(request.getChargeCode().trim());
        }
    }

    /**
     * Creates a new namespace entity from the request information.
     *
     * @param request the request
     *
     * @return the newly created namespace entity
     */
    private NamespaceEntity createNamespaceEntity(NamespaceCreateRequest request)
    {
        // Create a new entity.
        NamespaceEntity namespaceEntity = new NamespaceEntity();
        namespaceEntity.setCode(request.getNamespaceCode());
        namespaceEntity.setChargeCode(request.getChargeCode());
        return namespaceEntity;
    }

    /**
     * Creates the namespace from the persisted entity.
     *
     * @param namespaceEntity the newly persisted namespace entity.
     *
     * @return the namespace.
     */
    private Namespace createNamespaceFromEntity(NamespaceEntity namespaceEntity)
    {
        // Create the namespace information.
        Namespace namespace = new Namespace();
        namespace.setNamespaceCode(namespaceEntity.getCode());
        namespace.setChargeCode(namespaceEntity.getChargeCode());
        namespace.setS3KeyPrefix(s3KeyPrefixHelper.s3KeyPrefixFormat(namespaceEntity.getCode()));
        return namespace;
    }

    /**
     * Validates the namespace update request. This method also trims request parameters.
     *
     * @param request the request
     *
     * @throws IllegalArgumentException if any validation errors were found
     */
    private void validateNamespaceUpdateRequest(NamespaceUpdateRequest request)
    {
        if (request.getChargeCode() != null)
        {
            request.setChargeCode(request.getChargeCode().trim());
        }
    }

    /**
     * Updates a new namespace entity from the request information.
     *
     * @param request the request
     *
     * @return the newly created namespace entity
     */
    private NamespaceEntity updateNamespaceEntity(NamespaceEntity namespaceEntity, NamespaceUpdateRequest request)
    {
        namespaceEntity.setChargeCode(request.getChargeCode());
        return namespaceEntity;
    }
}
