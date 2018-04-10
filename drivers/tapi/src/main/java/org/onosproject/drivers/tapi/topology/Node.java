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
import org.onosproject.drivers.tapi.common.LayerProtocolName;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.ResourceSpec;
import org.onosproject.drivers.tapi.common.UniversalId;
import org.onosproject.drivers.tapi.topology.NodeEdgePoint;
import org.onosproject.drivers.tapi.topology.TransferCapacityPac;
import org.onosproject.drivers.tapi.topology.TransferCostPac;
import org.onosproject.drivers.tapi.topology.TransferIntegrityPac;
import org.onosproject.drivers.tapi.topology.TransferTimingPac;

/**
 * The ForwardingDomain (FD) object class models the “ForwardingDomain” topological component which is used to effect forwarding of transport characteristic information and offers the potential to enable forwarding. 
 * At the lowest level of recursion, an FD (within a network element (NE)) represents a switch matrix (i.e., a fabric). Note that an NE can encompass multiple switch matrices (FDs). 
 */
@JsonInclude(Include.NON_NULL)
public class Node extends ResourceSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<NodeEdgePoint> ownedNodeEdgePoint;

  
  private final List<UniversalId> aggregatedNodeEdgePoint;

  
  private final UniversalId encapTopology;

  
  private final AdminStatePac state;

  
  private final TransferCapacityPac transferCapacity;

  
  private final TransferCostPac transferCost;

  
  private final TransferIntegrityPac transferIntegrity;

  
  private final TransferTimingPac transferTiming;

  
  private final List<LayerProtocolName> layerProtocolName;


  @JsonCreator
  public Node (@JsonProperty("uuid") UniversalId uuid,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("label") List<NameAndValue> label,
    @JsonProperty("owned-node-edge-point") List<NodeEdgePoint> ownedNodeEdgePoint,
    @JsonProperty("aggregated-node-edge-point") List<UniversalId> aggregatedNodeEdgePoint,
    @JsonProperty("encap-topology") UniversalId encapTopology,
    @JsonProperty("state") AdminStatePac state,
    @JsonProperty("transfer-capacity") TransferCapacityPac transferCapacity,
    @JsonProperty("transfer-cost") TransferCostPac transferCost,
    @JsonProperty("transfer-integrity") TransferIntegrityPac transferIntegrity,
    @JsonProperty("transfer-timing") TransferTimingPac transferTiming,
    @JsonProperty("layer-protocol-name") List<LayerProtocolName> layerProtocolName){
    super(uuid, name, label);
    this.ownedNodeEdgePoint = ownedNodeEdgePoint != null ? ImmutableList.copyOf(ownedNodeEdgePoint) : ImmutableList.<NodeEdgePoint>of();
    this.aggregatedNodeEdgePoint = aggregatedNodeEdgePoint != null ? ImmutableList.copyOf(aggregatedNodeEdgePoint) : ImmutableList.of();
    this.encapTopology = encapTopology;
    this.state = state;
    this.transferCapacity = transferCapacity;
    this.transferCost = transferCost;
    this.transferIntegrity = transferIntegrity;
    this.transferTiming = transferTiming;
    this.layerProtocolName = layerProtocolName != null ? ImmutableList.copyOf(layerProtocolName) : ImmutableList.of();
  }


  @JsonProperty("owned-node-edge-point")
  public List<NodeEdgePoint> getOwnedNodeEdgePoint(){
    return this.ownedNodeEdgePoint;
  }

  @JsonProperty("aggregated-node-edge-point")
  public List<UniversalId> getAggregatedNodeEdgePoint(){
    return this.aggregatedNodeEdgePoint;
  }

  @JsonProperty("encap-topology")
  public UniversalId getEncapTopology(){
    return this.encapTopology;
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

  @JsonProperty("layer-protocol-name")
  public List<LayerProtocolName> getLayerProtocolName(){
    return this.layerProtocolName;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), ownedNodeEdgePoint, aggregatedNodeEdgePoint, encapTopology, state, transferCapacity, transferCost, transferIntegrity, transferTiming, layerProtocolName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Node that = (Node) o;
    return super.equals(o) &&
       Objects.equals(this.ownedNodeEdgePoint, that.ownedNodeEdgePoint) &&
       Objects.equals(this.aggregatedNodeEdgePoint, that.aggregatedNodeEdgePoint) &&
       Objects.equals(this.encapTopology, that.encapTopology) &&
       Objects.equals(this.state, that.state) &&
       Objects.equals(this.transferCapacity, that.transferCapacity) &&
       Objects.equals(this.transferCost, that.transferCost) &&
       Objects.equals(this.transferIntegrity, that.transferIntegrity) &&
       Objects.equals(this.transferTiming, that.transferTiming) &&
       Objects.equals(this.layerProtocolName, that.layerProtocolName);
  }

}