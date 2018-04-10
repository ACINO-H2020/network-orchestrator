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
import org.onlab.util.Frequency;
import org.onlab.util.Spectrum;
import org.onosproject.drivers.tapi.topology.NodeEdgePoint;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.ChannelSpacing;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.OchSignal;
import org.onosproject.net.OduSignalType;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.SparseAnnotations;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.optical.device.OchPortHelper;
import org.onosproject.net.optical.device.port.ClientPortMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.onosproject.drivers.tapi.NodeWrapper.*;
import static org.onosproject.drivers.tapi.TopologyWrapper.nameAndValueToMap;

/**
 * Wraps TAPI node edge points and adds additional methods.
 */
class NodeEdgePointWrapper {
    static final String UUID_TAG = "uuid";
    private static final String CHANNELS = "channels";
    private static final String CLIENT_PORT_TYPE = "client";
    private static final String CLIENT_RATE_TAG = "ethClientRate";
    private static final String LINE_PORT_TYPE = "line";
    private static final Logger LOG = LoggerFactory.getLogger(NodeEdgePointWrapper.class);
    private static final String NETWORK_PORT_TAG = "networkPort";
    private static final String PORT_TYPE_TAG = "portType";
    private static ObjectMapper mapper = new ObjectMapper();
    private final NodeEdgePoint edgePoint;
    private Map<String, String> labels;
    private Map<String, String> names;

    /**
     * Constructor for wrapping a node edge point.
     *
     * @param edgePoint the node edge point to be wrapped
     */
    NodeEdgePointWrapper(NodeEdgePoint edgePoint) {
        this.edgePoint = edgePoint;
        if (edgePoint.getLabel() != null) {
            labels = nameAndValueToMap(edgePoint.getLabel());
        } else {
            labels = new HashMap<>();
        }
        if (edgePoint.getName() != null) {
            names = nameAndValueToMap(edgePoint.getName());
        } else {
            names = new HashMap<>();
        }
    }

    /**
     * @return the name tag or null
     */
    String getNameTag() {
        return names.get(NAME_TAG);
    }

    /**
     * @return the uuid string or null
     */
    String uuidString() {
        return edgePoint.getUuid().getUniversalId();
    }

    /**
     * Collects all annotations that are available in the TAPI representation.
     *
     * @return annotations for a port description
     */
    SparseAnnotations collectAnnotations() {
        DefaultAnnotations.Builder annotationsBuilder = DefaultAnnotations.builder();
        annotationsBuilder.set(UUID_TAG, edgePoint.getUuid().getUniversalId());
        if (names.containsKey(NAME_TAG)) {
            annotationsBuilder.set(AnnotationKeys.PORT_NAME, getNameTag());
        }
        if (!edgePoint.getMappedServiceInterfacePoint().isEmpty()) {
            // FIXME: only taking the first service-end-point
            annotationsBuilder.set(SERVICE_END_POINT_TAG,
                    edgePoint.getMappedServiceInterfacePoint().get(0).getUniversalId());
        }
        if (labels.containsKey(ENCRYPTION_TAG)) {
            annotationsBuilder.set(ENCRYPTION_TAG, "true");
        }
        // TODO: there should be a different handling for ODUCLT, OCH and OMS ports.
//        if (labels.containsKey(CHANNELS)) {
//            annotationsBuilder.set(CHANNELS, getChannelString());
//        }
        if (labels.containsKey(NETWORK_PORT_TAG)) {
            annotationsBuilder.set(ClientPortMapper.NETWORK_PORT, labels.get(NETWORK_PORT_TAG));
        }
        return annotationsBuilder.build();
    }

    /**
     * Translates the string to a Port.Type taking into account if the port is part of a disaggregation process.
     *
     * @param disaggregate indicates whether the port type is needed for a disaggregation
     * @return the port type
     */
    Port.Type getPortType(boolean disaggregate) {
        String type = labels.get(PORT_TYPE_TAG);
        if (CLIENT_PORT_TYPE.equals(type)) {
            //FIXME change port type
            return disaggregate ? Port.Type.COPPER : Port.Type.COPPER;
        } else if (LINE_PORT_TYPE.equals(type)) {
            return disaggregate ? Port.Type.OMS : Port.Type.FIBER;
        } else {
            throw new IllegalArgumentException("Unknown port type: " + type);
        }
    }

    /**
     * @return the data rate in Mbps
     */
    int getDataRate() {
        return Integer.parseInt(labels.getOrDefault(CLIENT_RATE_TAG, "1")) * 1000;
    }

    /**
     * @return true if a network port name is defined
     */
    boolean hasNetworkPort() {
        return labels.containsKey(NETWORK_PORT_TAG);
    }

    /**
     * Extracts the port description of the network port if available, otherwise null.
     *
     * @param portNumber the port number that should be assigned to the network port
     * @return the network port's description or null
     */
    PortDescription getNetworkPort(PortNumber portNumber) {
        if (!hasNetworkPort()) {
            return null;
        }
        DefaultAnnotations.Builder builder =
                DefaultAnnotations.builder().set(AnnotationKeys.PORT_NAME, labels.get(NETWORK_PORT_TAG));
        List<Double> channels = getChannels();
        if (!channels.isEmpty()) {
            builder.set(CHANNELS, getChannelString());
            // FIXME: always assigning first frequency. How to handle multiple frequencies?
            Frequency centerFrequency = Frequency.ofTHz(channels.get(0));
            int frequencyIndex = (int) Math.round((double) centerFrequency.
                    subtract(Spectrum.CENTER_FREQUENCY).asHz() / ChannelSpacing.CHL_50GHZ.frequency().asHz());
            OchSignal ochSignal = OchSignal.newDwdmSlot(ChannelSpacing.CHL_50GHZ, frequencyIndex);
            return OchPortHelper.ochPortDescription(portNumber,
                    true,
                    OduSignalType.ODU2e,
                    channels.size() > 1,
                    ochSignal,
                    builder.build());
        } else {
            // assuming that no channels means fully tunable
            return OchPortHelper.ochPortDescription(portNumber,
                    true,
                    OduSignalType.ODU2e,
                    true,
                    OchSignal.newDwdmSlot(ChannelSpacing.CHL_50GHZ, 0),
                    builder.build());
        }
    }

    /**
     * @return a list of channels if available, otherwise empty
     */
    private List<Double> getChannels() {
        String channelLabel = labels.get(CHANNELS);
        List<Double> channels = new ArrayList<>();
        if (channelLabel == null) {
            return channels;
        }
        try {
            JsonNode rootNode = mapper.readValue(channelLabel, JsonNode.class);
            for (JsonNode channel : rootNode) {
                channels.add(channel.asDouble());
            }
        } catch (IOException e) {
            LOG.error("Unable to parse the channels.", e);
        }
        return channels;
    }

    /**
     * A string representation of the channels used for the annotations.
     *
     * @return comma separated channel values
     */
    private String getChannelString() {
        return getChannels().stream().map(Object::toString).collect(Collectors.joining(","));
    }
}
