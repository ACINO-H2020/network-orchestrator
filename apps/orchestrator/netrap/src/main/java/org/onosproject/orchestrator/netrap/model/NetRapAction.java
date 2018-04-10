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


import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class NetRapAction {


    private String action = null;
    private NetRapDemand demand = null;

    /**
     * Enumerator for the type of action, NEW, ROUTE, MOVE, UPDATE, or FAIL
     **/
    public NetRapAction action(String action) {
        this.action = action;
        return this;
    }


    @ApiModelProperty(example = "null", required = true, value = "Enumerator for the type of action, NEW, ROUTE, MOVE, UPDATE, or FAIL")
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    /**
     **/
    public NetRapAction demand(NetRapDemand demand) {
        this.demand = demand;
        return this;
    }


    @ApiModelProperty(example = "null", required = true, value = "")
    public NetRapDemand getDemand() {
        return demand;
    }

    public void setDemand(NetRapDemand demand) {
        this.demand = demand;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetRapAction netRapAction = (NetRapAction) o;
        return Objects.equals(action, netRapAction.action) &&
                Objects.equals(demand, netRapAction.demand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, demand);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NetRapAction {\n");

        sb.append("    action: ").append(toIndentedString(action)).append("\n");
        sb.append("    demand: ").append(toIndentedString(demand)).append("\n");
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
