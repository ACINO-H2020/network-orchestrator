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

import org.onosproject.drivers.tapi.topology.BandwidthProfileType;
import org.onosproject.drivers.tapi.topology.FixedCapacityValue;

/**
 * Information on capacity of a particular TopologicalEntity.
 */
@JsonInclude(Include.NON_NULL)
public class Capacity implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Total capacity of the TopologicalEntity in MB/s
   */
  private final FixedCapacityValue totalSize;

  
  private final BandwidthProfileType packetBwProfileType;

  
  private final int committedInformationRate;

  
  private final int committedBurstSize;

  
  private final int peakInformationRate;

  
  private final int peakBurstSize;

  
  private final boolean colorAware;

  
  private final boolean couplingFlag;


  @JsonCreator
  public Capacity (
    @JsonProperty("total-size") FixedCapacityValue totalSize,
    @JsonProperty("packet-bw-profile-type") BandwidthProfileType packetBwProfileType,
    @JsonProperty("committed-information-rate") int committedInformationRate,
    @JsonProperty("committed-burst-size") int committedBurstSize,
    @JsonProperty("peak-information-rate") int peakInformationRate,
    @JsonProperty("peak-burst-size") int peakBurstSize,
    @JsonProperty("color-aware") boolean colorAware,
    @JsonProperty("coupling-flag") boolean couplingFlag){
    this.totalSize = totalSize;
    this.packetBwProfileType = packetBwProfileType;
    this.committedInformationRate = committedInformationRate;
    this.committedBurstSize = committedBurstSize;
    this.peakInformationRate = peakInformationRate;
    this.peakBurstSize = peakBurstSize;
    this.colorAware = colorAware;
    this.couplingFlag = couplingFlag;
  }


  @JsonProperty("total-size")
  public FixedCapacityValue getTotalSize(){
    return this.totalSize;
  }

  @JsonProperty("packet-bw-profile-type")
  public BandwidthProfileType getPacketBwProfileType(){
    return this.packetBwProfileType;
  }

  @JsonProperty("committed-information-rate")
  public int getCommittedInformationRate(){
    return this.committedInformationRate;
  }

  @JsonProperty("committed-burst-size")
  public int getCommittedBurstSize(){
    return this.committedBurstSize;
  }

  @JsonProperty("peak-information-rate")
  public int getPeakInformationRate(){
    return this.peakInformationRate;
  }

  @JsonProperty("peak-burst-size")
  public int getPeakBurstSize(){
    return this.peakBurstSize;
  }

  @JsonProperty("color-aware")
  public boolean getColorAware(){
    return this.colorAware;
  }

  @JsonProperty("coupling-flag")
  public boolean getCouplingFlag(){
    return this.couplingFlag;
  }


  @Override
  public int hashCode() {
    return Objects.hash(totalSize, packetBwProfileType, committedInformationRate, committedBurstSize, peakInformationRate, peakBurstSize, colorAware, couplingFlag);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Capacity that = (Capacity) o;
    return Objects.equals(this.totalSize, that.totalSize) &&
       Objects.equals(this.packetBwProfileType, that.packetBwProfileType) &&
       Objects.equals(this.committedInformationRate, that.committedInformationRate) &&
       Objects.equals(this.committedBurstSize, that.committedBurstSize) &&
       Objects.equals(this.peakInformationRate, that.peakInformationRate) &&
       Objects.equals(this.peakBurstSize, that.peakBurstSize) &&
       Objects.equals(this.colorAware, that.colorAware) &&
       Objects.equals(this.couplingFlag, that.couplingFlag);
  }

}