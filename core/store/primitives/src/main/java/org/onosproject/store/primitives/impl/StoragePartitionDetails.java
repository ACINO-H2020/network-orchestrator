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

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.atomix.protocols.raft.cluster.RaftMember;
import org.onosproject.cluster.PartitionId;
import org.onosproject.store.service.PartitionInfo;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

/**
 * Operational details for a {@code StoragePartition}.
 */
public class StoragePartitionDetails {

    private final PartitionId partitionId;
    private final Set<RaftMember> activeMembers;
    private final Set<RaftMember> configuredMembers;
    private final RaftMember leader;
    private final long leaderTerm;

    public StoragePartitionDetails(PartitionId partitionId,
            Collection<RaftMember> activeMembers,
            Collection<RaftMember> configuredMembers,
            RaftMember leader,
            long leaderTerm) {
        this.partitionId = partitionId;
        this.activeMembers = ImmutableSet.copyOf(activeMembers);
        this.configuredMembers = ImmutableSet.copyOf(configuredMembers);
        this.leader = leader;
        this.leaderTerm = leaderTerm;
    }

    /**
     * Returns the set of active members.
     * @return active members
     */
    public Set<RaftMember> activeMembers() {
        return activeMembers;
    }

    /**
     * Returns the set of configured members.
     * @return configured members
     */
    public Set<RaftMember> configuredMembers() {
        return configuredMembers;
    }

    /**
     * Returns the partition leader.
     * @return leader
     */
    public RaftMember leader() {
        return leader;
    }

    /**
     * Returns the partition leader term.
     * @return leader term
     */
    public long leaderTerm() {
        return leaderTerm;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("activeMembers", activeMembers)
                .add("configuredMembers", configuredMembers)
                .add("leader", leader)
                .add("leaderTerm", leaderTerm)
                .toString();
    }

    /**
     * Returns the details as an instance of {@code PartitionInfo}.
     * @return partition info
     */
    public PartitionInfo toPartitionInfo() {
        Function<RaftMember, String> memberToString =
                m -> m == null ? "none" : m.memberId().toString();
        return new PartitionInfo(partitionId,
                leaderTerm,
                activeMembers.stream().map(memberToString).collect(Collectors.toList()),
                memberToString.apply(leader));
    }
}
