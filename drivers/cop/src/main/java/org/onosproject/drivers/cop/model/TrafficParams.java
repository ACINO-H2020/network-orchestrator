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

package org.onosproject.drivers.cop.model;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrafficParams implements Serializable {

    private final Integer latency;
    private final Double osnr;
    private final Double estimatedPLR;

    public enum QosClassEnum {
        GOLD, SILVER
    }

    private final QosClassEnum qosClass;
    private final Integer reservedBandwidth;

    public TrafficParams(@JsonProperty("latency") Integer latency,
                         @JsonProperty("OSNR") Double osnr,
                         @JsonProperty("estimatedPLR") Double estimatedPlr,
                         @JsonProperty("qosClass") QosClassEnum qosClass,
                         @JsonProperty("reservedBandwidth") Integer reservedBandwidth) {
        this.latency = latency;
        this.osnr = osnr;
        this.estimatedPLR = estimatedPlr;
        this.qosClass = qosClass;
        this.reservedBandwidth = reservedBandwidth;
    }

    @JsonProperty("latency")
    public Integer getLatency() {
        return latency;
    }

    @JsonProperty("OSNR")
    public Double getOsnr() {
        return osnr;
    }

    @JsonProperty("estimatedPLR")
    public Double getEstimatedPlr() {
        return estimatedPLR;
    }

    @JsonProperty("qosClass")
    public QosClassEnum getQosClass() {
        return qosClass;
    }

    @JsonProperty("reservedBandwidth")
    public Integer getReservedBandwidth() {
        return reservedBandwidth;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TrafficParams trafficParams = (TrafficParams) o;

        return Objects.equals(latency, trafficParams.latency)
                && Objects.equals(osnr, trafficParams.osnr)
                && Objects.equals(estimatedPLR, trafficParams.estimatedPLR)
                && Objects.equals(qosClass, trafficParams.qosClass)
                && Objects.equals(reservedBandwidth,
                                  trafficParams.reservedBandwidth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latency, osnr, estimatedPLR, qosClass,
                            reservedBandwidth);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TrafficParams {\n");
        sb.append("  latency: ").append(toIndentedString(latency)).append("\n");
        sb.append("  OSNR: ").append(toIndentedString(osnr)).append("\n");
        sb.append("  estimatedPLR: ").append(toIndentedString(estimatedPLR))
                .append("\n");
        sb.append("  qosClass: ").append(toIndentedString(qosClass))
                .append("\n");
        sb.append("  reservedBandwidth: ")
                .append(toIndentedString(reservedBandwidth)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n  ");
    }
}
