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

import org.onlab.util.Identifier;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.intent.Key;

import static com.google.common.base.Preconditions.checkArgument;

public final class DismiIntentId extends Identifier<String> {

    // Public construction is prohibited
    private DismiIntentId(String id) {
        super(id);
        checkArgument(id != null && id.length() > 0, "DismiIntentId cannot be null or empty");
    }

    // Default constructor for serialization
    protected DismiIntentId() {
        super("");
    }

    /**
     * Creates a service id using the supplied backing id.
     *
     * @param id service id
     * @return service identifier
     */
    public static DismiIntentId getId(String id) {
        return new DismiIntentId(id);
    }

    /**
     * Creates a key for an intent based on hash values of intent id and its aci no, and applcaiotns id.
     *
     * @return intent key if specified, null otherwise
     */
    public static Key createDismiIntentKey(ApplicationId appIdForIntent, DismiIntentId dismiIntentId, int aciIntentNo) {
        return Key.of(dismiIntentId + "-" + aciIntentNo, appIdForIntent);
    }

    public static Key calculateKeyFromString(String intentKey, ApplicationId applicationId) {
        return Key.of(intentKey, applicationId);
    }

}
