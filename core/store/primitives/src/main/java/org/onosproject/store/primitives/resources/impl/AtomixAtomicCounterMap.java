/*
 * Copyright 2017-present Open Networking Foundation
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

import java.util.concurrent.CompletableFuture;

import io.atomix.protocols.raft.proxy.RaftProxy;
import org.onlab.util.KryoNamespace;
import org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.AddAndGet;
import org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.DecrementAndGet;
import org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.Get;
import org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.GetAndAdd;
import org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.GetAndDecrement;
import org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.GetAndIncrement;
import org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.IncrementAndGet;
import org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.Put;
import org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.PutIfAbsent;
import org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.Remove;
import org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.RemoveValue;
import org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.Replace;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.AsyncAtomicCounterMap;
import org.onosproject.store.service.Serializer;

import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.ADD_AND_GET;
import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.CLEAR;
import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.DECREMENT_AND_GET;
import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.GET;
import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.GET_AND_ADD;
import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.GET_AND_DECREMENT;
import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.GET_AND_INCREMENT;
import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.INCREMENT_AND_GET;
import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.IS_EMPTY;
import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.PUT;
import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.PUT_IF_ABSENT;
import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.REMOVE;
import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.REMOVE_VALUE;
import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.REPLACE;
import static org.onosproject.store.primitives.resources.impl.AtomixAtomicCounterMapOperations.SIZE;

/**
 * {@code AsyncAtomicCounterMap} implementation backed by Atomix.
 */
public class AtomixAtomicCounterMap extends AbstractRaftPrimitive implements AsyncAtomicCounterMap<String> {
    private static final Serializer SERIALIZER = Serializer.using(KryoNamespace.newBuilder()
            .register(KryoNamespaces.BASIC)
            .register(AtomixAtomicCounterMapOperations.NAMESPACE)
            .build());

    public AtomixAtomicCounterMap(RaftProxy proxy) {
        super(proxy);
    }

    @Override
    public CompletableFuture<Long> incrementAndGet(String key) {
        return proxy.invoke(INCREMENT_AND_GET, SERIALIZER::encode, new IncrementAndGet(key), SERIALIZER::decode);
    }

    @Override
    public CompletableFuture<Long> decrementAndGet(String key) {
        return proxy.invoke(DECREMENT_AND_GET, SERIALIZER::encode, new DecrementAndGet(key), SERIALIZER::decode);
    }

    @Override
    public CompletableFuture<Long> getAndIncrement(String key) {
        return proxy.invoke(GET_AND_INCREMENT, SERIALIZER::encode, new GetAndIncrement(key), SERIALIZER::decode);
    }

    @Override
    public CompletableFuture<Long> getAndDecrement(String key) {
        return proxy.invoke(GET_AND_DECREMENT, SERIALIZER::encode, new GetAndDecrement(key), SERIALIZER::decode);
    }

    @Override
    public CompletableFuture<Long> addAndGet(String key, long delta) {
        return proxy.invoke(ADD_AND_GET, SERIALIZER::encode, new AddAndGet(key, delta), SERIALIZER::decode);
    }

    @Override
    public CompletableFuture<Long> getAndAdd(String key, long delta) {
        return proxy.invoke(GET_AND_ADD, SERIALIZER::encode, new GetAndAdd(key, delta), SERIALIZER::decode);
    }

    @Override
    public CompletableFuture<Long> get(String key) {
        return proxy.invoke(GET, SERIALIZER::encode, new Get(key), SERIALIZER::decode);
    }

    @Override
    public CompletableFuture<Long> put(String key, long newValue) {
        return proxy.invoke(PUT, SERIALIZER::encode, new Put(key, newValue), SERIALIZER::decode);
    }

    @Override
    public CompletableFuture<Long> putIfAbsent(String key, long newValue) {
        return proxy.invoke(PUT_IF_ABSENT, SERIALIZER::encode, new PutIfAbsent(key, newValue), SERIALIZER::decode);
    }

    @Override
    public CompletableFuture<Boolean> replace(String key, long expectedOldValue, long newValue) {
        return proxy.invoke(
                REPLACE,
                SERIALIZER::encode,
                new Replace(key, expectedOldValue, newValue),
                SERIALIZER::decode);
    }

    @Override
    public CompletableFuture<Long> remove(String key) {
        return proxy.invoke(REMOVE, SERIALIZER::encode, new Remove(key), SERIALIZER::decode);
    }

    @Override
    public CompletableFuture<Boolean> remove(String key, long value) {
        return proxy.invoke(REMOVE_VALUE, SERIALIZER::encode, new RemoveValue(key, value), SERIALIZER::decode);
    }

    @Override
    public CompletableFuture<Integer> size() {
        return proxy.invoke(SIZE, SERIALIZER::decode);
    }

    @Override
    public CompletableFuture<Boolean> isEmpty() {
        return proxy.invoke(IS_EMPTY, SERIALIZER::decode);
    }

    @Override
    public CompletableFuture<Void> clear() {
        return proxy.invoke(CLEAR);
    }
}