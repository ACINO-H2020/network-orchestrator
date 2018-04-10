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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Connection implements Serializable {

    private final String controllerDomainId;
    private final TrafficParams trafficParams;
    private final String connectionId;
    private final Endpoint zEnd;

    public enum OperStatusEnum {
        DOWN, UP
    }

    private final OperStatusEnum operStatus;
    private final Endpoint aEnd;
    private final PathType path;
    private final TransportLayerType transportLayer;
    private final String match;

    public Connection(@JsonProperty("controllerDomainId") String controllerDomainId,
                      @JsonProperty("trafficParams") TrafficParams trafficParams,
                      @JsonProperty("connectionId") String connectionId,
                      @JsonProperty("zEnd") Endpoint zEnd,
                      @JsonProperty("operStatus") OperStatusEnum operStatus,
                      @JsonProperty("aEnd") Endpoint aEnd,
                      @JsonProperty("path") PathType path,
                      @JsonProperty("transportLayer") TransportLayerType transportLayer,
                      @JsonProperty("match") String match) {
        this.controllerDomainId = controllerDomainId;
        this.trafficParams = trafficParams;
        this.connectionId = connectionId;
        this.zEnd = zEnd;
        this.operStatus = operStatus;
        this.aEnd = aEnd;
        this.path = path;
        this.transportLayer = transportLayer;
        this.match = match;
    }

    @JsonProperty("controllerDomainId")
    public String getControllerDomainId() {
        return controllerDomainId;
    }

    @JsonProperty("trafficParams")
    public TrafficParams getTrafficParams() {
        return trafficParams;
    }

    @JsonProperty("connectionId")
    public String getConnectionId() {
        return connectionId;
    }

    @JsonProperty("zEnd")
    public Endpoint getZEnd() {
        return zEnd;
    }

    @JsonProperty("operStatus")
    public OperStatusEnum getOperStatus() {
        return operStatus;
    }

    @JsonProperty("aEnd")
    public Endpoint getAEnd() {
        return aEnd;
    }

    @JsonProperty("path")
    public PathType getPath() {
        return path;
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
        Connection connection = (Connection) o;

        return Objects.equals(controllerDomainId, connection.controllerDomainId)
                && Objects.equals(trafficParams, connection.trafficParams)
                && Objects.equals(connectionId, connection.connectionId)
                && Objects.equals(zEnd, connection.zEnd)
                && Objects.equals(operStatus, connection.operStatus)
                && Objects.equals(aEnd, connection.aEnd)
                && Objects.equals(path, connection.path)
                && Objects.equals(transportLayer, connection.transportLayer)
                && Objects.equals(match, connection.match);
    }

    @Override
    public int hashCode() {
        return Objects.hash(controllerDomainId, trafficParams, connectionId,
                            zEnd, operStatus, aEnd, path, transportLayer,
                            match);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Connection {\n");

        sb.append("  controllerDomainId: ")
                .append(toIndentedString(controllerDomainId)).append("\n");
        sb.append("  trafficParams: ").append(toIndentedString(trafficParams))
                .append("\n");
        sb.append("  connectionId: ").append(toIndentedString(connectionId))
                .append("\n");
        sb.append("  zEnd: ").append(toIndentedString(zEnd)).append("\n");
        sb.append("  operStatus: ").append(toIndentedString(operStatus))
                .append("\n");
        sb.append("  aEnd: ").append(toIndentedString(aEnd)).append("\n");
        sb.append("  path: ").append(toIndentedString(path)).append("\n");
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
