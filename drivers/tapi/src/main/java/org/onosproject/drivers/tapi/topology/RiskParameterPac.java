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

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.onosproject.drivers.tapi.topology.RiskCharacteristic;

/**
 * The risk characteristics of a TopologicalEntity come directly from the underlying physical realization. 
 * The risk characteristics propagate from the physical realization to the client and from the server layer to the client layer, this propagation may be modified by protection.
 * A TopologicalEntity may suffer degradation or failure as a result of a problem in a part of the underlying realization.
 * The realization can be partitioned into segments which have some relevant common failure modes.
 * There is a risk of failure/degradation of each segment of the underlying realization.
 * Each segment is a part of a larger physical/geographical unit that behaves as one with respect to failure (i.e. a failure will have a high probability of impacting the whole unit (e.g. all cables in the same duct).
 * Disruptions to that larger physical/geographical unit will impact (cause failure/errors to) all TopologicalEntities that use any part of that larger physical/geographical entity.
 * Any TopologicalEntity that uses any part of that larger physical/geographical unit will suffer impact and hence each TopologicalEntity shares risk.
 * The identifier of each physical/geographical unit that is involved in the realization of each segment of a Topological entity can be listed in the RiskParameter_Pac of that TopologicalEntity.
 * A segment has one or more risk characteristic.
 * Shared risk between two TopologicalEntities compromises the integrity of any solution that use one of those TopologicalEntity as a backup for the other.
 * Where two TopologicalEntities have a common risk characteristic they have an elevated probability of failing simultaneously compared to two TopologicalEntities that do not share risk characteristics.
 */
@JsonInclude(Include.NON_NULL)
public class RiskParameterPac implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * A list of risk characteristics for consideration in an analysis of shared risk. Each element of the list represents a specific risk consideration.
   */
  private final List<RiskCharacteristic> riskCharacteristic;


  @JsonCreator
  public RiskParameterPac (
    @JsonProperty("risk-characteristic") List<RiskCharacteristic> riskCharacteristic){
    this.riskCharacteristic = riskCharacteristic != null ? ImmutableList.copyOf(riskCharacteristic) : ImmutableList.<RiskCharacteristic>of();
  }


  @JsonProperty("risk-characteristic")
  public List<RiskCharacteristic> getRiskCharacteristic(){
    return this.riskCharacteristic;
  }


  @Override
  public int hashCode() {
    return Objects.hash(riskCharacteristic);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RiskParameterPac that = (RiskParameterPac) o;
    return Objects.equals(this.riskCharacteristic, that.riskCharacteristic);
  }

}