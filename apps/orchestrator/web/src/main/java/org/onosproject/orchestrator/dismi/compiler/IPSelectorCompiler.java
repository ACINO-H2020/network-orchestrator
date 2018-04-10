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

package org.onosproject.orchestrator.dismi.compiler;

import org.onlab.packet.Ethernet;
import org.onlab.packet.IpPrefix;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.orchestrator.dismi.primitives.IPSelector;
import org.slf4j.Logger;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2017-02-06.
 */
public class IPSelectorCompiler {
    private final Logger log = getLogger(getClass());
    private IPSelector ipSelector = null;

    public IPSelectorCompiler(IPSelector ipSelector) {
        this.ipSelector = ipSelector;
    }

    public TrafficSelector toTrafficSelector() {
        log.info("Compiling DISMI level IPSelector !");
        IpPrefix srcIpPrefix = null;
        IpPrefix dstIpPrefix = null;

        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        if (!isNullOrEmpty(ipSelector.getIpSrcAddr())) {
            srcIpPrefix = IpPrefix.valueOf(ipSelector.getIpSrcAddr());
            if (srcIpPrefix.isIp4()) {
                selectorBuilder.matchIPSrc(srcIpPrefix);
            } else {
                selectorBuilder.matchIPv6Src(srcIpPrefix);
            }
            /*srcIpPrefix = IpPrefix.valueOf(ipSelector.getIpSrcAddr());
            if (srcIpPrefix.isIp4()) {
                selectorBuilder.matchIPSrc(IpPrefix.valueOf(
                        Ip4Address.valueOf(ipSelector.getIpSrcAddr().split("/")[0]), 32));
            } else {
                selectorBuilder.matchIPv6Src(srcIpPrefix);
            }*/
        } else {
            log.error("IPSelector source address is null !");
        }

        if (!isNullOrEmpty(ipSelector.getIpDestAddr())) {
            dstIpPrefix = IpPrefix.valueOf(ipSelector.getIpDestAddr());
            if (dstIpPrefix.isIp4()) {
                selectorBuilder.matchIPDst(dstIpPrefix);
            } else {
                selectorBuilder.matchIPv6Dst(dstIpPrefix);
            }
           /* dstIpPrefix = IpPrefix.valueOf(ipSelector.getIpDestAddr());
            if (dstIpPrefix.isIp4()) {
                selectorBuilder.matchIPDst(IpPrefix.valueOf(
                        Ip4Address.valueOf(ipSelector.getIpDestAddr().split("/")[0]), 32));
            } else {
                selectorBuilder.matchIPv6Dst(dstIpPrefix);
            }*/
        } else {
            log.error("IPSelector destiantion address is null !");
        }

        if ((srcIpPrefix != null) && (dstIpPrefix != null) &&
                (srcIpPrefix.version() != dstIpPrefix.version())) {
            // ERROR: IP src/dst version mismatch
            log.error("IP source and destination version mismatch");
            throw new IllegalArgumentException(
                    "IP source and destination version mismatch");
        }

        // Set the default EthType based on the IP version if the matching source or destination IP prefixes.

        Short ethType = null;
        if ((srcIpPrefix != null) && srcIpPrefix.isIp6()) {
            log.info("Ethernet Type is " + Ethernet.TYPE_IPV6);
            ethType = Ethernet.TYPE_IPV6;
        } else if ((srcIpPrefix != null) && srcIpPrefix.isIp4()) {

        }
        if ((dstIpPrefix != null) && dstIpPrefix.isIp6()) {
            log.info("Ethernet Type is " + Ethernet.TYPE_IPV6);
            ethType = Ethernet.TYPE_IPV6;
        }
        if (ethType != null) {
            selectorBuilder.matchEthType(ethType);
        } else if (((srcIpPrefix != null) && srcIpPrefix.isIp4()) || ((dstIpPrefix != null) && dstIpPrefix.isIp4())) {
            log.info("Ethernet Type is " + Ethernet.TYPE_IPV4);
            ethType = Ethernet.TYPE_IPV4;
            selectorBuilder.matchEthType(ethType);
        }
        log.info("Setting up selector protocl filed !");
        if (!isNullOrEmpty(ipSelector.getIpProtocol().toString())) {
            CompilerUtils compilerUtils = new CompilerUtils();
            short ipProtoShort = compilerUtils.parseIpProtocolFromString(ipSelector.getIpProtocol());
            if (ipProtoShort != -1) {
                selectorBuilder.matchIPProtocol((byte) ipProtoShort);
            }
        }
        log.info("Build traffic selector !");
        return selectorBuilder.build();
    }
}
