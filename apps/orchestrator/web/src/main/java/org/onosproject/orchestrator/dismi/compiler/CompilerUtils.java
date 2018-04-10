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

import org.onlab.packet.IPv4;
import org.onlab.packet.IPv6;
import org.onosproject.orchestrator.dismi.primitives.IPSelector;

/**
 * Created by aghafoor on 2017-01-18.
 */
public class CompilerUtils {


    public short parseIpProtocolFromString(IPSelector.IpProtocolEnum ipProtocolEnum) {

        if (ipProtocolEnum.toString().compareTo("ICMP") == 0) {
            return IPv4.PROTOCOL_ICMP;
        }
        if (ipProtocolEnum.toString().compareTo("TCP") == 0) {
            return IPv4.PROTOCOL_TCP;
        }
        if (ipProtocolEnum.toString().compareTo("UDP") == 0) {
            return IPv4.PROTOCOL_UDP;
        }
        if (ipProtocolEnum.toString().compareTo("ICMP6") == 0) {
            return IPv6.PROTOCOL_ICMP6;
        }
        if (ipProtocolEnum.toString().compareTo("ALL") == 0) {
            return -1;
        }
        return -1;
    }
}
