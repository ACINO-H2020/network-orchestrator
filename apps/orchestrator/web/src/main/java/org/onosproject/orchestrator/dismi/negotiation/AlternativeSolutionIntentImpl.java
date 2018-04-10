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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.KryoNamespace;
import org.onosproject.orchestrator.dismi.aciIntents.DismiIntentId;
import org.onosproject.orchestrator.dismi.primitives.Action;
import org.onosproject.orchestrator.dismi.primitives.Aggregate;
import org.onosproject.orchestrator.dismi.primitives.AvailabilityConstraint;
import org.onosproject.orchestrator.dismi.primitives.BandwidthConstraint;
import org.onosproject.orchestrator.dismi.primitives.Calendaring;
import org.onosproject.orchestrator.dismi.primitives.Connection;
import org.onosproject.orchestrator.dismi.primitives.ConnectionPoint;
import org.onosproject.orchestrator.dismi.primitives.Constraint;
import org.onosproject.orchestrator.dismi.primitives.DelayConstraint;
import org.onosproject.orchestrator.dismi.primitives.DismiIntentState;
import org.onosproject.orchestrator.dismi.primitives.EndPoint;
import org.onosproject.orchestrator.dismi.primitives.EthEndPoint;
import org.onosproject.orchestrator.dismi.primitives.EthSelector;
import org.onosproject.orchestrator.dismi.primitives.FiberEndPoint;
import org.onosproject.orchestrator.dismi.primitives.GprsSelector;
import org.onosproject.orchestrator.dismi.primitives.HighAvailabilityConstraint;
import org.onosproject.orchestrator.dismi.primitives.IPEndPoint;
import org.onosproject.orchestrator.dismi.primitives.IPSelector;
import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.LambdaEndPoint;
import org.onosproject.orchestrator.dismi.primitives.LambdaSelector;
import org.onosproject.orchestrator.dismi.primitives.Mesh;
import org.onosproject.orchestrator.dismi.primitives.Multicast;
import org.onosproject.orchestrator.dismi.primitives.Path;
import org.onosproject.orchestrator.dismi.primitives.Priority;
import org.onosproject.orchestrator.dismi.primitives.SDWAN;
import org.onosproject.orchestrator.dismi.primitives.SecurityConstraint;
import org.onosproject.orchestrator.dismi.primitives.Selector;
import org.onosproject.orchestrator.dismi.primitives.ServerInfo;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.Tree;
import org.onosproject.orchestrator.dismi.primitives.extended.BandwidthConstraintExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.CalendaringExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.ConnectionPointExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.DelayConstraintExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.IntentExtended;
import org.onosproject.orchestrator.dismi.store.CpId;
import org.onosproject.orchestrator.dismi.store.IntentFiniteStateMachine;
import org.onosproject.orchestrator.dismi.store.InternalIntentState;
import org.onosproject.orchestrator.dismi.store.ServiceId;
import org.onosproject.orchestrator.dismi.utils.InputAssertion;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.slf4j.Logger;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2017-06-02.
 */

@Component(immediate = true)
@Service
public class AlternativeSolutionIntentImpl implements AlternativeSolutionIntentIface {

