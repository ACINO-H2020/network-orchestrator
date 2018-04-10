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

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.onosproject.drivers.tapi.common.LocalClass;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.PortDirection;
import org.onosproject.drivers.tapi.common.UniversalId;
import org.onosproject.drivers.tapi.connectivity.SelectionControl;
import org.onosproject.drivers.tapi.connectivity.SelectionReason;

/**
 * The class models the switched forwarding of traffic (traffic flow) between FcPorts (ConnectionEndPoints) and is present where there is protection functionality in the FC (Connection). 
 * If an FC exposes protection (having two or more FcPorts that provide alternative identical inputs/outputs), the FC will have one or more associated FcSwitch objects to represent the alternative flow choices visible at the edge of the FC.
 * The FC switch represents and defines a protection switch structure encapsulated in the FC. 
 * Essentially performs one of the functions of the Protection Group in a traditional model. It associates to 2 or more FcPorts each playing the role of a Protection Unit. 
 * One or more protection, i.e. standby/backup, FcPorts provide protection for one or more working (i.e. regular/main/preferred) FcPorts where either protection or working can feed one or more protected FcPort.
 * The switch may be used in revertive or non-revertive (symmetric) mode. When in revertive mode it may define a waitToRestore time.
 * It may be used in one of several modes including source switch, destination switched, source and destination switched etc (covering cases such as 1+1 and 1:1).
 * It may be locked out (prevented from switching), force switched or manual switched.
 * It will indicate switch state and change of state.
 * The switch can be switched away from all sources such that it becomes open and hence two coordinated switches can both feed the same LTP so long as at least one of the two is switched away from all sources (is 'open').
 * The ability for a Switch to be 'high impedance' allows bidirectional ForwardingConstructs to be overlaid on the same bidirectional LTP where the appropriate control is enabled to prevent signal conflict.
 * This ability allows multiple alternate routes to be present that otherwise would be in conflict.
 */
@JsonInclude(Include.NON_NULL)
public class Switch extends LocalClass implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<UniversalId> selectedConnectionEndPoint;

  
  private final List<String> selectedRoute;

  /**
   * Degree of administrative control applied to the switch selection.
   */
  private final SelectionControl selectionControl;

  /**
   * The reason for the current switch selection.
   */
  private final SelectionReason selectionReason;

  /**
   * Indicates whether the switch selects from ingress to the FC or to egress of the FC, or both.
   */
  private final PortDirection switchDirection;


  @JsonCreator
  public Switch (@JsonProperty("local-id") String localId,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("selected-connection-end-point") List<UniversalId> selectedConnectionEndPoint,
    @JsonProperty("selected-route") List<String> selectedRoute,
    @JsonProperty("selection-control") SelectionControl selectionControl,
    @JsonProperty("selection-reason") SelectionReason selectionReason,
    @JsonProperty("switch-direction") PortDirection switchDirection){
    super(localId, name);
    this.selectedConnectionEndPoint = selectedConnectionEndPoint != null ? ImmutableList.copyOf(selectedConnectionEndPoint) : ImmutableList.of();
    this.selectedRoute = selectedRoute != null ? ImmutableList.copyOf(selectedRoute) : ImmutableList.of();
    this.selectionControl = selectionControl;
    this.selectionReason = selectionReason;
    this.switchDirection = switchDirection;
  }


  @JsonProperty("selected-connection-end-point")
  public List<UniversalId> getSelectedConnectionEndPoint(){
    return this.selectedConnectionEndPoint;
  }

  @JsonProperty("selected-route")
  public List<String> getSelectedRoute(){
    return this.selectedRoute;
  }

  @JsonProperty("selection-control")
  public SelectionControl getSelectionControl(){
    return this.selectionControl;
  }

  @JsonProperty("selection-reason")
  public SelectionReason getSelectionReason(){
    return this.selectionReason;
  }

  @JsonProperty("switch-direction")
  public PortDirection getSwitchDirection(){
    return this.switchDirection;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), selectedConnectionEndPoint, selectedRoute, selectionControl, selectionReason, switchDirection);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Switch that = (Switch) o;
    return super.equals(o) &&
       Objects.equals(this.selectedConnectionEndPoint, that.selectedConnectionEndPoint) &&
       Objects.equals(this.selectedRoute, that.selectedRoute) &&
       Objects.equals(this.selectionControl, that.selectionControl) &&
       Objects.equals(this.selectionReason, that.selectionReason) &&
       Objects.equals(this.switchDirection, that.switchDirection);
  }

}