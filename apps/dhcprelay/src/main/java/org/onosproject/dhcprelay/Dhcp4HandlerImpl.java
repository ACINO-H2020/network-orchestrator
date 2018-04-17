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
 *
 */

package org.onosproject.dhcprelay;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.BasePacket;
import org.onlab.packet.DHCP;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.TpPort;
import org.onlab.packet.UDP;
import org.onlab.packet.VlanId;
import org.onlab.packet.dhcp.CircuitId;
import org.onlab.packet.dhcp.DhcpOption;
import org.onlab.packet.dhcp.DhcpRelayAgentOption;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.dhcprelay.api.DhcpHandler;
import org.onosproject.dhcprelay.api.DhcpServerInfo;
import org.onosproject.dhcprelay.config.DhcpServerConfig;
import org.onosproject.dhcprelay.config.IgnoreDhcpConfig;
import org.onosproject.dhcprelay.store.DhcpRecord;
import org.onosproject.dhcprelay.store.DhcpRelayStore;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.behaviour.Pipeliner;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.flowobjective.Objective;
import org.onosproject.net.flowobjective.ObjectiveContext;
import org.onosproject.net.flowobjective.ObjectiveError;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostProvider;
import org.onosproject.net.host.HostProviderRegistry;
import org.onosproject.net.host.HostProviderService;
import org.onosproject.net.intf.Interface;
import org.onosproject.net.intf.InterfaceService;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.routeservice.Route;
import org.onosproject.routeservice.RouteStore;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.HostLocation;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.host.DefaultHostDescription;
import org.onosproject.net.host.HostDescription;
import org.onosproject.net.host.HostService;
import org.onosproject.net.host.InterfaceIpAddress;
import org.onosproject.net.packet.DefaultOutboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.onlab.packet.DHCP.DHCPOptionCode.OptionCode_CircuitID;
import static org.onlab.packet.DHCP.DHCPOptionCode.OptionCode_END;
import static org.onlab.packet.DHCP.DHCPOptionCode.OptionCode_MessageType;
import static org.onlab.packet.MacAddress.valueOf;
import static org.onlab.packet.dhcp.DhcpRelayAgentOption.RelayAgentInfoOptions.CIRCUIT_ID;
import static org.onosproject.net.flowobjective.Objective.Operation.ADD;
import static org.onosproject.net.flowobjective.Objective.Operation.REMOVE;

@Component
@Service
@Property(name = "version", value = "4")
public class Dhcp4HandlerImpl implements DhcpHandler, HostProvider {
    public static final String DHCP_V4_RELAY_APP = "org.onosproject.Dhcp4HandlerImpl";
    public static final ProviderId PROVIDER_ID = new ProviderId("dhcp4", DHCP_V4_RELAY_APP);
    private static final String BROADCAST_IP = "255.255.255.255";
    private static final int IGNORE_CONTROL_PRIORITY = PacketPriority.CONTROL.priorityValue() + 1000;

