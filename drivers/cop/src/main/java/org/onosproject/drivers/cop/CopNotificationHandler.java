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

import java.io.IOException;
import java.util.List;

import org.onosproject.drivers.cop.CopNotification.PortState;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.protocol.restproxy.NotificationHandler;
import org.onosproject.protocol.restproxy.PortEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A notification handler implementation for the COP protocol.
 */
public class CopNotificationHandler extends AbstractHandlerBehaviour
        implements NotificationHandler {

    // logger
    private static final Logger LOG = LoggerFactory
            .getLogger(CopNotificationHandler.class);
    // mapper for JSON objects
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public PortEvent translateNotification(String notification) {
        try {
            LOG.debug("Received notification: {}", notification);
            // map the notification to a COP notification
            CopNotification copNotification = mapper.readValue(notification,
                    CopNotification.class);
            // create a device ID to query the device service
            DeviceId deviceId = DeviceId
                    .deviceId(CopTopologyDiscovery.DEVICE_URI_PREFIX
                            + copNotification.nodeAddress);
            // get device service
            DeviceService deviceService = handler().get(DeviceService.class);
            // retrieve ports
            List<Port> ports = deviceService.getPorts(deviceId);
            // go through ports that are available on that devices
            for (Port port : ports) {
                // find the affected port
                if (copNotification.portAddress.equals(
                        port.annotations().value(AnnotationKeys.PORT_NAME))) {
                    // create the updated port
                    PortDescription updatedPort = new DefaultPortDescription(
                            port.number(),
                            PortState.UP.equals(copNotification.portState),
                            port.type(), port.portSpeed(),
                            DefaultAnnotations.builder()
                                    .putAll(port.annotations()).build());
                    // return the device event
                    return new PortEvent(deviceId, updatedPort);
                }
            }
        } catch (IOException e) {
            LOG.warn("Failed to parse the notification: {}", e);
        }
        // no matching port
        return null;
    }

}
