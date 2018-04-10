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

package org.onosproject.orchestrator.netrap.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Name and description of an algorithm
 **/

@ApiModel(description = "Name and description of an algorithm")

public class NetRapAlgo {

    private String description = null;
    private String name = null;

    /**
     * Description of the algorithm
     **/
    public NetRapAlgo description(String description) {
        this.description = description;
        return this;
    }


    @ApiModelProperty(example = "null", required = true, value = "Description of the algorithm")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Name of the algorithm
     **/
    public NetRapAlgo name(String name) {
        this.name = name;
        return this;
    }


    @ApiModelProperty(example = "null", required = true, value = "Name of the algorithm")
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
        NetRapAlgo netRapAlgo = (NetRapAlgo) o;
        return Objects.equals(description, netRapAlgo.description) &&
                Objects.equals(name, netRapAlgo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NetRapAlgo {\n");

        sb.append("    description: ").append(toIndentedString(description)).append("\n");
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
