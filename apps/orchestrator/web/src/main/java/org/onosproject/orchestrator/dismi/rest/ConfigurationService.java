/*
 * Copyright (c) 2018 ACINO Consortium
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

package org.onosproject.orchestrator.dismi.rest;

import io.swagger.annotations.ApiParam;
import org.onosproject.orchestrator.dismi.ConfigurationApiService;
import org.onosproject.orchestrator.dismi.ConfigurationApiServiceFactory;
import org.onosproject.orchestrator.dismi.api.NotFoundException;
import org.onosproject.orchestrator.dismi.primitives.DismiConfiguration;
import org.onosproject.orchestrator.dismi.primitives.Resource;
import org.onosproject.orchestrator.dismi.utils.JsonMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Created by aghafoor on 2017-07-26.
 */
@Path("admin")
public class ConfigurationService {

    private final ConfigurationApiService delegate = ConfigurationApiServiceFactory.getConfigurationApi();

    @GET
    @Path("/conf")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Update specific service.", notes = "Update specific service.",
            response = Resource.class, tags = {"Services"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(
                    code = 202, message = "The request to update the servive has been accepted for processing.",
                    response = Resource.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error", response = Resource.class)})

    public Response getDismiConfigurtion() {
        return delegate.configurationGet();
    }

    @PUT
    @Path("/conf")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Update specific service.", notes = "Update specific service.",
            response = Resource.class, tags = {"Services"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(
                    code = 202, message = "The request to update the servive has been accepted for processing.",
                    response = Resource.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error", response = Resource.class)})

    public Response dismiConfigurationPut(
            @ApiParam(value = "ID of the service to update. FIXME: Why do we need the ID? It is contained" +
                    " in the service definition since it is an existing service!", required = true)
                    String dismiConfigurationData)
            throws NotFoundException {
        DismiConfiguration dismiConfiguration = null;
        try {
            dismiConfiguration = deserializeDismiConfiguration(dismiConfigurationData);
            return delegate.configurationPut(dismiConfiguration);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @PUT
    @Path("/conf/default")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Update specific service.", notes = "Update specific service.",
            response = Resource.class, tags = {"Services"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(
                    code = 202, message = "The request to update the servive has been accepted for processing.",
                    response = Resource.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error", response = Resource.class)})

    public Response dismiConfigurationDefault(
            @ApiParam(value = "ID of the service to update. FIXME: Why do we need the ID? It is contained" +
                    " in the service definition since it is an existing service!", required = true)
            @PathParam("serviceId") String serviceId,
            @ApiParam(value = "New definition of the service.", required = true) String serviceData)
            throws NotFoundException {
        return delegate.configurationDefault();
    }


    private DismiConfiguration deserializeDismiConfiguration(String dismiConfigurationData) throws Exception {
        DismiConfiguration dismiConfiguration = null;
        JsonMapper jsonMapper = new JsonMapper();
        dismiConfiguration = jsonMapper.jsonToDismiConfiguration(dismiConfigurationData);
        return dismiConfiguration;
    }
}
