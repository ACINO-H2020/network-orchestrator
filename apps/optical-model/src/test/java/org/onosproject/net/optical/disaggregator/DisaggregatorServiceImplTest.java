/*
 * Copyright 2017-present Open Networking Laboratory
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

package org.onosproject.net.optical.disaggregator;

import org.junit.Before;
import org.junit.Test;
import org.onlab.packet.ChassisId;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.DefaultDevice;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.SparseAnnotations;
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.optical.device.RoadmArchitecture;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.onosproject.net.AnnotationKeys.PORT_NAME;
import static org.onosproject.net.Device.Type.OTN;
import static org.onosproject.net.Device.Type.ROADM;
import static org.onosproject.net.Port.Type.OCH;
import static org.onosproject.net.Port.Type.ODUCLT;
import static org.onosproject.net.Port.Type.OMS;
import static org.onosproject.net.Port.Type.PACKET;
import static org.onosproject.net.optical.device.DisaggregatedType.BOTTOM_ROADM;
import static org.onosproject.net.optical.device.DisaggregatedType.DISAGGREGATED_TYPE_ANNOTATION;
import static org.onosproject.net.optical.device.DisaggregatedType.TOP_ROADM;
import static org.onosproject.net.optical.device.DisaggregatedType.TRANSPONDER;
import static org.onosproject.net.optical.device.DisaggregatedType.WSS_ROADM;
import static org.onosproject.net.optical.device.RoadmArchitecture.ARCH_ANNOTATION;
import static org.onosproject.net.optical.device.RoadmArchitecture.BOTTOM_ROADM_ID;
import static org.onosproject.net.optical.device.RoadmArchitecture.ORIGINAL_NODE_ANNOTATION;
import static org.onosproject.net.optical.device.RoadmArchitecture.TOP_ROADM_ID;
import static org.onosproject.net.optical.device.RoadmArchitecture.TRANSPONDER_ID;
import static org.onosproject.net.optical.device.RoadmArchitecture.WSS_ROADM_ID;
import static org.onosproject.net.optical.device.port.ClientPortMapper.NETWORK_PORT;

public class DisaggregatorServiceImplTest {

    //Device ids definition
    private final static DeviceId parentDevId = DeviceId.deviceId("rest:test1");
    private final static DeviceId trId1 = DeviceId.deviceId("rest:test1_" + TRANSPONDER_ID + "_0");
    private final static DeviceId trId2 = DeviceId.deviceId("rest:test1_" + TRANSPONDER_ID + "_1");
    private final static DeviceId bottomDevId = DeviceId.deviceId("rest:test1_" + BOTTOM_ROADM_ID);
    private final static DeviceId topDevId = DeviceId.deviceId("rest:test1_" + TOP_ROADM_ID);
    private final static DeviceId wssDevId = DeviceId.deviceId("rest:test1_" + WSS_ROADM_ID);
    private final static Set<DeviceId> dDisaggreatedIds = new HashSet<>();
    private final static Set<DeviceId> cdcDisaggreatedIds = new HashSet<>();


    //Device Descriptions definition
    private final static SparseAnnotations dLessAnn = DefaultAnnotations.builder()
            .set(ARCH_ANNOTATION, RoadmArchitecture.D.toString()).build();
    private final static DeviceDescription parentDDesc = new DefaultDeviceDescription(parentDevId.uri(), ROADM,
            "", "", "", "", new ChassisId(), false, dLessAnn);

    private final static SparseAnnotations cdcAnn = DefaultAnnotations.builder()
            .set(ARCH_ANNOTATION, RoadmArchitecture.D.toString()).build();
    private final static DeviceDescription parentCdcDesc = new DefaultDeviceDescription(parentDevId.uri(), ROADM,
            "", "", "", "", new ChassisId(), false, cdcAnn);

    //Device
    private final static SparseAnnotations trAnn = DefaultAnnotations.builder()
            .set(DISAGGREGATED_TYPE_ANNOTATION, TRANSPONDER.toString()).build();
    Device tr1 = new DefaultDevice(null, trId1, OTN, "", "", "", "", new ChassisId(), trAnn);
    Device tr2 = new DefaultDevice(null, trId2, OTN, "", "", "", "", new ChassisId(), trAnn);

    private final static SparseAnnotations wssAnn = DefaultAnnotations.builder()
            .set(DISAGGREGATED_TYPE_ANNOTATION, WSS_ROADM.toString()).build();
    Device wss = new DefaultDevice(null, wssDevId, ROADM, "", "", "", "", new ChassisId(), wssAnn);

    //Port Descriptions definition
    private final static SparseAnnotations annClient1 = DefaultAnnotations.builder().set(NETWORK_PORT, "test1").build();
    private final static PortDescription clientDesc1 = new DefaultPortDescription(PortNumber.portNumber(1), true,
            PACKET, 1000, annClient1);
    private final static SparseAnnotations annClient2 = DefaultAnnotations.builder().set(NETWORK_PORT, "test2").build();
    private final static PortDescription clientDesc2 = new DefaultPortDescription(PortNumber.portNumber(2), true,
            ODUCLT, 1000, annClient2);
    private final static PortDescription clientDesc3 = new DefaultPortDescription(PortNumber.portNumber(2), true,
            ODUCLT, 1000, annClient2);

    private final static SparseAnnotations annNet1 = DefaultAnnotations.builder().set(PORT_NAME, "test1").build();
    private final static PortDescription netDesc1 = new DefaultPortDescription(PortNumber.portNumber(3), true,
            OCH, 1000, annNet1);
    private final static SparseAnnotations annNet2 = DefaultAnnotations.builder().set(PORT_NAME, "test2").build();
    private final static PortDescription netDesc2 = new DefaultPortDescription(PortNumber.portNumber(4), true,
            OCH, 1000, annNet2);

    private final static PortDescription lineDesc1 = new DefaultPortDescription(PortNumber.portNumber(2), true,
            OMS, 1000, DefaultAnnotations.EMPTY);
    private final static PortDescription lineDesc2 = new DefaultPortDescription(PortNumber.portNumber(2), true,
            OMS, 1000, DefaultAnnotations.EMPTY);

    private final List<PortDescription> portDescs = new ArrayList<>();

    private DisaggregatorServiceImpl disaggregator = new DisaggregatorServiceImpl();
    DeviceService deviceService;

    @Before
    public void setUp() {

        deviceService = createMock(DeviceService.class);
        expect(deviceService.getDevice(trId1)).andReturn(tr1).anyTimes();
        expect(deviceService.getDevice(trId2)).andReturn(tr2).anyTimes();
        expect(deviceService.getDevice(wssDevId)).andReturn(wss).anyTimes();

        disaggregator.deviceService = deviceService;

        dDisaggreatedIds.add(trId1);
        dDisaggreatedIds.add(trId2);
        dDisaggreatedIds.add(topDevId);
        dDisaggreatedIds.add(bottomDevId);

        portDescs.add(clientDesc1);
        portDescs.add(clientDesc2);
        portDescs.add(clientDesc3);
        portDescs.add(netDesc1);
        portDescs.add(netDesc2);
        portDescs.add(lineDesc1);
        portDescs.add(lineDesc2);

        cdcDisaggreatedIds.add(trId1);
        cdcDisaggreatedIds.add(trId2);
        cdcDisaggreatedIds.add(wssDevId);

        replay(deviceService);

    }

    @Test
    public void disaggregatedDevIds() {
        Set<DeviceId> dIds = disaggregator.getDisaggregatedDevIds(parentDevId, RoadmArchitecture.D, portDescs);
        assertEquals(dDisaggreatedIds, dIds);

        Set<DeviceId> cdcIds = disaggregator.getDisaggregatedDevIds(parentDevId, RoadmArchitecture.CDC, portDescs);
        assertEquals(cdcDisaggreatedIds, cdcIds);
    }

    @Test
    public void disaggregatedTopRoadmDescription() {
        DeviceDescription topRoadmDesc = disaggregator.getDisaggregatedDevDescription(topDevId, parentDDesc);
        assertEquals(parentDevId.toString(), topRoadmDesc.annotations().value(ORIGINAL_NODE_ANNOTATION));
        assertEquals(TOP_ROADM.toString(), topRoadmDesc.annotations().value(DISAGGREGATED_TYPE_ANNOTATION));
        assertEquals(ROADM, topRoadmDesc.type());
    }

    @Test
    public void disaggregatedBottomRoadmDescription() {
        DeviceDescription topRoadmDesc = disaggregator.getDisaggregatedDevDescription(bottomDevId, parentDDesc);
        assertEquals(parentDevId.toString(), topRoadmDesc.annotations().value(ORIGINAL_NODE_ANNOTATION));
        assertEquals(BOTTOM_ROADM.toString(), topRoadmDesc.annotations().value(DISAGGREGATED_TYPE_ANNOTATION));
        assertEquals(ROADM, topRoadmDesc.type());
    }

    @Test
    public void disaggregatedWssRoadmDescription() {
        DeviceDescription wssRoadmDesc = disaggregator.getDisaggregatedDevDescription(wssDevId, parentCdcDesc);
        assertEquals(parentDevId.toString(), wssRoadmDesc.annotations().value(ORIGINAL_NODE_ANNOTATION));
        assertEquals(WSS_ROADM.toString(), wssRoadmDesc.annotations().value(DISAGGREGATED_TYPE_ANNOTATION));
        assertEquals(ROADM, wssRoadmDesc.type());
    }

    @Test
    public void disaggregatedTrRoadmDescription() {
        DeviceDescription topRoadmDesc = disaggregator.getDisaggregatedDevDescription(trId1, parentDDesc);
        assertEquals(parentDevId.toString(), topRoadmDesc.annotations().value(ORIGINAL_NODE_ANNOTATION));
        assertEquals(TRANSPONDER.toString(), topRoadmDesc.annotations().value(DISAGGREGATED_TYPE_ANNOTATION));
        assertEquals(OTN, topRoadmDesc.type());
    }

    @Test
    public void disaggregatedPortDescriptions() {
        //FIXME: hardcoded values
        assertEquals(2, disaggregator.getDisaggregatedPortDescriptions(trId1, portDescs).size());

        List<PortDescription> txp2Ports = disaggregator.getDisaggregatedPortDescriptions(trId2, portDescs);
        assertEquals(3, txp2Ports.size());
        verifyDescriptions(txp2Ports);

        List<PortDescription> wssPorts = disaggregator.getDisaggregatedPortDescriptions(wssDevId, portDescs);
        assertEquals(5, wssPorts.size());
        verifyDescriptions(wssPorts);;

    }

    //FIXME: need to verify not only the order, but also the type
    private void verifyDescriptions(List<PortDescription> portDescs) {
        portDescs.stream().sorted(Comparator.comparingLong(p -> p.portNumber().toLong())).collect(Collectors.toList());
        for (int i = 1; i > portDescs.size(); i++) {
            assertTrue(portDescs.get(i).portNumber().toLong() == i);
        }
    }

    @Test
    public void diaggregatedLinkDescriptions() {

    }


}