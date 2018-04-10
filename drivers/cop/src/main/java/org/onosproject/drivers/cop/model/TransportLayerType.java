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

public class TransportLayerType {

    public enum LayerEnum {
        DWDM_LINK, ETHERNET, ETHERNET_BROADCAST, MPLS
    }

    private final LayerEnum layer;

    public enum DirectionEnum {
        UNIDIR, BIDIR
    }

    private final DirectionEnum direction;
    private final String layerId;

    public TransportLayerType(@JsonProperty("layer") LayerEnum layer,
                              @JsonProperty("direction") DirectionEnum direction,
                              @JsonProperty("layerId") String layerId) {
        this.layer = layer;
        this.direction = direction;
        this.layerId = layerId;
    }

    @JsonProperty("layer")
    public LayerEnum getLayer() {
        return layer;
    }

    @JsonProperty("direction")
    public DirectionEnum getDirection() {
        return direction;
    }

    @JsonProperty("layerId")
    public String getLayerId() {
        return layerId;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransportLayerType transportLayerType = (TransportLayerType) o;

        return Objects.equals(layer, transportLayerType.layer)
                && Objects.equals(direction, transportLayerType.direction)
                && Objects.equals(layerId, transportLayerType.layerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(layer, direction, layerId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TransportLayerType {\n");

        sb.append("  layer: ").append(toIndentedString(layer)).append("\n");
        sb.append("  direction: ").append(toIndentedString(direction))
                .append("\n");
        sb.append("  layerId: ").append(toIndentedString(layerId)).append("\n");
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
