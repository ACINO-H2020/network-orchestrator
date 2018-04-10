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

package org.onosproject.drivers.tapi;

import org.onosproject.net.domain.DomainIntent;

import java.util.Set;

/**
 * A service interface for maintaining the mapping between the intent's
 * identifier and the tunnel identifier received through the TAPI.
 */
public interface TapiIntentToTunnel {

    /**
     * Add a tunnel identifier for this intent.
     *
     * @param intent     the intent
     * @param identifier the identifier that the intent is mapped to
     */
    void addTunnelIdentifier(DomainIntent intent, String identifier);

    /**
     * Checks if this intent is known.
     *
     * @param intent the intent to be checked
     * @return true if known, otherwise false
     */
    boolean contains(DomainIntent intent);

    /**
     * Find all intents that have the same key attribute.
     *
     * @param intent the intent with a key attribute
     * @return all known intents with the same key attribute
     */
    Set<DomainIntent> sameKey(DomainIntent intent);

    /**
     * Checks if the intent is tagged for deletion. An intent is tagged when the
     * first intent with the same key is deleted.
     *
     * @param intent the intent to be checked
     * @return true if it is tagged, otherwise false
     */
    boolean isTaggedForDeletion(DomainIntent intent);

    /**
     * Get the tunnel identifier for this intent, while keeping it.
     *
     * @param intent the intent
     * @return the identifier matching this intent
     */
    String getTunnelIdentifier(DomainIntent intent);

    /**
     * Retrieve the tunnel identifier and remove the entry.
     *
     * @param intent the intent
     * @return the identifier matching this intent
     */
    String removeIntent(DomainIntent intent);

}
