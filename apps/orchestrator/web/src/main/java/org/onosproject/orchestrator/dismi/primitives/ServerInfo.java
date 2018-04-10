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
 * A ServerInfo contains data provided by the server to the client inside other primitives (Service and Intent).
 **/

@ApiModel(description = "A ServerInfo contains data provided by the server to the client" +
        " inside other primitives (Service and Intent).")
@javax.annotation.Generated(
        value = "manually",
        date = "2016-11-16T16:03:00.000Z")

public class ServerInfo {
    private String id = null;
    private DismiIntentState status = DismiIntentState.UNKNOWN;

    public ServerInfo() {
        id = "";
    }


    /**
     * Unique identifier representing a specific service available.
     **/
    public ServerInfo id(String id) {
        this.id = id;
        return this;
    }


    @ApiModelProperty(required = false, value = "Unique identifier representing a specific service or Intent")
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Current status of the Service or Intent.
     **/

    public ServerInfo status(DismiIntentState status) {
        this.status = status;
        return this;
    }


    @ApiModelProperty(required = false, value = "Enum representing the current state of a Service or intent")
    @JsonProperty("status")
    public DismiIntentState getStatus() {
        return status;
    }

    public void setStatus(DismiIntentState status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerInfo serverInfo = (ServerInfo) o;
        return Objects.equals(id, serverInfo.id) &&
                Objects.equals(status, serverInfo.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServerStatus {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
