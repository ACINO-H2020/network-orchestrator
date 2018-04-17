/*
* Copyright 2016-present Open Networking Foundation
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

package org.onosproject.openstacknetworking.impl;

import com.google.common.base.Strings;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.Ethernet;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.DriverService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.instructions.ExtensionTreatment;
import org.onosproject.openstacknetworking.api.InstancePort;
import org.onosproject.openstacknetworking.api.InstancePortEvent;
import org.onosproject.openstacknetworking.api.InstancePortListener;
import org.onosproject.openstacknetworking.api.InstancePortService;
import org.onosproject.openstacknetworking.api.OpenstackFlowRuleService;
import org.onosproject.openstacknetworking.api.OpenstackNetworkEvent;
import org.onosproject.openstacknetworking.api.OpenstackNetworkListener;
import org.onosproject.openstacknetworking.api.OpenstackNetworkService;
import org.onosproject.openstacknetworking.api.OpenstackSecurityGroupService;
import org.onosproject.openstacknode.api.OpenstackNode;
import org.onosproject.openstacknode.api.OpenstackNodeService;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.NetworkType;
import org.openstack4j.model.network.Port;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.onlab.util.Tools.groupedThreads;
import static org.onosproject.openstacknetworking.api.Constants.ACL_TABLE;
import static org.onosproject.openstacknetworking.api.Constants.FORWARDING_TABLE;
import static org.onosproject.openstacknetworking.api.Constants.OPENSTACK_NETWORKING_APP_ID;
import static org.onosproject.openstacknetworking.api.Constants.PRIORITY_ADMIN_RULE;
import static org.onosproject.openstacknetworking.api.Constants.PRIORITY_SWITCHING_RULE;
import static org.onosproject.openstacknetworking.api.Constants.PRIORITY_TUNNEL_TAG_RULE;
import static org.onosproject.openstacknetworking.api.Constants.SRC_VNI_TABLE;
import static org.onosproject.openstacknetworking.api.OpenstackNetworkEvent.Type.OPENSTACK_NETWORK_CREATED;
import static org.onosproject.openstacknetworking.api.OpenstackNetworkEvent.Type.OPENSTACK_NETWORK_REMOVED;
import static org.onosproject.openstacknetworking.api.OpenstackNetworkEvent.Type.OPENSTACK_NETWORK_UPDATED;
import static org.onosproject.openstacknetworking.api.OpenstackNetworkEvent.Type.OPENSTACK_PORT_CREATED;
import static org.onosproject.openstacknetworking.api.OpenstackNetworkEvent.Type.OPENSTACK_PORT_REMOVED;
import static org.onosproject.openstacknetworking.api.OpenstackNetworkEvent.Type.OPENSTACK_PORT_UPDATED;
import static org.onosproject.openstacknetworking.impl.RulePopulatorUtil.buildExtension;
import static org.onosproject.openstacknode.api.OpenstackNode.NodeType.COMPUTE;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Populates switching flow rules on OVS for the basic connectivity among the
 * virtual instances in the same network.
 */
@Component(immediate = true)
public final class OpenstackSwitchingHandler {

    private final Logger log = getLogger(getClass());

    private static final String ERR_SET_FLOWS = "Failed to set flows for %s: ";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OpenstackFlowRuleService osFlowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected InstancePortService instancePortService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OpenstackNetworkService osNetworkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OpenstackNodeService osNodeService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DriverService driverService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OpenstackSecurityGroupService securityGroupService;

    private final ExecutorService eventExecutor = newSingleThreadExecutor(
            groupedThreads(this.getClass().getSimpleName(), "event-handler"));
    private final InstancePortListener instancePortListener = new InternalInstancePortListener();
    private final InternalOpenstackNetworkListener osNetworkListener =
            new InternalOpenstackNetworkListener();
    private ApplicationId appId;

    @Activate
    protected void activate() {
        appId = coreService.registerApplication(OPENSTACK_NETWORKING_APP_ID);
        instancePortService.addListener(instancePortListener);
        osNetworkService.addListener(osNetworkListener);

        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        instancePortService.removeListener(instancePortListener);
        osNetworkService.removeListener(osNetworkListener);
        eventExecutor.shutdown();

        log.info("Stopped");
    }

    private void setNetworkRules(InstancePort instPort, boolean install) {
        switch (osNetworkService.network(instPort.networkId()).getNetworkType()) {
            case VXLAN:
                setTunnelTagFlowRules(instPort, install);
                setForwardingRules(instPort, install);
                break;
            case VLAN:
                setVlanTagFlowRules(instPort, install);
                setForwardingRulesForVlan(instPort, install);
                break;
            default:
                break;
        }
    }

