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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.finra.herd.model.api.xml.Attribute;
import org.finra.herd.model.api.xml.GlobalAttributeDefinitionKey;
import org.finra.herd.model.api.xml.GlobalAttributeDefinitionKeys;
import org.finra.herd.model.jpa.GlobalAttributeDefinitionLevelEntity;
import org.finra.herd.service.GlobalAttributeDefinitionService;

/**
 * A helper class for Attribute related code.
 */
@Component
public class AttributeHelper
{
    @Autowired
    private AlternateKeyHelper alternateKeyHelper;

    @Autowired
    private GlobalAttributeDefinitionService globalAttributeDefinitionService;

    @Autowired
    private GlobalAttributeDefinitionDaoHelper globalAttributeDefinitionDaoHelper;

    /**
     * Validates the attributes.
     *
     * @param attributes the attributes to validate. Null shouldn't be specified.
     * @return the validated attribute map
     * @throws IllegalArgumentException if any invalid attributes were found.
     */
    public  Map<String, String> validateAttributes(List<Attribute> attributes) throws IllegalArgumentException
    {
        // Validate attributes if they are specified.
        Map<String, String> attributeNameValidationMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(attributes))
        {
            for (Attribute attribute : attributes)
            {
                attribute.setName(alternateKeyHelper.validateStringParameter("An", "attribute name", attribute.getName()));

                // Ensure the attribute key isn't a duplicate by using a map with a "lowercase" name as the key for case insensitivity.
                String validationMapKey = attribute.getName().toLowerCase();
                if (attributeNameValidationMap.containsKey(validationMapKey))
                {
                    throw new IllegalArgumentException("Duplicate attribute name found: " + attribute.getName());
                }
                attributeNameValidationMap.put(validationMapKey, attribute.getValue());
            }
        }

        return attributeNameValidationMap;
    }

    /**
     * Validate format attributes
     *
     * @param attributes the attributes
     * 
     * @throws IllegalArgumentException  if any invalid attributes were found.
     */
    public void validateFormatAttributes(List<Attribute> attributes) throws IllegalArgumentException
    {
        Map<String, String> attributeNameValidationMap = validateAttributes(attributes);

        //Validate each format level global attribute exists and attribute value is from allowed list, if the allowed list exists
        for (GlobalAttributeDefinitionKey globalAttributeFormat : getGlobalAttributesDefinitionForFormat())
        {
            String globalAttributeDefinitionNameOriginal = globalAttributeFormat.getGlobalAttributeDefinitionName();
            String globalAttributeDefinitionName = globalAttributeDefinitionNameOriginal.toLowerCase();
            if (!attributeNameValidationMap.containsKey(globalAttributeDefinitionName) ||
                StringUtils.isBlank(attributeNameValidationMap.get(globalAttributeDefinitionName)))
            {
                throw new IllegalArgumentException(String
                    .format("The business object format has a required attribute \"%s\" which was not specified or has a value which is blank.",
                        globalAttributeDefinitionNameOriginal));
            }
            else
            {
                List<String> allowedAttributeValues = globalAttributeDefinitionDaoHelper.getAllowedAttributeValues(globalAttributeFormat);
                if (allowedAttributeValues != null)
                {
                    String attributeValue = attributeNameValidationMap.get(globalAttributeDefinitionName);
                    if (!allowedAttributeValues.contains(attributeValue))
                    {
                        throw new IllegalArgumentException(String
                            .format("The business object format attribute \"%s\" value \"%s\" is not from allowed attribute values.",
                                globalAttributeDefinitionNameOriginal, attributeValue));
                    }
                }
            }
        }
    }

    /**
     * Return all the format level global attribute definitions
     *
     * @return global attribute definition keys
     */
    public List<GlobalAttributeDefinitionKey> getGlobalAttributesDefinitionForFormat()
    {
        List<GlobalAttributeDefinitionKey> globalAttributeDefinitionKeys = new ArrayList<>();
        GlobalAttributeDefinitionKeys globalAttributesDefinitions = globalAttributeDefinitionService.getGlobalAttributeDefinitionKeys();
        for (GlobalAttributeDefinitionKey globalAttributeDefinitionKey : globalAttributesDefinitions.getGlobalAttributeDefinitionKeys())
        {
            if (GlobalAttributeDefinitionLevelEntity.GlobalAttributeDefinitionLevels.BUS_OBJCT_FRMT.name()
                .equalsIgnoreCase(globalAttributeDefinitionKey.getGlobalAttributeDefinitionLevel()))
            {
                globalAttributeDefinitionKeys.add(globalAttributeDefinitionKey);
            }
        }

        return globalAttributeDefinitionKeys;
    }
}
