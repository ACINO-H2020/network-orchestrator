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
import org.onosproject.drivers.tapi.common.LayerProtocolName;
import org.onosproject.drivers.tapi.common.LocalClass;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.UniversalId;


@JsonInclude(Include.NON_NULL)
public class TopologyConstraint extends LocalClass implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<UniversalId> includeTopology;

  
  private final List<UniversalId> avoidTopology;

  
  private final List<UniversalId> includePath;

  
  private final List<UniversalId> excludePath;

  
  private final List<UniversalId> includeLink;

  
  private final List<UniversalId> excludeLink;

  /**
   * soft constraint requested by client to indicate the layer(s) of transport connection that it prefers to carry the service. This could be same as the service layer or one of the supported server layers
   */
  private final List<LayerProtocolName> preferredTransportLayer;


  @JsonCreator
  public TopologyConstraint (@JsonProperty("local-id") String localId,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("include-topology") List<UniversalId> includeTopology,
    @JsonProperty("avoid-topology") List<UniversalId> avoidTopology,
    @JsonProperty("include-path") List<UniversalId> includePath,
    @JsonProperty("exclude-path") List<UniversalId> excludePath,
    @JsonProperty("include-link") List<UniversalId> includeLink,
    @JsonProperty("exclude-link") List<UniversalId> excludeLink,
    @JsonProperty("preferred-transport-layer") List<LayerProtocolName> preferredTransportLayer){
    super(localId, name);
    this.includeTopology = includeTopology != null ? ImmutableList.copyOf(includeTopology) : ImmutableList.of();
    this.avoidTopology = avoidTopology != null ? ImmutableList.copyOf(avoidTopology) : ImmutableList.of();
    this.includePath = includePath != null ? ImmutableList.copyOf(includePath) : ImmutableList.of();
    this.excludePath = excludePath != null ? ImmutableList.copyOf(excludePath) : ImmutableList.of();
    this.includeLink = includeLink != null ? ImmutableList.copyOf(includeLink) : ImmutableList.of();
    this.excludeLink = excludeLink != null ? ImmutableList.copyOf(excludeLink) : ImmutableList.of();
    this.preferredTransportLayer = preferredTransportLayer != null ? ImmutableList.copyOf(preferredTransportLayer) : ImmutableList.of();
  }


  @JsonProperty("include-topology")
  public List<UniversalId> getIncludeTopology(){
    return this.includeTopology;
  }

  @JsonProperty("avoid-topology")
  public List<UniversalId> getAvoidTopology(){
    return this.avoidTopology;
  }

  @JsonProperty("include-path")
  public List<UniversalId> getIncludePath(){
    return this.includePath;
  }

  @JsonProperty("exclude-path")
  public List<UniversalId> getExcludePath(){
    return this.excludePath;
  }

  @JsonProperty("include-link")
  public List<UniversalId> getIncludeLink(){
    return this.includeLink;
  }

  @JsonProperty("exclude-link")
  public List<UniversalId> getExcludeLink(){
    return this.excludeLink;
  }

  @JsonProperty("preferred-transport-layer")
  public List<LayerProtocolName> getPreferredTransportLayer(){
    return this.preferredTransportLayer;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), includeTopology, avoidTopology, includePath, excludePath, includeLink, excludeLink, preferredTransportLayer);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TopologyConstraint that = (TopologyConstraint) o;
    return super.equals(o) &&
       Objects.equals(this.includeTopology, that.includeTopology) &&
       Objects.equals(this.avoidTopology, that.avoidTopology) &&
       Objects.equals(this.includePath, that.includePath) &&
       Objects.equals(this.excludePath, that.excludePath) &&
       Objects.equals(this.includeLink, that.includeLink) &&
       Objects.equals(this.excludeLink, that.excludeLink) &&
       Objects.equals(this.preferredTransportLayer, that.preferredTransportLayer);
  }

}