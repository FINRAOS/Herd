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
package org.finra.herd.dao.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import org.finra.herd.dao.NotificationMessageDao;
import org.finra.herd.model.jpa.NotificationMessageEntity;
import org.finra.herd.model.jpa.NotificationMessageEntity_;

@Repository
public class NotificationMessageDaoImpl extends AbstractHerdDao implements NotificationMessageDao
{
    @Override
    public NotificationMessageEntity getOldestNotificationMessage()
    {
        // Create the criteria builder and the criteria.
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<NotificationMessageEntity> criteria = builder.createQuery(NotificationMessageEntity.class);

        // The criteria root is the notification message.
        Root<NotificationMessageEntity> notificationMessageEntity = criteria.from(NotificationMessageEntity.class);

        // Add the select clause.
        criteria.select(notificationMessageEntity);

        // Add the order by clause, since we want to return only the oldest notification message (a message with the smallest sequence generated id).
        criteria.orderBy(builder.asc(notificationMessageEntity.get(NotificationMessageEntity_.id)));

        // Execute the query and ask it to return only the first record.
        List<NotificationMessageEntity> resultList = entityManager.createQuery(criteria).setMaxResults(1).getResultList();

        // Return the result.
        return resultList.size() > 0 ? resultList.get(0) : null;
    }
}
