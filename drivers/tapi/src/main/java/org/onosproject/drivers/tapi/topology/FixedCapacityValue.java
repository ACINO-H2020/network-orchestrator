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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The Capacity (Bandwidth) values that are applicable for digital layers. 
 */
public enum FixedCapacityValue {
  NOT_APPLICABLE("not-applicable"), _10MBPS("10mbps"), _100MBPS("100mbps"), _1GBPS("1gbps"), _2_4GBPS("2.4gbps"), _10GBPS("10gbps"), _40GBPS("40gbps"), _100GBPS("100gbps"), _200GBPS("200gbps"), _400GBPS("400gbps");

  private final String jsonName;

  private FixedCapacityValue(){
      this.jsonName = this.name();
  }

  private FixedCapacityValue(String jsonName) {
    this.jsonName = jsonName;
  }

  @JsonCreator
  public static FixedCapacityValue fromJsonString(String jsonString) {
    for (FixedCapacityValue value : FixedCapacityValue.values())
      if (value.jsonName.equals(jsonString)) {
        return value;
      }
    return null;
  }

  @JsonValue
  public String toJsonString() {
    return this.jsonName;
  }

}