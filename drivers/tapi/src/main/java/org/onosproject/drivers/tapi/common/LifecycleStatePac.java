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

import org.onosproject.drivers.tapi.common.LifecycleState;

/**
 * Provides state attributes for an entity that has lifeccycle aspects only.
 */
@JsonInclude(Include.NON_NULL)
public class LifecycleStatePac implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final LifecycleState lifecycleState;


  @JsonCreator
  public LifecycleStatePac (
    @JsonProperty("lifecycle-state") LifecycleState lifecycleState){
    this.lifecycleState = lifecycleState;
  }


  @JsonProperty("lifecycle-state")
  public LifecycleState getLifecycleState(){
    return this.lifecycleState;
  }


  @Override
  public int hashCode() {
    return Objects.hash(lifecycleState);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LifecycleStatePac that = (LifecycleStatePac) o;
    return Objects.equals(this.lifecycleState, that.lifecycleState);
  }

}