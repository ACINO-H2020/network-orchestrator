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
import org.onosproject.drivers.tapi.common.AdminStatePac;
import org.onosproject.drivers.tapi.common.ForwardingDirection;
import org.onosproject.drivers.tapi.common.LayerProtocolName;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.ServiceSpec;
import org.onosproject.drivers.tapi.common.TimeRange;
import org.onosproject.drivers.tapi.common.UniversalId;
import org.onosproject.drivers.tapi.connectivity.ConnectivityConstraint;
import org.onosproject.drivers.tapi.connectivity.ConnectivityServiceEndPoint;
import org.onosproject.drivers.tapi.connectivity.TopologyConstraint;

/**
 * The ForwardingConstruct (FC) object class models enabled potential for forwarding between two or more LTPs and like the LTP supports any transport protocol including all circuit and packet forms.
 * At the lowest level of recursion, a FC represents a cross-connection within an NE.
 */
@JsonInclude(Include.NON_NULL)
public class ConnectivityService extends ServiceSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<UniversalId> connection;

  
  private final List<ConnectivityServiceEndPoint> serviceEndPoint;

  
  private final ConnectivityConstraint connConstraint;

  
  private final TopologyConstraint topoConstraint;

  
  private final TimeRange schedule;

  
  private final AdminStatePac state;

  
  private final ForwardingDirection direction;

  
  private final LayerProtocolName layerProtocolName;


  @JsonCreator
  public ConnectivityService (@JsonProperty("uuid") UniversalId uuid,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("label") List<NameAndValue> label,
    @JsonProperty("connection") List<UniversalId> connection,
    @JsonProperty("service-end-point") List<ConnectivityServiceEndPoint> serviceEndPoint,
    @JsonProperty("conn-constraint") ConnectivityConstraint connConstraint,
    @JsonProperty("topo-constraint") TopologyConstraint topoConstraint,
    @JsonProperty("schedule") TimeRange schedule,
    @JsonProperty("state") AdminStatePac state,
    @JsonProperty("direction") ForwardingDirection direction,
    @JsonProperty("layer-protocol-name") LayerProtocolName layerProtocolName){
    super(uuid, name, label);
    this.connection = connection != null ? ImmutableList.copyOf(connection) : ImmutableList.of();
    this.serviceEndPoint = serviceEndPoint != null ? ImmutableList.copyOf(serviceEndPoint) : ImmutableList.<ConnectivityServiceEndPoint>of();
    this.connConstraint = connConstraint;
    this.topoConstraint = topoConstraint;
    this.schedule = schedule;
    this.state = state;
    this.direction = direction;
    this.layerProtocolName = layerProtocolName;
  }


  @JsonProperty("connection")
  public List<UniversalId> getConnection(){
    return this.connection;
  }

  @JsonProperty("service-end-point")
  public List<ConnectivityServiceEndPoint> getServiceEndPoint(){
    return this.serviceEndPoint;
  }

  @JsonProperty("conn-constraint")
  public ConnectivityConstraint getConnConstraint(){
    return this.connConstraint;
  }

  @JsonProperty("topo-constraint")
  public TopologyConstraint getTopoConstraint(){
    return this.topoConstraint;
  }

  @JsonProperty("schedule")
  public TimeRange getSchedule(){
    return this.schedule;
  }

  @JsonProperty("state")
  public AdminStatePac getState(){
    return this.state;
  }

  @JsonProperty("direction")
  public ForwardingDirection getDirection(){
    return this.direction;
  }

  @JsonProperty("layer-protocol-name")
  public LayerProtocolName getLayerProtocolName(){
    return this.layerProtocolName;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), connection, serviceEndPoint, connConstraint, topoConstraint, schedule, state, direction, layerProtocolName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConnectivityService that = (ConnectivityService) o;
    return super.equals(o) &&
       Objects.equals(this.connection, that.connection) &&
       Objects.equals(this.serviceEndPoint, that.serviceEndPoint) &&
       Objects.equals(this.connConstraint, that.connConstraint) &&
       Objects.equals(this.topoConstraint, that.topoConstraint) &&
       Objects.equals(this.schedule, that.schedule) &&
       Objects.equals(this.state, that.state) &&
       Objects.equals(this.direction, that.direction) &&
       Objects.equals(this.layerProtocolName, that.layerProtocolName);
  }

}