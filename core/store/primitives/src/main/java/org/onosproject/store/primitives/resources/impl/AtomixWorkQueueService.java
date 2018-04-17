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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import io.atomix.protocols.raft.service.AbstractRaftService;
import io.atomix.protocols.raft.service.Commit;
import io.atomix.protocols.raft.service.RaftServiceExecutor;
import io.atomix.protocols.raft.session.RaftSession;
import io.atomix.protocols.raft.storage.snapshot.SnapshotReader;
import io.atomix.protocols.raft.storage.snapshot.SnapshotWriter;
import org.onlab.util.KryoNamespace;
import org.onosproject.store.primitives.resources.impl.AtomixWorkQueueOperations.Add;
import org.onosproject.store.primitives.resources.impl.AtomixWorkQueueOperations.Complete;
import org.onosproject.store.primitives.resources.impl.AtomixWorkQueueOperations.Take;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.Task;
import org.onosproject.store.service.WorkQueueStats;

import static org.onosproject.store.primitives.resources.impl.AtomixWorkQueueEvents.TASK_AVAILABLE;
import static org.onosproject.store.primitives.resources.impl.AtomixWorkQueueOperations.ADD;
import static org.onosproject.store.primitives.resources.impl.AtomixWorkQueueOperations.CLEAR;
import static org.onosproject.store.primitives.resources.impl.AtomixWorkQueueOperations.COMPLETE;
import static org.onosproject.store.primitives.resources.impl.AtomixWorkQueueOperations.REGISTER;
import static org.onosproject.store.primitives.resources.impl.AtomixWorkQueueOperations.STATS;
import static org.onosproject.store.primitives.resources.impl.AtomixWorkQueueOperations.TAKE;
import static org.onosproject.store.primitives.resources.impl.AtomixWorkQueueOperations.UNREGISTER;

/**
 * State machine for {@link AtomixWorkQueue} resource.
 */
public class AtomixWorkQueueService extends AbstractRaftService {

    private static final Serializer SERIALIZER = Serializer.using(KryoNamespace.newBuilder()
            .register(KryoNamespaces.BASIC)
            .register(AtomixWorkQueueOperations.NAMESPACE)
            .register(AtomixWorkQueueEvents.NAMESPACE)
            .register(TaskAssignment.class)
            .register(new HashMap().keySet().getClass())
            .register(ArrayDeque.class)
            .build());

    private final AtomicLong totalCompleted = new AtomicLong(0);

    private Queue<Task<byte[]>> unassignedTasks = Queues.newArrayDeque();
    private Map<String, TaskAssignment> assignments = Maps.newHashMap();
    private Map<Long, RaftSession> registeredWorkers = Maps.newHashMap();

    @Override
    public void snapshot(SnapshotWriter writer) {
        writer.writeObject(Sets.newHashSet(registeredWorkers.keySet()), SERIALIZER::encode);
        writer.writeObject(assignments, SERIALIZER::encode);
        writer.writeObject(unassignedTasks, SERIALIZER::encode);
        writer.writeLong(totalCompleted.get());
    }

    @Override
    public void install(SnapshotReader reader) {
        registeredWorkers = Maps.newHashMap();
        for (Long sessionId : reader.<Set<Long>>readObject(SERIALIZER::decode)) {
            registeredWorkers.put(sessionId, sessions().getSession(sessionId));
        }
        assignments = reader.readObject(SERIALIZER::decode);
        unassignedTasks = reader.readObject(SERIALIZER::decode);
        totalCompleted.set(reader.readLong());
    }

    @Override
    protected void configure(RaftServiceExecutor executor) {
        executor.register(STATS, this::stats, SERIALIZER::encode);
        executor.register(REGISTER, this::register);
        executor.register(UNREGISTER, this::unregister);
        executor.register(ADD, SERIALIZER::decode, this::add);
        executor.register(TAKE, SERIALIZER::decode, this::take, SERIALIZER::encode);
        executor.register(COMPLETE, SERIALIZER::decode, this::complete);
        executor.register(CLEAR, this::clear);
    }

