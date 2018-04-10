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

package org.onosproject.drivers.tapi.path.computation;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.ServiceSpec;
import org.onosproject.drivers.tapi.common.UniversalId;
import org.onosproject.drivers.tapi.path.computation.PathObjectiveFunction;
import org.onosproject.drivers.tapi.path.computation.PathOptimizationConstraint;
import org.onosproject.drivers.tapi.path.computation.PathServiceEndPoint;
import org.onosproject.drivers.tapi.path.computation.RoutingConstraint;


@JsonInclude(Include.NON_NULL)
public class PathComputationService extends ServiceSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<UniversalId> path;

  
  private final List<PathServiceEndPoint> servicePort;

  
  private final RoutingConstraint routingConstraint;

  
  private final PathObjectiveFunction objectiveFunction;

  
  private final PathOptimizationConstraint optimizationConstraint;


  @JsonCreator
  public PathComputationService (@JsonProperty("uuid") UniversalId uuid,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("label") List<NameAndValue> label,
    @JsonProperty("path") List<UniversalId> path,
    @JsonProperty("service-port") List<PathServiceEndPoint> servicePort,
    @JsonProperty("routing-constraint") RoutingConstraint routingConstraint,
    @JsonProperty("objective-function") PathObjectiveFunction objectiveFunction,
    @JsonProperty("optimization-constraint") PathOptimizationConstraint optimizationConstraint){
    super(uuid, name, label);
    this.path = path != null ? ImmutableList.copyOf(path) : ImmutableList.of();
    this.servicePort = servicePort != null ? ImmutableList.copyOf(servicePort) : ImmutableList.<PathServiceEndPoint>of();
    this.routingConstraint = routingConstraint;
    this.objectiveFunction = objectiveFunction;
    this.optimizationConstraint = optimizationConstraint;
  }


  @JsonProperty("path")
  public List<UniversalId> getPath(){
    return this.path;
  }

  @JsonProperty("service-port")
  public List<PathServiceEndPoint> getServicePort(){
    return this.servicePort;
  }

  @JsonProperty("routing-constraint")
  public RoutingConstraint getRoutingConstraint(){
    return this.routingConstraint;
  }

  @JsonProperty("objective-function")
  public PathObjectiveFunction getObjectiveFunction(){
    return this.objectiveFunction;
  }

  @JsonProperty("optimization-constraint")
  public PathOptimizationConstraint getOptimizationConstraint(){
    return this.optimizationConstraint;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), path, servicePort, routingConstraint, objectiveFunction, optimizationConstraint);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PathComputationService that = (PathComputationService) o;
    return super.equals(o) &&
       Objects.equals(this.path, that.path) &&
       Objects.equals(this.servicePort, that.servicePort) &&
       Objects.equals(this.routingConstraint, that.routingConstraint) &&
       Objects.equals(this.objectiveFunction, that.objectiveFunction) &&
       Objects.equals(this.optimizationConstraint, that.optimizationConstraint);
  }

}