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

#ifndef __PARSER__
#define __PARSER__

#include "define.p4"

parser FabricParser (
packet_in packet,
out parsed_headers_t hdr,
inout fabric_metadata_t fabric_metadata,
inout standard_metadata_t standard_metadata) {
    state start {
        transition select(standard_metadata.ingress_port) {
            CPU_PORT: parse_packet_out;
            default: parse_ethernet;
        }
    }

    state parse_packet_out {
        packet.extract(hdr.packet_out);
        transition parse_ethernet;
    }

    state parse_ethernet {
        packet.extract(hdr.ethernet);
        transition select(hdr.ethernet.ether_type){
            ETHERTYPE_QINQ_NON_STD: parse_vlan_tag;
            ETHERTYPE_QINQ: parse_vlan_tag;
            ETHERTYPE_VLAN: parse_vlan_tag;
            ETHERTYPE_MPLS: parse_mpls;
            ETHERTYPE_ARP: parse_arp;
            ETHERTYPE_IPV4: parse_ipv4;
            ETHERTYPE_IPV6: parse_ipv6;
            default: accept;
        }
    }

    state parse_vlan_tag {
        packet.extract(hdr.vlan_tag);
        transition select(hdr.vlan_tag.ether_type){
            ETHERTYPE_VLAN: parse_inner_vlan_tag;
            ETHERTYPE_ARP: parse_arp;
            ETHERTYPE_IPV4: parse_ipv4;
            ETHERTYPE_IPV6: parse_ipv6;
            default: accept;
        }
    }

    state parse_inner_vlan_tag {
        packet.extract(hdr.inner_vlan_tag);
        transition select(hdr.vlan_tag.ether_type){
            ETHERTYPE_ARP: parse_arp;
            ETHERTYPE_IPV4: parse_ipv4;
            ETHERTYPE_IPV6: parse_ipv6;
            default: accept;
        }
    }

    state parse_mpls {
        packet.extract(hdr.mpls);

        //There is only one MPLS label for this fabric.
        transition select(packet.lookahead<ipv4_t>().version) {
            //The packet should be either IPv4 or IPv6.
            IP_VERSION_4: parse_ipv4;
            IP_VERSION_6: parse_ipv6;
            default: parse_ethernet;
        }
    }

    state parse_ipv4 {
        packet.extract(hdr.ipv4);
        fabric_metadata.ip_proto = hdr.ipv4.protocol;
        //Need header verification?
        transition select(hdr.ipv4.protocol) {
            PROTO_TCP: parse_tcp;
            PROTO_UDP: parse_udp;
            PROTO_ICMP: parse_icmp;
            default: accept;
        }
    }

    state parse_ipv6 {
        packet.extract(hdr.ipv6);
        fabric_metadata.ip_proto = hdr.ipv6.next_hdr;
        transition select(hdr.ipv6.next_hdr) {
            PROTO_TCP: parse_tcp;
            PROTO_UDP: parse_udp;
            PROTO_ICMPV6: parse_icmp;
            default: accept;
        }
    }

    state parse_arp {
        packet.extract(hdr.arp);
        transition accept;
    }

    state parse_tcp {
        packet.extract(hdr.tcp);
        fabric_metadata.l4_src_port = hdr.tcp.src_port;
        fabric_metadata.l4_dst_port = hdr.tcp.dst_port;
        transition accept;
    }

    state parse_udp {
        packet.extract(hdr.udp);
        fabric_metadata.l4_src_port = hdr.udp.src_port;
        fabric_metadata.l4_dst_port = hdr.udp.dst_port;
        transition accept;
    }

    state parse_icmp {
        packet.extract(hdr.icmp);
        transition accept;
    }
}

control FabricDeparser(packet_out packet, in parsed_headers_t hdr) {
    apply{
        packet.emit(hdr.packet_in);
        packet.emit(hdr.ethernet);
        packet.emit(hdr.vlan_tag);
        packet.emit(hdr.inner_vlan_tag);
        packet.emit(hdr.mpls);
        packet.emit(hdr.arp);
        packet.emit(hdr.ipv4);
        packet.emit(hdr.ipv6);
        packet.emit(hdr.tcp);
        packet.emit(hdr.udp);
        packet.emit(hdr.icmp);
    }
}

#endif
