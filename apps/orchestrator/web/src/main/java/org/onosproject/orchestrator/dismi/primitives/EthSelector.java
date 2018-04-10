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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;


/**
 * This is the Ethernet Selector.
 **/

/**
 * This is the Ethernet Selector.
 */
@ApiModel(description = "This is the Ethernet Selector.")
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-10-12T14:35:41.641Z")
public class EthSelector extends Selector {
    private String ethSrcMacAddr = null;

    private String ethDestAddr = null;

    private Integer vlanId = null;

    private Integer vlanPrio = null;

    public EthSelector() {
    }

    public EthSelector ethSrcMacAddr(String ethSrcMacAddr) {
        this.ethSrcMacAddr = ethSrcMacAddr;
        return this;
    }

    /**
     * Ethernet source MAC address.
     *
     * @return ethSrcMacAddr
     **/
    @ApiModelProperty(value = "Ethernet source MAC address")
    @JsonProperty("ethSrcMacAddr")
    public String getEthSrcMacAddr() {
        return ethSrcMacAddr;
    }

    public void setEthSrcMacAddr(String ethSrcMacAddr) {
        this.ethSrcMacAddr = ethSrcMacAddr;
    }

    public EthSelector ethDestAddr(String ethDestAddr) {
        this.ethDestAddr = ethDestAddr;
        return this;
    }

    /**
     * Ethernet destination MAC address.
     *
     * @return ethDestAddr
     **/
    @ApiModelProperty(value = "Ethernet destination MAC address")
    @JsonProperty("ethDestAddr")
    public String getEthDestAddr() {
        return ethDestAddr;
    }

    public void setEthDestAddr(String ethDestAddr) {
        this.ethDestAddr = ethDestAddr;
    }

    public EthSelector vlanId(Integer vlanId) {
        this.vlanId = vlanId;
        return this;
    }

    /**
     * VLAN id.
     *
     * @return vlanId
     **/
    @ApiModelProperty(value = "VLAN id")
    @JsonProperty("vlanId")
    public Integer getVlanId() {
        return vlanId;
    }

    public void setVlanId(Integer vlanId) {
        this.vlanId = vlanId;
    }

    public EthSelector vlanPrio(Integer vlanPrio) {
        this.vlanPrio = vlanPrio;
        return this;
    }

    /**
     * VLAN priority (pcp).
     *
     * @return vlanPrio
     **/
    @ApiModelProperty(value = "VLAN priority (pcp)")
    @JsonProperty("vlanPrio")
    public Integer getVlanPrio() {
        return vlanPrio;
    }

    public void setVlanPrio(Integer vlanPrio) {
        this.vlanPrio = vlanPrio;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EthSelector ethSelector = (EthSelector) o;
        return Objects.equals(this.ethSrcMacAddr, ethSelector.ethSrcMacAddr) &&
                Objects.equals(this.ethDestAddr, ethSelector.ethDestAddr) &&
                Objects.equals(this.vlanId, ethSelector.vlanId) &&
                Objects.equals(this.vlanPrio, ethSelector.vlanPrio) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ethSrcMacAddr, ethDestAddr, vlanId, vlanPrio, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class EthSelector {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    ethSrcMacAddr: ").append(toIndentedString(ethSrcMacAddr)).append("\n");
        sb.append("    ethDestAddr: ").append(toIndentedString(ethDestAddr)).append("\n");
        sb.append("    vlanId: ").append(toIndentedString(vlanId)).append("\n");
        sb.append("    vlanPrio: ").append(toIndentedString(vlanPrio)).append("\n");
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
}

