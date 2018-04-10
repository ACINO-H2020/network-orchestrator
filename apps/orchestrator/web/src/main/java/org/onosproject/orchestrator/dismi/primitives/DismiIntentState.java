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

package org.onosproject.orchestrator.dismi.primitives;

/**
 * Created by stejun on 12/2/16.
 */
public enum DismiIntentState {

    UNKNOWN("UNKNOWN"),
    PROCESSING("PROCESSING"),
    PROCESSING_FAILED("PROCESSING_FAILED"),
    INSTALLING("INSTALLING"),
    FAILED("FAILED"),
    INSTALLED("INSTALLED"),
    WITHDRAWING("WITHDRAWING"),
    WITHDRAWN("WITHDRAWN"),
    NEGOTIATION("NEGOTIATION");// Added by Abdul

    private String value;

    DismiIntentState(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
