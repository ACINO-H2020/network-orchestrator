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

package org.onosproject.drivers.cop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.onosproject.drivers.cop.model.Call;
import org.onosproject.drivers.cop.model.Call.OperStatusEnum;
import org.onosproject.drivers.cop.model.Endpoint;
import org.onosproject.drivers.cop.model.TransportLayerType;
import org.onosproject.drivers.cop.model.TransportLayerType.DirectionEnum;
import org.onosproject.drivers.cop.model.TransportLayerType.LayerEnum;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleProgrammable;
import org.onosproject.protocol.rest.RestSBController;
import org.onosproject.protocol.restproxy.EndToEndFlow;
import org.onosproject.protocol.restproxy.RestProxyController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.drivers.cop.CopTopologyDiscovery.*;

/**
 * COP implementation of service related behavior.
 */
public class CopServiceHandler extends AbstractHandlerBehaviour
        implements FlowRuleProgrammable {

    private static final Logger LOG = LoggerFactory
            .getLogger(CopServiceHandler.class);

    private static ObjectMapper mapper = new ObjectMapper();
    private static final String CALLS_REQUEST = "/calls";
    private static final String CALL_ID_PREFIX = "/call=";

    /**************************
     * CURRENTLY UNUSED START *
     **************************/

    /**
     * Get the input stream for the calls by querying the controller. This only
     * works if a device identifier is available in the handler data.
     *
     * @return the call input stream for the device
     */
    private InputStream getDeviceCallStream() {
        // get the handler to retrieve the controller and the data
        DriverHandler handler = handler();
        RestSBController controller = checkNotNull(handler
                .get(RestSBController.class));
        DeviceId deviceId = handler.data().deviceId();
        // request the current topology
        Response response = controller.get(deviceId, CALLS_REQUEST, JSON);
        return new ByteArrayInputStream(response.readEntity(String.class).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Get the calls from the input stream.
     *
     * @param input the input stream
     * @return a list of calls
     * @throws IOException calls could not be parsed
     */
    private List<Call> getCalls(InputStream input) throws IOException {
        List<Call> calls = new ArrayList<>();
        JsonNode rootNode;
        rootNode = mapper.readValue(input, JsonNode.class);
        for (JsonNode callNode : rootNode) {
            calls.add(mapper.treeToValue(callNode, Call.class));
        }
        return calls;
    }

    /************************
     * CURRENTLY UNUSED END *
     ************************/

    /**
     * Returns the flow entries based on the controller.
     */
    @Override
    public Collection<FlowEntry> getFlowEntries() {
        DriverHandler handler = handler();
        RestProxyController controller = checkNotNull(handler
                .get(RestProxyController.class));
        return controller.getFlows(handler.data().deviceId());
    }

    @Override
    public Collection<FlowRule> applyFlowRules(Collection<FlowRule> rules) {
        // get the handler to retrieve the controller and the data
        DriverHandler handler = handler();
        RestProxyController controller = checkNotNull(handler
                .get(RestProxyController.class));
        DeviceId deviceId = handler.data().deviceId();
        // retrieve the list of end to end connections that need to be installed
        Collection<EndToEndFlow> end2endFlows = controller.addFlows(rules);
        LOG.info("Received {} end2end flows.", end2endFlows.size());
        Iterator<EndToEndFlow> end2endIter = end2endFlows.iterator();
        // install all tunnels
        while (end2endIter.hasNext()) {
            EndToEndFlow end2end = end2endIter.next();
            // String name = port.annotations().value(AnnotationKeys.PORT_NAME);
            Call call = createCall(end2end.getIdentifier(), end2end.getSource(),
                                   end2end.getDestination(),
                                   end2end.getSrcPort(), end2end.getDstPort());
            LOG.info("Installing call: {}", call);
            boolean ok = false;
//            try {
//                // post the call
//                ok = controller.post(deviceId, CALLS_REQUEST + CALL_ID_PREFIX
//                                        + call.getCallId(),
//                                mapper.writeValueAsString(call),
//                                     MediaType.APPLICATION_JSON);
//            } catch (JsonProcessingException e) {
//                LOG.error("Failed to convert call to json: {}", call);
//            }
            if(!ok) {
                LOG.error("The post request failed!");
                return Collections.emptyList();
            }
        }
        return rules;
    }

    /**
     * Creates a COP call.
     *
     * @param identifier the (unique) identifier for the call
     * @param source the source device identifier
     * @param destination the destination device identifier
     * @param sourcePort the source port number
     * @param destinationPort the destination port number
     * @return the call representation
     */
    private Call createCall(String identifier, DeviceId source,
                            DeviceId destination, PortNumber sourcePort,
                            PortNumber destinationPort) {
        DeviceService deviceService = handler().get(DeviceService.class);
        Port aPort = deviceService.getPort(source, sourcePort);
        Port zPort = deviceService.getPort(destination, destinationPort);
        Endpoint aEnd = new Endpoint(source.uri().toString()
                .replace(DEVICE_URI_PREFIX, ""), "",
                                     aPort.annotations()
                                             .value(AnnotationKeys.PORT_NAME));
        Endpoint zEnd = new Endpoint(destination.uri().toString()
                .replace(DEVICE_URI_PREFIX, ""), "",
                                     zPort.annotations()
                                             .value(AnnotationKeys.PORT_NAME));
        return new Call(OperStatusEnum.UP, identifier, zEnd, ImmutableList.of(),
                        null, aEnd,
                        new TransportLayerType(LayerEnum.DWDM_LINK,
                                               DirectionEnum.BIDIR, "layer"),
                        null);
    }

    @Override
    public Collection<FlowRule> removeFlowRules(Collection<FlowRule> rules) {
        // get the handler to retrieve the controller and the data
        DriverHandler handler = handler();
        RestProxyController controller = checkNotNull(handler
                .get(RestProxyController.class));
        DeviceId deviceId = handler.data().deviceId();
        // update controller and retrieve a list of tunnels to be deleted
        Collection<String> tunnelNames = controller.deleteFlows(rules);
        Iterator<String> tunnelIter = tunnelNames.iterator();
        // create delete calls for all tunnel names
        while (tunnelIter.hasNext()) {
            String callId = tunnelIter.next();
            // don't send requests for empty names
            if (callId != null && !"".equals(callId)) {
                LOG.info("Deleting call: {}", callId);
                // delete the call matching the call identifier
                controller.delete(deviceId, CALLS_REQUEST
                        + CALL_ID_PREFIX + callId, MediaType.APPLICATION_JSON);
            } else {
                // ignore empty entries
                LOG.debug("Received an empty call identifier.");
            }
        }
        return rules;
    }

}
