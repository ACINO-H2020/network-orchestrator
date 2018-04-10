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

import java.util.List;
import java.util.Objects;

/**
 * Topology representation in the COP protocol.
 */
public class Topology {

    private final String topologyId;
    private final List<String> underlayTopology;
    private final List<Node> nodes;
    private final List<Edge> edges;

    /**
     * Create a new topology.
     *
     * @param topologyId the topology's identifier
     * @param underlayTopology names of all included underlay topologies
     * @param nodes all nodes that are part of the topology
     * @param edges all edges that are part of the topology
     */
    public Topology(@JsonProperty("topologyId") String topologyId,
                    @JsonProperty("underlayTopology") List<String> underlayTopology,
                    @JsonProperty("nodes") List<Node> nodes,
                    @JsonProperty("edges") List<Edge> edges) {
        this.topologyId = topologyId;
        this.underlayTopology = underlayTopology != null ? ImmutableList
                .copyOf(underlayTopology) : ImmutableList.of();
        this.nodes = nodes != null ? ImmutableList.copyOf(nodes)
                                   : ImmutableList.of();
        this.edges = edges != null ? ImmutableList.copyOf(edges)
                                   : ImmutableList.of();
    }

    /**
     * @return the topology's identifier
     */
    @JsonProperty("topologyId")
    public String getTopologyId() {
        return topologyId;
    }

    /**
     * @return names of all included underlay topologies
     */
    @JsonProperty("underlayTopology")
    public List<String> getUnderlayTopology() {
        return underlayTopology;
    }

    /**
     * @return all nodes that are part of the topology
     */
    @JsonProperty("nodes")
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * @return all edges that are part of the topology
     */
    @JsonProperty("edges")
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * Searches for a node with the identifier.
     *
     * @param nodeId the node's identifier
     * @return the node or null
     */
    public Node getNodeById(String nodeId) {
        for (Node node : nodes) {
            if (node.getNodeId().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Topology topology = (Topology) o;

        return Objects.equals(topologyId, topology.topologyId)
                && Objects.equals(underlayTopology, topology.underlayTopology)
                && Objects.equals(nodes, topology.nodes)
                && Objects.equals(edges, topology.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topologyId, underlayTopology, nodes, edges);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Topology {\n");
        sb.append("  topologyId: ").append(toIndentedString(topologyId))
                .append("\n");
        sb.append("  underlayTopology: ")
                .append(toIndentedString(underlayTopology)).append("\n");
        sb.append("  nodes: ").append(toIndentedString(nodes)).append("\n");
        sb.append("  edges: ").append(toIndentedString(edges)).append("\n");
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
