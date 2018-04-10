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
import org.onosproject.drivers.tapi.common.ForwardingDirection;
import org.onosproject.drivers.tapi.common.LayerProtocolName;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.OperationalStatePac;
import org.onosproject.drivers.tapi.common.ResourceSpec;
import org.onosproject.drivers.tapi.common.UniversalId;
import org.onosproject.drivers.tapi.connectivity.ConnectionEndPoint;
import org.onosproject.drivers.tapi.connectivity.Route;
import org.onosproject.drivers.tapi.connectivity.SwitchControl;

/**
 * The ForwardingConstruct (FC) object class models enabled potential for forwarding between two or more LTPs and like the LTP supports any transport protocol including all circuit and packet forms.
 * At the lowest level of recursion, a FC represents a cross-connection within an NE.
 */
@JsonInclude(Include.NON_NULL)
public class Connection extends ResourceSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<ConnectionEndPoint> connectionEndPoint;

  
  private final List<Route> route;

  
  private final UniversalId node;

  
  private final List<SwitchControl> switchControl;

  
  private final OperationalStatePac state;

  
  private final LayerProtocolName layerProtocolName;

  
  private final ForwardingDirection direction;


  @JsonCreator
  public Connection (@JsonProperty("uuid") UniversalId uuid,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("label") List<NameAndValue> label,
    @JsonProperty("connection-end-point") List<ConnectionEndPoint> connectionEndPoint,
    @JsonProperty("route") List<Route> route,
    @JsonProperty("node") UniversalId node,
    @JsonProperty("switch-control") List<SwitchControl> switchControl,
    @JsonProperty("state") OperationalStatePac state,
    @JsonProperty("layer-protocol-name") LayerProtocolName layerProtocolName,
    @JsonProperty("direction") ForwardingDirection direction){
    super(uuid, name, label);
    this.connectionEndPoint = connectionEndPoint != null ? ImmutableList.copyOf(connectionEndPoint) : ImmutableList.<ConnectionEndPoint>of();
    this.route = route != null ? ImmutableList.copyOf(route) : ImmutableList.<Route>of();
    this.node = node;
    this.switchControl = switchControl != null ? ImmutableList.copyOf(switchControl) : ImmutableList.<SwitchControl>of();
    this.state = state;
    this.layerProtocolName = layerProtocolName;
    this.direction = direction;
  }


  @JsonProperty("connection-end-point")
  public List<ConnectionEndPoint> getConnectionEndPoint(){
    return this.connectionEndPoint;
  }

  @JsonProperty("route")
  public List<Route> getRoute(){
    return this.route;
  }

  @JsonProperty("node")
  public UniversalId getNode(){
    return this.node;
  }

  @JsonProperty("switch-control")
  public List<SwitchControl> getSwitchControl(){
    return this.switchControl;
  }

  @JsonProperty("state")
  public OperationalStatePac getState(){
    return this.state;
  }

  @JsonProperty("layer-protocol-name")
  public LayerProtocolName getLayerProtocolName(){
    return this.layerProtocolName;
  }

  @JsonProperty("direction")
  public ForwardingDirection getDirection(){
    return this.direction;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), connectionEndPoint, route, node, switchControl, state, layerProtocolName, direction);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Connection that = (Connection) o;
    return super.equals(o) &&
       Objects.equals(this.connectionEndPoint, that.connectionEndPoint) &&
       Objects.equals(this.route, that.route) &&
       Objects.equals(this.node, that.node) &&
       Objects.equals(this.switchControl, that.switchControl) &&
       Objects.equals(this.state, that.state) &&
       Objects.equals(this.layerProtocolName, that.layerProtocolName) &&
       Objects.equals(this.direction, that.direction);
  }

}