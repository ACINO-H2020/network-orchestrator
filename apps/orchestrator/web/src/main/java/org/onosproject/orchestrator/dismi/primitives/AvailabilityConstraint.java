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
 * Availability can be specified as an availability percentage or as its
 * equivalent downtime per time period. This constraint allows specifying the
 * availability as a Mean Time To Recovery (MTTR), Mean Time between Failures
 * (MTBF) and optionally as an availability in percentage.
 **/

@ApiModel(description = "Availability can be specified as an availability percentage or as its equivalent downtime " +
        "per time period. This constraint allows specifying the availability as a Mean Time To Recovery (MTTR), " +
        "Mean Time between Failures (MTBF) and optionally as an availability in percentage.")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class AvailabilityConstraint extends Constraint {
    private String mttr = null;

    private String mtbf = null;

    private String availability = null;


    public AvailabilityConstraint() {
    }


    /**
     * Mean Time To Recovery - specify unit (us, ms, s, min, h, d, w).
     *
     * @return mttr
     **/
    @ApiModelProperty(required = true, value = "Mean Time To Recovery - specify unit (us, ms, s, min, h, d, w).")
    @JsonProperty("mttr")
    public String getMttr() {
        return mttr;
    }

    public void setMttr(String mttr) {
        this.mttr = mttr;
    }


    /**
     * Mean Time Between Failures - specify unit (us, ms, s, min, h, d, w).
     **/
    public AvailabilityConstraint mtbf(String mtbf) {
        this.mtbf = mtbf;
        return this;
    }

    /**
     * Mean Time Between Failures - specify unit (us, ms, s, min, h, d, w).
     *
     * @return mtbf
     **/
    @ApiModelProperty(required = true, value = "Mean Time Between Failures - specify unit (us, ms, s, min, h, d, w).")
    @JsonProperty("mtbf")
    public String getMtbf() {
        return mtbf;
    }

    public void setMtbf(String mtbf) {
        this.mtbf = mtbf;
    }


    /**
     * Availability in percentage.
     **/
    public AvailabilityConstraint availability(String availability) {
        this.availability = availability;
        return this;
    }


    @ApiModelProperty(value = "Availability in percentage - - end with symbole %")
    @JsonProperty("availability")
    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
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
        AvailabilityConstraint availabilityConstraint = (AvailabilityConstraint) o;
        return Objects.equals(mttr, availabilityConstraint.mttr) &&
                Objects.equals(mtbf, availabilityConstraint.mtbf) &&
                Objects.equals(availability, availabilityConstraint.availability) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mttr, mtbf, availability);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AvailabilityConstraint {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    mttr: ").append(toIndentedString(mttr)).append("\n");
        sb.append("    mtbf: ").append(toIndentedString(mtbf)).append("\n");
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
