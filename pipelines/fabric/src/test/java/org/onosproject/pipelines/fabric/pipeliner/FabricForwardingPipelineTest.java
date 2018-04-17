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

package org.onosproject.pipelines.fabric.pipeliner;

import org.junit.Ignore;
import org.junit.Test;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.TpPort;
import org.onlab.packet.UDP;
import org.onlab.util.ImmutableByteSequence;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.group.GroupDescription;
import org.onosproject.net.pi.model.PiTableId;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;
import org.onosproject.pipelines.fabric.FabricConstants;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for fabric.p4 pipeline forwarding control block.
 */
public class FabricForwardingPipelineTest extends FabricPipelinerTest {

    /**
     * Test versatile flag of forwarding objective with ARP match.
     */
    @Test
    public void testAclArp() {
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .wipeDeferred()
                .punt()
                .build();
        // ARP
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_ARP)
                .build();
        ForwardingObjective fwd = DefaultForwardingObjective.builder()
                .withSelector(selector)
                .withPriority(PRIORITY)
                .fromApp(APP_ID)
                .makePermanent()
                .withFlag(ForwardingObjective.Flag.VERSATILE)
                .withTreatment(treatment)
                .add();

        PipelinerTranslationResult result = pipeliner.pipelinerForward.forward(fwd);

        List<FlowRule> flowRulesInstalled = (List<FlowRule>) result.flowRules();
        List<GroupDescription> groupsInstalled = (List<GroupDescription>) result.groups();
        assertEquals(1, flowRulesInstalled.size());
        assertTrue(groupsInstalled.isEmpty());

        FlowRule actualFlowRule = flowRulesInstalled.get(0);
        FlowRule expectedFlowRule = DefaultFlowRule.builder()
                .forDevice(DEVICE_ID)
                .forTable(FabricConstants.TBL_ACL_ID)
                .withPriority(PRIORITY)
                .makePermanent()
                .withSelector(selector)
                .withTreatment(treatment)
                .fromApp(APP_ID)
                .build();

