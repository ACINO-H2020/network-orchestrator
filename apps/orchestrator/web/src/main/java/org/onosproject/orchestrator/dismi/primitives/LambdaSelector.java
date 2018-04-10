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
 * This is the Lambda Selector.
 **/

/**
 * This is the Lambda Selector.
 */
@ApiModel(description = "This is the Lambda Selector.")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-10-12T14:35:41.641Z")
public class LambdaSelector extends Selector {
    private Double lambdaCentre = null;

    private Double lambdaWitdh = null;

    public LambdaSelector() {
    }

    public LambdaSelector lambdaCentre(Double lambdaCentre) {
        this.lambdaCentre = lambdaCentre;
        return this;
    }

    /**
     * Central frequency of the laser channel. - FIXME: frequency or a
     * wavelength?.
     *
     * @return lambdaCentre
     **/
    @ApiModelProperty(required = true,
            value = "Central frequency of the laser channel - " +
                    "FIXME: frequency or a wavelength?")
    public Double getLambdaCentre() {
        return lambdaCentre;
    }

    public void setLambdaCentre(Double lambdaCentre) {
        this.lambdaCentre = lambdaCentre;
    }

    public LambdaSelector lambdaWitdh(Double lambdaWitdh) {
        this.lambdaWitdh = lambdaWitdh;
        return this;
    }

    /**
     * Width of the laser channel. - FIXME: frequency or a wavelength?.
     *
     * @return lambdaWitdh
     **/
    @ApiModelProperty(value = "Width of the laser channel - " +
            "FIXME: frequency or a wavelength?")
    public Double getLambdaWitdh() {
        return lambdaWitdh;
    }

    public void setLambdaWitdh(Double lambdaWitdh) {
        this.lambdaWitdh = lambdaWitdh;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LambdaSelector lambdaSelector = (LambdaSelector) o;
        return Objects.equals(this.lambdaCentre, lambdaSelector.lambdaCentre) &&
                Objects.equals(this.lambdaWitdh, lambdaSelector.lambdaWitdh) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lambdaCentre, lambdaWitdh, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LambdaSelector {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    lambdaCentre: ").append(toIndentedString(lambdaCentre)).append("\n");
        sb.append("    lambdaWitdh: ").append(toIndentedString(lambdaWitdh)).append("\n");
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

