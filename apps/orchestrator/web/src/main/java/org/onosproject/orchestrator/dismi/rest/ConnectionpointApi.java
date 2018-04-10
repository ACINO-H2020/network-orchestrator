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
import org.onosproject.orchestrator.dismi.ConnectionpointApiService;
import org.onosproject.orchestrator.dismi.ConnectionpointApiServiceFactory;
import org.onosproject.orchestrator.dismi.api.ApiResponseMessage;
import org.onosproject.orchestrator.dismi.api.NotFoundException;
import org.onosproject.orchestrator.dismi.primitives.ConnectionPoint;
import org.onosproject.orchestrator.dismi.primitives.extended.ConnectionPointExtended;
import org.onosproject.orchestrator.dismi.utils.JsonMapper;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("connectionpoint")

@Produces({"application/json"})
@Consumes({"application/json"})
@io.swagger.annotations.Api(description = "the connectionpoint API")
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class ConnectionpointApi extends AbstractWebResource {

    private final ConnectionpointApiService delegate = ConnectionpointApiServiceFactory.getConnectionpointApi();

    @GET
    @Produces({"application/json"})
    @Consumes({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Get the list of the defined ConnectionPoints.",
            notes = "Get the list of the defined ConnectionPoints.",
            response = ConnectionPoint.class,
            responseContainer = "List",
            tags = {"ConnectionPoints",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200,
                    message = "A list of valid connection points is returned.",
                    response = ConnectionPoint.class,
                    responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(code = 400,
                    message = "Unexpected error",
                    response = ConnectionPoint.class,
                    responseContainer = "List")})

    public Response connectionpointGet()
            throws NotFoundException {
        return delegate.connectionpointGet();
    }

    @GET
    @Path("{name}")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Get the overview of a ConnectionPoint by specifying its name.",
            notes = "Get the overview of a ConnectionPoint by specifying its name.",
            response = ConnectionPoint.class,
            tags = {"ConnectionPoints",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200,
                    message = "The specification of the ConnectionPoint.",
                    response = ConnectionPoint.class),

            @io.swagger.annotations.ApiResponse(code = 400,
                    message = "Unexpected error",
                    response = ConnectionPoint.class)})

    public Response connectionpointNameGet(
            @ApiParam(value = "Name of the ConnectionPoint.", required = true)
            @PathParam("name") String name)
            throws NotFoundException {
        return delegate.connectionpointNameGet(name);
    }

    @PUT
    @Path("{name}")

    @Produces({"application/json"})
    @Consumes({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Update a ConnectionPoint by specifying its name.",
            notes = "Update a ConnectionPoint by specifying its name.")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200,
                    message = "The specification of the ConnectionPoint.",
                    response = ConnectionPoint.class),

            @io.swagger.annotations.ApiResponse(code = 400,
                    message = "Unexpected error",
                    response = ConnectionPoint.class)})

    public Response connectionpointNamePut(
            @ApiParam(value = "Name of the ConnectionPoint.", required = true)
            @PathParam("name") String name,
            @ApiParam(value = "Updated definition of the ConnectionPoint",
                    required = true)
                    String point)
            throws NotFoundException {
        ConnectionPointExtended cpe = null;

        try {
            cpe = deserializeConnectionPointExtended(point);
            return delegate.connectionpointNamePut(name, cpe);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return Response.status(Response.Status.BAD_REQUEST).build();
        ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.ErrorCode.BODY_MALFORMED, point);
        String responseMessage = null;
        try {
            JsonMapper jsonMapper = new JsonMapper();
            responseMessage = jsonMapper.toJson(response);
            return Response.serverError().entity(responseMessage).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
        /*
        We could also respond by using an object created by Swagger, see below. Something to look into?
        return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR,"magic!")).build();
        */
    }

    @PUT
    @Path("{currentName}/{newName}")

    @Produces({"application/json"})
    @Consumes({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Update the name of a ConnectionPoint.",
            notes = "Update the name of a ConnectionPoint.")

    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200,
                    message = "The specification of the ConnectionPoint.",
                    response = ConnectionPoint.class),

            @io.swagger.annotations.ApiResponse(code = 400,
                    message = "Unexpected error",
                    response = ConnectionPoint.class)})

    public Response connectionpointNameNamePut(
            @ApiParam(value = "Name of the ConnectionPoint.", required = true)
            @PathParam("currentName") String currentName,
            @PathParam("newName") String newName)
            throws NotFoundException {

        return delegate.connectionpointNamePut(currentName, newName);
    }

    @POST
    @Produces({"application/json"})
    @Consumes({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Create a new ConnectionPoint by specifying its content in the body.",
            notes = "Create a new ConnectionPoint by specifying its content in the body.")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200,
                    message = "The specification of the ConnectionPointExtended.",
                    response = ConnectionPointExtended.class)}) //,
/*
            @io.swagger.annotations.ApiResponse(code = 400,
                    message = "Unexpected error",
                    response = ConnectionPoint.class) })
*/
    public Response connectionpointNamePost(
            @ApiParam(
                    value = "New ConnectionPointExtended to be created.",
                    required = true)
                    String point)
            throws NotFoundException {
        ConnectionPointExtended cpe = null;

        try {
            cpe = deserializeConnectionPointExtended(point);
            return delegate.connectionpointPost(cpe);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @DELETE
    @Path("{name}")

    @Produces({"application/json"})
    @Consumes({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Delete an existing ConnectionPoint by specifying its name.",
            notes = "Delete an existing ConnectionPoint by specifying its name.")

    /*
    @io.swagger.annotations.ApiResponses(
            value = { @io.swagger.annotations.ApiResponse(
                    code = 200,
                    message = "The specification of the ConnectionPoint.",
                    response = ConnectionPoint.class),

                    @io.swagger.annotations.ApiResponse(
                    code = 400,
                    message = "Unexpected error",
                    response = ConnectionPoint.class) }
    )
    */

    public Response connectionpointNameDelete(
            @ApiParam(value = "Name of the ConnectionPoint.", required = true)
            @PathParam("name") String name)
            throws NotFoundException {
        return delegate.connectionpointNameDelete(name);
    }

    private ConnectionPoint deserializeConnectionPoint(String data) throws Exception {
        ConnectionPoint cp = null;
        JsonMapper jsonMapper = new JsonMapper();
        cp = jsonMapper.jsonToConnectionPoint(data);
        return cp;
    }

    private ConnectionPointExtended deserializeConnectionPointExtended(String data) throws Exception {
        ConnectionPointExtended cpe = null;
        JsonMapper jsonMapper = new JsonMapper();
        cpe = jsonMapper.jsonToConnectionPointExtended(data);
        return cpe;
    }
}
