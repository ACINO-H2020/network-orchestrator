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

package org.onosproject.orchestrator.dismi.primitives.extended;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.onosproject.orchestrator.dismi.primitives.AvailabilityConstraint;

import java.util.Objects;


/**
 * Availability can be specified as an availability percentage or as its equivalent downtime per time period. But a 99%
 * availability over a year may translate into a single down time event of 3.65 days of consecutive unavailability or
 * 313650 events, each with one second of unavailability. While both of these situations represent 99% availability, the
 * consequences for the Application are likely quite different. This constraint allows specifying the availability as a
 * Mean Time To Recovery (MTTR), Mean Time between Failures (MTBF) and as an availability in percentage.
 **/

@ApiModel(description = "\"AvailabilityConstraintExtended is an inernal " +
        "primitive that extends the AvailabilityConstraint with\" +\n" +
        "\" its associated parameters. It is created during the resolution " +
        "phase of an intent validation and\" +\n" +
        "\" resolution process.\")")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class AvailabilityConstraintExtended extends AvailabilityConstraint {

    private Double mttrExt = null; // a mandatory param.
    private Double mtbfExt = null; // a mandatory param.
    private Double availabilityExt = null; // Optionsl. we may calculate using following formulas

    /**
     * A = MTBF/(MTBF + MTTR)
     * A (MTBF  + MTTR) = MTBF
     * MTTR = MTBF (1-A)/A
     * MTBF = (MTTR * A) / (1-A)
     */

    public AvailabilityConstraintExtended(AvailabilityConstraint availabilityConstraint) {
        setMttr(availabilityConstraint.getMttr());
        setMtbf(availabilityConstraint.getMtbf());
        setAvailability(availabilityConstraint.getAvailability());
    }


    @ApiModelProperty(required = true,
            value = "Mean Time To Recovery - FIXME: should we use date-time format?")
    @JsonProperty("mttrExt")
    public Double getMttrExt() {
        return mttrExt;
    }

    public void setMttrExt(Double mttrExt) {
        this.mttrExt = mttrExt;
    }

    @ApiModelProperty(required = true,
            value = "Mean Time Between Failures  - FIXME: should we use date-time format?")
    @JsonProperty("mtbfExt")
    public Double getMtbfExt() {
        return mtbfExt;
    }

    public void setMtbfExt(Double mtbfExt) {
        this.mtbfExt = mtbfExt;
    }


    @ApiModelProperty(value = "AvailabilityExt in percentage - should be a double instead?")
    @JsonProperty("availabilityExt")
    public Double getAvailabilityExt() {
        return availabilityExt;
    }

    public void setAvailabilityExt(Double availabilityExt) {
        this.availabilityExt = availabilityExt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AvailabilityConstraintExtended availabilityConstraint = (AvailabilityConstraintExtended) o;
        return Objects.equals(mttrExt, availabilityConstraint.mttrExt) &&
                Objects.equals(mtbfExt, availabilityConstraint.mtbfExt) &&
                Objects.equals(availabilityExt, availabilityConstraint.availabilityExt) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mttrExt, mtbfExt, availabilityExt, super.hashCode());
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AvailabilityConstraint {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    mttr: ").append(toIndentedString(mttrExt)).append("\n");
        sb.append("    mtbf: ").append(toIndentedString(mtbfExt)).append("\n");
        sb.append("    availability: ").append(toIndentedString(availabilityExt)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
