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

/**
 * The information for a particular risk characteristic where there is a list of risk identifiers related to that characteristic.
 */
@JsonInclude(Include.NON_NULL)
public class RiskCharacteristic implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The name of the risk characteristic. The characteristic may be related to a specific degree of closeness. 
   * For example a particular characteristic may apply to failures that are localized (e.g. to one side of a road) where as another characteristic may relate to failures that have a broader impact (e.g. both sides of a road that crosses a bridge).
   * Depending upon the importance of the traffic being routed different risk characteristics will be evaluated.
   */
  private final String riskCharacteristicName;

  /**
   * A list of the identifiers of each physical/geographic unit (with the specific risk characteristic) that is related to a segment of the TopologicalEntity.
   */
  private final List<String> riskIdentifierList;


  @JsonCreator
  public RiskCharacteristic (
    @JsonProperty("risk-characteristic-name") String riskCharacteristicName,
    @JsonProperty("risk-identifier-list") List<String> riskIdentifierList){
    this.riskCharacteristicName = riskCharacteristicName;
    this.riskIdentifierList = riskIdentifierList != null ? ImmutableList.copyOf(riskIdentifierList) : ImmutableList.of();
  }


  @JsonProperty("risk-characteristic-name")
  public String getRiskCharacteristicName(){
    return this.riskCharacteristicName;
  }

  @JsonProperty("risk-identifier-list")
  public List<String> getRiskIdentifierList(){
    return this.riskIdentifierList;
  }


  @Override
  public int hashCode() {
    return Objects.hash(riskCharacteristicName, riskIdentifierList);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RiskCharacteristic that = (RiskCharacteristic) o;
    return Objects.equals(this.riskCharacteristicName, that.riskCharacteristicName) &&
       Objects.equals(this.riskIdentifierList, that.riskIdentifierList);
  }

}