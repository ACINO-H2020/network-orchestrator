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
import org.onosproject.drivers.tapi.common.LayerProtocolName;
import org.onosproject.drivers.tapi.common.LocalClass;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.PortDirection;
import org.onosproject.drivers.tapi.common.PortRole;
import org.onosproject.drivers.tapi.common.UniversalId;

/**
 * The association of the FC to LTPs is made via EndPoints.
 * The EndPoint (EP) object class models the access to the FC function. 
 * The traffic forwarding between the associated EPs of the FC depends upon the type of FC and may be associated with FcSwitch object instances.  
 * In cases where there is resilience the EndPoint may convey the resilience role of the access to the FC. 
 * It can represent a protected (resilient/reliable) point or a protecting (unreliable working or protection) point.
 * The EP replaces the Protection Unit of a traditional protection model. 
 * The ForwadingConstruct can be considered as a component and the EndPoint as a Port on that component
 */
@JsonInclude(Include.NON_NULL)
public class PathServiceEndPoint extends LocalClass implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final UniversalId serviceInterfacePoint;

  /**
   * Each EP of the FC has a role (e.g., working, protection, protected, symmetric, hub, spoke, leaf, root)  in the context of the FC with respect to the FC function. 
   */
  private final PortRole role;

  /**
   * The orientation of defined flow at the EndPoint.
   */
  private final PortDirection direction;

  
  private final LayerProtocolName serviceLayer;


  @JsonCreator
  public PathServiceEndPoint (@JsonProperty("local-id") String localId,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("service-interface-point") UniversalId serviceInterfacePoint,
    @JsonProperty("role") PortRole role,
    @JsonProperty("direction") PortDirection direction,
    @JsonProperty("service-layer") LayerProtocolName serviceLayer){
    super(localId, name);
    this.serviceInterfacePoint = serviceInterfacePoint;
    this.role = role;
    this.direction = direction;
    this.serviceLayer = serviceLayer;
  }


  @JsonProperty("service-interface-point")
  public UniversalId getServiceInterfacePoint(){
    return this.serviceInterfacePoint;
  }

  @JsonProperty("role")
  public PortRole getRole(){
    return this.role;
  }

  @JsonProperty("direction")
  public PortDirection getDirection(){
    return this.direction;
  }

  @JsonProperty("service-layer")
  public LayerProtocolName getServiceLayer(){
    return this.serviceLayer;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), serviceInterfacePoint, role, direction, serviceLayer);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PathServiceEndPoint that = (PathServiceEndPoint) o;
    return super.equals(o) &&
       Objects.equals(this.serviceInterfacePoint, that.serviceInterfacePoint) &&
       Objects.equals(this.role, that.role) &&
       Objects.equals(this.direction, that.direction) &&
       Objects.equals(this.serviceLayer, that.serviceLayer);
  }

}