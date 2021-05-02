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
package org.finra.herd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.finra.herd.model.api.xml.NamespaceIamRoleAuthorizationCreateRequest;
import org.finra.herd.model.api.xml.NamespaceIamRoleAuthorizationKey;

/**
 * NamespaceIamRoleAuthorizationServiceTestHelper
 */
@Component
public class NamespaceIamRoleAuthorizationServiceTestHelper
{
    @Autowired
    private NamespaceIamRoleAuthorizationService namespaceIamRoleAuthorizationService;

    /**
     * Helper method to create an iam role authorization.
     *
     * @param namespace the namespace
     * @param iamRoleName the IAM role name
     * @param description the description
     */
    public void createNamespaceIamRoleAuthorization(final String namespace, final String iamRoleName, final String description)
    {
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(namespace, iamRoleName);
        NamespaceIamRoleAuthorizationCreateRequest request =
            new NamespaceIamRoleAuthorizationCreateRequest(namespaceIamRoleAuthorizationKey, description);
        namespaceIamRoleAuthorizationService.createNamespaceIamRoleAuthorization(request);
    }
}
