/*
 * Copyright 2016-present Open Networking Foundation
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
package org.onosproject.store.primitives.impl;

import com.google.common.collect.Maps;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.ControllerNode;
import org.onosproject.cluster.Member;
import org.onosproject.cluster.MembershipService;
import org.onosproject.cluster.NodeId;
import org.onosproject.cluster.PartitionId;
import org.onosproject.persistence.PersistenceService;
import org.onosproject.store.cluster.messaging.ClusterCommunicationService;
import org.onosproject.store.primitives.DistributedPrimitiveCreator;
import org.onosproject.store.primitives.PartitionAdminService;
import org.onosproject.store.primitives.PartitionService;
import org.onosproject.store.primitives.TransactionId;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.AsyncAtomicValue;
import org.onosproject.store.service.AsyncConsistentMultimap;
import org.onosproject.store.service.AsyncConsistentTreeMap;
import org.onosproject.store.service.AsyncDocumentTree;
import org.onosproject.store.service.AtomicCounterBuilder;
import org.onosproject.store.service.AtomicCounterMapBuilder;
import org.onosproject.store.service.AtomicIdGeneratorBuilder;
import org.onosproject.store.service.AtomicValueBuilder;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.ConsistentMapBuilder;
import org.onosproject.store.service.ConsistentMultimapBuilder;
import org.onosproject.store.service.ConsistentTreeMapBuilder;
import org.onosproject.store.service.DistributedSetBuilder;
import org.onosproject.store.service.DocumentTreeBuilder;
import org.onosproject.store.service.EventuallyConsistentMapBuilder;
import org.onosproject.store.service.LeaderElectorBuilder;
import org.onosproject.store.service.MapInfo;
import org.onosproject.store.service.PartitionInfo;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageAdminService;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.Topic;
import org.onosproject.store.service.TransactionContextBuilder;
import org.onosproject.store.service.WorkQueue;
import org.onosproject.store.service.WorkQueueStats;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.onosproject.security.AppGuard.checkPermission;
import static org.onosproject.security.AppPermission.Type.STORAGE_WRITE;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation for {@code StorageService} and {@code StorageAdminService}.
 */
@Service
@Component(immediate = true)
public class StorageManager implements StorageService, StorageAdminService {

    private static final int BUCKETS = 128;

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterService clusterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterCommunicationService clusterCommunicator;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PersistenceService persistenceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PartitionService partitionService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PartitionAdminService partitionAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MembershipService membershipService;

    private final Supplier<TransactionId> transactionIdGenerator =
            () -> TransactionId.from(UUID.randomUUID().toString());
    private DistributedPrimitiveCreator federatedPrimitiveCreator;
    private TransactionManager transactionManager;

    @Activate
    public void activate() {
        Map<PartitionId, DistributedPrimitiveCreator> partitionMap = Maps.newHashMap();
        partitionService.getAllPartitionIds().stream()
            .filter(id -> !id.equals(PartitionId.SHARED))
            .forEach(id -> partitionMap.put(id, partitionService.getDistributedPrimitiveCreator(id)));
        federatedPrimitiveCreator = new FederatedDistributedPrimitiveCreator(partitionMap, BUCKETS);
        transactionManager = new TransactionManager(this, partitionService, BUCKETS);
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
    }

    @Override
    public <K, V> EventuallyConsistentMapBuilder<K, V> eventuallyConsistentMapBuilder() {
        checkPermission(STORAGE_WRITE);
        final NodeId localNodeId = clusterService.getLocalNode().id();

        Supplier<List<NodeId>> peersSupplier = () -> membershipService.getMembers().stream()
                .map(Member::nodeId)
                .filter(nodeId -> !nodeId.equals(localNodeId))
                .filter(id -> clusterService.getState(id).isActive())
                .collect(Collectors.toList());

        Supplier<List<NodeId>> bootstrapPeersSupplier = () -> {
            if (membershipService.getMembers().size() == 1) {
                return clusterService.getNodes()
                        .stream()
                        .map(ControllerNode::id)
                        .filter(id -> !localNodeId.equals(id))
                        .filter(id -> clusterService.getState(id).isActive())
                        .collect(Collectors.toList());
            } else {
                return membershipService.getMembers()
                        .stream()
                        .map(Member::nodeId)
                        .filter(id -> !localNodeId.equals(id))
                        .filter(id -> clusterService.getState(id).isActive())
                        .collect(Collectors.toList());
            }
        };


        return new EventuallyConsistentMapBuilderImpl<>(
                localNodeId,
                clusterCommunicator,
                persistenceService,
                peersSupplier,
                bootstrapPeersSupplier
        );
    }

    @Override
    public <K, V> ConsistentMapBuilder<K, V> consistentMapBuilder() {
        checkPermission(STORAGE_WRITE);
        return new DefaultConsistentMapBuilder<>(federatedPrimitiveCreator);
    }

    @Override
    public <V> DocumentTreeBuilder<V> documentTreeBuilder() {
        checkPermission(STORAGE_WRITE);
        return new DefaultDocumentTreeBuilder<V>(federatedPrimitiveCreator);
    }

    @Override
    public <V> ConsistentTreeMapBuilder<V> consistentTreeMapBuilder() {
        return new DefaultConsistentTreeMapBuilder<V>(
                federatedPrimitiveCreator);
    }

