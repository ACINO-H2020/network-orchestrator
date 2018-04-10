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
import org.onosproject.drivers.tapi.common.LayerProtocolName;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.ResourceSpec;
import org.onosproject.drivers.tapi.common.UniversalId;
import org.onosproject.drivers.tapi.topology.Link;
import org.onosproject.drivers.tapi.topology.Node;

/**
 * The ForwardingDomain (FD) object class models the “ForwardingDomain” topological component which is used to effect forwarding of transport characteristic information and offers the potential to enable forwarding. 
 * At the lowest level of recursion, an FD (within a network element (NE)) represents a switch matrix (i.e., a fabric). Note that an NE can encompass multiple switch matrices (FDs). 
 */
@JsonInclude(Include.NON_NULL)
public class Topology extends ResourceSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<Node> node;

  
  private final List<Link> link;

  
  private final List<LayerProtocolName> layerProtocolName;


  @JsonCreator
  public Topology (@JsonProperty("uuid") UniversalId uuid,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("label") List<NameAndValue> label,
    @JsonProperty("node") List<Node> node,
    @JsonProperty("link") List<Link> link,
    @JsonProperty("layer-protocol-name") List<LayerProtocolName> layerProtocolName){
    super(uuid, name, label);
    this.node = node != null ? ImmutableList.copyOf(node) : ImmutableList.<Node>of();
    this.link = link != null ? ImmutableList.copyOf(link) : ImmutableList.<Link>of();
    this.layerProtocolName = layerProtocolName != null ? ImmutableList.copyOf(layerProtocolName) : ImmutableList.of();
  }


  @JsonProperty("node")
  public List<Node> getNode(){
    return this.node;
  }

  @JsonProperty("link")
  public List<Link> getLink(){
    return this.link;
  }

  @JsonProperty("layer-protocol-name")
  public List<LayerProtocolName> getLayerProtocolName(){
    return this.layerProtocolName;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), node, link, layerProtocolName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Topology that = (Topology) o;
    return super.equals(o) &&
       Objects.equals(this.node, that.node) &&
       Objects.equals(this.link, that.link) &&
       Objects.equals(this.layerProtocolName, that.layerProtocolName);
  }

}