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

package org.onosproject.drivers.cop;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple port state notification.
 */
public class CopNotification {

    // the node and port address
    public final String nodeAddress, portAddress;
    // the state of the port
    public final PortState portState;

    /**
     * Create a COP notification.
     *
     * @param nodeAddress the node's address
     * @param portAddress the port's address
     * @param portState the port state
     */
    public CopNotification(@JsonProperty("nodeAddress") String nodeAddress,
                           @JsonProperty("portAddress") String portAddress,
                           @JsonProperty("portState") PortState portState) {
        this.nodeAddress = nodeAddress;
        this.portAddress = portAddress;
        this.portState = portState;
    }

    @Override
    public String toString() {
        return "CopNotification [nodeAddress=" + nodeAddress + ", portAddress="
                + portAddress + ", portState=" + portState + "]";
    }

    /**
     * Enumeration of port states.
     */
    public enum PortState {
        UP, DOWN;
    }
}
