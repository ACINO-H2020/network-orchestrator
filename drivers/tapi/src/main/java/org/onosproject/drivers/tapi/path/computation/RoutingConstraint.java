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

package org.onosproject.drivers.tapi.path.computation;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.onosproject.drivers.tapi.common.LayerProtocolName;
import org.onosproject.drivers.tapi.common.LocalClass;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.UniversalId;
import org.onosproject.drivers.tapi.topology.Capacity;
import org.onosproject.drivers.tapi.topology.CostCharacteristic;
import org.onosproject.drivers.tapi.topology.LatencyCharacteristic;


@JsonInclude(Include.NON_NULL)
public class RoutingConstraint extends LocalClass implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final Capacity requestedCapacity;

  /**
   * An abstract value the meaning of which is mutually agreed – typically represents metrics such as - Class of service, priority, resiliency, availability
   */
  private final String serviceLevel;

  
  private final List<LayerProtocolName> pathLayer;

  /**
   * The list of costs where each cost relates to some aspect of the TopologicalEntity.
   */
  private final List<CostCharacteristic> costCharacteristic;

  /**
   * The effect on the latency of a queuing process. This only has significant effect for packet based systems and has a complex characteristic.
   */
  private final List<LatencyCharacteristic> latencyCharacteristic;

  
  private final List<UniversalId> includeTopology;

  
  private final List<UniversalId> avoidTopology;


  @JsonCreator
  public RoutingConstraint (@JsonProperty("local-id") String localId,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("requested-capacity") Capacity requestedCapacity,
    @JsonProperty("service-level") String serviceLevel,
    @JsonProperty("path-layer") List<LayerProtocolName> pathLayer,
    @JsonProperty("cost-characteristic") List<CostCharacteristic> costCharacteristic,
    @JsonProperty("latency-characteristic") List<LatencyCharacteristic> latencyCharacteristic,
    @JsonProperty("include-topology") List<UniversalId> includeTopology,
    @JsonProperty("avoid-topology") List<UniversalId> avoidTopology){
    super(localId, name);
    this.requestedCapacity = requestedCapacity;
    this.serviceLevel = serviceLevel;
    this.pathLayer = pathLayer != null ? ImmutableList.copyOf(pathLayer) : ImmutableList.of();
    this.costCharacteristic = costCharacteristic != null ? ImmutableList.copyOf(costCharacteristic) : ImmutableList.<CostCharacteristic>of();
    this.latencyCharacteristic = latencyCharacteristic != null ? ImmutableList.copyOf(latencyCharacteristic) : ImmutableList.<LatencyCharacteristic>of();
    this.includeTopology = includeTopology != null ? ImmutableList.copyOf(includeTopology) : ImmutableList.of();
    this.avoidTopology = avoidTopology != null ? ImmutableList.copyOf(avoidTopology) : ImmutableList.of();
  }


  @JsonProperty("requested-capacity")
  public Capacity getRequestedCapacity(){
    return this.requestedCapacity;
  }

  @JsonProperty("service-level")
  public String getServiceLevel(){
    return this.serviceLevel;
  }

  @JsonProperty("path-layer")
  public List<LayerProtocolName> getPathLayer(){
    return this.pathLayer;
  }

  @JsonProperty("cost-characteristic")
  public List<CostCharacteristic> getCostCharacteristic(){
    return this.costCharacteristic;
  }

  @JsonProperty("latency-characteristic")
  public List<LatencyCharacteristic> getLatencyCharacteristic(){
    return this.latencyCharacteristic;
  }

  @JsonProperty("include-topology")
  public List<UniversalId> getIncludeTopology(){
    return this.includeTopology;
  }

  @JsonProperty("avoid-topology")
  public List<UniversalId> getAvoidTopology(){
    return this.avoidTopology;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), requestedCapacity, serviceLevel, pathLayer, costCharacteristic, latencyCharacteristic, includeTopology, avoidTopology);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RoutingConstraint that = (RoutingConstraint) o;
    return super.equals(o) &&
       Objects.equals(this.requestedCapacity, that.requestedCapacity) &&
       Objects.equals(this.serviceLevel, that.serviceLevel) &&
       Objects.equals(this.pathLayer, that.pathLayer) &&
       Objects.equals(this.costCharacteristic, that.costCharacteristic) &&
       Objects.equals(this.latencyCharacteristic, that.latencyCharacteristic) &&
       Objects.equals(this.includeTopology, that.includeTopology) &&
       Objects.equals(this.avoidTopology, that.avoidTopology);
  }

}