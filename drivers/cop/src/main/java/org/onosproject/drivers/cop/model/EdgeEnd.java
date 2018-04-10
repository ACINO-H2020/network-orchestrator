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
 * Edge end representation in the COP protocol.
 */
public class EdgeEnd {

    /**
     * Enumeration describing the switching capabilities.
     */
    public enum SwitchingCapEnum {
        /** lambda switching. */
        LSC, /** packet switching. */
        PSC
    }

    private final SwitchingCapEnum switchingCap;
    private final String edgeEndId;
    private final String name;
    private final String peerNodeId;

    /**
     * Create a new edge end.
     *
     * @param switchingCap the switching capabilities
     * @param edgeEndId the edge end's identifier
     * @param name the edge end's name
     * @param peerNodeId the peer node's identifier
     */
    public EdgeEnd(@JsonProperty("switchingCap") SwitchingCapEnum switchingCap,
                   @JsonProperty("edgeEndId") String edgeEndId,
                   @JsonProperty("name") String name,
                   @JsonProperty("peerNodeId") String peerNodeId) {
        this.switchingCap = switchingCap;
        this.edgeEndId = edgeEndId;
        this.name = name;
        this.peerNodeId = peerNodeId;
    }

    /**
     * @return the switching capabilities
     */
    @JsonProperty("switchingCap")
    public SwitchingCapEnum getSwitchingCap() {
        return switchingCap;
    }

    /**
     * @return the edge end's identifier
     */
    @JsonProperty("edgeEndId")
    public String getEdgeEndId() {
        return edgeEndId;
    }

    /**
     * @return the edge end's name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * @return the peer node's identifier
     */
    @JsonProperty("peerNodeId")
    public String getPeerNodeId() {
        return peerNodeId;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EdgeEnd edgeEnd = (EdgeEnd) o;

        return Objects.equals(switchingCap, edgeEnd.switchingCap)
                && Objects.equals(edgeEndId, edgeEnd.edgeEndId)
                && Objects.equals(name, edgeEnd.name)
                && Objects.equals(peerNodeId, edgeEnd.peerNodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(switchingCap, edgeEndId, name, peerNodeId);
    }

    @Override
    public String toString() {
        return "EdgeEnd [switchingCap=" + switchingCap + ", edgeEndId="
                + edgeEndId + ", name=" + name + ", peerNodeId=" + peerNodeId
                + "]";
    }

}
