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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Edge representation in the COP protocol.
 */
public class Edge {

    private final String name;
    private final String edgeId;

    /**
     * Enumeration of edge types.
     */
    public enum EdgeTypeEnum {
        /** Fiber based DWDM edge. */
        DWDM_EDGE, /** Copper based Ethernet edge. */
        ETH_EDGE
    }

    private final EdgeTypeEnum edgeType;
    private final String switchingCap;
    private final String metric;
    private final String maxResvBw;
    private final Node source;
    private final EdgeEnd localIfid;
    private final EdgeEnd remoteIfid;
    private final String unreservBw;
    private final Node target;

    /**
     * Create a new edge.
     *
     * @param name the edge name
     * @param edgeId the edge identifier
     * @param edgeType the edge type
     * @param switchingCap the switching capabilities
     * @param metric the metric
     * @param maxResvBw the maximum bandwidth for reservations
     * @param source the source node
     * @param localIfid the edge end description of the local interface
     * @param remoteIfid the edge end description of the remote interface
     * @param unreservBw the amount of unreserved bandwidth
     * @param target the target node
     */
    public Edge(@JsonProperty("name") String name,
                @JsonProperty("edgeId") String edgeId,
                @JsonProperty("edgeType") EdgeTypeEnum edgeType,
                @JsonProperty("switchingCap") String switchingCap,
                @JsonProperty("metric") String metric,
                @JsonProperty("maxResvBw") String maxResvBw,
                @JsonProperty("source") Node source,
                @JsonProperty("localIfid") EdgeEnd localIfid,
                @JsonProperty("remoteIfid") EdgeEnd remoteIfid,
                @JsonProperty("unreservBw") String unreservBw,
                @JsonProperty("target") Node target) {
        this.name = name;
        this.edgeId = edgeId;
        this.edgeType = edgeType;
        this.switchingCap = switchingCap;
        this.metric = metric;
        this.maxResvBw = maxResvBw;
        this.source = source;
        this.localIfid = localIfid;
        this.remoteIfid = remoteIfid;
        this.unreservBw = unreservBw;
        this.target = target;
    }

    /**
     * @return the edge name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * @return the edge identifier
     */
    @JsonProperty("edgeId")
    public String getEdgeId() {
        return edgeId;
    }

    /**
     * @return the edge type
     */
    @JsonProperty("edgeType")
    public EdgeTypeEnum getEdgeType() {
        return edgeType;
    }

    /**
     * @return the switching capabilities
     */
    @JsonProperty("switchingCap")
    public String getSwitchingCap() {
        return switchingCap;
    }

    /**
     * @return the metric
     */
    @JsonProperty("metric")
    public String getMetric() {
        return metric;
    }

    /**
     * @return the maximum bandwidth for reservations
     */
    @JsonProperty("maxResvBw")
    public String getMaxResvBw() {
        return maxResvBw;
    }

    /**
     * @return the source node
     */
    @JsonProperty("source")
    public Node getSource() {
        return source;
    }

    /**
     * @return the edge end description of the local interface
     */
    @JsonProperty("localIfid")
    public EdgeEnd getLocalIfid() {
        return localIfid;
    }

    /**
     * @return the edge end description of the remote interface
     */
    @JsonProperty("remoteIfid")
    public EdgeEnd getRemoteIfid() {
        return remoteIfid;
    }

    /**
     * @return the amount of unreserved bandwidth
     */
    @JsonProperty("unreservBw")
    public String getUnreservBw() {
        return unreservBw;
    }

    /**
     * @return the target node
     */
    @JsonProperty("target")
    public Node getTarget() {
        return target;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Edge edge = (Edge) o;

        return Objects.equals(name, edge.name)
                && Objects.equals(edgeId, edge.edgeId)
                && Objects.equals(edgeType, edge.edgeType)
                && Objects.equals(switchingCap, edge.switchingCap)
                && Objects.equals(metric, edge.metric)
                && Objects.equals(maxResvBw, edge.maxResvBw)
                && Objects.equals(source, edge.source)
                && Objects.equals(localIfid, edge.localIfid)
                && Objects.equals(remoteIfid, edge.remoteIfid)
                && Objects.equals(unreservBw, edge.unreservBw)
                && Objects.equals(target, edge.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, edgeId, edgeType, switchingCap, metric,
                            maxResvBw, source, localIfid, remoteIfid,
                            unreservBw, target);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Edge {\n");
        sb.append("  name: ").append(toIndentedString(name)).append("\n");
        sb.append("  edgeId: ").append(toIndentedString(edgeId)).append("\n");
        sb.append("  edgeType: ").append(toIndentedString(edgeType))
                .append("\n");
        sb.append("  switchingCap: ").append(toIndentedString(switchingCap))
                .append("\n");
        sb.append("  metric: ").append(toIndentedString(metric)).append("\n");
        sb.append("  maxResvBw: ").append(toIndentedString(maxResvBw))
                .append("\n");
        sb.append("  source: ").append(toIndentedString(source)).append("\n");
        sb.append("  localIfid: ").append(toIndentedString(localIfid))
                .append("\n");
        sb.append("  remoteIfid: ").append(toIndentedString(remoteIfid))
                .append("\n");
        sb.append("  unreservBw: ").append(toIndentedString(unreservBw))
                .append("\n");
        sb.append("  target: ").append(toIndentedString(target)).append("\n");
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
