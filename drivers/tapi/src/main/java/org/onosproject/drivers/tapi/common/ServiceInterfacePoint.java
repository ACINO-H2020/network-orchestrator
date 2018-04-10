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

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.onosproject.drivers.tapi.common.LayerProtocol;
import org.onosproject.drivers.tapi.common.LifecycleStatePac;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.ResourceSpec;
import org.onosproject.drivers.tapi.common.TerminationDirection;
import org.onosproject.drivers.tapi.common.UniversalId;

/**
 * The LogicalTerminationPoint (LTP) object class encapsulates the termination and adaptation functions of one or more transport layers. 
 * The structure of LTP supports all transport protocols including circuit and packet forms.
 */
@JsonInclude(Include.NON_NULL)
public class ServiceInterfacePoint extends ResourceSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<LayerProtocol> layerProtocol;

  
  private final LifecycleStatePac state;

  
  private final TerminationDirection direction;


  @JsonCreator
  public ServiceInterfacePoint (@JsonProperty("uuid") UniversalId uuid,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("label") List<NameAndValue> label,
    @JsonProperty("layer-protocol") List<LayerProtocol> layerProtocol,
    @JsonProperty("state") LifecycleStatePac state,
    @JsonProperty("direction") TerminationDirection direction){
    super(uuid, name, label);
    this.layerProtocol = layerProtocol != null ? ImmutableList.copyOf(layerProtocol) : ImmutableList.<LayerProtocol>of();
    this.state = state;
    this.direction = direction;
  }


  @JsonProperty("layer-protocol")
  public List<LayerProtocol> getLayerProtocol(){
    return this.layerProtocol;
  }

  @JsonProperty("state")
  public LifecycleStatePac getState(){
    return this.state;
  }

  @JsonProperty("direction")
  public TerminationDirection getDirection(){
    return this.direction;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), layerProtocol, state, direction);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceInterfacePoint that = (ServiceInterfacePoint) o;
    return super.equals(o) &&
       Objects.equals(this.layerProtocol, that.layerProtocol) &&
       Objects.equals(this.state, that.state) &&
       Objects.equals(this.direction, that.direction);
  }

}