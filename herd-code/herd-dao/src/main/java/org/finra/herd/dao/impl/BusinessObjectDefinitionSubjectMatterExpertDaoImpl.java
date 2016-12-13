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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import org.finra.herd.dao.BusinessObjectDefinitionSubjectMatterExpertDao;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionSubjectMatterExpertKey;
import org.finra.herd.model.jpa.BusinessObjectDefinitionEntity;
import org.finra.herd.model.jpa.BusinessObjectDefinitionEntity_;
import org.finra.herd.model.jpa.BusinessObjectDefinitionSubjectMatterExpertEntity;
import org.finra.herd.model.jpa.BusinessObjectDefinitionSubjectMatterExpertEntity_;
import org.finra.herd.model.jpa.NamespaceEntity;
import org.finra.herd.model.jpa.NamespaceEntity_;

@Repository
public class BusinessObjectDefinitionSubjectMatterExpertDaoImpl extends AbstractHerdDao implements BusinessObjectDefinitionSubjectMatterExpertDao
{
    @Override
    public BusinessObjectDefinitionSubjectMatterExpertEntity getBusinessObjectDefinitionSubjectMatterExpert(
        BusinessObjectDefinitionEntity businessObjectDefinitionEntity, String userId)
    {
        // Create the criteria builder and the criteria.
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<BusinessObjectDefinitionSubjectMatterExpertEntity> criteria =
            builder.createQuery(BusinessObjectDefinitionSubjectMatterExpertEntity.class);

        // The criteria root is the business object definition subject matter expert.
        Root<BusinessObjectDefinitionSubjectMatterExpertEntity> businessObjectDefinitionSubjectMatterExpertEntityRoot =
            criteria.from(BusinessObjectDefinitionSubjectMatterExpertEntity.class);

        // Create the standard restrictions (i.e. the standard where clauses).
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder
            .equal(businessObjectDefinitionSubjectMatterExpertEntityRoot.get(BusinessObjectDefinitionSubjectMatterExpertEntity_.businessObjectDefinition),
                businessObjectDefinitionEntity));
        predicates.add(builder
            .equal(builder.upper(businessObjectDefinitionSubjectMatterExpertEntityRoot.get(BusinessObjectDefinitionSubjectMatterExpertEntity_.userId)),
                userId.toUpperCase()));

        // Add the clauses for the query.
        criteria.select(businessObjectDefinitionSubjectMatterExpertEntityRoot).where(builder.and(predicates.toArray(new Predicate[predicates.size()])));

        // Execute the query and return the results.
        return executeSingleResultQuery(criteria, String.format(
            "Found more than one business object definition subject matter expert instance with parameters {namespace=\"%s\", " +
                "businessObjectDefinitionName=\"%s\", userId=\"%s\"}.", businessObjectDefinitionEntity.getNamespace().getCode(),
            businessObjectDefinitionEntity.getName(), userId));
    }

    @Override
    public BusinessObjectDefinitionSubjectMatterExpertEntity getBusinessObjectDefinitionSubjectMatterExpertByKey(
        BusinessObjectDefinitionSubjectMatterExpertKey key)
    {
        // Create the criteria builder and the criteria.
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<BusinessObjectDefinitionSubjectMatterExpertEntity> criteria =
            builder.createQuery(BusinessObjectDefinitionSubjectMatterExpertEntity.class);

        // The criteria root is the business object definition subject matter expert.
        Root<BusinessObjectDefinitionSubjectMatterExpertEntity> businessObjectDefinitionSubjectMatterExpertEntityRoot =
            criteria.from(BusinessObjectDefinitionSubjectMatterExpertEntity.class);

        // Join to the other tables we can filter on.
        Join<BusinessObjectDefinitionSubjectMatterExpertEntity, BusinessObjectDefinitionEntity> businessObjectDefinitionEntityJoin =
            businessObjectDefinitionSubjectMatterExpertEntityRoot.join(BusinessObjectDefinitionSubjectMatterExpertEntity_.businessObjectDefinition);
        Join<BusinessObjectDefinitionEntity, NamespaceEntity> namespaceEntityJoin =
            businessObjectDefinitionEntityJoin.join(BusinessObjectDefinitionEntity_.namespace);

        // Create the standard restrictions (i.e. the standard where clauses).
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(builder.upper(namespaceEntityJoin.get(NamespaceEntity_.code)), key.getNamespace().toUpperCase()));
        predicates.add(builder.equal(builder.upper(businessObjectDefinitionEntityJoin.get(BusinessObjectDefinitionEntity_.name)),
            key.getBusinessObjectDefinitionName().toUpperCase()));
        predicates.add(builder
            .equal(builder.upper(businessObjectDefinitionSubjectMatterExpertEntityRoot.get(BusinessObjectDefinitionSubjectMatterExpertEntity_.userId)),
                key.getUserId().toUpperCase()));

        // Add the clauses for the query.
        criteria.select(businessObjectDefinitionSubjectMatterExpertEntityRoot).where(builder.and(predicates.toArray(new Predicate[predicates.size()])));

        // Execute the query and return the results.
        return executeSingleResultQuery(criteria, String.format(
            "Found more than one business object definition subject matter expert instance with parameters {namespace=\"%s\", " +
                "businessObjectDefinitionName=\"%s\", userId=\"%s\"}.", key.getNamespace(), key.getBusinessObjectDefinitionName(), key.getUserId()));
    }
}
