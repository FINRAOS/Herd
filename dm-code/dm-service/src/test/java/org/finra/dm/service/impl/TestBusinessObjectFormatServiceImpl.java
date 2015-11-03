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
package org.finra.dm.service.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.finra.dm.dao.config.DaoSpringModuleConfig;
import org.finra.dm.model.api.xml.BusinessObjectFormat;
import org.finra.dm.model.api.xml.BusinessObjectFormatDdl;
import org.finra.dm.model.api.xml.BusinessObjectFormatDdlCollectionRequest;
import org.finra.dm.model.api.xml.BusinessObjectFormatDdlCollectionResponse;
import org.finra.dm.model.api.xml.BusinessObjectFormatDdlRequest;
import org.finra.dm.model.api.xml.BusinessObjectFormatKey;

/**
 * This is a Business Object Format service implementation for testing.  It overwrites the base class methods to change transactional attributes.
 */
@Service
@Transactional(value = DaoSpringModuleConfig.DM_TRANSACTION_MANAGER_BEAN_NAME)
@Primary
public class TestBusinessObjectFormatServiceImpl extends BusinessObjectFormatServiceImpl
{
    @Override
    public BusinessObjectFormat getBusinessObjectFormat(BusinessObjectFormatKey businessObjectFormatKey)
    {
        return getBusinessObjectFormatImpl(businessObjectFormatKey);
    }

    @Override
    public BusinessObjectFormatDdl generateBusinessObjectFormatDdl(BusinessObjectFormatDdlRequest request)
    {
        return generateBusinessObjectFormatDdlImpl(request, false);
    }

    @Override
    public BusinessObjectFormatDdlCollectionResponse generateBusinessObjectFormatDdlCollection(BusinessObjectFormatDdlCollectionRequest request)
    {
        return generateBusinessObjectFormatDdlCollectionImpl(request);
    }
}
