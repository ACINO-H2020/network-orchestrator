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

package org.onosproject.drivers.tapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.onosproject.drivers.tapi.topology.Node;
import org.onosproject.drivers.tapi.topology.Topology;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.behaviour.DevicesDiscovery;
import org.onosproject.net.behaviour.LinkDiscovery;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.net.device.DeviceDescriptionDiscovery;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.link.LinkDescription;
import org.onosproject.net.optical.device.RoadmArchitecture;
import org.onosproject.net.optical.disaggregator.DisaggregatorService;
import org.onosproject.protocol.http.HttpSBController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TAPI implementation of topology related behavior.
 */
public class TapiTopologyDiscovery extends AbstractHandlerBehaviour implements DeviceDescriptionDiscovery,
        DevicesDiscovery, LinkDiscovery {

    static final String DEVICE_URI_PREFIX = "rest:";
    private static final Logger LOG = LoggerFactory.getLogger(TapiTopologyDiscovery.class);
    private static final String TOPOLOGIES_REQUEST = "operations/tapi-topology%3Aget-topology-list";
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public Set<DeviceId> deviceIds() {
        try {
            try {
                final DisaggregatorService disaggregator = handler().get(DisaggregatorService.class);
                return getTopologyNodeWrappers()
                        .map(nodeWrapper -> disaggregator.getDisaggregatedDevIds(nodeWrapper.getDeviceId(),
                                nodeWrapper.getArchitecture(),
                                nodeWrapper.getPortDescriptions(true)))
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet());
            } catch (org.onlab.osgi.ServiceNotFoundException e) {
                LOG.info("Disaggregator service is not available, getting device IDs locally.");
            }
            return getTopologyNodeWrappers().map(NodeWrapper::getDeviceId).collect(Collectors.toSet());
        } catch (IOException e) {
            LOG.error("Unable to parse topologies.", e);
            return new HashSet<>();
        }
    }

    /**
     * Returns a stream of wrapped nodes that are available in the topology.
     *
     * @return stream of wrapped nodes
     * @throws IOException unable to parse the topology
     */
    private Stream<NodeWrapper> getTopologyNodeWrappers() throws IOException {
        return getTopologies().stream().flatMap(topology -> topology.getNode().stream()).map(NodeWrapper::new);
    }

    /**
     * Retrieves a list of all available topologies from the controller. An exception is thrown in case of failure.
     *
     * @return list of available topologies
     * @throws IOException retrieving the topologies failed
     */
    private List<Topology> getTopologies() throws IOException {
        HttpSBController controller = Preconditions.checkNotNull(handler().get(HttpSBController.class));
        DeviceId deviceId = handler().data().deviceId();
        String jsonNode = controller.post(deviceId,
                TOPOLOGIES_REQUEST,
                new ByteArrayInputStream(new byte[0]),
                MediaType.APPLICATION_JSON_TYPE,
                String.class);
        JsonNode rootNode = mapper.readValue(jsonNode, JsonNode.class);
        List<Topology> topologies = new ArrayList<>();
        for (JsonNode topology : rootNode) {
            topologies.add(mapper.treeToValue(topology, Topology.class));
        }
        return topologies;
    }

    @Override
    public DeviceDescription deviceDetails(DeviceId deviceId) {
        try {
            DisaggregatorService disaggregator = handler().get(DisaggregatorService.class);
            try {
                Optional<DeviceDescription> matchingNode = getTopologyNodeWrappers()
                        .filter(nodeWrapper -> disaggregator
                                .getDisaggregatedDevIds(nodeWrapper.getDeviceId(),
                                        nodeWrapper.getArchitecture(),
                                        nodeWrapper.getPortDescriptions(true))
                                .contains(deviceId))
                        .map(nodeWrapper -> disaggregator.getDisaggregatedDevDescription(deviceId,
                                nodeWrapper.getDeviceDescription()))
                        .findAny();
                if (matchingNode.isPresent()) {
                    return matchingNode.get();
                } else {
                    throw new IllegalArgumentException("Device ID did not match any known device.");
                }
            } catch (IOException e) {
                LOG.error("Unable to parse topologies.", e);
                return null;
            }
        } catch (org.onlab.osgi.ServiceNotFoundException e) {
            LOG.info("Disaggregator service is not available, getting device details locally.");
            NodeWrapper node = new NodeWrapper(getNodeFromTopology(deviceId));
            return node.getDeviceDescription();
        }
    }

    @Override
    public DeviceDescription discoverDeviceDetails() {
        return null;
    }

    @Override
    public List<PortDescription> discoverPortDetails() {
        try {
            DisaggregatorService disaggregator = handler().get(DisaggregatorService.class);
            DeviceService deviceService = handler().get(DeviceService.class);
            DeviceId deviceId = handler().data().deviceId();
            String originalIdString =
                    deviceService.getDevice(deviceId).annotations().value(RoadmArchitecture.ORIGINAL_NODE_ANNOTATION);
            NodeWrapper wrappedNode = new NodeWrapper(getNodeFromTopology(DeviceId.deviceId(originalIdString)));
            return disaggregator.getDisaggregatedPortDescriptions(deviceId, wrappedNode.getPortDescriptions(true));
        } catch (org.onlab.osgi.ServiceNotFoundException e) {
            Node node = getNodeFromTopology(handler().data().deviceId());
            if (node != null) {
                return new NodeWrapper(node).getPortDescriptions(false);
            } else {
                return new ArrayList<>();
            }
        }
    }

    /**
     * Looks for a node in the topology that corresponds to the given device ID. Only IDs of physical nodes can be
     * found. This method will not work for disaggregated device IDs.
     *
     * @param deviceId the requested device ID
     * @return the matching node in the topology or null if not found
     */
    private Node getNodeFromTopology(DeviceId deviceId) {
        try {
            List<Topology> topologies = getTopologies();
            for (Topology topology : topologies) {
                Node node = new TopologyWrapper(topology).getNodeById(deviceId);
                if (node != null) {
                    LOG.debug("Found a matching node: {}", deviceId);
                    return node;
                }
            }
        } catch (IOException e) {
            LOG.warn("Unable to parse the topology.");
        }
        LOG.warn("Unable to find node with ID: {}", handler().data().deviceId());
        return null;
    }

    @Override
    public Set<LinkDescription> getLinks() {
        Set<LinkDescription> links = new HashSet<>();
        try {
            try {
                DisaggregatorService disaggregator = handler().get(DisaggregatorService.class);
                for (Topology topology : getTopologies()) {
                    links.addAll(disaggregator.getDisaggregatedLinks(handler().data().deviceId(),
                            new TopologyWrapper(topology).getAllLinkDescriptions()));
                }
            } catch (org.onlab.osgi.ServiceNotFoundException e) {
                // go through all the received topologies
                for (Topology topology : getTopologies()) {
                    links.addAll(new TopologyWrapper(topology).getLinkDescription(handler().data().deviceId()));
                }
            }
        } catch (IOException e1) {
            LOG.warn("Unable to parse the topology: {}", e1);
        }
        return links;
    }

}
