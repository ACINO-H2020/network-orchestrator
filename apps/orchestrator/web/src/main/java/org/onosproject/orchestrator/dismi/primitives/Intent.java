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
 * An Intent consists of an Action, an optional list of Selectors, an optional
 * list of Constraints, an optional list of Calendaring, and an optional list of
 * Priorities.
 **/

@ApiModel(description = "An Intent consists of an Action, an optional " +
        "list of Selectors, an optional list of Constraints, " +
        "an optional list of Calendaring, and an optional " +
        "list of Priorities.")
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")

public class Intent {

    private ServerInfo serverInfo = null;
    private String displayName = null;
    private Boolean isNegotiatable = false;
    private String intentServiceProviderKey = null;
    private String providerName;
    private Action action = null;
    private List<Selector> selectors = new ArrayList<Selector>();
    private List<Constraint> constraints = new ArrayList<Constraint>();
    private List<Calendaring> calendaring = null;
    private List<Priority> priorities = null;


    /**
     * Primitive to let the server provide information to the client about the Service.
     **/
    public Intent serverInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        return this;
    }


    @ApiModelProperty(value = "Primitive to let the server provide information to the client about the Service.")
    @JsonProperty("server_info")
    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    /*
     *   Direct access to the id and the status
     */
    public void setIntentId(String id) {
        if (null == serverInfo) {
            this.serverInfo = new ServerInfo();
        }
        serverInfo.setId(id);
    }

    public String getIntentServiceProviderKey() {
        return intentServiceProviderKey;
    }

    public void setIntentServiceProviderKey(String intentServiceProviderKey) {
        this.intentServiceProviderKey = intentServiceProviderKey;
    }

    public String getIntentId() {
        if (null == serverInfo) {
            this.serverInfo = new ServerInfo();
        }
        return serverInfo.getId();
    }

    public void setIntentStatus(DismiIntentState status) {
        if (null == serverInfo) {
            this.serverInfo = new ServerInfo();
        }
        serverInfo.setStatus(status);
    }

    public DismiIntentState getIntentStatus() {
        if (null == serverInfo) {
            this.serverInfo = new ServerInfo();
        }
        return serverInfo.getStatus();
    }


    /**
     * Optional name that the application may give to the intent.
     **/
    public Intent displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderName() {
        return providerName;
    }

    @ApiModelProperty(value = "Optional name that the application may give " +
            "to the intent")
    @JsonProperty("display_name")
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @ApiModelProperty(required = true, value = "Flag telling DISMI whether the negotiation can be initiated" +
            "if the Intent constraints can't be fulfilled.")
    @JsonProperty("isNegotiatable")
    public Boolean getIsNegotiatable() {
        return isNegotiatable;
    }

    public void setIsNegotiatable(Boolean isNegotiatable) {
        this.isNegotiatable = isNegotiatable;
    }


    /**
     **/
    public Intent action(Action action) {
        this.action = action;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty("action")
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }


    /**
     * Optional list of selectors that apply to the Intent.
     **/
    public Intent selectors(List<Selector> selectors) {
        this.selectors = selectors;
        return this;
    }

    @ApiModelProperty(required = false, value = "Optional list of selectors that apply to the Intent.")
    @JsonProperty("selectors")
    public List<Selector> getSelectors() {
        return selectors;
    }

    public void setSelectors(List<Selector> selectors) {
        this.selectors = selectors;
    }

    public Intent addSelectorsItem(Selector selectorsItem) {
        this.selectors.add(selectorsItem);
        return this;
    }

    /**
     **/
    public Intent constraints(List<Constraint> constraints) {
        this.constraints = constraints;
        return this;
    }


    @ApiModelProperty(required = false,
            value = "Optional list of constraints that apply " +
                    "to the Intent.")
    @JsonProperty("constraints")
    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    public Intent addConstraintsItem(Constraint constraintsItem) {
        this.constraints.add(constraintsItem);
        return this;
    }

    /**
     **/
    public Intent calendaring(List<Calendaring> calendaring) {
        this.calendaring = calendaring;
        return this;
    }


    @ApiModelProperty(required = false, value = "")
    @JsonProperty("calendaring")
    public List<Calendaring> getCalendaring() {
        return calendaring;
    }

    public void setCalendaring(List<Calendaring> calendaring) {
        this.calendaring = calendaring;
    }

    public Intent addCalendaringItem(Calendaring calendaringItem) {
        this.calendaring.add(calendaringItem);
        return this;
    }

    /**
     **/
    public Intent priorities(List<Priority> priorities) {
        this.priorities = priorities;
        return this;
    }


    @ApiModelProperty(required = false, value = "")
    @JsonProperty("priorities")
    public List<Priority> getPriorities() {
        return priorities;
    }

    public void setPriorities(List<Priority> priorities) {
        this.priorities = priorities;
    }

    public Intent addPrioritiesItem(Priority prioritiesItem) {
        this.priorities.add(prioritiesItem);
        return this;
    }

    public boolean updateIntent(Intent p) {
        this.serverInfo = p.serverInfo;
        this.displayName = p.displayName;
        this.isNegotiatable = p.isNegotiatable;
        this.action = p.action;
        this.selectors = p.selectors;
        this.constraints = p.constraints;
        this.calendaring = p.calendaring;
        this.priorities = p.priorities;

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Intent intent = (Intent) o;
        return Objects.equals(this.serverInfo, intent.serverInfo) &&
                Objects.equals(this.displayName, intent.displayName) &&
                Objects.equals(this.isNegotiatable, intent.isNegotiatable) &&
                Objects.equals(this.action, intent.action) &&
                Objects.equals(this.selectors, intent.selectors) &&
                Objects.equals(this.constraints, intent.constraints) &&
                Objects.equals(this.calendaring, intent.calendaring) &&
                Objects.equals(this.priorities, intent.priorities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverInfo, displayName, action, selectors, constraints, calendaring, priorities);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Intent {\n");
        sb.append("    serverInfo: ").append(toIndentedString(serverInfo)).append("\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    isNegotiatable: ").append(isNegotiatable.booleanValue()).append("\n");
        sb.append("    action: ").append(toIndentedString(action)).append("\n");
        sb.append("    selectors: ").append(toIndentedString(selectors)).append("\n");
        sb.append("    constraints: ").append(toIndentedString(constraints)).append("\n");
        sb.append("    calendaring: ").append(toIndentedString(calendaring)).append("\n");
        sb.append("    priorities: ").append(toIndentedString(priorities)).append("\n");
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

