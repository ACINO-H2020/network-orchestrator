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

package org.onosproject.drivers.tapi.connectivity;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.onosproject.drivers.tapi.connectivity.ProtectionType;
import org.onosproject.drivers.tapi.connectivity.ReversionMode;

/**
 * A list of control parameters to apply to a switch.
 */
@JsonInclude(Include.NON_NULL)
public class ControlParametersPac implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Indicates the protection scheme that is used for the ProtectionGroup.
   */
  private final ProtectionType protType;

  /**
   * Indcates whether the protection scheme is revertive or non-revertive.
   */
  private final ReversionMode reversionMode;

  /**
   * If the protection system is revertive, this attribute specifies the time, in minutes, to wait after a fault clears on a higher priority (preferred) resource before reverting to the preferred resource.
   */
  private final int waitToRevertTime;

  /**
   * This attribute indicates the time, in milliseconds, between declaration of signal degrade or signal fail, and the initialization of the protection switching algorithm.
   */
  private final int holdOffTime;

  
  private final boolean isLockOut;

  
  private final boolean isFrozen;

  /**
   * Is operating such that switching at both ends of each flow acorss the FC is coordinated at both ingress and egress ends.
   */
  private final boolean isCoordinatedSwitchingBothEnds;


  @JsonCreator
  public ControlParametersPac (
    @JsonProperty("prot-type") ProtectionType protType,
    @JsonProperty("reversion-mode") ReversionMode reversionMode,
    @JsonProperty("wait-to-revert-time") int waitToRevertTime,
    @JsonProperty("hold-off-time") int holdOffTime,
    @JsonProperty("is-lock-out") boolean isLockOut,
    @JsonProperty("is-frozen") boolean isFrozen,
    @JsonProperty("is-coordinated-switching-both-ends") boolean isCoordinatedSwitchingBothEnds){
    this.protType = protType;
    this.reversionMode = reversionMode;
    this.waitToRevertTime = waitToRevertTime;
    this.holdOffTime = holdOffTime;
    this.isLockOut = isLockOut;
    this.isFrozen = isFrozen;
    this.isCoordinatedSwitchingBothEnds = isCoordinatedSwitchingBothEnds;
  }


  @JsonProperty("prot-type")
  public ProtectionType getProtType(){
    return this.protType;
  }

  @JsonProperty("reversion-mode")
  public ReversionMode getReversionMode(){
    return this.reversionMode;
  }

  @JsonProperty("wait-to-revert-time")
  public int getWaitToRevertTime(){
    return this.waitToRevertTime;
  }

  @JsonProperty("hold-off-time")
  public int getHoldOffTime(){
    return this.holdOffTime;
  }

  @JsonProperty("is-lock-out")
  public boolean getIsLockOut(){
    return this.isLockOut;
  }

  @JsonProperty("is-frozen")
  public boolean getIsFrozen(){
    return this.isFrozen;
  }

  @JsonProperty("is-coordinated-switching-both-ends")
  public boolean getIsCoordinatedSwitchingBothEnds(){
    return this.isCoordinatedSwitchingBothEnds;
  }


  @Override
  public int hashCode() {
    return Objects.hash(protType, reversionMode, waitToRevertTime, holdOffTime, isLockOut, isFrozen, isCoordinatedSwitchingBothEnds);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ControlParametersPac that = (ControlParametersPac) o;
    return Objects.equals(this.protType, that.protType) &&
       Objects.equals(this.reversionMode, that.reversionMode) &&
       Objects.equals(this.waitToRevertTime, that.waitToRevertTime) &&
       Objects.equals(this.holdOffTime, that.holdOffTime) &&
       Objects.equals(this.isLockOut, that.isLockOut) &&
       Objects.equals(this.isFrozen, that.isFrozen) &&
       Objects.equals(this.isCoordinatedSwitchingBothEnds, that.isCoordinatedSwitchingBothEnds);
  }

}