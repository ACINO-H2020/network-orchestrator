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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.google.common.base.Throwables;
import io.atomix.protocols.raft.proxy.RaftProxy;
import io.atomix.protocols.raft.service.RaftService;
import org.junit.Test;
import org.onosproject.store.primitives.NodeUpdate;
import org.onosproject.store.primitives.TransactionId;
import org.onosproject.store.service.DocumentPath;
import org.onosproject.store.service.DocumentTreeEvent;
import org.onosproject.store.service.DocumentTreeListener;
import org.onosproject.store.service.IllegalDocumentModificationException;
import org.onosproject.store.service.NoSuchDocumentPathException;
import org.onosproject.store.service.Ordering;
import org.onosproject.store.service.TransactionLog;
import org.onosproject.store.service.Version;
import org.onosproject.store.service.Versioned;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link AtomixDocumentTree}.
 */
public class AtomixDocumentTreeTest extends AtomixTestBase<AtomixDocumentTree> {
    private Ordering ordering = Ordering.NATURAL;

    @Override
    protected RaftService createService() {
        return new AtomixDocumentTreeService(ordering);
    }

    @Override
    protected AtomixDocumentTree createPrimitive(RaftProxy proxy) {
        return new AtomixDocumentTree(proxy);
    }

    @Override
    protected AtomixDocumentTree newPrimitive(String name) {
        return newPrimitive(name, Ordering.NATURAL);
    }

    protected AtomixDocumentTree newPrimitive(String name, Ordering ordering) {
        this.ordering = ordering;
        return super.newPrimitive(name);
    }

    /**
     * Tests queries (get and getChildren).
     */
    @Test
    public void testQueries() throws Throwable {
        AtomixDocumentTree tree = newPrimitive(UUID.randomUUID().toString());
        Versioned<byte[]> root = tree.get(path("root")).join();
        assertEquals(1, root.version());
        assertNull(root.value());
    }

    /**
     * Tests create.
     */
    @Test
    public void testCreate() throws Throwable {
        AtomixDocumentTree tree = newPrimitive(UUID.randomUUID().toString());
        tree.create(path("root.a"), "a".getBytes()).join();
        tree.create(path("root.a.b"), "ab".getBytes()).join();
        tree.create(path("root.a.c"), "ac".getBytes()).join();
        Versioned<byte[]> a = tree.get(path("root.a")).join();
        assertArrayEquals("a".getBytes(), a.value());

        Versioned<byte[]> ab = tree.get(path("root.a.b")).join();
        assertArrayEquals("ab".getBytes(), ab.value());

        Versioned<byte[]> ac = tree.get(path("root.a.c")).join();
        assertArrayEquals("ac".getBytes(), ac.value());

        tree.create(path("root.x"), null).join();
        Versioned<byte[]> x = tree.get(path("root.x")).join();
        assertNull(x.value());
    }

    /**
     * Tests recursive create.
     */
    @Test
    public void testRecursiveCreate() throws Throwable {
        AtomixDocumentTree tree = newPrimitive(UUID.randomUUID().toString());
        tree.createRecursive(path("root.a.b.c"), "abc".getBytes()).join();
        Versioned<byte[]> a = tree.get(path("root.a")).join();
        assertArrayEquals(null, a.value());

        Versioned<byte[]> ab = tree.get(path("root.a.b")).join();
        assertArrayEquals(null, ab.value());

        Versioned<byte[]> abc = tree.get(path("root.a.b.c")).join();
        assertArrayEquals("abc".getBytes(), abc.value());
    }

    /**
     * Tests child node order.
     */
    @Test
    public void testOrder() throws Throwable {
        AtomixDocumentTree naturalTree = newPrimitive(UUID.randomUUID().toString(), Ordering.NATURAL);
        naturalTree.create(path("root.c"), "foo".getBytes());
        naturalTree.create(path("root.b"), "bar".getBytes());
        naturalTree.create(path("root.a"), "baz".getBytes());

        Iterator<Map.Entry<String, Versioned<byte[]>>> naturalIterator = naturalTree.getChildren(path("root"))
                .join().entrySet().iterator();
        assertEquals("a", naturalIterator.next().getKey());
        assertEquals("b", naturalIterator.next().getKey());
        assertEquals("c", naturalIterator.next().getKey());

        AtomixDocumentTree insertionTree = newPrimitive(UUID.randomUUID().toString(), Ordering.INSERTION);
        insertionTree.create(path("root.c"), "foo".getBytes());
        insertionTree.create(path("root.b"), "bar".getBytes());
        insertionTree.create(path("root.a"), "baz".getBytes());

        Iterator<Map.Entry<String, Versioned<byte[]>>> insertionIterator = insertionTree.getChildren(path("root"))
                .join().entrySet().iterator();
        assertEquals("c", insertionIterator.next().getKey());
        assertEquals("b", insertionIterator.next().getKey());
        assertEquals("a", insertionIterator.next().getKey());
    }

