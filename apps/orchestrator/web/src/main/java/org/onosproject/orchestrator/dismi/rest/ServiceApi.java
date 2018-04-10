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
import org.onosproject.orchestrator.dismi.ServiceApiService;
import org.onosproject.orchestrator.dismi.ServiceApiServiceFactory;
import org.onosproject.orchestrator.dismi.aciIntents.DismiIntentId;
import org.onosproject.orchestrator.dismi.api.NotFoundException;
import org.onosproject.orchestrator.dismi.negotiation.NegoUtils;
import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Resource;
import org.onosproject.orchestrator.dismi.primitives.Service;
import org.onosproject.orchestrator.dismi.utils.JsonMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("service")

@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the service API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class ServiceApi {
    private final ServiceApiService delegate = ServiceApiServiceFactory.getServiceApi();

    @GET
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the list of services currently run by the customer",
            notes = "ACINO provides a list of services that the customer has started or scheduled.",
            response = Service.class,
            responseContainer = "List",
            tags = {"Services",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "A list of current Services is returned.",
                    response = Service.class, responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error",
                    response = Service.class, responseContainer = "List")})

    public Response serviceGet()
            throws NotFoundException {

        return delegate.serviceGet();
    }

    @GET
    @Path("/{serviceId}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the overview of a specific service by specifying its service ID.",
            notes = "Get the overview of a spefific service by specifying its service ID.",
            response = Service.class, tags = {"Services",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "The specification of the Service requested",
                    response = Service.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error", response = Service.class)})

    public Response serviceServiceIdGet(
            @ApiParam(value = "Service Unique ID of the service for which the overview is requested.", required = true)
            @PathParam("serviceId") String serviceId)
            throws NotFoundException {
        return delegate.serviceServiceIdGet(serviceId);
    }

    @GET
    @Path("/{service_id}/{intent_id}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Get the overview of a specific intent in a service by specifying the service and intent IDs.",
            notes = "Get the overview of a specific intent in a service by specifying the service and intent IDs.",
            response = Intent.class, tags = {"Services", "Intents",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(
                    code = 200, message = "The specification of the Intent requested",
                    response = Intent.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error", response = Intent.class)})

    public Response serviceServiceIdIntentIdGet(
            @ApiParam(value = "Service Unique ID of the service containing the Intent of interest.", required = true)
            @PathParam("service_id") String serviceId,
            @ApiParam(value = "Intent Unique ID of the intent for which the overview is requested.", required = true)
            @PathParam("intent_id") String intentId)
            throws NotFoundException {
        return delegate.serviceServiceIdIntentIdGet(serviceId, intentId);
    }

    //
    @GET
    @Path("/iid={intentId}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the list of services currently run by the customer",
            notes = "ACINO provides a list of services that the customer has started or scheduled.",
            response = Intent.class,
            responseContainer = "List",
            tags = {"Intents",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "A list of current Services is returned.",
                    response = Intent.class, responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error",
                    response = Intent.class, responseContainer = "List")})

    public Response alternativeSolitionsGET(@ApiParam(value = "Intent Unique ID of the intent for which the overview is requested.", required = true)
                                            @PathParam("intentId") String intentId)
            throws NotFoundException {
        return delegate.getAlternativeSolitions(DismiIntentId.getId(intentId));
    }


    @POST
    @Produces({"application/json"})
    @Consumes({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Create a new service", notes = "Request the creation or scheduling of a new service",
            response = Resource.class, tags = {"Services",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(
                    code = 202, message = "The request to create a new service has been accepted for processing.",
                    response = Resource.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error", response = Resource.class)})

    public Response servicePost(
            @ApiParam(value = "Definition of the service to be created.",
                    required = true) String serviceData)
            throws NotFoundException {
        Service service = null;
        try {
            service = deserializeService(serviceData);
            return delegate.servicePost(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @PUT
    @Path("/{serviceId}")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Update specific service.", notes = "Update specific service.",
            response = Resource.class, tags = {"Services"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(
                    code = 202, message = "The request to update the servive has been accepted for processing.",
                    response = Resource.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error", response = Resource.class)})

    public Response serviceServiceIdPut(
            @ApiParam(value = "ID of the service to update. FIXME: Why do we need the ID? It is contained" +
                    " in the service definition since it is an existing service!", required = true)
            @PathParam("serviceId") String serviceId,
            @ApiParam(value = "New definition of the service.", required = true) String serviceData)
            throws NotFoundException {
        Service service = null;
        try {
            service = deserializeService(serviceData);
            return delegate.serviceServiceIdPut(serviceId, service);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @PUT
    @Path("/{service_id}/{intent_id}")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Update a specific intent in a service by specifying the service and intent IDs.",
            notes = "Update a specific intent in a service by specifying the service and intent IDs.",
            response = Resource.class, tags = {"Services", "Intents",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(
                    code = 202, message = "The request to update the intent has been accepted for processing.",
                    response = Resource.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error", response = Resource.class)})

    public Response serviceServiceIdIntentIdPut(
            @ApiParam(value = "Service Unique ID of the service containinf the Intent of interest.", required = true)
            @PathParam("service_id") String serviceId,
            @ApiParam(value = "Intent Unique ID of the intent for which the overview is requested.", required = true)
            @PathParam("intent_id") String intentId,
            @ApiParam(value = "Updated definition of the Intent.", required = true) String intentData)
            throws NotFoundException {
        Intent intent = null;
        try {
            intent = deserializeIntent(intentData);
            return delegate.serviceServiceIdIntentIdPut(serviceId, intentId, intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @PUT
    @Path("/{intent_id}={updatedintent_id}")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Update a specific intent in a service by specifying the service and intent IDs.",
            notes = "Update a specific intent in a service by specifying the service and intent IDs.",
            response = Resource.class, tags = {"Services", "Intents",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(
                    code = 202, message = "The request to update the intent has been accepted for processing.",
                    response = Resource.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error", response = Resource.class)})

    public Response serviceIntentIdUpdateintentIdPut(
            @ApiParam(value = "Intent Unique ID of the intent for which the overview is requested.", required = true)
            @PathParam("intent_id") String intentId,
            @ApiParam(value = "Updated Intent ID used to fetch intent from Alternative Solutions.", required = true)
            @PathParam("updatedintent_id") String updatedintent_id)
            throws NotFoundException {
        Intent intent = null;
        try {
            NegoUtils negoUtils = new NegoUtils();
            intent = negoUtils.getASIntentForInstallation(intentId, updatedintent_id);
            String serviceId = negoUtils.extractServiceId(intentId);
            //intent = deserializeIntent(intentData);
            intent.setIntentId(intentId);
            System.out.println("intentId:" + intentId + "\tupdatedintent_id:" + updatedintent_id);
            return delegate.serviceServiceIdIntentIdPut(serviceId, intentId, intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }


    @DELETE
    @Path("/{serviceId}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Delete a specific service by specifying its service ID.",
            notes = "Delete a specific service by specifying its service ID.",
            response = Resource.class, tags = {"Services",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(
                    code = 202, message = "The request to delete the service has been accepted for processing.",
                    response = Resource.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error", response = Resource.class)})

    public Response serviceServiceIdDelete(
            @ApiParam(value = "Service Unique ID of the service for which the deletion is requested.", required = true)
            @PathParam("serviceId") String serviceId)
            throws NotFoundException {
        return delegate.serviceServiceIdDelete(serviceId);
    }

    @DELETE
    @Path("/{service_id}/{intent_id}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Delete a specific intent in a service by specifying the service and intent IDs.",
            notes = "Delete a specific intent in a service by specifying the service and intent IDs.",
            response = void.class, tags = {"Services", "Intents",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(
                    code = 200, message = "Confirming the deletion of the Intent.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Unexpected error", response = void.class)})

    public Response serviceServiceIdIntentIdDelete(
            @ApiParam(value = "Service Unique ID of the service containinf the Intent of interest.", required = true)
            @PathParam("service_id") String serviceId,
            @ApiParam(value = "Intent Unique ID of the intent for which the overview is requested.", required = true)
            @PathParam("intent_id") String intentId)
            throws NotFoundException {
        return delegate.serviceServiceIdIntentIdDelete(serviceId, intentId);
    }

    private Service deserializeService(String serviceData) throws Exception {
        Service service = null;
        JsonMapper jsonMapper = new JsonMapper();
        service = jsonMapper.jsonToService(serviceData);
        return service;
    }

    private Intent deserializeIntent(String intentData) throws Exception {
        Intent intent = null;
        JsonMapper jsonMapper = new JsonMapper();
        intent = jsonMapper.jsonToIntent(intentData);
        return intent;
    }

    //   protected void activate() {
    //   intentService.addListener(new InternalIntentListener());
    //   ipLinksExecutor = Executors.newScheduledThreadPool(CORE_POOL_SIZE);
    //   log.info("Started");
}
