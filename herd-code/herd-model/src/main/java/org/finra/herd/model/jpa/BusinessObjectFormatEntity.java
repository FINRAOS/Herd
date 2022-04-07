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

import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.Type;

/**
 * A business object format.
 */
@Table(name = BusinessObjectFormatEntity.TABLE_NAME)
@Entity
public class BusinessObjectFormatEntity extends AuditableEntity
{
    /**
     * The table name.
     */
    public static final String TABLE_NAME = "bus_objct_frmt";

    @Id
    @Column(name = TABLE_NAME + "_id")
    @GeneratedValue(generator = TABLE_NAME + "_seq")
    @SequenceGenerator(name = TABLE_NAME + "_seq", sequenceName = TABLE_NAME + "_seq", allocationSize = 1)
    private Long id;

    @JsonBackReference(value = "businessObjectDefinition-businessObjectFormats")
    @ManyToOne
    @JoinColumn(name = "bus_objct_dfntn_id", referencedColumnName = "bus_objct_dfntn_id", nullable = false)
    private BusinessObjectDefinitionEntity businessObjectDefinition;

    @Column(name = "bus_objct_dfntn_id", insertable = false, updatable = false)
    private Integer businessObjectDefinitionId;

    /**
     * The usage column.
     */
    @Column(name = "usage_cd")
    private String usage;

    @ManyToOne
    @JoinColumn(name = "file_type_cd", referencedColumnName = "file_type_cd", nullable = false)
    private FileTypeEntity fileType;

    @Column(name = "file_type_cd", insertable = false, updatable = false)
    private String fileTypeCode;

    /**
     * The format version column.
     */
    @Column(name = "frmt_vrsn_nb", nullable = false)
    private Integer businessObjectFormatVersion;

    @Column(name = "ltst_vrsn_fl")
    @Type(type = "yes_no")
    private Boolean latestVersion;

    @Column(name = "desc_tx")
    private String description;

    @Column(name = "doc_schm_tx")
    private String documentSchema;

    @Column(name = "doc_schm_url_tx")
    private String documentSchemaUrl;

    @JsonManagedReference(value = "businessObjectFormat-attributes")
    @OneToMany(mappedBy = "businessObjectFormat", orphanRemoval = true, cascade = {CascadeType.ALL})
    @OrderBy("name")
    private Collection<BusinessObjectFormatAttributeEntity> attributes;

    @Column(name = "prtn_key_tx")
    private String partitionKey;

    @Column(name = "null_value_tx")
    private String nullValue;

    @Column(name = "dlmtr_tx")
    private String delimiter;

    @Column(name = "clctn_items_dlmtr_tx")
    private String collectionItemsDelimiter;

    @Column(name = "map_keys_dlmtr_tx")
    private String mapKeysDelimiter;

    @Column(name = "escp_char_tx")
    private String escapeCharacter;

    @Column(name = "cstm_row_frmt_tx")
    private String customRowFormat;

    @Column(name = "cstm_clstr_by_tx")
    private String customClusteredBy;

    @Column(name = "cstm_tbl_prpties_tx")
    private String customTblProperties;

    @ManyToOne
    @JoinColumn(name = "prtn_key_group_tx", referencedColumnName = "prtn_key_group_tx")
    private PartitionKeyGroupEntity partitionKeyGroup;

    @JsonManagedReference(value = "businessObjectFormat-attributeDefinitions")
    @OneToMany(mappedBy = "businessObjectFormat", orphanRemoval = true, cascade = {CascadeType.ALL})
    @OrderBy("name")
    private Collection<BusinessObjectDataAttributeDefinitionEntity> attributeDefinitions;

    @JsonManagedReference(value = "businessObjectFormat-schemaColumns")
    @OneToMany(mappedBy = "businessObjectFormat", orphanRemoval = true, cascade = {CascadeType.ALL})
    @OrderBy("position")
    private Collection<SchemaColumnEntity> schemaColumns;

    // These are the parents (i.e. the data that was needed to create this data).
    @JsonIgnore
    @JoinTable(name = "bus_objct_frmt_prnt", joinColumns = {
        @JoinColumn(name = TABLE_NAME + "_id", referencedColumnName = TABLE_NAME + "_id")}, inverseJoinColumns = {
        @JoinColumn(name = "prnt_bus_objct_frmt_id", referencedColumnName = TABLE_NAME + "_id")})
    @ManyToMany
    private List<BusinessObjectFormatEntity> businessObjectFormatParents;

