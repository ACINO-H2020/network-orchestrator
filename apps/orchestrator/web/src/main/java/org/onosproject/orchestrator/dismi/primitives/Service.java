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
 * A Service consists of a list of Intents, containing at least one.
 **/

@ApiModel(description = "A Service consists of a list of Intents, containing " +
        "at least one.")
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class Service {

    private ServerInfo serverInfo = null;
    private String displayName = null;
    private List<Intent> intents = new ArrayList<Intent>();


    /**
     * Primitive to let the server provide information to the client about the Service.
     **/
    public Service serverInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        return this;
    }


    @ApiModelProperty(required = false, value = "Primitive to let the server provide information to the client about the Service.")
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
    public void setServiceId(String id) {
        if (null == serverInfo) {
            this.serverInfo = new ServerInfo();
        }
        serverInfo.setId(id);
    }

    public String getServiceId() {
        if (null == serverInfo) {
            this.serverInfo = new ServerInfo();
        }
        return serverInfo.getId();
    }

    public void setServiceStatus(DismiIntentState status) {
        if (null == serverInfo) {
            this.serverInfo = new ServerInfo();
        }
        serverInfo.setStatus(status);
    }

    public DismiIntentState getServiceStatus() {
        if (null == serverInfo) {
            this.serverInfo = new ServerInfo();
        }
        return serverInfo.getStatus();
    }


    /**
     * Optional name that the application may give to the service.
     **/
    public Service displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }


    @ApiModelProperty(value = "Optional name that the application may give to the service")
    @JsonProperty("display_name")
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    /**
     * List of intents associated with this service. At least one intent needs
     * to be present.
     **/
    public Service intents(List<Intent> intents) {
        this.intents = intents;
        return this;
    }

    public Service addIntentsItem(Intent intentsItem) {
        this.intents.add(intentsItem);
        return this;
    }

    @ApiModelProperty(value = "List of intents associated with this service. " +
            "At least one intent needs to be present.")
    @JsonProperty("intents")
    public List<Intent> getIntents() {
        return intents;
    }

    public void setIntents(List<Intent> intents) {
        this.intents = intents;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Service service = (Service) o;
        return Objects.equals(serverInfo, service.serverInfo) &&
                Objects.equals(displayName, service.displayName) &&
                Objects.equals(intents, service.intents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverInfo, displayName, intents);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Service {\n");

        sb.append("    serviceInfo: ").append(toIndentedString(serverInfo.toString())).append("\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    intents: ").append(toIndentedString(intents)).append("\n");
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
