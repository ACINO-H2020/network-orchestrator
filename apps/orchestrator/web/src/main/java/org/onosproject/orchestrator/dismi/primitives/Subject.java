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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A subject consists of a ConnectionPoint, an optional list of Selectors and an
 * optional list of Constraints.
 **/

@ApiModel(description = "A subject consists of a ConnectionPoint, an optional list of Selectors " +
        "and an optional list of Constraints.")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class Subject {

    private ConnectionPoint connectionPoint = null;
    private List<Selector> selectors = new ArrayList<Selector>();
    private List<Constraint> constraints = new ArrayList<Constraint>();


    /**
     **/
    public Subject connectionPoint(ConnectionPoint connectionPoint) {
        this.connectionPoint = connectionPoint;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("connection_point")
    public ConnectionPoint getConnectionPoint() {
        return connectionPoint;
    }

    public void setConnectionPoint(ConnectionPoint connectionPoint) {
        this.connectionPoint = connectionPoint;
    }


    /**
     * Optional list of Selectors specifying which traffic belongs to the
     * application class at this ingress connection point.
     **/
    public Subject selectors(List<Selector> selectors) {
        this.selectors = selectors;
        return this;
    }

    public Subject addSelectorsItem(Selector selectorsItem) {
        this.selectors.add(selectorsItem);
        return this;
    }

    @ApiModelProperty(value = "Optional list of Selectors specifying which traffic belongs to the application class " +
            "at this ingress connection point.")
    @JsonProperty("selectors")
    public List<Selector> getSelectors() {
        return selectors;
    }

    public void setSelectors(List<Selector> selectors) {
        this.selectors = selectors;
    }


    /**
     * Optional list of Constraints specifying the service characteristic that
     * the customer is requesting.
     **/
    public Subject constraints(List<Constraint> constraints) {
        this.constraints = constraints;
        return this;
    }

    public Subject addConstraintsItem(Constraint constraintsItem) {
        this.constraints.add(constraintsItem);
        return this;
    }

    @ApiModelProperty(value = "Optional list of Constraints specifying the service characteristic " +
            " that the customer is requesting: bandwidth, delay, calendaring, ....")
    @JsonProperty("constraints")
    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Subject subject = (Subject) o;
        return Objects.equals(connectionPoint, subject.connectionPoint) &&
                Objects.equals(selectors, subject.selectors) &&
                Objects.equals(constraints, subject.constraints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionPoint, selectors, constraints);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Subject {\n");
        sb.append("    connectionPoint: ").append(toIndentedString(connectionPoint)).append("\n");
        sb.append("    selectors: ").append(toIndentedString(selectors)).append("\n");
        sb.append("    constraints: ").append(toIndentedString(constraints)).append("\n");
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

