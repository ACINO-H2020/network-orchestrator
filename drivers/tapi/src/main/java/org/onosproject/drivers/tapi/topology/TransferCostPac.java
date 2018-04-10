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
import org.onosproject.drivers.tapi.topology.CostCharacteristic;

/**
 * The cost characteristics of a TopologicalEntity not necessarily correlated to the cost of the underlying physical realization. 
 * They may be quite specific to the individual TopologicalEntity e.g. opportunity cost. Relates to layer capacity
 * There may be many perspectives from which cost may be considered  for a particular TopologicalEntity and hence many specific costs and potentially cost algorithms. 
 * Using an entity will incur a cost. 
 */
@JsonInclude(Include.NON_NULL)
public class TransferCostPac implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The list of costs where each cost relates to some aspect of the TopologicalEntity.
   */
  private final List<CostCharacteristic> costCharacteristic;


  @JsonCreator
  public TransferCostPac (
    @JsonProperty("cost-characteristic") List<CostCharacteristic> costCharacteristic){
    this.costCharacteristic = costCharacteristic != null ? ImmutableList.copyOf(costCharacteristic) : ImmutableList.<CostCharacteristic>of();
  }


  @JsonProperty("cost-characteristic")
  public List<CostCharacteristic> getCostCharacteristic(){
    return this.costCharacteristic;
  }


  @Override
  public int hashCode() {
    return Objects.hash(costCharacteristic);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransferCostPac that = (TransferCostPac) o;
    return Objects.equals(this.costCharacteristic, that.costCharacteristic);
  }

}