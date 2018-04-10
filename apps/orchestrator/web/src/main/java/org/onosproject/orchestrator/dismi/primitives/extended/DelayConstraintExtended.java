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

package org.onosproject.orchestrator.dismi.primitives.extended;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.onosproject.orchestrator.dismi.primitives.DelayConstraint;

import java.util.Objects;


/**
 * Maximum latency and maximum jitter are simple primitives that are interpreted
 * as one way latency/jitter or two-way latency/jitter depending on the Action
 * they are associated with (i.e. one-way for Path, two-way for Connection).
 **/


@ApiModel(description = "\"A DelayConstraintExtended is an inernal primitive " +
        "that extends the DelayConstraint with\" +\n" +
        "\" its associated parameters. It is created during the " +
        "resolution phase of an intent validation and\" +\n" +
        "\" resolution process.\")")
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-10-11T12:29:51.886Z")
public class DelayConstraintExtended extends DelayConstraint {

    private Double latencyExt = null;
    private Double jitterExt = null;

    /**
     **/
    public DelayConstraintExtended(DelayConstraint delayConstraint) {
        setLatency(delayConstraint.getLatency());
        setJitter(delayConstraint.getJitter());
    }

    @ApiModelProperty(required = true,
            value = "Latency constraint, specified as a time - FIXME: should we use date-time format?")
    @JsonProperty("latencyExt")
    public Double getLatencyExt() {
        return latencyExt;
    }

    public void setLatencyExt(Double latencyExt) {
        this.latencyExt = latencyExt;
    }


    @ApiModelProperty(required = true,
            value = "Jitter constraint, specified as a time - FIXME: should we use date-time format?")
    @JsonProperty("jitterExt")
    public Double getJitterExt() {
        return jitterExt;
    }

    public void setJitterExt(Double jitterExt) {
        this.jitterExt = jitterExt;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DelayConstraintExtended delayConstraint = (DelayConstraintExtended) o;
        return Objects.equals(latencyExt, delayConstraint.latencyExt) &&
                Objects.equals(jitterExt, delayConstraint.jitterExt) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latencyExt, jitterExt, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DelayConstraint {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    latency: ").append(toIndentedString(latencyExt)).append("\n");
        sb.append("    jitter: ").append(toIndentedString(jitterExt)).append("\n");
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

