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
public class PathOptimizationConstraint extends LocalClass implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final DirectiveValue trafficInterruption;


  @JsonCreator
  public PathOptimizationConstraint (@JsonProperty("local-id") String localId,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("traffic-interruption") DirectiveValue trafficInterruption){
    super(localId, name);
    this.trafficInterruption = trafficInterruption;
  }


  @JsonProperty("traffic-interruption")
  public DirectiveValue getTrafficInterruption(){
    return this.trafficInterruption;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), trafficInterruption);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PathOptimizationConstraint that = (PathOptimizationConstraint) o;
    return super.equals(o) &&
       Objects.equals(this.trafficInterruption, that.trafficInterruption);
  }

}