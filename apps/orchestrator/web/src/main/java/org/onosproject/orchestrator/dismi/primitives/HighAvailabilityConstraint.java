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
 * Created by aghafoor on 2017-06-28.
 */

@ApiModel(description = "The HighAvailabilityConstraint allows requesting to provide high availability")
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2017-28-22T15:29:51.886Z")
public class HighAvailabilityConstraint extends Constraint {

    private Boolean availability = false;


    /**
     **/
    public HighAvailabilityConstraint() {
    }

    /**
     * true or false - boolean.
     **/
    public HighAvailabilityConstraint availability(Boolean availability) {
        this.availability = availability;
        return this;
    }

    @ApiModelProperty(required = true, value = "true or false")
    @JsonProperty("availability")
    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HighAvailabilityConstraint highAvailabilityConstraint = (HighAvailabilityConstraint) o;
        return Objects.equals(availability, highAvailabilityConstraint.availability) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), availability);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HighAvailabilityConstraint {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    availability: ").append(toIndentedString(availability)).append("\n");
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
