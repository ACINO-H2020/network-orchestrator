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

/**
 * The reversion mode associated with protection.
 */
public enum ReversionMode {
  REVERTIVE("revertive"), NON_REVERTIVE("non-revertive");

  private final String jsonName;

  private ReversionMode(){
      this.jsonName = this.name();
  }

  private ReversionMode(String jsonName) {
    this.jsonName = jsonName;
  }

  @JsonCreator
  public static ReversionMode fromJsonString(String jsonString) {
    for (ReversionMode value : ReversionMode.values())
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