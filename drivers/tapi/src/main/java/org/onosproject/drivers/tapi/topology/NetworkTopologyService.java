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
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.ServiceSpec;
import org.onosproject.drivers.tapi.common.UniversalId;


@JsonInclude(Include.NON_NULL)
public class NetworkTopologyService extends ServiceSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<UniversalId> topology;


  @JsonCreator
  public NetworkTopologyService (@JsonProperty("uuid") UniversalId uuid,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("label") List<NameAndValue> label,
    @JsonProperty("topology") List<UniversalId> topology){
    super(uuid, name, label);
    this.topology = topology != null ? ImmutableList.copyOf(topology) : ImmutableList.of();
  }


  @JsonProperty("topology")
  public List<UniversalId> getTopology(){
    return this.topology;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), topology);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NetworkTopologyService that = (NetworkTopologyService) o;
    return super.equals(o) &&
       Objects.equals(this.topology, that.topology);
  }

}