    /**
     * Tests set.
     */
    @Test
    public void testSet() throws Throwable {
        AtomixDocumentTree tree = newPrimitive(UUID.randomUUID().toString());
        tree.create(path("root.a"), "a".getBytes()).join();
        tree.create(path("root.a.b"), "ab".getBytes()).join();
        tree.create(path("root.a.c"), "ac".getBytes()).join();

        tree.set(path("root.a.d"), "ad".getBytes()).join();
        Versioned<byte[]> ad = tree.get(path("root.a.d")).join();
        assertArrayEquals("ad".getBytes(), ad.value());

        tree.set(path("root.a"), "newA".getBytes()).join();
        Versioned<byte[]> newA = tree.get(path("root.a")).join();
        assertArrayEquals("newA".getBytes(), newA.value());

        tree.set(path("root.a.b"), "newAB".getBytes()).join();
        Versioned<byte[]> newAB = tree.get(path("root.a.b")).join();
        assertArrayEquals("newAB".getBytes(), newAB.value());

        tree.set(path("root.x"), null).join();
        Versioned<byte[]> x = tree.get(path("root.x")).join();
        assertNull(x.value());
    }

    /**
     * Tests replace if version matches.
     */
    @Test
    public void testReplaceVersion() throws Throwable {
        AtomixDocumentTree tree = newPrimitive(UUID.randomUUID().toString());
        tree.create(path("root.a"), "a".getBytes()).join();
        tree.create(path("root.a.b"), "ab".getBytes()).join();
        tree.create(path("root.a.c"), "ac".getBytes()).join();

        Versioned<byte[]> ab = tree.get(path("root.a.b")).join();
        assertTrue(tree.replace(path("root.a.b"), "newAB".getBytes(), ab.version()).join());
        Versioned<byte[]> newAB = tree.get(path("root.a.b")).join();
        assertArrayEquals("newAB".getBytes(), newAB.value());

        assertFalse(tree.replace(path("root.a.b"), "newestAB".getBytes(), ab.version()).join());
        assertArrayEquals("newAB".getBytes(), tree.get(path("root.a.b")).join().value());

        assertFalse(tree.replace(path("root.a.d"), "foo".getBytes(), 1).join());
    }

    /**
     * Tests replace if value matches.
     */
    @Test
    public void testReplaceValue() throws Throwable {
        AtomixDocumentTree tree = newPrimitive(UUID.randomUUID().toString());
        tree.create(path("root.a"), "a".getBytes()).join();
        tree.create(path("root.a.b"), "ab".getBytes()).join();
        tree.create(path("root.a.c"), "ac".getBytes()).join();

        Versioned<byte[]> ab = tree.get(path("root.a.b")).join();
        assertTrue(tree.replace(path("root.a.b"), "newAB".getBytes(), ab.value()).join());
        Versioned<byte[]> newAB = tree.get(path("root.a.b")).join();
        assertArrayEquals("newAB".getBytes(), newAB.value());

        assertFalse(tree.replace(path("root.a.b"), "newestAB".getBytes(), ab.value()).join());
        assertArrayEquals("newAB".getBytes(), tree.get(path("root.a.b")).join().value());

        assertFalse(tree.replace(path("root.a.d"), "bar".getBytes(), "foo".getBytes()).join());
    }

    /**
     * Tests remove.
     */
    @Test
    public void testRemove() throws Throwable {
        AtomixDocumentTree tree = newPrimitive(UUID.randomUUID().toString());
        tree.create(path("root.a"), "a".getBytes()).join();
        tree.create(path("root.a.b"), "ab".getBytes()).join();
        tree.create(path("root.a.c"), "ac".getBytes()).join();

        Versioned<byte[]> ab = tree.removeNode(path("root.a.b")).join();
        assertArrayEquals("ab".getBytes(), ab.value());
        assertNull(tree.get(path("root.a.b")).join());

        Versioned<byte[]> ac = tree.removeNode(path("root.a.c")).join();
        assertArrayEquals("ac".getBytes(), ac.value());
        assertNull(tree.get(path("root.a.c")).join());

        Versioned<byte[]> a = tree.removeNode(path("root.a")).join();
        assertArrayEquals("a".getBytes(), a.value());
        assertNull(tree.get(path("root.a")).join());

        tree.create(path("root.x"), null).join();
        Versioned<byte[]> x = tree.removeNode(path("root.x")).join();
        assertNull(x.value());
        assertNull(tree.get(path("root.a.x")).join());
    }

