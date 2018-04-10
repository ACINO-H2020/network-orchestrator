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

package org.onosproject.orchestrator.dismi.primitives;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;


/**
 * This is the IP Selector.
 **/

@ApiModel(description = "This is the IP Selector.")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-10-12T14:35:41.641Z")
public class IPSelector extends Selector {
    private String ipSrcAddr = null;

    private String ipDestAddr = null;
    private IpProtocolEnum ipProtocol = IpProtocolEnum.ALL;
    private String ipTos = null;
    private String ipDscp = null;

    public IPSelector() {
    }

    public IPSelector ipSrcAddr(String ipSrcAddr) {
        this.ipSrcAddr = ipSrcAddr;
        return this;
    }

    /**
     * IP source address and mask: a.b.c.d/e.f.g.h or a.b.c.d/n.
     *
     * @return ipSrcAddr
     **/
    @ApiModelProperty(value = "IP source address and mask: a.b.c.d/e.f.g.h or a.b.c.d/n")
    public String getIpSrcAddr() {
        return ipSrcAddr;
    }

    public void setIpSrcAddr(String ipSrcAddr) {
        this.ipSrcAddr = ipSrcAddr;
    }

    public IPSelector ipDestAddr(String ipDestAddr) {
        this.ipDestAddr = ipDestAddr;
        return this;
    }

    /**
     * IP destination address + mask: a.b.c.d/e.f.g.h or a.b.c.d/n.
     *
     * @return ipDestAddr
     **/
    @ApiModelProperty(value = "IP destination address + mask: a.b.c.d/e.f.g.h or a.b.c.d/n")
    public String getIpDestAddr() {
        return ipDestAddr;
    }

    public void setIpDestAddr(String ipDestAddr) {
        this.ipDestAddr = ipDestAddr;
    }

    public IPSelector ipProtocol(IpProtocolEnum ipProtocol) {
        this.ipProtocol = ipProtocol;
        return this;
    }

    /**
     * Protocol: TCP, UDP, ICMP or All.
     *
     * @return ipProtocol
     **/
    @ApiModelProperty(value = "Protocol: TCP, UDP, ICMP or All")
    public IpProtocolEnum getIpProtocol() {
        return ipProtocol;
    }

    public void setIpProtocol(IpProtocolEnum ipProtocol) {
        this.ipProtocol = ipProtocol;
    }

    public IPSelector ipTos(String ipTos) {
        this.ipTos = ipTos;
        return this;
    }

    /**
     * Type of Service bits - FIXME: as number or name?.
     *
     * @return ipTos
     **/
    @ApiModelProperty(value = "Type of Service bits - FIXME: as number or name?")
    public String getIpTos() {
        return ipTos;
    }

    public void setIpTos(String ipTos) {
        this.ipTos = ipTos;
    }

    public IPSelector ipDscp(String ipDscp) {
        this.ipDscp = ipDscp;
        return this;
    }

    /**
     * Comma-separated list of Differenciated Service Code Point (DSCP), as
     * numbers or names.
     *
     * @return ipDscp
     **/
    @ApiModelProperty(value = "Comma-separated list of Differenciated Service Code Point (DSCP), as numbers or names")
    public String getIpDscp() {
        return ipDscp;
    }

    public void setIpDscp(String ipDscp) {
        this.ipDscp = ipDscp;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IPSelector iPSelector = (IPSelector) o;
        return Objects.equals(this.ipSrcAddr, iPSelector.ipSrcAddr) &&
                Objects.equals(this.ipDestAddr, iPSelector.ipDestAddr) &&
                Objects.equals(this.ipProtocol, iPSelector.ipProtocol) &&
                Objects.equals(this.ipTos, iPSelector.ipTos) &&
                Objects.equals(this.ipDscp, iPSelector.ipDscp) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipSrcAddr, ipDestAddr, ipProtocol, ipTos, ipDscp, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class IPSelector {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    ipSrcAddr: ").append(toIndentedString(ipSrcAddr)).append("\n");
        sb.append("    ipDestAddr: ").append(toIndentedString(ipDestAddr)).append("\n");
        sb.append("    ipProtocol: ").append(toIndentedString(ipProtocol)).append("\n");
        sb.append("    ipTos: ").append(toIndentedString(ipTos)).append("\n");
        sb.append("    ipDscp: ").append(toIndentedString(ipDscp)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * Protocol: TCP, UDP, ICMP or All.
     */
    public enum IpProtocolEnum {
        TCP("TCP"),

        UDP("UDP"),

        ICMP("ICMP"),

        ALL("ALL");

        private String value;

        IpProtocolEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}

