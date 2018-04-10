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
 * This is the Aggregate action that creates unidirectional connections from several sources to a unique destination.
 **/

@ApiModel(description = "This is the Aggregate action that creates unidirectional connections from several sources " +
        "to a unique destination.",
        parent = Action.class)

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class Aggregate extends Action {
    private List<Subject> source = new ArrayList<Subject>();

    private Subject destination = null;

    public Aggregate() {
    }

    public Aggregate source(List<Subject> source) {
        this.source = source;
        return this;
    }

    public Aggregate addSourceItem(Subject sourceItem) {
        this.source.add(sourceItem);
        return this;
    }

    /**
     * Source subject.
     *
     * @return source
     **/
    @ApiModelProperty(required = true, value = "Source subject")
    @JsonProperty("source")
    public List<Subject> getSource() {
        return source;
    }

    public void setSource(List<Subject> source) {
        this.source = source;
    }

    public Aggregate destination(Subject destination) {
        this.destination = destination;
        return this;
    }

    /**
     * Get destination.
     *
     * @return destination
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("destination")
    public Subject getDestination() {
        return destination;
    }

    public void setDestination(Subject destination) {
        this.destination = destination;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Aggregate aggregate = (Aggregate) o;
        return Objects.equals(this.source, aggregate.source) &&
                Objects.equals(this.destination, aggregate.destination) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Aggregate {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    source: ").append(toIndentedString(source)).append("\n");
        sb.append("    destination: ").append(toIndentedString(destination)).append("\n");
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

