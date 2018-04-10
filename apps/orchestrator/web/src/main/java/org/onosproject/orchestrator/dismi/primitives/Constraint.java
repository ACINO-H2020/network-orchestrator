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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;


/**
 * A constraint refers to a specific service characteristic that the customer is
 * requesting: bandwidth, delay, calendaring, ...
 **/

@ApiModel(description = "A constraint refers to a specific service " +
        "characteristic that the customer is requesting: " +
        "bandwidth, delay, calendaring, ...")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "constraint")
public class Constraint {
    private Constraint negotiationLimit = null;

    public Constraint negotiationLimit(Constraint negotiationLimit) {
        this.negotiationLimit = negotiationLimit;
        return this;
    }

    /**
     * If this constraint exists, it is the worst condition acceptable to the
     * client.
     *
     * @return negotiationLimit
     **/
    @ApiModelProperty(value = "If this constraint exists, it is the worst condition acceptable to the client.")
    @JsonProperty("negotiationLimit")
    public Constraint getNegotiationLimit() {
        return negotiationLimit;
    }

    public void setNegotiationLimit(Constraint negotiationLimit) {
        this.negotiationLimit = negotiationLimit;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Constraint constraint = (Constraint) o;
        return Objects.equals(this.negotiationLimit, constraint.negotiationLimit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), "Constraint", negotiationLimit);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Constraint {\n");
        sb.append("    negotiationLimit: ").append(toIndentedString(negotiationLimit)).append("\n");
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

