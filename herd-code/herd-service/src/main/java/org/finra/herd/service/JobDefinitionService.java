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

import org.finra.herd.model.api.xml.JobDefinition;
import org.finra.herd.model.api.xml.JobDefinitionCreateRequest;
import org.finra.herd.model.api.xml.JobDefinitionKeys;
import org.finra.herd.model.api.xml.JobDefinitionUpdateRequest;

/**
 * The job definition service.
 */
public interface JobDefinitionService
{
    public JobDefinition createJobDefinition(JobDefinitionCreateRequest jobDefinitionCreateRequest, boolean enforceAsync) throws Exception;

    public JobDefinition getJobDefinition(String namespace, String jobName) throws Exception;

    public JobDefinition updateJobDefinition(String namespace, String jobName, JobDefinitionUpdateRequest jobDefinitionUpdateRequest, boolean enforceAsync)
        throws Exception;

    /**
     * Gets a list of keys for all job definitions defined in the system for the specified namespace.
     *
     * @param namespace the namespace of the job definition
     *
     * @return the job definition keys
     */
    public JobDefinitionKeys getJobDefinitionKeys(String namespace);
}
