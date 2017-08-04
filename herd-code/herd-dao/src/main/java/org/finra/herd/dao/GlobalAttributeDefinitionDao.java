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

import java.util.List;

import org.finra.herd.model.api.xml.GlobalAttributeDefinitionKey;
import org.finra.herd.model.jpa.GlobalAttributeDefinitionEntity;

public interface GlobalAttributeDefinitionDao extends BaseJpaDao
{
    /**
     * Gets a list of keys for all global attribute definitions registered in the system.
     *
     * @return the global attribute definitions list
     */
    public List<GlobalAttributeDefinitionKey> getAllGlobalAttributeDefinitionKeys();

    /**
     * Gets a global attribute definition based on its key.
     *
     * @param globalAttributeDefinitionKey the global attribute definition key
     *
     * @return the global attribute definition
     */
    public GlobalAttributeDefinitionEntity getGlobalAttributeDefinitionByKey(GlobalAttributeDefinitionKey globalAttributeDefinitionKey);
}
