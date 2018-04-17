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

import java.util.Objects;

import io.atomix.protocols.raft.service.AbstractRaftService;
import io.atomix.protocols.raft.service.Commit;
import io.atomix.protocols.raft.service.RaftServiceExecutor;
import io.atomix.protocols.raft.storage.snapshot.SnapshotReader;
import io.atomix.protocols.raft.storage.snapshot.SnapshotWriter;
import org.onlab.util.KryoNamespace;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.Serializer;

import static org.onosproject.store.primitives.resources.impl.AtomixCounterOperations.ADD_AND_GET;
import static org.onosproject.store.primitives.resources.impl.AtomixCounterOperations.AddAndGet;
import static org.onosproject.store.primitives.resources.impl.AtomixCounterOperations.COMPARE_AND_SET;
import static org.onosproject.store.primitives.resources.impl.AtomixCounterOperations.CompareAndSet;
import static org.onosproject.store.primitives.resources.impl.AtomixCounterOperations.GET;
import static org.onosproject.store.primitives.resources.impl.AtomixCounterOperations.GET_AND_ADD;
import static org.onosproject.store.primitives.resources.impl.AtomixCounterOperations.GET_AND_INCREMENT;
import static org.onosproject.store.primitives.resources.impl.AtomixCounterOperations.GetAndAdd;
import static org.onosproject.store.primitives.resources.impl.AtomixCounterOperations.INCREMENT_AND_GET;
import static org.onosproject.store.primitives.resources.impl.AtomixCounterOperations.SET;
import static org.onosproject.store.primitives.resources.impl.AtomixCounterOperations.Set;

/**
 * Atomix long state.
 */
public class AtomixCounterService extends AbstractRaftService {
    private static final Serializer SERIALIZER = Serializer.using(KryoNamespace.newBuilder()
            .register(KryoNamespaces.BASIC)
            .register(AtomixCounterOperations.NAMESPACE)
            .build());

    private Long value = 0L;

    @Override
    protected void configure(RaftServiceExecutor executor) {
        executor.register(SET, SERIALIZER::decode, this::set);
        executor.register(GET, this::get, SERIALIZER::encode);
        executor.register(COMPARE_AND_SET, SERIALIZER::decode, this::compareAndSet, SERIALIZER::encode);
        executor.register(INCREMENT_AND_GET, this::incrementAndGet, SERIALIZER::encode);
        executor.register(GET_AND_INCREMENT, this::getAndIncrement, SERIALIZER::encode);
        executor.register(ADD_AND_GET, SERIALIZER::decode, this::addAndGet, SERIALIZER::encode);
        executor.register(GET_AND_ADD, SERIALIZER::decode, this::getAndAdd, SERIALIZER::encode);
    }

    @Override
    public void snapshot(SnapshotWriter writer) {
        writer.writeLong(value);
    }

    @Override
    public void install(SnapshotReader reader) {
        value = reader.readLong();
    }

    /**
     * Handles a set commit.
     *
     * @param commit the commit to handle
     */
    protected void set(Commit<Set> commit) {
        value = commit.value().value();
    }

    /**
     * Handles a get commit.
     *
     * @param commit the commit to handle
     * @return counter value
     */
    protected Long get(Commit<Void> commit) {
        return value;
    }

    /**
     * Handles a compare and set commit.
     *
     * @param commit the commit to handle
     * @return counter value
     */
    protected boolean compareAndSet(Commit<CompareAndSet> commit) {
        if (Objects.equals(value, commit.value().expect())) {
            value = commit.value().update();
            return true;
        }
        return false;
    }

    /**
     * Handles an increment and get commit.
     *
     * @param commit the commit to handle
     * @return counter value
     */
    protected long incrementAndGet(Commit<Void> commit) {
        Long oldValue = value;
        value = oldValue + 1;
        return value;
    }

    /**
     * Handles a get and increment commit.
     *
     * @param commit the commit to handle
     * @return counter value
     */
    protected long getAndIncrement(Commit<Void> commit) {
        Long oldValue = value;
        value = oldValue + 1;
        return oldValue;
    }

    /**
     * Handles an add and get commit.
     *
     * @param commit the commit to handle
     * @return counter value
     */
    protected long addAndGet(Commit<AddAndGet> commit) {
        Long oldValue = value;
        value = oldValue + commit.value().delta();
        return value;
    }

    /**
     * Handles a get and add commit.
     *
     * @param commit the commit to handle
     * @return counter value
     */
    protected long getAndAdd(Commit<GetAndAdd> commit) {
        Long oldValue = value;
        value = oldValue + commit.value().delta();
        return oldValue;
    }
}