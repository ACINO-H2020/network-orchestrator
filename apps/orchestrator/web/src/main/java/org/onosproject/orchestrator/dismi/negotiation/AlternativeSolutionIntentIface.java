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

package org.onosproject.orchestrator.dismi.negotiation;

import org.onosproject.orchestrator.dismi.aciIntents.DismiIntentId;
import org.onosproject.orchestrator.dismi.primitives.Intent;

import java.util.Set;

/**
 * Created by aghafoor on 2017-06-02.
 */
public interface AlternativeSolutionIntentIface {
    Set<Intent> get(DismiIntentId key);

    Set<Intent> remove(DismiIntentId key);

    boolean add(DismiIntentId key, Intent asIntent);

    Set<Intent> put(DismiIntentId asId, Set<Intent> asIntents);

    Set<DismiIntentId> listKeys();
}

