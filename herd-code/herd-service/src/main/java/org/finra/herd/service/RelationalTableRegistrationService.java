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
package org.finra.herd.service;

import org.finra.herd.model.api.xml.BusinessObjectData;
import org.finra.herd.model.api.xml.RelationalTableRegistrationCreateRequest;

/**
 * The relational table registration service.
 */
public interface RelationalTableRegistrationService
{
    /**
     * Creates a new relational table registration. The relation table registration includes creation of the following entities: <ul> <li>a business object
     * definition</li> <li>a business object format with schema extracted from the specified relational table in the specified storage of RELATIONAL storage
     * platform type</li> <li>a business object data</li> <li>a storage unit that links together the business object data with the storage</li> </ul>
     *
     * @param relationalTableRegistrationCreateRequest the relational table registration create request
     *
     * @return the information for the newly created business object data
     */
    BusinessObjectData createRelationalTableRegistration(RelationalTableRegistrationCreateRequest relationalTableRegistrationCreateRequest);
}