    // These are the children (i.e. the data that is dependent on this data).
    @JsonIgnore
    @ManyToMany(mappedBy = "businessObjectFormatParents")
    private List<BusinessObjectFormatEntity> businessObjectFormatChildren;

    @JsonIgnore
    @OneToMany(mappedBy = "businessObjectFormat", orphanRemoval = true, cascade = {CascadeType.ALL})
    @OrderBy("xtrnl_intrfc_cd")
    private Collection<BusinessObjectFormatExternalInterfaceEntity> businessObjectFormatExternalInterfaces;

    @Column(name = "rec_fl", nullable = true)
    @Type(type = "yes_no")
    private Boolean recordFlag;

    @Column(name = "alw_non_bckwrds_cmptbl_chgs_fl", nullable = true)
    @Type(type = "yes_no")
    private Boolean allowNonBackwardsCompatibleChanges;

    @Column(name = "rtntn_prd_days", nullable = true)
    private Integer retentionPeriodInDays;

    @ManyToOne
    @JoinColumn(name = "rtntn_type_cd", referencedColumnName = "rtntn_type_cd", nullable = true)
    private RetentionTypeEntity retentionType;

    @Column(name = "rltnl_schm_nm_tx")
    private String relationalSchemaName;

    @Column(name = "rltnl_tbl_nm_tx")
    private String relationalTableName;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public BusinessObjectDefinitionEntity getBusinessObjectDefinition()
    {
        return businessObjectDefinition;
    }

    public void setBusinessObjectDefinition(BusinessObjectDefinitionEntity businessObjectDefinition)
    {
        this.businessObjectDefinition = businessObjectDefinition;
    }

    public Integer getBusinessObjectDefinitionId()
    {
        return businessObjectDefinitionId;
    }

    public void setBusinessObjectDefinitionId(Integer businessObjectDefinitionId)
    {
        this.businessObjectDefinitionId = businessObjectDefinitionId;
    }

    public String getUsage()
    {
        return usage;
    }

    public void setUsage(String usage)
    {
        this.usage = usage;
    }

    public FileTypeEntity getFileType()
    {
        return fileType;
    }

    public void setFileType(FileTypeEntity fileType)
    {
        this.fileType = fileType;
    }

    public String getFileTypeCode()
    {
        return fileTypeCode;
    }

    public void setFileTypeCode(String fileTypeCode)
    {
        this.fileTypeCode = fileTypeCode;
    }

    public Integer getBusinessObjectFormatVersion()
    {
        return businessObjectFormatVersion;
    }

    public void setBusinessObjectFormatVersion(Integer businessObjectFormatVersion)
    {
        this.businessObjectFormatVersion = businessObjectFormatVersion;
    }

    public Boolean getLatestVersion()
    {
        return latestVersion;
    }

    public void setLatestVersion(Boolean latestVersion)
    {
        this.latestVersion = latestVersion;
    }

    public String getDescription()
    {
        return description;
    }

    public Collection<BusinessObjectFormatAttributeEntity> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(Collection<BusinessObjectFormatAttributeEntity> attributes)
    {
        this.attributes = attributes;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getPartitionKey()
    {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey)
    {
        this.partitionKey = partitionKey;
    }

    public String getNullValue()
    {
        return nullValue;
    }

    public void setNullValue(String nullValue)
    {
        this.nullValue = nullValue;
    }

    public String getDelimiter()
    {
        return delimiter;
    }

    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }

    public String getCollectionItemsDelimiter()
    {
        return collectionItemsDelimiter;
    }

    public void setCollectionItemsDelimiter(String collectionItemsDelimiter)
    {
        this.collectionItemsDelimiter = collectionItemsDelimiter;
    }

    public String getMapKeysDelimiter()
    {
        return mapKeysDelimiter;
    }

    public void setMapKeysDelimiter(String mapKeysDelimiter)
    {
        this.mapKeysDelimiter = mapKeysDelimiter;
    }

    public String getEscapeCharacter()
    {
        return escapeCharacter;
    }

    public void setEscapeCharacter(String escapeCharacter)
    {
        this.escapeCharacter = escapeCharacter;
    }

    public PartitionKeyGroupEntity getPartitionKeyGroup()
    {
        return partitionKeyGroup;
    }

