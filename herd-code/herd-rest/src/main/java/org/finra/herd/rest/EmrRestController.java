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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.finra.herd.model.api.xml.EmrCluster;
import org.finra.herd.model.api.xml.EmrClusterCreateRequest;
import org.finra.herd.model.api.xml.EmrHadoopJarStep;
import org.finra.herd.model.api.xml.EmrHadoopJarStepAddRequest;
import org.finra.herd.model.api.xml.EmrHiveStep;
import org.finra.herd.model.api.xml.EmrHiveStepAddRequest;
import org.finra.herd.model.api.xml.EmrMasterSecurityGroup;
import org.finra.herd.model.api.xml.EmrMasterSecurityGroupAddRequest;
import org.finra.herd.model.api.xml.EmrPigStep;
import org.finra.herd.model.api.xml.EmrPigStepAddRequest;
import org.finra.herd.model.api.xml.EmrShellStep;
import org.finra.herd.model.api.xml.EmrShellStepAddRequest;
import org.finra.herd.model.dto.EmrClusterAlternateKeyDto;
import org.finra.herd.model.dto.SecurityFunctions;
import org.finra.herd.service.EmrService;
import org.finra.herd.ui.constants.UiConstants;

/**
 * The REST controller that handles EMR REST requests.
 */
@RestController
@RequestMapping(value = UiConstants.REST_URL_BASE, produces = {"application/xml", "application/json"})
@Api(tags = "EMR")
public class EmrRestController extends HerdBaseController
{
    @Autowired
    private EmrService emrService;

    /**
     * Adds security groups to the master node of an existing cluster <p>Requires WRITE permission on namespace</p>
     *
     * @param request the information needed to add security groups to master node of the EMR cluster.
     *
     * @return the created EMR master groups.
     */
    @RequestMapping(value = "/emrMasterSecurityGroups", method = RequestMethod.POST, consumes = {"application/xml", "application/json"})
    @Secured(SecurityFunctions.FN_EMR_MASTER_SECURITY_GROUPS_POST)
    public EmrMasterSecurityGroup addGroupsToEmrClusterMaster(@RequestBody EmrMasterSecurityGroupAddRequest request) throws Exception
    {
        return emrService.addSecurityGroupsToClusterMaster(request);
    }

    /**
     * Adds a Hadoop Jar step to the existing cluster <p>Requires EXECUTE permission on namespace</p>
     *
     * @param request the information needed to add Hadoop Jar step to the EMR cluster.
     *
     * @return the created EMR Hadoop Jar step.
     */
    @RequestMapping(value = "/emrHadoopJarSteps", method = RequestMethod.POST, consumes = {"application/xml", "application/json"})
    @Secured(SecurityFunctions.FN_EMR_HADOOP_JAR_STEPS_POST)
    public EmrHadoopJarStep addHadoopJarStepToEmrCluster(@RequestBody EmrHadoopJarStepAddRequest request) throws Exception
    {
        return (EmrHadoopJarStep) emrService.addStepToCluster(request);
    }

    /**
     * Adds a hive step to the existing cluster <p>Requires EXECUTE permission on namespace</p>
     *
     * @param request the information needed to add hive step to the EMR cluster.
     *
     * @return the created EMR hive step.
     */
    @RequestMapping(value = "/emrHiveSteps", method = RequestMethod.POST, consumes = {"application/xml", "application/json"})
    @Secured(SecurityFunctions.FN_EMR_HIVE_STEPS_POST)
    public EmrHiveStep addHiveStepToEmrCluster(@RequestBody EmrHiveStepAddRequest request) throws Exception
    {
        return (EmrHiveStep) emrService.addStepToCluster(request);
    }

    /**
     * Adds a Pig step to the existing cluster <p>Requires EXECUTE permission on namespace</p>
     *
     * @param request the information needed to add Pig step to the EMR cluster.
     *
     * @return the created EMR Pig step.
     */
    @RequestMapping(value = "/emrPigSteps", method = RequestMethod.POST, consumes = {"application/xml", "application/json"})
    @Secured(SecurityFunctions.FN_EMR_PIG_STEPS_POST)
    public EmrPigStep addPigStepToEmrCluster(@RequestBody EmrPigStepAddRequest request) throws Exception
    {
        return (EmrPigStep) emrService.addStepToCluster(request);
    }

    /**
     * Adds a shell step to the existing cluster <p>Requires EXECUTE permission on namespace</p>
     *
     * @param request the information needed to add shell step to the EMR cluster.
     *
     * @return the created EMR shell step.
     * @throws Exception if a shell step couldn't be added to the EMR cluster.
     */
    @RequestMapping(value = "/emrShellSteps", method = RequestMethod.POST, consumes = {"application/xml", "application/json"})
    @Secured(SecurityFunctions.FN_EMR_SHELL_STEPS_POST)
    public EmrShellStep addShellStepToEmrCluster(@RequestBody EmrShellStepAddRequest request) throws Exception
    {
        return (EmrShellStep) emrService.addStepToCluster(request);
    }

