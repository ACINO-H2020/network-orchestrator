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
import org.onosproject.drivers.tapi.topology.LatencyCharacteristic;

/**
 * A TopologicalEntity will suffer effects from the underlying physical realization related to the timing of the information passed by the TopologicalEntity.
 */
@JsonInclude(Include.NON_NULL)
public class TransferTimingPac implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The effect on the latency of a queuing process. This only has significant effect for packet based systems and has a complex characteristic.
   */
  private final List<LatencyCharacteristic> latencyCharacteristic;


  @JsonCreator
  public TransferTimingPac (
    @JsonProperty("latency-characteristic") List<LatencyCharacteristic> latencyCharacteristic){
    this.latencyCharacteristic = latencyCharacteristic != null ? ImmutableList.copyOf(latencyCharacteristic) : ImmutableList.<LatencyCharacteristic>of();
  }


  @JsonProperty("latency-characteristic")
  public List<LatencyCharacteristic> getLatencyCharacteristic(){
    return this.latencyCharacteristic;
  }


  @Override
  public int hashCode() {
    return Objects.hash(latencyCharacteristic);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransferTimingPac that = (TransferTimingPac) o;
    return Objects.equals(this.latencyCharacteristic, that.latencyCharacteristic);
  }

}