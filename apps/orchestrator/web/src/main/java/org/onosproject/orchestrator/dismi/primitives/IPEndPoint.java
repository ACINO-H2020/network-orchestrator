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
 * An IP EndPoint represents a router and is characterised by an IP address and
 * a port.
 **/

@ApiModel(description = "An IP EndPoint represents a router and is " +
        "characterised by an IP address and a port.")
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class IPEndPoint extends EndPoint {
    private String routerId = null;

    private Integer portId = null;

    private String inAddr = null;

    private String subnets = null;

    public IPEndPoint() {
        super();
    }

    /**
     * Router ID of the router.
     **/
    public IPEndPoint routerId(String routerId) {
        this.routerId = routerId;
        return this;
    }


    @ApiModelProperty(required = true, value = "Router ID of the router")
    @JsonProperty("router_id")
    public String getRouterId() {
        return routerId;
    }

    public void setRouterId(String routerId) {
        this.routerId = routerId;
    }


    /**
     * Port ID of the end point.
     **/
    public IPEndPoint portId(Integer portId) {
        this.portId = portId;
        return this;
    }


    @ApiModelProperty(required = true, value = "Port ID of the end point")
    @JsonProperty("port_id")
    public Integer getPortId() {
        return portId;
    }

    public void setPortId(Integer portId) {
        this.portId = portId;
    }


    /**
     * IP addresss of the end point.
     **/
    public IPEndPoint inAddr(String inAddr) {
        this.inAddr = inAddr;
        return this;
    }


    @ApiModelProperty(required = true, value = "IP addresss of the end point")
    @JsonProperty("in_addr")
    public String getInAddr() {
        return inAddr;
    }

    public void setInAddr(String inAddr) {
        this.inAddr = inAddr;
    }


    /**
     * Subnets.
     **/
    public IPEndPoint subnets(String subnets) {
        this.subnets = subnets;
        return this;
    }


    @ApiModelProperty(value = "Subnets")
    @JsonProperty("subnets")
    public String getSubnets() {
        return subnets;
    }

    public void setSubnets(String subnets) {
        this.subnets = subnets;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IPEndPoint iPEndPoint = (IPEndPoint) o;
        return Objects.equals(this.routerId, iPEndPoint.routerId) &&
                Objects.equals(this.portId, iPEndPoint.portId) &&
                Objects.equals(this.inAddr, iPEndPoint.inAddr) &&
                Objects.equals(this.subnets, iPEndPoint.subnets) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routerId, portId, inAddr, subnets, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class IPEndPoint {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    routerId: ").append(toIndentedString(routerId)).append("\n");
        sb.append("    portId: ").append(toIndentedString(portId)).append("\n");
        sb.append("    inAddr: ").append(toIndentedString(inAddr)).append("\n");
        sb.append("    subnets: ").append(toIndentedString(subnets)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
