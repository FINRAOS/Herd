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
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * A storage unit status.
 */
@Table(name = "strge_unit_stts_cd_lk")
@Entity
public class StorageUnitStatusEntity extends AuditableEntity
{
    // List of common statuses
    public static final String ENABLED = "ENABLED";

    public static final String DISABLED = "DISABLED";

    public static final String ARCHIVING = "ARCHIVING";

    public static final String ARCHIVED = "ARCHIVED";

    public static final String RESTORING = "RESTORING";

    public static final String RESTORED = "RESTORED";

    public static final String EXPIRING = "EXPIRING";

    @Id
    @Column(name = "strge_unit_stts_cd")
    private String code;

    @Column(name = "strge_unit_stts_ds")
    private String description;

    @Column(name = "avlbl_fl")
    @Type(type = "yes_no")
    private Boolean available;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Boolean getAvailable()
    {
        return available;
    }

    public void setAvailable(Boolean available)
    {
        this.available = available;
    }
}
