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
import org.onosproject.drivers.tapi.common.ForwardingDirection;
import org.onosproject.drivers.tapi.common.LayerProtocolName;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.ResourceSpec;
import org.onosproject.drivers.tapi.common.UniversalId;
import org.onosproject.drivers.tapi.topology.LayerProtocolTransitionPac;
import org.onosproject.drivers.tapi.topology.RiskParameterPac;
import org.onosproject.drivers.tapi.topology.TransferCapacityPac;
import org.onosproject.drivers.tapi.topology.TransferCostPac;
import org.onosproject.drivers.tapi.topology.TransferIntegrityPac;
import org.onosproject.drivers.tapi.topology.TransferTimingPac;
import org.onosproject.drivers.tapi.topology.ValidationPac;

/**
 * The Link object class models effective adjacency between two or more ForwardingDomains (FD). 
 */
@JsonInclude(Include.NON_NULL)
public class Link extends ResourceSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<UniversalId> nodeEdgePoint;

  
  private final List<UniversalId> node;

  
  private final AdminStatePac state;

  
  private final TransferCapacityPac transferCapacity;

  
  private final TransferCostPac transferCost;

  
  private final TransferIntegrityPac transferIntegrity;

  
  private final TransferTimingPac transferTiming;

  
  private final RiskParameterPac riskParameter;

  
  private final ValidationPac validation;

  
  private final LayerProtocolTransitionPac lpTransition;

  
  private final List<LayerProtocolName> layerProtocolName;

  /**
   * The directionality of the Link. 
   * Is applicable to simple Links where all LinkEnds are BIDIRECTIONAL (the Link will be BIDIRECTIONAL) or UNIDIRECTIONAL (the Link will be UNIDIRECTIONAL). 
   * Is not present in more complex cases.
   */
  private final ForwardingDirection direction;


  @JsonCreator
  public Link (@JsonProperty("uuid") UniversalId uuid,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("label") List<NameAndValue> label,
    @JsonProperty("node-edge-point") List<UniversalId> nodeEdgePoint,
    @JsonProperty("node") List<UniversalId> node,
    @JsonProperty("state") AdminStatePac state,
    @JsonProperty("transfer-capacity") TransferCapacityPac transferCapacity,
    @JsonProperty("transfer-cost") TransferCostPac transferCost,
    @JsonProperty("transfer-integrity") TransferIntegrityPac transferIntegrity,
    @JsonProperty("transfer-timing") TransferTimingPac transferTiming,
    @JsonProperty("risk-parameter") RiskParameterPac riskParameter,
    @JsonProperty("validation") ValidationPac validation,
    @JsonProperty("lp-transition") LayerProtocolTransitionPac lpTransition,
    @JsonProperty("layer-protocol-name") List<LayerProtocolName> layerProtocolName,
    @JsonProperty("direction") ForwardingDirection direction){
    super(uuid, name, label);
    this.nodeEdgePoint = nodeEdgePoint != null ? ImmutableList.copyOf(nodeEdgePoint) : ImmutableList.of();
    this.node = node != null ? ImmutableList.copyOf(node) : ImmutableList.of();
    this.state = state;
    this.transferCapacity = transferCapacity;
    this.transferCost = transferCost;
    this.transferIntegrity = transferIntegrity;
    this.transferTiming = transferTiming;
    this.riskParameter = riskParameter;
    this.validation = validation;
    this.lpTransition = lpTransition;
    this.layerProtocolName = layerProtocolName != null ? ImmutableList.copyOf(layerProtocolName) : ImmutableList.of();
    this.direction = direction;
  }


  @JsonProperty("node-edge-point")
  public List<UniversalId> getNodeEdgePoint(){
    return this.nodeEdgePoint;
  }

  @JsonProperty("node")
  public List<UniversalId> getNode(){
    return this.node;
  }

  @JsonProperty("state")
  public AdminStatePac getState(){
    return this.state;
  }

  @JsonProperty("transfer-capacity")
  public TransferCapacityPac getTransferCapacity(){
    return this.transferCapacity;
  }

  @JsonProperty("transfer-cost")
  public TransferCostPac getTransferCost(){
    return this.transferCost;
  }

  @JsonProperty("transfer-integrity")
  public TransferIntegrityPac getTransferIntegrity(){
    return this.transferIntegrity;
  }

  @JsonProperty("transfer-timing")
  public TransferTimingPac getTransferTiming(){
    return this.transferTiming;
  }

  @JsonProperty("risk-parameter")
  public RiskParameterPac getRiskParameter(){
    return this.riskParameter;
  }

  @JsonProperty("validation")
  public ValidationPac getValidation(){
    return this.validation;
  }

  @JsonProperty("lp-transition")
  public LayerProtocolTransitionPac getLpTransition(){
    return this.lpTransition;
  }

  @JsonProperty("layer-protocol-name")
  public List<LayerProtocolName> getLayerProtocolName(){
    return this.layerProtocolName;
  }

  @JsonProperty("direction")
  public ForwardingDirection getDirection(){
    return this.direction;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), nodeEdgePoint, node, state, transferCapacity, transferCost, transferIntegrity, transferTiming, riskParameter, validation, lpTransition, layerProtocolName, direction);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Link that = (Link) o;
    return super.equals(o) &&
       Objects.equals(this.nodeEdgePoint, that.nodeEdgePoint) &&
       Objects.equals(this.node, that.node) &&
       Objects.equals(this.state, that.state) &&
       Objects.equals(this.transferCapacity, that.transferCapacity) &&
       Objects.equals(this.transferCost, that.transferCost) &&
       Objects.equals(this.transferIntegrity, that.transferIntegrity) &&
       Objects.equals(this.transferTiming, that.transferTiming) &&
       Objects.equals(this.riskParameter, that.riskParameter) &&
       Objects.equals(this.validation, that.validation) &&
       Objects.equals(this.lpTransition, that.lpTransition) &&
       Objects.equals(this.layerProtocolName, that.layerProtocolName) &&
       Objects.equals(this.direction, that.direction);
  }

}