    /**
     * Tests invalid removes.
     */
    @Test
    public void testRemoveFailures() throws Throwable {
        AtomixDocumentTree tree = newPrimitive(UUID.randomUUID().toString());
        tree.create(path("root.a"), "a".getBytes()).join();
        tree.create(path("root.a.b"), "ab".getBytes()).join();
        tree.create(path("root.a.c"), "ac".getBytes()).join();

        try {
            tree.removeNode(path("root")).join();
            fail();
        } catch (Exception e) {
            assertTrue(Throwables.getRootCause(e) instanceof IllegalDocumentModificationException);
        }

        try {
            tree.removeNode(path("root.a")).join();
            fail();
        } catch (Exception e) {
            assertTrue(Throwables.getRootCause(e) instanceof IllegalDocumentModificationException);
        }

        try {
            tree.removeNode(path("root.d")).join();
            fail();
        } catch (Exception e) {
            assertTrue(Throwables.getRootCause(e) instanceof NoSuchDocumentPathException);
        }
    }

    /**
     * Tests invalid create.
     */
    @Test
    public void testCreateFailures() throws Throwable {
        AtomixDocumentTree tree = newPrimitive(UUID.randomUUID().toString());
        try {
            tree.create(path("root.a.c"), "ac".getBytes()).join();
            fail();
        } catch (Exception e) {
            assertTrue(Throwables.getRootCause(e) instanceof IllegalDocumentModificationException);
        }
    }

    /**
     * Tests invalid set.
     */
    @Test
    public void testSetFailures() throws Throwable {
        AtomixDocumentTree tree = newPrimitive(UUID.randomUUID().toString());
        try {
            tree.set(path("root.a.c"), "ac".getBytes()).join();
            fail();
        } catch (Exception e) {
            assertTrue(Throwables.getRootCause(e) instanceof IllegalDocumentModificationException);
        }
    }

    /**
     * Tests getChildren.
     */
    @Test
    public void testGetChildren() throws Throwable {
        AtomixDocumentTree tree = newPrimitive(UUID.randomUUID().toString());
        tree.create(path("root.a"), "a".getBytes()).join();
        tree.create(path("root.a.b"), "ab".getBytes()).join();
        tree.create(path("root.a.c"), "ac".getBytes()).join();

        Map<String, Versioned<byte[]>> rootChildren = tree.getChildren(path("root")).join();
        assertEquals(1, rootChildren.size());
        Versioned<byte[]> a = rootChildren.get("a");
        assertArrayEquals("a".getBytes(), a.value());

        Map<String, Versioned<byte[]>> children = tree.getChildren(path("root.a")).join();
        assertEquals(2, children.size());
        Versioned<byte[]> ab = children.get("b");
        assertArrayEquals("ab".getBytes(), ab.value());
        Versioned<byte[]> ac = children.get("c");
        assertArrayEquals("ac".getBytes(), ac.value());

        assertEquals(0, tree.getChildren(path("root.a.b")).join().size());
        assertEquals(0, tree.getChildren(path("root.a.c")).join().size());
    }

    /**
     * Tests destroy.
     */
    @Test
    public void testClear() {
        AtomixDocumentTree tree = newPrimitive(UUID.randomUUID().toString());
        tree.create(path("root.a"), "a".getBytes()).join();
        tree.create(path("root.a.b"), "ab".getBytes()).join();
        tree.create(path("root.a.c"), "ac".getBytes()).join();

        tree.destroy().join();
        assertEquals(0, tree.getChildren(path("root")).join().size());
    }

    /**
     * Tests listeners.
     */
    @Test
    public void testNotifications() throws Exception {
        AtomixDocumentTree tree = newPrimitive(UUID.randomUUID().toString());
        TestEventListener listener = new TestEventListener();

        // add listener; create a node in the tree and verify an CREATED event is received.
        tree.addListener(listener).thenCompose(v -> tree.set(path("root.a"), "a".getBytes())).join();
        DocumentTreeEvent<byte[]> event = listener.event();
        assertEquals(DocumentTreeEvent.Type.CREATED, event.type());
        assertFalse(event.oldValue().isPresent());
        assertArrayEquals("a".getBytes(), event.newValue().get().value());
        // update a node in the tree and verify an UPDATED event is received.
        tree.set(path("root.a"), "newA".getBytes()).join();
        event = listener.event();
        assertEquals(DocumentTreeEvent.Type.UPDATED, event.type());
        assertArrayEquals("newA".getBytes(), event.newValue().get().value());
        assertArrayEquals("a".getBytes(), event.oldValue().get().value());
        // remove a node in the tree and verify an REMOVED event is received.
        tree.removeNode(path("root.a")).join();
        event = listener.event();
        assertEquals(DocumentTreeEvent.Type.DELETED, event.type());
        assertFalse(event.newValue().isPresent());
        assertArrayEquals("newA".getBytes(), event.oldValue().get().value());
        // recursively create a node and verify CREATED events for all intermediate nodes.
        tree.createRecursive(path("root.x.y"), "xy".getBytes()).join();
        event = listener.event();
        assertEquals(DocumentTreeEvent.Type.CREATED, event.type());
        assertEquals(path("root.x"), event.path());
        event = listener.event();
        assertEquals(DocumentTreeEvent.Type.CREATED, event.type());
        assertEquals(path("root.x.y"), event.path());
        assertArrayEquals("xy".getBytes(), event.newValue().get().value());
    }

