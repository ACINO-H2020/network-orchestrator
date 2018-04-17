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
package org.onosproject.store.primitives.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import org.onlab.util.Tools;
import org.onosproject.cluster.PartitionId;
import org.onosproject.store.primitives.NodeUpdate;
import org.onosproject.store.primitives.TransactionId;
import org.onosproject.store.service.AsyncDocumentTree;
import org.onosproject.store.service.DocumentPath;
import org.onosproject.store.service.DocumentTreeListener;
import org.onosproject.store.service.NoSuchDocumentPathException;
import org.onosproject.store.service.TransactionLog;
import org.onosproject.store.service.Version;
import org.onosproject.store.service.Versioned;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Partitioned asynchronous document tree.
 */
public class PartitionedAsyncDocumentTree<V> implements AsyncDocumentTree<V> {

    private final String name;
    private final TreeMap<PartitionId, AsyncDocumentTree<V>> partitions = Maps.newTreeMap();
    private final Hasher<DocumentPath> pathHasher;

    public PartitionedAsyncDocumentTree(
            String name,
            Map<PartitionId, AsyncDocumentTree<V>> partitions,
            Hasher<DocumentPath> pathHasher) {
        this.name = name;
        this.partitions.putAll(checkNotNull(partitions));
        this.pathHasher = checkNotNull(pathHasher);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public DocumentPath root() {
        return DocumentPath.ROOT;
    }

    /**
     * Returns the document tree (partition) to which the specified path maps.
     *
     * @param path path
     * @return AsyncConsistentMap to which path maps
     */
    private AsyncDocumentTree<V> partition(DocumentPath path) {
        return partitions.get(pathHasher.hash(path));
    }

    /**
     * Returns all the constituent trees.
     *
     * @return collection of partitions.
     */
    private Collection<AsyncDocumentTree<V>> partitions() {
        return partitions.values();
    }

    @Override
    public CompletableFuture<Map<String, Versioned<V>>> getChildren(DocumentPath path) {
        return Tools.allOf(partitions().stream()
                .map(partition -> partition.getChildren(path).exceptionally(r -> null))
                .collect(Collectors.toList())).thenApply(allChildren -> {
            Map<String, Versioned<V>> children = Maps.newLinkedHashMap();
            allChildren.stream().filter(Objects::nonNull).forEach(children::putAll);
            return children;
        });
    }

    @Override
    public CompletableFuture<Versioned<V>> get(DocumentPath path) {
        return partition(path).get(path);
    }

    @Override
    public CompletableFuture<Versioned<V>> set(DocumentPath path, V value) {
        return partition(path).set(path, value);
    }

    @Override
    public CompletableFuture<Boolean> create(DocumentPath path, V value) {
        if (path.parent() == null) {
            // create value on root
            return partition(path).createRecursive(path, value);
        }
        // TODO: This operation is not atomic
        return partition(path.parent()).get(path.parent()).thenCompose(parentValue -> {
            if (parentValue == null) {
                return Tools.exceptionalFuture(new NoSuchDocumentPathException(String.valueOf(path.parent())));
            } else {
                // not atomic: parent did exist at some point, so moving forward
                return partition(path).createRecursive(path, value);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> createRecursive(DocumentPath path, V value) {
        return partition(path).createRecursive(path, value);
    }

    @Override
    public CompletableFuture<Boolean> replace(DocumentPath path, V newValue, long version) {
        return partition(path).replace(path, newValue, version);
    }

    @Override
    public CompletableFuture<Boolean> replace(DocumentPath path, V newValue, V currentValue) {
        return partition(path).replace(path, newValue, currentValue);
    }

    @Override
    public CompletableFuture<Versioned<V>> removeNode(DocumentPath path) {
        return partition(path).removeNode(path);
    }

    @Override
    public CompletableFuture<Void> addListener(DocumentPath path, DocumentTreeListener<V> listener) {
        return CompletableFuture.allOf(partitions().stream()
                .map(map -> map.addListener(path, listener))
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Void> removeListener(DocumentTreeListener<V> listener) {
        return CompletableFuture.allOf(partitions().stream()
                .map(map -> map.removeListener(listener))
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Version> begin(TransactionId transactionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Boolean> prepare(TransactionLog<NodeUpdate<V>> transactionLog) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Boolean> prepareAndCommit(TransactionLog<NodeUpdate<V>> transactionLog) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Void> commit(TransactionId transactionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Void> rollback(TransactionId transactionId) {
        throw new UnsupportedOperationException();
    }
}
