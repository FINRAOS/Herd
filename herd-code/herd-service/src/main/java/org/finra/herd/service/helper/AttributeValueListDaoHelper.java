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
package org.finra.herd.service.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.finra.herd.dao.AttributeValueListDao;
import org.finra.herd.model.ObjectNotFoundException;
import org.finra.herd.model.api.xml.AttributeValueList;
import org.finra.herd.model.api.xml.AttributeValueListKey;
import org.finra.herd.model.jpa.AttributeValueListEntity;

/**
 * Helper for attribute value list related operations which require DAO.
 */
@Component
public class AttributeValueListDaoHelper
{
    @Autowired
    private AttributeValueListDao attributeValueListDao;

    /**
     * Gets the attribute value list entity and ensure it exists.
     *
     * @param attributeValueListKey the attribute value list key (case insensitive)
     *
     * @return the attribute value list entity
     */
    public AttributeValueListEntity getAttributeValueListEntity(AttributeValueListKey attributeValueListKey)
    {
        AttributeValueListEntity attributeValueListEntity = attributeValueListDao.getAttributeValueListByKey(attributeValueListKey);

        if (attributeValueListEntity == null)
        {
            throw new ObjectNotFoundException(String
                .format("Attribute value list with name \"%s\" doesn't exist for namespace \"%s\".", attributeValueListKey.getAttributeValueListName(),
                    attributeValueListKey.getNamespace()));
        }

        return attributeValueListEntity;
    }

    /**
     * Creates the attribute value list from the persisted entity.
     *
     * @param attributeValueListEntity the attribute value list entity
     *
     * @return the attribute value list
     */
    public AttributeValueList createAttributeValueListFromEntity(AttributeValueListEntity attributeValueListEntity)
    {
        // Create the attribute value list.
        AttributeValueList attributeValueList = new AttributeValueList();

        AttributeValueListKey attributeValueListKey = new AttributeValueListKey();
        attributeValueListKey.setNamespace(attributeValueListEntity.getNamespace().getCode());
        attributeValueListKey.setAttributeValueListName(attributeValueListEntity.getName());

        attributeValueList.setAttributeValueListKey(attributeValueListKey);
        attributeValueList.setId(attributeValueListEntity.getId());

        return attributeValueList;
    }
}
