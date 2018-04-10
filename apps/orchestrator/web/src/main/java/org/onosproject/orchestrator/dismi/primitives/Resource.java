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
 * The resource is a link sent by the server to tell the application where it
 * can poll for information about the request. This is used in particular for
 * asynchronous responses.
 **/

@ApiModel(description = "The resource is a link sent by the server to tell " +
        "the application where it can poll for information about the " +
        "request. This is used in particular for asynchronous responses.")
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class Resource {

    private static final String RESOURCEPATH = "Dismi";
    private static final String SERVICEROOTNAME = "Service_";
    private static final String INTENTROOTNAME = "Intent_";
    private static final String ACIINTENTROOTNAME = "AciIntent_";
    private static final String SEPARATOR = "-";
    // This is the resource number when a requested service is not a service
    //  This should be intercepted at the REST API level instead to reply with a 404 code or similar!
    private static final String ERROR_NOT_A_SERVICE = "";

    private String resource = null;

    private Tracker tracker = null;


    /**
     **/
    public Resource resource(String resource) {
        this.resource = resource;
        return this;
    }


    @ApiModelProperty(value = "")
    @JsonProperty("resource")
    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Resource tracker(Tracker tracker) {
        this.tracker = tracker;
        return this;
    }

    public void serviceId(int id) {
        resource = RESOURCEPATH + SEPARATOR + SERVICEROOTNAME + Integer.toString(id);
    }

    public void intentId(int serviceId, int intentId) {
        resource = RESOURCEPATH + SEPARATOR + SERVICEROOTNAME + Integer.toString(serviceId)
                + SEPARATOR + INTENTROOTNAME + Integer.toString(intentId);
    }


    /**
     * Get tracker.
     *
     * @return tracker
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("tracker")
    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Resource resource = (Resource) o;
        return Objects.equals(this.resource, resource.resource) &&
                Objects.equals(this.tracker, resource.tracker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, tracker);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Resource {\n");
        sb.append("    resource: ").append(toIndentedString(resource)).append("\n");
        sb.append("    tracker: ").append(toIndentedString(tracker)).append("\n");
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

    public void setInvalid() {
        resource = ERROR_NOT_A_SERVICE;
    }

    public String getResourcePath() {
        String resourceArray[] = resource.split(SEPARATOR);

        if (resourceArray.length > 0) {
            return resourceArray[0];
        }

        return "";
    }

    public String getServiceId() {
        String resourceArray[] = resource.split(SEPARATOR);

        if (resourceArray.length > 1) {
            return resourceArray[1];
        }

        return "";
    }

    public String getIntentId() {
        String resourceArray[] = resource.split(SEPARATOR);

        if (resourceArray.length > 2) {
            return resourceArray[2];
        }

        return "";
    }

    public boolean isValid() {
        String serviceId = getServiceId();

        if ((serviceId.length() > 0) && serviceId.compareTo(ERROR_NOT_A_SERVICE) != 0) {
            return true;
        }

        return false;
    }

    public boolean isPathValid() {
        String path = getResourcePath();

        if (path.compareTo(RESOURCEPATH) != 0) {
            return false;
        }

        return true;
    }

    public boolean isServiceRootNameValid() {
        String serviceRoot = getServiceId();

        if (serviceRoot.startsWith(SERVICEROOTNAME)) {
            return true;
        }

        return false;
    }

    public boolean isIntentRootNameValid() {
        String intentRoot = getIntentId();

        if (intentRoot.startsWith(INTENTROOTNAME)) {
            return true;
        }

        return false;
    }

    public String getFullyQualifiedServiceId() {
        return RESOURCEPATH + "-" + getServiceId();
    }

    public boolean isServiceId() {
        String resourceArray[] = resource.split(SEPARATOR);

        if (resourceArray.length != 2) {
            return false;
        }

        if (resourceArray[1].startsWith(SERVICEROOTNAME) && (resourceArray[1].length() > SERVICEROOTNAME.length())) {
            return true;
        }

        return false;
    }
}

