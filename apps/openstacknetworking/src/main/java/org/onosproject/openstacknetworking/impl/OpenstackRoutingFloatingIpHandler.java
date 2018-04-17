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
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.LeadershipService;
import org.onosproject.cluster.NodeId;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.openstacknetworking.api.Constants;
import org.onosproject.openstacknetworking.api.InstancePort;
import org.onosproject.openstacknetworking.api.InstancePortService;
import org.onosproject.openstacknetworking.api.OpenstackFlowRuleService;
import org.onosproject.openstacknetworking.api.OpenstackNetworkService;
import org.onosproject.openstacknetworking.api.OpenstackRouterEvent;
import org.onosproject.openstacknetworking.api.OpenstackRouterListener;
import org.onosproject.openstacknetworking.api.OpenstackRouterService;
import org.onosproject.openstacknode.api.OpenstackNode;
import org.onosproject.openstacknode.api.OpenstackNodeEvent;
import org.onosproject.openstacknode.api.OpenstackNodeListener;
import org.onosproject.openstacknode.api.OpenstackNodeService;
import org.openstack4j.model.network.NetFloatingIP;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.NetworkType;
import org.openstack4j.model.network.Port;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.onlab.util.Tools.groupedThreads;
import static org.onosproject.openstacknetworking.api.Constants.GW_COMMON_TABLE;
import static org.onosproject.openstacknetworking.api.Constants.OPENSTACK_NETWORKING_APP_ID;
import static org.onosproject.openstacknetworking.api.Constants.PRIORITY_FLOATING_EXTERNAL;
import static org.onosproject.openstacknetworking.api.Constants.PRIORITY_FLOATING_INTERNAL;
import static org.onosproject.openstacknetworking.impl.RulePopulatorUtil.buildExtension;
import static org.onosproject.openstacknode.api.OpenstackNode.NodeType.GATEWAY;

/**
 * Handles OpenStack floating IP events.
 */
