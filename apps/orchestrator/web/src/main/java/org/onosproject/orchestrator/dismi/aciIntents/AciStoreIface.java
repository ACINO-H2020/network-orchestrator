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

package org.onosproject.orchestrator.dismi.aciIntents;

import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.Key;
import org.onosproject.orchestrator.dismi.abstraction.AbstractionLinkList;

import java.util.Set;

public interface AciStoreIface {

    Set<AciIntentKeyStatus> getKeys(DismiIntentId dismiID);

    boolean removeDismiIntent(DismiIntentId dismiID);

    void put(DismiIntentId dismiID, Set<AciIntentKeyStatus> keys);

    void updateKey(DismiIntentId dismiID, AciIntentKeyStatus keys);

    Set<DismiIntentId> listDismiIntentId();

    void addKeyIntent(Key intentKey, Intent onosIntent);

    Intent removeIntentKey(Key intentKey);

    void updateAbstractLinkList(DismiIntentId dismiIntentId, Key key, AbstractionLinkList abstractionLinks);
}
