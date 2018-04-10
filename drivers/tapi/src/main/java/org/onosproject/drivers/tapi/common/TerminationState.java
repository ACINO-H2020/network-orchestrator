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
 * Provides support for the range of behaviours and specific states that an LP can take with respect to termination of the signal.
 * Indicates to what degree the LayerTermination is terminated.
 */
public enum TerminationState {
  LP_CAN_NEVER_TERMINATE("lp-can-never-terminate"), LT_NOT_TERMINATED("lt-not-terminated"), TERMINATED_SERVER_TO_CLIENT_FLOW("terminated-server-to-client-flow"), TERMINATED_CLIENT_TO_SERVER_FLOW("terminated-client-to-server-flow"), TERMINATED_BIDIRECTIONAL("terminated-bidirectional"), LT_PERMENANTLY_TERMINATED("lt-permenantly-terminated"), TERMINATION_STATE_UNKNOWN("termination-state-unknown");

  private final String jsonName;

  private TerminationState(){
      this.jsonName = this.name();
  }

  private TerminationState(String jsonName) {
    this.jsonName = jsonName;
  }

  @JsonCreator
  public static TerminationState fromJsonString(String jsonString) {
    for (TerminationState value : TerminationState.values())
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