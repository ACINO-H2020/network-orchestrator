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

import java.util.Date;
import java.util.Objects;


/**
 * The calendaring constraint provides the ability to request a service from and to a certain time,
 * or for a certain cost.  With this constraint, the application can define:
 * i) An open-ended service starting at a certain time but without a specified ending;
 * ii) A service with both start and end;
 * iii) A service terminating at a certain point, and
 * iv) A service that is not started unless the hourly cost is lower than specified.
 **/

@ApiModel(description = "Calendaring provides the ability to request a service from and to a certain " +
        "time, or for a certain cost.  With this feature, the application can define: " +
        "i) An open-ended service starting at a certain time but without a specified ending; " +
        " ii) A service with both start and end; " +
        " iii) A service terminating at a certain point, and " +
        " iv) A service that is not started unless the hourly cost is lower than specified.")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class Calendaring {

    private Date startTime = null;

    private Date stopTime = null;

    private String costLimit = null;
    private RecurrenceEnum recurrence = RecurrenceEnum.ONCE;

    /**
     * The time at which the service should start.
     **/
    public Calendaring startTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    @ApiModelProperty(required = true, value = "The time at which the service should start")
    @JsonProperty("start_time")
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * The time at which the service should end.
     **/
    public Calendaring stopTime(Date stopTime) {
        this.stopTime = stopTime;
        return this;
    }

    @ApiModelProperty(required = true, value = "The time at which the service should end")
    @JsonProperty("stop_time")
    public Date getStopTime() {
        return stopTime;
    }

    public void setStopTime(Date stopTime) {
        this.stopTime = stopTime;
    }

    /**
     * The maximum hourly cost of the service.
     **/
    public Calendaring costLimit(String costLimit) {
        this.costLimit = costLimit;
        return this;
    }

    @ApiModelProperty(value = "The maximum hourly cost of the service")
    @JsonProperty("cost_limit")
    public String getCostLimit() {
        return costLimit;
    }

    public void setCostLimit(String costLimit) {
        this.costLimit = costLimit;
    }

    public Calendaring recurrence(RecurrenceEnum recurrence) {
        this.recurrence = recurrence;
        return this;
    }

    /**
     * Get recurrence.
     *
     * @return recurrence
     **/
    @ApiModelProperty(value = "")
    public RecurrenceEnum getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(RecurrenceEnum recurrence) {
        this.recurrence = recurrence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Calendaring calendaring = (Calendaring) o;
        return Objects.equals(this.startTime, calendaring.startTime) &&
                Objects.equals(this.stopTime, calendaring.stopTime) &&
                Objects.equals(this.costLimit, calendaring.costLimit) &&
                Objects.equals(this.recurrence, calendaring.recurrence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, stopTime, costLimit, recurrence);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Calendaring {\n");

        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    stopTime: ").append(toIndentedString(stopTime)).append("\n");
        sb.append("    costLimit: ").append(toIndentedString(costLimit)).append("\n");
        sb.append("    recurrence: ").append(toIndentedString(recurrence)).append("\n");
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

    /**
     * Gets or Sets recurrence.
     */
    public enum RecurrenceEnum {
        ONCE("ONCE"),

        HOURLY("HOURLY"),

        DAYLY("DAYLY"),

        WEEKLY("WEEKLY");

        private String value;

        RecurrenceEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}