    public void setPartitionKeyGroup(PartitionKeyGroupEntity partitionKeyGroup)
    {
        this.partitionKeyGroup = partitionKeyGroup;
    }

    public Collection<BusinessObjectDataAttributeDefinitionEntity> getAttributeDefinitions()
    {
        return attributeDefinitions;
    }

    public void setAttributeDefinitions(Collection<BusinessObjectDataAttributeDefinitionEntity> attributeDefinitions)
    {
        this.attributeDefinitions = attributeDefinitions;
    }

    public Collection<SchemaColumnEntity> getSchemaColumns()
    {
        return schemaColumns;
    }

    public void setSchemaColumns(Collection<SchemaColumnEntity> schemaColumns)
    {
        this.schemaColumns = schemaColumns;
    }

    public List<BusinessObjectFormatEntity> getBusinessObjectFormatParents()
    {
        return businessObjectFormatParents;
    }

    public void setBusinessObjectFormatParents(List<BusinessObjectFormatEntity> businessObjectFormatParents)
    {
        this.businessObjectFormatParents = businessObjectFormatParents;
    }

    public List<BusinessObjectFormatEntity> getBusinessObjectFormatChildren()
    {
        return businessObjectFormatChildren;
    }

    public void setBusinessObjectFormatChildren(List<BusinessObjectFormatEntity> businessObjectFormatChildren)
    {
        this.businessObjectFormatChildren = businessObjectFormatChildren;
    }

    public Collection<BusinessObjectFormatExternalInterfaceEntity> getBusinessObjectFormatExternalInterfaces()
    {
        return businessObjectFormatExternalInterfaces;
    }

    public void setBusinessObjectFormatExternalInterfaces(Collection<BusinessObjectFormatExternalInterfaceEntity> businessObjectFormatExternalInterfaces)
    {
        this.businessObjectFormatExternalInterfaces = businessObjectFormatExternalInterfaces;
    }

    public Boolean isRecordFlag()
    {
        return recordFlag;
    }

    public void setRecordFlag(Boolean recordFlag)
    {
        this.recordFlag = recordFlag;
    }

    public Integer getRetentionPeriodInDays()
    {
        return retentionPeriodInDays;
    }

    public void setRetentionPeriodInDays(Integer retentionPeriodInDays)
    {
        this.retentionPeriodInDays = retentionPeriodInDays;
    }

    public RetentionTypeEntity getRetentionType()
    {
        return retentionType;
    }

    public void setRetentionType(RetentionTypeEntity retentionType)
    {
        this.retentionType = retentionType;
    }

    public Boolean isAllowNonBackwardsCompatibleChanges()
    {
        return allowNonBackwardsCompatibleChanges;
    }

    public void setAllowNonBackwardsCompatibleChanges(Boolean allowNonBackwardsCompatibleChanges)
    {
        this.allowNonBackwardsCompatibleChanges = allowNonBackwardsCompatibleChanges;
    }

    public String getDocumentSchema()
    {
        return documentSchema;
    }

    public void setDocumentSchema(String documentSchema)
    {
        this.documentSchema = documentSchema;
    }

    public String getDocumentSchemaUrl()
    {
        return documentSchemaUrl;
    }

    public void setDocumentSchemaUrl(String documentSchemaUrl)
    {
        this.documentSchemaUrl = documentSchemaUrl;
    }

    public String getCustomRowFormat()
    {
        return customRowFormat;
    }

    public void setCustomRowFormat(String customRowFormat)
    {
        this.customRowFormat = customRowFormat;
    }

    public String getCustomClusteredBy()
    {
        return customClusteredBy;
    }

    public void setCustomClusteredBy(String customClusteredBy)
    {
        this.customClusteredBy = customClusteredBy;
    }

    public String getCustomTblProperties()
    {
        return customTblProperties;
    }

    public void setCustomTblProperties(String customTblProperties)
    {
        this.customTblProperties = customTblProperties;
    }

    public String getRelationalSchemaName()
    {
        return relationalSchemaName;
    }

    public void setRelationalSchemaName(String relationalSchemaName)
    {
        this.relationalSchemaName = relationalSchemaName;
    }

    public String getRelationalTableName()
    {
        return relationalTableName;
    }

    public void setRelationalTableName(String relationalTableName)
    {
        this.relationalTableName = relationalTableName;
    }

}
