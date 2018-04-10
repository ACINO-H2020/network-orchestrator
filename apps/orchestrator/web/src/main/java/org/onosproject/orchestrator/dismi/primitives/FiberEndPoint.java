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
 * An EndPoints refers to a specific point in the ACINO network that can ingress
 * and egress customer traffic. This particular EndPoint represents an optical
 * fibre switch and is characterised by a switch name and port number.
 **/

@ApiModel(description = "An EndPoints refers to a specific point in the " +
        "ACINO network that can ingress and egress customer " +
        "traffic. This particular EndPoint represents an " +
        "optical fibre switch and is characterised by a " +
        "switch name and port number.")
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class FiberEndPoint extends EndPoint {
    private String switchId = null;

    private Integer portId = null;

    public FiberEndPoint() {
    }

    /**
     * Optical switch.
     **/
    public FiberEndPoint switchId(String switchId) {
        this.switchId = switchId;
        return this;
    }


    @ApiModelProperty(required = true, value = "Optical switch")
    @JsonProperty("switch_id")
    public String getSwitchId() {
        return switchId;
    }

    public void setSwitchId(String switchId) {
        this.switchId = switchId;
    }


    /**
     * Port ID of the optical switch.
     **/
    public FiberEndPoint portId(Integer portId) {
        this.portId = portId;
        return this;
    }


    @ApiModelProperty(required = true, value = "Port ID of the optical switch")
    @JsonProperty("port_id")
    public Integer getPortId() {
        return portId;
    }

    public void setPortId(Integer portId) {
        this.portId = portId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FiberEndPoint fiberEndPoint = (FiberEndPoint) o;
        return Objects.equals(this.switchId, fiberEndPoint.switchId) &&
                Objects.equals(this.portId, fiberEndPoint.portId) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(switchId, portId, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FiberEndPoint {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    switchId: ").append(toIndentedString(switchId)).append("\n");
        sb.append("    portId: ").append(toIndentedString(portId)).append("\n");
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
