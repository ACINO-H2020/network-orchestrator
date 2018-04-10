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
 * Possible degrees of administrative control applied to the Route selection.
 */
public enum SelectionControl {
  LOCK_OUT("lock-out"), NORMAL("normal"), MANUAL("manual"), FORCED("forced");

  private final String jsonName;

  private SelectionControl(){
      this.jsonName = this.name();
  }

  private SelectionControl(String jsonName) {
    this.jsonName = jsonName;
  }

  @JsonCreator
  public static SelectionControl fromJsonString(String jsonString) {
    for (SelectionControl value : SelectionControl.values())
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