    private static final TrafficSelector CLIENT_SERVER_SELECTOR = DefaultTrafficSelector.builder()
            .matchEthType(Ethernet.TYPE_IPV4)
            .matchIPProtocol(IPv4.PROTOCOL_UDP)
            .matchIPSrc(Ip4Address.ZERO.toIpPrefix())
            .matchIPDst(Ip4Address.valueOf(BROADCAST_IP).toIpPrefix())
            .matchUdpSrc(TpPort.tpPort(UDP.DHCP_CLIENT_PORT))
            .matchUdpDst(TpPort.tpPort(UDP.DHCP_SERVER_PORT))
            .build();
    private static final TrafficSelector SERVER_RELAY_SELECTOR = DefaultTrafficSelector.builder()
            .matchEthType(Ethernet.TYPE_IPV4)
            .matchIPProtocol(IPv4.PROTOCOL_UDP)
            .matchUdpSrc(TpPort.tpPort(UDP.DHCP_SERVER_PORT))
            .matchUdpDst(TpPort.tpPort(UDP.DHCP_SERVER_PORT))
            .build();
    static final Set<TrafficSelector> DHCP_SELECTORS = ImmutableSet.of(
            CLIENT_SERVER_SELECTOR,
            SERVER_RELAY_SELECTOR
    );
    private static Logger log = LoggerFactory.getLogger(Dhcp4HandlerImpl.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DhcpRelayStore dhcpRelayStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected RouteStore routeStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected InterfaceService interfaceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;

    protected HostProviderService providerService;
    protected ApplicationId appId;
    protected Multimap<DeviceId, VlanId> ignoredVlans = HashMultimap.create();
    private InternalHostListener hostListener = new InternalHostListener();

    private List<DhcpServerInfo> defaultServerInfoList = Lists.newArrayList();
    private List<DhcpServerInfo> indirectServerInfoList = Lists.newArrayList();

    @Activate
    protected void activate() {
        appId = coreService.registerApplication(DHCP_V4_RELAY_APP);
        hostService.addListener(hostListener);
        providerService = providerRegistry.register(this);
    }

    @Deactivate
    protected void deactivate() {
        providerRegistry.unregister(this);
        hostService.removeListener(hostListener);
        defaultServerInfoList.forEach(this::stopMonitoringIps);
        defaultServerInfoList.clear();
        indirectServerInfoList.forEach(this::stopMonitoringIps);
        indirectServerInfoList.clear();
    }

    private void stopMonitoringIps(DhcpServerInfo serverInfo) {
        serverInfo.getDhcpGatewayIp4().ifPresent(gatewayIp -> {
            hostService.stopMonitoringIp(gatewayIp);
        });
        serverInfo.getDhcpServerIp4().ifPresent(serverIp -> {
            hostService.stopMonitoringIp(serverIp);
        });
    }

    @Override
    public void setDefaultDhcpServerConfigs(Collection<DhcpServerConfig> configs) {
        setDhcpServerConfigs(configs, defaultServerInfoList);
    }

    @Override
    public void setIndirectDhcpServerConfigs(Collection<DhcpServerConfig> configs) {
        setDhcpServerConfigs(configs, indirectServerInfoList);
    }

    @Override
    public List<DhcpServerInfo> getDefaultDhcpServerInfoList() {
        return defaultServerInfoList;
    }

    @Override
    public List<DhcpServerInfo> getIndirectDhcpServerInfoList() {
        return indirectServerInfoList;
    }

    @Override
    public void updateIgnoreVlanConfig(IgnoreDhcpConfig config) {
        if (config == null) {
            ignoredVlans.forEach(((deviceId, vlanId) -> {
                processIgnoreVlanRule(deviceId, vlanId, REMOVE);
            }));
            return;
        }
        config.ignoredVlans().forEach((deviceId, vlanId) -> {
            if (ignoredVlans.get(deviceId).contains(vlanId)) {
                // don't need to process if it already ignored
                return;
            }
            processIgnoreVlanRule(deviceId, vlanId, ADD);
        });

        ignoredVlans.forEach((deviceId, vlanId) -> {
            if (!config.ignoredVlans().get(deviceId).contains(vlanId)) {
                // not contains in new config, remove it
                processIgnoreVlanRule(deviceId, vlanId, REMOVE);
            }
        });
    }

    public void setDhcpServerConfigs(Collection<DhcpServerConfig> configs, List<DhcpServerInfo> serverInfoList) {
        if (configs.size() == 0) {
            // no config to update
            return;
        }

        // TODO: currently we pick up first DHCP server config.
        // Will use other server configs in the future for HA.
        DhcpServerConfig serverConfig = configs.iterator().next();

        if (!serverConfig.getDhcpServerIp4().isPresent()) {
            // not a DHCPv4 config
            return;
        }

        if (!serverInfoList.isEmpty()) {
            // remove old server info
            DhcpServerInfo oldServerInfo = serverInfoList.remove(0);

            // stop monitoring gateway or server
            oldServerInfo.getDhcpGatewayIp4().ifPresent(gatewayIp -> {
                hostService.stopMonitoringIp(gatewayIp);
            });
            oldServerInfo.getDhcpServerIp4().ifPresent(serverIp -> {
                hostService.stopMonitoringIp(serverIp);
                cancelDhcpPacket(serverIp);
            });
        }

        // Create new server info according to the config
        DhcpServerInfo newServerInfo = new DhcpServerInfo(serverConfig,
                                                          DhcpServerInfo.Version.DHCP_V4);
        checkState(newServerInfo.getDhcpServerConnectPoint().isPresent(),
                   "Connect point not exists");
        checkState(newServerInfo.getDhcpServerIp4().isPresent(),
                   "IP of DHCP server not exists");

        log.debug("DHCP server connect point: {}", newServerInfo.getDhcpServerConnectPoint().orElse(null));
        log.debug("DHCP server IP: {}", newServerInfo.getDhcpServerIp4().orElse(null));

        Ip4Address serverIp = newServerInfo.getDhcpServerIp4().get();
        Ip4Address ipToProbe;
        if (newServerInfo.getDhcpGatewayIp4().isPresent()) {
            ipToProbe = newServerInfo.getDhcpGatewayIp4().get();
        } else {
            ipToProbe = newServerInfo.getDhcpServerIp4().orElse(null);
        }
        String hostToProbe = newServerInfo.getDhcpGatewayIp4()
                .map(ip -> "gateway").orElse("server");

        log.debug("Probing to resolve {} IP {}", hostToProbe, ipToProbe);
        hostService.startMonitoringIp(ipToProbe);

        Set<Host> hosts = hostService.getHostsByIp(ipToProbe);
        if (!hosts.isEmpty()) {
            Host host = hosts.iterator().next();
            newServerInfo.setDhcpConnectVlan(host.vlan());
            newServerInfo.setDhcpConnectMac(host.mac());
        }

        // Add new server info
        synchronized (this) {
            serverInfoList.clear();
            serverInfoList.add(0, newServerInfo);
        }

        requestDhcpPacket(serverIp);
    }

    @Override
    public void processDhcpPacket(PacketContext context, BasePacket payload) {
        checkNotNull(payload, "DHCP payload can't be null");
        checkState(payload instanceof DHCP, "Payload is not a DHCP");
        DHCP dhcpPayload = (DHCP) payload;
        if (!configured()) {
            log.warn("Missing default DHCP relay server config. Abort packet processing");
            return;
        }

        ConnectPoint inPort = context.inPacket().receivedFrom();
        checkNotNull(dhcpPayload, "Can't find DHCP payload");
        Ethernet packet = context.inPacket().parsed();
        DHCP.MsgType incomingPacketType = dhcpPayload.getOptions().stream()
                .filter(dhcpOption -> dhcpOption.getCode() == OptionCode_MessageType.getValue())
                .map(DhcpOption::getData)
                .map(data -> DHCP.MsgType.getType(data[0]))
                .findFirst()
                .orElse(null);
        checkNotNull(incomingPacketType, "Can't get message type from DHCP payload {}", dhcpPayload);
        switch (incomingPacketType) {
            case DHCPDISCOVER:
                // Add the gateway IP as virtual interface IP for server to understand
                // the lease to be assigned and forward the packet to dhcp server.
                Ethernet ethernetPacketDiscover =
                        processDhcpPacketFromClient(context, packet);
                if (ethernetPacketDiscover != null) {
                    writeRequestDhcpRecord(inPort, packet, dhcpPayload);
                    handleDhcpDiscoverAndRequest(ethernetPacketDiscover, dhcpPayload);
                }
                break;
            case DHCPOFFER:
                //reply to dhcp client.
                Ethernet ethernetPacketOffer = processDhcpPacketFromServer(packet);
                if (ethernetPacketOffer != null) {
                    writeResponseDhcpRecord(ethernetPacketOffer, dhcpPayload);
                    sendResponseToClient(ethernetPacketOffer, dhcpPayload);
                }
                break;
            case DHCPREQUEST:
                // add the gateway ip as virtual interface ip for server to understand
                // the lease to be assigned and forward the packet to dhcp server.
                Ethernet ethernetPacketRequest =
                        processDhcpPacketFromClient(context, packet);
                if (ethernetPacketRequest != null) {
                    writeRequestDhcpRecord(inPort, packet, dhcpPayload);
                    handleDhcpDiscoverAndRequest(ethernetPacketRequest, dhcpPayload);
                }
                break;
            case DHCPDECLINE:
                break;
            case DHCPACK:
                // reply to dhcp client.
                Ethernet ethernetPacketAck = processDhcpPacketFromServer(packet);
                if (ethernetPacketAck != null) {
                    writeResponseDhcpRecord(ethernetPacketAck, dhcpPayload);
                    handleDhcpAck(ethernetPacketAck, dhcpPayload);
                    sendResponseToClient(ethernetPacketAck, dhcpPayload);
                }
                break;
            case DHCPNAK:
                break;
            case DHCPRELEASE:
                // TODO: release the ip address from client
                break;
            case DHCPINFORM:
                break;
            case DHCPFORCERENEW:
                break;
            case DHCPLEASEQUERY:
                handleLeaseQueryMsg(context, packet, dhcpPayload);
                break;
            case DHCPLEASEACTIVE:
                handleLeaseQueryActivateMsg(packet, dhcpPayload);
                break;
            case DHCPLEASEUNASSIGNED:
            case DHCPLEASEUNKNOWN:
                handleLeaseQueryUnknown(packet, dhcpPayload);
                break;
            default:
                break;
        }
    }

    /**
     * Checks if this app has been configured.
     *
     * @return true if all information we need have been initialized
     */
    private boolean configured() {
        return !defaultServerInfoList.isEmpty();
    }

    /**
     * Returns the first interface ip from interface.
     *
     * @param iface interface of one connect point
     * @return the first interface IP; null if not exists an IP address in
     *         these interfaces
     */
    private Ip4Address getFirstIpFromInterface(Interface iface) {
        checkNotNull(iface, "Interface can't be null");
        return iface.ipAddressesList().stream()
                .map(InterfaceIpAddress::ipAddress)
                .filter(IpAddress::isIp4)
                .map(IpAddress::getIp4Address)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets Interface facing to the server for default host.
     *
     * @return the Interface facing to the server; null if not found
     */
    private Interface getDefaultServerInterface() {
        return getServerInterface(defaultServerInfoList);
    }

    /**
     * Gets Interface facing to the server for indirect hosts.
     * Use default server Interface if indirect server not configured.
     *
     * @return the Interface facing to the server; null if not found
     */
    private Interface getIndirectServerInterface() {
        return getServerInterface(indirectServerInfoList);
    }

    private Interface getServerInterface(List<DhcpServerInfo> serverInfos) {
        return serverInfos.stream()
                .findFirst()
                .map(serverInfo -> {
                    ConnectPoint dhcpServerConnectPoint =
                            serverInfo.getDhcpServerConnectPoint().orElse(null);
                    VlanId dhcpConnectVlan = serverInfo.getDhcpConnectVlan().orElse(null);
                    if (dhcpServerConnectPoint == null || dhcpConnectVlan == null) {
                        return null;
                    }
                    return interfaceService.getInterfacesByPort(dhcpServerConnectPoint)
                            .stream()
                            .filter(iface -> interfaceContainsVlan(iface, dhcpConnectVlan))
                            .findFirst()
                            .orElse(null);
                })
                .orElse(null);
    }

    /**
     * Determind if an Interface contains a vlan id.
     *
     * @param iface the Interface
     * @param vlanId the vlan id
     * @return true if the Interface contains the vlan id
     */
    private boolean interfaceContainsVlan(Interface iface, VlanId vlanId) {
        if (vlanId.equals(VlanId.NONE)) {
            // untagged packet, check if vlan untagged or vlan native is not NONE
            return !iface.vlanUntagged().equals(VlanId.NONE) ||
                    !iface.vlanNative().equals(VlanId.NONE);
        }
        // tagged packet, check if the interface contains the vlan
        return iface.vlanTagged().contains(vlanId);
    }

    private void handleLeaseQueryActivateMsg(Ethernet packet, DHCP dhcpPayload) {
        log.debug("LQ: Got DHCPLEASEACTIVE packet!");

        // TODO: release the ip address from client
        MacAddress clientMacAddress = MacAddress.valueOf(dhcpPayload.getClientHardwareAddress());
        VlanId vlanId = VlanId.vlanId(packet.getVlanID());
        HostId hostId = HostId.hostId(clientMacAddress, vlanId);
        DhcpRecord record = dhcpRelayStore.getDhcpRecord(hostId).orElse(null);

        if (record == null) {
            log.warn("Can't find record for host {} when processing DHCPLEASEACTIVE", hostId);
            return;
        }

        // need to update routes
        log.debug("Lease Query for Client results in DHCPLEASEACTIVE - route needs to be modified");
        // get current route
        // find the ip of that client with the DhcpRelay store

        Ip4Address clientIP = record.ip4Address().orElse(null);
        log.debug("LQ: IP of host is " + clientIP.getIp4Address());

        MacAddress nextHopMac = record.nextHop().orElse(null);
        log.debug("LQ: MAC of resulting *OLD* NH for that host is " + nextHopMac.toString());

        // find the new NH by looking at the src MAC of the dhcp request
        // from the LQ store
        MacAddress newNextHopMac = record.nextHopTemp().orElse(null);
        log.debug("LQ: MAC of resulting *NEW* NH for that host is " + newNextHopMac.toString());

        log.debug("LQ: updating dhcp relay record with new NH");
        record.nextHop(newNextHopMac);

        // find the next hop IP from its mac
        HostId gwHostId = HostId.hostId(newNextHopMac, vlanId);
        Host gwHost = hostService.getHost(gwHostId);

        if (gwHost == null) {
            log.warn("Can't find gateway for new NH host " + gwHostId);
            return;
        }

        Ip4Address nextHopIp = gwHost.ipAddresses()
                .stream()
                .filter(IpAddress::isIp4)
                .map(IpAddress::getIp4Address)
                .findFirst()
                .orElse(null);

        if (nextHopIp == null) {
            log.warn("Can't find IP address of gateway " + gwHost);
            return;
        }

        log.debug("LQ: *NEW* NH IP for host is " + nextHopIp.getIp4Address());
        Route route = new Route(Route.Source.STATIC, clientIP.toIpPrefix(), nextHopIp);
        routeStore.updateRoute(route);

        // and forward to client
        Ethernet ethernetPacket = processLeaseQueryFromServer(packet);
        if (ethernetPacket != null) {
            sendResponseToClient(ethernetPacket, dhcpPayload);
        }
    }

    /**
     *
     */
    private void handleLeaseQueryMsg(PacketContext context, Ethernet packet, DHCP dhcpPayload) {
        log.debug("LQ: Got DHCPLEASEQUERY packet!");
        MacAddress clientMacAddress = MacAddress.valueOf(dhcpPayload.getClientHardwareAddress());
        log.debug("LQ: got DHCPLEASEQUERY with MAC " + clientMacAddress.toString());
        // add the client mac (hostid) of this request to a store (the entry will be removed with
        // the reply sent to the originator)
        VlanId vlanId = VlanId.vlanId(packet.getVlanID());
        HostId hId = HostId.hostId(clientMacAddress, vlanId);
        DhcpRecord record = dhcpRelayStore.getDhcpRecord(hId).orElse(null);
        if (record != null) {
            //new NH is to be taken from src mac of LQ packet
            MacAddress newNextHop = packet.getSourceMAC();
            record.nextHopTemp(newNextHop);
            record.ip4Status(dhcpPayload.getPacketType());
            record.updateLastSeen();

            // do a basic routing of the packet (this is unicast routing
            // not a relay operation like for other broadcast dhcp packets
            Ethernet ethernetPacketLQ = processLeaseQueryFromAgent(context, packet);
            // and forward to server
            handleDhcpDiscoverAndRequest(ethernetPacketLQ, dhcpPayload);
        } else {
            log.warn("LQ: Error! - DHCP relay record for that client not found - ignoring LQ!");
        }
    }

    private void handleLeaseQueryUnknown(Ethernet packet, DHCP dhcpPayload) {
        log.debug("Lease Query for Client results in DHCPLEASEUNASSIGNED or " +
                          "DHCPLEASEUNKNOWN - removing route & forwarding reply to originator");
        MacAddress clientMacAddress = MacAddress.valueOf(dhcpPayload.getClientHardwareAddress());
        VlanId vlanId = VlanId.vlanId(packet.getVlanID());
        HostId hostId = HostId.hostId(clientMacAddress, vlanId);
        DhcpRecord record = dhcpRelayStore.getDhcpRecord(hostId).orElse(null);

        if (record == null) {
            log.warn("Can't find record for host {} when handling LQ UNKNOWN/UNASSIGNED message", hostId);
            return;
        }

        Ip4Address clientIP = record.ip4Address().orElse(null);
        log.debug("LQ: IP of host is " + clientIP.getIp4Address());

        // find the new NH by looking at the src MAC of the dhcp request
        // from the LQ store
        MacAddress nextHopMac = record.nextHop().orElse(null);
        log.debug("LQ: MAC of resulting *Existing* NH for that route is " + nextHopMac.toString());

        // find the next hop IP from its mac
        HostId gwHostId = HostId.hostId(nextHopMac, vlanId);
        Host gwHost = hostService.getHost(gwHostId);

        if (gwHost == null) {
            log.warn("Can't find gateway for new NH host " + gwHostId);
            return;
        }

        Ip4Address nextHopIp = gwHost.ipAddresses()
                .stream()
                .filter(IpAddress::isIp4)
                .map(IpAddress::getIp4Address)
                .findFirst()
                .orElse(null);

        if (nextHopIp == null) {
            log.warn("Can't find IP address of gateway {}", gwHost);
            return;
        }

        log.debug("LQ: *Existing* NH IP for host is " + nextHopIp.getIp4Address() + " removing route for it");
        Route route = new Route(Route.Source.STATIC, clientIP.toIpPrefix(), nextHopIp);
        routeStore.removeRoute(route);

        // remove from temp store
        dhcpRelayStore.removeDhcpRecord(hostId);

        // and forward to client
        Ethernet ethernetPacket = processLeaseQueryFromServer(packet);
        if (ethernetPacket != null) {
            sendResponseToClient(ethernetPacket, dhcpPayload);
        }
    }

    /**
     * Build the DHCP discover/request packet with gateway IP(unicast packet).
     *
     * @param context the packet context
     * @param ethernetPacket the ethernet payload to process
     * @return processed packet
     */
    private Ethernet processDhcpPacketFromClient(PacketContext context,
                                                 Ethernet ethernetPacket) {
        ConnectPoint receivedFrom = context.inPacket().receivedFrom();
        DeviceId receivedFromDevice = receivedFrom.deviceId();

        // get dhcp header.
        Ethernet etherReply = ethernetPacket.duplicate();
        IPv4 ipv4Packet = (IPv4) etherReply.getPayload();
        UDP udpPacket = (UDP) ipv4Packet.getPayload();
        DHCP dhcpPacket = (DHCP) udpPacket.getPayload();

        // TODO: refactor
        VlanId dhcpConnectVlan = null;
        MacAddress dhcpConnectMac = null;
        Ip4Address dhcpServerIp = null;
        Ip4Address relayAgentIp = null;

        VlanId indirectDhcpConnectVlan = null;
        MacAddress indirectDhcpConnectMac = null;
        Ip4Address indirectDhcpServerIp = null;
        Ip4Address indirectRelayAgentIp = null;

        if (!defaultServerInfoList.isEmpty()) {
            DhcpServerInfo serverInfo = defaultServerInfoList.get(0);
            dhcpConnectVlan = serverInfo.getDhcpConnectVlan().orElse(null);
            dhcpConnectMac = serverInfo.getDhcpConnectMac().orElse(null);
            dhcpServerIp = serverInfo.getDhcpServerIp4().orElse(null);
            relayAgentIp = serverInfo.getRelayAgentIp4(receivedFromDevice).orElse(null);
        }

        if (!indirectServerInfoList.isEmpty()) {
            DhcpServerInfo indirectServerInfo = indirectServerInfoList.get(0);
            indirectDhcpConnectVlan = indirectServerInfo.getDhcpConnectVlan().orElse(null);
            indirectDhcpConnectMac = indirectServerInfo.getDhcpConnectMac().orElse(null);
            indirectDhcpServerIp = indirectServerInfo.getDhcpServerIp4().orElse(null);
            indirectRelayAgentIp = indirectServerInfo.getRelayAgentIp4(receivedFromDevice).orElse(null);
        }

        Ip4Address clientInterfaceIp =
                interfaceService.getInterfacesByPort(context.inPacket().receivedFrom())
                        .stream()
                        .map(Interface::ipAddressesList)
                        .flatMap(Collection::stream)
                        .map(InterfaceIpAddress::ipAddress)
                        .filter(IpAddress::isIp4)
                        .map(IpAddress::getIp4Address)
                        .findFirst()
                        .orElse(null);
        if (clientInterfaceIp == null) {
            log.warn("Can't find interface IP for client interface for port {}",
                     context.inPacket().receivedFrom());
            return null;
        }
        boolean isDirectlyConnected = directlyConnected(dhcpPacket);
        Interface serverInterface;
        if (isDirectlyConnected) {
            serverInterface = getDefaultServerInterface();
        } else {
            serverInterface = getIndirectServerInterface();
            if (serverInterface == null) {
                // Indirect server interface not found, use default server interface
                serverInterface = getDefaultServerInterface();
            }
        }
        if (serverInterface == null) {
            log.warn("Can't get {} server interface, ignore", isDirectlyConnected ? "direct" : "indirect");
            return null;
        }
        Ip4Address ipFacingServer = getFirstIpFromInterface(serverInterface);
        MacAddress macFacingServer = serverInterface.mac();
        if (ipFacingServer == null || macFacingServer == null) {
            log.warn("No IP address for server Interface {}", serverInterface);
            return null;
        }
        if (dhcpConnectMac == null) {
            log.warn("DHCP Server/Gateway IP not yet resolved .. Aborting DHCP "
                             + "packet processing from client on port: {}",
                     context.inPacket().receivedFrom());
            return null;
        }

        etherReply.setSourceMACAddress(macFacingServer);
        ipv4Packet.setSourceAddress(ipFacingServer.toInt());

        if (isDirectlyConnected) {
            etherReply.setDestinationMACAddress(dhcpConnectMac);
            etherReply.setVlanID(dhcpConnectVlan.toShort());
            ipv4Packet.setDestinationAddress(dhcpServerIp.toInt());

            ConnectPoint inPort = context.inPacket().receivedFrom();
            VlanId vlanId = VlanId.vlanId(ethernetPacket.getVlanID());
            // add connected in port and vlan
            CircuitId cid = new CircuitId(inPort.toString(), vlanId);
            byte[] circuitId = cid.serialize();
            DhcpOption circuitIdSubOpt = new DhcpOption();
            circuitIdSubOpt
                    .setCode(CIRCUIT_ID.getValue())
                    .setLength((byte) circuitId.length)
                    .setData(circuitId);

            DhcpRelayAgentOption newRelayAgentOpt = new DhcpRelayAgentOption();
            newRelayAgentOpt.setCode(OptionCode_CircuitID.getValue());
            newRelayAgentOpt.addSubOption(circuitIdSubOpt);

            // Removes END option first
            List<DhcpOption> options = dhcpPacket.getOptions().stream()
                    .filter(opt -> opt.getCode() != OptionCode_END.getValue())
                    .collect(Collectors.toList());

            // push relay agent option
            options.add(newRelayAgentOpt);

            // make sure option 255(End) is the last option
            DhcpOption endOption = new DhcpOption();
            endOption.setCode(OptionCode_END.getValue());
            options.add(endOption);

            dhcpPacket.setOptions(options);

            // Sets relay agent IP
            int effectiveRelayAgentIp = relayAgentIp != null ?
                    relayAgentIp.toInt() : clientInterfaceIp.toInt();
            dhcpPacket.setGatewayIPAddress(effectiveRelayAgentIp);
        } else {
            if (indirectDhcpServerIp != null) {
                // Use indirect server config for indirect packets if configured
                etherReply.setDestinationMACAddress(indirectDhcpConnectMac);
                etherReply.setVlanID(indirectDhcpConnectVlan.toShort());
                ipv4Packet.setDestinationAddress(indirectDhcpServerIp.toInt());

                // Set giaddr if indirect relay agent IP is configured
                if (indirectRelayAgentIp != null) {
                    dhcpPacket.setGatewayIPAddress(indirectRelayAgentIp.toInt());
                }
            } else {
                // Otherwise, use default server config for indirect packets
                etherReply.setDestinationMACAddress(dhcpConnectMac);
                etherReply.setVlanID(dhcpConnectVlan.toShort());
                ipv4Packet.setDestinationAddress(dhcpServerIp.toInt());

                // Set giaddr if direct relay agent IP is configured
                if (relayAgentIp != null) {
                    dhcpPacket.setGatewayIPAddress(relayAgentIp.toInt());
                }
            }
        }

        // Remove broadcast flag
        dhcpPacket.setFlags((short) 0);

        udpPacket.setPayload(dhcpPacket);
        // As a DHCP relay, the source port should be server port( instead
        // of client port.
        udpPacket.setSourcePort(UDP.DHCP_SERVER_PORT);
        udpPacket.setDestinationPort(UDP.DHCP_SERVER_PORT);
        ipv4Packet.setPayload(udpPacket);
        ipv4Packet.setTtl((byte) 64);
        etherReply.setPayload(ipv4Packet);
        return etherReply;
    }


    /**
     * Do a basic routing for a packet from client (used for LQ processing).
     *
     * @param context the packet context
     * @param ethernetPacket the ethernet payload to process
     * @return processed packet
     */
    private Ethernet processLeaseQueryFromAgent(PacketContext context,
                                                Ethernet ethernetPacket) {
        // get dhcp header.
        Ethernet etherReply = (Ethernet) ethernetPacket.clone();
        IPv4 ipv4Packet = (IPv4) etherReply.getPayload();
        UDP udpPacket = (UDP) ipv4Packet.getPayload();
        DHCP dhcpPacket = (DHCP) udpPacket.getPayload();

        VlanId dhcpConnectVlan = null;
        MacAddress dhcpConnectMac = null;
        Ip4Address dhcpServerIp = null;

        VlanId indirectDhcpConnectVlan = null;
        MacAddress indirectDhcpConnectMac = null;
        Ip4Address indirectDhcpServerIp = null;

        if (!defaultServerInfoList.isEmpty()) {
            DhcpServerInfo serverInfo = defaultServerInfoList.get(0);
            dhcpConnectVlan = serverInfo.getDhcpConnectVlan().orElse(null);
            dhcpConnectMac = serverInfo.getDhcpConnectMac().orElse(null);
            dhcpServerIp = serverInfo.getDhcpServerIp4().orElse(null);
        }

        if (!indirectServerInfoList.isEmpty()) {
            DhcpServerInfo indirectServerInfo = indirectServerInfoList.get(0);
            indirectDhcpConnectVlan = indirectServerInfo.getDhcpConnectVlan().orElse(null);
            indirectDhcpConnectMac = indirectServerInfo.getDhcpConnectMac().orElse(null);
            indirectDhcpServerIp = indirectServerInfo.getDhcpServerIp4().orElse(null);
        }

        Ip4Address clientInterfaceIp =
                interfaceService.getInterfacesByPort(context.inPacket().receivedFrom())
                        .stream()
                        .map(Interface::ipAddressesList)
                        .flatMap(Collection::stream)
                        .map(InterfaceIpAddress::ipAddress)
                        .filter(IpAddress::isIp4)
                        .map(IpAddress::getIp4Address)
                        .findFirst()
                        .orElse(null);
        if (clientInterfaceIp == null) {
            log.warn("Can't find interface IP for client interface for port {}",
                     context.inPacket().receivedFrom());
            return null;
        }
        boolean isDirectlyConnected = directlyConnected(dhcpPacket);
        Interface serverInterface;
        if (isDirectlyConnected) {
            serverInterface = getDefaultServerInterface();
        } else {
            serverInterface = getIndirectServerInterface();
            if (serverInterface == null) {
                // Indirect server interface not found, use default server interface
                serverInterface = getDefaultServerInterface();
            }
        }
        if (serverInterface == null) {
            log.warn("Can't get {} server interface, ignore", isDirectlyConnected ? "direct" : "indirect");
            return null;
        }
        Ip4Address ipFacingServer = getFirstIpFromInterface(serverInterface);
        MacAddress macFacingServer = serverInterface.mac();
        if (ipFacingServer == null || macFacingServer == null) {
            log.warn("No IP address for server Interface {}", serverInterface);
            return null;
        }
        if (dhcpConnectMac == null) {
            log.warn("DHCP server/gateway not yet resolved .. Aborting DHCP "
                             + "packet processing from client on port: {}",
                     context.inPacket().receivedFrom());
            return null;
        }

        etherReply.setSourceMACAddress(macFacingServer);
        etherReply.setDestinationMACAddress(dhcpConnectMac);
        etherReply.setVlanID(dhcpConnectVlan.toShort());
        ipv4Packet.setSourceAddress(ipFacingServer.toInt());
        ipv4Packet.setDestinationAddress(dhcpServerIp.toInt());

        if (indirectDhcpServerIp != null) {
            // Indirect case, replace destination to indirect dhcp server if exist
            etherReply.setDestinationMACAddress(indirectDhcpConnectMac);
            etherReply.setVlanID(indirectDhcpConnectVlan.toShort());
            ipv4Packet.setDestinationAddress(indirectDhcpServerIp.toInt());
        }

        udpPacket.setPayload(dhcpPacket);
        // As a DHCP relay, the source port should be server port( instead
        // of client port.
        udpPacket.setSourcePort(UDP.DHCP_SERVER_PORT);
        udpPacket.setDestinationPort(UDP.DHCP_SERVER_PORT);
        ipv4Packet.setPayload(udpPacket);
        etherReply.setPayload(ipv4Packet);
        return etherReply;
    }


    /**
     * Writes DHCP record to the store according to the request DHCP packet (Discover, Request).
     *
     * @param location the location which DHCP packet comes from
     * @param ethernet the DHCP packet
     * @param dhcpPayload the DHCP payload
     */
    private void writeRequestDhcpRecord(ConnectPoint location,
                                        Ethernet ethernet,
                                        DHCP dhcpPayload) {
        VlanId vlanId = VlanId.vlanId(ethernet.getVlanID());
        MacAddress macAddress = MacAddress.valueOf(dhcpPayload.getClientHardwareAddress());
        HostId hostId = HostId.hostId(macAddress, vlanId);
        DhcpRecord record = dhcpRelayStore.getDhcpRecord(hostId).orElse(null);
        if (record == null) {
            record = new DhcpRecord(HostId.hostId(macAddress, vlanId));
        } else {
            record = record.clone();
        }
        record.addLocation(new HostLocation(location, System.currentTimeMillis()));
        record.ip4Status(dhcpPayload.getPacketType());
        record.setDirectlyConnected(directlyConnected(dhcpPayload));
        if (!directlyConnected(dhcpPayload)) {
            // Update gateway mac address if the host is not directly connected
            record.nextHop(ethernet.getSourceMAC());
        }
        record.updateLastSeen();
        dhcpRelayStore.updateDhcpRecord(HostId.hostId(macAddress, vlanId), record);
    }

    /**
     * Writes DHCP record to the store according to the response DHCP packet (Offer, Ack).
     *
     * @param ethernet the DHCP packet
     * @param dhcpPayload the DHCP payload
     */
    private void writeResponseDhcpRecord(Ethernet ethernet,
                                         DHCP dhcpPayload) {
        Optional<Interface> outInterface = getClientInterface(ethernet, dhcpPayload);
        if (!outInterface.isPresent()) {
            log.warn("Failed to determine where to send {}", dhcpPayload.getPacketType());
            return;
        }

        Interface outIface = outInterface.get();
        ConnectPoint location = outIface.connectPoint();
        VlanId vlanId = getVlanIdFromRelayAgentOption(dhcpPayload);
        if (vlanId == null) {
            vlanId = outIface.vlan();
        }
        MacAddress macAddress = MacAddress.valueOf(dhcpPayload.getClientHardwareAddress());
        HostId hostId = HostId.hostId(macAddress, vlanId);
        DhcpRecord record = dhcpRelayStore.getDhcpRecord(hostId).orElse(null);
        if (record == null) {
            record = new DhcpRecord(HostId.hostId(macAddress, vlanId));
        } else {
            record = record.clone();
        }
        record.addLocation(new HostLocation(location, System.currentTimeMillis()));
        if (dhcpPayload.getPacketType() == DHCP.MsgType.DHCPACK) {
            record.ip4Address(Ip4Address.valueOf(dhcpPayload.getYourIPAddress()));
        }
        record.ip4Status(dhcpPayload.getPacketType());
        record.setDirectlyConnected(directlyConnected(dhcpPayload));
        record.updateLastSeen();
        dhcpRelayStore.updateDhcpRecord(HostId.hostId(macAddress, vlanId), record);
    }

    /**
     * Build the DHCP offer/ack with proper client port.
     *
     * @param ethernetPacket the original packet comes from server
     * @return new packet which will send to the client
     */
    private Ethernet processDhcpPacketFromServer(Ethernet ethernetPacket) {
        // get dhcp header.
        Ethernet etherReply = ethernetPacket.duplicate();
        IPv4 ipv4Packet = (IPv4) etherReply.getPayload();
        UDP udpPacket = (UDP) ipv4Packet.getPayload();
        DHCP dhcpPayload = (DHCP) udpPacket.getPayload();

        // determine the vlanId of the client host - note that this vlan id
        // could be different from the vlan in the packet from the server
        Interface clientInterface = getClientInterface(ethernetPacket, dhcpPayload).orElse(null);

        if (clientInterface == null) {
            log.warn("Cannot find the interface for the DHCP {}", dhcpPayload);
            return null;
        }
        VlanId vlanId;
        if (clientInterface.vlanTagged().isEmpty()) {
            vlanId = clientInterface.vlan();
        } else {
            // might be multiple vlan in same interface
            vlanId = getVlanIdFromRelayAgentOption(dhcpPayload);
        }
        if (vlanId == null) {
            vlanId = VlanId.NONE;
        }
        etherReply.setVlanID(vlanId.toShort());
        etherReply.setSourceMACAddress(clientInterface.mac());

        if (!directlyConnected(dhcpPayload)) {
            // if client is indirectly connected, try use next hop mac address
            MacAddress macAddress = MacAddress.valueOf(dhcpPayload.getClientHardwareAddress());
            HostId hostId = HostId.hostId(macAddress, vlanId);
            DhcpRecord record = dhcpRelayStore.getDhcpRecord(hostId).orElse(null);
            if (record != null) {
                // if next hop can be found, use mac address of next hop
                record.nextHop().ifPresent(etherReply::setDestinationMACAddress);
            } else {
                // otherwise, discard the packet
                log.warn("Can't find record for host id {}, discard packet", hostId);
                return null;
            }
        } else {
            etherReply.setDestinationMACAddress(dhcpPayload.getClientHardwareAddress());
        }

        // we leave the srcMac from the original packet
        // figure out the relay agent IP corresponding to the original request
        Ip4Address ipFacingClient = getFirstIpFromInterface(clientInterface);
        if (ipFacingClient == null) {
            log.warn("Cannot determine relay agent interface Ipv4 addr for host {}/{}. "
                             + "Aborting relay for dhcp packet from server {}",
                     etherReply.getDestinationMAC(), clientInterface.vlan(),
                     ethernetPacket);
            return null;
        }
        // SRC_IP: relay agent IP
        // DST_IP: offered IP
        ipv4Packet.setSourceAddress(ipFacingClient.toInt());
        ipv4Packet.setDestinationAddress(dhcpPayload.getYourIPAddress());
        udpPacket.setSourcePort(UDP.DHCP_SERVER_PORT);
        if (directlyConnected(dhcpPayload)) {
            udpPacket.setDestinationPort(UDP.DHCP_CLIENT_PORT);
        } else {
            // forward to another dhcp relay
            // FIXME: Currently we assume the DHCP comes from a L2 relay with
            // Option 82, this might not work if DHCP message comes from
            // L3 relay.
            udpPacket.setDestinationPort(UDP.DHCP_CLIENT_PORT);
        }

        udpPacket.setPayload(dhcpPayload);
        ipv4Packet.setPayload(udpPacket);
        etherReply.setPayload(ipv4Packet);
        return etherReply;
    }

    /**
     * Build the DHCP offer/ack with proper client port.
     *
     * @param ethernetPacket the original packet comes from server
     * @return new packet which will send to the client
     */
    private Ethernet processLeaseQueryFromServer(Ethernet ethernetPacket) {
        // get dhcp header.
        Ethernet etherReply = (Ethernet) ethernetPacket.clone();
        IPv4 ipv4Packet = (IPv4) etherReply.getPayload();
        UDP udpPacket = (UDP) ipv4Packet.getPayload();
        DHCP dhcpPayload = (DHCP) udpPacket.getPayload();

        // determine the vlanId of the client host - note that this vlan id
        // could be different from the vlan in the packet from the server
        Interface clientInterface = getClientInterface(ethernetPacket, dhcpPayload).orElse(null);

        if (clientInterface == null) {
            log.warn("Cannot find the interface for the DHCP {}", dhcpPayload);
            return null;
        }
        VlanId vlanId;
        if (clientInterface.vlanTagged().isEmpty()) {
            vlanId = clientInterface.vlan();
        } else {
            // might be multiple vlan in same interface
            vlanId = getVlanIdFromRelayAgentOption(dhcpPayload);
        }
        if (vlanId == null) {
            vlanId = VlanId.NONE;
        }
        etherReply.setVlanID(vlanId.toShort());
        etherReply.setSourceMACAddress(clientInterface.mac());

        if (!directlyConnected(dhcpPayload)) {
            // if client is indirectly connected, try use next hop mac address
            MacAddress macAddress = MacAddress.valueOf(dhcpPayload.getClientHardwareAddress());
            HostId hostId = HostId.hostId(macAddress, vlanId);
            DhcpRecord record = dhcpRelayStore.getDhcpRecord(hostId).orElse(null);
            if (record != null) {
                // if next hop can be found, use mac address of next hop
                record.nextHop().ifPresent(etherReply::setDestinationMACAddress);
            } else {
                // otherwise, discard the packet
                log.warn("Can't find record for host id {}, discard packet", hostId);
                return null;
            }
        } else {
            etherReply.setDestinationMACAddress(dhcpPayload.getClientHardwareAddress());
        }

        // default is client port
        udpPacket.setSourcePort(UDP.DHCP_SERVER_PORT);
        udpPacket.setDestinationPort(UDP.DHCP_CLIENT_PORT);

        udpPacket.setPayload(dhcpPayload);
        ipv4Packet.setPayload(udpPacket);
        etherReply.setPayload(ipv4Packet);
        return etherReply;
    }
    /**
     * Extracts VLAN ID from relay agent option.
     *
     * @param dhcpPayload the DHCP payload
     * @return VLAN ID from DHCP payload; null if not exists
     */
    private VlanId getVlanIdFromRelayAgentOption(DHCP dhcpPayload) {
        DhcpRelayAgentOption option = (DhcpRelayAgentOption) dhcpPayload.getOption(OptionCode_CircuitID);
        if (option == null) {
            return null;
        }
        DhcpOption circuitIdSubOption = option.getSubOption(CIRCUIT_ID.getValue());
        if (circuitIdSubOption == null) {
            return null;
        }
        try {
            CircuitId circuitId = CircuitId.deserialize(circuitIdSubOption.getData());
            return circuitId.vlanId();
        } catch (IllegalArgumentException e) {
            // can't deserialize the circuit ID
            return null;
        }
    }

    /**
     * Removes DHCP relay agent information option (option 82) from DHCP payload.
     * Also reset giaddr to 0
     *
     * @param ethPacket the Ethernet packet to be processed
     * @return Ethernet packet processed
     */
    private Ethernet removeRelayAgentOption(Ethernet ethPacket) {
        Ethernet ethernet = ethPacket.duplicate();
        IPv4 ipv4 = (IPv4) ethernet.getPayload();
        UDP udp = (UDP) ipv4.getPayload();
        DHCP dhcpPayload = (DHCP) udp.getPayload();

        // removes relay agent information option
        List<DhcpOption> options = dhcpPayload.getOptions();
        options = options.stream()
                .filter(option -> option.getCode() != OptionCode_CircuitID.getValue())
                .collect(Collectors.toList());
        dhcpPayload.setOptions(options);
        dhcpPayload.setGatewayIPAddress(0);

        udp.setPayload(dhcpPayload);
        ipv4.setPayload(udp);
        ethernet.setPayload(ipv4);
        return ethernet;
    }


    /**
     * Check if the host is directly connected to the network or not.
     *
     * @param dhcpPayload the dhcp payload
     * @return true if the host is directly connected to the network; false otherwise
     */
    private boolean directlyConnected(DHCP dhcpPayload) {
        DhcpRelayAgentOption relayAgentOption =
                (DhcpRelayAgentOption) dhcpPayload.getOption(OptionCode_CircuitID);

        // Doesn't contains relay option
        if (relayAgentOption == null) {
            return true;
        }

        // check circuit id, if circuit id is invalid, we say it is an indirect host
        DhcpOption circuitIdOpt = relayAgentOption.getSubOption(CIRCUIT_ID.getValue());

        try {
            CircuitId.deserialize(circuitIdOpt.getData());
            return true;
        } catch (Exception e) {
            // invalid circuit id
            return false;
        }
    }


    /**
     * Send the DHCP ack to the requester host.
     * Modify Host or Route store according to the type of DHCP.
     *
     * @param ethernetPacketAck the packet
     * @param dhcpPayload the DHCP data
     */
    private void handleDhcpAck(Ethernet ethernetPacketAck, DHCP dhcpPayload) {
        Optional<Interface> outInterface = getClientInterface(ethernetPacketAck, dhcpPayload);
        if (!outInterface.isPresent()) {
            log.warn("Can't find output interface for dhcp: {}", dhcpPayload);
            return;
        }

        Interface outIface = outInterface.get();
        HostLocation hostLocation = new HostLocation(outIface.connectPoint(), System.currentTimeMillis());
        MacAddress macAddress = MacAddress.valueOf(dhcpPayload.getClientHardwareAddress());
        VlanId vlanId = getVlanIdFromRelayAgentOption(dhcpPayload);
        if (vlanId == null) {
            vlanId = outIface.vlan();
        }
        HostId hostId = HostId.hostId(macAddress, vlanId);
        Ip4Address ip = Ip4Address.valueOf(dhcpPayload.getYourIPAddress());

        if (directlyConnected(dhcpPayload)) {
            // Add to host store if it connect to network directly
            Set<IpAddress> ips = Sets.newHashSet(ip);
            Host host = hostService.getHost(hostId);

            Set<HostLocation> hostLocations = Sets.newHashSet(hostLocation);
            if (host != null) {
                // Dual homing support:
                // if host exists, use old locations and new location
                hostLocations.addAll(host.locations());
            }
            HostDescription desc = new DefaultHostDescription(macAddress, vlanId,
                                                              hostLocations, ips, false);
            // Add IP address when dhcp server give the host new ip address
            providerService.hostDetected(hostId, desc, false);
        } else {
            // Add to route store if it does not connect to network directly
            // Get gateway host IP according to host mac address
            // TODO: remove relay store here
            DhcpRecord record = dhcpRelayStore.getDhcpRecord(hostId).orElse(null);

            if (record == null) {
                log.warn("Can't find DHCP record of host {}", hostId);
                return;
            }

            MacAddress gwMac = record.nextHop().orElse(null);
            if (gwMac == null) {
                log.warn("Can't find gateway mac address from record {}", record);
                return;
            }

            HostId gwHostId = HostId.hostId(gwMac, record.vlanId());
            Host gwHost = hostService.getHost(gwHostId);

            if (gwHost == null) {
                log.warn("Can't find gateway host {}", gwHostId);
                return;
            }

            Ip4Address nextHopIp = gwHost.ipAddresses()
                    .stream()
                    .filter(IpAddress::isIp4)
                    .map(IpAddress::getIp4Address)
                    .findFirst()
                    .orElse(null);

            if (nextHopIp == null) {
                log.warn("Can't find IP address of gateway {}", gwHost);
                return;
            }

            Route route = new Route(Route.Source.STATIC, ip.toIpPrefix(), nextHopIp);
            routeStore.updateRoute(route);
        }
    }

    /**
     * forward the packet to ConnectPoint where the DHCP server is attached.
     *
     * @param packet the packet
     */
    private void handleDhcpDiscoverAndRequest(Ethernet packet, DHCP dhcpPayload) {
        boolean direct = directlyConnected(dhcpPayload);
        DhcpServerInfo serverInfo = defaultServerInfoList.get(0);
        if (!direct && !indirectServerInfoList.isEmpty()) {
            serverInfo = indirectServerInfoList.get(0);
        }
        ConnectPoint portToFotward = serverInfo.getDhcpServerConnectPoint().orElse(null);
        // send packet to dhcp server connect point.
        if (portToFotward != null) {
            TrafficTreatment t = DefaultTrafficTreatment.builder()
                    .setOutput(portToFotward.port()).build();
            OutboundPacket o = new DefaultOutboundPacket(
                    portToFotward.deviceId(), t, ByteBuffer.wrap(packet.serialize()));
            if (log.isTraceEnabled()) {
                log.trace("Relaying packet to dhcp server {}", packet);
            }
            packetService.emit(o);
        } else {
            log.warn("Can't find DHCP server connect point, abort.");
        }
    }


    /**
     * Gets output interface of a dhcp packet.
     * If option 82 exists in the dhcp packet and the option was sent by
     * ONOS (circuit format is correct), use the connect
     * point and vlan id from circuit id; otherwise, find host by destination
     * address and use vlan id from sender (dhcp server).
     *
     * @param ethPacket the ethernet packet
     * @param dhcpPayload the dhcp packet
     * @return an interface represent the output port and vlan; empty value
     *         if the host or circuit id not found
     */
    private Optional<Interface> getClientInterface(Ethernet ethPacket, DHCP dhcpPayload) {
        VlanId originalPacketVlanId = VlanId.vlanId(ethPacket.getVlanID());
        DhcpRelayAgentOption option = (DhcpRelayAgentOption) dhcpPayload.getOption(OptionCode_CircuitID);

        DhcpOption circuitIdSubOption = option.getSubOption(CIRCUIT_ID.getValue());
        try {
            CircuitId circuitId = CircuitId.deserialize(circuitIdSubOption.getData());
            ConnectPoint connectPoint = ConnectPoint.deviceConnectPoint(circuitId.connectPoint());
            VlanId vlanId = circuitId.vlanId();
            return interfaceService.getInterfacesByPort(connectPoint)
                    .stream()
                    .filter(iface -> interfaceContainsVlan(iface, vlanId))
                    .findFirst();
        } catch (IllegalArgumentException ex) {
            // invalid circuit format, didn't sent by ONOS
            log.debug("Invalid circuit {}, use information from dhcp payload",
                      circuitIdSubOption.getData());
        }

        // Use Vlan Id from DHCP server if DHCP relay circuit id was not
        // sent by ONOS or circuit Id can't be parsed
        // TODO: remove relay store from this method
        MacAddress dstMac = valueOf(dhcpPayload.getClientHardwareAddress());
        Optional<DhcpRecord> dhcpRecord = dhcpRelayStore.getDhcpRecord(HostId.hostId(dstMac, originalPacketVlanId));
        ConnectPoint clientConnectPoint = dhcpRecord
                .map(DhcpRecord::locations)
                .orElse(Collections.emptySet())
                .stream()
                .reduce((hl1, hl2) -> {
                    // find latest host connect point
                    if (hl1 == null || hl2 == null) {
                        return hl1 == null ? hl2 : hl1;
                    }
                    return hl1.time() > hl2.time() ? hl1 : hl2;
                })
                .orElse(null);

        if (clientConnectPoint != null) {
            return interfaceService.getInterfacesByPort(clientConnectPoint)
                    .stream()
                    .filter(iface -> interfaceContainsVlan(iface, originalPacketVlanId))
                    .findFirst();
        }
        return Optional.empty();
    }

    /**
     * Send the response DHCP to the requester host.
     *
     * @param ethPacket the packet
     * @param dhcpPayload the DHCP data
     */
    private void sendResponseToClient(Ethernet ethPacket, DHCP dhcpPayload) {
        Optional<Interface> outInterface = getClientInterface(ethPacket, dhcpPayload);
        if (directlyConnected(dhcpPayload)) {
            ethPacket = removeRelayAgentOption(ethPacket);
        }
        if (!outInterface.isPresent()) {
            log.warn("Can't find output interface for client, ignore");
            return;
        }
        Interface outIface = outInterface.get();
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .setOutput(outIface.connectPoint().port())
                .build();
        OutboundPacket o = new DefaultOutboundPacket(
                outIface.connectPoint().deviceId(),
                treatment,
                ByteBuffer.wrap(ethPacket.serialize()));
        if (log.isTraceEnabled()) {
            log.trace("Relaying packet to DHCP client {} via {}, vlan {}",
                      ethPacket,
                      outIface.connectPoint(),
                      outIface.vlan());
        }
        packetService.emit(o);
    }

    @Override
    public void triggerProbe(Host host) {
        // Do nothing here
    }

    @Override
    public ProviderId id() {
        return PROVIDER_ID;
    }

    class InternalHostListener implements HostListener {
        @Override
        public void event(HostEvent event) {
            if (!configured()) {
                return;
            }
            switch (event.type()) {
                case HOST_ADDED:
                case HOST_UPDATED:
                    hostUpdated(event.subject());
                    break;
                case HOST_REMOVED:
                    hostRemoved(event.subject());
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Handle host updated.
     * If the host is DHCP server or gateway, update connect mac and vlan.
     *
     * @param host the host
     */
    private void hostUpdated(Host host) {
        hostUpdated(host, defaultServerInfoList);
        hostUpdated(host, indirectServerInfoList);
    }

    private void hostUpdated(Host host, List<DhcpServerInfo> srverInfoList) {
        DhcpServerInfo serverInfo;
        Ip4Address targetIp;
        if (!srverInfoList.isEmpty()) {
            serverInfo = srverInfoList.get(0);
            targetIp = serverInfo.getDhcpGatewayIp4().orElse(null);
            Ip4Address serverIp = serverInfo.getDhcpServerIp4().orElse(null);

            if (targetIp == null) {
                targetIp = serverIp;
            }

            if (targetIp != null) {
                if (host.ipAddresses().contains(targetIp)) {
                    serverInfo.setDhcpConnectMac(host.mac());
                    serverInfo.setDhcpConnectVlan(host.vlan());
                    requestDhcpPacket(serverIp);
                }
            }
        }
    }


    /**
     * Handle host removed.
     * If the host is DHCP server or gateway, unset connect mac and vlan.
     *
     * @param host the host
     */
    private void hostRemoved(Host host) {
        hostRemoved(host, defaultServerInfoList);
        hostRemoved(host, indirectServerInfoList);
    }

    private void hostRemoved(Host host, List<DhcpServerInfo> serverInfoList) {
        DhcpServerInfo serverInfo;
        Ip4Address targetIp;
        if (!serverInfoList.isEmpty()) {
            serverInfo = serverInfoList.get(0);
            Ip4Address serverIp = serverInfo.getDhcpServerIp4().orElse(null);
            targetIp = serverInfo.getDhcpGatewayIp4().orElse(null);

            if (targetIp == null) {
                targetIp = serverIp;
            }

            if (targetIp != null) {
                if (host.ipAddresses().contains(targetIp)) {
                    serverInfo.setDhcpConnectVlan(null);
                    serverInfo.setDhcpConnectMac(null);
                    cancelDhcpPacket(serverIp);
                }
            }
        }
    }

    private void requestDhcpPacket(Ip4Address serverIp) {
        requestServerDhcpPacket(serverIp);
        requestClientDhcpPacket(serverIp);
    }

    private void cancelDhcpPacket(Ip4Address serverIp) {
        cancelServerDhcpPacket(serverIp);
        cancelClientDhcpPacket(serverIp);
    }

    private void cancelServerDhcpPacket(Ip4Address serverIp) {
        TrafficSelector serverSelector =
                DefaultTrafficSelector.builder(SERVER_RELAY_SELECTOR)
                        .matchIPSrc(serverIp.toIpPrefix())
                        .build();
        packetService.cancelPackets(serverSelector,
                                    PacketPriority.CONTROL,
                                    appId);
    }

    private void requestServerDhcpPacket(Ip4Address serverIp) {
        TrafficSelector serverSelector =
                DefaultTrafficSelector.builder(SERVER_RELAY_SELECTOR)
                        .matchIPSrc(serverIp.toIpPrefix())
                        .build();
        packetService.requestPackets(serverSelector,
                                     PacketPriority.CONTROL,
                                     appId);
    }

    private void cancelClientDhcpPacket(Ip4Address serverIp) {
        // Packet comes from relay
        TrafficSelector indirectClientSelector =
                DefaultTrafficSelector.builder(SERVER_RELAY_SELECTOR)
                        .matchIPDst(serverIp.toIpPrefix())
                        .build();
        packetService.cancelPackets(indirectClientSelector,
                                    PacketPriority.CONTROL,
                                    appId);

        // Packet comes from client
        packetService.cancelPackets(CLIENT_SERVER_SELECTOR,
                                    PacketPriority.CONTROL,
                                    appId);
    }

    private void requestClientDhcpPacket(Ip4Address serverIp) {
        // Packet comes from relay
        TrafficSelector indirectClientSelector =
                DefaultTrafficSelector.builder(SERVER_RELAY_SELECTOR)
                        .matchIPDst(serverIp.toIpPrefix())
                        .build();
        packetService.requestPackets(indirectClientSelector,
                                     PacketPriority.CONTROL,
                                     appId);

        // Packet comes from client
        packetService.requestPackets(CLIENT_SERVER_SELECTOR,
                                     PacketPriority.CONTROL,
                                     appId);
    }

    /**
     * Process the ignore rules.
     *
     * @param deviceId the device id
     * @param vlanId the vlan to be ignored
     * @param op the operation, ADD to install; REMOVE to uninstall rules
     */
    private void processIgnoreVlanRule(DeviceId deviceId, VlanId vlanId, Objective.Operation op) {
        AtomicInteger installedCount = new AtomicInteger(DHCP_SELECTORS.size());
        DHCP_SELECTORS.forEach(trafficSelector -> {
            TrafficSelector selector = DefaultTrafficSelector.builder(trafficSelector)
                    .matchVlanId(vlanId)
                    .build();

            ForwardingObjective.Builder builder = DefaultForwardingObjective.builder()
                    .withFlag(ForwardingObjective.Flag.VERSATILE)
                    .withSelector(selector)
                    .withPriority(IGNORE_CONTROL_PRIORITY)
                    .withTreatment(DefaultTrafficTreatment.emptyTreatment())
                    .fromApp(appId);


            ObjectiveContext objectiveContext = new ObjectiveContext() {
                @Override
                public void onSuccess(Objective objective) {
                    log.info("Ignore rule {} (Vlan id {}, device {}, selector {})",
                             op, vlanId, deviceId, selector);
                    int countDown = installedCount.decrementAndGet();
                    if (countDown != 0) {
                        return;
                    }
                    switch (op) {
                        case ADD:
                            ignoredVlans.put(deviceId, vlanId);
                            break;
                        case REMOVE:
                            ignoredVlans.remove(deviceId, vlanId);
                            break;
                        default:
                            log.warn("Unsupported objective operation {}", op);
                            break;
                    }
                }

                @Override
                public void onError(Objective objective, ObjectiveError error) {
                    log.warn("Can't {} ignore rule (vlan id {}, selector {}, device {}) due to {}",
                             op, vlanId, selector, deviceId, error);
                }
            };

            ForwardingObjective fwd;
            switch (op) {
                case ADD:
                    fwd = builder.add(objectiveContext);
                    break;
                case REMOVE:
                    fwd = builder.remove(objectiveContext);
                    break;
                default:
                    log.warn("Unsupported objective operation {}", op);
                    return;
            }

            Device device = deviceService.getDevice(deviceId);
            if (device == null || !device.is(Pipeliner.class)) {
                log.warn("Device {} is not available now, wait until device is available", deviceId);
                return;
            }
            flowObjectiveService.apply(deviceId, fwd);
        });
    }

    @Override
    public void setDhcpFpmEnabled(Boolean enabled) {
        // v4 does not use fpm. Do nothing.
    }
}
