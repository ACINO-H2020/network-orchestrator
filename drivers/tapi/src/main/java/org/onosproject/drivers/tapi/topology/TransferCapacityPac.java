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
import org.onosproject.drivers.tapi.topology.Capacity;

/**
 * The TopologicalEntity derives capacity from the underlying realization. 
 * A TopologicalEntity may be an abstraction and virtualization of a subset of the underlying capability offered in a view or may be directly reflecting the underlying realization.
 * A TopologicalEntity may be directly used in the view or may be assigned to another view for use.
 * The clients supported by a multi-layer TopologicalEntity may interact such that the resources used by one client may impact those available to another. This is derived from the LTP spec details.
 * Represents the capacity available to user (client) along with client interaction and usage. 
 * A TopologicalEntity may reflect one or more client protocols and one or more members for each profile.
 */
@JsonInclude(Include.NON_NULL)
public class TransferCapacityPac implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * An optimistic view of the capacity of the TopologicalEntity assuming that any shared capacity is available to be taken.
   */
  private final Capacity totalPotentialCapacity;

  /**
   * Capacity available to be assigned.
   */
  private final Capacity availableCapacity;

  /**
   * Capacity already assigned
   */
  private final List<Capacity> capacityAssignedToUserView;

  /**
   * A reference to an algorithm that describes how various chunks of allocated capacity interact (e.g. when shared)
   */
  private final String capacityInteractionAlgorithm;


  @JsonCreator
  public TransferCapacityPac (
    @JsonProperty("total-potential-capacity") Capacity totalPotentialCapacity,
    @JsonProperty("available-capacity") Capacity availableCapacity,
    @JsonProperty("capacity-assigned-to-user-view") List<Capacity> capacityAssignedToUserView,
    @JsonProperty("capacity-interaction-algorithm") String capacityInteractionAlgorithm){
    this.totalPotentialCapacity = totalPotentialCapacity;
    this.availableCapacity = availableCapacity;
    this.capacityAssignedToUserView = capacityAssignedToUserView != null ? ImmutableList.copyOf(capacityAssignedToUserView) : ImmutableList.<Capacity>of();
    this.capacityInteractionAlgorithm = capacityInteractionAlgorithm;
  }


  @JsonProperty("total-potential-capacity")
  public Capacity getTotalPotentialCapacity(){
    return this.totalPotentialCapacity;
  }

  @JsonProperty("available-capacity")
  public Capacity getAvailableCapacity(){
    return this.availableCapacity;
  }

  @JsonProperty("capacity-assigned-to-user-view")
  public List<Capacity> getCapacityAssignedToUserView(){
    return this.capacityAssignedToUserView;
  }

  @JsonProperty("capacity-interaction-algorithm")
  public String getCapacityInteractionAlgorithm(){
    return this.capacityInteractionAlgorithm;
  }


  @Override
  public int hashCode() {
    return Objects.hash(totalPotentialCapacity, availableCapacity, capacityAssignedToUserView, capacityInteractionAlgorithm);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransferCapacityPac that = (TransferCapacityPac) o;
    return Objects.equals(this.totalPotentialCapacity, that.totalPotentialCapacity) &&
       Objects.equals(this.availableCapacity, that.availableCapacity) &&
       Objects.equals(this.capacityAssignedToUserView, that.capacityAssignedToUserView) &&
       Objects.equals(this.capacityInteractionAlgorithm, that.capacityInteractionAlgorithm);
  }

}