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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.orchestrator.api.ServiceStore;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.slf4j.LoggerFactory.getLogger;

@Path("servicerequest")
public class ServiceRequestWeb extends AbstractWebResource {


    private final Logger log = getLogger(getClass());

    /**
     * Get services. Returns the list of services
     *
     * @return JSON with a list of services
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServices() {

        log.debug("Requesting all services");
        ServiceStore service = get(ServiceStore.class);
        ObjectNode result = new ObjectMapper().createObjectNode();
        //TODO: to be encoded in json
        result.put("services", service.getServices().toString());
        return ok(result.toString()).build();

    }
}
