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

/**
 * Identifies the validation mechanism and describes the characteristics of that mechanism
 */
@JsonInclude(Include.NON_NULL)
public class ValidationMechanism implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Name of mechanism used to validate adjacency
   */
  private final String validationMechanism;

  /**
   * State of validatiion
   */
  private final String layerProtocolAdjacencyValidated;

  /**
   * Quality of validation (i.e. how likely is the stated validation to be invalid)
   */
  private final String validationRobustness;


  @JsonCreator
  public ValidationMechanism (
    @JsonProperty("validation-mechanism") String validationMechanism,
    @JsonProperty("layer-protocol-adjacency-validated") String layerProtocolAdjacencyValidated,
    @JsonProperty("validation-robustness") String validationRobustness){
    this.validationMechanism = validationMechanism;
    this.layerProtocolAdjacencyValidated = layerProtocolAdjacencyValidated;
    this.validationRobustness = validationRobustness;
  }


  @JsonProperty("validation-mechanism")
  public String getValidationMechanism(){
    return this.validationMechanism;
  }

  @JsonProperty("layer-protocol-adjacency-validated")
  public String getLayerProtocolAdjacencyValidated(){
    return this.layerProtocolAdjacencyValidated;
  }

  @JsonProperty("validation-robustness")
  public String getValidationRobustness(){
    return this.validationRobustness;
  }


  @Override
  public int hashCode() {
    return Objects.hash(validationMechanism, layerProtocolAdjacencyValidated, validationRobustness);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValidationMechanism that = (ValidationMechanism) o;
    return Objects.equals(this.validationMechanism, that.validationMechanism) &&
       Objects.equals(this.layerProtocolAdjacencyValidated, that.layerProtocolAdjacencyValidated) &&
       Objects.equals(this.validationRobustness, that.validationRobustness);
  }

}