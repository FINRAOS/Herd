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
package org.finra.herd.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Allowed attribute values entity.
 */
@Table(name = AllowedAttributeValueEntity.TABLE_NAME)
@Entity
public class AllowedAttributeValueEntity extends AuditableEntity
{
    /**
     * The table name.
     */
    public static final String TABLE_NAME = "alwd_atrbt_value";

    @Id
    @Column(name = TABLE_NAME + "_id")
    @GeneratedValue(generator = TABLE_NAME + "_seq")
    @SequenceGenerator(name = TABLE_NAME + "_seq", sequenceName = TABLE_NAME + "_seq", allocationSize = 1)
    private Integer id;

    /**
     * The attribute value list id column.
     */
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "atrbt_value_list_id", referencedColumnName = "atrbt_value_list_id", nullable = false)
    private AttributeValueListEntity attributeValueList;

    /**
     * The allowed attribute value column.
     */
    @Column(name = "alwd_atrbt_value_tx", nullable = false)
    private String allowedAttributeValue;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public AttributeValueListEntity getAttributeValueList()
    {
        return attributeValueList;
    }

    public void setAttributeValueList(AttributeValueListEntity attributeValueList)
    {
        this.attributeValueList = attributeValueList;
    }

    public String getAllowedAttributeValue()
    {
        return allowedAttributeValue;
    }

    public void setAllowedAttributeValue(String allowedAttributeValue)
    {
        this.allowedAttributeValue = allowedAttributeValue;
    }
}
