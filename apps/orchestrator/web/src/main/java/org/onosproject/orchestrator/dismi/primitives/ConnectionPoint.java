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
 * A ConnectionPoint is a customer edge of the ACINO network in an abstract way. It has a relevant meaning
 * within the Application and may be for example \&quot;Office-Berlin\&quot;, \&quot;Client-A\&quot;, etc.
 **/

/*
@ApiModel(description = "A ConnectionPoint is a customer edge of the ACINO network in an abstract way." +
                        " It has a relevant meaning within the Application and may be for example " +
                        "\"Office-Berlin\", \"Client-A\", etc. ConnectionPoints can refer to multiple EndPoints" +
                        " which are network points relevant to the ACINO orchestrator, such as an IP subnet " +
                        " on a router port, a port on a switch, or a lambda or fiber on an optical device." +
                        " The mapping between Connection- and EndPoints has to be done a priori, for example" +
                        " as part of a contract negotiation between the ACINO provider and the Application owner." +
                        " Additionally, a separate interface through the MLTPI or through DISMI may be provided" +
                        " for mappings to be updated during runtime by authorized personnel.")
*/
@ApiModel(description = "A ConnectionPoint is a customer edge of the ACINO " +
        "network in an abstract way. It has a relevant " +
        "meaning within the Application and may be for " +
        "example \"Office-Berlin\", \"Client-A\", etc.")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class ConnectionPoint {
    private String name = null;

    public ConnectionPoint name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Connection point Unique name (for the application).
     *
     * @return name
     **/
    @ApiModelProperty(value = "Connection point Unique name (for the application)")
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConnectionPoint connectionPoint = (ConnectionPoint) o;
        return Objects.equals(this.name, connectionPoint.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConnectionPoint {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
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

