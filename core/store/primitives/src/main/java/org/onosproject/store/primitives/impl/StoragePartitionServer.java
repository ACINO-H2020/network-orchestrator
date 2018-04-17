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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.atomix.protocols.raft.RaftServer;
import io.atomix.protocols.raft.cluster.MemberId;
import io.atomix.protocols.raft.storage.RaftStorage;
import io.atomix.storage.StorageLevel;
import org.onosproject.cluster.Partition;
import org.onosproject.store.cluster.messaging.ClusterCommunicationService;
import org.onosproject.store.primitives.resources.impl.AtomixSerializerAdapter;
import org.onosproject.store.service.PartitionInfo;
import org.onosproject.store.service.Serializer;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * {@link StoragePartition} server.
 */
public class StoragePartitionServer implements Managed<StoragePartitionServer> {

    private final Logger log = getLogger(getClass());

    private static final int MAX_SEGMENT_SIZE = 1024 * 1024 * 64;
    private static final long ELECTION_TIMEOUT_MILLIS = 2500;
    private static final long HEARTBEAT_INTERVAL_MILLIS = 250;

    private final MemberId localMemberId;
    private final StoragePartition partition;
    private final ClusterCommunicationService clusterCommunicator;
    private RaftServer server;

    public StoragePartitionServer(
            StoragePartition partition,
            MemberId localMemberId,
            ClusterCommunicationService clusterCommunicator) {
        this.partition = partition;
        this.localMemberId = localMemberId;
        this.clusterCommunicator = clusterCommunicator;
    }

    @Override
    public CompletableFuture<Void> open() {
        log.info("Starting server for partition {} ({})", partition.getId(), partition.getVersion());
        CompletableFuture<RaftServer> serverOpenFuture;
        if (partition.getMemberIds().contains(localMemberId)) {
            if (server != null && server.isRunning()) {
                return CompletableFuture.completedFuture(null);
            }
            synchronized (this) {
                server = buildServer();
            }
            serverOpenFuture = server.bootstrap(partition.getMemberIds());
        } else {
            serverOpenFuture = CompletableFuture.completedFuture(null);
        }
        return serverOpenFuture.whenComplete((r, e) -> {
            if (e == null) {
                log.info("Successfully started server for partition {} ({})",
                        partition.getId(), partition.getVersion());
            } else {
                log.info("Failed to start server for partition {} ({})",
                        partition.getId(), partition.getVersion(), e);
            }
        }).thenApply(v -> null);
    }

    @Override
    public CompletableFuture<Void> close() {
        return server.shutdown();
    }

    /**
     * Closes the server and exits the partition.
     * @return future that is completed when the operation is complete
     */
    public CompletableFuture<Void> closeAndExit() {
        return server.leave();
    }

