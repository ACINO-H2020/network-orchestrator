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
 * and egress customer traffic.
 **/

@ApiModel(description = "An EndPoints refers to a specific point in the " +
        "ACINO network that can ingress and egress " +
        "customer traffic.")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class LambdaEndPoint extends EndPoint {
    private String switchId = null;

    private Integer portId = null;

    private Double lambda = null;

    private Double witdh = null;

    public LambdaEndPoint() {
    }

    /**
     * Switch ID of the optical switch.
     **/
    public LambdaEndPoint switchId(String switchId) {
        this.switchId = switchId;
        return this;
    }


    @ApiModelProperty(required = true,
            value = "Switch ID of the optical switch")
    @JsonProperty("switch_id")
    public String getSwitchId() {
        return switchId;
    }

    public void setSwitchId(String switchId) {
        this.switchId = switchId;
    }


    /**
     * Port ID of the end point.
     **/
    public LambdaEndPoint portId(Integer portId) {
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
     * Central frequency of the laser - FIXME: frequency or a wavelength?.
     **/
    public LambdaEndPoint lambda(Double lambda) {
        this.lambda = lambda;
        return this;
    }


    @ApiModelProperty(required = true,
            value = "Central frequency of the laser - FIXME: frequency or a wavelength?")
    @JsonProperty("lambda")
    public Double getLambda() {
        return lambda;
    }

    public void setLambda(Double lambda) {
        this.lambda = lambda;
    }


    /**
     * Width of the laser channel - FIXME: frequency or a wavelength?
     **/
    public LambdaEndPoint witdh(Double witdh) {
        this.witdh = witdh;
        return this;
    }


    @ApiModelProperty(required = true,
            value = "Width of the laser channel - FIXME: frequency or a wavelength?")
    @JsonProperty("witdh")
    public Double getWitdh() {
        return witdh;
    }

    public void setWitdh(Double witdh) {
        this.witdh = witdh;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LambdaEndPoint lambdaEndPoint = (LambdaEndPoint) o;
        return Objects.equals(this.switchId, lambdaEndPoint.switchId) &&
                Objects.equals(this.portId, lambdaEndPoint.portId) &&
                Objects.equals(this.lambda, lambdaEndPoint.lambda) &&
                Objects.equals(this.witdh, lambdaEndPoint.witdh) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(switchId, portId, lambda, witdh, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LambdaEndPoint {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    switchId: ").append(toIndentedString(switchId)).append("\n");
        sb.append("    portId: ").append(toIndentedString(portId)).append("\n");
        sb.append("    lambda: ").append(toIndentedString(lambda)).append("\n");
        sb.append("    witdh: ").append(toIndentedString(witdh)).append("\n");
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