    @Override
    public <K, V> ConsistentMultimapBuilder<K, V> consistentMultimapBuilder() {
        checkPermission(STORAGE_WRITE);
        return new DefaultConsistentMultimapBuilder<K, V>(
                federatedPrimitiveCreator);
    }

    @Override
    public <K> AtomicCounterMapBuilder<K> atomicCounterMapBuilder() {
        checkPermission(STORAGE_WRITE);
        return new DefaultAtomicCounterMapBuilder<>(federatedPrimitiveCreator);
    }

    @Override
    public <E> DistributedSetBuilder<E> setBuilder() {
        checkPermission(STORAGE_WRITE);
        return new DefaultDistributedSetBuilder<>(() -> this.<E, Boolean>consistentMapBuilder());
    }

    @Override
    public AtomicCounterBuilder atomicCounterBuilder() {
        checkPermission(STORAGE_WRITE);
        return new DefaultAtomicCounterBuilder(federatedPrimitiveCreator);
    }

    @Override
    public AtomicIdGeneratorBuilder atomicIdGeneratorBuilder() {
        checkPermission(STORAGE_WRITE);
        return new DefaultAtomicIdGeneratorBuilder(federatedPrimitiveCreator);
    }

    @Override
    public <V> AtomicValueBuilder<V> atomicValueBuilder() {
        checkPermission(STORAGE_WRITE);
        Supplier<ConsistentMapBuilder<String, byte[]>> mapBuilderSupplier =
                () -> this.<String, byte[]>consistentMapBuilder()
                          .withName("onos-atomic-values")
                          .withSerializer(Serializer.using(KryoNamespaces.BASIC));
        return new DefaultAtomicValueBuilder<>(mapBuilderSupplier);
    }

    @Override
    public TransactionContextBuilder transactionContextBuilder() {
        checkPermission(STORAGE_WRITE);
        return new DefaultTransactionContextBuilder(transactionIdGenerator.get(), transactionManager);
    }

    @Override
    public LeaderElectorBuilder leaderElectorBuilder() {
        checkPermission(STORAGE_WRITE);
        return new DefaultLeaderElectorBuilder(federatedPrimitiveCreator);
    }

    @Override
    public <E> WorkQueue<E> getWorkQueue(String name, Serializer serializer) {
        checkPermission(STORAGE_WRITE);
        return federatedPrimitiveCreator.newWorkQueue(name, serializer);
    }

    @Override
    public <V> AsyncDocumentTree<V> getDocumentTree(String name, Serializer serializer) {
        checkPermission(STORAGE_WRITE);
        return federatedPrimitiveCreator.newAsyncDocumentTree(name, serializer);
    }

    @Override
    public <K, V> AsyncConsistentMultimap<K, V> getAsyncSetMultimap(
            String name, Serializer serializer) {
        checkPermission(STORAGE_WRITE);
        return federatedPrimitiveCreator.newAsyncConsistentSetMultimap(name,
                                                                serializer);
    }

    @Override
    public <V> AsyncConsistentTreeMap<V> getAsyncTreeMap(
            String name, Serializer serializer) {
        checkPermission(STORAGE_WRITE);
        return federatedPrimitiveCreator.newAsyncConsistentTreeMap(name,
                                                                   serializer);
    }

    @Override
    public List<MapInfo> getMapInfo() {
        return listMapInfo(federatedPrimitiveCreator);
    }

    @Override
    public Map<String, Long> getCounters() {
        Map<String, Long> counters = Maps.newConcurrentMap();
        federatedPrimitiveCreator.getAsyncAtomicCounterNames()
               .forEach(name -> counters.put(name,
                       federatedPrimitiveCreator.newAsyncCounter(name).asAtomicCounter().get()));
        return counters;
    }

    @Override
    public Map<String, WorkQueueStats> getQueueStats() {
        Map<String, WorkQueueStats> workQueueStats = Maps.newConcurrentMap();
        federatedPrimitiveCreator.getWorkQueueNames()
               .forEach(name -> workQueueStats.put(name,
                       federatedPrimitiveCreator.newWorkQueue(name,
                                                              Serializer.using(KryoNamespaces.BASIC))
                                                .stats()
                                                .join()));
        return workQueueStats;
    }

    @Override
    public List<PartitionInfo> getPartitionInfo() {
        return partitionAdminService.partitionInfo();
    }

    @Override
    public Collection<TransactionId> getPendingTransactions() {
        return transactionManager.getPendingTransactions();
    }

    private List<MapInfo> listMapInfo(DistributedPrimitiveCreator creator) {
        Serializer serializer = Serializer.using(KryoNamespaces.BASIC);
        return creator.getAsyncConsistentMapNames()
        .stream()
        .map(name -> {
            ConsistentMap<String, byte[]> map =
                    creator.<String, byte[]>newAsyncConsistentMap(name, serializer)
                                             .asConsistentMap();
                    return new MapInfo(name, map.size());
        }).collect(Collectors.toList());
    }

    @Override
    public <T> Topic<T> getTopic(String name, Serializer serializer) {
        AsyncAtomicValue<T> atomicValue = this.<T>atomicValueBuilder()
                                              .withName("topic-" + name)
                                              .withSerializer(serializer)
                                              .build();
        return new DefaultDistributedTopic<>(atomicValue);
    }
}
