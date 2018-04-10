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

package org.onosproject.drivers.tapi.common;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import org.onosproject.drivers.tapi.common.LayerProtocolName;
import org.onosproject.drivers.tapi.common.LocalClass;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.TerminationDirection;
import org.onosproject.drivers.tapi.common.TerminationState;

/**
 * Each transport layer is represented by a LayerProtocol (LP) instance. The LayerProtocol instances it can be used for controlling termination and monitoring functionality. 
 * It can also be used for controlling the adaptation (i.e. encapsulation and/or multiplexing of client signal), tandem connection monitoring, traffic conditioning and/or shaping functionality at an intermediate point along a connection. 
 * Where the client – server relationship is fixed 1:1 and immutable, the layers can be encapsulated in a single LTP instance. Where the is a n:1 relationship between client and server, the layers must be split over two separate instances of LTP. 
 */
@JsonInclude(Include.NON_NULL)
public class LayerProtocol extends LocalClass implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Indicate the specific layer-protocol described by the LayerProtocol entity.
   */
  private final LayerProtocolName layerProtocolName;

  /**
   * The overall directionality of the LP. 
   * - A BIDIRECTIONAL LP will have some SINK and/or SOURCE flowss.
   * - A SINK LP can only contain elements with SINK flows or CONTRA_DIRECTION_SOURCE flows
   * - A SOURCE LP can only contain SOURCE flows or CONTRA_DIRECTION_SINK flows
   */
  private final TerminationDirection terminationDirection;

  /**
   * Indicates whether the layer is terminated and if so how.
   */
  private final TerminationState terminationState;


  @JsonCreator
  public LayerProtocol (@JsonProperty("local-id") String localId,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("layer-protocol-name") LayerProtocolName layerProtocolName,
    @JsonProperty("termination-direction") TerminationDirection terminationDirection,
    @JsonProperty("termination-state") TerminationState terminationState){
    super(localId, name);
    this.layerProtocolName = layerProtocolName;
    this.terminationDirection = terminationDirection;
    this.terminationState = terminationState;
  }


  @JsonProperty("layer-protocol-name")
  public LayerProtocolName getLayerProtocolName(){
    return this.layerProtocolName;
  }

  @JsonProperty("termination-direction")
  public TerminationDirection getTerminationDirection(){
    return this.terminationDirection;
  }

  @JsonProperty("termination-state")
  public TerminationState getTerminationState(){
    return this.terminationState;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), layerProtocolName, terminationDirection, terminationState);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LayerProtocol that = (LayerProtocol) o;
    return super.equals(o) &&
       Objects.equals(this.layerProtocolName, that.layerProtocolName) &&
       Objects.equals(this.terminationDirection, that.terminationDirection) &&
       Objects.equals(this.terminationState, that.terminationState);
  }

}