    private static final Serializer SERIALIZER = Serializer
            .using(new KryoNamespace
                    .Builder()
                           .register(KryoNamespaces.API)

                           //  Aci store primitives
                           .register(CpId.class)
                           .register(EndPoint.class)
                           .register(IPEndPoint.class)
                           .register(EthEndPoint.class)
                           .register(LambdaEndPoint.class)
                           .register(FiberEndPoint.class)
                           .register(DismiIntentId.class)
                           .register(ServiceId.class)
                           .register(Action.class)
                           .register(Aggregate.class)
                           .register(AvailabilityConstraint.class)
                           .register(BandwidthConstraint.class)
                           .register(Calendaring.class)
                           .register(Calendaring.RecurrenceEnum.class)
                           .register(Connection.class)
                           .register(ConnectionPoint.class)
                           .register(Constraint.class)
                           .register(Date.class)
                           .register(DismiIntentState.class)
                           .register(DelayConstraint.class)
                           .register(EthSelector.class)
                           .register(GprsSelector.class)
                           .register(Intent.class)
                           .register(IntentExtended.class)
                           .register(IntentFiniteStateMachine.class)
                           .register(InternalIntentState.class)
                           .register(IPSelector.class)
                           .register(IPSelector.IpProtocolEnum.class)
                           .register(Issue.class)
                           .register(Issue.SeverityEnum.class)
                           .register(Issue.ErrorTypeEnum.class)
                           .register(LambdaSelector.class)
                           .register(Mesh.class)
                           .register(Multicast.class)
                           .register(Path.class)
                           .register(Priority.class)
                           .register(Priority.PriorityEnum.class)
                           //.register(org.onosproject.orchestrator.dismi.primitives.Service.class)
                           //.register(ServiceExtended.class)
                           .register(SecurityConstraint.class)
                           .register(SecurityConstraint.EncStrengthEnum.class)
                           .register(SecurityConstraint.IntStrengthEnum.class)
                           .register(Selector.class)
                           .register(Subject.class)
                           .register(Tracker.class)
                           .register(Tree.class)
                           .register(ServerInfo.class)
                           .register(ConnectionPointExtended.class)
                           .register(CalendaringExtended.class)
                           .register(InputAssertion.IntentTime.class)
                           .register(BandwidthConstraintExtended.class)
                           .register(DelayConstraintExtended.class)
                           .register(HighAvailabilityConstraint.class)
                           .register(SDWAN.class)
                           .build("AciNegotiationStore"));
    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    private Map<DismiIntentId, Set<Intent>> storeMap;

    /**
     * To activate store and init storeConsistentMap
     */
    @Activate
    public void activate() {

        storeMap = storageService.<DismiIntentId, Set<Intent>>consistentMapBuilder()
                .withSerializer(SERIALIZER)
                .withName("consistent-map-negotiation-store")
                .withRelaxedReadConsistency()
                .build()
                .asJavaMap();
        log.info("Service NegotationStore started");
    }

    /**
     * Deallocate resources if you have
     */
    @Deactivate
    public void deactivate() {
        log.info("Service NegotationStore stopped");
    }

    /**
     * @param intentId
     * @return
     */
    @Override
    public Set<Intent> get(DismiIntentId intentId) {
        //  Sanity check
        if (null == intentId) {
            Set<Intent> set = new HashSet<>();
            return ImmutableSet.copyOf(set);
        }
        //  Retrieve the set for this String
        Set<Intent> set = storeMap.get(intentId);
        if (null == set) {
            // Can't be, should return an empty Set instead. But who knows... Return empty Map
            set = new HashSet<>();
            return ImmutableSet.copyOf(set);
        }
        return ImmutableSet.copyOf(set);
    }

    /**
     * @param intentId
     * @return
     */
    @Override
    public Set<Intent> remove(DismiIntentId intentId) {
        //  Sanity check
        if (null == intentId) {
            return new HashSet<>(); // Return empty Map
        }
        //  Retrieve the set for this intentId
        Set<Intent> set = storeMap.remove(intentId);
        if (null == set) {
            // Can't be, should return an empty Set instead. But who knows... Return empty Map
            return new HashSet<>();
        }
        return ImmutableSet.copyOf(set);
    }

    /**
     * @param intentId
     * @param Intent
     * @return
     */
    @Override
    public boolean add(DismiIntentId intentId, Intent Intent) {
        //  Sanity check
        if ((null == intentId) || (null == Intent)) {
            return false;
        }
        //  Retrieve the set for this intentId
        Set<Intent> set = storeMap.get(intentId);
        boolean isOK = set.add(Intent);
        return isOK;
    }

    /**
     * @param intentId
     * @param Intents
     * @return
     */
    @Override
    public Set<Intent> put(DismiIntentId intentId, Set<Intent> Intents) {
        //  Sanity check
        if ((null == intentId) || (null == Intents)) {
            return new HashSet<>();
        }

        storeMap.computeIfPresent(intentId, (dismiIntentId, intents) -> {
            intents.addAll(Intents);
            return intents;
        });

        storeMap.computeIfAbsent(intentId, (id) -> Sets.newHashSet(Intents));

        return ImmutableSet.copyOf(storeMap.get(intentId));
    }

    /**
     * @return
     */
    @Override
    public Set<DismiIntentId> listKeys() {
        return ImmutableSet.copyOf(storeMap.keySet());
    }
}
