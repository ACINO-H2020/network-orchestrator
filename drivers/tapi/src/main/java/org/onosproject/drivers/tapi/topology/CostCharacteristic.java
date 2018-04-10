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
 * The information for a particular cost characteristic.
 */
@JsonInclude(Include.NON_NULL)
public class CostCharacteristic implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The cost characteristic will related to some aspect of the TopologicalEntity (e.g. $ cost, routing weight). This aspect will be conveyed by the costName.
   */
  private final String costName;

  /**
   * The specific cost.
   */
  private final String costValue;

  /**
   * The cost may vary based upon some properties of the TopologicalEntity. The rules for the variation are conveyed by the costAlgorithm.
   */
  private final String costAlgorithm;


  @JsonCreator
  public CostCharacteristic (
    @JsonProperty("cost-name") String costName,
    @JsonProperty("cost-value") String costValue,
    @JsonProperty("cost-algorithm") String costAlgorithm){
    this.costName = costName;
    this.costValue = costValue;
    this.costAlgorithm = costAlgorithm;
  }


  @JsonProperty("cost-name")
  public String getCostName(){
    return this.costName;
  }

  @JsonProperty("cost-value")
  public String getCostValue(){
    return this.costValue;
  }

  @JsonProperty("cost-algorithm")
  public String getCostAlgorithm(){
    return this.costAlgorithm;
  }


  @Override
  public int hashCode() {
    return Objects.hash(costName, costValue, costAlgorithm);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CostCharacteristic that = (CostCharacteristic) o;
    return Objects.equals(this.costName, that.costName) &&
       Objects.equals(this.costValue, that.costValue) &&
       Objects.equals(this.costAlgorithm, that.costAlgorithm);
  }

}