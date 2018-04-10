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

package org.onosproject.drivers.tapi.connectivity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum ProtectionType {
  LINEAR_1_PLUS_1("linear-1-plus-1"), LINEAR_1_FOR_1("linear-1-for-1");

  private final String jsonName;

  private ProtectionType(){
      this.jsonName = this.name();
  }

  private ProtectionType(String jsonName) {
    this.jsonName = jsonName;
  }

  @JsonCreator
  public static ProtectionType fromJsonString(String jsonString) {
    for (ProtectionType value : ProtectionType.values())
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