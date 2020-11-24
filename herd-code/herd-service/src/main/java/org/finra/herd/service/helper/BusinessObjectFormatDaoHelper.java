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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.finra.herd.dao.BusinessObjectFormatDao;
import org.finra.herd.dao.RetentionTypeDao;
import org.finra.herd.model.ObjectNotFoundException;
import org.finra.herd.model.api.xml.BusinessObjectFormatKey;
import org.finra.herd.model.jpa.BusinessObjectFormatAttributeEntity;
import org.finra.herd.model.jpa.BusinessObjectFormatEntity;
import org.finra.herd.model.jpa.RetentionTypeEntity;

/**
 * Helper for business object format related operations which require DAO.
 */
@Component
public class BusinessObjectFormatDaoHelper
{
    @Autowired
    private BusinessObjectFormatDao businessObjectFormatDao;

    @Autowired
    private BusinessObjectFormatHelper businessObjectFormatHelper;

    @Autowired
    private RetentionTypeDao retentionTypeDao;

    /**
     * Gets a business object format entity based on the alternate key and makes sure that it exists. If a format version isn't specified in the business object
     * format alternate key, the latest available format version will be used.
     *
     * @param businessObjectFormatKey the business object format key
     *
     * @return the business object format entity
     *
     * @throws ObjectNotFoundException if the business object format entity doesn't exist
     */
    public BusinessObjectFormatEntity getBusinessObjectFormatEntity(BusinessObjectFormatKey businessObjectFormatKey) throws ObjectNotFoundException
    {
        BusinessObjectFormatEntity businessObjectFormatEntity = businessObjectFormatDao.getBusinessObjectFormatByAltKey(businessObjectFormatKey);

        if (businessObjectFormatEntity == null)
        {
            throw new ObjectNotFoundException(String.format("Business object format with namespace \"%s\", business object definition name \"%s\", " +
                    "format usage \"%s\", format file type \"%s\", and format version \"%d\" doesn't exist.", businessObjectFormatKey.getNamespace(),
                businessObjectFormatKey.getBusinessObjectDefinitionName(), businessObjectFormatKey.getBusinessObjectFormatUsage(),
                businessObjectFormatKey.getBusinessObjectFormatFileType(), businessObjectFormatKey.getBusinessObjectFormatVersion()));
        }

        return businessObjectFormatEntity;
    }

    /**
     * Gets record retention type entity form retention type code
     *
     * @param retentionTypeCode retention type code
     *
     * @return the retention type entity
     */
    public RetentionTypeEntity getRecordRetentionTypeEntity(String retentionTypeCode)
    {
        RetentionTypeEntity recordRetentionTypeEntity = retentionTypeDao.getRetentionTypeByCode(retentionTypeCode);

        if (recordRetentionTypeEntity == null)
        {
            throw new ObjectNotFoundException(String.format("Record retention type with code \"%s\" doesn't exist.", retentionTypeCode));
        }

        return recordRetentionTypeEntity;
    }
}
