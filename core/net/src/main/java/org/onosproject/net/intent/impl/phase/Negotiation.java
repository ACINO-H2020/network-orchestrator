/*
 * Copyright 2015-present Open Networking Laboratory
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
package org.onosproject.net.intent.impl.phase;

import org.onosproject.net.intent.IntentData;
import org.onosproject.net.intent.IntentNegotiationException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.net.intent.IntentState.FAILED;
import static org.onosproject.net.intent.IntentState.NEGOTIATION_REQ;

/**
 * Represents a phase where the compile has failed.
 */
public class Negotiation extends FinalIntentProcessPhase {

    private final IntentData data;
    private final IntentNegotiationException e;

    /**
     * Create an instance with the specified data.
     *
     * @param data intentData
     */
    Negotiation(IntentData data, IntentNegotiationException e) {
        this.data = checkNotNull(data);
        this.data.setState(NEGOTIATION_REQ);
        this.e = e;
    }

    @Override
    public IntentData data() {
        throw e;
        //return data;
    }
}
