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

/**
 * Relevant for a Link that is formed by abstracting one or more LTPs (in a stack) to focus on the flow and deemphasize the protocol transformation. 
 * This abstraction is relevant when considering multi-layer routing. 
 * The layer protocols of the LTP and the order of their application to the signal is still relevant and need to be accounted for. This is derived from the LTP spec details.
 * This Pac provides the relevant abstractions of the LTPs and provides the necessary association to the LTPs involved.
 * Links that included details in this Pac are often referred to as Transitional Links.
 */
@JsonInclude(Include.NON_NULL)
public class LayerProtocolTransitionPac implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Provides the ordered structure of layer protocol transitions encapsulated in the TopologicalEntity. The ordering relates to the LinkPort role.
   */
  private final List<String> transitionedLayerProtocolName;


  @JsonCreator
  public LayerProtocolTransitionPac (
    @JsonProperty("transitioned-layer-protocol-name") List<String> transitionedLayerProtocolName){
    this.transitionedLayerProtocolName = transitionedLayerProtocolName != null ? ImmutableList.copyOf(transitionedLayerProtocolName) : ImmutableList.of();
  }


  @JsonProperty("transitioned-layer-protocol-name")
  public List<String> getTransitionedLayerProtocolName(){
    return this.transitionedLayerProtocolName;
  }


  @Override
  public int hashCode() {
    return Objects.hash(transitionedLayerProtocolName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LayerProtocolTransitionPac that = (LayerProtocolTransitionPac) o;
    return Objects.equals(this.transitionedLayerProtocolName, that.transitionedLayerProtocolName);
  }

}