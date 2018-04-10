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


public enum BandwidthProfileType {
  NOT_APPLICABLE("not-applicable"), MEF_10_X("mef-10.x"), RFC_2697("rfc-2697"), RFC_2698("rfc-2698"), RFC_4115("rfc-4115");

  private final String jsonName;

  private BandwidthProfileType(){
      this.jsonName = this.name();
  }

  private BandwidthProfileType(String jsonName) {
    this.jsonName = jsonName;
  }

  @JsonCreator
  public static BandwidthProfileType fromJsonString(String jsonString) {
    for (BandwidthProfileType value : BandwidthProfileType.values())
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