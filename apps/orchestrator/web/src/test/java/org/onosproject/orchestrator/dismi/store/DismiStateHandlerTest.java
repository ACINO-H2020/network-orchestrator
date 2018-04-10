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

package org.onosproject.orchestrator.dismi.store;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.DefaultApplicationId;
import org.onosproject.net.intent.IntentEvent;
import org.onosproject.net.intent.Key;
import org.onosproject.orchestrator.dismi.abstraction.AbstractionLinkList;
import org.onosproject.orchestrator.dismi.aciIntents.AciIntentKeyStatus;
import org.onosproject.orchestrator.dismi.aciIntents.AciStoreIface;
import org.onosproject.orchestrator.dismi.aciIntents.DismiIntentId;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class DismiStateHandlerTest {

    private DismiStateHandler dismiStateHandler;
    private Key intentKey;
    private AciStoreIface aciStoreIface;
    private ApplicationId applicationId;

    @Before
    public void setUp() throws Exception {

        aciStoreIface = new TestAciIface();
        applicationId = new DefaultApplicationId(1, "orchestrator");

        dismiStateHandler = new DismiStateHandler(null, aciStoreIface, null, null);

    }

    @Test
    public void dismiNextStatusFromNegotiationToInstallationSuccess() {

        DismiIntentId intentId = DismiIntentId.getId("intent1");
        intentKey = DismiIntentId.createDismiIntentKey(applicationId, intentId, 1);

        AciIntentKeyStatus aciIntentKeyStatus = new AciIntentKeyStatus();
        aciIntentKeyStatus.setIntentKey(intentKey);
        aciIntentKeyStatus.setStatus(IntentEvent.Type.NEGOTIATION_REQ);

        aciStoreIface.put(intentId, Sets.newHashSet(aciIntentKeyStatus));

        AciIntentKeyStatus newAciIntentKeyStatus = new AciIntentKeyStatus();
        newAciIntentKeyStatus.setIntentKey(intentKey);
        newAciIntentKeyStatus.setStatus(IntentEvent.Type.INSTALLED);

        IntentFsmEvent event = dismiStateHandler
                .findDismiIntentNextStatus(intentId, intentKey, newAciIntentKeyStatus, true);

        assertEquals(IntentFsmEvent.InstallationSuccess, event);

        aciStoreIface.removeDismiIntent(intentId);

    }

    @Test
    public void dismiNextStatusFromNegotiationToWithdrawn() {

        DismiIntentId intentId = DismiIntentId.getId("intent1");

        AciIntentKeyStatus aciIntentKeyStatus = new AciIntentKeyStatus();
        aciIntentKeyStatus.setIntentKey(this.intentKey);
        aciIntentKeyStatus.setStatus(IntentEvent.Type.NEGOTIATION_REQ);

        aciStoreIface.put(intentId, Sets.newHashSet(aciIntentKeyStatus));

        AciIntentKeyStatus newAciIntentKeyStatus = new AciIntentKeyStatus();
        newAciIntentKeyStatus.setIntentKey(this.intentKey);
        newAciIntentKeyStatus.setStatus(IntentEvent.Type.WITHDRAWN);

        IntentFsmEvent event = dismiStateHandler
                .findDismiIntentNextStatus(intentId, intentKey, newAciIntentKeyStatus, false);

        assertEquals(IntentFsmEvent.WithdrawalSuccess, event);

        aciStoreIface.removeDismiIntent(intentId);

    }

    @Test
    public void dismiNextStatusMultipleIntents() {

        DismiIntentId intentId = DismiIntentId.getId("intent1");
        Key intentOneKey = DismiIntentId.createDismiIntentKey(applicationId, intentId, 1);

        AciIntentKeyStatus aciIntentKeyStatus = new AciIntentKeyStatus();
        aciIntentKeyStatus.setIntentKey(intentOneKey);
        aciIntentKeyStatus.setStatus(IntentEvent.Type.NEGOTIATION_REQ);

        Key intentTwoKey = DismiIntentId.createDismiIntentKey(applicationId, intentId, 2);
        AciIntentKeyStatus aciKeyIntentTwo = new AciIntentKeyStatus();
        aciKeyIntentTwo.setIntentKey(intentTwoKey);
        aciKeyIntentTwo.setStatus(IntentEvent.Type.WITHDRAWN);

        aciStoreIface.put(intentId, Sets.newHashSet(aciIntentKeyStatus, aciKeyIntentTwo));

        AciIntentKeyStatus newAciIntentKeyStatus = new AciIntentKeyStatus();
        newAciIntentKeyStatus.setIntentKey(intentOneKey);
        newAciIntentKeyStatus.setStatus(IntentEvent.Type.INSTALLED);

        IntentFsmEvent event = dismiStateHandler
                .findDismiIntentNextStatus(intentId, intentOneKey, newAciIntentKeyStatus, true);

        assertEquals(IntentFsmEvent.InstallationSuccess, event);

        aciStoreIface.removeDismiIntent(intentId);

    }

    private class TestAciIface implements AciStoreIface {

        private Map<DismiIntentId, Set<AciIntentKeyStatus>> aciDatabase = Maps.newHashMap();

        @Override
        public Set<AciIntentKeyStatus> getKeys(DismiIntentId dismiID) {
            return aciDatabase.computeIfAbsent(dismiID, k -> Sets.newHashSet());
        }

        @Override
        public boolean removeDismiIntent(DismiIntentId dismiID) {
            aciDatabase.remove(dismiID);
            return true;
        }

        @Override
        public void put(DismiIntentId dismiID, Set<AciIntentKeyStatus> keys) {
            aciDatabase.put(dismiID, keys);
        }

        @Override
        public void updateKey(DismiIntentId dismiID, AciIntentKeyStatus keys) {

        }

        @Override
        public Set<DismiIntentId> listDismiIntentId() {
            return aciDatabase.keySet();
        }

        @Override
        public void addKeyIntent(Key intentKey, org.onosproject.net.intent.Intent onosIntent) {

        }

        @Override
        public org.onosproject.net.intent.Intent removeIntentKey(Key intentKey) {
            return null;
        }

        @Override
        public void updateAbstractLinkList(DismiIntentId dismiIntentId, Key key, AbstractionLinkList abstractionLinks) {

        }
    }
}