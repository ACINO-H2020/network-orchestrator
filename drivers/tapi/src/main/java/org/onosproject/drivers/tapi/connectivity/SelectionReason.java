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
 * The cause of the current route selection.
 */
public enum SelectionReason {
  LOCKOUT("lockout"), NORMAL("normal"), MANUAL("manual"), FORCED("forced"), WAIT_TO_REVERT("wait-to-revert"), SIGNAL_DEGRADE("signal-degrade"), SIGNAL_FAIL("signal-fail");

  private final String jsonName;

  private SelectionReason(){
      this.jsonName = this.name();
  }

  private SelectionReason(String jsonName) {
    this.jsonName = jsonName;
  }

  @JsonCreator
  public static SelectionReason fromJsonString(String jsonString) {
    for (SelectionReason value : SelectionReason.values())
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