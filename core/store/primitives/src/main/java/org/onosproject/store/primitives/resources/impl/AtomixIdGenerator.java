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
import java.util.concurrent.atomic.AtomicLong;

import org.onosproject.store.service.AsyncAtomicCounter;
import org.onosproject.store.service.AsyncAtomicIdGenerator;

/**
 * {@code AsyncAtomicIdGenerator} implementation backed by Atomix
 * {@link AsyncAtomicCounter}.
 */
public class AtomixIdGenerator implements AsyncAtomicIdGenerator {

    private static final long DEFAULT_BATCH_SIZE = 1000;
    private final AsyncAtomicCounter counter;
    private final long batchSize;
    private CompletableFuture<Long> reserveFuture;
    private long base;
    private final AtomicLong delta = new AtomicLong();

    public AtomixIdGenerator(AsyncAtomicCounter counter) {
        this(counter, DEFAULT_BATCH_SIZE);
    }

    AtomixIdGenerator(AsyncAtomicCounter counter, long batchSize) {
        this.counter = counter;
        this.batchSize = batchSize;
    }

    @Override
    public String name() {
        return counter.name();
    }

    @Override
    public synchronized CompletableFuture<Long> nextId() {
        long nextDelta = delta.incrementAndGet();
        if ((base == 0 && reserveFuture == null) || nextDelta > batchSize) {
            delta.set(0);
            long delta = this.delta.incrementAndGet();
            return reserve().thenApply(base -> base + delta);
        } else {
            return reserveFuture.thenApply(base -> base + nextDelta);
        }
    }

    private CompletableFuture<Long> reserve() {
        if (reserveFuture == null || reserveFuture.isDone()) {
            reserveFuture = counter.getAndAdd(batchSize);
        } else {
            reserveFuture = reserveFuture.thenCompose(v -> counter.getAndAdd(batchSize));
        }
        reserveFuture = reserveFuture.thenApply(base -> {
            this.base = base;
            return base;
        });
        return reserveFuture;
    }
}