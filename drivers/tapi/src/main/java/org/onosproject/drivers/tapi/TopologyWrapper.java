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

import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.UniversalId;
import org.onosproject.drivers.tapi.topology.Link;
import org.onosproject.drivers.tapi.topology.Node;
import org.onosproject.drivers.tapi.topology.Topology;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.DeviceId;
import org.onosproject.net.SparseAnnotations;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkDescription;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.onosproject.drivers.tapi.NodeEdgePointWrapper.UUID_TAG;
import static org.onosproject.net.optical.device.port.ClientPortMapper.DST_PORT_NAME_ANNOTATION;
import static org.onosproject.net.optical.device.port.ClientPortMapper.SRC_PORT_NAME_ANNOTATION;

/**
 * Wraps TAPI topologies and adds additional methods.
 */
class TopologyWrapper {

    private final Topology topology;

    TopologyWrapper(Topology topology) {
        this.topology = topology;
    }

    static Map<String, String> nameAndValueToMap(List<NameAndValue> list) {
        return list.stream().collect(Collectors.toMap(NameAndValue::getValueName, NameAndValue::getValue));
    }

    /**
     * Retrieve a node by device ID.
     *
     * @param deviceId the device ID of the node
     * @return the matching node
     */
    Node getNodeById(DeviceId deviceId) {
        Optional<Node> optNode = topology
                .getNode()
                .stream()
                .filter(node -> deviceId.equals(new NodeWrapper(node).getDeviceId()))
                .findAny();
        return optNode.orElse(null);
    }

    /**
     * Finds the node with the given UUID.
     *
     * @param uuid the UUID of the node
     * @return the matching node
     */
    private Node getNodeByUniversalId(UniversalId uuid) {
        Optional<Node> optNode = topology.getNode().stream().filter(node -> uuid.equals(node.getUuid())).findAny();
        return optNode.orElse(null);
    }

    /**
     * Collects all link descriptions related to this particular device ID.
     *
     * @param deviceId the links for this device ID
     * @return set of all links for this device ID
     */
    Set<LinkDescription> getLinkDescription(DeviceId deviceId) {
        Set<LinkDescription> links = new HashSet<>();
        Node node = getNodeById(deviceId);
        if (node != null) {
            for (Link edge : topology.getLink()) {
                List<ConnectPoint> connectPoints = getConnectionPoints(edge);
                if (edge.getNode().get(0).equals(node.getUuid())) {
                    links.add(createLink(connectPoints, false, org.onosproject.net.Link.Type.DIRECT,
                            getAnnotations(edge, false)));
                } else if (edge.getNode().get(1).equals(node.getUuid())) {
                    links.add(createLink(connectPoints, true, org.onosproject.net.Link.Type.DIRECT,
                            getAnnotations(edge, true)));
                }
            }
        }
        return links;
    }

    /**
     * Collects all link descriptions available in the topology.
     *
     * @return set of all links
     */
    Set<LinkDescription> getAllLinkDescriptions() {
        Set<LinkDescription> links = new HashSet<>();
        for (Link link : topology.getLink()) {
            List<ConnectPoint> connectPoints = getConnectionPoints(link);
            links.add(createLink(connectPoints,
                    false,
                    org.onosproject.net.Link.Type.OPTICAL,
                    getAnnotations(link, false)));
            links.add(createLink(connectPoints,
                    true,
                    org.onosproject.net.Link.Type.OPTICAL,
                    getAnnotations(link, true)));
        }
        return links;
    }

    /**
     * Creates a link description based on a list of connect points (currently 2). The direction can be reversed if
     * needed.
     *
     * @param connectPoints the (two) connect points
     * @param reverse reverse direction
     * @return the link description
     */
    private LinkDescription createLink(List<ConnectPoint> connectPoints, boolean reverse,
                                       org.onosproject.net.Link.Type linkType, SparseAnnotations... annotations) {
        ConnectPoint one = connectPoints.get(0);
        ConnectPoint two = connectPoints.get(1);
        if (!reverse) {
            return new DefaultLinkDescription(one, two, linkType, annotations);
        } else {
            return new DefaultLinkDescription(two, one, linkType, annotations);
        }
    }

    /**
     * Get the connection points based on the link description.
     *
     * @param link the link
     * @return the connect points
     */
    private List<ConnectPoint> getConnectionPoints(Link link) {
        if (link.getNode().size() != link.getNodeEdgePoint().size()) {
            throw new IllegalArgumentException(
                    "The number of nodes has to match the number of node edge points for a link.");
        }
        List<ConnectPoint> connectPoints = new ArrayList<>();
        List<UniversalId> nodes = link.getNode();
        List<UniversalId> edgePoints = link.getNodeEdgePoint();
        for (int i = 0; i < nodes.size(); i++) {
            connectPoints.add(getConnectionPoint(nodes.get(i), edgePoints.get(i)));
        }
        return connectPoints;
    }

    private SparseAnnotations getAnnotations(Link link, boolean reverse) {
        DefaultAnnotations.Builder builder = DefaultAnnotations.builder();
        List<UniversalId> nodes = link.getNode();
        List<UniversalId> edgePoints = link.getNodeEdgePoint();
        builder.set(reverse ? DST_PORT_NAME_ANNOTATION : SRC_PORT_NAME_ANNOTATION,
                new NodeWrapper(getNodeByUniversalId(nodes.get(0))).getEdgeEndName(edgePoints.get(0)));
        builder.set(reverse ? SRC_PORT_NAME_ANNOTATION : DST_PORT_NAME_ANNOTATION,
                new NodeWrapper(getNodeByUniversalId(nodes.get(1))).getEdgeEndName(edgePoints.get(1)));
        builder.set(UUID_TAG, link.getUuid().getUniversalId());
        return builder.build();
    }

    /**
     * Gets the connection point for the given node uuid and port uuid.
     *
     * @param nodeId the node identifier
     * @param portName the port name
     * @return the connection point representation
     */

    private ConnectPoint getConnectionPoint(UniversalId nodeId, UniversalId portName) {
        NodeWrapper node = new NodeWrapper(getNodeByUniversalId(nodeId));
        return new ConnectPoint(node.getDeviceId(), node.getEdgeEndNumber(portName));
    }
}
