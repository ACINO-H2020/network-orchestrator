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

package org.onosproject.orchestrator.intent;

import org.junit.Test;
import org.onlab.graph.ScalarWeight;
import org.onlab.packet.ChassisId;
import org.onosproject.net.Annotations;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.DefaultDevice;
import org.onosproject.net.DefaultPath;
import org.onosproject.net.DefaultPort;
import org.onosproject.net.Device;
import org.onosproject.net.Element;
import org.onosproject.net.Host;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.onosproject.net.NetTestTools.*;

public class AciIntentCompilerTest {
    private static final String ROADM1 = "roadm:1";
    private static final String ROADM2 = "roadm:2";
    private static final String SWITCH1 = "switch:1";
    private static final String SWITCH2 = "switch:2";
    private static final Annotations ENCRYPTION =
            DefaultAnnotations.builder().set("encryption", "true").build();
    // default infrastructure devices
    private Device roadm1 = encryptedDevice(ROADM1, Device.Type.ROADM, true);
    private Device switch1 = encryptedDevice(SWITCH1, Device.Type.SWITCH, true);
    private Port r1p1 = encryptedPort(roadm1, 1, true);
    private Port r1p2 = encryptedPort(roadm1, 2, false);
    private Port s1p1 = encryptedPort(switch1, 1, true);
    private Port s1p2 = encryptedPort(switch1, 2, false);
    private ConnectPoint roadm1p1 = connectPoint(ROADM1, 1);
    private ConnectPoint roadm1p2 = connectPoint(ROADM1, 2);
    private ConnectPoint switch1p1 = connectPoint(SWITCH1, 1);
    private ConnectPoint switch1p2 = connectPoint(SWITCH1, 2);
    private Device roadm2 = encryptedDevice(ROADM2, Device.Type.ROADM, true);
    private Device switch2 = encryptedDevice(SWITCH2, Device.Type.SWITCH, true);
    private Port r2p1 = encryptedPort(roadm2, 1, true);
    private Port r2p2 = encryptedPort(roadm2, 2, false);
    private Port s2p1 = encryptedPort(switch2, 1, true);
    private Port s2p2 = encryptedPort(switch2, 2, false);
    private ConnectPoint roadm2p1 = connectPoint(ROADM2, 1);
    private ConnectPoint roadm2p2 = connectPoint(ROADM2, 2);
    private ConnectPoint switch2p1 = connectPoint(SWITCH2, 1);
    private ConnectPoint switch2p2 = connectPoint(SWITCH2, 2);
    private Host host1 = host("00:00:00:00:00:00:01", ROADM1);
    private ConnectPoint host1c = new ConnectPoint(host1.id(), PortNumber.ANY);
    private Host host2 = host("00:00:00:00:00:00:02", ROADM2);
    private ConnectPoint host2c = new ConnectPoint(host2.id(), PortNumber.ANY);
    private AciIntentCompiler aci;
    private DeviceService deviceService;

    public static Device encryptedDevice(String id, Device.Type type,
                                         boolean encrypted) {
        return new DefaultDevice(PID, did(id), type, "mfg", "1.0", "1.1",
                                 "1234", new ChassisId(),
                                 encrypted ? ENCRYPTION : DefaultAnnotations.EMPTY);
    }

    public static Port encryptedPort(Element element, long portNumber,
                                     boolean encrypted) {
        return new DefaultPort(element, PortNumber.portNumber(portNumber), true,
                               encrypted ? ENCRYPTION : DefaultAnnotations.EMPTY);
    }

    private Path createRoadmPath() {
        List<Link> links = new ArrayList<>();
        links.add(link(host1c, roadm1p1));
        links.add(link(roadm1p2, roadm2p2));
        links.add(link(roadm2p1, host2c));
        return new DefaultPath(PID, links, new ScalarWeight(links.size()));
    }

    private Path createSwitchPath() {
        List<Link> links = new ArrayList<>();
        links.add(link(host1c, switch1p1));
        links.add(link(switch1p2, switch2p2));
        links.add(link(switch2p1, host2c));
        return new DefaultPath(PID, links, new ScalarWeight(links.size()));
    }

    @Test
    public void isMacEncrypted() throws Exception {
        assertThat("MAC encrypted path",
                   aci.isMacEncrypted(createSwitchPath()));
    }

    @Test
    public void isWdmEncrypted() throws Exception {
        assertThat("WDM encrypted path", aci.isWdmEncrypted(createRoadmPath()));
    }

    @org.junit.Before
    public void setUp() throws Exception {
        aci = new AciIntentCompiler();
        deviceService = createMock(DeviceService.class);
        expect(deviceService.getDevice(roadm1.id())).andReturn(roadm1)
                .anyTimes();
        expect(deviceService.getPort(roadm1.id(), r1p1.number()))
                .andReturn(r1p1).anyTimes();
        expect(deviceService.getPort(roadm1.id(), r1p2.number()))
                .andReturn(r1p2).anyTimes();
        expect(deviceService.getDevice(roadm2.id())).andReturn((roadm2))
                .anyTimes();
        expect(deviceService.getPort(roadm2.id(), r2p1.number()))
                .andReturn(r2p1).anyTimes();
        expect(deviceService.getPort(roadm2.id(), r2p2.number()))
                .andReturn(r2p2).anyTimes();
        expect(deviceService.getDevice(switch1.id())).andReturn(switch1)
                .anyTimes();
        expect(deviceService.getPort(switch1.id(), s1p1.number()))
                .andReturn(s1p1).anyTimes();
        expect(deviceService.getPort(switch1.id(), s1p2.number()))
                .andReturn(s1p2).anyTimes();
        expect(deviceService.getDevice(switch2.id())).andReturn((switch2))
                .anyTimes();
        expect(deviceService.getPort(switch2.id(), s2p1.number()))
                .andReturn(s2p1).anyTimes();
        expect(deviceService.getPort(switch2.id(), s2p2.number()))
                .andReturn(s2p2).anyTimes();
        aci.deviceService = deviceService;

        replay(deviceService);
    }

    @Test
    public void isPortEncrypted() throws Exception {
        assertThat("Encrypted port.", aci.isPortEncrypted(roadm1p1));
        assertThat("Unencrypted port.", !aci.isPortEncrypted(roadm1p2));
        assertThat("Host port", !aci.isPortEncrypted(host1c));
    }

}
