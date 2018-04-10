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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.KryoNamespace;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentEvent;
import org.onosproject.net.intent.Key;
import org.onosproject.net.intent.SDWANProviderOneIntent;
import org.onosproject.net.intent.SDWANProviderTwoIntent;
import org.onosproject.net.intent.ServiceProviderIntent;
import org.onosproject.net.intent.constraint.AvailabilityConstraint;
import org.onosproject.net.intent.constraint.HighAvailabilityConstraint;
import org.onosproject.orchestrator.dismi.abstraction.AbstractionLink;
import org.onosproject.orchestrator.dismi.abstraction.AbstractionLinkList;
import org.onosproject.orchestrator.dismi.primitives.EndPoint;
import org.onosproject.orchestrator.dismi.primitives.EthEndPoint;
import org.onosproject.orchestrator.dismi.primitives.FiberEndPoint;
import org.onosproject.orchestrator.dismi.primitives.IPEndPoint;
import org.onosproject.orchestrator.dismi.primitives.LambdaEndPoint;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
@Service
public class AciStoreImpl implements AciStoreIface {
    private static final Serializer SERIALIZER = Serializer
            .using(KryoNamespace.newBuilder()
                           .register(KryoNamespaces.API)

                           //  Aci store primitives
                           .register(DismiIntentId.class)
                           .register(EndPoint.class)
                           .register(IPEndPoint.class)
                           .register(EthEndPoint.class)
                           .register(LambdaEndPoint.class)
                           .register(FiberEndPoint.class)
                           .register(AbstractionLink.class)
                           .register(AbstractionLinkList.class)
                           .register(AciIntentKeyStatus.class)
                           .register(SDWANProviderOneIntent.class)
                           .register(SDWANProviderTwoIntent.class)
                           .register(ServiceProviderIntent.class)
                           .register(AvailabilityConstraint.class)
                           .register(HighAvailabilityConstraint.class)
                           .register(IntentEvent.class)
                           .register(IntentEvent.Type.class)
                           .build("AciCompilerStore"));
    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    private Map<DismiIntentId, Set<AciIntentKeyStatus>> storeMap;

    private Map<Key, Intent> intentDatabase;

    @Activate
    public void activate() {

        storeMap = storageService.<DismiIntentId, Set<AciIntentKeyStatus>>consistentMapBuilder()
                .withSerializer(SERIALIZER)
                .withName("consistent-map-aci-store")
                .withRelaxedReadConsistency()
                .build()
                .asJavaMap();

        intentDatabase = storageService.<Key, Intent>consistentMapBuilder()
                .withSerializer(SERIALIZER)
                .withName("consistent-map-database-intent-store")
                .withRelaxedReadConsistency()
                .build()
                .asJavaMap();

        log.info("Service AciStore started");
    }

    @Deactivate
    public void deactivate() {
        log.info("Service AciStore stopped");
    }

    /**
     * @param dismiID : to find aci keys and their status
     * @return : set of AciIntentKeyStatus belongs to dsimi id
     */
    @Override
    public Set<AciIntentKeyStatus> getKeys(DismiIntentId dismiID) {

        //  Retrieve the set for this dismiID
        storeMap.computeIfAbsent(dismiID, (dismiIntentId) -> Sets.newHashSet());
        return ImmutableSet.copyOf(storeMap.get(dismiID));
    }

    @Override
    public boolean removeDismiIntent(DismiIntentId dismiID) {

        //  Retrieve the set for this dismiID
        Set<AciIntentKeyStatus> set = storeMap.remove(dismiID);
        return set != null;
    }

    @Override
    public void put(DismiIntentId dismiID, Set<AciIntentKeyStatus> keys) {
        storeMap.put(dismiID, keys);
    }

    @Override
    public void updateKey(DismiIntentId dismiID, AciIntentKeyStatus key) {

        storeMap.computeIfAbsent(dismiID, dismiIntentId -> Sets.newHashSet(key));

        storeMap.computeIfPresent(dismiID, (dismiIntentId, aciIntentKeyStatuses) -> {
            aciIntentKeyStatuses.add(key);
            return aciIntentKeyStatuses;
        });
    }

    @Override
    public Set<DismiIntentId> listDismiIntentId() {
        return ImmutableSet.copyOf(storeMap.keySet());
    }

    @Override
    public void addKeyIntent(Key intentKey, Intent onosIntent) {
        intentDatabase.put(intentKey, onosIntent);
    }

    @Override
    public Intent removeIntentKey(Key intentKey) {
        return intentDatabase.remove(intentKey);
    }

    @Override
    public void updateAbstractLinkList(DismiIntentId dismiIntentId, Key key, AbstractionLinkList abstractionLinks) {

        Set<AciIntentKeyStatus> aciIntentKeyStatuses = getKeys(dismiIntentId);

        if (aciIntentKeyStatuses.isEmpty()) {

            AciIntentKeyStatus newIntentKeyStatus = new AciIntentKeyStatus(key, IntentEvent.Type.INSTALL_REQ, false);
            newIntentKeyStatus.setAbstractionLinks(abstractionLinks);
            updateKey(dismiIntentId, newIntentKeyStatus);

        } else {

            Set<AciIntentKeyStatus> updatedaciIntentKeyStatuses = new HashSet<AciIntentKeyStatus>();

            boolean matching = false;

            for (AciIntentKeyStatus keyStatus : aciIntentKeyStatuses) {

                if (keyStatus.getIntentKey().equals(key)) {

                    matching = true;

                    AciIntentKeyStatus aciIntentKeyStatus = new AciIntentKeyStatus();
                    aciIntentKeyStatus.setIntentKey(keyStatus.getIntentKey());
                    aciIntentKeyStatus.setStatus(keyStatus.getStatus());
                    aciIntentKeyStatus.setCalculated(keyStatus.isCalculated());
                    aciIntentKeyStatus.setAbstractionLinks(abstractionLinks);

                    updatedaciIntentKeyStatuses.add(aciIntentKeyStatus);
                } else {
                    updatedaciIntentKeyStatuses.add(keyStatus);
                }
            }

            if (!matching) {
                AciIntentKeyStatus newIntentKeyStatus = new AciIntentKeyStatus(key, IntentEvent.Type.INSTALL_REQ, false);
                newIntentKeyStatus.setAbstractionLinks(abstractionLinks);
                updatedaciIntentKeyStatuses.add(newIntentKeyStatus);
            }

            put(dismiIntentId, updatedaciIntentKeyStatuses);
        }

        log.info("Abstract endpoint list updated !");

    }
}
