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
 * This is the Connection action that creates a bidirectional connection between
 * a source and a destination.
 **/

@ApiModel(description = "This is the Connection action that creates a " +
        "bidirectional connection between a source and " +
        "a destination.",
        parent = Action.class)

@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class Connection extends Action {

    private Subject source = null;

    private Subject destination = null;

    public Connection() {
    }

    /**
     **/
    public Connection source(Subject source) {
        this.source = source;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty("source")
    public Subject getSource() {
        return source;
    }

    public void setSource(Subject source) {
        this.source = source;
    }


    /**
     **/
    public Connection destination(Subject destination) {
        this.destination = destination;
        return this;
    }


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
        Connection connection = (Connection) o;
        return Objects.equals(this.source, connection.source) &&
                Objects.equals(this.destination, connection.destination) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), source, destination);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Connection {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        if (source != null) {
            sb.append("    source: ").append(toIndentedString(source)).append("\n");
        }
        if (destination != null) {
            sb.append("    destination: ").append(toIndentedString(destination)).append("\n");
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

