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

import org.onlab.osgi.DefaultServiceDirectory;
import org.onosproject.orchestrator.dismi.aciIntents.DismiIntentId;
import org.onosproject.orchestrator.dismi.api.ApiResponseMessage;
import org.onosproject.orchestrator.dismi.negotiation.AlternativeSolutionIntentIface;
import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Resource;
import org.onosproject.orchestrator.dismi.primitives.Service;
import org.onosproject.orchestrator.dismi.primitives.extended.IntentList;
import org.onosproject.orchestrator.dismi.store.DismiStoreIface;
import org.onosproject.orchestrator.dismi.utils.JsonMapper;
import org.onosproject.orchestrator.dismi.validation.DismiValidationServiceIface;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.onosproject.orchestrator.dismi.primitives.DismiIntentState.PROCESSING;
import static org.slf4j.LoggerFactory.getLogger;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class ServiceApiServiceImpl extends AbstractWebResource implements ServiceApiService {
    private final Logger log = getLogger(getClass());

    /**
     * serviceGet returns a list of all known services
     *
     * @throws InternalServerErrorException if DismiStoreIface can't be reached
     * @return: a ServiceList object if found, a ApiResponseMessage object in case of trouble (http 404)
     */
    @Override
    public Response serviceGet() throws InternalServerErrorException {
        //log.info("Request to GET service received at REST serviceGet() endpoint !");
        DismiStoreIface store = get(DismiStoreIface.class);
        if (null == store) {
            throw (new InternalServerErrorException("Can't reach the store service"));
        }

        //Get a list of services
        List<Service> list = store.getServicesAsList();
        //log.info("Request to GET service 'serviceGet()' processed !");
        return returnJson(list, "", "Failed to retrieve data", "Failed to retrieve services's data");
    }

    /**
     * serviceServiceIdGet returns the details, including status, of a requested service
     *
     * @param serviceId: serviceId of the requested service
     * @throws InternalServerErrorException if DismiStoreIface can't be reached
     * @return: a Service object if found, a ApiResponseMessage object in case of trouble (http 404)
     */
    @Override
    public Response serviceServiceIdGet(String serviceId) throws InternalServerErrorException {
        log.info("Request to GET service received at REST serviceServiceIdGet(serviceId) endpoint !");
        DismiStoreIface store = get(DismiStoreIface.class);
        if (null == store) {
            throw (new InternalServerErrorException("Can't reach the store service"));
        }

        //Get a service against service id
        Service service = store.getOriginalService(serviceId);

        String errorNoService = "Failed to retrieve the original service";
        String errorException = "Failed to retrieve service data";
        String errorNoResponse = "Failed to retrieve service data";
        log.info("Request to GET service 'serviceServiceIdGet(serviceId)' processed !");
        return returnJson(service, errorNoService, errorException, errorNoResponse);
    }

    /**
     * serviceServiceIdIntentIdGet returns the details, including status, of a requested intent
     *
     * @param serviceId: serviceId of the service the intent belongs to
     * @param intentId:  intentId of the requested intent
     * @throws InternalServerErrorException if DismiStoreIface can't be reached
     * @return: an Intent object if found, a ApiResponseMessage object in case of trouble (http 404)
     */
    @Override
    public Response serviceServiceIdIntentIdGet(String serviceId, String intentId)
            throws InternalServerErrorException {
        log.info("Request to GET Service-Intent received at REST serviceServiceIdIntentIdGet(serviceId,intentId) endpoint !");
        DismiStoreIface store = get(DismiStoreIface.class);
        if (null == store) {
            throw (new InternalServerErrorException("Can't reach the store service"));
        }

        //  Retrieve the requested intent
        Intent intent = store.getOriginalIntent(serviceId, intentId);

        String errorNoIntent = "Failed to retrieve the intent";
        String errorException = "Failed to retrieve intent data";
        String errorNoResponse = "Failed to retrieve intent data";
        log.info("Request to GET Service-Intent 'serviceServiceIdIntentIdGet(serviceId,intentId)' processed !");
        return returnJson(intent, errorNoIntent, errorException, errorNoResponse);
    }

    /**
     * servicePost creates a new service, returning a Resouce object that contains the serviceId
     *
     * @param service: description of the Service to create
     * @throws InternalServerErrorException if DismiValidationServiceIface can't be reached
     * @return: a Resource object in case of successful request submission,
     * a ApiResponseMessage object in case of trouble (http 404)
     */
    //public static int totoalGenericIntents = 0;
    @Override
    public Response servicePost(Service service)
            throws InternalServerErrorException {
        //log.info("Request to POST Service received at REST servicePost(service) endpoint !");
        String path = System.getProperty("user.home");//home/aghafoor/Desktop/Development/acino/PerformanceTests/;
        //  totoalGenericIntents = 0;
        //DismiPerformanceTest dismiPerformanceTest = new DismiPerformanceTest(path );
        //log.info(dismiPerformanceTest.filePath());
        //dismiPerformanceTest.write("Method:INTENT_SIZE:START:Time(ms)");
        //dismiPerformanceTest.write("POST:INTENT_SIZE-"+service.getIntents().size()+":START:");

        DismiValidationServiceIface dismiValidationServiceIface = get(DismiValidationServiceIface.class);
        if (null == dismiValidationServiceIface) {
            throw (new InternalServerErrorException("Can't reach the Dismi Validation service"));
        }
        Resource resource = dismiValidationServiceIface.submitNewService(service);
        String errorFailure = "Failed to register service";
        String errorException = "Failed to serialize resource";
        String errorNoResponse = "Failed to receive a Response to the request";
        //log.info("Request to POST Service 'servicePost(service)' processed !");
        //dismiPerformanceTest.write("POST:INTENT_SIZE-"+service.getIntents().size()+":RESPONSE:");
        return returnJson(resource, errorFailure, errorException, errorNoResponse);
    }

    /**
     * serviceServiceIdPut updates a service and returns a resource
     *
     * @param serviceId: serviceId of the original service
     * @param service:   description of the new service
     * @throws InternalServerErrorException if DismiValidationServiceIface can't be reached
     * @return: a Resource object in case of successful request submission,
     * a ApiResponseMessage object in case of trouble (http 404)
     */
    @Override
    public Response serviceServiceIdPut(String serviceId, Service service)
            throws InternalServerErrorException {
        log.info("Request to PUT Service received at REST serviceServiceIdPut(serviceId, service) endpoint !");
        // Converting simple intent's id to fullyqualified intetn's id
        List<Intent> intentList = service.getIntents();
        List<Intent> updatedList = new ArrayList<Intent>();
        for (Intent intent : intentList) {
            intent.setIntentId(toFullyQualifiedIntentId(serviceId, intent.getIntentId()));
            intent.setIntentStatus(PROCESSING);
            updatedList.add(intent);
        }
        service.setIntents(updatedList);
        service.setServiceStatus(PROCESSING);
        DismiValidationServiceIface dismiValidationServiceIface = get(DismiValidationServiceIface.class);
        if (null == dismiValidationServiceIface) {
            throw (new InternalServerErrorException("Can't reach the Dismi validation service"));
        }
        Resource resource = dismiValidationServiceIface.submitServiceUpdate(serviceId, service);

        String errorFailure = "Failed to receive a resource";
        String errorException = "Failed to serialize resource";
        String errorNoResponse = "Failed to receive a Response to the request";
        log.info("Request to PUT Service 'serviceServiceIdPut(serviceId, service)' processed!");
        return returnJson(resource, errorFailure, errorException, errorNoResponse);
    }

    /**
     * serviceServiceIdIntentIdPut updates an Intent and returns a resource
     *
     * @param serviceId: serviceId of the original service
     * @param intentId:  intentId of the original intent
     * @param intent:    description of the new intent
     * @throws InternalServerErrorException if DismiValidationServiceIface can't be reached
     * @return: a Resource object
     */
    @Override
    public Response serviceServiceIdIntentIdPut(String serviceId, String intentId, Intent intent)
            throws InternalServerErrorException {
        log.info("Request to PUT Service-Intent received at REST serviceServiceIdIntentIdPut(serviceId, intentId, " +
                         "intent) endpoint !");
        DismiValidationServiceIface dismiValidationServiceIface = get(DismiValidationServiceIface.class);
        if (null == dismiValidationServiceIface) {
            throw (new InternalServerErrorException("Can't reach the Dismi validation service"));
        }

        Resource resource = dismiValidationServiceIface.submitIntentUpdate(serviceId, intentId, intent);

        String errorFailure = "Failed to receive a resource";
        String errorException = "Failed to serialize resource";
        String errorNoResponse = "Failed to receive a Response to the request";
        log.info("Request to PUT Service 'serviceServiceIdIntentIdPut(serviceId, intentId, intent)' processed!");
        return returnJson(resource, errorFailure, errorException, errorNoResponse);
    }

    /**
     * serviceServiceIdDelete deletes a service
     *
     * @param serviceId: serviceId of the service to delete
     * @throws InternalServerErrorException if DismiValidationServiceIface can't be reached
     * @return: OK or an ApiResponseMessage object in case of trouble (http 404)
     */
    @Override
    public Response serviceServiceIdDelete(String serviceId)
            throws NotFoundException {
        //log.info("Request to DELETE Service received at REST serviceServiceIdDelete(serviceId) endpoint !");
        String path = System.getProperty("user.home");//home/aghafoor/Desktop/Development/acino/PerformanceTests/;
        //DismiPerformanceTest dismiPerformanceTest = new DismiPerformanceTest(path );
        //dismiPerformanceTest.write("DELETE:INTENT_SIZE-DONTNEED:START:");

        DismiValidationServiceIface dismiValidationServiceIface = get(DismiValidationServiceIface.class);
        if (null == dismiValidationServiceIface) {
            throw (new InternalServerErrorException("Can't reach the Dismi validation service"));
        }
        String excuseMessage = "Error processing the service deletion";
        log.info("Request to DELETE Service 'serviceServiceIdDelete(serviceId)' processed !");
        return returnBoolean(dismiValidationServiceIface.deleteService(serviceId), excuseMessage);
    }

    /**
     * serviceServiceIdDelete deletes a service
     *
     * @param serviceId: serviceId of the service that the intent belongs to
     * @param intentId:  intentId of the intent to delete
     * @throws InternalServerErrorException if DismiValidationServiceIface can't be reached
     * @return: OK or an ApiResponseMessage object in case of trouble (http 404)
     */
    @Override
    public Response serviceServiceIdIntentIdDelete(String serviceId, String intentId)
            throws InternalServerErrorException {
        log.info("Request to DELETE Service-Intent received at REST serviceServiceIdIntentIdDelete(serviceId, " +
                         "intentId) endpoint !");
        DismiValidationServiceIface dismiValidationServiceIface = get(DismiValidationServiceIface.class);
        if (null == dismiValidationServiceIface) {
            throw (new InternalServerErrorException("Can't reach the Dismi validation service"));
        }

        String excuseMessage = "Error processing the intent deletion";
        log.info("Request to DELETE Service-Intent 'serviceServiceIdIntentIdDelete(serviceId, intentId)' processed " +
                         "serviceServiceIdIntentIdDelete(serviceId, intentId) !");
        return returnBoolean(dismiValidationServiceIface.deleteIntent(serviceId, intentId), excuseMessage);
    }

    /*
        Function to return a Response that serializes a primitive
     */
    private Response returnJson(Object object, String objectNull, String excuseCatch, String excuseNoResponse) {
        String response = null;

        if (null == object) {
            log.error("Requested object not found !");
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.OBJECT_NOT_FOUND,
                                                                        objectNull).toJson()).build();
        }

        try {
            JsonMapper jsonMapper = new JsonMapper();
            response = jsonMapper.toJson(object);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Problems when processing. " + e.getMessage());
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.PROCESSING_ERROR,
                                                                        excuseCatch).toJson()).build();
        }

        if (null == response) {
            log.error("*Problems when processing !");
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.PROCESSING_ERROR,
                                                                        excuseNoResponse).toJson()).build();
        }
        return Response.ok(response).build();
    }

    /*
        Function to return either OK or serverError
     */
    private Response returnBoolean(boolean b, String excuseMessage) {
        if (b) {
            return Response.ok().build();
        } else {
            log.error("*Requested object not found !");
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.OBJECT_NOT_FOUND,
                                                                        excuseMessage).toJson()).build();
        }
    }

    /**
     * @param serviceId Provide service id to construct fullyqualified id of Itents
     * @param intentId  itent id
     * @return String     fullyqualified id of intents
     */
    private String toFullyQualifiedIntentId(String serviceId, String intentId) {
        return serviceId + "-" + intentId;
    }

    public Response getAlternativeSolitions(DismiIntentId intentId) throws NotFoundException {
        AlternativeSolutionIntentIface alternativeSolutionIntentIface = DefaultServiceDirectory.getService
                (AlternativeSolutionIntentIface.class);
        Set<Intent> asIntents = alternativeSolutionIntentIface.get(intentId);
        IntentList intents = new IntentList();
        Iterator<Intent> iti = asIntents.iterator();
        while (iti.hasNext()) {
            intents.add(iti.next());
        }
        return returnJson(intents, "", "Failed to retrieve data", "Failed to retrieve services's data");
    }
}
