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
package org.onosproject.store.service;

import java.util.concurrent.TimeUnit;

import org.onosproject.store.primitives.DistributedPrimitiveBuilder;

/**
 * Builder for constructing new {@link AsyncLeaderElector} instances.
 */
public abstract class LeaderElectorBuilder
    extends DistributedPrimitiveBuilder<LeaderElectorBuilder, AsyncLeaderElector> {

    private long electionTimeoutMillis = DistributedPrimitive.DEFAULT_OPERATION_TIMEOUT_MILLIS;

    public LeaderElectorBuilder() {
        super(DistributedPrimitive.Type.LEADER_ELECTOR);
    }

    /**
     * Sets the election timeout in milliseconds.
     *
     * @param electionTimeoutMillis the election timeout in milliseconds
     * @return leader elector builder
     */
    public LeaderElectorBuilder withElectionTimeout(long electionTimeoutMillis) {
        this.electionTimeoutMillis = electionTimeoutMillis;
        return this;
    }

    /**
     * Sets the election timeout.
     *
     * @param electionTimeout the election timeout
     * @param timeUnit the timeout time unit
     * @return leader elector builder
     */
    public LeaderElectorBuilder withElectionTimeout(long electionTimeout, TimeUnit timeUnit) {
        return withElectionTimeout(timeUnit.toMillis(electionTimeout));
    }

    /**
     * Returns the election timeout in milliseconds.
     *
     * @return the election timeout in milliseconds
     */
    public final long electionTimeoutMillis() {
        return electionTimeoutMillis;
    }
}
