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

package org.onosproject.drivers.cop.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.Port.Type;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.device.PortDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Node representation in the COP protocol.
 */
public class Node {

    private final String domain;
    private final String nodetype;
    private final String name;
    private final List<EdgeEnd> edgeEnd;
    private final String nodeId;
    private final List<String> underlayAbstractTopology;

    /**
     * Create a new node.
     *
     * @param domain the domain name
     * @param nodetype the node type
     * @param name the node name
     * @param edgeEnd list of all edge ends
     * @param nodeId the node's identifier
     * @param underlayAbstractTopology names of all abstract underlay topologies
     */
    public Node(@JsonProperty("domain") String domain,
                @JsonProperty("nodetype") String nodetype,
                @JsonProperty("name") String name,
                @JsonProperty("edgeEnd") List<EdgeEnd> edgeEnd,
                @JsonProperty("nodeId") String nodeId,
                @JsonProperty("underlayAbstractTopology") List<String> underlayAbstractTopology) {
        this.domain = domain;
        this.nodetype = nodetype;
        this.name = name;
        this.edgeEnd = edgeEnd != null ? ImmutableList.copyOf(edgeEnd)
                                       : ImmutableList.of();
        this.nodeId = nodeId;
        this.underlayAbstractTopology = underlayAbstractTopology != null ? ImmutableList
                .copyOf(underlayAbstractTopology) : ImmutableList.of();
    }

    /**
     * @return the domain name
     */
    @JsonProperty("domain")
    public String getDomain() {
        return domain;
    }

    /**
     * @return the node type
     */
    @JsonProperty("nodetype")
    public String getNodetype() {
        return nodetype;
    }

    /**
     * @return the node name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * @return list of all edge ends
     */
    @JsonProperty("edgeEnd")
    public List<EdgeEnd> getEdgeEnd() {
        return edgeEnd;
    }

    /**
     * @return the node's identifier
     */
    @JsonProperty("nodeId")
    public String getNodeId() {
        return nodeId;
    }

    /**
     * @return names of all abstract underlay topologies
     */
    @JsonProperty("underlayAbstractTopology")
    public List<String> getUnderlayAbstractTopology() {
        return underlayAbstractTopology;
    }

    /**
     * Translates an edge end identifier to the corresponding port number.
     *
     * @param edgeEndId the edge end's identifier
     * @return the port number or null
     */
    public PortNumber getEdgeEndNumber(String edgeEndId) {
        for (PortDescription port : getPortDescriptions()) {
            if (port.annotations().value(AnnotationKeys.PORT_NAME)
                    .equals(edgeEndId)) {
                return port.portNumber();
            }
        }
        return null;
    }

    /**
     * Translates all edge ends to a list of port descriptions.
     *
     * @return list of all port descriptions
     */
    public List<PortDescription> getPortDescriptions() {
        ArrayList<PortDescription> ports = new ArrayList<>();
        int i = 1;
        for (EdgeEnd port : edgeEnd) {

            PortNumber number = PortNumber.portNumber(i++);
            DefaultAnnotations.Builder annotationsBuilder = DefaultAnnotations
                    .builder()
                    .set(AnnotationKeys.PORT_NAME, port.getEdgeEndId());
            Port.Type type;
            if (port.getEdgeEndId().contains("OL")) {
                type = Type.FIBER;
            } else {
                type = Type.COPPER;
            }
            PortDescription portDescription = new DefaultPortDescription(number,
                                                                         true,
                                                                         type,
                                                                         1000,
                                                                         annotationsBuilder
                                                                                 .build());
            ports.add(portDescription);
        }
        return ports;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Node node = (Node) o;

        return Objects.equals(domain, node.domain)
                && Objects.equals(nodetype, node.nodetype)
                && Objects.equals(name, node.name)
                && Objects.equals(edgeEnd, node.edgeEnd)
                && Objects.equals(nodeId, node.nodeId)
                && Objects.equals(underlayAbstractTopology,
                                  node.underlayAbstractTopology);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, nodetype, name, edgeEnd, nodeId,
                            underlayAbstractTopology);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Node {\n");
        sb.append("  domain: ").append(toIndentedString(domain)).append("\n");
        sb.append("  nodetype: ").append(toIndentedString(nodetype))
                .append("\n");
        sb.append("  name: ").append(toIndentedString(name)).append("\n");
        sb.append("  edgeEnd: ").append(toIndentedString(edgeEnd)).append("\n");
        sb.append("  nodeId: ").append(toIndentedString(nodeId)).append("\n");
        sb.append("  underlayAbstractTopology: ")
                .append(toIndentedString(underlayAbstractTopology))
                .append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     *
     * @param o the object to be converted
     * @return indented string representation
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n  ");
    }
}
