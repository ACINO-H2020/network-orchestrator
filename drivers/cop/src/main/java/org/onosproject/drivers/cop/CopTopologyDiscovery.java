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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onlab.packet.ChassisId;
import org.onosproject.drivers.cop.model.Edge;
import org.onosproject.drivers.cop.model.Node;
import org.onosproject.drivers.cop.model.Topology;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.ConnectionInfo;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link.Type;
import org.onosproject.net.PortNumber;
import org.onosproject.net.SparseAnnotations;
import org.onosproject.net.behaviour.LinkDiscovery;
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.net.device.DeviceDescriptionDiscovery;
import org.onosproject.net.device.DevicesDiscovery;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkDescription;
import org.onosproject.protocol.restproxy.RestProxyController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * COP implementation of topology related behavior.
 */
public class CopTopologyDiscovery extends AbstractHandlerBehaviour
        implements DeviceDescriptionDiscovery, DevicesDiscovery, LinkDiscovery {

    protected static final String JSON = MediaType.APPLICATION_JSON;
    protected static final String DEVICE_URI_PREFIX = "restproxy:";
    private final static String MANUFACTURER = "ACINO";
    private final static String VERSION = "1.0.0";
    private static final Logger LOG = LoggerFactory
            .getLogger(CopTopologyDiscovery.class);
    private static final String TOPOLOGIES_REQUEST = "/topologies";
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public DeviceDescription discoverDeviceDetails() {
        return null;
    }

    @Override
    public List<PortDescription> discoverPortDetails() {
        // request the current topology
        InputStream topologyStream = getDeviceTopologyStream();
        try {
            List<Topology> topologies = getTopologies(topologyStream);
            DeviceId deviceId = handler().data().deviceId();
            // extract the node ID by removing the prefix
            String nodeId = deviceId.uri().toString().replace(DEVICE_URI_PREFIX,
                                                              "");
            for (Topology topology : topologies) {
                Optional<Node> matchingNode = topology.getNodes().stream()
                        .filter(node -> node.getNodeId().equals(nodeId))
                        .findAny();
                if (matchingNode.isPresent()) {
                    LOG.debug("Found matching node: {}", matchingNode.get());
                    return matchingNode.get().getPortDescriptions();
                } else {
                    LOG.warn("Unable to find node with ID: {}", deviceId);
                }
            }
        } catch (IOException e) {
            LOG.warn("Unable to parse the topology.");
        }
        return new ArrayList<>();
    }

    @Override
    public Set<LinkDescription> getLinks() {
        // request the current topology
        InputStream topologyStream = getDeviceTopologyStream();
        Set<LinkDescription> links = new HashSet<>();
        try {
            List<Topology> topologies = getTopologies(topologyStream);
            String nodeId = handler().data().deviceId().toString()
                    .replace(DEVICE_URI_PREFIX, "");
            // go through all the received topologies
            for (Topology topology : topologies) {
                // find edges that contain the node
                List<Edge> edges = topology.getEdges();
                for (Edge edge : edges) {
                    if (edge.getSource().getNodeId().equals(nodeId)) {
                        ConnectPoint src = getPortNumber(topology, edge
                                .getSource().getNodeId(), edge.getSource()
                                                                 .getEdgeEnd().get(0).getEdgeEndId());
                        ConnectPoint dst = getPortNumber(topology, edge
                                .getTarget().getNodeId(), edge.getTarget()
                                                                 .getEdgeEnd().get(0).getEdgeEndId());
                        links.add(new DefaultLinkDescription(src, dst,
                                                             Type.DIRECT));
                    } else if (edge.getTarget().getNodeId().equals(nodeId)) {
                        ConnectPoint dst = getPortNumber(topology, edge
                                .getSource().getNodeId(), edge.getSource()
                                                                 .getEdgeEnd().get(0).getEdgeEndId());
                        ConnectPoint src = getPortNumber(topology, edge
                                .getTarget().getNodeId(), edge.getTarget()
                                                                 .getEdgeEnd().get(0).getEdgeEndId());
                        links.add(new DefaultLinkDescription(src, dst,
                                                             Type.DIRECT));
                    }
                }
            }
        } catch (IOException e) {
            LOG.warn("Unable to parse the topology: {}", e);
        }
        return links;
    }

    /**
     * Gets the port number translation.
     *
     * @param topology the topology
     * @param nodeId   the node identifier
     * @param portName the port name
     * @return the connection point representation
     */
    private ConnectPoint getPortNumber(Topology topology, String nodeId,
                                       String portName) {
        Node node = topology.getNodeById(nodeId);
        PortNumber portNumber = node.getEdgeEndNumber(portName);
        return new ConnectPoint(DeviceId.deviceId(DEVICE_URI_PREFIX + nodeId),
                                portNumber);
    }

    /**
     * Get the input stream for the topologies by querying the controller. This
     * only works if a device identifier is available in the handler data.
     *
     * @return the topology input stream for the device
     */
    private InputStream getDeviceTopologyStream() {
        // get the handler to retrieve the controller and the data
        DriverHandler handler = handler();
        RestProxyController controller = checkNotNull(handler
                                                              .get(RestProxyController.class));
        DeviceId deviceId = handler.data().deviceId();
        // request the current topology
        Response response = controller.get(deviceId, TOPOLOGIES_REQUEST, JSON);
        return new ByteArrayInputStream(response.readEntity(String.class).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Get the topologies from the input stream.
     *
     * @param input the input stream
     * @return a list of topologies
     * @throws IOException topologies could not be parsed
     */
    private List<Topology> getTopologies(InputStream input) throws IOException {
        List<Topology> topologies = new ArrayList<>();
        JsonNode rootNode;
        rootNode = mapper.readValue(input, JsonNode.class);
        for (JsonNode topology : rootNode) {
            topologies.add(mapper.treeToValue(topology, Topology.class));
        }
        return topologies;
    }

    @Override
    public Map<DeviceId, DeviceDescription> devices(ConnectionInfo connectionInfo) {
        // get the controller
        RestProxyController controller =
                checkNotNull(handler().get(RestProxyController.class));
        // query the controller
        Response response =
                controller.get(connectionInfo.getConfigurationBasePath() + TOPOLOGIES_REQUEST, JSON);
        InputStream topologyStream =
                new ByteArrayInputStream(response.readEntity(String.class).getBytes(StandardCharsets.UTF_8));
        // create an empty response set
        Map<DeviceId, DeviceDescription> devices = new HashMap<>();
        try {
            List<Topology> topologies = getTopologies(topologyStream);
            // go through all the received topologies and extract the nodes
            for (Topology topology : topologies) {
                List<Node> nodes = topology.getNodes();
                for (Node node : nodes) {
                    try {
                        // tries to create the URI and the device ID
                        URI deviceUri = new URI(DEVICE_URI_PREFIX
                                                        + node.getNodeId());
                        DeviceId idToAdd = DeviceId.deviceId(deviceUri);
                        // adding the needed annotations
                        SparseAnnotations annotations =
                                DefaultAnnotations.builder()
                                        .set(AnnotationKeys.PROTOCOL, "RESTPROXY")
                                        .build();
                        // creating the device description
                        DeviceDescription description =
                                new DefaultDeviceDescription(idToAdd.uri(),
                                                             Device.Type.ROADM,
                                                             MANUFACTURER, VERSION,
                                                             VERSION, "",
                                                             new ChassisId(),
                                                             annotations);
                        devices.put(idToAdd, description);
                        LOG.debug("Added device id: {}", idToAdd);
                    } catch (URISyntaxException e) {
                        LOG.warn("Unable to create URI: {}",
                                 DEVICE_URI_PREFIX + node.getNodeId());
                    }
                }
            }
        } catch (IOException e1) {
            LOG.warn("Unable to parse the topology: {}", e1);
        }
        // the set is either empty (in the case of failure) or contains all
        // nodes that are present in the topology
        return devices;
    }
}
