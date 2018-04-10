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

package org.onosproject.orchestrator.impl;

import com.google.common.collect.ImmutableSet;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.KryoNamespace;
import org.onosproject.app.ApplicationService;
import org.onosproject.net.intent.Intent;
import org.onosproject.orchestrator.api.ServiceId;
import org.onosproject.orchestrator.api.ServiceStore;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
@Service
public class DistributedOrchestratorStore implements ServiceStore {

    private static final Serializer SERIALIZER = Serializer
            .using(new KryoNamespace.Builder().register(KryoNamespaces.API)
                           .register(ServiceId.class)
                           //TODO: addAditionalClass
                           .nextId(KryoNamespaces.BEGIN_USER_CUSTOM_ID)
                           .build("OrchestratorStore"));
    private final Logger log = getLogger(getClass());
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ApplicationService applicationService;
    private ConsistentMap<ServiceId, Set<Intent>> serviceIdIntentsConsistentMap;
    private Map<ServiceId, Set<Intent>> intentServiceIdMap;

    @Activate
    public void activate() {

        serviceIdIntentsConsistentMap = storageService.<ServiceId, Set<Intent>>consistentMapBuilder()
                .withSerializer(SERIALIZER)
                .withName("onos-serviceId-intents")
                .withRelaxedReadConsistency()
                .build();
        intentServiceIdMap = serviceIdIntentsConsistentMap.asJavaMap();

        log.info("Started");

        //FIXME: this is a test to add a service. It should be removed!
        addIntentToService(ServiceId.serviceId("ciao"), null);
    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
    }

    @Override
    public Set<ServiceId> getServices() {
        return ImmutableSet.copyOf(intentServiceIdMap.keySet());
    }

    @Override
    public void addIntentToService(ServiceId serviceId, Intent intent) {


        Set<Intent> intentSet = intentServiceIdMap.get(serviceId);
        if (intentSet == null) {
            intentSet = new HashSet<>();
        }
        intentSet.add(intent);
        intentServiceIdMap.put(serviceId, intentSet);
    }
}
