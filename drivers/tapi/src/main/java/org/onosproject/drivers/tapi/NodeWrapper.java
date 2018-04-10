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

import org.onlab.packet.ChassisId;
import org.onosproject.drivers.tapi.common.LayerProtocolName;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.UniversalId;
import org.onosproject.drivers.tapi.topology.Node;
import org.onosproject.drivers.tapi.topology.NodeEdgePoint;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.optical.device.RoadmArchitecture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.onosproject.drivers.tapi.NodeEdgePointWrapper.UUID_TAG;
import static org.onosproject.drivers.tapi.TopologyWrapper.nameAndValueToMap;
import static org.onosproject.net.optical.device.RoadmArchitecture.ARCH_ANNOTATION;

/**
 * Wraps TAPI nodes and adds additional methods.
 */
class NodeWrapper {

    static final String SERVICE_END_POINT_TAG = "service-end-point";
    static final String NAME_TAG = "name";
    static final String ENCRYPTION_TAG = "encryption";
    private static final String ARCHITECTURE_TAG = "nodeArchitecture";
    private static final String CDC_AD = "CdcAddDrop";
    private static final String DIRECTIONLESS_AD = "DirectionlessAddDrop";
    private static final String DOMAIN_ID = "domainId";
    private static final Logger LOG = LoggerFactory.getLogger(NodeWrapper.class);
    private static final String MANUFACTURER = "TAPI";

    private static final String VERSION = "1.0.0";

    private final Node node;
    private Map<String, String> labels;

    /**
     * Constructor for wrapping a node.
     *
     * @param node the node to be wrapped
     */
    NodeWrapper(Node node) {
        this.node = node;
        labels = nameAndValueToMap(node.getLabel());
    }

    /**
     * Method for sorting {@link NodeEdgePoint}s by name. In case a name is not available the UUID is used instead.
     *
     * @param p1 the first node edge point
     * @param p2 the second node edge point
     * @return the result of the comparison based on compareTo
     */
    private static int compareByName(NodeEdgePoint p1, NodeEdgePoint p2) {
        NodeEdgePointWrapper wp1 = new NodeEdgePointWrapper(p1);
        NodeEdgePointWrapper wp2 = new NodeEdgePointWrapper(p2);
        if (wp1.getNameTag() != null && wp2.getNameTag() != null) {
            return wp1.getNameTag().compareTo(wp2.getNameTag());
        }

        return wp1.uuidString().compareTo(wp2.uuidString());
    }

    /**
     * Translates an edge end identifier to the corresponding port number.
     *
     * @param edgeEndId the edge end's identifier
     * @return the port number or null
     */
    PortNumber getEdgeEndNumber(UniversalId edgeEndId) {
        for (PortDescription port : getPortDescriptions(false)) {
            if (port.annotations().value(UUID_TAG).equals(edgeEndId.getUniversalId())) {
                return port.portNumber();
            }
        }
        return null;
    }

    String getEdgeEndName(UniversalId edgeEndId) {
        Optional<NodeEdgePoint> edgeEnd = node
                .getOwnedNodeEdgePoint()
                .stream()
                .filter(nodeEdgePoint -> nodeEdgePoint.getUuid().equals(edgeEndId))
                .findFirst();
        NodeEdgePointWrapper wrappedEdgeEnd = new NodeEdgePointWrapper(edgeEnd.get());
        return wrappedEdgeEnd.getNameTag();
    }

    /**
     * Returns the device description of the wrapped node.
     *
     * @return the device description
     */
    DeviceDescription getDeviceDescription() {
        // adding the needed annotations
        DefaultAnnotations.Builder annotations = DefaultAnnotations.builder()
                // TODO: who needs to do this? Driver or Provider.
                .set(AnnotationKeys.PROTOCOL, "REST")
                // FIXME: the domain ID should be unique per controller
                .set(DOMAIN_ID, "tapi").set(UUID_TAG, node.getUuid().getUniversalId());
        Device.Type devType =
                node.getLayerProtocolName().contains(LayerProtocolName.ETH) ? Device.Type.SWITCH : Device.Type.ROADM;
        if (labels.containsKey(ENCRYPTION_TAG)) {
            annotations.set(ENCRYPTION_TAG, "true");
        }
        RoadmArchitecture roadmArchitecture = getArchitecture();
        if (roadmArchitecture != null) {
            annotations.set(ARCH_ANNOTATION, roadmArchitecture.toString());
        }
        // creating the device description
        return new DefaultDeviceDescription(getDeviceId().uri(),
                devType,
                MANUFACTURER,
                VERSION,
                VERSION,
                "",
                new ChassisId(),
                annotations.build());
    }

    /**
     * Translates an architecture type string to an enumeration value.
     *
     * @return the corresponding enumeration value
     */
    public RoadmArchitecture getArchitecture() {
        //TODO: improve as soon as more types are available
        if (labels.containsKey(ARCHITECTURE_TAG)) {

            String archType = labels.get(ARCHITECTURE_TAG);
            if (archType.equals(DIRECTIONLESS_AD)) {
                return RoadmArchitecture.D;
            } else if (archType.equalsIgnoreCase(CDC_AD)) {
                return RoadmArchitecture.CDC;
            } else {
                LOG.warn("Unknown architecture type: {}", archType);
                return null;
            }
        }
        LOG.warn("Architecture not annotated");
        return null;
    }

    /**
     * Translates all edge ends to a list of port descriptions.
     *
     * @param disaggregate indicates whether the port descriptions are needed for a disaggregation
     * @return list of all port descriptions
     */
    List<PortDescription> getPortDescriptions(boolean disaggregate) {
        ArrayList<PortDescription> ports = new ArrayList<>();
        Map<String, PortDescription> ochPorts = new HashMap<>();
        int portNumber = 1;
        List<NodeEdgePoint> nodeEdgePoints = new ArrayList<>(node.getOwnedNodeEdgePoint());
        nodeEdgePoints.sort(NodeWrapper::compareByName);
        for (NodeEdgePoint edgePoint : nodeEdgePoints) {
            NodeEdgePointWrapper wrappedEdgePoint = new NodeEdgePointWrapper(edgePoint);
            PortNumber number = PortNumber.portNumber(portNumber++);
            // labels
            if (wrappedEdgePoint.hasNetworkPort()) {
                ochPorts.putIfAbsent(wrappedEdgePoint.getNameTag(),
                        wrappedEdgePoint.getNetworkPort(PortNumber.portNumber(portNumber++)));
            }
            ports.add(new DefaultPortDescription(number,
                    true,
                    wrappedEdgePoint.getPortType(disaggregate),
                    wrappedEdgePoint.getDataRate(),
                    wrappedEdgePoint.collectAnnotations()));
        }
        ports.addAll(ochPorts.values());
        return ports;
    }

    /**
     * @return the node's device ID
     */
    DeviceId getDeviceId() {
        Optional<String> nodeName = node
                .getName()
                .stream()
                .filter(entry -> NAME_TAG.equals(entry.getValueName()))
                .map(NameAndValue::getValue)
                .findAny();
        String identifier = nodeName.orElse(node.getUuid().getUniversalId());
        return DeviceId.deviceId(TapiTopologyDiscovery.DEVICE_URI_PREFIX + identifier);
    }

}
