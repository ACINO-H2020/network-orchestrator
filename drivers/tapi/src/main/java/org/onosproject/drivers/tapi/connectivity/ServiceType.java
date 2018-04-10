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


public enum ServiceType {
  POINT_TO_POINT_CONNECTIVITY("point-to-point-connectivity"), POINT_TO_MULTIPOINT_CONNECTIVTY("point-to-multipoint-connectivty"), MULTIPOINT_CONNECTIVITY("multipoint-connectivity");

  private final String jsonName;

  private ServiceType(){
      this.jsonName = this.name();
  }

  private ServiceType(String jsonName) {
    this.jsonName = jsonName;
  }

  @JsonCreator
  public static ServiceType fromJsonString(String jsonString) {
    for (ServiceType value : ServiceType.values())
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