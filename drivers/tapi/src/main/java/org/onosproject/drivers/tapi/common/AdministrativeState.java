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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The possible values of the administrativeState.
 */
public enum AdministrativeState {
  LOCKED("locked"), UNLOCKED("unlocked");

  private final String jsonName;

  private AdministrativeState(){
      this.jsonName = this.name();
  }

  private AdministrativeState(String jsonName) {
    this.jsonName = jsonName;
  }

  @JsonCreator
  public static AdministrativeState fromJsonString(String jsonString) {
    for (AdministrativeState value : AdministrativeState.values())
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