    @Test
    public void testFilteredNotifications() throws Throwable {
        String treeName = UUID.randomUUID().toString();
        AtomixDocumentTree tree1 = newPrimitive(treeName);
        AtomixDocumentTree tree2 = newPrimitive(treeName);

        TestEventListener listener1a = new TestEventListener(3);
        TestEventListener listener1ab = new TestEventListener(2);
        TestEventListener listener2abc = new TestEventListener(1);

        tree1.addListener(path("root.a"), listener1a).join();
        tree1.addListener(path("root.a.b"), listener1ab).join();
        tree2.addListener(path("root.a.b.c"), listener2abc).join();

        tree1.createRecursive(path("root.a.b.c"), "abc".getBytes()).join();
        DocumentTreeEvent<byte[]> event = listener1a.event();
        assertEquals(path("root.a"), event.path());
        event = listener1a.event();
        assertEquals(path("root.a.b"), event.path());
        event = listener1a.event();
        assertEquals(path("root.a.b.c"), event.path());
        event = listener1ab.event();
        assertEquals(path("root.a.b"), event.path());
        event = listener1ab.event();
        assertEquals(path("root.a.b.c"), event.path());
        event = listener2abc.event();
        assertEquals(path("root.a.b.c"), event.path());
    }

    @Test
    public void testTransaction() throws Throwable {
        String treeName = UUID.randomUUID().toString();
        AtomixDocumentTree tree = newPrimitive(treeName);

        byte[] value1 = "abc".getBytes();
        byte[] value2 = "def".getBytes();

        assertTrue(tree.create(path("root.a"), value1).join());
        assertTrue(tree.create(path("root.b"), value2).join());

        long aVersion = tree.get(path("root.a")).join().version();
        long bVersion = tree.get(path("root.b")).join().version();

        TransactionId transactionId = TransactionId.from("1");
        Version transactionVersion = tree.begin(transactionId).join();
        List<NodeUpdate<byte[]>> records = Arrays.asList(
                NodeUpdate.<byte[]>newBuilder()
                        .withType(NodeUpdate.Type.CREATE_NODE)
                        .withPath(path("root.c"))
                        .withValue(value1)
                        .build(),
                NodeUpdate.<byte[]>newBuilder()
                        .withType(NodeUpdate.Type.UPDATE_NODE)
                        .withPath(path("root.a"))
                        .withValue(value2)
                        .withVersion(aVersion)
                        .build(),
                NodeUpdate.<byte[]>newBuilder()
                        .withType(NodeUpdate.Type.DELETE_NODE)
                        .withPath(path("root.b"))
                        .withVersion(bVersion)
                        .build());
        TransactionLog<NodeUpdate<byte[]>> transactionLog = new TransactionLog<>(
                transactionId,
                transactionVersion.value(),
                records);
        assertTrue(tree.prepare(transactionLog).join());
        tree.commit(transactionId).join();

        assertArrayEquals(value2, tree.get(path("root.a")).join().value());
        assertNull(tree.get(path("root.b")).join());
        assertArrayEquals(value1, tree.get(path("root.c")).join().value());
    }

    private static class TestEventListener implements DocumentTreeListener<byte[]> {

        private final BlockingQueue<DocumentTreeEvent<byte[]>> queue;

        public TestEventListener() {
            this(1);
        }

        public TestEventListener(int maxEvents) {
            queue = new ArrayBlockingQueue<>(maxEvents);
        }

        @Override
        public void event(DocumentTreeEvent<byte[]> event) {

            try {
                queue.put(event);
            } catch (InterruptedException e) {
                Throwables.propagate(e);
            }
        }

        public DocumentTreeEvent<byte[]> event() throws InterruptedException {
            return queue.take();
        }
    }

    private static DocumentPath path(String path) {
        return DocumentPath.from(path.replace(".", DocumentPath.DEFAULT_SEPARATOR));
    }
}