        assertTrue(expectedFlowRule.exactMatch(actualFlowRule));
    }

    /**
     * Test versatile flag of forwarding objective with DHCP match.
     */
    @Test
    public void testAclDhcp() {
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .wipeDeferred()
                .punt()
                .build();
        // DHCP
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4)
                .matchIPProtocol(IPv4.PROTOCOL_UDP)
                .matchUdpSrc(TpPort.tpPort(UDP.DHCP_CLIENT_PORT))
                .matchUdpDst(TpPort.tpPort(UDP.DHCP_SERVER_PORT))
                .build();
        ForwardingObjective fwd = DefaultForwardingObjective.builder()
                .withSelector(selector)
                .withPriority(PRIORITY)
                .fromApp(APP_ID)
                .makePermanent()
                .withFlag(ForwardingObjective.Flag.VERSATILE)
                .withTreatment(treatment)
                .add();

        PipelinerTranslationResult result = pipeliner.pipelinerForward.forward(fwd);

        List<FlowRule> flowRulesInstalled = (List<FlowRule>) result.flowRules();
        List<GroupDescription> groupsInstalled = (List<GroupDescription>) result.groups();
        assertEquals(1, flowRulesInstalled.size());
        assertTrue(groupsInstalled.isEmpty());

        FlowRule actualFlowRule = flowRulesInstalled.get(0);
        FlowRule expectedFlowRule = DefaultFlowRule.builder()
                .forDevice(DEVICE_ID)
                .forTable(FabricConstants.TBL_ACL_ID)
                .withPriority(PRIORITY)
                .makePermanent()
                .withSelector(selector)
                .withTreatment(treatment)
                .fromApp(APP_ID)
                .build();

        assertTrue(expectedFlowRule.exactMatch(actualFlowRule));
    }

    /**
     * Test programming L2 unicast rule to bridging table.
     */
    @Test
    public void testL2Unicast() {
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchVlanId(VLAN_100)
                .matchEthDst(HOST_MAC)
                .build();
        testSpecificForward(FabricConstants.TBL_BRIDGING_ID, selector, selector, NEXT_ID_1);
    }

    @Test
    public void testL2Broadcast() {
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchVlanId(VLAN_100)
                .build();
        testSpecificForward(FabricConstants.TBL_BRIDGING_ID, selector, selector, NEXT_ID_1);
    }

    @Test
    @Ignore
    public void testIPv4Unicast() {
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4)
                .matchIPDst(IPV4_UNICAST_ADDR)
                .build();
        TrafficSelector expectedSelector = DefaultTrafficSelector.builder()
                .matchIPDst(IPV4_UNICAST_ADDR)
                .build();
        testSpecificForward(FabricConstants.TBL_UNICAST_V4_ID, expectedSelector, selector, NEXT_ID_1);
    }

    @Test
    @Ignore
    public void testIPv4Multicast() {
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4)
                .matchVlanId(VLAN_100)
                .matchIPDst(IPV4_MCAST_ADDR)
                .build();
        TrafficSelector expectedSelector = DefaultTrafficSelector.builder()
                .matchIPDst(IPV4_MCAST_ADDR)
                .build();
        testSpecificForward(FabricConstants.TBL_MULTICAST_V4_ID, expectedSelector, selector, NEXT_ID_1);
    }

    @Test
    @Ignore
    public void testIPv6Unicast() {
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV6)
                .matchIPDst(IPV6_UNICAST_ADDR)
                .build();
        TrafficSelector expectedSelector = DefaultTrafficSelector.builder()
                .matchIPDst(IPV6_UNICAST_ADDR)
                .build();
        testSpecificForward(FabricConstants.TBL_UNICAST_V6_ID, expectedSelector, selector, NEXT_ID_1);
    }

    @Test
    @Ignore
    public void testIPv6Multicast() {
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV6)
                .matchVlanId(VLAN_100)
                .matchIPDst(IPV6_MCAST_ADDR)
                .build();
        TrafficSelector expectedSelector = DefaultTrafficSelector.builder()
                .matchIPDst(IPV6_MCAST_ADDR)
                .build();
        testSpecificForward(FabricConstants.TBL_MULTICAST_V6_ID, expectedSelector, selector, NEXT_ID_1);
    }

    @Test
    @Ignore
    public void testMpls() {

    }

    private void testSpecificForward(PiTableId expectedTableId, TrafficSelector expectedSelector,
                                     TrafficSelector selector, Integer nextId) {
        ForwardingObjective fwd = DefaultForwardingObjective.builder()
                .withSelector(selector)
                .withPriority(PRIORITY)
                .fromApp(APP_ID)
                .makePermanent()
                .withFlag(ForwardingObjective.Flag.SPECIFIC)
                .nextStep(nextId)
                .add();

        PipelinerTranslationResult result = pipeliner.pipelinerForward.forward(fwd);

        List<FlowRule> flowRulesInstalled = (List<FlowRule>) result.flowRules();
        List<GroupDescription> groupsInstalled = (List<GroupDescription>) result.groups();
        assertEquals(1, flowRulesInstalled.size());
        assertTrue(groupsInstalled.isEmpty());

        FlowRule actualFlowRule = flowRulesInstalled.get(0);
        PiActionParam nextIdParam = new PiActionParam(FabricConstants.ACT_PRM_NEXT_ID_ID,
                                                      ImmutableByteSequence.copyFrom(nextId.byteValue()));
        PiAction setNextIdAction = PiAction.builder()
                .withId(FabricConstants.ACT_SET_NEXT_ID_ID)
                .withParameter(nextIdParam)
                .build();
        TrafficTreatment setNextIdTreatment = DefaultTrafficTreatment.builder()
                .piTableAction(setNextIdAction)
                .build();

        FlowRule expectedFlowRule = DefaultFlowRule.builder()
                .forDevice(DEVICE_ID)
                .forTable(expectedTableId)
                .withPriority(PRIORITY)
                .makePermanent()
                .withSelector(expectedSelector)
                .withTreatment(setNextIdTreatment)
                .fromApp(APP_ID)
                .build();

        assertTrue(expectedFlowRule.exactMatch(actualFlowRule));
    }
}
