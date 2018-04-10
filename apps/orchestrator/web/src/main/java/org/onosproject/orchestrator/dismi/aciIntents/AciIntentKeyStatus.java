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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.onosproject.net.intent.IntentEvent;
import org.onosproject.net.intent.Key;
import org.onosproject.orchestrator.dismi.abstraction.AbstractionLinkList;

/**
 * Created by aghafoor on 2017-03-16.
 * The puspose if to store Aci intent key along with current status to make decision about the status of DISMI
 * intents.
 */
public class AciIntentKeyStatus {

    // Use to identify aci key
    private Key intentKey;
    // the status of aci intent
    private IntentEvent.Type status;

    private AbstractionLinkList abstractionLinks;

    private boolean isCalculated;

    public AciIntentKeyStatus() {

    }

    public AciIntentKeyStatus(Key intentKey, IntentEvent.Type status, boolean isCalculated) {
        this.isCalculated = isCalculated;
        this.intentKey = intentKey;
        this.status = status;
        this.abstractionLinks = null;

    }

    public Key getIntentKey() {
        return intentKey;
    }

    public void setIntentKey(Key intentKey) {
        this.intentKey = intentKey;
    }

    public IntentEvent.Type getStatus() {
        return status;
    }

    public void setStatus(IntentEvent.Type status) {
        this.status = status;
    }

    public boolean isCalculated() {
        return isCalculated;
    }

    public void setCalculated(boolean calculated) {
        isCalculated = calculated;
    }

    public AbstractionLinkList getAbstractionLinks() {
        return abstractionLinks;
    }

    public void setAbstractionLinks(AbstractionLinkList abstractionLinks) {
        this.abstractionLinks = abstractionLinks;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("intentKey", intentKey)
                .add("status", status)
                .add("abstractionLinks", abstractionLinks)
                .add("isCalculated", isCalculated)
                .toString();
    }
}
