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
package org.finra.herd.rest;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.finra.herd.model.api.xml.SearchIndexValidation;
import org.finra.herd.model.api.xml.SearchIndexValidationCreateRequest;
import org.finra.herd.model.dto.SecurityFunctions;
import org.finra.herd.service.SearchIndexValidationService;
import org.finra.herd.ui.constants.UiConstants;

/**
 * The REST controller that handles search index validation requests.
 */
@RestController
@RequestMapping(value = UiConstants.REST_URL_BASE, produces = {"application/xml", "application/json"})
@Api(tags = "Search Index Validation")
public class SearchIndexValidationRestController extends HerdBaseController
{
    public static final String SEARCH_INDEX_VALIDATION_URI_PREFIX = "/searchIndexValidations";

    @Autowired
    private SearchIndexValidationService searchIndexValidationService;

    /**
     * Validates the search index.
     *
     * @param request the information needed to validate a search index
     *
     * @return the validation response
     */
    @RequestMapping(value = SEARCH_INDEX_VALIDATION_URI_PREFIX, method = RequestMethod.POST, consumes = {"application/xml", "application/json"})
    @Secured(SecurityFunctions.FN_SEARCH_INDEXES_VALIDATION_POST)
    public SearchIndexValidation createSearchIndexValidation(@RequestBody SearchIndexValidationCreateRequest request)
    {
        return searchIndexValidationService.createSearchIndexValidation(request);
    }

}