    /**
     * Deletes the server.
     */
    public void delete() {
        try {
            Files.walkFileTree(partition.getDataFolder().toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Failed to delete partition: {}", e);
        }
    }

    /**
     * Forks the existing partition into a new partition.
     *
     * @param fromPartition the partition from which to fork the server
     * @return future to be completed once the fork operation is complete
     */
    public CompletableFuture<Void> fork(Partition fromPartition) {
        log.info("Forking server for partition {} ({}->{})",
                partition.getId(), fromPartition.getVersion(), partition.getVersion());
        RaftServer.Builder builder = RaftServer.newBuilder(localMemberId)
                .withName(String.format("partition-%s", fromPartition.getId()))
                .withProtocol(new RaftServerCommunicator(
                        String.format("partition-%s-%s", fromPartition.getId(), fromPartition.getVersion()),
                        Serializer.using(StorageNamespaces.RAFT_PROTOCOL),
                        clusterCommunicator))
                .withElectionTimeout(Duration.ofMillis(ELECTION_TIMEOUT_MILLIS))
                .withHeartbeatInterval(Duration.ofMillis(HEARTBEAT_INTERVAL_MILLIS))
                .withStorage(RaftStorage.newBuilder()
                        .withPrefix(String.format("partition-%s", partition.getId()))
                        .withStorageLevel(StorageLevel.MAPPED)
                        .withSerializer(new AtomixSerializerAdapter(Serializer.using(StorageNamespaces.RAFT_STORAGE)))
                        .withDirectory(partition.getDataFolder())
                        .withMaxSegmentSize(MAX_SEGMENT_SIZE)
                        .build());
        StoragePartition.RAFT_SERVICES.forEach(builder::addService);
        RaftServer server = builder.build();

        // Create a collection of members currently in the source partition.
        Collection<MemberId> members = fromPartition.getMembers()
                .stream()
                .map(id -> MemberId.from(id.id()))
                .collect(Collectors.toList());

        // If this node is a member of the partition, join the partition. Otherwise, listen to the partition.
        CompletableFuture<RaftServer> future = members.contains(localMemberId)
                ? server.bootstrap(members) : server.listen(members);

        // TODO: We should leave the cluster for nodes that aren't normally members to ensure the source
        // cluster's configuration is kept consistent for rolling back upgrades, but Atomix deletes configuration
        // files when a node leaves the cluster so we can't do that here.
        return future.thenCompose(v -> server.shutdown())
                .thenCompose(v -> {
                    // Delete the cluster configuration file from the forked partition.
                    try {
                        Files.delete(new File(
                                partition.getDataFolder(),
                                String.format("partition-%s.conf", partition.getId())).toPath());
                    } catch (IOException e) {
                        log.error("Failed to delete partition configuration: {}", e);
                    }

                    // Build and bootstrap a new server.
                    this.server = buildServer();
                    return this.server.bootstrap();
                }).whenComplete((r, e) -> {
                    if (e == null) {
                        log.info("Successfully forked server for partition {} ({}->{})",
                                partition.getId(), fromPartition.getVersion(), partition.getVersion());
                    } else {
                        log.info("Failed to fork server for partition {} ({}->{})",
                                partition.getId(), fromPartition.getVersion(), partition.getVersion(), e);
                    }
                }).thenApply(v -> null);
    }

    private RaftServer buildServer() {
        RaftServer.Builder builder = RaftServer.newBuilder(localMemberId)
                .withName(String.format("partition-%s", partition.getId()))
                .withProtocol(new RaftServerCommunicator(
                        String.format("partition-%s-%s", partition.getId(), partition.getVersion()),
                        Serializer.using(StorageNamespaces.RAFT_PROTOCOL),
                        clusterCommunicator))
                .withElectionTimeout(Duration.ofMillis(ELECTION_TIMEOUT_MILLIS))
                .withHeartbeatInterval(Duration.ofMillis(HEARTBEAT_INTERVAL_MILLIS))
                .withStorage(RaftStorage.newBuilder()
                        .withPrefix(String.format("partition-%s", partition.getId()))
                        .withStorageLevel(StorageLevel.MAPPED)
                        .withSerializer(new AtomixSerializerAdapter(Serializer.using(StorageNamespaces.RAFT_STORAGE)))
                        .withDirectory(partition.getDataFolder())
                        .withMaxSegmentSize(MAX_SEGMENT_SIZE)
                        .build());
        StoragePartition.RAFT_SERVICES.forEach(builder::addService);
        return builder.build();
    }

    public CompletableFuture<Void> join(Collection<MemberId> otherMembers) {
        log.info("Joining partition {} ({})", partition.getId(), partition.getName());
        server = buildServer();
        return server.join(otherMembers).whenComplete((r, e) -> {
            if (e == null) {
                log.info("Successfully joined partition {} ({})", partition.getId(), partition.getName());
            } else {
                log.info("Failed to join partition {} ({})", partition.getId(), partition.getName(), e);
            }
        }).thenApply(v -> null);
    }

    @Override
    public boolean isOpen() {
        return server.isRunning();
    }

    /**
     * Returns the partition information.
     * @return partition info
     */
    public PartitionInfo info() {
        return new StoragePartitionDetails(partition.getId(),
                server.cluster().getMembers(),
                server.cluster().getMembers(),
                server.cluster().getLeader(),
                server.cluster().getTerm()).toPartitionInfo();
    }
}
