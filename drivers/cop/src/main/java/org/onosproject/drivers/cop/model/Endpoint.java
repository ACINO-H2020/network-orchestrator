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

public class Endpoint implements Serializable {

    private final String routerId;
    private final String interfaceId;
    private final String endpointId;

    public Endpoint(@JsonProperty("routerId") String routerId,
                    @JsonProperty("interfaceId") String interfaceId,
                    @JsonProperty("endpointId") String endpointId) {
        this.routerId = routerId;
        this.interfaceId = interfaceId;
        this.endpointId = endpointId;
    }

    @JsonProperty("routerId")
    public String getRouterId() {
        return routerId;
    }

    @JsonProperty("interfaceId")
    public String getInterfaceId() {
        return interfaceId;
    }

    @JsonProperty("endpointId")
    public String getEndpointId() {
        return endpointId;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Endpoint endpoint = (Endpoint) o;

        return Objects.equals(routerId, endpoint.routerId)
                && Objects.equals(interfaceId, endpoint.interfaceId)
                && Objects.equals(endpointId, endpoint.endpointId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routerId, interfaceId, endpointId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Endpoint {\n");
        sb.append("  routerId: ").append(toIndentedString(routerId))
                .append("\n");
        sb.append("  interfaceId: ").append(toIndentedString(interfaceId))
                .append("\n");
        sb.append("  endpointId: ").append(toIndentedString(endpointId))
                .append("\n");
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
