/*
 * Copyright 2017-present Open Networking Foundation
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
package org.onosproject.net.intent.constraint;

import org.onosproject.net.intent.AciIntent;
import org.onosproject.net.intent.ConnectivityIntent;
import org.onosproject.net.intent.Intent;

public final class NegotiableConstraint extends MarkerConstraint {

    private static final NegotiableConstraint NEGOTIABLE_CONSTRAINT = new NegotiableConstraint();

    /**
     * Determines whether to utilize path protection for the given intent.
     *
     * @param intent  intent to be inspected
     * @return        whether the intent has a ProtectionConstraint
     */
    public static boolean negotiationAllowed(Intent intent) {
        if (intent instanceof ConnectivityIntent) {
            ConnectivityIntent connectivityIntent = (ConnectivityIntent) intent;
            return connectivityIntent.constraints().stream()
                    .anyMatch(p -> p instanceof NegotiableConstraint);
        }
        return false;
    }

    public static NegotiableConstraint negotiable() {
        return NEGOTIABLE_CONSTRAINT;
    }

    protected NegotiableConstraint() {

    }

    @Override
    public String toString() {
        return "Negotiable";
    }

}