    private void setForwardingRules(InstancePort instPort, boolean install) {
        // switching rules for the instPorts in the same node
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4)
                .matchIPDst(instPort.ipAddress().toIpPrefix())
                .matchTunnelId(getVni(instPort))
                .build();

        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .setEthDst(instPort.macAddress())
                .setOutput(instPort.portNumber())
                .build();

        osFlowRuleService.setRule(
                appId,
                instPort.deviceId(),
                selector,
                treatment,
                PRIORITY_SWITCHING_RULE,
                FORWARDING_TABLE,
                install);

        // switching rules for the instPorts in the remote node
        OpenstackNode localNode = osNodeService.node(instPort.deviceId());
        if (localNode == null) {
            final String error = String.format("Cannot find openstack node for %s",
                    instPort.deviceId());
            throw new IllegalStateException(error);
        }
        osNodeService.completeNodes(COMPUTE).stream()
                .filter(remoteNode -> !remoteNode.intgBridge().equals(localNode.intgBridge()))
                .forEach(remoteNode -> {
                    TrafficTreatment treatmentToRemote = DefaultTrafficTreatment.builder()
                            .extension(buildExtension(
                                    deviceService,
                                    remoteNode.intgBridge(),
                                    localNode.dataIp().getIp4Address()),
                                    remoteNode.intgBridge())
                            .setOutput(remoteNode.tunnelPortNum())
                            .build();

                    osFlowRuleService.setRule(
                            appId,
                            remoteNode.intgBridge(),
                            selector,
                            treatmentToRemote,
                            PRIORITY_SWITCHING_RULE,
                            FORWARDING_TABLE,
                            install);
                });
    }

    private void setForwardingRulesForVlan(InstancePort instPort, boolean install) {
        // switching rules for the instPorts in the same node
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4)
                .matchIPDst(instPort.ipAddress().toIpPrefix())
                .matchVlanId(getVlanId(instPort))
                .build();

        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .popVlan()
                .setEthDst(instPort.macAddress())
                .setOutput(instPort.portNumber())
                .build();

        osFlowRuleService.setRule(
                appId,
                instPort.deviceId(),
                selector,
                treatment,
                PRIORITY_SWITCHING_RULE,
                FORWARDING_TABLE,
                install);

        // switching rules for the instPorts in the remote node
        osNodeService.completeNodes(COMPUTE).stream()
                .filter(remoteNode -> !remoteNode.intgBridge().equals(instPort.deviceId()) &&
                        remoteNode.vlanIntf() != null)
                .forEach(remoteNode -> {
                    TrafficTreatment treatmentToRemote = DefaultTrafficTreatment.builder()
                                    .setOutput(remoteNode.vlanPortNum())
                                    .build();

                    osFlowRuleService.setRule(
                            appId,
                            remoteNode.intgBridge(),
                            selector,
                            treatmentToRemote,
                            PRIORITY_SWITCHING_RULE,
                            FORWARDING_TABLE,
                            install);
                });
    }

    private void setTunnelTagFlowRules(InstancePort instPort, boolean install) {
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4)
                .matchInPort(instPort.portNumber())
                .build();

        // XXX All egress traffic needs to go through connection tracking module, which might hurt its performance.
        ExtensionTreatment ctTreatment =
                RulePopulatorUtil.niciraConnTrackTreatmentBuilder(driverService, instPort.deviceId())
                        .commit(true).build();

        TrafficTreatment.Builder tb = DefaultTrafficTreatment.builder()
                .setTunnelId(getVni(instPort))
                .transition(ACL_TABLE);

        if (securityGroupService.isSecurityGroupEnabled()) {
            tb.extension(ctTreatment, instPort.deviceId());
        }

        osFlowRuleService.setRule(
                appId,
                instPort.deviceId(),
                selector,
                tb.build(),
                PRIORITY_TUNNEL_TAG_RULE,
                SRC_VNI_TABLE,
                install);
    }

    private void setVlanTagFlowRules(InstancePort instPort, boolean install) {
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4)
                .matchInPort(instPort.portNumber())
                .build();

        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .pushVlan()
                .setVlanId(getVlanId(instPort))
                .transition(ACL_TABLE)
                .build();

        osFlowRuleService.setRule(
                appId,
                instPort.deviceId(),
                selector,
                treatment,
                PRIORITY_TUNNEL_TAG_RULE,
                SRC_VNI_TABLE,
                install);

    }

    private void setNetworkAdminRules(Network network, boolean install) {
        TrafficSelector selector;
        if (network.getNetworkType() == NetworkType.VXLAN) {

            selector = DefaultTrafficSelector.builder()
                    .matchTunnelId(Long.valueOf(network.getProviderSegID()))
                    .build();
        } else {
            selector = DefaultTrafficSelector.builder()
                    .matchVlanId(VlanId.vlanId(network.getProviderSegID()))
                    .build();
        }

        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .drop()
                .build();

        osNodeService.completeNodes().stream()
                .filter(osNode -> osNode.type() == COMPUTE)
                .forEach(osNode -> {
                    osFlowRuleService.setRule(
                            appId,
                            osNode.intgBridge(),
                            selector,
                            treatment,
                            PRIORITY_ADMIN_RULE,
                            ACL_TABLE,
                            install);
                });
    }

    private void setPortAdminRules(Port port, boolean install) {
        InstancePort instancePort = instancePortService.instancePort(MacAddress.valueOf(port.getMacAddress()));
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchInPort(instancePort.portNumber())
                .build();

        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .drop()
                .build();

        osFlowRuleService.setRule(
                appId,
                instancePort.deviceId(),
                selector,
                treatment,
                PRIORITY_ADMIN_RULE,
                SRC_VNI_TABLE,
                install);
    }

    private VlanId getVlanId(InstancePort instPort) {
        Network osNet = osNetworkService.network(instPort.networkId());

        if (osNet == null || Strings.isNullOrEmpty(osNet.getProviderSegID())) {
            final String error = String.format(
                    ERR_SET_FLOWS + "Failed to get VNI for %s",
                    instPort, osNet == null ? "<none>" : osNet.getName());
            throw new IllegalStateException(error);
        }

        return VlanId.vlanId(osNet.getProviderSegID());
    }


    private Long getVni(InstancePort instPort) {
        Network osNet = osNetworkService.network(instPort.networkId());
        if (osNet == null || Strings.isNullOrEmpty(osNet.getProviderSegID())) {
            final String error = String.format(
                    ERR_SET_FLOWS + "Failed to get VNI for %s",
                    instPort, osNet == null ? "<none>" : osNet.getName());
            throw new IllegalStateException(error);
        }
        return Long.valueOf(osNet.getProviderSegID());
    }

    private class InternalInstancePortListener implements InstancePortListener {

        @Override
        public boolean isRelevant(InstancePortEvent event) {
            InstancePort instPort = event.subject();
            return mastershipService.isLocalMaster(instPort.deviceId());
        }

        @Override
        public void event(InstancePortEvent event) {
            InstancePort instPort = event.subject();
            switch (event.type()) {
                case OPENSTACK_INSTANCE_PORT_UPDATED:
                case OPENSTACK_INSTANCE_PORT_DETECTED:
                    eventExecutor.execute(() -> {
                        log.info("Instance port detected MAC:{} IP:{}",
                                instPort.macAddress(),
                                instPort.ipAddress());
                        instPortDetected(event.subject());
                    });
                    break;
                case OPENSTACK_INSTANCE_PORT_VANISHED:
                    eventExecutor.execute(() -> {
                        log.info("Instance port vanished MAC:{} IP:{}",
                                instPort.macAddress(),
                                instPort.ipAddress());
                        instPortRemoved(event.subject());
                    });
                    break;
                default:
                    break;
            }
        }

        private void instPortDetected(InstancePort instPort) {
            setNetworkRules(instPort, true);
            // TODO add something else if needed
        }

        private void instPortRemoved(InstancePort instPort) {
            setNetworkRules(instPort, false);
            // TODO add something else if needed
        }
    }

    private class InternalOpenstackNetworkListener implements OpenstackNetworkListener {

        @Override
        public boolean isRelevant(OpenstackNetworkEvent event) {
            return !(event.subject() == null && event.port() == null);
        }

        @Override
        public void event(OpenstackNetworkEvent event) {

            if ((event.type() == OPENSTACK_NETWORK_CREATED ||
                    event.type() == OPENSTACK_NETWORK_UPDATED) && !event.subject().isAdminStateUp()) {
                setNetworkAdminRules(event.subject(), true);
            } else if ((event.type() == OPENSTACK_NETWORK_UPDATED && event.subject().isAdminStateUp()) ||
                    (event.type() == OPENSTACK_NETWORK_REMOVED && !event.subject().isAdminStateUp())) {
                setNetworkAdminRules(event.subject(), false);
            }

            if ((event.type() == OPENSTACK_PORT_CREATED ||
                    event.type() == OPENSTACK_PORT_UPDATED) && !event.port().isAdminStateUp()) {
                setPortAdminRules(event.port(), true);
            } else if ((event.type() == OPENSTACK_PORT_UPDATED && event.port().isAdminStateUp()) ||
                    (event.type() == OPENSTACK_PORT_REMOVED && !event.port().isAdminStateUp())) {
                setPortAdminRules(event.port(), false);
            }
        }
    }
}
