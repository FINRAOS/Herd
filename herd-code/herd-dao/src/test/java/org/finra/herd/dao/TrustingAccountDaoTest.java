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
package org.finra.herd.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import org.finra.herd.model.jpa.TrustingAccountEntity;

public class TrustingAccountDaoTest extends AbstractDaoTest
{
    @Test
    public void testGetTrustingAccountById()
    {
        // Create a trusting account entity.
        TrustingAccountEntity trustingAccountEntity = trustingAccountDaoTestHelper.createTrustingAccountEntity(AWS_ACCOUNT_ID, AWS_ROLE_ARN);

        // Retrieve the trusting account entity.
        assertEquals(trustingAccountEntity, trustingAccountDao.getTrustingAccountById(AWS_ACCOUNT_ID));

        // Try invalid values for all input parameters.
        assertNull(trustingAccountDao.getTrustingAccountById("I_DO_NOT_EXIST"));
    }
}
