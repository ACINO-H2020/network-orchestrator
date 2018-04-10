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

package org.onosproject.orchestrator.dismi;

import org.onosproject.orchestrator.dismi.api.ApiResponseMessage;
import org.onosproject.orchestrator.dismi.api.NotFoundException;
import org.onosproject.orchestrator.dismi.primitives.ConnectionPoint;
import org.onosproject.orchestrator.dismi.primitives.EndPoint;
import org.onosproject.orchestrator.dismi.primitives.extended.ConnectionPointExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.ConnectionPointList;
import org.onosproject.orchestrator.dismi.primitives.extended.EndPointList;
import org.onosproject.orchestrator.dismi.store.DismiStoreIface;
import org.onosproject.orchestrator.dismi.utils.JsonMapper;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;

import javax.ws.rs.core.Response;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class ConnectionpointApiServiceImpl extends AbstractWebResource implements ConnectionpointApiService {
    private final Logger log = getLogger(getClass());

    /*
     * Stephane, 2016-03-18.
     * This function returns the list of all connectionPoints.
     */
    @Override
    public Response connectionpointGet()
            throws NotFoundException {
        log.info("Request to GET connection point received at REST connectionpointGet endpoint !");
        DismiStoreIface store = get(DismiStoreIface.class);
        ConnectionPointList list = store.getConnectionPointsAsList();
        String response = null;

        try {
            JsonMapper jsonMapper = new JsonMapper();
            response = jsonMapper.toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Problems when processing request to connectionpointGet(), Failed to " +
                              "retrieve data");
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.PROCESSING_ERROR,
                                                                        "Failed to retrieve data").toJson()).build();
        }
        log.info("Request to GET connection point processed !");
        return Response.ok(response).build();
    }

    /*
     * Stephane, 2016-03-18.
     * This function returns a connectionPoint requested by name.
     */
    @Override
    public Response connectionpointNameGet(String name)
            throws NotFoundException {
        log.info("Request to GET connection point received at REST connectionpointGet(" + name + ") endpoint !");
        DismiStoreIface store = get(DismiStoreIface.class);
        ConnectionPoint cp = new ConnectionPoint();
        cp.setName(name);
        EndPointList list = store.getEndPointsAsList(cp);

        String response = null;

        /*
            If the ConnectionPoint does not exist, the store returns a valid but empty ConnectionPointList
            that we send back to the client.
            A failure to serialize the object causes an INTERNAL_SERVER_ERROR response.
         */
        try {
            JsonMapper jsonMapper = new JsonMapper();
            response = jsonMapper.toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Problems when processing request to connectionpointNameGet(String name), " +
                              "Failed to retrieve data");
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.PROCESSING_ERROR,
                                                                        "Failed to retrieve data").toJson()).build();
        }
        log.info("Request to GET connection point (" + name + ") processed !");
        return Response.ok(response).build();
    }

    /*
     * Stephane, 2016-03-18.
     * This function updates the name of a ConnectionPoint.
     */
    @Override
    public Response connectionpointNamePut(String currentName, String newName) throws NotFoundException {
        log.info("Request to PUT connection point received at REST connectionpointNamePut" +
                         "(currentName:" + currentName + ", newName:" + newName + ") endpoint !");
        DismiStoreIface store = get(DismiStoreIface.class);

        // Is the current name valid??
        if ((null == currentName) || (currentName.length() == 0)) {
            log.error("Problems when processing request to connectionpointNamePut(currentName,newName), " +
                              "Current name is missing !");
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.EMPTY_PARAMETER,
                                                                        "Current name null or empty").toJson()).build();
        }

        // Is the new name valid??
        if ((null == newName) || (newName.length() == 0)) {
            log.error("Problems when processing request to connectionpointNamePut(currentName,newName), " +
                              "New name is missing !");
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.EMPTY_PARAMETER,
                                                                        "New name null or empty").toJson()).build();
        }
        ConnectionPoint cp1 = new ConnectionPoint();
        ConnectionPoint cp2 = new ConnectionPoint();
        cp1.setName(currentName);
        cp2.setName(newName);
        if (!store.updateConnectionPoint(cp1, cp2)) {
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.PROCESSING_ERROR,
                                                                        "Failed to update ConnectionPoint " + currentName).toJson()).build();
        }
        log.info("Request to PUT connectionpointNamePut(currentName:" + currentName + ", newName:" + newName + ") " +
                         "processed !");
        return Response.ok().build();
    }

    /*
     * Stephane, 2016-03-18.
     * This function updates a connectionPoint.
     */
    @Override
    public Response connectionpointNamePut(String currentName, ConnectionPointExtended pointExtended)
            throws NotFoundException {
        log.info("Request to PUT connection point received at REST connectionpointNamePut" +
                         "(currentName:" + currentName + ", newName inc endpoints:" + pointExtended.getName() + ") endpoint !");
        DismiStoreIface store = get(DismiStoreIface.class);

        String newName = pointExtended.getName();
        Set<EndPoint> set = pointExtended.getEndpoints();

        // Is the current name valid??
        if ((null == currentName) || (currentName.length() == 0)) {
            log.error("Problems when processing request to connectionpointNamePut(currentName, pointExtended), " +
                              "Current name is missing !");
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.EMPTY_PARAMETER,
                                                                        "Current name null or empty").toJson()).build();
        }

        // Is this a valid ConnectionPointExtended?
        if ((null == newName) || (newName.length() == 0) || (null == set) || (set.size() == 0)) {
            log.error("Problems when processing request to connectionpointNamePut(currentName,pointExtended), " +
                              "New name is missing !");
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.EMPTY_PARAMETER,
                                                                        "New name is null or empty").toJson()).build();
        }

        // The store returns false if the ConnectionPoint already exist

        ConnectionPoint currentCP = new ConnectionPoint();
        ConnectionPoint newCP = new ConnectionPoint();
        currentCP.setName(currentName);
        newCP.setName(pointExtended.getName());

        if (!store.updateConnectionPoint(currentCP, newCP, pointExtended.getEndpoints())) {
            log.error("Problems when processing request to connectionpointNamePut(currentName,pointExtended), " +
                              "Failed to update ConnectionPoint !");
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.PROCESSING_ERROR,
                                                                        "Failed to update ConnectionPoint " + currentName).toJson()).build();
        }
        log.info("Request to PUT connectionpointNamePut(currentName:" + currentName + ", newName inc " +
                         "endpoints:" + newName + ") processed !");
        return Response.ok().build();
    }

    /*
        Create a new ConnectionPoint.
     */
    @Override
    public Response connectionpointPost(ConnectionPointExtended pointExtended) throws NotFoundException {
        log.info("Request to POST connection point '" + pointExtended.getName() + "'received at REST connectionpointPost" +
                         "(connectionpointPost) endpoint!");
        DismiStoreIface store = get(DismiStoreIface.class);
        ConnectionPoint connectionPoint = null;
        String name = pointExtended.getName();
        Set<EndPoint> set = pointExtended.getEndpoints();

        // Is this a valid ConnectionPointExtended?
        if ((null == name) || (null == set) || (set.size() == 0)) {
            log.error("Problems when processing request to connectionpointPost(pointExtended), " +
                              "Name is missing !");
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.EMPTY_PARAMETER,
                                                                        "Name is null or empty").toJson()).build();
        }

        // Does the ConnectionPoint already exist?
        connectionPoint = new ConnectionPoint();
        connectionPoint.setName(name);
        if (store.connectionPointExists(connectionPoint)) {
            log.error("Problems when processing request to connectionpointPost(pointExtended), " +
                              "Connection point already exists !");
            return Response.serverError().entity(
                    new ApiResponseMessage(ApiResponseMessage.ErrorCode.OBJECT_ALREADY_EXISTS,
                                           "ConnectionPoint " + pointExtended.getName()).toJson()).build();
        }

        // Let's add the ConnectionPoint and its Endpoints to the store
        for (EndPoint e : set) {
            store.addEndPoint(connectionPoint, e);
        }
        log.info("Request to POST connection point '" + pointExtended.getName() + "'processed !");
        return Response.ok().build();
    }

    /*
        Delete a ConnectionPoint.
     */
    @Override
    public Response connectionpointNameDelete(String name) throws NotFoundException {
        log.info("Request to DELETE connection point '" + name + "'received at REST connectionpointNameDelete(name) endpoint!");
        DismiStoreIface store = get(DismiStoreIface.class);

        if (null == name) { // Not possible?
            log.error("Problems when processing request to connectionpointNameDelete(name), " +
                              "Name is missing !");
            return Response.serverError().entity(
                    new ApiResponseMessage(ApiResponseMessage.ErrorCode.EMPTY_PARAMETER,
                                           "Name is null or empty").toJson()).build();
        }

        ConnectionPoint connectionPoint = new ConnectionPoint();
        connectionPoint.setName(name);
        if (store.deleteConnectionPoint(connectionPoint)) {
            log.info("Request to DELETE connection point '" + name + "' processed !");
            return Response.ok().build();
        } else {
            log.error("Problems when processing request to connectionpointNameDelete(name), " +
                              "Failed to delete ConnectionPoint !");
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.PROCESSING_ERROR,
                                                                        "Failed to delete ConnectionPoint " + name).toJson()).build();
        }
    }
}
