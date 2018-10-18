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
package org.finra.herd.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.finra.herd.model.jpa.ExternalInterfaceEntity;

@Component
public class ExternalInterfaceDaoTestHelper
{
    @Autowired
    private ExternalInterfaceDao externalInterfaceDao;

    /**
     * Creates and persists an external interface entity.
     *
     * @param code the name of the external interface
     *
     * @return the external interface entity
     */
    public ExternalInterfaceEntity createExternalInterfaceEntity(String code)
    {
        ExternalInterfaceEntity externalInterfaceEntity = new ExternalInterfaceEntity();
        externalInterfaceEntity.setCode(code);
        return externalInterfaceDao.saveAndRefresh(externalInterfaceEntity);
    }
}
