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
import org.onosproject.orchestrator.dismi.primitives.BandwidthConstraint;

import java.util.Objects;


/**
 * The Bandwith constraint simply specifies the minimum guaranteed rate without going into details, allowing underlying
 * layers to set typical default values, or estimate them based on the rate (e.g. using a heuristics as bucket size
 * should correspond to number of packets send during time t at maximum rate). Bandwidth specifies only the minimum
 * guaranteed bandwidth, there may still be additional bandwidth available but ACINO does not provide any guarantees on
 * this bandwidth. The actual configuration of the network, policing/shaping mechanisms and accounting of resources
 * could also be affected by other parameters such as latency, as larger token buffers cause higher latencies in the
 * network.
 **/

@ApiModel(description = "\"A BandwidthConstraintExtended is an inernal primitive that extends the " +
        "BandwidthConstraint with\" +\n\" its associated parameters. It is created during the resolution phase of " +
        "an intent validation and\" +\n \" resolution process.\")")
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-10-10T16:12:51.886Z")
public class BandwidthConstraintExtended extends BandwidthConstraint {

    private Double bitrateExt = null; // Contains resolved bitrate in bits

    public BandwidthConstraintExtended(BandwidthConstraint bandwidthConstraint) {
        setBitrate(bandwidthConstraint.getBitrate());
    }

    /**
     * Bitrate.
     **/
    public BandwidthConstraintExtended bitrateExt(Double bitrateExt) {
        this.bitrateExt = bitrateExt;
        return this;
    }


    @ApiModelProperty(required = true, value = "BitrateExt")
    @JsonProperty("bitrateExt")
    public double getBitrateExt() {
        return bitrateExt;
    }

    public void setBitrateExt(Double bitrateExt) {
        this.bitrateExt = bitrateExt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BandwidthConstraintExtended bandwidthConstraintExtended = (BandwidthConstraintExtended) o;
        return Objects.equals(getBitrate(), bandwidthConstraintExtended.getBitrate()) &&
                Objects.equals(getBitrateExt(), bandwidthConstraintExtended.getBitrateExt()) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bitrateExt);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BandwidthConstraint {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    bitrate: ").append(toIndentedString(bitrateExt)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
