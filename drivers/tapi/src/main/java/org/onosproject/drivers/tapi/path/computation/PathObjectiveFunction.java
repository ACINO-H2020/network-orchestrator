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

import java.util.List;
import org.onosproject.drivers.tapi.common.DirectiveValue;
import org.onosproject.drivers.tapi.common.LocalClass;
import org.onosproject.drivers.tapi.common.NameAndValue;


@JsonInclude(Include.NON_NULL)
public class PathObjectiveFunction extends LocalClass implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final DirectiveValue bandwidthOptimization;

  
  private final DirectiveValue concurrentPaths;

  
  private final DirectiveValue costOptimization;

  
  private final DirectiveValue linkUtilization;

  
  private final DirectiveValue resourceSharing;


  @JsonCreator
  public PathObjectiveFunction (@JsonProperty("local-id") String localId,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("bandwidth-optimization") DirectiveValue bandwidthOptimization,
    @JsonProperty("concurrent-paths") DirectiveValue concurrentPaths,
    @JsonProperty("cost-optimization") DirectiveValue costOptimization,
    @JsonProperty("link-utilization") DirectiveValue linkUtilization,
    @JsonProperty("resource-sharing") DirectiveValue resourceSharing){
    super(localId, name);
    this.bandwidthOptimization = bandwidthOptimization;
    this.concurrentPaths = concurrentPaths;
    this.costOptimization = costOptimization;
    this.linkUtilization = linkUtilization;
    this.resourceSharing = resourceSharing;
  }


  @JsonProperty("bandwidth-optimization")
  public DirectiveValue getBandwidthOptimization(){
    return this.bandwidthOptimization;
  }

  @JsonProperty("concurrent-paths")
  public DirectiveValue getConcurrentPaths(){
    return this.concurrentPaths;
  }

  @JsonProperty("cost-optimization")
  public DirectiveValue getCostOptimization(){
    return this.costOptimization;
  }

  @JsonProperty("link-utilization")
  public DirectiveValue getLinkUtilization(){
    return this.linkUtilization;
  }

  @JsonProperty("resource-sharing")
  public DirectiveValue getResourceSharing(){
    return this.resourceSharing;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), bandwidthOptimization, concurrentPaths, costOptimization, linkUtilization, resourceSharing);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PathObjectiveFunction that = (PathObjectiveFunction) o;
    return super.equals(o) &&
       Objects.equals(this.bandwidthOptimization, that.bandwidthOptimization) &&
       Objects.equals(this.concurrentPaths, that.concurrentPaths) &&
       Objects.equals(this.costOptimization, that.costOptimization) &&
       Objects.equals(this.linkUtilization, that.linkUtilization) &&
       Objects.equals(this.resourceSharing, that.resourceSharing);
  }

}