/*
 * Copyright 2017-present Open Networking Foundation
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
package org.onosproject.openstacknode.impl;

import org.onlab.packet.ChassisId;
import org.onlab.packet.IpAddress;
import org.onosproject.net.DefaultDevice;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.openstacknode.api.NodeState;
import org.onosproject.openstacknode.api.OpenstackNode;
import org.onosproject.openstacknode.api.OpenstackNode.NodeType;

import static org.onosproject.net.Device.Type.SWITCH;

/**
 * Provides a set of test OpenstackNode parameters for use with OpenstackNode related tests.
 */
abstract class OpenstackNodeTest {

    protected static Device createDevice(long devIdNum) {
        return new DefaultDevice(new ProviderId("of", "foo"),
                DeviceId.deviceId(String.format("of:%016d", devIdNum)),
                SWITCH,
                "manufacturer",
                "hwVersion",
                "swVersion",
                "serialNumber",
                new ChassisId(1));
    }

    protected static OpenstackNode createNode(String hostname, NodeType type,
                                              Device intgBridge, IpAddress ipAddr,
                                              NodeState state) {
        return org.onosproject.openstacknode.impl.DefaultOpenstackNode.builder()
                .hostname(hostname)
                .type(type)
                .intgBridge(intgBridge.id())
                .managementIp(ipAddr)
                .dataIp(ipAddr)
                .state(state)
                .build();
    }

    protected static OpenstackNode createNode(String hostname, NodeType type,
                                              Device intgBridge, Device routerBridge,
                                              IpAddress ipAddr, NodeState state) {
        return org.onosproject.openstacknode.impl.DefaultOpenstackNode.builder()
                .hostname(hostname)
                .type(type)
                .intgBridge(intgBridge.id())
                .routerBridge(routerBridge.id())
                .managementIp(ipAddr)
                .dataIp(ipAddr)
                .state(state)
                .build();
    }
}
