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
 * Maximum latency and maximum jitter are simple primitives that are interpreted
 * as one way latency/jitter or two-way latency/jitter depending on the Action
 * they are associated with (i.e. one-way for Path, two-way for Connection).
 **/

@ApiModel(description = "Maximum latency and maximum jitter are simple " +
        "primitives that are interpreted as one way " +
        "latency/jitter or two-way latency/jitter depending " +
        "on the Action they are associated with " +
        "(i.e. one-way for Path, two-way for Connection).")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class DelayConstraint extends Constraint {

    private String latency = null;

    private String jitter = null;


    /**
     **/
    public DelayConstraint() {
    }

    /**
     * Latency constraint, specified as a time -  specify the uning (s, ms, us).
     **/
    public DelayConstraint latency(String latency) {
        this.latency = latency;
        return this;
    }

    /**
     * Latency constraint, specified as a time - specify the uning (s, ms, us).
     *
     * @return latency
     **/
    @ApiModelProperty(required = true,
            value = "Latency constraint, specified as a time - " +
                    "specify the uning (s, ms, us)")
    @JsonProperty("latency")
    public String getLatency() {
        return latency;
    }

    public void setLatency(String latency) {
        this.latency = latency;
    }


    /**
     * Jitter constraint, specified as a time - specify the uning (s, ms, us).
     **/
    public DelayConstraint jitter(String jitter) {
        this.jitter = jitter;
        return this;
    }


    @ApiModelProperty(required = true, value = "Jitter constraint, specified as a time - specify the uning (s, ms, us)")
    @JsonProperty("jitter")
    public String getJitter() {
        return jitter;
    }

    public void setJitter(String jitter) {
        this.jitter = jitter;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DelayConstraint delayConstraint = (DelayConstraint) o;
        return Objects.equals(this.latency, delayConstraint.latency) &&
                Objects.equals(this.jitter, delayConstraint.jitter) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latency, jitter, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DelayConstraint {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    latency: ").append(toIndentedString(latency)).append("\n");
        sb.append("    jitter: ").append(toIndentedString(jitter)).append("\n");
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

