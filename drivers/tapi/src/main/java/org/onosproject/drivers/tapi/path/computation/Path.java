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
import org.onosproject.drivers.tapi.common.ResourceSpec;
import org.onosproject.drivers.tapi.common.UniversalId;
import org.onosproject.drivers.tapi.path.computation.RoutingConstraint;

/**
 * Path is described by an ordered list of TE Links. A TE Link is defined by a pair of Node/NodeEdgePoint IDs. A Connection is realized by concatenating link resources (associated with a Link) and the lower-level connections (cross-connections) in the different nodes
 */
@JsonInclude(Include.NON_NULL)
public class Path extends ResourceSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<UniversalId> link;

  
  private final RoutingConstraint routingConstraint;


  @JsonCreator
  public Path (@JsonProperty("uuid") UniversalId uuid,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("label") List<NameAndValue> label,
    @JsonProperty("link") List<UniversalId> link,
    @JsonProperty("routing-constraint") RoutingConstraint routingConstraint){
    super(uuid, name, label);
    this.link = link != null ? ImmutableList.copyOf(link) : ImmutableList.of();
    this.routingConstraint = routingConstraint;
  }


  @JsonProperty("link")
  public List<UniversalId> getLink(){
    return this.link;
  }

  @JsonProperty("routing-constraint")
  public RoutingConstraint getRoutingConstraint(){
    return this.routingConstraint;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), link, routingConstraint);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Path that = (Path) o;
    return super.equals(o) &&
       Objects.equals(this.link, that.link) &&
       Objects.equals(this.routingConstraint, that.routingConstraint);
  }

}