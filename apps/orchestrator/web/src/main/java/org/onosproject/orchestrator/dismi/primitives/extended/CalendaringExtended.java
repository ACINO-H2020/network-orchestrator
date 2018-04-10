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
import org.onosproject.orchestrator.dismi.primitives.Calendaring;
import org.onosproject.orchestrator.dismi.validation.InputAssertion;

import java.util.Objects;


/**
 * The calendaring constraint provides the ability to request a service from and
 * to a certain time, or for a certain cost.  With this constraint, the
 * application can define: i) An open-ended service starting at a certain time
 * but without a specified ending; ii) A service with both start and end; iii) A
 * service terminating at a certain point, and iv) A service that is not started
 * unless the hourly cost is lower than specified.
 **/

@ApiModel(description = "CalendaringExtended ,an inernal primitive, is an " +
        "extention the AvailabilityConstraint with" +
        "\" its associated parameters. " +
        "It is created during the resolution phase of an " +
        "intent validation and\" +\n" +
        "\" resolution process.\")")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class CalendaringExtended extends Calendaring {

    private Double costLimitExt = null;
    private InputAssertion.IntentTime calendaringType;


    public CalendaringExtended(Calendaring calendaring) {
        setStartTime(calendaring.getStartTime());
        setStopTime(calendaring.getStopTime());
        setCostLimit(calendaring.getCostLimit());
    }

    @ApiModelProperty(value = "The maximum hourly cost of the service")
    @JsonProperty("calendaringType")
    public InputAssertion.IntentTime getCalendaringType() {
        return calendaringType;
    }

    public void setCalendaringType(InputAssertion.IntentTime calendaringType) {
        this.calendaringType = calendaringType;
    }


    @ApiModelProperty(value = "The maximum hourly cost of the service")
    @JsonProperty("cost_limit")
    public Double getCostLimitExt() {
        return costLimitExt;
    }

    public void setCostLimitExt(Double costLimit) {
        this.costLimitExt = costLimit;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CalendaringExtended calendaring = (CalendaringExtended) o;
        return Objects.equals(costLimitExt, calendaring.costLimitExt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(costLimitExt, calendaringType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Calendaring {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        if (getStartTime() != null) {
            sb.append("    startTime: ").append(toIndentedString(getStartTime())).append("\n");
        }
        if (getStopTime() != null) {
            sb.append("    stopTime: ").append(toIndentedString(getStopTime())).append("\n");
        }
        if (costLimitExt != null) {
            sb.append("    costLimit: ").append(toIndentedString(costLimitExt)).append("\n");
        }
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