    /**
     * Creates a new EMR cluster. <p>Requires EXECUTE permission on namespace</p>
     *
     * @param request the information needed to create the EMR cluster.
     *
     * @return the created EMR cluster.
     */
    @RequestMapping(value = "/emrClusters", method = RequestMethod.POST, consumes = {"application/xml", "application/json"})
    @Secured(SecurityFunctions.FN_EMR_CLUSTERS_POST)
    public EmrCluster createEmrCluster(@RequestBody EmrClusterCreateRequest request) throws Exception
    {
        return emrService.createCluster(request);
    }

    /**
     * Gets an existing EMR cluster details. <p>Requires READ permission on namespace</p>
     *
     * @param namespace the namespace
     * @param emrClusterDefinitionName the EMR cluster definition name
     * @param emrClusterName the EMR cluster name
     * @param emrClusterId the cluster id of the cluster to get details
     * @param emrStepId the step id of the step to get details
     * @param verbose parameter for whether to return detailed information
     * @param accountId the account Id
     * @param retrieveInstanceFleets parameter for whether to retrieve instance fleets
     *
     * @return the EMR Cluster object with details.
     * @throws Exception if there was an error getting the EMR cluster.
     */
    @RequestMapping(value = "/emrClusters/namespaces/{namespace}/emrClusterDefinitionNames/{emrClusterDefinitionName}/emrClusterNames/{emrClusterName}",
        method = RequestMethod.GET)
    @Secured(SecurityFunctions.FN_EMR_CLUSTERS_GET)
    public EmrCluster getEmrCluster(@PathVariable("namespace") String namespace, @PathVariable("emrClusterDefinitionName") String emrClusterDefinitionName,
        @PathVariable("emrClusterName") String emrClusterName, @RequestParam(value = "emrClusterId", required = false) String emrClusterId,
        @RequestParam(value = "emrStepId", required = false) String emrStepId,
        @RequestParam(value = "verbose", required = false, defaultValue = "false") Boolean verbose,
        @RequestParam(value = "accountId", required = false) String accountId,
        @RequestParam(value = "retrieveInstanceFleets", required = false, defaultValue = "false") Boolean retrieveInstanceFleets) throws Exception
    {
        EmrClusterAlternateKeyDto alternateKey =
            EmrClusterAlternateKeyDto.builder().withNamespace(namespace).withEmrClusterDefinitionName(emrClusterDefinitionName)
                .withEmrClusterName(emrClusterName).build();

        return emrService.getCluster(alternateKey, emrClusterId, emrStepId, verbose, accountId, retrieveInstanceFleets);
    }

    /**
     * Terminates an existing EMR cluster. <p>Requires EXECUTE permission on namespace</p>
     *
     * @param namespace the namespace
     * @param emrClusterDefinitionName the EMR cluster definition name
     * @param emrClusterName the EMR cluster name
     * @param overrideTerminationProtection parameter for whether to override termination protection
     * @param emrClusterId EMR cluster ID
     * @param accountId account Id
     *
     * @return the EMR cluster that was terminated
     * @throws Exception if there was an error terminating the EMR cluster.
     */
    @RequestMapping(value = "/emrClusters/namespaces/{namespace}/emrClusterDefinitionNames/{emrClusterDefinitionName}/emrClusterNames/{emrClusterName}",
        method = RequestMethod.DELETE)
    @Secured(SecurityFunctions.FN_EMR_CLUSTERS_DELETE)
    public EmrCluster terminateEmrCluster(@PathVariable("namespace") String namespace,
        @PathVariable("emrClusterDefinitionName") String emrClusterDefinitionName, @PathVariable("emrClusterName") String emrClusterName,
        @RequestParam(value = "overrideTerminationProtection", required = false, defaultValue = "false") Boolean overrideTerminationProtection,
        @RequestParam(value = "emrClusterId", required = false) String emrClusterId, @RequestParam(value = "accountId", required = false) String accountId)
        throws Exception
    {
        EmrClusterAlternateKeyDto alternateKey =
            EmrClusterAlternateKeyDto.builder().withNamespace(namespace).withEmrClusterDefinitionName(emrClusterDefinitionName)
                .withEmrClusterName(emrClusterName).build();

        return emrService.terminateCluster(alternateKey, overrideTerminationProtection, emrClusterId, accountId);
    }
}
