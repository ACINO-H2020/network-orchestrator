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
 * This constraint allows the Application to specify soft priorities, or goals, of the requested service. If there are
 * several network configurations that meet the requirements, priorities are used to choose the most appropriate one. If
 * multiple priorities are included they are taken into account by order.
 **/

@ApiModel(description = "This feature is a 'soft constraint' that lets the " +
        "Application specify goals for the requested " +
        "service. If there are several network " +
        "configurations that meet the requirements, " +
        "priorities are used to choose the most appropriate " +
        "one. If multiple priorities are included, they are " +
        "taken into account by order.")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class Priority {

    private PriorityEnum priority = PriorityEnum.BANDWIDTH;

    public Priority priority(PriorityEnum priority) {
        this.priority = priority;
        return this;
    }

    /**
     * choose between higher availability, lower cost, lower latency, lower jitter or higher bandwidth.
     *
     * @return priority
     **/
    @ApiModelProperty(required = true, value = "choose between higher availability, lower cost, " +
            "lower latency, lower jitter or higher bandwidth")
    @JsonProperty("priority")
    public PriorityEnum getPriority() {
        return priority;
    }

    public void setPriority(PriorityEnum priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Priority priority = (Priority) o;
        return Objects.equals(this.priority, priority.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Priority {\n");

        sb.append("    priority: ").append(toIndentedString(priority)).append("\n");
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

    /**
     * choose between higher availability, lower cost, lower latency, lower jitter or higher bandwidth.
     */
    public enum PriorityEnum {
        AVAILABILITY("availability"),
        COST("cost"),
        LATENCY("latency"),
        JITTER("jitter"),
        BANDWIDTH("bandwidth");
        private String value;

        PriorityEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