@Component(immediate = true)
public class OpenstackRoutingFloatingIpHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String ERR_FLOW = "Failed set flows for floating IP %s: ";
    private static final String ERR_UNSUPPORTED_NET_TYPE = "Unsupported network type";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LeadershipService leadershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterService clusterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OpenstackNodeService osNodeService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected InstancePortService instancePortService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OpenstackRouterService osRouterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OpenstackNetworkService osNetworkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OpenstackFlowRuleService osFlowRuleService;

    private final ExecutorService eventExecutor = newSingleThreadExecutor(
            groupedThreads(this.getClass().getSimpleName(), "event-handler", log));
    private final OpenstackRouterListener floatingIpLisener = new InternalFloatingIpListener();
    private final OpenstackNodeListener osNodeListener = new InternalNodeListener();

    private ApplicationId appId;
    private NodeId localNodeId;

    @Activate
    protected void activate() {
        appId = coreService.registerApplication(OPENSTACK_NETWORKING_APP_ID);
        localNodeId = clusterService.getLocalNode().id();
        leadershipService.runForLeadership(appId.name());
        osRouterService.addListener(floatingIpLisener);
        osNodeService.addListener(osNodeListener);

        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        osNodeService.removeListener(osNodeListener);
        osRouterService.removeListener(floatingIpLisener);
        leadershipService.withdraw(appId.name());
        eventExecutor.shutdown();

        log.info("Stopped");
    }

    private void setFloatingIpRules(NetFloatingIP floatingIp, Port osPort,
                                    boolean install) {
        Network osNet = osNetworkService.network(osPort.getNetworkId());
        if (osNet == null) {
            final String error = String.format(ERR_FLOW + "no network(%s) exists",
                    floatingIp.getFloatingIpAddress(),
                    osPort.getNetworkId());
            throw new IllegalStateException(error);
        }

        MacAddress srcMac = MacAddress.valueOf(osPort.getMacAddress());
        InstancePort instPort = instancePortService.instancePort(srcMac);
        if (instPort == null) {
            final String error = String.format(ERR_FLOW + "no host(MAC:%s) found",
                    floatingIp.getFloatingIpAddress(), srcMac);
            throw new IllegalStateException(error);
        }

        setDownstreamRules(floatingIp, osNet, instPort, install);
        setUpstreamRules(floatingIp, osNet, instPort, install);
    }

    private void setDownstreamRules(NetFloatingIP floatingIp, Network osNet,
                                    InstancePort instPort, boolean install) {
        OpenstackNode cNode = osNodeService.node(instPort.deviceId());
        if (cNode == null) {
            final String error = String.format("Cannot find openstack node for device %s",
                    instPort.deviceId());
            throw new IllegalStateException(error);
        }
        if (osNet.getNetworkType() == NetworkType.VXLAN && cNode.dataIp() == null) {
            final String error = String.format(ERR_FLOW +
                    "VXLAN mode is not ready for %s", floatingIp, cNode.hostname());
            throw new IllegalStateException(error);
        }
        if (osNet.getNetworkType() == NetworkType.VLAN && cNode.vlanIntf() == null) {
            final String error = String.format(ERR_FLOW +
                    "VLAN mode is not ready for %s", floatingIp, cNode.hostname());
            throw new IllegalStateException(error);
        }

        IpAddress floating = IpAddress.valueOf(floatingIp.getFloatingIpAddress());
        TrafficSelector externalSelector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4)
                .matchIPDst(floating.toIpPrefix())
                .build();

        osNodeService.completeNodes(GATEWAY).forEach(gNode -> {
            TrafficTreatment.Builder externalBuilder = DefaultTrafficTreatment.builder()
                    .setEthSrc(Constants.DEFAULT_GATEWAY_MAC)
                    .setEthDst(instPort.macAddress())
                    .setIpDst(instPort.ipAddress().getIp4Address());

            switch (osNet.getNetworkType()) {
                case VXLAN:
                    externalBuilder.setTunnelId(Long.valueOf(osNet.getProviderSegID()))
                            .extension(buildExtension(
                                    deviceService,
                                    gNode.intgBridge(),
                                    cNode.dataIp().getIp4Address()),
                                    gNode.intgBridge())
                            .setOutput(gNode.tunnelPortNum());
                    break;
                case VLAN:
                    externalBuilder.pushVlan()
                            .setVlanId(VlanId.vlanId(osNet.getProviderSegID()))
                            .setOutput(gNode.vlanPortNum());
                    break;
                default:
                    final String error = String.format(ERR_UNSUPPORTED_NET_TYPE + "%s",
                            osNet.getNetworkType());
                    throw new IllegalStateException(error);
            }

            osFlowRuleService.setRule(
                    appId,
                    gNode.intgBridge(),
                    externalSelector,
                    externalBuilder.build(),
                    PRIORITY_FLOATING_EXTERNAL,
                    GW_COMMON_TABLE,
                    install);

            // access from one VM to the others via floating IP
            TrafficSelector internalSelector = DefaultTrafficSelector.builder()
                    .matchEthType(Ethernet.TYPE_IPV4)
                    .matchIPDst(floating.toIpPrefix())
                    .matchInPort(gNode.tunnelPortNum())
                    .build();

            TrafficTreatment.Builder internalBuilder = DefaultTrafficTreatment.builder()
                    .setEthSrc(Constants.DEFAULT_GATEWAY_MAC)
                    .setEthDst(instPort.macAddress())
                    .setIpDst(instPort.ipAddress().getIp4Address());

            switch (osNet.getNetworkType()) {
                case VXLAN:
                    internalBuilder.setTunnelId(Long.valueOf(osNet.getProviderSegID()))
                            .extension(buildExtension(
                                    deviceService,
                                    gNode.intgBridge(),
                                    cNode.dataIp().getIp4Address()),
                                    gNode.intgBridge())
                            .setOutput(PortNumber.IN_PORT);
                    break;
                case VLAN:
                    internalBuilder.pushVlan()
                            .setVlanId(VlanId.vlanId(osNet.getProviderSegID()))
                            .setOutput(PortNumber.IN_PORT);
                    break;
                default:
                    final String error = String.format(ERR_UNSUPPORTED_NET_TYPE + "%s",
                            osNet.getNetworkType());
                    throw new IllegalStateException(error);
            }

            osFlowRuleService.setRule(
                    appId,
                    gNode.intgBridge(),
                    internalSelector,
                    internalBuilder.build(),
                    PRIORITY_FLOATING_INTERNAL,
                    GW_COMMON_TABLE,
                    install);
        });
    }

    private void setUpstreamRules(NetFloatingIP floatingIp, Network osNet,
                                  InstancePort instPort, boolean install) {
        IpAddress floating = IpAddress.valueOf(floatingIp.getFloatingIpAddress());
        TrafficSelector.Builder sBuilder = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4)
                .matchIPSrc(instPort.ipAddress().toIpPrefix());

        switch (osNet.getNetworkType()) {
            case VXLAN:
                sBuilder.matchTunnelId(Long.valueOf(osNet.getProviderSegID()));
                break;
            case VLAN:
                sBuilder.matchVlanId(VlanId.vlanId(osNet.getProviderSegID()));
                break;
            default:
                final String error = String.format(ERR_UNSUPPORTED_NET_TYPE + "%s",
                        osNet.getNetworkType());
                throw new IllegalStateException(error);
        }

        osNodeService.completeNodes(GATEWAY).forEach(gNode -> {
            TrafficTreatment.Builder tBuilder = DefaultTrafficTreatment.builder()
                    .setIpSrc(floating.getIp4Address())
                    .setEthSrc(Constants.DEFAULT_GATEWAY_MAC)
                    .setEthDst(Constants.DEFAULT_EXTERNAL_ROUTER_MAC);

            if (osNet.getNetworkType().equals(NetworkType.VLAN)) {
                tBuilder.popVlan();
            }

            osFlowRuleService.setRule(
                    appId,
                    gNode.intgBridge(),
                    sBuilder.build(),
                    tBuilder.setOutput(gNode.patchPortNum()).build(),
                    PRIORITY_FLOATING_EXTERNAL,
                    GW_COMMON_TABLE,
                    install);
        });
    }

    private class InternalFloatingIpListener implements OpenstackRouterListener {

        @Override
        public boolean isRelevant(OpenstackRouterEvent event) {
            // do not allow to proceed without leadership
            NodeId leader = leadershipService.getLeader(appId.name());
            if (!Objects.equals(localNodeId, leader)) {
                return false;
            }
            return event.floatingIp() != null;
        }

        @Override
        public void event(OpenstackRouterEvent event) {
            switch (event.type()) {
                case OPENSTACK_FLOATING_IP_ASSOCIATED:
                    eventExecutor.execute(() -> {
                        NetFloatingIP osFip = event.floatingIp();
                        associateFloatingIp(osFip);
                        log.info("Associated floating IP {}:{}",
                                osFip.getFloatingIpAddress(), osFip.getFixedIpAddress());
                    });
                    break;
                case OPENSTACK_FLOATING_IP_DISASSOCIATED:
                    eventExecutor.execute(() -> {
                        NetFloatingIP osFip = event.floatingIp();
                        disassociateFloatingIp(osFip, event.portId());
                        log.info("Disassociated floating IP {}:{}",
                                osFip.getFloatingIpAddress(), osFip.getFixedIpAddress());
                    });
                    break;
                case OPENSTACK_FLOATING_IP_REMOVED:
                    eventExecutor.execute(() -> {
                        NetFloatingIP osFip = event.floatingIp();
                        if (!Strings.isNullOrEmpty(osFip.getPortId())) {
                            disassociateFloatingIp(osFip, osFip.getPortId());
                        }
                        log.info("Removed floating IP {}", osFip.getFloatingIpAddress());
                    });
                    break;
                case OPENSTACK_FLOATING_IP_CREATED:
                    eventExecutor.execute(() -> {
                        NetFloatingIP osFip = event.floatingIp();
                        if (!Strings.isNullOrEmpty(osFip.getPortId())) {
                            associateFloatingIp(event.floatingIp());
                        }
                        log.info("Created floating IP {}", osFip.getFloatingIpAddress());
                    });
                    break;
                case OPENSTACK_FLOATING_IP_UPDATED:
                case OPENSTACK_ROUTER_CREATED:
                case OPENSTACK_ROUTER_UPDATED:
                case OPENSTACK_ROUTER_REMOVED:
                case OPENSTACK_ROUTER_INTERFACE_ADDED:
                case OPENSTACK_ROUTER_INTERFACE_UPDATED:
                case OPENSTACK_ROUTER_INTERFACE_REMOVED:
                default:
                    // do nothing for the other events
                    break;
            }
        }

        private void associateFloatingIp(NetFloatingIP osFip) {
            Port osPort = osNetworkService.port(osFip.getPortId());
            if (osPort == null) {
                final String error = String.format(ERR_FLOW + "port(%s) not found",
                        osFip.getFloatingIpAddress(), osFip.getPortId());
                throw new IllegalStateException(error);
            }
            // set floating IP rules only if the port is associated to a VM
            if (!Strings.isNullOrEmpty(osPort.getDeviceId())) {
                setFloatingIpRules(osFip, osPort, true);
            }
        }

        private void disassociateFloatingIp(NetFloatingIP osFip, String portId) {
            Port osPort = osNetworkService.port(portId);
            if (osPort == null) {
                // FIXME when a port with floating IP removed without
                // disassociation step, it can reach here
                return;
            }
            // set floating IP rules only if the port is associated to a VM
            if (!Strings.isNullOrEmpty(osPort.getDeviceId())) {
                setFloatingIpRules(osFip, osPort, false);
            }
        }
    }

    private class InternalNodeListener implements OpenstackNodeListener {

        @Override
        public boolean isRelevant(OpenstackNodeEvent event) {
            // do not allow to proceed without leadership
            NodeId leader = leadershipService.getLeader(appId.name());
            if (!Objects.equals(localNodeId, leader)) {
                return false;
            }
            return event.subject().type() == GATEWAY;
        }

        @Override
        public void event(OpenstackNodeEvent event) {

            switch (event.type()) {
                case OPENSTACK_NODE_COMPLETE:
                    eventExecutor.execute(() -> {
                        for (NetFloatingIP fip : osRouterService.floatingIps()) {
                            if (Strings.isNullOrEmpty(fip.getPortId())) {
                                continue;
                            }
                            Port osPort = osNetworkService.port(fip.getPortId());
                            if (osPort == null) {
                                log.warn("Failed to set floating IP {}", fip.getId());
                                continue;
                            }
                            setFloatingIpRules(fip, osPort, true);
                        }
                    });
                    break;
                case OPENSTACK_NODE_CREATED:
                case OPENSTACK_NODE_UPDATED:
                case OPENSTACK_NODE_REMOVED:
                case OPENSTACK_NODE_INCOMPLETE:
                default:
                    // do nothing
                    break;
            }
        }
    }
}
