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

package org.onosproject.drivers.tapi.topology;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Provides information on latency characteristic for a particular stated trafficProperty.
 */
@JsonInclude(Include.NON_NULL)
public class LatencyCharacteristic implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * A TopologicalEntity suffers delay caused by the realization of the servers (e.g. distance related; FEC encoding etc.) along with some client specific processing. This is the total average latency effect of the TopologicalEntity
   */
  private final String fixedLatencyCharacteristic;

  /**
   * High frequency deviation from true periodicity of a signal and therefore a small high rate of change of transfer latency.
   * Applies to TDM systems (and not packet).
   */
  private final String jitterCharacteristic;

  /**
   * Low frequency deviation from true periodicity of a signal and therefore a small low rate of change of transfer latency.
   * Applies to TDM systems (and not packet).
   */
  private final String wanderCharacteristic;

  /**
   * The identifier of the specific traffic property to which the queuing latency applies.
   */
  private final String trafficPropertyName;

  /**
   * The specific queuing latency for the traffic property.
   */
  private final String trafficPropertyQueingLatency;


  @JsonCreator
  public LatencyCharacteristic (
    @JsonProperty("fixed-latency-characteristic") String fixedLatencyCharacteristic,
    @JsonProperty("jitter-characteristic") String jitterCharacteristic,
    @JsonProperty("wander-characteristic") String wanderCharacteristic,
    @JsonProperty("traffic-property-name") String trafficPropertyName,
    @JsonProperty("traffic-property-queing-latency") String trafficPropertyQueingLatency){
    this.fixedLatencyCharacteristic = fixedLatencyCharacteristic;
    this.jitterCharacteristic = jitterCharacteristic;
    this.wanderCharacteristic = wanderCharacteristic;
    this.trafficPropertyName = trafficPropertyName;
    this.trafficPropertyQueingLatency = trafficPropertyQueingLatency;
  }


  @JsonProperty("fixed-latency-characteristic")
  public String getFixedLatencyCharacteristic(){
    return this.fixedLatencyCharacteristic;
  }

  @JsonProperty("jitter-characteristic")
  public String getJitterCharacteristic(){
    return this.jitterCharacteristic;
  }

  @JsonProperty("wander-characteristic")
  public String getWanderCharacteristic(){
    return this.wanderCharacteristic;
  }

  @JsonProperty("traffic-property-name")
  public String getTrafficPropertyName(){
    return this.trafficPropertyName;
  }

  @JsonProperty("traffic-property-queing-latency")
  public String getTrafficPropertyQueingLatency(){
    return this.trafficPropertyQueingLatency;
  }


  @Override
  public int hashCode() {
    return Objects.hash(fixedLatencyCharacteristic, jitterCharacteristic, wanderCharacteristic, trafficPropertyName, trafficPropertyQueingLatency);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LatencyCharacteristic that = (LatencyCharacteristic) o;
    return Objects.equals(this.fixedLatencyCharacteristic, that.fixedLatencyCharacteristic) &&
       Objects.equals(this.jitterCharacteristic, that.jitterCharacteristic) &&
       Objects.equals(this.wanderCharacteristic, that.wanderCharacteristic) &&
       Objects.equals(this.trafficPropertyName, that.trafficPropertyName) &&
       Objects.equals(this.trafficPropertyQueingLatency, that.trafficPropertyQueingLatency);
  }

}