    protected WorkQueueStats stats(Commit<Void> commit) {
        return WorkQueueStats.builder()
                .withTotalCompleted(totalCompleted.get())
                .withTotalPending(unassignedTasks.size())
                .withTotalInProgress(assignments.size())
                .build();
    }

    protected void clear(Commit<Void> commit) {
        unassignedTasks.clear();
        assignments.clear();
        registeredWorkers.clear();
        totalCompleted.set(0);
    }

    protected void register(Commit<Void> commit) {
        registeredWorkers.put(commit.session().sessionId().id(), commit.session());
    }

    protected void unregister(Commit<Void> commit) {
        registeredWorkers.remove(commit.session().sessionId().id());
    }

    protected void add(Commit<? extends Add> commit) {
        Collection<byte[]> items = commit.value().items();

        AtomicInteger itemIndex = new AtomicInteger(0);
        items.forEach(item -> {
            String taskId = String.format("%d:%d:%d", commit.session().sessionId().id(),
                    commit.index(),
                    itemIndex.getAndIncrement());
            unassignedTasks.add(new Task<>(taskId, item));
        });

        // Send an event to all sessions that have expressed interest in task processing
        // and are not actively processing a task.
        registeredWorkers.values().forEach(session -> session.publish(TASK_AVAILABLE));
        // FIXME: This generates a lot of event traffic.
    }

    protected Collection<Task<byte[]>> take(Commit<? extends Take> commit) {
        try {
            if (unassignedTasks.isEmpty()) {
                return ImmutableList.of();
            }
            long sessionId = commit.session().sessionId().id();
            int maxTasks = commit.value().maxTasks();
            return IntStream.range(0, Math.min(maxTasks, unassignedTasks.size()))
                    .mapToObj(i -> {
                        Task<byte[]> task = unassignedTasks.poll();
                        String taskId = task.taskId();
                        TaskAssignment assignment = new TaskAssignment(sessionId, task);

                        // bookkeeping
                        assignments.put(taskId, assignment);

                        return task;
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (Exception e) {
            logger().warn("State machine update failed", e);
            throw Throwables.propagate(e);
        }
    }

    protected void complete(Commit<? extends Complete> commit) {
        long sessionId = commit.session().sessionId().id();
        try {
            commit.value().taskIds().forEach(taskId -> {
                TaskAssignment assignment = assignments.get(taskId);
                if (assignment != null && assignment.sessionId() == sessionId) {
                    assignments.remove(taskId);
                    // bookkeeping
                    totalCompleted.incrementAndGet();
                }
            });
        } catch (Exception e) {
            logger().warn("State machine update failed", e);
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void onExpire(RaftSession session) {
        evictWorker(session.sessionId().id());
    }

    @Override
    public void onClose(RaftSession session) {
        evictWorker(session.sessionId().id());
    }

    private void evictWorker(long sessionId) {
        registeredWorkers.remove(sessionId);

        // TODO: Maintain an index of tasks by session for efficient access.
        Iterator<Map.Entry<String, TaskAssignment>> iter = assignments.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, TaskAssignment> entry = iter.next();
            TaskAssignment assignment = entry.getValue();
            if (assignment.sessionId() == sessionId) {
                unassignedTasks.add(assignment.task());
                iter.remove();
            }
        }
    }

    private static class TaskAssignment {
        private final long sessionId;
        private final Task<byte[]> task;

        public TaskAssignment(long sessionId, Task<byte[]> task) {
            this.sessionId = sessionId;
            this.task = task;
        }

        public long sessionId() {
            return sessionId;
        }

        public Task<byte[]> task() {
            return task;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(getClass())
                    .add("sessionId", sessionId)
                    .add("task", task)
                    .toString();
        }
    }
}