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
import org.onosproject.drivers.tapi.common.AdminStatePac;
import org.onosproject.drivers.tapi.common.LayerProtocol;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.PortDirection;
import org.onosproject.drivers.tapi.common.PortRole;
import org.onosproject.drivers.tapi.common.ResourceSpec;
import org.onosproject.drivers.tapi.common.TerminationDirection;
import org.onosproject.drivers.tapi.common.UniversalId;

/**
 * The LogicalTerminationPoint (LTP) object class encapsulates the termination and adaptation functions of one or more transport layers. 
 * The structure of LTP supports all transport protocols including circuit and packet forms.
 */
@JsonInclude(Include.NON_NULL)
public class NodeEdgePoint extends ResourceSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<LayerProtocol> layerProtocol;

  
  private final List<UniversalId> aggregatedNodeEdgePoint;

  
  private final List<UniversalId> mappedServiceInterfacePoint;

  
  private final AdminStatePac state;

  
  private final TerminationDirection terminationDirection;

  /**
   * The orientation of defined flow at the LinkEnd.
   */
  private final PortDirection linkPortDirection;

  /**
   * Each LinkEnd of the Link has a role (e.g., symmetric, hub, spoke, leaf, root)  in the context of the Link with respect to the Link function. 
   */
  private final PortRole linkPortRole;


  @JsonCreator
  public NodeEdgePoint (@JsonProperty("uuid") UniversalId uuid,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("label") List<NameAndValue> label,
    @JsonProperty("layer-protocol") List<LayerProtocol> layerProtocol,
    @JsonProperty("aggregated-node-edge-point") List<UniversalId> aggregatedNodeEdgePoint,
    @JsonProperty("mapped-service-interface-point") List<UniversalId> mappedServiceInterfacePoint,
    @JsonProperty("state") AdminStatePac state,
    @JsonProperty("termination-direction") TerminationDirection terminationDirection,
    @JsonProperty("link-port-direction") PortDirection linkPortDirection,
    @JsonProperty("link-port-role") PortRole linkPortRole){
    super(uuid, name, label);
    this.layerProtocol = layerProtocol != null ? ImmutableList.copyOf(layerProtocol) : ImmutableList.<LayerProtocol>of();
    this.aggregatedNodeEdgePoint = aggregatedNodeEdgePoint != null ? ImmutableList.copyOf(aggregatedNodeEdgePoint) : ImmutableList.of();
    this.mappedServiceInterfacePoint = mappedServiceInterfacePoint != null ? ImmutableList.copyOf(mappedServiceInterfacePoint) : ImmutableList.of();
    this.state = state;
    this.terminationDirection = terminationDirection;
    this.linkPortDirection = linkPortDirection;
    this.linkPortRole = linkPortRole;
  }


  @JsonProperty("layer-protocol")
  public List<LayerProtocol> getLayerProtocol(){
    return this.layerProtocol;
  }

  @JsonProperty("aggregated-node-edge-point")
  public List<UniversalId> getAggregatedNodeEdgePoint(){
    return this.aggregatedNodeEdgePoint;
  }

  @JsonProperty("mapped-service-interface-point")
  public List<UniversalId> getMappedServiceInterfacePoint(){
    return this.mappedServiceInterfacePoint;
  }

  @JsonProperty("state")
  public AdminStatePac getState(){
    return this.state;
  }

  @JsonProperty("termination-direction")
  public TerminationDirection getTerminationDirection(){
    return this.terminationDirection;
  }

  @JsonProperty("link-port-direction")
  public PortDirection getLinkPortDirection(){
    return this.linkPortDirection;
  }

  @JsonProperty("link-port-role")
  public PortRole getLinkPortRole(){
    return this.linkPortRole;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), layerProtocol, aggregatedNodeEdgePoint, mappedServiceInterfacePoint, state, terminationDirection, linkPortDirection, linkPortRole);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeEdgePoint that = (NodeEdgePoint) o;
    return super.equals(o) &&
       Objects.equals(this.layerProtocol, that.layerProtocol) &&
       Objects.equals(this.aggregatedNodeEdgePoint, that.aggregatedNodeEdgePoint) &&
       Objects.equals(this.mappedServiceInterfacePoint, that.mappedServiceInterfacePoint) &&
       Objects.equals(this.state, that.state) &&
       Objects.equals(this.terminationDirection, that.terminationDirection) &&
       Objects.equals(this.linkPortDirection, that.linkPortDirection) &&
       Objects.equals(this.linkPortRole, that.linkPortRole);
  }

}