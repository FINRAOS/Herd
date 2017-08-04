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

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.finra.herd.dao.config.DaoSpringModuleConfig;
import org.finra.herd.model.annotation.PublishNotificationMessages;
import org.finra.herd.model.api.xml.BusinessObjectDataKey;
import org.finra.herd.model.dto.CompleteUploadSingleParamsDto;

/**
 * This is an upload download helper service implementation for testing.
 */
@Service
@Transactional(value = DaoSpringModuleConfig.HERD_TRANSACTION_MANAGER_BEAN_NAME)
@Primary
public class TestUploadDownloadHelperServiceImpl extends UploadDownloadHelperServiceImpl
{
    /**
     * {@inheritDoc}
     * <p/>
     * Overwrite the base class method to change transactional attributes.
     */
    @PublishNotificationMessages
    @Override
    public void prepareForFileMove(String objectKey, CompleteUploadSingleParamsDto completeUploadSingleParamsDto)
    {
        prepareForFileMoveImpl(objectKey, completeUploadSingleParamsDto);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overwrite the base class method to change transactional attributes.
     */
    @Override
    public void performFileMove(CompleteUploadSingleParamsDto completeUploadSingleParamsDto)
    {
        performFileMoveImpl(completeUploadSingleParamsDto);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overwrite the base class method to change transactional attributes.
     */
    @PublishNotificationMessages
    @Override
    public void executeFileMoveAfterSteps(CompleteUploadSingleParamsDto completeUploadSingleParamsDto)
    {
        executeFileMoveAfterStepsImpl(completeUploadSingleParamsDto);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overwrite the base class method to change transactional attributes.
     */
    @PublishNotificationMessages
    @Override
    public void updateBusinessObjectDataStatus(BusinessObjectDataKey businessObjectDataKey, String businessObjectDataStatus)
    {
        updateBusinessObjectDataStatusImpl(businessObjectDataKey, businessObjectDataStatus);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overwrite the base class method to change transactional attributes.
     */
    @Override
    public void deleteSourceFileFromS3(CompleteUploadSingleParamsDto completeUploadSingleParamsDto)
    {
        deleteSourceFileFromS3Impl(completeUploadSingleParamsDto);
    }
}
