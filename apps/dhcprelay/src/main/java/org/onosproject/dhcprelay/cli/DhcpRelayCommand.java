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

package org.onosproject.dhcprelay.cli;

import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onlab.util.Tools;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.dhcprelay.api.DhcpServerInfo;
import org.onosproject.dhcprelay.api.DhcpRelayService;
import org.onosproject.dhcprelay.store.DhcpRecord;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.host.HostService;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Prints DHCP server and DHCP relay status.
 */
@Command(scope = "onos", name = "dhcp-relay", description = "DHCP relay app cli.")
public class DhcpRelayCommand extends AbstractShellCommand {
    private static final String HEADER = "DHCP relay records ([D]: Directly connected):";
    private static final String NO_RECORDS = "No DHCP relay record found";
    private static final String HOST = "id=%s/%s, locations=%s%s, last-seen=%s, IPv4=%s, IPv6=%s";
    private static final String DHCP_SERVER_GW = "DHCP Server: %s, %s via %s (Mac: %s)";
    private static final String DHCP_SERVER = "DHCP Server: %s, %s (Mac: %s)";
    private static final String MISSING_SERVER_CFG = "DHCP Server info not available";
    private static final String DIRECTLY = "[D]";
    private static final String EMPTY = "";
    private static final String NA = "N/A";
    private static final String STATUS_FMT = "[%s, %s]";
    private static final String STATUS_FMT_NH = "[%s via %s, %s]";
    private static final String DEFAULT_SERVERS = "Default DHCP servers:";
    private static final String INDIRECT_SERVERS = "Indirect DHCP servers:";

    private static final DhcpRelayService DHCP_RELAY_SERVICE = get(DhcpRelayService.class);
    private static final HostService HOST_SERVICE = get(HostService.class);

    @Override
    protected void execute() {
        List<DhcpServerInfo> defaultDhcpServerInfoList = DHCP_RELAY_SERVICE.getDefaultDhcpServerInfoList();
        List<DhcpServerInfo> indirectDhcpServerInfoList = DHCP_RELAY_SERVICE.getIndirectDhcpServerInfoList();

        if (defaultDhcpServerInfoList.isEmpty() && indirectDhcpServerInfoList.isEmpty()) {
            print(MISSING_SERVER_CFG);
            return;
        }

        if (!defaultDhcpServerInfoList.isEmpty()) {
            print(DEFAULT_SERVERS);
            listServers(defaultDhcpServerInfoList);
        }
        if (!indirectDhcpServerInfoList.isEmpty()) {
            print(INDIRECT_SERVERS);
            listServers(indirectDhcpServerInfoList);
        }

        // DHCP records
        Collection<DhcpRecord> records = DHCP_RELAY_SERVICE.getDhcpRecords();
        if (records.isEmpty()) {
            print(NO_RECORDS);
            return;
        }
        print(HEADER);
        records.forEach(record -> print(HOST,
                                        record.macAddress(),
                                        record.vlanId(),
                                        record.locations(),
                                        record.directlyConnected() ? DIRECTLY : EMPTY,
                                        Tools.timeAgo(record.lastSeen()),
                                        ip4State(record),
                                        ip6State(record)));
    }

    private void listServers(List<DhcpServerInfo> dhcpServerInfoList) {
        dhcpServerInfoList.forEach(dhcpServerInfo -> {
            String connectPoint = dhcpServerInfo.getDhcpServerConnectPoint()
                    .map(Object::toString).orElse(NA);
            String serverMac = dhcpServerInfo.getDhcpConnectMac()
                    .map(Object::toString).orElse(NA);
            String gatewayAddress;
            String serverIp;

            switch (dhcpServerInfo.getVersion()) {
                case DHCP_V4:
                    gatewayAddress = dhcpServerInfo.getDhcpGatewayIp4()
                            .map(Object::toString).orElse(null);
                    serverIp = dhcpServerInfo.getDhcpServerIp4()
                            .map(Object::toString).orElse(NA);
                    break;
                case DHCP_V6:
                    gatewayAddress = dhcpServerInfo.getDhcpGatewayIp6()
                            .map(Object::toString).orElse(null);
                    serverIp = dhcpServerInfo.getDhcpServerIp6()
                            .map(Object::toString).orElse(NA);
                    break;
                default:
                    return;
            }
            if (gatewayAddress != null) {
                print(DHCP_SERVER_GW, connectPoint, serverIp, gatewayAddress, serverMac);
            } else {
                print(DHCP_SERVER, connectPoint, serverIp, serverMac);
            }
        });
    }

    private String ip4State(DhcpRecord record) {
        String nextHopIp = findNextHopIp(IpAddress::isIp4,
                                         record.nextHop().orElse(null),
                                         record.vlanId());
        return ipState(record.ip4Address().map(Object::toString).orElse(NA),
                       record.ip4Status().map(Object::toString).orElse(NA),
                       record.directlyConnected(),
                       nextHopIp);
    }

    private String ip6State(DhcpRecord record) {
        String nextHopIp = findNextHopIp(IpAddress::isIp6,
                                         record.nextHop().orElse(null),
                                         record.vlanId());
        return ipState(record.ip6Address().map(Object::toString).orElse(NA),
                       record.ip6Status().map(Object::toString).orElse(NA),
                       record.directlyConnected(),
                       nextHopIp);
    }

    private String ipState(String ipAddress, String status,
                           boolean directlyConnected,
                           String nextHopIp) {
        if (directlyConnected) {
            return String.format(STATUS_FMT, ipAddress, status);
        } else {
            return String.format(STATUS_FMT_NH, ipAddress, nextHopIp, status);
        }
    }

    private String findNextHopIp(Predicate<IpAddress> ipFilter, MacAddress nextHopMac, VlanId vlanId) {
        if (ipFilter == null || nextHopMac == null || vlanId == null) {
            return NA;
        }
        Host host = HOST_SERVICE.getHost(HostId.hostId(nextHopMac, vlanId));
        if (host == null) {
            return NA;
        }
        return host.ipAddresses().stream()
                .filter(ipFilter)
                .filter(ip -> !ip.isLinkLocal())
                .map(Object::toString)
                .findFirst()
                .orElse(NA);
    }
}
