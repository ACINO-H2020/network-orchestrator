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

package org.onosproject.drivers.tapi.common;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.onosproject.drivers.tapi.common.AdministrativeState;
import org.onosproject.drivers.tapi.common.LifecycleState;
import org.onosproject.drivers.tapi.common.OperationalState;

/**
 * Provides state attributes that are applicable to an entity that can be administered. Such an entity also has operational and lifecycle aspects.
 */
@JsonInclude(Include.NON_NULL)
public class AdminStatePac implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final AdministrativeState administrativeState;

  
  private final OperationalState operationalState;

  
  private final LifecycleState lifecycleState;


  @JsonCreator
  public AdminStatePac (
    @JsonProperty("administrative-state") AdministrativeState administrativeState,
    @JsonProperty("operational-state") OperationalState operationalState,
    @JsonProperty("lifecycle-state") LifecycleState lifecycleState){
    this.administrativeState = administrativeState;
    this.operationalState = operationalState;
    this.lifecycleState = lifecycleState;
  }


  @JsonProperty("administrative-state")
  public AdministrativeState getAdministrativeState(){
    return this.administrativeState;
  }

  @JsonProperty("operational-state")
  public OperationalState getOperationalState(){
    return this.operationalState;
  }

  @JsonProperty("lifecycle-state")
  public LifecycleState getLifecycleState(){
    return this.lifecycleState;
  }


  @Override
  public int hashCode() {
    return Objects.hash(administrativeState, operationalState, lifecycleState);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdminStatePac that = (AdminStatePac) o;
    return Objects.equals(this.administrativeState, that.administrativeState) &&
       Objects.equals(this.operationalState, that.operationalState) &&
       Objects.equals(this.lifecycleState, that.lifecycleState);
  }

}