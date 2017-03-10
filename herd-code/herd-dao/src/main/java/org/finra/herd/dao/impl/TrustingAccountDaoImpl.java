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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import org.finra.herd.dao.TrustingAccountDao;
import org.finra.herd.model.jpa.TrustingAccountEntity;
import org.finra.herd.model.jpa.TrustingAccountEntity_;

@Repository
public class TrustingAccountDaoImpl extends AbstractHerdDao implements TrustingAccountDao
{
    @Override
    public TrustingAccountEntity getTrustingAccountById(String accountId)
    {
        // Create the criteria builder and the criteria.
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrustingAccountEntity> criteria = builder.createQuery(TrustingAccountEntity.class);

        // The criteria root is the trusting account.
        Root<TrustingAccountEntity> trustingAccountEntity = criteria.from(TrustingAccountEntity.class);

        // Create the standard restrictions (i.e. the standard where clauses).
        Predicate queryRestriction = builder.equal(trustingAccountEntity.get(TrustingAccountEntity_.id), accountId);

        // Add the clauses for the query.
        criteria.select(trustingAccountEntity).where(queryRestriction);

        // Execute the query.
        List<TrustingAccountEntity> resultList = entityManager.createQuery(criteria).getResultList();

        // Return single result or null.
        return resultList.size() >= 1 ? resultList.get(0) : null;
    }
}
