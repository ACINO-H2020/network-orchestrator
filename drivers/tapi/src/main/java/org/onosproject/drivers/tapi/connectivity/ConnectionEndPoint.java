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
import org.onosproject.drivers.tapi.common.LayerProtocol;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.OperationalStatePac;
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
public class ConnectionEndPoint extends ResourceSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<LayerProtocol> layerProtocol;

  
  private final List<UniversalId> clientNodeEdgePoint;

  
  private final UniversalId serverNodeEdgePoint;

  
  private final UniversalId peerConnectionEndPoint;

  
  private final OperationalStatePac state;

  
  private final TerminationDirection terminationDirection;

  /**
   * The orientation of defined flow at the EndPoint.
   */
  private final PortDirection connectionPortDirection;

  /**
   * Each EP of the FC has a role (e.g., working, protection, protected, symmetric, hub, spoke, leaf, root)  in the context of the FC with respect to the FC function. 
   */
  private final PortRole connectionPortRole;


  @JsonCreator
  public ConnectionEndPoint (@JsonProperty("uuid") UniversalId uuid,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("label") List<NameAndValue> label,
    @JsonProperty("layer-protocol") List<LayerProtocol> layerProtocol,
    @JsonProperty("client-node-edge-point") List<UniversalId> clientNodeEdgePoint,
    @JsonProperty("server-node-edge-point") UniversalId serverNodeEdgePoint,
    @JsonProperty("peer-connection-end-point") UniversalId peerConnectionEndPoint,
    @JsonProperty("state") OperationalStatePac state,
    @JsonProperty("termination-direction") TerminationDirection terminationDirection,
    @JsonProperty("connection-port-direction") PortDirection connectionPortDirection,
    @JsonProperty("connection-port-role") PortRole connectionPortRole){
    super(uuid, name, label);
    this.layerProtocol = layerProtocol != null ? ImmutableList.copyOf(layerProtocol) : ImmutableList.<LayerProtocol>of();
    this.clientNodeEdgePoint = clientNodeEdgePoint != null ? ImmutableList.copyOf(clientNodeEdgePoint) : ImmutableList.of();
    this.serverNodeEdgePoint = serverNodeEdgePoint;
    this.peerConnectionEndPoint = peerConnectionEndPoint;
    this.state = state;
    this.terminationDirection = terminationDirection;
    this.connectionPortDirection = connectionPortDirection;
    this.connectionPortRole = connectionPortRole;
  }


  @JsonProperty("layer-protocol")
  public List<LayerProtocol> getLayerProtocol(){
    return this.layerProtocol;
  }

  @JsonProperty("client-node-edge-point")
  public List<UniversalId> getClientNodeEdgePoint(){
    return this.clientNodeEdgePoint;
  }

  @JsonProperty("server-node-edge-point")
  public UniversalId getServerNodeEdgePoint(){
    return this.serverNodeEdgePoint;
  }

  @JsonProperty("peer-connection-end-point")
  public UniversalId getPeerConnectionEndPoint(){
    return this.peerConnectionEndPoint;
  }

  @JsonProperty("state")
  public OperationalStatePac getState(){
    return this.state;
  }

  @JsonProperty("termination-direction")
  public TerminationDirection getTerminationDirection(){
    return this.terminationDirection;
  }

  @JsonProperty("connection-port-direction")
  public PortDirection getConnectionPortDirection(){
    return this.connectionPortDirection;
  }

  @JsonProperty("connection-port-role")
  public PortRole getConnectionPortRole(){
    return this.connectionPortRole;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), layerProtocol, clientNodeEdgePoint, serverNodeEdgePoint, peerConnectionEndPoint, state, terminationDirection, connectionPortDirection, connectionPortRole);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConnectionEndPoint that = (ConnectionEndPoint) o;
    return super.equals(o) &&
       Objects.equals(this.layerProtocol, that.layerProtocol) &&
       Objects.equals(this.clientNodeEdgePoint, that.clientNodeEdgePoint) &&
       Objects.equals(this.serverNodeEdgePoint, that.serverNodeEdgePoint) &&
       Objects.equals(this.peerConnectionEndPoint, that.peerConnectionEndPoint) &&
       Objects.equals(this.state, that.state) &&
       Objects.equals(this.terminationDirection, that.terminationDirection) &&
       Objects.equals(this.connectionPortDirection, that.connectionPortDirection) &&
       Objects.equals(this.connectionPortRole, that.connectionPortRole);
  }

}