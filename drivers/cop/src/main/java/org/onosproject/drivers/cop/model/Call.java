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

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

public class Call implements Serializable {

    public enum OperStatusEnum {
        DOWN, UP
    }

    private final OperStatusEnum operStatus;
    private final String callId;
    private final Endpoint zEnd;
    private final List<Connection> connections;
    private final TrafficParams trafficParams;
    private final Endpoint aEnd;
    private final TransportLayerType transportLayer;
    private final String match;

    public Call(@JsonProperty("operStatus") OperStatusEnum operStatus,
                @JsonProperty("callId") String callId,
                @JsonProperty("zEnd") Endpoint zEnd,
                @JsonProperty("connections") List<Connection> connections,
                @JsonProperty("trafficParams") TrafficParams trafficParams,
                @JsonProperty("aEnd") Endpoint aEnd,
                @JsonProperty("transportLayer") TransportLayerType transportLayer,
                @JsonProperty("match") String match) {
        this.operStatus = operStatus;
        this.callId = callId;
        this.zEnd = zEnd;
        this.connections = connections != null ? ImmutableList
                .copyOf(connections) : ImmutableList.of();
        this.trafficParams = trafficParams;
        this.aEnd = aEnd;
        this.transportLayer = transportLayer;
        this.match = match;
    }

    @JsonProperty("operStatus")
    public OperStatusEnum getOperStatus() {
        return operStatus;
    }

    @JsonProperty("callId")
    public String getCallId() {
        return callId;
    }

    @JsonProperty("zEnd")
    public Endpoint getZEnd() {
        return zEnd;
    }

    @JsonProperty("connections")
    public List<Connection> getConnections() {
        return connections;
    }

    @JsonProperty("trafficParams")
    public TrafficParams getTrafficParams() {
        return trafficParams;
    }

    @JsonProperty("aEnd")
    public Endpoint getAEnd() {
        return aEnd;
    }

    @JsonProperty("transportLayer")
    public TransportLayerType getTransportLayer() {
        return transportLayer;
    }

    @JsonProperty("match")
    public String getMatch() {
        return match;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Call call = (Call) o;

        return Objects.equals(operStatus, call.operStatus)
                && Objects.equals(callId, call.callId)
                && Objects.equals(zEnd, call.zEnd)
                && Objects.equals(connections, call.connections)
                && Objects.equals(trafficParams, call.trafficParams)
                && Objects.equals(aEnd, call.aEnd)
                && Objects.equals(transportLayer, call.transportLayer)
                && Objects.equals(match, call.match);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operStatus, callId, zEnd, connections,
                            trafficParams, aEnd, transportLayer, match);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Call {\n");
        sb.append("  operStatus: ").append(toIndentedString(operStatus))
                .append("\n");
        sb.append("  callId: ").append(toIndentedString(callId)).append("\n");
        sb.append("  zEnd: ").append(toIndentedString(zEnd)).append("\n");
        sb.append("  connections: ").append(toIndentedString(connections))
                .append("\n");
        sb.append("  trafficParams: ").append(toIndentedString(trafficParams))
                .append("\n");
        sb.append("  aEnd: ").append(toIndentedString(aEnd)).append("\n");
        sb.append("  transportLayer: ").append(toIndentedString(transportLayer))
                .append("\n");
        sb.append("  match: ").append(toIndentedString(match)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n  ");
    }
}
