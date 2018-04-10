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
import org.onosproject.drivers.tapi.topology.ValidationMechanism;

/**
 * Validation covers the various adjacenct discovery and reachability verification protocols. Also may cover Information source and degree of integrity.
 */
@JsonInclude(Include.NON_NULL)
public class ValidationPac implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Provides details of the specific validation mechanism(s) used to confirm the presence of an intended topologicalEntity.
   */
  private final List<ValidationMechanism> validationMechanism;


  @JsonCreator
  public ValidationPac (
    @JsonProperty("validation-mechanism") List<ValidationMechanism> validationMechanism){
    this.validationMechanism = validationMechanism != null ? ImmutableList.copyOf(validationMechanism) : ImmutableList.<ValidationMechanism>of();
  }


  @JsonProperty("validation-mechanism")
  public List<ValidationMechanism> getValidationMechanism(){
    return this.validationMechanism;
  }


  @Override
  public int hashCode() {
    return Objects.hash(validationMechanism);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValidationPac that = (ValidationPac) o;
    return Objects.equals(this.validationMechanism, that.validationMechanism);
  }

}