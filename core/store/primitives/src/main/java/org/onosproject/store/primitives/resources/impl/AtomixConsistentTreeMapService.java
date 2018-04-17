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

package org.onosproject.store.primitives.resources.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.google.common.collect.Maps;
import io.atomix.protocols.raft.service.Commit;
import io.atomix.protocols.raft.service.RaftServiceExecutor;
import io.atomix.protocols.raft.session.RaftSession;
import org.onlab.util.KryoNamespace;
import org.onosproject.store.primitives.TransactionId;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.TransactionLog;
import org.onosproject.store.service.Versioned;

import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.CEILING_ENTRY;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.CEILING_KEY;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.CeilingEntry;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.CeilingKey;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.FIRST_ENTRY;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.FIRST_KEY;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.FLOOR_ENTRY;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.FLOOR_KEY;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.FloorEntry;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.FloorKey;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.HIGHER_ENTRY;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.HIGHER_KEY;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.HigherEntry;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.HigherKey;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.LAST_ENTRY;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.LAST_KEY;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.LOWER_ENTRY;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.LOWER_KEY;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.LowerEntry;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.LowerKey;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.POLL_FIRST_ENTRY;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.POLL_LAST_ENTRY;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.SUB_MAP;
import static org.onosproject.store.primitives.resources.impl.AtomixConsistentTreeMapOperations.SubMap;

/**
 * State machine corresponding to {@link AtomixConsistentTreeMap} backed by a
 * {@link TreeMap}.
 */
public class AtomixConsistentTreeMapService extends AtomixConsistentMapService {

    private static final Serializer SERIALIZER = Serializer.using(KryoNamespace.newBuilder()
            .register(KryoNamespaces.BASIC)
            .register(AtomixConsistentMapOperations.NAMESPACE)
            .register(AtomixConsistentTreeMapOperations.NAMESPACE)
            .register(AtomixConsistentMapEvents.NAMESPACE)
            .nextId(KryoNamespaces.BEGIN_USER_CUSTOM_ID + 150)
            .register(TransactionScope.class)
            .register(TransactionLog.class)
            .register(TransactionId.class)
            .register(MapEntryValue.class)
            .register(MapEntryValue.Type.class)
            .register(new HashMap().keySet().getClass())
            .register(TreeMap.class)
            .build());

    @Override
    protected TreeMap<String, MapEntryValue> createMap() {
        return Maps.newTreeMap();
    }

    @Override
    protected TreeMap<String, MapEntryValue> entries() {
        return (TreeMap<String, MapEntryValue>) super.entries();
    }

    @Override
    protected Serializer serializer() {
        return SERIALIZER;
    }

    @Override
    public void configure(RaftServiceExecutor executor) {
        super.configure(executor);
        executor.register(SUB_MAP, serializer()::decode, this::subMap, serializer()::encode);
        executor.register(FIRST_KEY, (Commit<Void> c) -> firstKey(), serializer()::encode);
        executor.register(LAST_KEY, (Commit<Void> c) -> lastKey(), serializer()::encode);
        executor.register(FIRST_ENTRY, (Commit<Void> c) -> firstEntry(), serializer()::encode);
        executor.register(LAST_ENTRY, (Commit<Void> c) -> lastEntry(), serializer()::encode);
        executor.register(POLL_FIRST_ENTRY, (Commit<Void> c) -> pollFirstEntry(), serializer()::encode);
        executor.register(POLL_LAST_ENTRY, (Commit<Void> c) -> pollLastEntry(), serializer()::encode);
        executor.register(LOWER_ENTRY, serializer()::decode, this::lowerEntry, serializer()::encode);
        executor.register(LOWER_KEY, serializer()::decode, this::lowerKey, serializer()::encode);
        executor.register(FLOOR_ENTRY, serializer()::decode, this::floorEntry, serializer()::encode);
        executor.register(FLOOR_KEY, serializer()::decode, this::floorKey, serializer()::encode);
        executor.register(CEILING_ENTRY, serializer()::decode, this::ceilingEntry, serializer()::encode);
        executor.register(CEILING_KEY, serializer()::decode, this::ceilingKey, serializer()::encode);
        executor.register(HIGHER_ENTRY, serializer()::decode, this::higherEntry, serializer()::encode);
        executor.register(HIGHER_KEY, serializer()::decode, this::higherKey, serializer()::encode);
    }

    protected NavigableMap<String, MapEntryValue> subMap(
            Commit<? extends SubMap> commit) {
        // Do not support this until lazy communication is possible.  At present
        // it transmits up to the entire map.
        SubMap<String, MapEntryValue> subMap = commit.value();
        return entries().subMap(subMap.fromKey(), subMap.isInclusiveFrom(),
                subMap.toKey(), subMap.isInclusiveTo());
    }

    protected String firstKey() {
        return isEmpty() ? null : entries().firstKey();
    }

    protected String lastKey() {
        return isEmpty() ? null : entries().lastKey();
    }

    protected Map.Entry<String, Versioned<byte[]>> higherEntry(Commit<? extends HigherEntry> commit) {
        return isEmpty() ? null : toVersionedEntry(entries().higherEntry(commit.value().key()));
    }

    protected Map.Entry<String, Versioned<byte[]>> firstEntry() {
        return isEmpty() ? null : toVersionedEntry(entries().firstEntry());
    }

    protected Map.Entry<String, Versioned<byte[]>> lastEntry() {
        return isEmpty() ? null : toVersionedEntry(entries().lastEntry());
    }

    protected Map.Entry<String, Versioned<byte[]>> pollFirstEntry() {
        return toVersionedEntry(entries().pollFirstEntry());
    }

    protected Map.Entry<String, Versioned<byte[]>> pollLastEntry() {
        return toVersionedEntry(entries().pollLastEntry());
    }

    protected Map.Entry<String, Versioned<byte[]>> lowerEntry(Commit<? extends LowerEntry> commit) {
        return toVersionedEntry(entries().lowerEntry(commit.value().key()));
    }

    protected String lowerKey(Commit<? extends LowerKey> commit) {
        return entries().lowerKey(commit.value().key());
    }

    protected Map.Entry<String, Versioned<byte[]>> floorEntry(Commit<? extends FloorEntry> commit) {
        return toVersionedEntry(entries().floorEntry(commit.value().key()));
    }

    protected String floorKey(Commit<? extends FloorKey> commit) {
        return entries().floorKey(commit.value().key());
    }

    protected Map.Entry<String, Versioned<byte[]>> ceilingEntry(Commit<CeilingEntry> commit) {
        return toVersionedEntry(entries().ceilingEntry(commit.value().key()));
    }

    protected String ceilingKey(Commit<CeilingKey> commit) {
        return entries().ceilingKey(commit.value().key());
    }

    protected String higherKey(Commit<HigherKey> commit) {
        return entries().higherKey(commit.value().key());
    }

    private Map.Entry<String, Versioned<byte[]>> toVersionedEntry(
            Map.Entry<String, MapEntryValue> entry) {
        return entry == null || valueIsNull(entry.getValue())
                ? null : Maps.immutableEntry(entry.getKey(), toVersioned(entry.getValue()));
    }

    @Override
    public void onExpire(RaftSession session) {
        closeListener(session.sessionId().id());
    }

    @Override
    public void onClose(RaftSession session) {
        closeListener(session.sessionId().id());
    }

    private void closeListener(Long sessionId) {
        listeners.remove(sessionId);
    }
}