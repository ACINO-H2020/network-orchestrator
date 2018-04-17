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
package org.onosproject.store.primitives;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.onosproject.store.service.AsyncAtomicCounter;
import org.onosproject.store.service.AsyncAtomicCounterMap;
import org.onosproject.store.service.AsyncAtomicIdGenerator;
import org.onosproject.store.service.AsyncAtomicValue;
import org.onosproject.store.service.AsyncConsistentMap;
import org.onosproject.store.service.AsyncConsistentMultimap;
import org.onosproject.store.service.AsyncConsistentTreeMap;
import org.onosproject.store.service.AsyncDistributedSet;
import org.onosproject.store.service.AsyncDocumentTree;
import org.onosproject.store.service.AsyncLeaderElector;
import org.onosproject.store.service.Ordering;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.WorkQueue;

import static org.onosproject.store.service.DistributedPrimitive.DEFAULT_OPERATION_TIMEOUT_MILLIS;

/**
 * Interface for entity that can create instances of different distributed primitives.
 */
public interface DistributedPrimitiveCreator {

    /**
     * Creates a new {@code AsyncConsistentMap}.
     *
     * @param name map name
     * @param serializer serializer to use for serializing/deserializing map entries
     * @param <K> key type
     * @param <V> value type
     * @return map
     */
    <K, V> AsyncConsistentMap<K, V> newAsyncConsistentMap(String name, Serializer serializer);

    /**
     * Creates a new {@code AsyncConsistentTreeMap}.
     *
     * @param name tree name
     * @param serializer serializer to use for serializing/deserializing map entries
     * @param <V> value type
     * @return distributedTreeMap
     */
    <V> AsyncConsistentTreeMap<V> newAsyncConsistentTreeMap(String name, Serializer serializer);

    /**
     * Creates a new set backed {@code AsyncConsistentMultimap}.
     *
     * @param name multimap name
     * @param serializer serializer to use for serializing/deserializing
     * @param <K> key type
     * @param <V> value type
     * @return set backed distributedMultimap
     */
    <K, V> AsyncConsistentMultimap<K, V> newAsyncConsistentSetMultimap(String name, Serializer serializer);

    /**
     * Creates a new {@code AsyncAtomicCounterMap}.
     *
     * @param name counter map name
     * @param serializer serializer to use for serializing/deserializing keys
     * @param <K> key type
     * @return atomic counter map
     */
    <K> AsyncAtomicCounterMap<K> newAsyncAtomicCounterMap(String name, Serializer serializer);

    /**
     * Creates a new {@code AsyncAtomicCounter}.
     *
     * @param name counter name
     * @return counter
     */
    AsyncAtomicCounter newAsyncCounter(String name);

    /**
     * Creates a new {@code AsyncAtomixIdGenerator}.
     *
     * @param name ID generator name
     * @return ID generator
     */
    AsyncAtomicIdGenerator newAsyncIdGenerator(String name);

    /**
     * Creates a new {@code AsyncAtomicValue}.
     *
     * @param name value name
     * @param serializer serializer to use for serializing/deserializing value type
     * @param <V> value type
     * @return value
     */
    <V> AsyncAtomicValue<V> newAsyncAtomicValue(String name, Serializer serializer);

    /**
     * Creates a new {@code AsyncDistributedSet}.
     *
     * @param name set name
     * @param serializer serializer to use for serializing/deserializing set entries
     * @param <E> set entry type
     * @return set
     */
    <E> AsyncDistributedSet<E> newAsyncDistributedSet(String name, Serializer serializer);

    /**
     * Creates a new {@code AsyncLeaderElector}.
     *
     * @param name leader elector name
     * @return leader elector
     */
    default AsyncLeaderElector newAsyncLeaderElector(String name) {
        return newAsyncLeaderElector(name, DEFAULT_OPERATION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a new {@code AsyncLeaderElector}.
     *
     * @param name leader elector name
     * @param electionTimeout leader election timeout
     * @param timeUnit leader election timeout time unit
     * @return leader elector
     */
    AsyncLeaderElector newAsyncLeaderElector(String name, long electionTimeout, TimeUnit timeUnit);

    /**
     * Creates a new {@code WorkQueue}.
     *
     * @param <E> work element type
     * @param name work queue name
     * @param serializer serializer
     * @return work queue
     */
    <E> WorkQueue<E> newWorkQueue(String name, Serializer serializer);

    /**
     * Creates a new {@code AsyncDocumentTree}.
     *
     * @param <V> document tree node value type
     * @param name tree name
     * @param serializer serializer
     * @return document tree
     */
    default <V> AsyncDocumentTree<V> newAsyncDocumentTree(String name, Serializer serializer) {
        return newAsyncDocumentTree(name, serializer, Ordering.NATURAL);
    }

    /**
     * Creates a new {@code AsyncDocumentTree}.
     *
     * @param <V> document tree node value type
     * @param name tree name
     * @param serializer serializer
     * @param ordering tree node ordering
     * @return document tree
     */
    <V> AsyncDocumentTree<V> newAsyncDocumentTree(String name, Serializer serializer, Ordering ordering);

    /**
     * Returns the names of all created {@code AsyncConsistentMap} instances.
     * @return set of {@code AsyncConsistentMap} names
     */
    Set<String> getAsyncConsistentMapNames();

    /**
     * Returns the names of all created {@code AsyncAtomicCounter} instances.
     * @return set of {@code AsyncAtomicCounter} names
     */
    Set<String> getAsyncAtomicCounterNames();

    /**
     * Returns the names of all created {@code WorkQueue} instances.
     * @return set of {@code WorkQueue} names
     */
    Set<String> getWorkQueueNames();
}
